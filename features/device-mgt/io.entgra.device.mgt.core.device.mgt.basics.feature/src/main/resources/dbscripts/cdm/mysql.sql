CREATE TABLE IF NOT EXISTS DM_DEVICE_TYPE (
     ID   INTEGER AUTO_INCREMENT NOT NULL,
     NAME VARCHAR(300) DEFAULT NULL,
     DEVICE_TYPE_META VARCHAR(20000) DEFAULT NULL,
     LAST_UPDATED_TIMESTAMP TIMESTAMP NOT NULL,
     PROVIDER_TENANT_ID INTEGER DEFAULT 0,
     SHARED_WITH_ALL_TENANTS BOOLEAN NOT NULL DEFAULT FALSE,
     PRIMARY KEY (ID)
)ENGINE = InnoDB;

CREATE INDEX IDX_DEVICE_TYPE ON DM_DEVICE_TYPE (NAME, PROVIDER_TENANT_ID);
CREATE INDEX IDX_DEVICE_NAME ON DM_DEVICE_TYPE (NAME);
CREATE INDEX IDX_DEVICE_TYPE_DEVICE_NAME ON DM_DEVICE_TYPE(ID, NAME);

CREATE TABLE IF NOT EXISTS DM_DEVICE_CERTIFICATE (
      ID INTEGER auto_increment NOT NULL,
      SERIAL_NUMBER VARCHAR(500) DEFAULT NULL,
      CERTIFICATE BLOB DEFAULT NULL,
      TENANT_ID INTEGER DEFAULT 0,
      DEVICE_IDENTIFIER VARCHAR(300),
      USERNAME  VARCHAR(500) DEFAULT NULL,
      PRIMARY KEY (ID)
)ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS DM_GROUP (
  ID          INTEGER AUTO_INCREMENT NOT NULL,
  GROUP_NAME  VARCHAR(100) DEFAULT NULL,
  STATUS VARCHAR(50) DEFAULT NULL,
  DESCRIPTION TEXT         DEFAULT NULL,
  OWNER       VARCHAR(255)  DEFAULT NULL,
  PARENT_PATH VARCHAR(255) DEFAULT NULL,
  TENANT_ID   INTEGER      DEFAULT 0,
  PARENT_GROUP_ID   INTEGER   DEFAULT 0,
  PRIMARY KEY (ID)
)
  ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS DM_ROLE_GROUP_MAP (
  ID        INTEGER AUTO_INCREMENT NOT NULL,
  GROUP_ID  INTEGER     DEFAULT NULL,
  ROLE      VARCHAR(45) DEFAULT NULL,
  TENANT_ID INTEGER     DEFAULT 0,
  PRIMARY KEY (ID),
  CONSTRAINT DM_ROLE_GROUP_MAP_DM_GROUP2 FOREIGN KEY (GROUP_ID)
  REFERENCES DM_GROUP (ID)
    ON DELETE CASCADE
    ON UPDATE CASCADE
)
  ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS DM_DEVICE (
     ID                    INTEGER AUTO_INCREMENT NOT NULL,
     DESCRIPTION           TEXT DEFAULT NULL,
     NAME                  VARCHAR(100) DEFAULT NULL,
     DEVICE_TYPE_ID        INT(11) NOT NULL,
     DEVICE_IDENTIFICATION VARCHAR(300) NOT NULL,
     LAST_UPDATED_TIMESTAMP TIMESTAMP NOT NULL,
     TENANT_ID INTEGER DEFAULT 0,
     PRIMARY KEY (ID),
     CONSTRAINT fk_DM_DEVICE_DM_DEVICE_TYPE2 FOREIGN KEY (DEVICE_TYPE_ID)
     REFERENCES DM_DEVICE_TYPE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
)ENGINE = InnoDB;

CREATE INDEX IDX_DM_DEVICE ON DM_DEVICE(TENANT_ID, DEVICE_TYPE_ID);
CREATE INDEX IDX_DM_DEVICE_TYPE_ID_DEVICE_IDENTIFICATION ON DM_DEVICE(TENANT_ID, DEVICE_TYPE_ID,DEVICE_IDENTIFICATION);
CREATE INDEX IDX_DM_DEVICE_DEVICE_IDENTIFICATION ON DM_DEVICE(DEVICE_IDENTIFICATION);
CREATE INDEX IDX_DM_DEVICE_LAST_UPDATED_TIMESTAMP ON DM_DEVICE(LAST_UPDATED_TIMESTAMP);

CREATE TABLE IF NOT EXISTS DM_DEVICE_PROPERTIES (
     DEVICE_TYPE_NAME VARCHAR(300) NOT NULL,
     DEVICE_IDENTIFICATION VARCHAR(300) NOT NULL,
     PROPERTY_NAME VARCHAR(100) DEFAULT 0,
     PROPERTY_VALUE VARCHAR(100) DEFAULT NULL,
     TENANT_ID VARCHAR(100),
     PRIMARY KEY (DEVICE_TYPE_NAME, DEVICE_IDENTIFICATION, PROPERTY_NAME, TENANT_ID)
)ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS GROUP_PROPERTIES (
     GROUP_ID INTEGER NOT NULL,
     PROPERTY_NAME VARCHAR(100) DEFAULT 0,
     PROPERTY_VALUE VARCHAR(100) DEFAULT NULL,
     TENANT_ID VARCHAR(100),
     PRIMARY KEY (GROUP_ID, PROPERTY_NAME, TENANT_ID)
)ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS DM_DEVICE_GROUP_MAP (
  ID        INTEGER AUTO_INCREMENT NOT NULL,
  DEVICE_ID INTEGER DEFAULT NULL,
  GROUP_ID  INTEGER DEFAULT NULL,
  TENANT_ID INTEGER DEFAULT 0,
  PRIMARY KEY (ID),
  CONSTRAINT fk_DM_DEVICE_GROUP_MAP_DM_DEVICE2 FOREIGN KEY (DEVICE_ID)
  REFERENCES DM_DEVICE (ID)
    ON DELETE CASCADE
    ON UPDATE CASCADE ,
  CONSTRAINT fk_DM_DEVICE_GROUP_MAP_DM_GROUP2 FOREIGN KEY (GROUP_ID)
  REFERENCES DM_GROUP (ID)
    ON DELETE CASCADE
    ON UPDATE CASCADE
)
  ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS DM_OPERATION (
    ID INTEGER AUTO_INCREMENT NOT NULL,
    TYPE VARCHAR(20) NOT NULL,
    CREATED_TIMESTAMP BIGINT(15) NOT NULL,
    RECEIVED_TIMESTAMP BIGINT(15) NULL,
    OPERATION_CODE VARCHAR(50) NOT NULL,
    INITIATED_BY VARCHAR(100) NULL,
    OPERATION_DETAILS BLOB DEFAULT NULL,
    OPERATION_PROPERTIES BLOB DEFAULT NULL,
    ENABLED BOOLEAN NOT NULL DEFAULT FALSE,
    TENANT_ID INT NOT NULL,
    PRIMARY KEY (ID)
)ENGINE = InnoDB;

CREATE INDEX IDX_OP_CREATED ON DM_OPERATION (CREATED_TIMESTAMP ASC);
CREATE INDEX IDX_OP_CODE ON DM_OPERATION (OPERATION_CODE ASC);
CREATE INDEX IDX_OP_INITIATED_BY ON DM_OPERATION (INITIATED_BY ASC);

CREATE TABLE IF NOT EXISTS DM_ENROLMENT (
    ID INTEGER AUTO_INCREMENT NOT NULL,
    DEVICE_ID INTEGER NOT NULL,
    DEVICE_TYPE VARCHAR(300) NOT NULL,
    DEVICE_IDENTIFICATION VARCHAR(300) NOT NULL,
    OWNER VARCHAR(255) NOT NULL,
    OWNERSHIP VARCHAR(45) DEFAULT NULL,
    STATUS VARCHAR(50) NULL,
    IS_TRANSFERRED BOOLEAN NOT NULL DEFAULT FALSE,
    DATE_OF_ENROLMENT TIMESTAMP NULL DEFAULT NULL,
    DATE_OF_LAST_UPDATE TIMESTAMP NULL DEFAULT NULL,
    TENANT_ID INT NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT FK_DM_DEVICE_ENROLMENT FOREIGN KEY (DEVICE_ID) REFERENCES
    DM_DEVICE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
)ENGINE = InnoDB;
CREATE INDEX IDX_ENROLMENT_DATE_OF_LAST_UPDATE ON DM_ENROLMENT(DATE_OF_LAST_UPDATE);
CREATE INDEX IDX_ENROLMENT_DEVICE_IDENTIFICATION ON DM_ENROLMENT(DEVICE_IDENTIFICATION);
CREATE INDEX IDX_ENROLMENT_DEVICE_TYPE ON DM_ENROLMENT(DEVICE_TYPE);
CREATE INDEX IDX_ENROLMENT_FK_DEVICE_ID ON DM_ENROLMENT(DEVICE_ID);
CREATE INDEX IDX_ENROLMENT_STATUS ON DM_ENROLMENT(STATUS);
CREATE INDEX IDX_ENROLMENT_TENANT_ID ON DM_ENROLMENT(TENANT_ID);


CREATE TABLE IF NOT EXISTS DM_DEVICE_STATUS (
    ID INTEGER AUTO_INCREMENT NOT NULL,
    ENROLMENT_ID INTEGER NOT NULL,
    DEVICE_ID INTEGER NOT NULL,
    STATUS VARCHAR(50) DEFAULT NULL,
    UPDATE_TIME TIMESTAMP DEFAULT NULL,
    CHANGED_BY VARCHAR(255) NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT FK_DM_DEVICE_STATUS_DEVICE FOREIGN KEY (DEVICE_ID) REFERENCES
        DM_DEVICE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT FK_DM_DEVICE_STATUS_ENROLMENT FOREIGN KEY (ENROLMENT_ID) REFERENCES
        DM_ENROLMENT (ID) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS DM_ENROLMENT_OP_MAPPING (
    ID INTEGER AUTO_INCREMENT NOT NULL,
    ENROLMENT_ID INTEGER NOT NULL,
    OPERATION_ID INTEGER NOT NULL,
    STATUS VARCHAR(50) NULL,
    PUSH_NOTIFICATION_STATUS VARCHAR(50) NULL,
    CREATED_TIMESTAMP INTEGER NOT NULL,
    UPDATED_TIMESTAMP INTEGER NOT NULL,
    OPERATION_CODE VARCHAR(50) NOT NULL,
    INITIATED_BY VARCHAR(100) NULL,
    TYPE VARCHAR(20) NOT NULL,
    DEVICE_ID INTEGER DEFAULT NULL,
    DEVICE_TYPE VARCHAR(300) NOT NULL,
    DEVICE_IDENTIFICATION VARCHAR(300) DEFAULT NULL,
    TENANT_ID INTEGER DEFAULT 0,
    PRIMARY KEY (ID),
    KEY `fk_dm_device_operation_mapping_operation` (`OPERATION_ID`),
    KEY `IDX_DM_ENROLMENT_OP_MAPPING` (`ENROLMENT_ID`,`OPERATION_ID`),
    KEY `ID_DM_ENROLMENT_OP_MAPPING_UPDATED_TIMESTAMP` (`UPDATED_TIMESTAMP`),
    CONSTRAINT fk_dm_device_operation_mapping_device FOREIGN KEY (ENROLMENT_ID) REFERENCES
    DM_ENROLMENT (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_dm_device_operation_mapping_operation FOREIGN KEY (OPERATION_ID) REFERENCES
    DM_OPERATION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
)ENGINE = InnoDB;

CREATE INDEX IDX_ENROLMENT_OP_MAPPING ON DM_ENROLMENT_OP_MAPPING (UPDATED_TIMESTAMP);
CREATE INDEX IDX_EN_OP_MAPPING_EN_ID ON DM_ENROLMENT_OP_MAPPING(ENROLMENT_ID);
CREATE INDEX IDX_EN_OP_MAPPING_OP_ID ON DM_ENROLMENT_OP_MAPPING(OPERATION_ID);
CREATE INDEX IDX_EN_OP_MAPPING_EN_ID_STATUS ON DM_ENROLMENT_OP_MAPPING(ENROLMENT_ID, STATUS);
CREATE INDEX IDX_ENROLMENT_OP_MAPPING_CREATED_TS ON DM_ENROLMENT_OP_MAPPING (CREATED_TIMESTAMP ASC);

CREATE TABLE IF NOT EXISTS DM_DEVICE_OPERATION_RESPONSE
(
    ID                 INT(11)   NOT NULL AUTO_INCREMENT,
    ENROLMENT_ID       INTEGER   NOT NULL,
    OPERATION_ID       INTEGER   NOT NULL,
    EN_OP_MAP_ID       INTEGER   NOT NULL,
    OPERATION_RESPONSE VARCHAR(1024)      DEFAULT NULL,
    IS_LARGE_RESPONSE  BOOLEAN   NOT NULL DEFAULT FALSE,
    RECEIVED_TIMESTAMP TIMESTAMP NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_dm_device_operation_response_enrollment FOREIGN KEY (ENROLMENT_ID) REFERENCES
        DM_ENROLMENT (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_dm_device_operation_response_operation FOREIGN KEY (OPERATION_ID) REFERENCES
        DM_OPERATION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_dm_en_op_map_response FOREIGN KEY (EN_OP_MAP_ID) REFERENCES
        DM_ENROLMENT_OP_MAPPING (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE = InnoDB;

CREATE INDEX IDX_DM_RES_RT ON DM_DEVICE_OPERATION_RESPONSE(RECEIVED_TIMESTAMP);
CREATE INDEX IDX_ENID_OP_ID ON DM_DEVICE_OPERATION_RESPONSE(OPERATION_ID, ENROLMENT_ID);
CREATE INDEX IDX_DM_EN_OP_MAP_ID ON DM_DEVICE_OPERATION_RESPONSE(EN_OP_MAP_ID);

CREATE TABLE IF NOT EXISTS DM_DEVICE_OPERATION_RESPONSE_LARGE
(
    ID                    INT(11)   NOT NULL,
    OPERATION_RESPONSE    LONGBLOB     DEFAULT NULL,
    OPERATION_ID          INTEGER   NOT NULL,
    EN_OP_MAP_ID          INTEGER   NOT NULL,
    RECEIVED_TIMESTAMP    TIMESTAMP NULL,
    DEVICE_IDENTIFICATION VARCHAR(300) DEFAULT NULL,
    CONSTRAINT fk_dm_device_operation_response_large FOREIGN KEY (ID) REFERENCES
        DM_DEVICE_OPERATION_RESPONSE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_dm_en_op_map_response_large FOREIGN KEY (EN_OP_MAP_ID) REFERENCES
        DM_ENROLMENT_OP_MAPPING (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE = InnoDB;

CREATE INDEX IDX_DM_RES_LRG_RT ON DM_DEVICE_OPERATION_RESPONSE_LARGE(RECEIVED_TIMESTAMP);
CREATE INDEX IDX_DM_EN_OP_MAP_ID ON DM_DEVICE_OPERATION_RESPONSE_LARGE(EN_OP_MAP_ID);

-- POLICY RELATED TABLES ---

CREATE  TABLE IF NOT EXISTS DM_PROFILE (
  ID INT NOT NULL AUTO_INCREMENT ,
  PROFILE_NAME VARCHAR(45) NOT NULL ,
  TENANT_ID INT NOT NULL ,
  DEVICE_TYPE VARCHAR(300) NOT NULL ,
  CREATED_TIME DATETIME NOT NULL ,
  UPDATED_TIME DATETIME NOT NULL ,
  PRIMARY KEY (ID)
)ENGINE = InnoDB;


CREATE  TABLE IF NOT EXISTS DM_POLICY (
  ID INT(11) NOT NULL AUTO_INCREMENT ,
  NAME VARCHAR(45) DEFAULT NULL ,
  DESCRIPTION VARCHAR(1000) NULL,
  PAYLOAD_VERSION VARCHAR (45) DEFAULT NULL,
  TENANT_ID INT(11) NOT NULL ,
  PROFILE_ID INT(11) NOT NULL ,
  OWNERSHIP_TYPE VARCHAR(45) NULL,
  COMPLIANCE VARCHAR(100) NULL,
  PRIORITY INT NOT NULL,
  ACTIVE INT(2) NOT NULL,
  UPDATED INT(1) NULL,
  POLICY_TYPE VARCHAR(45) NULL,
  PRIMARY KEY (ID) ,
  CONSTRAINT FK_DM_PROFILE_DM_POLICY
    FOREIGN KEY (PROFILE_ID )
    REFERENCES DM_PROFILE (ID )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)ENGINE = InnoDB;


CREATE  TABLE IF NOT EXISTS DM_DEVICE_POLICY (
  ID INT(11) NOT NULL AUTO_INCREMENT ,
  DEVICE_ID INT(11) NOT NULL ,
  ENROLMENT_ID INT(11) NOT NULL,
  DEVICE BLOB NOT NULL,
  POLICY_ID INT(11) NOT NULL ,
  PRIMARY KEY (ID) ,
  CONSTRAINT FK_POLICY_DEVICE_POLICY
    FOREIGN KEY (POLICY_ID )
    REFERENCES DM_POLICY (ID )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT FK_DEVICE_DEVICE_POLICY
    FOREIGN KEY (DEVICE_ID )
    REFERENCES DM_DEVICE (ID )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)ENGINE = InnoDB;


CREATE  TABLE IF NOT EXISTS DM_DEVICE_TYPE_POLICY (
  ID INT(11) NOT NULL ,
  DEVICE_TYPE VARCHAR(300) NOT NULL ,
  POLICY_ID INT(11) NOT NULL ,
  PRIMARY KEY (ID) ,
  CONSTRAINT FK_DEVICE_TYPE_POLICY
    FOREIGN KEY (POLICY_ID )
    REFERENCES DM_POLICY (ID )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)ENGINE = InnoDB;


CREATE  TABLE IF NOT EXISTS DM_PROFILE_FEATURES (
  ID INT(11) NOT NULL AUTO_INCREMENT,
  PROFILE_ID INT(11) NOT NULL,
  FEATURE_CODE VARCHAR(100) NOT NULL,
  DEVICE_TYPE VARCHAR(300) NOT NULL ,
  TENANT_ID INT(11) NOT NULL ,
  CONTENT BLOB NULL DEFAULT NULL,
  PRIMARY KEY (ID),
  CONSTRAINT FK_DM_PROFILE_DM_POLICY_FEATURES
    FOREIGN KEY (PROFILE_ID)
    REFERENCES DM_PROFILE (ID)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS DM_POLICY_CORRECTIVE_ACTION (
  ID INT(11) NOT NULL AUTO_INCREMENT,
  ACTION_TYPE VARCHAR(45) NOT NULL,
  CORRECTIVE_POLICY_ID INT(11) DEFAULT NULL,
  POLICY_ID INT(11) NOT NULL,
  FEATURE_ID INT(11) DEFAULT NULL,
  IS_REACTIVE BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (ID),
  CONSTRAINT FK_DM_POLICY_DM_POLICY_CORRECTIVE_ACTION
    FOREIGN KEY (POLICY_ID)
    REFERENCES DM_POLICY (ID)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)ENGINE = InnoDB;

CREATE  TABLE IF NOT EXISTS DM_ROLE_POLICY (
  ID INT(11) NOT NULL AUTO_INCREMENT ,
  ROLE_NAME VARCHAR(45) NOT NULL ,
  POLICY_ID INT(11) NOT NULL ,
  PRIMARY KEY (ID) ,
  CONSTRAINT FK_ROLE_POLICY_POLICY
    FOREIGN KEY (POLICY_ID )
    REFERENCES DM_POLICY (ID )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)ENGINE = InnoDB;


CREATE  TABLE IF NOT EXISTS DM_USER_POLICY (
  ID INT NOT NULL AUTO_INCREMENT ,
  POLICY_ID INT NOT NULL ,
  USERNAME VARCHAR(45) NOT NULL ,
  PRIMARY KEY (ID) ,
  CONSTRAINT DM_POLICY_USER_POLICY
    FOREIGN KEY (POLICY_ID )
    REFERENCES DM_POLICY (ID )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)ENGINE = InnoDB;


 CREATE  TABLE IF NOT EXISTS DM_DEVICE_POLICY_APPLIED (
  ID INT NOT NULL AUTO_INCREMENT ,
  DEVICE_ID INT NOT NULL ,
  ENROLMENT_ID INT(11) NOT NULL,
  POLICY_ID INT NOT NULL ,
  POLICY_CONTENT TEXT NULL ,
  TENANT_ID INT NOT NULL,
  APPLIED TINYINT(1) NULL ,
  CREATED_TIME TIMESTAMP NULL ,
  UPDATED_TIME TIMESTAMP NULL ,
  APPLIED_TIME TIMESTAMP NULL ,
  PRIMARY KEY (ID) ,
  CONSTRAINT FK_DM_POLICY_DEVCIE_APPLIED
    FOREIGN KEY (DEVICE_ID )
    REFERENCES DM_DEVICE (ID )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS DM_CRITERIA (
  ID INT NOT NULL AUTO_INCREMENT,
  TENANT_ID INT NOT NULL,
  NAME VARCHAR(50) NULL,
  PRIMARY KEY (ID)
)ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS DM_POLICY_CRITERIA (
  ID INT NOT NULL AUTO_INCREMENT,
  CRITERIA_ID INT NOT NULL,
  POLICY_ID INT NOT NULL,
  PRIMARY KEY (ID),
  CONSTRAINT FK_CRITERIA_POLICY_CRITERIA
    FOREIGN KEY (CRITERIA_ID)
    REFERENCES DM_CRITERIA (ID)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT FK_POLICY_POLICY_CRITERIA
    FOREIGN KEY (POLICY_ID)
    REFERENCES DM_POLICY (ID)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS DM_POLICY_CRITERIA_PROPERTIES (
  ID INT NOT NULL AUTO_INCREMENT,
  POLICY_CRITERION_ID INT NOT NULL,
  PROP_KEY VARCHAR(45) NULL,
  PROP_VALUE VARCHAR(100) NULL,
  CONTENT BLOB NULL COMMENT 'This is used to ',
  PRIMARY KEY (ID),
  CONSTRAINT FK_POLICY_CRITERIA_PROPERTIES
    FOREIGN KEY (POLICY_CRITERION_ID)
    REFERENCES DM_POLICY_CRITERIA (ID)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS DM_POLICY_COMPLIANCE_STATUS (
  ID INT NOT NULL AUTO_INCREMENT,
  DEVICE_ID INT NOT NULL,
  ENROLMENT_ID INT(11) NOT NULL,
  POLICY_ID INT NOT NULL,
  TENANT_ID INT NOT NULL,
  STATUS INT NULL,
  LAST_SUCCESS_TIME TIMESTAMP NULL,
  LAST_REQUESTED_TIME TIMESTAMP NULL,
  LAST_FAILED_TIME TIMESTAMP NULL,
  ATTEMPTS INT NULL,
  PRIMARY KEY (ID)
)ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS DM_POLICY_CHANGE_MGT (
  ID INT NOT NULL AUTO_INCREMENT,
  POLICY_ID INT NOT NULL,
  DEVICE_TYPE VARCHAR(300) NOT NULL ,
  TENANT_ID INT(11) NOT NULL,
  PRIMARY KEY (ID)
)ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS DM_POLICY_COMPLIANCE_FEATURES (
  ID INT NOT NULL AUTO_INCREMENT,
  COMPLIANCE_STATUS_ID INT NOT NULL,
  TENANT_ID INT NOT NULL,
  FEATURE_CODE VARCHAR(100) NOT NULL,
  STATUS INT NULL,
  PRIMARY KEY (ID),
  CONSTRAINT FK_COMPLIANCE_FEATURES_STATUS
    FOREIGN KEY (COMPLIANCE_STATUS_ID)
    REFERENCES DM_POLICY_COMPLIANCE_STATUS (ID)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS DM_APPLICATION (
    ID INTEGER AUTO_INCREMENT NOT NULL,
    NAME VARCHAR(150) NOT NULL,
    APP_IDENTIFIER VARCHAR(150) NOT NULL,
    PLATFORM VARCHAR(50) DEFAULT NULL,
    CATEGORY VARCHAR(50) NULL,
    VERSION VARCHAR(50) NULL,
    TYPE VARCHAR(50) NULL,
    LOCATION_URL VARCHAR(100) DEFAULT NULL,
    IMAGE_URL VARCHAR(100) DEFAULT NULL,
    APP_PROPERTIES BLOB NULL,
    MEMORY_USAGE INTEGER(10) NULL,
    IS_ACTIVE BOOLEAN NOT NULL DEFAULT FALSE,
    DEVICE_ID INTEGER NOT NULL,
    ENROLMENT_ID INTEGER NOT NULL,
    TENANT_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_dm_device FOREIGN KEY (DEVICE_ID) REFERENCES
    DM_DEVICE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_dm_enrolement FOREIGN KEY (ENROLMENT_ID) REFERENCES
    DM_ENROLMENT (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
)ENGINE = InnoDB;

CREATE INDEX IDX_DM_APPLICATION ON DM_APPLICATION(DEVICE_ID, ENROLMENT_ID, TENANT_ID);
CREATE INDEX DM_APPLICATION_NAME ON DM_APPLICATION(NAME);
CREATE INDEX DM_APPLICATION_NAME_PLATFORM_TID ON DM_APPLICATION(NAME, PLATFORM, TENANT_ID);

-- END OF POLICY RELATED TABLES --

CREATE TABLE IF NOT EXISTS DM_APP_ICONS (
    ID INTEGER AUTO_INCREMENT NOT NULL,
    ICON_PATH VARCHAR(150) DEFAULT NULL,
    PACKAGE_NAME VARCHAR(150) NOT NULL,
    VERSION VARCHAR(50) DEFAULT '1.1.0',
    CREATED_TIMESTAMP TIMESTAMP NOT NULL,
    TENANT_ID INTEGER NOT NULL,
    PRIMARY KEY (ID)
)ENGINE = InnoDB;

-- POLICY AND DEVICE GROUP MAPPING --

CREATE TABLE IF NOT EXISTS DM_DEVICE_GROUP_POLICY (
  ID INT NOT NULL AUTO_INCREMENT,
  DEVICE_GROUP_ID INT NOT NULL,
  POLICY_ID INT NOT NULL,
  TENANT_ID INT NOT NULL,
  PRIMARY KEY (ID),
  CONSTRAINT FK_DM_DEVICE_GROUP_POLICY
    FOREIGN KEY (DEVICE_GROUP_ID)
    REFERENCES DM_GROUP (ID)
    ON DELETE CASCADE
    ON UPDATE CASCADE ,
  CONSTRAINT FK_DM_DEVICE_GROUP_DM_POLICY
    FOREIGN KEY (POLICY_ID)
    REFERENCES DM_POLICY (ID)
    ON DELETE CASCADE
    ON UPDATE CASCADE
)ENGINE = InnoDB;

-- END OF POLICY AND DEVICE GROUP MAPPING --

-- NOTIFICATION TABLES --

CREATE TABLE IF NOT EXISTS DM_NOTIFICATION (
    NOTIFICATION_ID INTEGER AUTO_INCREMENT NOT NULL,
    DEVICE_ID INTEGER NOT NULL,
    OPERATION_ID INTEGER NULL,
    TENANT_ID INTEGER NOT NULL,
    STATUS VARCHAR(10) NULL,
    DESCRIPTION VARCHAR(1000) NULL,
    LAST_UPDATED_TIMESTAMP TIMESTAMP NOT NULL,
    PRIMARY KEY (NOTIFICATION_ID),
    CONSTRAINT fk_dm_device_notification FOREIGN KEY (DEVICE_ID) REFERENCES
    DM_DEVICE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
)ENGINE = InnoDB;

CREATE INDEX IDX_NOTF_UT ON DM_NOTIFICATION(LAST_UPDATED_TIMESTAMP);

-- END NOTIFICATION TABLES --

CREATE TABLE IF NOT EXISTS DM_DEVICE_INFO (
  ID INTEGER AUTO_INCREMENT NOT NULL,
  DEVICE_ID INT NULL,
  ENROLMENT_ID INT NOT NULL,
  KEY_FIELD VARCHAR(45) NULL,
  VALUE_FIELD VARCHAR(1500) NULL,
  PRIMARY KEY (ID),
  INDEX DM_DEVICE_INFO_DEVICE_idx (DEVICE_ID ASC),
  INDEX DM_DEVICE_INFO_DEVICE_ENROLLMENT_idx (ENROLMENT_ID ASC),
  CONSTRAINT DM_DEVICE_INFO_DEVICE
    FOREIGN KEY (DEVICE_ID)
    REFERENCES DM_DEVICE (ID)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT DM_DEVICE_INFO_DEVICE_ENROLLMENT
    FOREIGN KEY (ENROLMENT_ID)
    REFERENCES DM_ENROLMENT (ID)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
ENGINE = InnoDB;

CREATE INDEX IDX_DM_DEVICE_INFO_DID_EID_KFIELD ON DM_DEVICE_INFO(DEVICE_ID, ENROLMENT_ID, KEY_FIELD);

CREATE TABLE IF NOT EXISTS DM_DEVICE_LOCATION (
  ID INTEGER AUTO_INCREMENT NOT NULL,
  DEVICE_ID INT NULL,
  ENROLMENT_ID INT NOT NULL,
  LATITUDE DOUBLE NULL,
  LONGITUDE DOUBLE NULL,
  STREET1 VARCHAR(255) NULL,
  STREET2 VARCHAR(45) NULL,
  CITY VARCHAR(45) NULL,
  ZIP VARCHAR(10) NULL,
  STATE VARCHAR(45) NULL,
  COUNTRY VARCHAR(45) NULL,
  GEO_HASH VARCHAR(45) NULL,
  UPDATE_TIMESTAMP BIGINT(15) NOT NULL,
  ALTITUDE DOUBLE NULL,
  SPEED FLOAT NULL,
  BEARING FLOAT NULL,
  DISTANCE DOUBLE NULL,
  PRIMARY KEY (ID),
  INDEX DM_DEVICE_LOCATION_DEVICE_idx (DEVICE_ID ASC),
  INDEX DM_DEVICE_LOCATION_GEO_hashx (GEO_HASH ASC),
  INDEX DM_DEVICE_LOCATION_DM_ENROLLMENT_idx (ENROLMENT_ID ASC),
  CONSTRAINT DM_DEVICE_LOCATION_DEVICE
    FOREIGN KEY (DEVICE_ID)
    REFERENCES DM_DEVICE (ID)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT DM_DEVICE_LOCATION_DM_ENROLLMENT
    FOREIGN KEY (ENROLMENT_ID)
    REFERENCES DM_ENROLMENT (ID)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS DM_DEVICE_HISTORY_LAST_SEVEN_DAYS
(
    ID               INTEGER AUTO_INCREMENT NOT NULL,
    DEVICE_ID        INT                    NOT NULL,
    DEVICE_ID_NAME   VARCHAR(255)           NOT NULL,
    TENANT_ID        INT                    NOT NULL,
    DEVICE_TYPE_NAME VARCHAR(45)            NOT NULL,
    LATITUDE         DOUBLE                 NULL,
    LONGITUDE        DOUBLE                 NULL,
    SPEED            FLOAT                  NULL,
    HEADING          FLOAT                  NULL,
    TIMESTAMP        BIGINT(15)             NOT NULL,
    GEO_HASH         VARCHAR(45)            NULL,
    DEVICE_OWNER     VARCHAR(45)            NULL,
    DEVICE_ALTITUDE  DOUBLE                 NULL,
    DISTANCE         DOUBLE                 NULL,
    PRIMARY KEY (ID)
)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS DM_DEVICE_DETAIL (
  ID INT NOT NULL AUTO_INCREMENT,
  DEVICE_ID INT NOT NULL,
  ENROLMENT_ID INT NOT NULL,
  DEVICE_MODEL VARCHAR(45) NULL,
  VENDOR VARCHAR(45) NULL,
  OS_VERSION VARCHAR(45) NULL,
  OS_BUILD_DATE VARCHAR(100) NULL,
  BATTERY_LEVEL DECIMAL(4) NULL,
  INTERNAL_TOTAL_MEMORY DECIMAL(30,3) NULL,
  INTERNAL_AVAILABLE_MEMORY DECIMAL(30,3) NULL,
  EXTERNAL_TOTAL_MEMORY DECIMAL(30,3) NULL,
  EXTERNAL_AVAILABLE_MEMORY DECIMAL(30,3) NULL,
  CONNECTION_TYPE VARCHAR(50) NULL,
  SSID VARCHAR(45) NULL,
  CPU_USAGE DECIMAL(5) NULL,
  TOTAL_RAM_MEMORY DECIMAL(30,3) NULL,
  AVAILABLE_RAM_MEMORY DECIMAL(30,3) NULL,
  PLUGGED_IN INT(1) NULL,
  UPDATE_TIMESTAMP BIGINT(15) NOT NULL,
  PRIMARY KEY (ID),
  INDEX FK_DM_DEVICE_DETAILS_DEVICE_idx (DEVICE_ID ASC),
  INDEX FK_DM_ENROLMENT_DEVICE_DETAILS_idx (ENROLMENT_ID ASC),
  CONSTRAINT FK_DM_DEVICE_DETAILS_DEVICE
    FOREIGN KEY (DEVICE_ID)
    REFERENCES DM_DEVICE (ID)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT FK_DM_ENROLMENT_DEVICE_DETAILS
    FOREIGN KEY (ENROLMENT_ID)
    REFERENCES DM_ENROLMENT (ID)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
ENGINE = InnoDB;

CREATE INDEX IDX_DM_DEVICE_DETAIL_DID_EID ON DM_DEVICE_DETAIL(DEVICE_ID, ENROLMENT_ID);

-- -----------------------------------------------------
-- Table `DM_DEVICE_TYPE_PLATFORM`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `DM_DEVICE_TYPE_PLATFORM` (
  `ID` INT NOT NULL AUTO_INCREMENT,
  `DEVICE_TYPE_ID` INT DEFAULT 0,
  `VERSION_NAME` VARCHAR(100) NULL,
  `VERSION_STATUS` VARCHAR(100) DEFAULT 'ACTIVE',
  PRIMARY KEY (`ID`),
  CONSTRAINT DM_DEVICE_TYPE_DM_DEVICE_TYPE_PLATFORM_MAPPING FOREIGN KEY (DEVICE_TYPE_ID)
  REFERENCES DM_DEVICE_TYPE (ID)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  UNIQUE KEY `device_type_version_uk` (`DEVICE_TYPE_ID`, `VERSION_NAME`)
  )
ENGINE = InnoDB;

-- METADATA TABLE --
CREATE TABLE IF NOT EXISTS DM_METADATA (
    METADATA_ID INTEGER NOT NULL AUTO_INCREMENT,
    DATA_TYPE VARCHAR(16) NOT NULL,
    METADATA_KEY VARCHAR(128) NOT NULL,
    METADATA_VALUE TEXT NOT NULL,
    TENANT_ID INTEGER NOT NULL,
    PRIMARY KEY (METADATA_ID),
    UNIQUE KEY METADATA_KEY_TENANT_ID (METADATA_KEY,TENANT_ID)
) ENGINE=InnoDB;
-- END OF METADATA TABLE --

-- DM_OTP_DATA TABLE --
CREATE TABLE IF NOT EXISTS DM_OTP_DATA (
   ID INT AUTO_INCREMENT NOT NULL,
   OTP_TOKEN VARCHAR(100) NOT NULL,
   TENANT_ID INT NOT NULL,
   USERNAME VARCHAR(500) NOT NULL,
   EMAIL VARCHAR(100) NOT NULL,
   EMAIL_TYPE VARCHAR(20) NOT NULL,
   META_INFO VARCHAR(20000) NULL,
   CREATED_AT TIMESTAMP NOT NULL,
   EXPIRY_TIME INT NOT NULL DEFAULT 3600,
   IS_EXPIRED BOOLEAN DEFAULT false,
   PRIMARY KEY (ID)
);
-- END OF DM_OTP_DATA TABLE --

-- DASHBOARD RELATED VIEWS --

CREATE VIEW DEVICE_INFO_VIEW AS
SELECT
DM_DEVICE.ID AS DEVICE_ID,
DM_DEVICE.DEVICE_IDENTIFICATION,
DM_DEVICE_TYPE.NAME AS PLATFORM,
DM_ENROLMENT.OWNERSHIP,
DM_ENROLMENT.STATUS AS CONNECTIVITY_STATUS,
DM_DEVICE.TENANT_ID
FROM DM_DEVICE, DM_DEVICE_TYPE, DM_ENROLMENT
WHERE DM_DEVICE.DEVICE_TYPE_ID = DM_DEVICE_TYPE.ID AND DM_DEVICE.ID = DM_ENROLMENT.DEVICE_ID;

CREATE VIEW DEVICE_WITH_POLICY_INFO_VIEW AS
SELECT
DEVICE_ID,
POLICY_ID,
STATUS AS IS_COMPLIANT
FROM DM_POLICY_COMPLIANCE_STATUS;

CREATE VIEW POLICY_COMPLIANCE_INFO AS
SELECT
DEVICE_INFO_VIEW.DEVICE_ID,
DEVICE_INFO_VIEW.DEVICE_IDENTIFICATION,
DEVICE_INFO_VIEW.PLATFORM,
DEVICE_INFO_VIEW.OWNERSHIP,
DEVICE_INFO_VIEW.CONNECTIVITY_STATUS,
IFNULL(DEVICE_WITH_POLICY_INFO_VIEW.POLICY_ID, -1) AS POLICY_ID,
IFNULL(DEVICE_WITH_POLICY_INFO_VIEW.IS_COMPLIANT, -1) AS IS_COMPLIANT,
DEVICE_INFO_VIEW.TENANT_ID
FROM
DEVICE_INFO_VIEW
LEFT JOIN
DEVICE_WITH_POLICY_INFO_VIEW
ON DEVICE_INFO_VIEW.DEVICE_ID = DEVICE_WITH_POLICY_INFO_VIEW.DEVICE_ID
ORDER BY DEVICE_INFO_VIEW.DEVICE_ID;

CREATE VIEW FEATURE_NON_COMPLIANCE_INFO AS
SELECT
DM_DEVICE.ID AS DEVICE_ID,
DM_DEVICE.DEVICE_IDENTIFICATION,
DM_DEVICE_DETAIL.DEVICE_MODEL,
DM_DEVICE_DETAIL.VENDOR,
DM_DEVICE_DETAIL.OS_VERSION,
DM_ENROLMENT.OWNERSHIP,
DM_ENROLMENT.OWNER,
DM_ENROLMENT.STATUS AS CONNECTIVITY_STATUS,
DM_POLICY_COMPLIANCE_STATUS.POLICY_ID,
DM_DEVICE_TYPE.NAME AS PLATFORM,
DM_POLICY_COMPLIANCE_FEATURES.FEATURE_CODE,
DM_POLICY_COMPLIANCE_FEATURES.STATUS AS IS_COMPLAINT,
DM_DEVICE.TENANT_ID
FROM
DM_POLICY_COMPLIANCE_FEATURES, DM_POLICY_COMPLIANCE_STATUS, DM_ENROLMENT, DM_DEVICE, DM_DEVICE_TYPE, DM_DEVICE_DETAIL
WHERE
DM_POLICY_COMPLIANCE_FEATURES.COMPLIANCE_STATUS_ID = DM_POLICY_COMPLIANCE_STATUS.ID AND
DM_POLICY_COMPLIANCE_STATUS.ENROLMENT_ID = DM_ENROLMENT.ID AND
DM_POLICY_COMPLIANCE_STATUS.DEVICE_ID = DM_DEVICE.ID AND
DM_DEVICE.DEVICE_TYPE_ID = DM_DEVICE_TYPE.ID AND
DM_DEVICE.ID = DM_DEVICE_DETAIL.DEVICE_ID
ORDER BY TENANT_ID, DEVICE_ID;

-- END OF DASHBOARD RELATED VIEWS --

-- DM_GEOFENCE TABLE--

CREATE TABLE IF NOT EXISTS DM_GEOFENCE (
    ID INT NOT NULL AUTO_INCREMENT,
    FENCE_NAME VARCHAR(255) NOT NULL,
    DESCRIPTION TEXT DEFAULT NULL,
    LATITUDE DOUBLE DEFAULT NULL,
    LONGITUDE DOUBLE DEFAULT NULL,
    RADIUS DOUBLE DEFAULT NULL,
    GEO_JSON TEXT DEFAULT NULL,
    FENCE_SHAPE VARCHAR(100) DEFAULT NULL,
    CREATED_TIMESTAMP TIMESTAMP NOT NULL,
    OWNER VARCHAR(255) NOT NULL,
    TENANT_ID INTEGER DEFAULT 0,
    PRIMARY KEY (ID)
) ENGINE=InnoDB;

-- END OF DM_GEOFENCE TABLE--

-- DM_GEOFENCE_GROUP_MAPPING TABLE--
CREATE TABLE IF NOT EXISTS DM_GEOFENCE_GROUP_MAPPING (
    ID INT NOT NULL AUTO_INCREMENT,
    FENCE_ID INT NOT NULL,
    GROUP_ID INT NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_dm_geofence_group_mapping_geofence FOREIGN KEY (FENCE_ID) REFERENCES
    DM_GEOFENCE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_dm_geofence_group_mapping_group FOREIGN KEY (GROUP_ID) REFERENCES
    DM_GROUP (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB;

-- END OF DM_GEOFENCE_GROUP_MAPPING TABLE--

-- DM_DEVICE_EVENT TABLE --

CREATE TABLE IF NOT EXISTS DM_DEVICE_EVENT (
    ID INT NOT NULL AUTO_INCREMENT,
    EVENT_SOURCE VARCHAR(100) NOT NULL,
    EVENT_LOGIC VARCHAR(100) NOT NULL,
    ACTIONS TEXT DEFAULT NULL,
    CREATED_TIMESTAMP TIMESTAMP NOT NULL,
    TENANT_ID INTEGER DEFAULT 0,
    PRIMARY KEY (ID)
) ENGINE=InnoDB;

-- END OF DM_DEVICE_EVENT TABLE --

-- DM_DEVICE_EVENT_GROUP_MAPPING TABLE--
CREATE TABLE IF NOT EXISTS DM_DEVICE_EVENT_GROUP_MAPPING (
    ID INT NOT NULL AUTO_INCREMENT,
    EVENT_ID INT NOT NULL,
    GROUP_ID INT NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_dm_event_group_mapping_event FOREIGN KEY (EVENT_ID) REFERENCES
    DM_DEVICE_EVENT (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_dm_event_group_mapping_group FOREIGN KEY (GROUP_ID) REFERENCES
    DM_GROUP (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB;

-- END OF DM_DEVICE_EVENT_GROUP_MAPPING TABLE--

-- DM_GEOFENCE_GROUP_MAPPING TABLE--
CREATE TABLE IF NOT EXISTS DM_GEOFENCE_EVENT_MAPPING (
    ID INT NOT NULL AUTO_INCREMENT,
    FENCE_ID INT NOT NULL,
    EVENT_ID INT NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_dm_geofence_event_mapping_geofence FOREIGN KEY (FENCE_ID) REFERENCES
    DM_GEOFENCE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_dm_geofence_event_mapping_event FOREIGN KEY (EVENT_ID) REFERENCES
    DM_DEVICE_EVENT (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB;

-- END OF DM_GEOFENCE_GROUP_MAPPING TABLE--

-- DM_EXT_GROUP_MAPPING TABLE--
CREATE TABLE IF NOT EXISTS DM_EXT_GROUP_MAPPING (
    ID INT NOT NULL AUTO_INCREMENT,
    TRACCAR_GROUP_ID INT DEFAULT 0,
    GROUP_ID INT NOT NULL,
    TENANT_ID INT NOT NULL,
    STATUS INT DEFAULT 0,
    PRIMARY KEY (ID)
);
-- END OF DM_EXT_GROUP_MAPPING TABLE--

-- END OF DM_EXT_DEVICE_MAPPING TABLE--
CREATE TABLE IF NOT EXISTS DM_EXT_DEVICE_MAPPING (
    ID INT NOT NULL AUTO_INCREMENT,
    TRACCAR_DEVICE_ID INT DEFAULT 0,
    DEVICE_ID INT NOT NULL,
    TENANT_ID INT NOT NULL,
    STATUS INT DEFAULT 0,
    PRIMARY KEY (ID)
);
-- END OF DM_EXT_DEVICE_MAPPING TABLE--

-- END OF DM_EXT_PERMISSION_MAPPING TABLE--
CREATE TABLE IF NOT EXISTS DM_EXT_PERMISSION_MAPPING (
    TRACCAR_DEVICE_ID INT DEFAULT 0,
    TRACCAR_USER_ID INT DEFAULT 0
);
-- END OF DM_EXT_PERMISSION_MAPPING TABLE--

-- DYNAMIC TASK TABLES--
CREATE TABLE IF NOT EXISTS DYNAMIC_TASK (
     DYNAMIC_TASK_ID INTEGER AUTO_INCREMENT NOT NULL,
     NAME VARCHAR(300) DEFAULT NULL ,
     CRON VARCHAR(100) DEFAULT NULL,
     IS_ENABLED BOOLEAN NOT NULL DEFAULT FALSE,
     TASK_CLASS_NAME VARCHAR(1000) DEFAULT NULL,
     TENANT_ID INTEGER DEFAULT 0,
     PRIMARY KEY (DYNAMIC_TASK_ID)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS DYNAMIC_TASK_PROPERTIES (
     DYNAMIC_TASK_ID INTEGER NOT NULL,
     PROPERTY_NAME VARCHAR(100) DEFAULT 0,
     PROPERTY_VALUE TEXT DEFAULT NULL,
     TENANT_ID INTEGER DEFAULT 0,
     PRIMARY KEY (DYNAMIC_TASK_ID, PROPERTY_NAME, TENANT_ID),
     CONSTRAINT FK_DYNAMIC_TASK_TASK_PROPERTIES FOREIGN KEY (DYNAMIC_TASK_ID) REFERENCES
             DYNAMIC_TASK (DYNAMIC_TASK_ID) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;
-- END OF DYNAMIC TASK TABLE--

-- DM_DEVICE_SUB_TYPE TABLE--
CREATE TABLE IF NOT EXISTS DM_DEVICE_SUB_TYPE (
    TENANT_ID INT DEFAULT 0,
    SUB_TYPE_ID VARCHAR(45) NOT NULL,
    DEVICE_TYPE VARCHAR(45) NOT NULL,
    SUB_TYPE_NAME VARCHAR(100) NOT NULL,
    TYPE_DEFINITION TEXT NOT NULL,
    PRIMARY KEY (SUB_TYPE_ID,DEVICE_TYPE)
) ENGINE=InnoDB;
-- END OF  DM_DEVICE_SUB_TYPE  TABLE--

-- DM_TRACCAR_UNSYNCED_DEVICES TABLE --
CREATE TABLE IF NOT EXISTS DM_TRACCAR_UNSYNCED_DEVICES (
    ID INT NOT NULL AUTO_INCREMENT,
    DEVICE_NAME VARCHAR(100) NOT NULL,
    IOTS_DEVICE_IDENTIFIER VARCHAR(300) DEFAULT NULL UNIQUE,
    TRACCAR_DEVICE_UNIQUE_ID INT NOT NULL,
    TRACCAR_USENAME VARCHAR(100) NULL,
    STATUS VARCHAR(100) NULL,
    TENANT_ID INTEGER DEFAULT 0,
    PRIMARY KEY (ID)
);

-- SUB_OPERATION_TEMPLATE TABLE--
CREATE TABLE SUB_OPERATION_TEMPLATE (
  SUB_OPERATION_TEMPLATE_ID int NOT NULL AUTO_INCREMENT,
  OPERATION_DEFINITION json NOT NULL,
  OPERATION_CODE varchar(100) NOT NULL,
  SUB_TYPE_ID VARCHAR(45) NOT NULL,
  DEVICE_TYPE VARCHAR(25) NOT NULL,
  CREATE_TIMESTAMP timestamp NULL DEFAULT NULL,
  UPDATE_TIMESTAMP timestamp NULL DEFAULT NULL,
  PRIMARY KEY (SUB_OPERATION_TEMPLATE_ID),
  UNIQUE KEY SUB_OPERATION_TEMPLATE (SUB_TYPE_ID, OPERATION_CODE, DEVICE_TYPE),
  CONSTRAINT fk_SUB_OPERATION_TEMPLATE_DM_DEVICE_SUB_TYPE FOREIGN KEY (SUB_TYPE_ID, DEVICE_TYPE) REFERENCES DM_DEVICE_SUB_TYPE (SUB_TYPE_ID, DEVICE_TYPE)
) ENGINE=InnoDB;

-- END OF SUB_OPERATION_TEMPLATE TABLE--

-- DM_DEVICE_ORGANIZATION TABLE--
CREATE TABLE IF NOT EXISTS DM_DEVICE_ORGANIZATION (
    ORGANIZATION_ID INT NOT NULL AUTO_INCREMENT,
    TENANT_ID INT DEFAULT 0,
    DEVICE_ID INT(11) NOT NULL,
    PARENT_DEVICE_ID INT(11) DEFAULT NULL,
    DEVICE_ORGANIZATION_META TEXT DEFAULT NULL,
    LAST_UPDATED_TIMESTAMP TIMESTAMP NOT NULL,
    PRIMARY KEY (ORGANIZATION_ID),
    CONSTRAINT fk_DM_DEVICE_DM_ID FOREIGN KEY (DEVICE_ID)
    REFERENCES DM_DEVICE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_DM_PARENT_DEVICE_DM_ID FOREIGN KEY (PARENT_DEVICE_ID)
    REFERENCES DM_DEVICE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT CHILD_PARENT_COMP_KEY UNIQUE (DEVICE_ID, PARENT_DEVICE_ID)
    );
-- END OF DM_DEVICE_ORGANIZATION TABLE--

-- DM_CEA_POLICIES TABLE --

CREATE TABLE IF NOT EXISTS DM_CEA_POLICIES (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    POLICY_CONTENT VARCHAR(2000) NOT NULL,
    CREATED_TIMESTAMP TIMESTAMP NOT NULL,
    UPDATED_TIMESTAMP TIMESTAMP NOT NULL,
    LAST_SYNCED_TIMESTAMP TIMESTAMP NULL,
    IS_SYNCED BOOLEAN NOT NULL DEFAULT FALSE,
    TENANT_ID INT UNIQUE NOT NULL
);

-- END OF DM_CEA_POLICIES TABLE --
