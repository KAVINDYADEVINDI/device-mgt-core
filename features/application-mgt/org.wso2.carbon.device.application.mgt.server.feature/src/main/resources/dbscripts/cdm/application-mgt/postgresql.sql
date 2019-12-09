-- -----------------------------------------------------
-- Table AP_APP
-- -----------------------------------------------------
CREATE SEQUENCE AP_APP_seq;

CREATE TABLE IF NOT EXISTS AP_APP(
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('AP_APP_seq'),
    NAME VARCHAR(350) NOT NULL,
    DESCRIPTION VARCHAR(200) NOT NULL,
    TYPE VARCHAR(200) NOT NULL,
    TENANT_ID INTEGER NOT NULL,
    STATUS VARCHAR(45) NOT NULL DEFAULT 'ACTIVE',
    SUB_TYPE VARCHAR(45) NOT NULL,
    CURRENCY VARCHAR(45) NULL DEFAULT '$',
    RATING DOUBLE PRECISION NULL DEFAULT NULL,
    DEVICE_TYPE_ID INTEGER NOT NULL,
    PRIMARY KEY (ID)
);

-- -----------------------------------------------------
-- Table AP_APP_RELEASE
-- -----------------------------------------------------
CREATE SEQUENCE AP_APP_RELEASE_seq;

CREATE TABLE IF NOT EXISTS AP_APP_RELEASE(
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('AP_APP_RELEASE_seq'),
    DESCRIPTION VARCHAR(200) NOT NULL,
    VERSION VARCHAR(70) NOT NULL,
    TENANT_ID INTEGER NOT NULL,
    UUID VARCHAR(200) NOT NULL,
    RELEASE_TYPE VARCHAR(45) NOT NULL,
    PACKAGE_NAME VARCHAR(150) NOT NULL,
    APP_PRICE DECIMAL(6, 2) NULL DEFAULT NULL,
    INSTALLER_LOCATION VARCHAR(100) NOT NULL,
    ICON_LOCATION VARCHAR(100) NOT NULL,
    BANNER_LOCATION VARCHAR(100) NULL DEFAULT NULL,
    SC_1_LOCATION VARCHAR(100) NOT NULL,
    SC_2_LOCATION VARCHAR(100) NULL DEFAULT NULL,
    SC_3_LOCATION VARCHAR(100) NULL DEFAULT NULL,
    APP_HASH_VALUE VARCHAR(1000) NOT NULL,
    SHARED_WITH_ALL_TENANTS BOOLEAN NOT NULL DEFAULT FALSE,
    APP_META_INFO VARCHAR(150) NULL DEFAULT NULL,
    SUPPORTED_OS_VERSIONS VARCHAR(45) NOT NULL,
    RATING DOUBLE PRECISION NULL DEFAULT NULL,
    CURRENT_STATE VARCHAR(45) NOT NULL,
    RATED_USERS INTEGER NULL,
    AP_APP_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_AP_APP_RELEASE_AP_APP1
        FOREIGN KEY (AP_APP_ID)
            REFERENCES AP_APP (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX fk_AP_APP_RELEASE_AP_APP1_idx ON AP_APP_RELEASE (AP_APP_ID ASC);

-- -----------------------------------------------------
-- Table AP_APP_REVIEW
-- -----------------------------------------------------
CREATE SEQUENCE AP_APP_REVIEW_seq;

CREATE TABLE IF NOT EXISTS AP_APP_REVIEW(
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('AP_APP_REVIEW_seq'),
    TENANT_ID INTEGER NOT NULL,
    COMMENT TEXT NOT NULL,
    ROOT_PARENT_ID INTEGER NOT NULL,
    IMMEDIATE_PARENT_ID INTEGER NOT NULL,
    CREATED_AT TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    MODIFIED_AT TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    RATING INTEGER NULL,
    USERNAME VARCHAR(45) NOT NULL,
    ACTIVE_REVIEW BOOLEAN NOT NULL DEFAULT TRUE,
    AP_APP_RELEASE_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_AP_APP_COMMENT_AP_APP_RELEASE1
        FOREIGN KEY (AP_APP_RELEASE_ID)
            REFERENCES AP_APP_RELEASE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX fk_AP_APP_COMMENT_AP_APP_RELEASE1_idx ON AP_APP_REVIEW (AP_APP_RELEASE_ID ASC);

-- -----------------------------------------------------
-- Table AP_APP_LIFECYCLE_STATE
-- -----------------------------------------------------
CREATE SEQUENCE AP_APP_LIFECYCLE_STATE_seq;

CREATE TABLE IF NOT EXISTS AP_APP_LIFECYCLE_STATE(
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('AP_APP_LIFECYCLE_STATE_seq'),
    CURRENT_STATE VARCHAR(45) NOT NULL,
    PREVIOUS_STATE VARCHAR(45) NOT NULL,
    TENANT_ID INTEGER NOT NULL,
    UPDATED_BY VARCHAR(100) NOT NULL,
    UPDATED_AT TIMESTAMP(0) NOT NULL,
    AP_APP_RELEASE_ID INTEGER NOT NULL,
    REASON TEXT DEFAULT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_AP_APP_LIFECYCLE_STATE_AP_APP_RELEASE1
        FOREIGN KEY (AP_APP_RELEASE_ID)
            REFERENCES AP_APP_RELEASE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX fk_AP_APP_LIFECYCLE_STATE_AP_APP_RELEASE1_idx ON AP_APP_LIFECYCLE_STATE( AP_APP_RELEASE_ID ASC);

-- -----------------------------------------------------
-- Table AP_APP_TAG
-- -----------------------------------------------------
CREATE SEQUENCE AP_APP_TAG_seq;

CREATE TABLE IF NOT EXISTS AP_APP_TAG(
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('AP_APP_TAG_seq'),
    TENANT_ID INTEGER NOT NULL,
    TAG VARCHAR(100) NOT NULL,
    PRIMARY KEY (ID)
);

-- -----------------------------------------------------
-- Table AP_DEVICE_SUBSCRIPTION
-- -----------------------------------------------------
CREATE SEQUENCE AP_DEVICE_SUBSCRIPTION_seq;

CREATE TABLE IF NOT EXISTS AP_DEVICE_SUBSCRIPTION(
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('AP_DEVICE_SUBSCRIPTION_seq'),
    TENANT_ID INTEGER NOT NULL,
    SUBSCRIBED_BY VARCHAR(100) NOT NULL,
    SUBSCRIBED_TIMESTAMP TIMESTAMP(0) NOT NULL,
    UNSUBSCRIBED BOOLEAN NOT NULL DEFAULT false,
    UNSUBSCRIBED_BY VARCHAR(100) NULL DEFAULT NULL,
    UNSUBSCRIBED_TIMESTAMP TIMESTAMP(0) NULL DEFAULT NULL,
    ACTION_TRIGGERED_FROM VARCHAR(45) NOT NULL,
    STATUS VARCHAR(45) NOT NULL,
    DM_DEVICE_ID INTEGER NOT NULL,
    AP_APP_RELEASE_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_AP_DEVICE_SUBSCRIPTION_AP_APP_RELEASE1
        FOREIGN KEY (AP_APP_RELEASE_ID)
            REFERENCES AP_APP_RELEASE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX fk_AP_DEVICE_SUBSCRIPTION_AP_APP_RELEASE1_idx ON AP_DEVICE_SUBSCRIPTION (AP_APP_RELEASE_ID ASC);

-- -----------------------------------------------------
-- Table AP_GROUP_SUBSCRIPTION
-- -----------------------------------------------------
CREATE SEQUENCE AP_GROUP_SUBSCRIPTION_seq;

CREATE TABLE IF NOT EXISTS AP_GROUP_SUBSCRIPTION(
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('AP_GROUP_SUBSCRIPTION_seq'),
    TENANT_ID INTEGER NOT NULL,
    SUBSCRIBED_BY VARCHAR(100) NOT NULL,
    SUBSCRIBED_TIMESTAMP TIMESTAMP(0) NOT NULL,
    UNSUBSCRIBED BOOLEAN NOT NULL DEFAULT false,
    UNSUBSCRIBED_BY VARCHAR(100) NULL DEFAULT NULL,
    UNSUBSCRIBED_TIMESTAMP TIMESTAMP(0) NULL DEFAULT NULL,
    GROUP_NAME VARCHAR(100) NOT NULL,
    AP_APP_RELEASE_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_AP_GROUP_SUBSCRIPTION_AP_APP_RELEASE1
        FOREIGN KEY (AP_APP_RELEASE_ID)
            REFERENCES AP_APP_RELEASE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX fk_AP_GROUP_SUBSCRIPTION_AP_APP_RELEASE1_idx ON AP_GROUP_SUBSCRIPTION (AP_APP_RELEASE_ID ASC);

-- -----------------------------------------------------
-- Table AP_ROLE_SUBSCRIPTION
-- -----------------------------------------------------
CREATE SEQUENCE AP_ROLE_SUBSCRIPTION_seq;

CREATE TABLE IF NOT EXISTS AP_ROLE_SUBSCRIPTION(
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('AP_ROLE_SUBSCRIPTION_seq'),
    TENANT_ID INTEGER NOT NULL,
    ROLE_NAME VARCHAR(100) NOT NULL,
    SUBSCRIBED_BY VARCHAR(100) NOT NULL,
    SUBSCRIBED_TIMESTAMP TIMESTAMP(0) NOT NULL,
    UNSUBSCRIBED BOOLEAN NOT NULL DEFAULT false,
    UNSUBSCRIBED_BY VARCHAR(100) NULL DEFAULT NULL,
    UNSUBSCRIBED_TIMESTAMP TIMESTAMP(0) NULL DEFAULT NULL,
    AP_APP_RELEASE_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_AP_ROLE_SUBSCRIPTION_AP_APP_RELEASE1
        FOREIGN KEY (AP_APP_RELEASE_ID)
            REFERENCES AP_APP_RELEASE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX fk_AP_ROLE_SUBSCRIPTION_AP_APP_RELEASE1_idx ON AP_ROLE_SUBSCRIPTION (AP_APP_RELEASE_ID ASC);

-- -----------------------------------------------------
-- Table AP_UNRESTRICTED_ROLE
-- -----------------------------------------------------
CREATE SEQUENCE AP_UNRESTRICTED_ROLE_seq;

CREATE TABLE IF NOT EXISTS AP_UNRESTRICTED_ROLE(
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('AP_UNRESTRICTED_ROLE_seq'),
    TENANT_ID INTEGER NOT NULL,
    ROLE VARCHAR(45) NOT NULL,
    AP_APP_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_AP_APP_VISIBILITY_AP_APP1
        FOREIGN KEY (AP_APP_ID)
            REFERENCES AP_APP (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX fk_AP_APP_VISIBILITY_AP_APP1_idx ON AP_UNRESTRICTED_ROLE (AP_APP_ID ASC);

-- -----------------------------------------------------
-- Table AP_USER_SUBSCRIPTION
-- -----------------------------------------------------
CREATE SEQUENCE AP_USER_SUBSCRIPTION_seq;

CREATE TABLE IF NOT EXISTS AP_USER_SUBSCRIPTION(
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('AP_USER_SUBSCRIPTION_seq'),
    TENANT_ID INTEGER NOT NULL,
    USER_NAME VARCHAR(100) NOT NULL,
    SUBSCRIBED_BY VARCHAR(100) NOT NULL,
    SUBSCRIBED_TIMESTAMP TIMESTAMP(0) NOT NULL,
    UNSUBSCRIBED BOOLEAN NOT NULL DEFAULT false,
    UNSUBSCRIBED_BY VARCHAR(100) NULL DEFAULT NULL,
    UNSUBSCRIBED_TIMESTAMP TIMESTAMP(0) NULL DEFAULT NULL,
    AP_APP_RELEASE_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_AP_USER_SUBSCRIPTION_AP_APP_RELEASE1
        FOREIGN KEY (AP_APP_RELEASE_ID)
            REFERENCES AP_APP_RELEASE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX fk_AP_USER_SUBSCRIPTION_AP_APP_RELEASE1_idx ON AP_USER_SUBSCRIPTION (AP_APP_RELEASE_ID ASC);

-- -----------------------------------------------------
-- Table AP_APP_CATEGORY
-- -----------------------------------------------------
CREATE SEQUENCE AP_APP_CATEGORY_seq;

CREATE TABLE IF NOT EXISTS AP_APP_CATEGORY(
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('AP_APP_CATEGORY_seq'),
    TENANT_ID INTEGER NOT NULL,
    CATEGORY VARCHAR(45) NOT NULL,
    CATEGORY_ICON VARCHAR(45) NULL,
    PRIMARY KEY (ID)
);

-- -----------------------------------------------------
-- Table AP_APP_TAG_MAPPING
-- -----------------------------------------------------
CREATE SEQUENCE AP_APP_TAG_MAPPING_seq;

CREATE TABLE IF NOT EXISTS AP_APP_TAG_MAPPING(
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('AP_APP_TAG_MAPPING_seq'),
    TENANT_ID INTEGER NOT NULL,
    AP_APP_TAG_ID INTEGER NOT NULL,
    AP_APP_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_AP_APP_TAG_copy1_AP_APP_TAG1
        FOREIGN KEY (AP_APP_TAG_ID)
            REFERENCES AP_APP_TAG (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_AP_APP_TAG_copy1_AP_APP1
        FOREIGN KEY (AP_APP_ID)
            REFERENCES AP_APP (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX fk_AP_APP_TAG_copy1_AP_APP_TAG1_idx ON AP_APP_TAG_MAPPING (AP_APP_TAG_ID ASC);
CREATE INDEX fk_AP_APP_TAG_copy1_AP_APP1_idx ON AP_APP_TAG_MAPPING (AP_APP_ID ASC);

-- -----------------------------------------------------
-- Table AP_APP_CATEGORY_MAPPING
-- -----------------------------------------------------
CREATE SEQUENCE AP_APP_CATEGORY_MAPPING_seq;

CREATE TABLE IF NOT EXISTS AP_APP_CATEGORY_MAPPING(
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('AP_APP_CATEGORY_MAPPING_seq'),
    TENANT_ID INTEGER NOT NULL,
    AP_APP_CATEGORY_ID INTEGER NOT NULL,
    AP_APP_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_AP_APP_CATEGORY_copy1_AP_APP_CATEGORY1
        FOREIGN KEY (AP_APP_CATEGORY_ID)
            REFERENCES AP_APP_CATEGORY (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_AP_APP_CATEGORY_copy1_AP_APP1
        FOREIGN KEY (AP_APP_ID)
            REFERENCES AP_APP (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX fk_AP_APP_CATEGORY_copy1_AP_APP_CATEGORY1_idx ON AP_APP_CATEGORY_MAPPING (AP_APP_CATEGORY_ID ASC);
CREATE INDEX fk_AP_APP_CATEGORY_copy1_AP_APP1_idx ON AP_APP_CATEGORY_MAPPING (AP_APP_ID ASC);

-- -----------------------------------------------------
-- Table AP_APP_SUB_OP_MAPPING
-- -----------------------------------------------------
CREATE SEQUENCE AP_APP_SUB_OP_MAPPING_seq;

CREATE TABLE IF NOT EXISTS AP_APP_SUB_OP_MAPPING (
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('AP_APP_SUB_OP_MAPPING_seq'),
    TENANT_ID INTEGER NOT NULL,
    OPERATION_ID INTEGER NOT NULL,
    AP_DEVICE_SUBSCRIPTION_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_AP_APP_SUB_OP_MAPPING_AP_DEVICE_SUBSCRIPTION1
        FOREIGN KEY (AP_DEVICE_SUBSCRIPTION_ID)
            REFERENCES AP_DEVICE_SUBSCRIPTION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX fk_AP_APP_SUB_OP_MAPPING_AP_DEVICE_SUBSCRIPTION1_idx ON AP_APP_SUB_OP_MAPPING (AP_DEVICE_SUBSCRIPTION_ID ASC);

-- -----------------------------------------------------
-- Table AP_SCHEDULED_SUBSCRIPTION
-- -----------------------------------------------------
CREATE SEQUENCE AP_SCHEDULED_SUBSCRIPTION_seq;

CREATE TABLE IF NOT EXISTS AP_SCHEDULED_SUBSCRIPTION(
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('AP_SCHEDULED_SUBSCRIPTION_seq'),
    TASK_NAME VARCHAR(100) NOT NULL,
    APPLICATION_UUID VARCHAR(36) NOT NULL,
    SUBSCRIBER_LIST TEXT NOT NULL,
    STATUS VARCHAR(15) NOT NULL,
    SCHEDULED_AT TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    SCHEDULED_BY VARCHAR(100) NOT NULL,
    SCHEDULED_TIMESTAMP TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    DELETED BOOLEAN,
    PRIMARY KEY (ID)
);
