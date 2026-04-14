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
 * Solr indexing plugin that indexes dc.branch from each item into two fields:
 *
 *   dc.branch_keyword  — original value as stored  (e.g. "college")
 *   dc.branch_lower    — lowercased value           (e.g. "college")
 *
 * The search plugin (BranchSecuritySearchPlugin) uses dc.branch_lower
 * for case-insensitive group-based filtering.
 *
 * IMPORTANT: dc.branch on items must store the branch SHORT CODE
 * (e.g. "college", "field", "admin") — not the full label.
 * This must match the branch codes configured in dspace.cfg:
 *   diracai.branch.group.college = <UUID>
 *   diracai.branch.group.field   = <UUID>
 *   diracai.branch.group.admin   = <UUID>
 */
public class BranchSecurityIndexingPlugin implements SolrServiceIndexPlugin {

    /** Original-case Solr field */
    public static final String BRANCH_FIELD = "dc.branch_keyword";

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

        // Read dc.branch from item metadata
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

                document.addField(BRANCH_FIELD,       original);
                document.addField(BRANCH_FIELD_LOWER, lowercase);
            }
        }
    }
}