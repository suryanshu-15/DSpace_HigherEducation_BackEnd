/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.discovery;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.discovery.indexobject.IndexableItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Solr indexing plugin that indexes the dc.branch metadata field on items.
 *
 * Indexes TWO fields per item:
 *   dc.branch_keyword  — original value (exact match)
 *   dc.branch_lower    — lowercased value (case-insensitive match)
 *
 * The search plugin uses dc.branch_lower so that "College" and "college"
 * both match a user whose eperson.branch = "college".
 */
public class BranchSecurityIndexingPlugin implements SolrServiceIndexPlugin {

    /** Original-case Solr field */
    public static final String BRANCH_FIELD       = "dc.branch_keyword";

    /** Lowercased Solr field — used by BranchSecuritySearchPlugin */
    public static final String BRANCH_FIELD_LOWER = "dc.branch_lower";

    @Autowired
    private ItemService itemService;

    @Override
    public void additionalIndex(Context context,
                                IndexableObject indexableObject,
                                SolrInputDocument document) {

        if (!(indexableObject instanceof IndexableItem)) {
            return;
        }

        Item item = ((IndexableItem) indexableObject).getIndexedObject();
        if (item == null) {
            return;
        }

        List<MetadataValue> branchValues = itemService.getMetadata(
            item, "dc", "branch", null, Item.ANY
        );

        if (branchValues == null || branchValues.isEmpty()) {
            return;
        }

        for (MetadataValue mv : branchValues) {
            if (mv.getValue() != null && !mv.getValue().isEmpty()) {
                String original  = mv.getValue().trim();
                String lowercase = original.toLowerCase();

                // Index both — original for display/reference, lower for filtering
                document.addField(BRANCH_FIELD,       original);
                document.addField(BRANCH_FIELD_LOWER, lowercase);
            }
        }
    }
}