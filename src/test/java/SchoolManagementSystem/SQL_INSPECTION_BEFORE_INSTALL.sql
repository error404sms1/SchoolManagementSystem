-- SAFE READ-ONLY QUERIES: execute first in Workbench.
USE school_management;
SHOW TABLES;
SHOW CREATE TABLE STUDENT;
SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_TYPE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA='school_management'
  AND TABLE_NAME IN ('APP_USER','SYSTEM_SETUP','TEACHER_SCOPE','STUDENT')
ORDER BY TABLE_NAME, ORDINAL_POSITION;
-- STOP if existing JPA account, teacher scope, or student mappings differ.
