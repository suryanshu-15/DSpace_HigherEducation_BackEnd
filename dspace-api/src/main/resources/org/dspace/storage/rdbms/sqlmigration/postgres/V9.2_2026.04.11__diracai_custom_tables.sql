-- =============================================================
-- Phase 1 | DSpace 7 | PostgreSQL
-- HED Odisha — Section / Institution lookup tables
-- Run this as your DSpace DB user
-- =============================================================

-- -------------------------------------------------------------
-- 1. BRANCH table  (the 4 top-level options)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hed_branch (
    id          SERIAL PRIMARY KEY,
    code        VARCHAR(20)  NOT NULL UNIQUE,   -- 'admin', 'field', 'college', 'univ'
    label       VARCHAR(100) NOT NULL,
    display_order INT        NOT NULL DEFAULT 0,
    active      BOOLEAN      NOT NULL DEFAULT true
);

INSERT INTO hed_branch (code, label, display_order) VALUES
    ('admin',   'Administrative Establishment', 1),
    ('field',   'Field Establishment',          2),
    ('college', 'College',                      3),
    ('univ',    'University',                   4);


-- -------------------------------------------------------------
-- 2. SECTION_TYPE table  (sub-options for each branch)
--    Replaces the massive <value-pairs> block in input-forms.xml
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hed_section_type (
    id            SERIAL PRIMARY KEY,
    branch_code   VARCHAR(20)  NOT NULL REFERENCES hed_branch(code),
    code          VARCHAR(30)  NOT NULL UNIQUE,
    label         VARCHAR(200) NOT NULL,
    group_name    VARCHAR(100),              -- e.g. 'Offices', 'Finance', 'Cells & Sections'
    has_children  BOOLEAN      NOT NULL DEFAULT false,  -- true = triggers step 3 (RDE regions)
    display_order INT          NOT NULL DEFAULT 0,
    active        BOOLEAN      NOT NULL DEFAULT true
);

-- Administrative Establishment sub-types
INSERT INTO hed_section_type (branch_code, code, label, group_name, display_order) VALUES
    ('admin', 'OM',     'Office of Minister',                  'Offices', 1),
    ('admin', 'OS',     'Office of Secretary',                 'Offices', 2),
    ('admin', 'OSS',    'Office of Special Secretary',         'Offices', 3),
    ('admin', 'OAS',    'Office of Additional Secretary',      'Offices', 4),
    ('admin', 'ACC1',   'Accounts-I',                          'Finance', 5),
    ('admin', 'ACC2',   'Accounts-II',                         'Finance', 6),
    ('admin', 'AUD',    'Audit',                               'Finance', 7),
    ('admin', 'BF',     'Budget & Finance',                    'Finance', 8),
    ('admin', 'LEGAL',  'Legal',                               'Cells & Sections', 9),
    ('admin', 'RTI',    'RTI',                                 'Cells & Sections', 10),
    ('admin', 'CPR',    'Communication & PR',                  'Cells & Sections', 11),
    ('admin', 'IT',     'IT & ET',                             'Cells & Sections', 12),
    ('admin', 'NEP',    'NEP Cell',                            'Cells & Sections', 13),
    ('admin', 'NSS',    'NSS',                                 'Cells & Sections', 14),
    ('admin', 'NCC',    'NCC',                                 'Cells & Sections', 15),
    ('admin', 'SCH',    'Scholarship & Loan',                  'Cells & Sections', 16),
    ('admin', 'TET',    'Teacher Education & Training',        'Cells & Sections', 17),
    ('admin', 'ENG',    'Engineering Cell',                    'Cells & Sections', 18),
    ('admin', 'DRY',    'Diary',                               'Cells & Sections', 19),
    ('admin', 'EIC',    'Edu-Invest Cell',                     'Cells & Sections', 20),
    ('admin', 'PTC',    'Performance Tracking Cell',           'Cells & Sections', 21),
    ('admin', 'PR',     'Permission & Recognition',            'Cells & Sections', 22),
    ('admin', 'SCHEME', 'Scheme',                              'Cells & Sections', 23),
    ('admin', 'OTHERS', 'Others',                              'Cells & Sections', 24);

-- Field Establishment sub-types
INSERT INTO hed_section_type (branch_code, code, label, group_name, has_children, display_order) VALUES
    ('field', 'SET',     'State Education Tribunal (SET)',  'State Bodies', false, 1),
    ('field', 'SSB',     'State Selection Board (SSB)',     'State Bodies', false, 2),
    ('field', 'OSHEC',   'OSHEC',                           'State Bodies', false, 3),
    ('field', 'AGACPS',  'AGACPS',                          'State Bodies', false, 4),
    ('field', 'RDE_BBS', 'RDE – Bhubaneswar',               'Regional Directors of Education', true, 5),
    ('field', 'RDE_BER', 'RDE – Berhampur',                 'Regional Directors of Education', true, 6),
    ('field', 'RDE_SBP', 'RDE – Sambalpur',                 'Regional Directors of Education', true, 7),
    ('field', 'RDE_BAL', 'RDE – Balasore',                  'Regional Directors of Education', true, 8),
    ('field', 'RDE_JEY', 'RDE – Jeypore',                   'Regional Directors of Education', true, 9);

-- College sub-types
INSERT INTO hed_section_type (branch_code, code, label, group_name, display_order) VALUES
    ('college', 'GC',      'Government College',             'By Management', 1),
    ('college', 'NGC_488', 'Non-Govt Aided (488 category)',  'By Management', 2),
    ('college', 'NGC_662', 'Non-Govt Aided (662 category)',  'By Management', 3),
    ('college', 'SC',      'Sanskrit College',               'By Management', 4),
    ('college', 'PVT',     'Private / Un-Aided College',     'By Management', 5),
    ('college', 'GIA',     'GIA Order',                      'Grant Order',   6),
    ('college', 'BLOCK',   'Block Grant Order',              'Grant Order',   7);

-- University sub-types
INSERT INTO hed_section_type (branch_code, code, label, group_name, display_order) VALUES
    ('univ', 'SPU', 'State Public University', 'Type', 1),
    ('univ', 'PU',  'Private University',      'Type', 2),
    ('univ', 'CU',  'Central University',      'Type', 3);


-- -------------------------------------------------------------
-- 3. DISTRICT table
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hed_district (
    id   SERIAL PRIMARY KEY,
    code VARCHAR(20)  NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    display_order INT NOT NULL DEFAULT 0
);

INSERT INTO hed_district (code, name, display_order) VALUES
    ('ANGUL', 'Angul',      1),
('BALANGIR', 'Balangir',   2),
('BALASORE', 'Balasore',   3),
('BARGARH', 'Bargarh',    4),
('BHADRAK', 'Bhadrak',    5),
('BOUDH', 'Boudh',      6),
('CUTTACK', 'Cuttack',    7),
('DEOGARH', 'Deogarh',    8),
('DHENKANAL','Dhenkanal',  9),
('GAJAPATI', 'Gajapati',   10),
('GANJAM', 'Ganjam',     11),
('JAGATSINGHPUR', 'Jagatsinghpur', 12),
('JAJPUR', 'Jajpur',     13),
('JHARSUGUDA', 'Jharsuguda', 14),
('KALAHANDI', 'Kalahandi',  15),
('KANDHAMAL', 'Kandhamal',  16),
('KENDRAPARA', 'Kendrapara', 17),
('KEONJHAR', 'Keonjhar',   18),
('KHURDA', 'Khurda',     19),
('KORAPUT', 'Koraput',    20),
('MALKANGIRI', 'Malkangiri', 21),
('MAYURBHANJ', 'Mayurbhanj', 22),
('NABARANGPUR', 'Nabarangpur',23),
('NAYAGARH', 'Nayagarh',   24),
('NUAPADA', 'Nuapada',    25),
('PURI', 'Puri',       26),
('RAYAGADA', 'Rayagada',   27),
('SAMBALPUR', 'Sambalpur',  28),
('SUBARNAPUR', 'Subarnapur', 29),
('SUNDARGARH', 'Sundargarh', 30);


-- -------------------------------------------------------------
-- 4. INSTITUTION_LOOKUP table
--    Holds all 600+ colleges and universities (Annexure B–H)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hed_institution (
    id            SERIAL PRIMARY KEY,
    annexure      VARCHAR(5)   NOT NULL,         -- 'B','C','D','E','F','G','H'
    branch_code   VARCHAR(20)  NOT NULL REFERENCES hed_branch(code),
    sub_type_code VARCHAR(30)  NOT NULL REFERENCES hed_section_type(code),
    district_code VARCHAR(20)  REFERENCES hed_district(code),
    name          TEXT         NOT NULL,
    short_name    VARCHAR(200),
    code          VARCHAR(30)  NOT NULL UNIQUE, 
    estd_year     INT,
    active        BOOLEAN      NOT NULL DEFAULT true,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Enable trigram extension
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Then your tables / indexes
CREATE INDEX idx_name_trgm
ON hed_institution
USING gin (name gin_trgm_ops);

-- Indexes for fast autocomplete queries
CREATE INDEX idx_hed_institution_branch    ON hed_institution(branch_code);
CREATE INDEX idx_hed_institution_subtype   ON hed_institution(sub_type_code);
CREATE INDEX idx_hed_institution_district  ON hed_institution(district_code);
CREATE INDEX idx_hed_institution_annexure  ON hed_institution(annexure);
CREATE INDEX idx_hed_institution_name_trgm ON hed_institution USING gin (name gin_trgm_ops);
-- ^ trigram index enables fast ILIKE '%search%' queries for autocomplete

-- Enable the trigram extension (run once per DB)
-- If you do not have superuser rights, ask your DBA to run this:
-- CREATE EXTENSION IF NOT EXISTS pg_trgm;


-- -------------------------------------------------------------
-- 5. SAMPLE seed data — Annexure-B (Government Colleges)
--    Replace / extend with your actual college list
-- -------------------------------------------------------------
-- INSERT INTO hed_institution (annexure, branch_code, sub_type_code, district_code, name, short_name, code) VALUES
--     ('B', 'college', 'GC', 'KHD', 'Ravenshaw University',                      'Ravenshaw',     'GC_KHD_001'),
--     ('B', 'college', 'GC', 'KHD', 'Utkal University',                          'Utkal Univ',    'GC_KHD_002'),
--     ('B', 'college', 'GC', 'CTC', 'Ravenshaw College, Cuttack',                'Ravenshaw CTC', 'GC_CTC_001'),
--     ('B', 'college', 'GC', 'GNJ', 'Berhampur University',                      'Berhampur',     'GC_GNJ_001'),
--     ('B', 'college', 'GC', 'SBR', 'Sambalpur University',                      'Sambalpur',     'GC_SBR_001'),
--     ('B', 'college', 'GC', 'MYB', 'Baripada College',                          'Baripada',      'GC_MYB_001'),
--     ('C', 'college', 'NGC_488', 'KHD', 'Stewart Science College',              'Stewart',       'NGC488_KHD_001'),
--     ('C', 'college', 'NGC_488', 'CTC', 'Ladies College, Cuttack',              'Ladies CTC',    'NGC488_CTC_001'),
--     ('E', 'college', 'SC',  'KHD', 'Sanskrit College, Bhubaneswar',            'Sanskrit BBSR', 'SC_KHD_001'),
--     ('G', 'univ',    'SPU', NULL,  'Utkal University',                         'Utkal',         'SPU_001'),
--     ('G', 'univ',    'SPU', NULL,  'Berhampur University',                     'Berhampur',     'SPU_002'),
--     ('G', 'univ',    'SPU', NULL,  'Sambalpur University',                     'Sambalpur',     'SPU_003'),
--     ('G', 'univ',    'SPU', NULL,  'Fakir Mohan University',                   'FM Univ',       'SPU_004'),
--     ('G', 'univ',    'SPU', NULL,  'North Orissa University',                  'NOU',           'SPU_005'),
--     ('H', 'univ',    'PU',  NULL,  'KIIT Deemed University',                   'KIIT',          'PU_001'),
--     ('H', 'univ',    'PU',  NULL,  'SOA University',                           'SOA',           'PU_002');


-- -------------------------------------------------------------
-- 6. Updated_at auto-trigger
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_hed_institution_updated_at
    BEFORE UPDATE ON hed_institution
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();