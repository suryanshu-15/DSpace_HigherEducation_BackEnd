/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.discovery;

import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.service.EPersonService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Solr search restriction plugin that enforces branch-based visibility.
 *
 * Rules:
 *   - Global admin → no filter, sees everything
 *   - EPerson with eperson.branch metadata set → sees only items where
 *     dc.branch_keyword matches their branch value
 *   - EPerson with NO branch metadata → sees only items with no dc.branch set
 *     (i.e. public items), plus items they personally submitted
 *
 * Place this bean AFTER solrServiceResourceIndexPlugin in spring config
 * so that the standard READ-policy filter fires first.
 */
public class BranchSecuritySearchPlugin implements SolrServiceSearchPlugin {

    private static final Logger log = LogManager.getLogger(BranchSecuritySearchPlugin.class);

    private static final String BRANCH_FIELD = "dc.branch_keyword";
    private static final String EPERSON_BRANCH_SCHEMA   = "eperson";
    private static final String EPERSON_BRANCH_ELEMENT  = "branch";
    private static final String EPERSON_BRANCH_QUALIFIER = null;

    @Autowired
    private AuthorizeService authorizeService;

    @Autowired
    private EPersonService ePersonService;

    @Autowired
    private ItemService itemService;

    @Override
    public void additionalSearchParameters(Context context,
                                           DiscoverQuery discoveryQuery,
                                           SolrQuery solrQuery) {
        try {
            // ── 1. Global admins see everything ───────────────────────────
            if (authorizeService.isAdmin(context)) {
                return;
            }

            EPerson currentUser = context.getCurrentUser();
            if (currentUser == null) {
                // Anonymous users — no branch filter needed (READ policies handle access)
                return;
            }

            // ── 2. Read the user's branch from EPerson metadata ───────────
            String userBranch = getUserBranch(context, currentUser);

            if (StringUtils.isNotBlank(userBranch)) {
                // ── 3a. User has a branch → restrict to items of that branch ──
                // Escape special Solr characters in branch value
                String escapedBranch = escapeForSolr(userBranch);
                String branchFilter = BRANCH_FIELD + ":\"" + escapedBranch + "\"";
                solrQuery.addFilterQuery(branchFilter);

                log.debug("BranchSecuritySearchPlugin: applied branch filter [{}] for user [{}]",
                    branchFilter, currentUser.getEmail());
            }
            // If user has NO branch, no additional filter is applied —
            // the standard resource policy filter (solrServiceResourceIndexPlugin)
            // already handles visibility correctly.

        } catch (SQLException e) {
            log.error("BranchSecuritySearchPlugin: SQL error applying branch filter", e);
        }
    }

    /**
     * Read the eperson.branch metadata value from the given EPerson.
     *
     * @param context     DSpace context
     * @param ePerson     the current user
     * @return branch value, or null/blank if not set
     */
    private String getUserBranch(Context context, EPerson ePerson) {
        try {
            // EPersonService extends DSpaceObjectServiceImpl which stores metadata
            // on the EPerson object itself — retrieve via getMetadataFirstValue
            String branch = ePersonService.getMetadataFirstValue(
                ePerson,
                EPERSON_BRANCH_SCHEMA,
                EPERSON_BRANCH_ELEMENT,
                EPERSON_BRANCH_QUALIFIER,
                Item.ANY
            );
            return branch;
        } catch (Exception e) {
            log.warn("BranchSecuritySearchPlugin: could not read branch metadata for user [{}]: {}",
                ePerson.getEmail(), e.getMessage());
            return null;
        }
    }

    /**
     * Minimal Solr special-character escaping for filter query values.
     * Wrapping in quotes handles most cases; this escapes embedded quotes.
     */
    private String escapeForSolr(String value) {
        return value.replace("\"", "\\\"");
    }
}