#DB connection

-- You're already connected as SYSTEM, so run:

-- Switch to pluggable database
ALTER SESSION SET CONTAINER=XEPDB1;

-- Verify you're in XEPDB1  
SHOW CON_NAME;

-- Create ECOMM user
CREATE USER ECOMM IDENTIFIED BY EcommPass123;

-- Grant privileges
GRANT CONNECT, RESOURCE TO ECOMM;
GRANT CREATE SESSION TO ECOMM;  
GRANT UNLIMITED TABLESPACE TO ECOMM;
GRANT CREATE TABLE TO ECOMM;
GRANT CREATE SEQUENCE TO ECOMM;

-- Verify creation
SELECT USERNAME FROM DBA_USERS WHERE USERNAME = 'ECOMM';

-- Exit and test connection
EXIT;

#run this once the db is creted in intelij powershell terminal
./gradlew bootRun

#TO connect to db in sql terminal
-- Connect as ECOMM user
CONNECT ECOMM/EcommPass123@localhost:1521/XEPDB1

-- List tables created by Hibernate
SELECT TABLE_NAME FROM USER_TABLES;

-- Check PRODUCTS table structure
DESC PRODUCTS;

-- View sequences
SELECT SEQUENCE_NAME FROM USER_SEQUENCES;

