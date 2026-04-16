# 📘 DSpace Backend Installation & Setup Guide (Beginner Friendly)

This guide will help you install and run DSpace backend only, step by step.

---

## 🧭 1. Project Structure

Create a clean workspace:

```bash
mkdir ~/dspace
cd ~/dspace

mkdir root      # Final installed DSpace will be here
mkdir servers   # Tomcat & Solr
mkdir source    # Source code (backend)
```

---

## ⚙️ 2. System Prerequisites

### 🔹 Update system
```bash
sudo apt update && sudo apt upgrade -y
```

### 🔹 Install required tools
```bash
sudo apt install -y git unzip wget curl ant
```

### 🔹 Install Java 17
```bash
sudo apt install -y openjdk-17-jdk
```

Verify installation:
```bash
java -version
```

### 🔹 Install Maven (Latest)

```bash
sudo apt remove maven -y

wget https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
tar -xvzf apache-maven-3.9.6-bin.tar.gz
sudo mv apache-maven-3.9.6 /opt/maven
```

Add to `.bashrc`:

```bash
export M2_HOME=/opt/maven
export PATH=$M2_HOME/bin:$PATH
```

Reload and verify:
```bash
source ~/.bashrc
mvn -version
```

---

## 🗄️ 3. Database Setup (PostgreSQL)

### 🔹 Install PostgreSQL
```bash
sudo apt install -y postgresql postgresql-contrib
```

### 🔹 Start PostgreSQL Service
```bash
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

### 🔹 Create Database & User

Access PostgreSQL terminal:
```bash
sudo -i -u postgres psql
```

Run the following SQL commands:

```sql
CREATE DATABASE education_department;
CREATE USER education WITH PASSWORD 'education';
ALTER DATABASE education_department OWNER TO education;
GRANT ALL PRIVILEGES ON DATABASE education TO education;
\c education_department
CREATE EXTENSION pgcrypto;
\q
```

Verify database creation:
```bash
psql -h localhost -U education -d education_department -c "SELECT version();"
```

---

## 🖥️ 4. Install Servers (Tomcat + Solr)

```bash
cd ~/dspace/servers
```

### 🔹 Install Apache Tomcat
```bash
wget https://archive.apache.org/dist/tomcat/tomcat-10/v10.1.36/bin/apache-tomcat-10.1.36.tar.gz
tar -xvzf apache-tomcat-10.1.36.tar.gz
```

### 🔹 Install Apache Solr
```bash
wget https://www.apache.org/dyn/closer.lua/lucene/solr/8.11.4/solr-8.11.4.tgz?action=download -O solr-8.11.4.tgz
tar -xvzf solr-8.11.4.tgz
```

### 🔹 Cleanup
```bash
rm -rf *.tar.gz *.tgz
```

---

## 📦 5. Clone DSpace Backend Source Code

```bash
cd ~/dspace/source

git clone https://github.com/bmahakud/DSpace-IndianJudiciary-BackEnd backend
```

Navigate to backend:
```bash
cd backend
```

---

## ⚙️ 6. Configure Backend

### 🔹 Locate Configuration File

The configuration file is typically in:
```bash
cd ~/dspace/source/backend
```

Create or edit local DSpace configuration. Check for existing config:
```bash
find . -name "dspace.cfg" -o -name "local.cfg"
```

### 🔹 Create/Edit Configuration

Create `dspace/config/local.cfg` if it doesn't exist:

```properties
# DSpace Installation Directory
dspace.dir=/home/YOUR_USERNAME/dspace/root

# Database Configuration
db.url=jdbc:postgresql://localhost:5432/education_department
db.driver=org.postgresql.Driver
db.username=education
db.password=education
db.dialect=org.dspace.util.DSpacePostgreSQLDialect
db.schema=public

# Default Language
default.language=en

# Server URL Configuration
dspace.baseUrl=http://localhost:8080/server

# Solr Configuration
solr.server=http://localhost:8983/solr

# Email Configuration
mail.server=localhost
mail.server.port=25
mail.server.username=
mail.server.password=

# Higher Education Department - Metadata
# Custom metadata schema ID
hed.metadata.schema.id=1

# Access Control Settings
hed.access-control.enabled=true

# Form Validation
hed.form-validation.strict=true

# OCR Integration Settings
hed.ocr.enabled=true
hed.ocr.tesseract.path=/usr/bin/tesseract
```

Replace `YOUR_USERNAME` with your actual username.

---

## 🔨 7. Build Backend

### 🔹 Build with Maven

```bash
cd ~/dspace/source/backend
mvn clean package
```

This process may take 5-10 minutes. Wait for it to complete.

### 🔹 Run DSpace Installation Ant Script

```bash
cd dspace/target/dspace-installer
ant fresh_install
```

This will:
- Create the DSpace directory structure in `~/dspace/root`
- Initialize the database schema
- Set up configuration files

---

## 🗃️ 8. Initialize Database & Indexes

Navigate to DSpace root bin directory:
```bash
cd ~/dspace/root/bin
```

### 🔹 Run Database Migrations

```bash
sudo ./dspace database migrate
```

### 🔹 Create Administrator Account

```bash
sudo ./dspace create-administrator
```

Follow the prompts to create an admin user with email and password.

### 🔹 Load Metadata Registry

Load the Dublin Core types configuration:
```bash
sudo ./dspace registry-loader -metadata ../config/registries/dublin-core-types.xml
```

### 🔹 Index Discovery

Build the search index:
```bash
sudo ./dspace index-discovery -b
```

This indexes all items for search functionality.

---

## 🔁 9. Deploy to Tomcat & Solr

### 🔹 Setup Solr Configuration

First, remove any existing Solr configs:
```bash
rm -rf ~/dspace/servers/solr-8.11.4/server/solr/configsets/*
```

Copy DSpace Solr configuration:
```bash
cp -r ~/dspace/root/solr/* ~/dspace/servers/solr-8.11.4/server/solr/configsets/
```

### 🔹 Deploy Backend WAR to Tomcat

Copy the compiled backend to Tomcat webapps:
```bash
cp -r ~/dspace/root/webapps/server ~/dspace/servers/apache-tomcat-10.1.36/webapps/
```

---

## 🚀 10. Start Services

### 🔹 Start Solr Service

```bash
cd ~/dspace/servers/solr-8.11.4/bin
./solr start -p 8983 -force
```

Verify Solr is running:
```bash
curl http://localhost:8983/solr/admin/cores
```

### 🔹 Start Tomcat Server

```bash
cd ~/dspace/servers/apache-tomcat-10.1.36/bin
./startup.sh
```

Monitor startup:
```bash
tail -f ~/dspace/servers/apache-tomcat-10.1.36/logs/catalina.out
```

Wait for message: `Server startup in XXX ms`

---

## ✅ Verify Backend Installation

### 🔹 Check Backend API Health

```bash
curl -X GET http://localhost:8080/server/api/discovery/search
```

Expected response: JSON with search results or metadata.

### 🔹 Check Solr Status

```bash
curl http://localhost:8983/solr/admin/cores?action=STATUS
```

Should show status of Solr cores.

### 🔹 Verify Database Connection

```bash
psql -h localhost -U dspace -d dspace -c "SELECT COUNT(*) FROM item;"
```

Should return a count without errors.

---

## 📝 Higher Education Department Features Enabled

The backend is configured with the following HED-specific features:

✅ **Custom Metadata Fields**
- Barcode Number (dc.barcode)
- File Number (dc.filenumber)
- File Name (dc.file.name)
- File Year (dc.file.year)
- Case Status (dc.case.status)
- Case Nature (dc.case.nature)
- Case Number (dc.case.number)
- Subject Matter (dc.subject.matter)
- Description (dc.comment)
- Petitioner Name (dc.petitioner)
- District Name (dc.district)
- Institution Name (dc.institution)
- Respondent Name (dc.respondent)

✅ **Database Tables**
- hed_branch (Organizational units)
- hed_section_type (Section taxonomy)
- hed_district (Geographic regions)
- hed_institution (Educational institutions)
- dspace_user_branch (Access control mapping)

✅ **Access Control**
- Role-based access control per section/branch
- Administrator override for all items
- Section-level data isolation

✅ **Form Rendering**
- Conditional form fields based on selections
- Dynamic institution dropdown based on district

✅ **OCR Integration**
- Automated institution name extraction from PDFs
- Python script integration for document processing

---

## 🔧 Stopping Services

### 🔹 Stop Tomcat

```bash
cd ~/dspace/servers/apache-tomcat-10.1.36/bin
./shutdown.sh
```

### 🔹 Stop Solr

```bash
cd ~/dspace/servers/solr-8.11.4/bin
./solr stop -p 8983
```

---

## 📊 Key Ports & URLs

| Service | URL | Port |
|---------|-----|------|
| Solr Admin | http://localhost:8983/solr | 8983 |
| Tomcat | http://localhost:8080 | 8080 |
| DSpace Backend API | http://localhost:8080/server/api | 8080 |
| PostgreSQL | localhost | 5432 |

---

## ⚠️ Common Issues & Solutions

### Issue: "Port 8080 already in use"
```bash
# Kill process using port 8080
sudo lsof -i :8080
sudo kill -9 <PID>
```

### Issue: "Database connection refused"
```bash
# Verify PostgreSQL is running
sudo systemctl status postgresql

# Start if not running
sudo systemctl start postgresql
```

### Issue: "Permission denied" for DSpace commands
```bash
# Add execute permission
sudo chmod +x ~/dspace/root/bin/dspace

# Run with sudo if needed
sudo ~/dspace/root/bin/dspace [command]
```

### Issue: "Solr configuration not found"
```bash
# Verify Solr directory exists
ls -la ~/dspace/servers/solr-8.11.4/server/solr/configsets/

# Recopy if missing
cp -r ~/dspace/root/solr/* ~/dspace/servers/solr-8.11.4/server/solr/configsets/
```

### Issue: "Maven build fails"
```bash
# Clear Maven cache and rebuild
mvn clean install -DskipTests
```

---

## 🎯 Next Steps

After successful backend installation:

1. **Configure Access Control**
   - Map users to branches in `dspace_user_branch` table
   - Set up role assignments

2. **Populate Master Data**
   - Insert districts into `hed_district` table
   - Insert institutions into `hed_institution` table
   - Insert branch types into `hed_section_type` table

3. **Test API Endpoints**
   ```bash
   # Get institutions by district
   curl -X GET "http://localhost:8080/server/api/hed/institutions?districtId=1"
   
   # Get all districts
   curl -X GET "http://localhost:8080/server/api/hed/districts"
   
   # Get branches
   curl -X GET "http://localhost:8080/server/api/hed/branches"
   ```

4. **Configure OCR** (Optional)
   - Install Tesseract: `sudo apt-get install tesseract-ocr`
   - Configure OCR script path in `VideoTranscriptionUtil.java`
   - Set up transcription output directory

5. **Deploy Frontend**
   - Follow frontend deployment guide separately
   - Configure CORS settings in backend

---

## 💡 Pro Tips

- Use `ant update` for updating without losing data
- Use `ant fresh_install` only for fresh setup
- Always backup database before major updates
- Monitor logs for errors: `tail -f ~/dspace/servers/apache-tomcat-10.1.36/logs/catalina.out`
- Test API endpoints before deploying frontend

---

##  Troubleshooting Help

For detailed troubleshooting:
- Check Tomcat logs: `~/dspace/servers/apache-tomcat-10.1.36/logs/`
- Check DSpace logs: `~/dspace/root/log/`
- Check Solr logs: `~/dspace/servers/solr-8.11.4/server/logs/`
- Check PostgreSQL logs: `/var/log/postgresql/`

---

**Document Version:** 1.0  
**Last Updated:** April 2026  
**Focus:** DSpace Backend Installation for Higher Education Department
