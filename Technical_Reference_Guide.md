# DSpace HED System - Technical Reference Guide

**Version 1.0 | April 2026**

---

## Quick Reference Index

1. [Metadata Field Registration](#metadata-field-registration)
2. [Database Table Schemas](#database-table-schemas)
3. [REST API Endpoints](#rest-api-endpoints)
4. [Configuration Files](#configuration-files)
5. [Code Snippets & Examples](#code-snippets--examples)
6. [Troubleshooting Guide](#troubleshooting-guide)

---

## Metadata Field Registration

### Full Metadata Field List

| Field Name | Element | Qualifier | Mandatory | Multiple | Type |
|---|---|---|---|---|---|
| Barcode Number | dc | barcode | No | No | String |
| File Number | dc | filenumber | Yes | Yes | String |
| File Name | dc | file | name | Yes | Yes | String |
| File Year | dc | file | year | Yes | No | Integer |
| Case Status | dc | case | status | Yes | No | String |
| Case Nature | dc | case | nature | No | No | String |
| Case Number | dc | case | number | Yes | Yes | String |
| Subject Matter | dc | subject | matter | Yes | No | String |
| Description | dc | comment | - | No | No | Text |
| Petitioner Name | dc | petitioner | - | No | Yes | String |
| District Name | dc | district | - | No | No | String |
| Institution Name | dc | institution | - | No | No | String |
| Respondent Name | dc | respondent | - | No | Yes | String |

### SQL: Metadata Field Registration

```sql
-- File: V9.1_2026.04.11__diracai_custom_data.sql
INSERT INTO metadatafieldregistry (metadata_schema_id, element, qualifier, scope_note)
VALUES
  (1, 'barcode', NULL, 'Barcode Number for archival identification'),
  (1, 'filenumber', NULL, 'Government-assigned file identifier'),
  (1, 'file', 'name', 'Official file name or title'),
  (1, 'file', 'year', 'Year of file creation'),
  (1, 'case', 'status', 'Current case status (Active/Closed/Pending)'),
  (1, 'case', 'nature', 'Type of case (Academic/Disciplinary/Legal)'),
  (1, 'case', 'number', 'Official case reference number'),
  (1, 'subject', 'matter', 'Subject matter classification'),
  (1, 'comment', NULL, 'Additional description or remarks'),
  (1, 'petitioner', NULL, 'Name of petitioner/complainant'),
  (1, 'district', NULL, 'Geographic district name'),
  (1, 'institution', NULL, 'Educational institution name'),
  (1, 'respondent', NULL, 'Name of respondent/defendant');
```

### XML: Dublin Core Types Configuration

```xml
<!-- File: dublin-core-types.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<dublin-core-types>
  <!-- File Metadata -->
  <dc-type>
    <schema>dc</schema>
    <element>barcode</element>
    <scope_note>Unique barcode identifier assigned during archival processing</scope_note>
  </dc-type>

  <dc-type>
    <schema>dc</schema>
    <element>file</element>
    <qualifier>name</qualifier>
    <scope_note>Official name or title of the file</scope_note>
  </dc-type>

  <dc-type>
    <schema>dc</schema>
    <element>file</element>
    <qualifier>year</qualifier>
    <scope_note>Year of file creation or initiation</scope_note>
  </dc-type>

  <!-- Case Metadata -->
  <dc-type>
    <schema>dc</schema>
    <element>case</element>
    <qualifier>status</qualifier>
    <scope_note>Current case status: Active, Closed, or Pending</scope_note>
  </dc-type>

  <dc-type>
    <schema>dc</schema>
    <element>case</element>
    <qualifier>nature</qualifier>
    <scope_note>Classification of case nature</scope_note>
  </dc-type>

  <dc-type>
    <schema>dc</schema>
    <element>case</element>
    <qualifier>number</qualifier>
    <scope_note>Official case reference number</scope_note>
  </dc-type>

  <!-- Subject & Parties -->
  <dc-type>
    <schema>dc</schema>
    <element>subject</element>
    <qualifier>matter</qualifier>
    <scope_note>Subject matter of the document</scope_note>
  </dc-type>

  <dc-type>
    <schema>dc</schema>
    <element>petitioner</element>
    <scope_note>Name of the petitioner or complainant</scope_note>
  </dc-type>

  <dc-type>
    <schema>dc</schema>
    <element>respondent</element>
    <scope_note>Name of the respondent or defendant</scope_note>
  </dc-type>

  <!-- Location & Organization -->
  <dc-type>
    <schema>dc</schema>
    <element>district</element>
    <scope_note>Geographic district name</scope_note>
  </dc-type>

  <dc-type>
    <schema>dc</schema>
    <element>institution</element>
    <scope_note>Name of the educational institution</scope_note>
  </dc-type>

  <dc-type>
    <schema>dc</schema>
    <element>comment</element>
    <scope_note>Additional description or remarks</scope_note>
  </dc-type>
</dublin-core-types>
```

### Solr Schema Configuration

```xml
<!-- File: solr/search/conf/schema.xml -->
<!-- Add within <fields> section -->

<!-- Identifier Fields -->
<field name="dc.barcode" type="string" indexed="true" stored="true" multiValued="false"/>
<field name="dc.filenumber" type="string" indexed="true" stored="true" multiValued="true"/>

<!-- File Metadata -->
<field name="dc.file.name" type="text_general" indexed="true" stored="true" multiValued="true"/>
<field name="dc.file.year" type="pint" indexed="true" stored="true" multiValued="false"/>

<!-- Case Information -->
<field name="dc.case.status" type="string" indexed="true" stored="true" multiValued="false"/>
<field name="dc.case.nature" type="string" indexed="true" stored="true" multiValued="false"/>
<field name="dc.case.number" type="string" indexed="true" stored="true" multiValued="true"/>

<!-- Content -->
<field name="dc.subject.matter" type="text_general" indexed="true" stored="true" multiValued="false"/>
<field name="dc.comment" type="text_general" indexed="true" stored="true" multiValued="false"/>

<!-- Parties -->
<field name="dc.petitioner" type="text_general" indexed="true" stored="true" multiValued="true"/>
<field name="dc.respondent" type="text_general" indexed="true" stored="true" multiValued="true"/>

<!-- Location & Organization -->
<field name="dc.district" type="string" indexed="true" stored="true" multiValued="false"/>
<field name="dc.institution" type="string" indexed="true" stored="true" multiValued="false"/>
```

---

## Database Table Schemas

### hed_branch (Organizational Units)

```sql
CREATE TABLE IF NOT EXISTS hed_branch (
    branch_id SERIAL PRIMARY KEY,
    branch_name VARCHAR(255) NOT NULL UNIQUE,
    section_type_id INTEGER NOT NULL REFERENCES hed_section_type(section_type_id) ON DELETE RESTRICT,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_branch_section_type FOREIGN KEY (section_type_id) 
        REFERENCES hed_section_type(section_type_id)
);

CREATE INDEX idx_branch_section ON hed_branch(section_type_id);
CREATE INDEX idx_branch_active ON hed_branch(is_active);
CREATE INDEX idx_branch_name ON hed_branch(branch_name);
```

### hed_section_type (Taxonomy)

```sql
CREATE TABLE IF NOT EXISTS hed_section_type (
    section_type_id SERIAL PRIMARY KEY,
    type_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true
);

INSERT INTO hed_section_type (type_name, description, display_order, is_active)
VALUES
    ('Academic Department', 'College or school academic unit', 1, true),
    ('Administrative Office', 'Administrative or support unit', 2, true),
    ('Examination Board', 'Examination conducting body', 3, true),
    ('Affiliation Council', 'Affiliation review and approval body', 4, true),
    ('Student Services', 'Student support and services division', 5, true);

CREATE INDEX idx_section_type_active ON hed_section_type(is_active);
```

### hed_district (Geographic Regions)

```sql
CREATE TABLE IF NOT EXISTS hed_district (
    district_id SERIAL PRIMARY KEY,
    district_name VARCHAR(255) NOT NULL UNIQUE,
    state_code VARCHAR(2),
    region_code VARCHAR(10),
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_state_code CHECK (state_code IS NULL OR LENGTH(state_code) = 2)
);

-- Sample data
INSERT INTO hed_district (district_name, state_code, region_code, description)
VALUES
    ('Bhubaneswar', 'OD', 'BR-01', 'State capital district'),
    ('Cuttack', 'OD', 'BR-02', 'Historical district'),
    ('Rourkela', 'OD', 'BR-03', 'Industrial city district'),
    ('Sambalpur', 'OD', 'BR-04', 'Western Odisha');

CREATE INDEX idx_district_code ON hed_district(state_code);
CREATE INDEX idx_district_active ON hed_district(is_active);
```

### hed_institution (Educational Institutions)

```sql
CREATE TABLE IF NOT EXISTS hed_institution (
    institution_id SERIAL PRIMARY KEY,
    institution_name VARCHAR(255) NOT NULL,
    institution_code VARCHAR(50),
    district_id INTEGER NOT NULL REFERENCES hed_district(district_id) ON DELETE RESTRICT,
    institution_type VARCHAR(100),
    affiliation_code VARCHAR(50),
    website_url VARCHAR(255),
    contact_email VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_institution_district FOREIGN KEY (district_id) 
        REFERENCES hed_district(district_id),
    CONSTRAINT unique_institution UNIQUE(institution_name, district_id)
);

CREATE INDEX idx_institution_district ON hed_institution(district_id);
CREATE INDEX idx_institution_code ON hed_institution(institution_code);
CREATE INDEX idx_institution_type ON hed_institution(institution_type);
CREATE INDEX idx_institution_active ON hed_institution(is_active);
CREATE INDEX idx_institution_affiliation ON hed_institution(affiliation_code);
```

### dspace_user_branch (Access Control Mapping)

```sql
CREATE TABLE IF NOT EXISTS dspace_user_branch (
    id SERIAL PRIMARY KEY,
    eperson_id INTEGER NOT NULL REFERENCES eperson(eperson_id) ON DELETE CASCADE,
    branch_id INTEGER NOT NULL REFERENCES hed_branch(branch_id) ON DELETE CASCADE,
    assigned_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by INTEGER REFERENCES eperson(eperson_id),
    CONSTRAINT unique_user_branch UNIQUE(eperson_id, branch_id)
);

CREATE INDEX idx_user_branch_person ON dspace_user_branch(eperson_id);
CREATE INDEX idx_user_branch_branch ON dspace_user_branch(branch_id);
```

---

## REST API Endpoints

### Institution Management

```
GET /api/hed/institutions
Query Parameters:
  - districtId (optional): Filter by district ID
  - sectionType (optional): Filter by section type
  - page (default: 0): Pagination offset
  - size (default: 50): Page size

Response:
{
  "data": [
    {
      "id": 1,
      "name": "Government College",
      "code": "GC001",
      "districtId": 1,
      "type": "Educational Institution",
      "affiliationCode": "OU-2020-001"
    }
  ],
  "total": 10,
  "status": "success"
}
```

```
GET /api/hed/districts
Response:
{
  "data": [
    {
      "id": 1,
      "name": "Bhubaneswar",
      "stateCode": "OD",
      "regionCode": "BR-01"
    }
  ],
  "status": "success"
}
```

```
GET /api/hed/branches
Query Parameters:
  - sectionTypeId (optional): Filter by section type
  - isActive (default: true): Include inactive branches

Response:
{
  "data": [
    {
      "id": 1,
      "name": "Academic Affairs",
      "sectionTypeId": 1,
      "description": "Academic management department"
    }
  ],
  "status": "success"
}
```

---

## Configuration Files

### application.properties

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/dspace
spring.datasource.username=dspace_admin
spring.datasource.password=secure_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL10Dialect
spring.jpa.show-sql=false

# Flyway Migration
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
spring.flyway.schemas=public
spring.flyway.locations=classpath:db/migration

# Solr Configuration
solr.server.url=http://localhost:8983/solr
solr.default.core=search
solr.connection.timeout=30000

# Logging
logging.level.org.dspace=INFO
logging.level.org.springframework.web=DEBUG
logging.file=logs/dspace.log

# HED System Specific
hed.access-control.enabled=true
hed.form-validation.strict=true
hed.ocr.enabled=true
```

### dspace.cfg

```
# DSpace Configuration File

# Basic DSpace Settings
dspace.dir=/dspace
dspace.baseUrl=http://localhost:8080/xmlui

# Database Configuration
db.driver=org.postgresql.Driver
db.url=jdbc:postgresql://localhost:5432/dspace
db.username=dspace_admin
db.password=secure_password

# Solr Configuration
solr.server=http://localhost:8983/solr

# Metadata Configuration
metadata.fieldregistry.cache.enabled=true
metadata.validate.input=true

# Authentication
authentication.method=password
authentication.implicitlogin=false

# CORS Configuration
cors.allowed-origins=http://localhost:3000,http://localhost:4200
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
cors.allowed-headers=Content-Type,Authorization,X-Requested-With
cors.allow-credentials=true
```

---

## Code Snippets & Examples

### Java: HedInstitutionController

```java
package org.dspace.app.rest.hed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/hed")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HedInstitutionController {

    @Autowired
    private HedInstitutionService hedInstitutionService;

    @GetMapping("/institutions")
    public ResponseEntity<?> getInstitutions(
            @RequestParam(required = false) Integer districtId,
            @RequestParam(required = false) String sectionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        try {
            List<HedInstitutionDTO> institutions = 
                hedInstitutionService.getInstitutionsByFilters(
                    districtId, sectionType, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", institutions);
            response.put("total", institutions.size());
            response.put("status", "success");
            response.put("timestamp", new Date());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", e.getMessage(),
                    "status", "error",
                    "timestamp", new Date()
                ));
        }
    }

    @GetMapping("/districts")
    public ResponseEntity<?> getDistricts() {
        try {
            List<HedDistrictDTO> districts = 
                hedInstitutionService.getAllDistricts();
            return ResponseEntity.ok(Map.of(
                "data", districts,
                "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/branches")
    public ResponseEntity<?> getBranches(
            @RequestParam(required = false) Integer sectionTypeId) {
        try {
            List<HedBranchDTO> branches = 
                hedInstitutionService.getBranchesByType(sectionTypeId);
            return ResponseEntity.ok(Map.of(
                "data", branches,
                "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
```

### Java: AccessControlService

```java
package org.dspace.service;

import org.dspace.authorize.AuthorizeManager;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.content.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.sql.SQLException;

@Service
public class AccessControlService {

    @Autowired
    private Context context;
    
    @Autowired
    private UserBranchRepository userBranchRepository;
    
    @Autowired
    private ItemService itemService;

    /**
     * Check if user has permission to access document
     * Admin users bypass all checks
     */
    public boolean canUserAccessDocument(EPerson user, Item item) 
            throws SQLException {
        
        // Admin override
        if (isUserAdmin(user)) {
            return true;
        }
        
        String documentBranch = getDocumentBranch(item);
        if (documentBranch == null) {
            return false;
        }
        
        List<String> userBranches = getUserBranches(user);
        return userBranches.contains(documentBranch);
    }

    /**
     * Filter collection to show only accessible items
     */
    public List<Item> filterItemsByUserAccess(EPerson user, 
            List<Item> items) throws SQLException {
        
        if (isUserAdmin(user)) {
            return items;
        }
        
        List<String> userBranches = getUserBranches(user);
        List<Item> accessibleItems = new ArrayList<>();
        
        for (Item item : items) {
            String itemBranch = getDocumentBranch(item);
            if (itemBranch != null && userBranches.contains(itemBranch)) {
                accessibleItems.add(item);
            }
        }
        
        return accessibleItems;
    }

    private boolean isUserAdmin(EPerson user) throws SQLException {
        return AuthorizeManager.isAdmin(context, user);
    }

    private List<String> getUserBranches(EPerson user) 
            throws SQLException {
        return userBranchRepository.findByEPerson(user.getID())
            .stream()
            .map(ub -> ub.getBranch().getBranchName())
            .collect(java.util.stream.Collectors.toList());
    }

    private String getDocumentBranch(Item item) throws SQLException {
        List<MetadataValue> values = itemService.getMetadata(
            item, "dc", "branch", null, Item.ANY);
        
        if (!values.isEmpty()) {
            return values.get(0).getValue();
        }
        return null;
    }
}
```

### React: Conditional Form Component

```javascript
import React, { useState, useEffect } from 'react';
import axios from 'axios';

const DocumentForm = () => {
  const [districts, setDistricts] = useState([]);
  const [institutions, setInstitutions] = useState([]);
  const [selectedDistrict, setSelectedDistrict] = useState(null);
  const [selectedInstitution, setSelectedInstitution] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchDistricts();
  }, []);

  const fetchDistricts = async () => {
    try {
      const response = await axios.get('/api/hed/districts');
      setDistricts(response.data.data);
      setError(null);
    } catch (err) {
      setError('Failed to load districts');
      console.error(err);
    }
  };

  const handleDistrictChange = async (districtId) => {
    setSelectedDistrict(districtId);
    setSelectedInstitution(null);
    setInstitutions([]);
    setLoading(true);
    
    try {
      const response = await axios.get(
        `/api/hed/institutions?districtId=${districtId}`
      );
      setInstitutions(response.data.data);
      setError(null);
    } catch (err) {
      setError('Failed to load institutions');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="document-form">
      <div className="form-group">
        <label>District*</label>
        <select 
          onChange={(e) => handleDistrictChange(e.target.value)}
          required
        >
          <option value="">Select District</option>
          {districts.map(d => (
            <option key={d.id} value={d.id}>
              {d.name}
            </option>
          ))}
        </select>
      </div>

      {selectedDistrict && (
        <div className="form-group">
          <label>Institution*</label>
          <select 
            onChange={(e) => setSelectedInstitution(e.target.value)}
            disabled={loading}
            required
          >
            <option value="">
              {loading ? 'Loading...' : 'Select Institution'}
            </option>
            {institutions.map(i => (
              <option key={i.id} value={i.id}>
                {i.name}
              </option>
            ))}
          </select>
          {loading && <span className="loading">Loading institutions...</span>}
        </div>
      )}

      {error && <div className="error">{error}</div>}

      <button type="submit" disabled={!selectedInstitution}>
        Submit
      </button>
    </form>
  );
};

export default DocumentForm;
```

### Python: OCR Script Configuration

```python
# config.json - OCR Script Database Configuration
{
  "host": "localhost",
  "port": 5432,
  "database": "dspace",
  "user": "dspace_admin",
  "password": "secure_password"
}

# Usage:
# python scripts/ocr/extract_institutions.py /path/to/pdfs \
#   --district-id 1 \
#   --db-config config.json \
#   --log-level INFO
```

---

## Troubleshooting Guide

### Metadata Fields Not Appearing in Forms

**Symptoms:** Custom metadata fields not visible in item submission form

**Solution:**
1. Verify metadata fields registered in `metadatafieldregistry` table:
   ```sql
   SELECT * FROM metadatafieldregistry WHERE element LIKE 'case%' OR element LIKE 'file%';
   ```
2. Check `dublin-core-types.xml` for syntax errors
3. Restart DSpace application server: `./bin/dspace restart`
4. Clear browser cache and reload form

### Conditional Form Not Loading Options

**Symptoms:** Institution dropdown remains empty after selecting district

**Cause:** API endpoint not responding or network issue

**Debug:**
```bash
# Test API endpoint
curl -X GET "http://localhost:8080/api/hed/institutions?districtId=1"

# Check application logs
tail -f /dspace/log/catalina.out

# Verify database connectivity
psql -h localhost -U dspace_admin -d dspace -c "SELECT COUNT(*) FROM hed_institution;"
```

### Access Control Returns 403 Forbidden

**Symptoms:** User cannot access documents they should have access to

**Solution:**
1. Verify user-branch mapping exists:
   ```sql
   SELECT * FROM dspace_user_branch WHERE eperson_id = 123;
   ```
2. Check if user is admin:
   ```sql
   SELECT * FROM eperson WHERE eperson_id = 123 AND netid = 'admin';
   ```
3. Verify document has dc.branch metadata:
   ```sql
   SELECT * FROM metadatavalue WHERE item_id = 456 AND metadata_field_id IN 
   (SELECT metadata_field_id FROM metadatafieldregistry WHERE element = 'branch');
   ```

### Solr Indexing Issues

**Symptoms:** New metadata fields not searchable

**Solution:**
1. Verify schema.xml has field definitions
2. Restart Solr: `systemctl restart solr`
3. Reindex items:
   ```bash
   cd /dspace
   ./bin/dspace index-discovery -f
   ```
4. Monitor indexing progress: `tail -f /var/solr/logs/solr.log`

### OCR Script Extraction Low Accuracy

**Symptoms:** Institution names not extracted correctly from PDFs

**Solutions:**
1. Verify Tesseract installation: `tesseract --version`
2. Install language packs: `apt-get install tesseract-ocr-osd`
3. Check PDF quality - scanned documents need higher resolution
4. Review OCR log output for failed patterns:
   ```bash
   python extract_institutions.py /pdfs --log-level DEBUG
   ```
5. Manually review and correct extracted institutions in database

### Database Migration Failure

**Symptoms:** Flyway migration fails during deployment

**Solution:**
```bash
# Check migration status
cd /dspace
mvn flyway:info

# Repair migration history (if needed)
mvn flyway:repair

# Retry migration
mvn flyway:migrate

# View migration logs
cat /dspace/logs/migration.log
```

---

## Maintenance Tasks

### Weekly Tasks
- Monitor Solr index size and performance
- Review application error logs
- Verify backup completion

### Monthly Tasks
- Analyze access control logs for security audits
- Performance tuning of frequently-used queries
- Database maintenance and statistics update:
  ```sql
  ANALYZE;
  REINDEX DATABASE dspace;
  ```

### Quarterly Tasks
- Test disaster recovery procedures
- Update password policies
- Review and update institution master data

---

## Support & References

**DSpace Documentation:** https://wiki.duraspace.org/display/DSPACE  
**Spring Boot Guides:** https://spring.io/guides  
**PostgreSQL Documentation:** https://www.postgresql.org/docs/  
**Solr Documentation:** https://lucene.apache.org/solr/  
**Tesseract OCR:** https://github.com/UB-Mannheim/tesseract/wiki  

---

**Document Version:** 1.0  
**Last Updated:** April 2026  
**Author:** Development Team
