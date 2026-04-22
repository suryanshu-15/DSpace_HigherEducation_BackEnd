-- Add parent_code to hed_section_type to support Combination A nested tree
ALTER TABLE hed_section_type ADD COLUMN parent_code VARCHAR(30) REFERENCES hed_section_type(code);

-- Let's make sure the root elements have null parent_code
UPDATE hed_section_type SET parent_code = NULL;

-- Replace College existing children with a single entry
INSERT INTO hed_section_type (branch_code, code, label, group_name, has_children, display_order)
VALUES ('college', 'NGC', 'Non-Government Aided College', 'By Management', false, 2);

-- Update existing NGC_488 and NGC_662 so they don't show up in the branch-level dropdown anymore
UPDATE hed_section_type 
SET active = false 
WHERE code IN ('NGC_488', 'NGC_662') AND branch_code = 'college';

-- RDE_BBS
INSERT INTO hed_section_type (branch_code, code, label, parent_code, has_children, display_order) VALUES
('field', 'RDE_BBS_GC', 'Government College', 'RDE_BBS', false, 1),
('field', 'RDE_BBS_NGC', 'Non-Government Aided College', 'RDE_BBS', true, 2),
('field', 'RDE_BBS_NGC_488', 'GIA Order', 'RDE_BBS_NGC', false, 1),
('field', 'RDE_BBS_NGC_662', 'Block Grant Order', 'RDE_BBS_NGC', false, 2),
('field', 'RDE_BBS_SC', 'Sanskrit College', 'RDE_BBS', false, 3),
('field', 'RDE_BBS_PVT', 'Un-Aided / Private College', 'RDE_BBS', false, 4);

-- RDE_BER
INSERT INTO hed_section_type (branch_code, code, label, parent_code, has_children, display_order) VALUES
('field', 'RDE_BER_GC', 'Government College', 'RDE_BER', false, 1),
('field', 'RDE_BER_NGC', 'Non-Government Aided College', 'RDE_BER', true, 2),
('field', 'RDE_BER_NGC_488', 'GIA Order', 'RDE_BER_NGC', false, 1),
('field', 'RDE_BER_NGC_662', 'Block Grant Order', 'RDE_BER_NGC', false, 2),
('field', 'RDE_BER_SC', 'Sanskrit College', 'RDE_BER', false, 3),
('field', 'RDE_BER_PVT', 'Un-Aided / Private College', 'RDE_BER', false, 4);

-- RDE_SBP
INSERT INTO hed_section_type (branch_code, code, label, parent_code, has_children, display_order) VALUES
('field', 'RDE_SBP_GC', 'Government College', 'RDE_SBP', false, 1),
('field', 'RDE_SBP_NGC', 'Non-Government Aided College', 'RDE_SBP', true, 2),
('field', 'RDE_SBP_NGC_488', 'GIA Order', 'RDE_SBP_NGC', false, 1),
('field', 'RDE_SBP_NGC_662', 'Block Grant Order', 'RDE_SBP_NGC', false, 2),
('field', 'RDE_SBP_SC', 'Sanskrit College', 'RDE_SBP', false, 3),
('field', 'RDE_SBP_PVT', 'Un-Aided / Private College', 'RDE_SBP', false, 4);

-- RDE_BAL
INSERT INTO hed_section_type (branch_code, code, label, parent_code, has_children, display_order) VALUES
('field', 'RDE_BAL_GC', 'Government College', 'RDE_BAL', false, 1),
('field', 'RDE_BAL_NGC', 'Non-Government Aided College', 'RDE_BAL', true, 2),
('field', 'RDE_BAL_NGC_488', 'GIA Order', 'RDE_BAL_NGC', false, 1),
('field', 'RDE_BAL_NGC_662', 'Block Grant Order', 'RDE_BAL_NGC', false, 2),
('field', 'RDE_BAL_SC', 'Sanskrit College', 'RDE_BAL', false, 3),
('field', 'RDE_BAL_PVT', 'Un-Aided / Private College', 'RDE_BAL', false, 4);

-- RDE_JEY
INSERT INTO hed_section_type (branch_code, code, label, parent_code, has_children, display_order) VALUES
('field', 'RDE_JEY_GC', 'Government College', 'RDE_JEY', false, 1),
('field', 'RDE_JEY_NGC', 'Non-Government Aided College', 'RDE_JEY', true, 2),
('field', 'RDE_JEY_NGC_488', 'GIA Order', 'RDE_JEY_NGC', false, 1),
('field', 'RDE_JEY_NGC_662', 'Block Grant Order', 'RDE_JEY_NGC', false, 2),
('field', 'RDE_JEY_SC', 'Sanskrit College', 'RDE_JEY', false, 3),
('field', 'RDE_JEY_PVT', 'Un-Aided / Private College', 'RDE_JEY', false, 4);
