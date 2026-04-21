INSERT INTO metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note)
SELECT * FROM (
    VALUES
    (1, 'barcode', NULL, 'Barcode Number'),
    (1, 'filenumber', NULL, 'File Number'),
    (1, 'file', 'name', 'File Name'),
    (1, 'file', 'year', 'File Year'),
    (1, 'case', 'nature', 'Case Nature'),
    (1, 'case', 'number', 'Case Number'),
    (1, 'case', 'status', 'Status of the case (e.g., pending, approved, rejected)'),
    (1, 'case', 'matter', 'Case Matter'),
    (1, 'case', 'type', 'Case Type'),
    (1, 'case', 'district', 'Case District'),
    (1, 'case', 'institution', 'Case Institution'),
    (1, 'petitioner', NULL, 'Name of the Petitioner'),
    (1, 'respondent', NULL, 'Respondent Name'),
    (1, 'district', NULL, 'Name of the District'),
    (1, 'institution', NULL, 'Name of the Institution'),
    (1, 'branch', NULL, 'Branch selected from institution selector'),
    (1, 'subtype', NULL, 'Sub-type selected from institution selector'),
    (1, 'display', NULL, 'Human-readable display value from institution selector'),
    (1, 'combined', NULL, 'Machine-readable stored value from institution selector'),
    (2, 'branch', NULL, 'Branch assigned to this EPerson for access control'),
    (1, 'comment', NULL, 'Detailed comment or note')
) AS v(metadata_schema_id, element, qualifier, scope_note)
WHERE NOT EXISTS (
    SELECT 1 FROM metadatafieldregistry m
    WHERE m.metadata_schema_id = v.metadata_schema_id
      AND m.element = v.element
      AND COALESCE(m.qualifier, '') = COALESCE(v.qualifier, '')
);