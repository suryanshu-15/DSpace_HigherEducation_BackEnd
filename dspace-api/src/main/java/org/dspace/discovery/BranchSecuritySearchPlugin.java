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
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.service.GroupService;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Solr search restriction plugin that enforces branch-based visibility using
 * DSpace EPerson Groups.
 *
 * Each branch has a dedicated DSpace group. The group UUID is mapped to a
 * branch code in dspace.cfg:
 *
 * diracai.branch.group.college = <UUID>
 * diracai.branch.group.field = <UUID>
 * diracai.branch.group.admin = <UUID>
 *
 * At search time: - Admin user → no filter, sees everything - User in College
 * group → filter: dc.branch_lower:"college" - User in Field group → filter:
 * dc.branch_lower:"field" - User in no branch group → no filter (resource
 * policies apply)
 */
public class BranchSecuritySearchPlugin implements SolrServiceSearchPlugin {

    private static final Logger log = LogManager.getLogger(BranchSecuritySearchPlugin.class);

    /**
     * Prefix used to look up branch group mappings in dspace.cfg. Full key
     * format: diracai.branch.group.<branchCode>
     * Example: diracai.branch.group.college = <UUID>
     */
    private static final String CONFIG_PREFIX = "diracai.branch.group.";

    /**
     * Solr field indexed by BranchSecurityIndexingPlugin (lowercased). Used for
     * case-insensitive filtering.
     */
    private static final String BRANCH_FIELD_LOWER = BranchSecurityIndexingPlugin.BRANCH_FIELD_LOWER;

    /**
     * All possible branch codes — must match keys in dspace.cfg. Add more here
     * if new branches are added.
     */
    private static final String[] BRANCH_CODES = {
        "college",
        "field",
        "admin",
        "university"
    };

    @Autowired
    private AuthorizeService authorizeService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ConfigurationService configurationService;

    @Override
    public void additionalSearchParameters(Context context,
            DiscoverQuery discoveryQuery,
            SolrQuery solrQuery) {
        log.info("########## BranchSecuritySearchPlugin CALLED ##########");

        try {
            // ── 1. Global admins see everything ───────────────────────────
            if (authorizeService.isAdmin(context)) {
                log.debug("BranchSecuritySearchPlugin: admin user — no filter applied");
                return;
            }

            EPerson currentUser = context.getCurrentUser();
            if (currentUser == null) {
                log.debug("BranchSecuritySearchPlugin: anonymous user — no filter applied");
                return;
            }

            // ── 2. Find which branch group this user belongs to ───────────
            String userBranchCode = resolveBranchCodeForUser(context, currentUser);

            if (StringUtils.isNotBlank(userBranchCode)) {
                // ── 3. Inject Solr filter for this branch ─────────────────
                String escaped = userBranchCode.trim().toLowerCase()
                        .replace("\"", "\\\"");
                String branchFilter = BRANCH_FIELD_LOWER + ":\"" + escaped + "\"";
                solrQuery.addFilterQuery(branchFilter);

                log.info("BranchSecuritySearchPlugin: user=[{}] group-branch=[{}] filter=[{}]",
                        currentUser.getEmail(), userBranchCode, branchFilter);
            } else {
                // User is not in any branch group — no branch filter applied.
                // Standard resource policies still control what they can see.
                log.warn("BranchSecuritySearchPlugin: user=[{}] is not in any branch group "
                        + "— no branch filter applied",
                        currentUser.getEmail());
            }

        } catch (SQLException e) {
            log.error("BranchSecuritySearchPlugin: SQL error applying branch filter", e);
        }
    }

    /**
     * Iterates over all configured branch codes, looks up each group UUID from
     * dspace.cfg, and checks if the current user is a member.
     *
     * Returns the first matching branch code, or null if user is in no branch
     * group.
     *
     * @param context DSpace context
     * @param currentUser the logged-in EPerson
     * @return branch code string (e.g. "college") or null
     */
    private String resolveBranchCodeForUser(Context context,
            EPerson currentUser) throws SQLException {

        for (String branchCode : BRANCH_CODES) {

            String configKey = CONFIG_PREFIX + branchCode;
            String groupName = configurationService.getProperty(configKey);

            if (StringUtils.isBlank(groupName)) {
                log.debug("No config found for key [{}]", configKey);
                continue;
            }

            Group branchGroup = groupService.findByName(context, groupName.trim());

            if (branchGroup == null) {
                log.warn("Group not found for name [{}] (key={})", groupName, configKey);
                continue;
            }

            if (groupService.isMember(context, currentUser, branchGroup)) {
                log.debug("User [{}] belongs to group [{}]",
                        currentUser.getEmail(), groupName);
                return branchCode;
            }
        }

        return null;
    }
}
