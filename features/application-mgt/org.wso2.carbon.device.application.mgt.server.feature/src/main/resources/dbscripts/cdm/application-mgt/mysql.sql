-- MySQL Script generated by MySQL Workbench
-- 2017-06-14 12:46:43 +0530
-- Model: New Model    Version: 1.0
-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema WSO2DM_APPM_DB
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Table `APPM_PLATFORM`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS APPM_PLATFORM (
ID INT NOT NULL AUTO_INCREMENT UNIQUE,
IDENTIFIER VARCHAR (100) NOT NULL,
TENANT_ID INT NOT NULL ,
NAME VARCHAR (255),
FILE_BASED BOOLEAN,
DESCRIPTION VARCHAR (2048),
IS_SHARED BOOLEAN,
IS_DEFAULT_TENANT_MAPPING BOOLEAN,
ICON_NAME VARCHAR (100),
PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS APPM_PLATFORM_PROPERTIES (
ID INT NOT NULL AUTO_INCREMENT,
PLATFORM_ID INT NOT NULL,
PROP_NAME VARCHAR (100) NOT NULL,
OPTIONAL BOOLEAN,
DEFAUL_VALUE VARCHAR (255),
FOREIGN KEY(PLATFORM_ID) REFERENCES APPM_PLATFORM(ID) ON DELETE CASCADE,
PRIMARY KEY (ID, PLATFORM_ID, PROP_NAME)
);


-- -----------------------------------------------------
-- Table `APPM_PLATFORM_TENENT_MAPPING`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `APPM_PLATFORM_TENANT_MAPPING` (
  `ID` INT NOT NULL AUTO_INCREMENT,
  `PLATFORM_ID` INT NOT NULL,
  `TENANT_ID` INT NOT NULL,
  PRIMARY KEY (`ID`, `PLATFORM_ID`),
  INDEX `FK_PLATFROM_TENANT_MAPPING_PLATFORM` (`PLATFORM_ID` ASC),
  CONSTRAINT `fk_APPM_PLATFORM_TENANT_MAPPING_APPM_SUPPORTED_PLATFORM1`
  FOREIGN KEY (`PLATFORM_ID`)
  REFERENCES `APPM_PLATFORM` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE )
  ENGINE = InnoDB
  COMMENT = 'This table contains the data related relationship between application platofrm and appication mappings';

-- -----------------------------------------------------
-- Table `APPM_APPLICATION_CATEGORY`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `APPM_APPLICATION_CATEGORY` (
  `ID` INT NOT NULL AUTO_INCREMENT,
  `NAME` VARCHAR(100) NOT NULL,
  `DESCRIPTION` TEXT NULL,
  `PUBLISHED` TINYINT(1) NULL,
  PRIMARY KEY (`ID`))
  ENGINE = InnoDB
  COMMENT = 'This table contains the data related to the application category';

INSERT INTO APPM_APPLICATION_CATEGORY (NAME, DESCRIPTION, PUBLISHED) VALUES ('Enterprise',
'Enterprise level applications which the artifacts need to be provided', 1);
INSERT INTO APPM_APPLICATION_CATEGORY (NAME, DESCRIPTION, PUBLISHED) VALUES ('Public',
'Public category in which the application need to be downloaded from the public application store', 1);

-- -----------------------------------------------------
-- Table `APPM_LIFECYCLE_STATE`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS APPM_LIFECYCLE_STATE (
  ID INT NOT NULL AUTO_INCREMENT UNIQUE,
  NAME VARCHAR(100) NOT NULL,
  IDENTIFIER VARCHAR(100) NOT NULL,
  DESCRIPTION TEXT NULL,
  PRIMARY KEY (ID),
  UNIQUE INDEX APPM_LIFECYCLE_STATE_IDENTIFIER_UNIQUE (IDENTIFIER ASC));

INSERT INTO APPM_LIFECYCLE_STATE (NAME, IDENTIFIER, DESCRIPTION) VALUES ('CREATED', 'CREATED',
'Application creation initial state');
INSERT INTO APPM_LIFECYCLE_STATE (NAME, IDENTIFIER, DESCRIPTION)
VALUES ('IN REVIEW', 'IN REVIEW', 'Application is in in-review state');
INSERT INTO APPM_LIFECYCLE_STATE (NAME, IDENTIFIER, DESCRIPTION)
VALUES ('APPROVED', 'APPROVED', 'State in which Application is approved after reviewing.');
INSERT INTO APPM_LIFECYCLE_STATE (NAME, IDENTIFIER, DESCRIPTION)
VALUES ('REJECTED', 'REJECTED', 'State in which Application is rejected after reviewing.');
INSERT INTO APPM_LIFECYCLE_STATE (NAME, IDENTIFIER, DESCRIPTION)
VALUES ('PUBLISHED', 'PUBLISHED', 'State in which Application is in published state.');
INSERT INTO APPM_LIFECYCLE_STATE (NAME, IDENTIFIER, DESCRIPTION)
VALUES ('UNPUBLISHED', 'UNPUBLISHED', 'State in which Application is in un published state.');
INSERT INTO APPM_LIFECYCLE_STATE (NAME, IDENTIFIER, DESCRIPTION)
VALUES ('RETIRED', 'RETIRED', 'Retiring an application to indicate end of life state,');


CREATE TABLE IF NOT EXISTS APPM_LC_STATE_TRANSITION
(
  ID INT NOT NULL AUTO_INCREMENT UNIQUE,
  INITIAL_STATE INT,
  NEXT_STATE INT,
  PERMISSION VARCHAR(1024),
  DESCRIPTION VARCHAR(2048),
  PRIMARY KEY (INITIAL_STATE, NEXT_STATE),
  FOREIGN KEY (INITIAL_STATE) REFERENCES APPM_LIFECYCLE_STATE(ID) ON DELETE CASCADE,
  FOREIGN KEY (NEXT_STATE) REFERENCES APPM_LIFECYCLE_STATE(ID) ON DELETE CASCADE
);

INSERT INTO APPM_LC_STATE_TRANSITION(INITIAL_STATE, NEXT_STATE, PERMISSION, DESCRIPTION) VALUES
  (1, 2, null, 'Submit for review');
INSERT INTO APPM_LC_STATE_TRANSITION(INITIAL_STATE, NEXT_STATE, PERMISSION, DESCRIPTION) VALUES
  (2, 1, null, 'Revoke from review');
INSERT INTO APPM_LC_STATE_TRANSITION(INITIAL_STATE, NEXT_STATE, PERMISSION, DESCRIPTION) VALUES
  (2, 3, '/permission/admin/manage/device-mgt/application/review', 'APPROVE');
INSERT INTO APPM_LC_STATE_TRANSITION(INITIAL_STATE, NEXT_STATE, PERMISSION, DESCRIPTION) VALUES
  (2, 4, '/permission/admin/manage/device-mgt/application/review', 'REJECT');
INSERT INTO APPM_LC_STATE_TRANSITION(INITIAL_STATE, NEXT_STATE, PERMISSION, DESCRIPTION) VALUES
  (3, 4, '/permission/admin/manage/device-mgt/application/review', 'REJECT');
INSERT INTO APPM_LC_STATE_TRANSITION(INITIAL_STATE, NEXT_STATE, PERMISSION, DESCRIPTION) VALUES
  (3, 5, null, 'PUBLISH');
INSERT INTO APPM_LC_STATE_TRANSITION(INITIAL_STATE, NEXT_STATE, PERMISSION, DESCRIPTION) VALUES
  (5, 6, null, 'UN PUBLISH');
INSERT INTO APPM_LC_STATE_TRANSITION(INITIAL_STATE, NEXT_STATE, PERMISSION, DESCRIPTION) VALUES
  (6, 5, null, 'PUBLISH');
INSERT INTO APPM_LC_STATE_TRANSITION(INITIAL_STATE, NEXT_STATE, PERMISSION, DESCRIPTION) VALUES
  (4, 1, null, 'Return to CREATE STATE');
INSERT INTO APPM_LC_STATE_TRANSITION(INITIAL_STATE, NEXT_STATE, PERMISSION, DESCRIPTION) VALUES
  (6, 1, null, 'Return to CREATE STATE');
INSERT INTO APPM_LC_STATE_TRANSITION(INITIAL_STATE, NEXT_STATE, PERMISSION, DESCRIPTION) VALUES
  (6, 7, null, 'Retire');

-- -----------------------------------------------------
-- Table `APPM_APPLICATION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `APPM_APPLICATION` (
  `ID` INT NOT NULL AUTO_INCREMENT,
  `UUID` VARCHAR(100) NOT NULL,
  `NAME` VARCHAR(100) NOT NULL,
  `SHORT_DESCRIPTION` VARCHAR(255) NULL,
  `DESCRIPTION` TEXT NULL,
  `VIDEO_NAME` VARCHAR(100) NULL,
  `SCREEN_SHOT_COUNT` INT DEFAULT 0,
  `CREATED_BY` VARCHAR(255) NULL,
  `CREATED_AT` DATETIME NOT NULL,
  `MODIFIED_AT` DATETIME NULL,
  `IS_FREE` TINYINT(1) NULL,
  `PAYMENT_CURRENCY` VARCHAR(45) NULL,
  `PAYMENT_PRICE` DECIMAL(10,2) NULL,
  `APPLICATION_CATEGORY_ID` INT NOT NULL,
  `LIFECYCLE_STATE_ID` INT NOT NULL,
  `LIFECYCLE_STATE_MODIFIED_BY` VARCHAR(255) NULL,
  `LIFECYCLE_STATE_MODIFIED_AT` DATETIME NULL,
  `TENANT_ID` INT NULL,
  `PLATFORM_ID` INT NOT NULL,
  PRIMARY KEY (`ID`, `APPLICATION_CATEGORY_ID`, `LIFECYCLE_STATE_ID`, `PLATFORM_ID`),
  UNIQUE INDEX `UUID_UNIQUE` (`UUID` ASC),
  INDEX `FK_APPLICATION_APPLICATION_CATEGORY` (`APPLICATION_CATEGORY_ID` ASC),
  INDEX `FK_APPLICATION_LIFECYCLE_STATE` (`LIFECYCLE_STATE_ID` ASC),
  INDEX `FK_APPM_APPLICATION_APPM_PLATFORM` (`PLATFORM_ID` ASC),
  CONSTRAINT `FK_APPLICATION_APPLICATION_CATEGORY`
  FOREIGN KEY (`APPLICATION_CATEGORY_ID`)
  REFERENCES `APPM_APPLICATION_CATEGORY` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_APPM_APPLICATION_APPM_LIFECYCLE_STATE1`
  FOREIGN KEY (`LIFECYCLE_STATE_ID`)
  REFERENCES `APPM_LIFECYCLE_STATE` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_APPM_APPLICATION_APPM_PLATFORM1`
  FOREIGN KEY (`PLATFORM_ID`)
  REFERENCES `APPM_PLATFORM` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  COMMENT = 'This table contains the data related to the applications';


-- -----------------------------------------------------
-- Table `APPM_APPLICATION_PROPERTY`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `APPM_APPLICATION_PROPERTY` (
  `PROP_KEY` VARCHAR(255) NOT NULL,
  `PROP_VAL` TEXT NULL,
  `APPLICATION_ID` INT NOT NULL,
  PRIMARY KEY (`PROP_KEY`, `APPLICATION_ID`),
  INDEX `FK_APPLICATION_PROPERTY_APPLICATION` (`APPLICATION_ID` ASC),
  CONSTRAINT `FK_APPLICATION_PROPERTY_APPLICATION`
  FOREIGN KEY (`APPLICATION_ID`)
  REFERENCES `APPM_APPLICATION` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  COMMENT = 'This table contains the data related to the properties that related to the application';


-- -----------------------------------------------------
-- Table `APPM_APPLICATION_RELEASE`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `APPM_APPLICATION_RELEASE` (
  `ID` INT NOT NULL AUTO_INCREMENT UNIQUE ,
  `VERSION_NAME` VARCHAR(100) NOT NULL,
  `RELEASE_RESOURCE` TEXT NULL,
  `RELEASE_CHANNEL` VARCHAR(50) DEFAULT 'ALPHA',
  `RELEASE_DETAILS` TEXT NULL,
  `CREATED_AT` DATETIME NOT NULL,
  `APPM_APPLICATION_ID` INT NOT NULL,
  `IS_DEFAULT` TINYINT(1) NULL,
  PRIMARY KEY (`APPM_APPLICATION_ID`, `VERSION_NAME`),
  INDEX `FK_APPLICATION_VERSION_APPLICATION` (`APPM_APPLICATION_ID` ASC),
  CONSTRAINT `FK_APPLICATION_VERSION_APPLICATION`
  FOREIGN KEY (`APPM_APPLICATION_ID`)
  REFERENCES `APPM_APPLICATION` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  COMMENT = 'This table contains the data related to the application releases';


-- -----------------------------------------------------
-- Table `APPM_RELEASE_PROPERTY`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `APPM_RELEASE_PROPERTY` (
  `PROP_KEY` VARCHAR(255) NOT NULL,
  `PROP_VALUE` TEXT NULL,
  `APPLICATION_RELEASE_ID` INT NOT NULL,
  PRIMARY KEY (`PROP_KEY`, `APPLICATION_RELEASE_ID`),
  INDEX `FK_RELEASE_PROPERTY_APPLICATION_RELEASE` (`APPLICATION_RELEASE_ID` ASC),
  CONSTRAINT `FK_RELEASE_PROPERTY_APPLICATION_RELEASE`
  FOREIGN KEY (`APPLICATION_RELEASE_ID`)
  REFERENCES `APPM_APPLICATION_RELEASE` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  COMMENT = 'This table contains the data related to the properties that related to the application release';


-- -----------------------------------------------------
-- Table `APPM_RESOURCE_TYPE`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `APPM_RESOURCE_TYPE` (
  `ID` INT NOT NULL AUTO_INCREMENT,
  `NAME` VARCHAR(45) NULL,
  `DESCRIPTION` TEXT NULL,
  PRIMARY KEY (`ID`))
  ENGINE = InnoDB;

INSERT INTO APPM_RESOURCE_TYPE (NAME , DESCRIPTION) VALUES ('PUBLIC', 'OPEN VISIBILITY, CAN BE VIEWED BY ALL LOGGED IN USERS');
INSERT INTO APPM_RESOURCE_TYPE (NAME , DESCRIPTION) VALUES ('ROLES', 'ROLE BASED RESTRICTION, CAN BE VIEWED BY ONLY GIVEN
 SET OF USER WHO HAVE THE SPECIFIED ROLE');
INSERT INTO APPM_RESOURCE_TYPE (NAME , DESCRIPTION) VALUES ('DEVICE_GROUPS', 'DEVICE GROUP LEVEL RESTRICTION,
CAN BE VIEWED BY THE DEVICES/ROLES BELONG TO THE GROUP');


-- -----------------------------------------------------
-- Table `APPM_SUBSCRIPTION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `APPM_SUBSCRIPTION` (
  `ID` INT NOT NULL AUTO_INCREMENT,
  `VALUE` VARCHAR(255) NOT NULL,
  `CREATED_AT` DATETIME NOT NULL,
  `RESOURCE_TYPE_ID` INT NOT NULL,
  `APPLICATION_ID` INT NOT NULL,
  `APPLICATION_RELEASE_ID` INT NULL,
  PRIMARY KEY (`ID`, `APPLICATION_ID`, `RESOURCE_TYPE_ID`),
  INDEX `FK_APPLICATION_SUBSCRIPTION_RESOURCE_TYPE` (`RESOURCE_TYPE_ID` ASC),
  INDEX `FK_APPLICATION_SUBSCRIPTION_APPLICATION` (`APPLICATION_ID` ASC),
  INDEX `FK_APPLICATION_SUBSCRIPTION_APPLICATION_RELEASE` (`APPLICATION_RELEASE_ID` ASC),
  CONSTRAINT `fk_APPM_APPLICATION_SUBSCRIPTION_APPM_RESOURCE_TYPE1`
  FOREIGN KEY (`RESOURCE_TYPE_ID`)
  REFERENCES `APPM_RESOURCE_TYPE` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_APPM_APPLICATION_SUBSCRIPTION_APPM_APPLICATION1`
  FOREIGN KEY (`APPLICATION_ID`)
  REFERENCES `APPM_APPLICATION` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_APPM_APPLICATION_SUBSCRIPTION_APPM_APPLICATION_RELEASE1`
  FOREIGN KEY (`APPLICATION_RELEASE_ID`)
  REFERENCES `APPM_APPLICATION_RELEASE` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  COMMENT = 'This table contains the data related to the application subscriptions';


-- -----------------------------------------------------
-- Table `APPM_COMMENT`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `APPM_COMMENT` (
  `ID` INT NOT NULL AUTO_INCREMENT,
  `APPLICATION_RELEASE_ID` INT NOT NULL,
  `COMMENT_SUBJECT` VARCHAR(255) NULL,
  `COMMENT_BODY` TEXT NULL,
  `RATING` INT NULL,
  `PARENT_ID` INT NULL,
  `CREATED_AT` DATETIME NOT NULL,
  `CREATED_BY` VARCHAR(45) NULL,
  `MODIFIED_AT` DATETIME NULL,
  `PUBLISHED` TINYINT(1) NULL,
  `APPROVED` TINYINT(1) NULL,
  PRIMARY KEY (`ID`, `APPLICATION_RELEASE_ID`),
  INDEX `FK_APPLICATION_COMMENTS_APPLICATION_RELEASE` (`APPLICATION_RELEASE_ID` ASC),
  CONSTRAINT `FK_APPLICATION_COMMENTS_APPLICATION_RELEASE`
  FOREIGN KEY (`APPLICATION_RELEASE_ID`)
  REFERENCES `APPM_APPLICATION_RELEASE` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  COMMENT = 'This table contains the data related to the application comments';


-- -----------------------------------------------------
-- Table `APPM_PLATFORM_TAG`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `APPM_PLATFORM_TAG` (
  `name` VARCHAR(100) NOT NULL,
  `PLATFORM_ID` INT NOT NULL,
  PRIMARY KEY (`PLATFORM_ID`, `name`),
  INDEX `FK_PLATFORM_TAGS_PLATFORM` (`PLATFORM_ID` ASC),
  CONSTRAINT `fk_APPM_SUPPORTED_PLATFORM_TAGS_APPM_SUPPORTED_PLATFORM1`
  FOREIGN KEY (`PLATFORM_ID`)
  REFERENCES `APPM_PLATFORM` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  COMMENT = 'This table contains the data related to the app platform tag';


-- -----------------------------------------------------
-- Table `APPM_APPLICATION_TAG`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `APPM_APPLICATION_TAG` (
  `NAME` VARCHAR(45) NOT NULL,
  `APPLICATION_ID` INT NOT NULL,
  PRIMARY KEY (`APPLICATION_ID`, `NAME`),
  INDEX `FK_APPLICATION_TAG_APPLICATION` (`APPLICATION_ID` ASC),
  CONSTRAINT `fk_APPM_APPLICATION_TAG_APPM_APPLICATION1`
  FOREIGN KEY (`APPLICATION_ID`)
  REFERENCES `APPM_APPLICATION` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  COMMENT = 'This table contains the data related to the application tags';


-- -----------------------------------------------------
-- Table `APPM_VISIBILITY`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `APPM_VISIBILITY` (
  `ID` INT NOT NULL AUTO_INCREMENT,
  `VALUE` VARCHAR(255),
  `RESOURCE_TYPE_ID` INT NOT NULL,
  `APPLICATION_ID` INT NULL,
  PRIMARY KEY (`ID`),
  INDEX `FK_APPM_VISIBILITY_RESOURCE_TYPE` (`RESOURCE_TYPE_ID` ASC),
  INDEX `FK_VISIBILITY_APPLICATION` (`APPLICATION_ID` ASC),
  CONSTRAINT `fk_APPM_VISIBILITY_APPM_RESOURCE_TYPE1`
  FOREIGN KEY (`RESOURCE_TYPE_ID`)
  REFERENCES `APPM_RESOURCE_TYPE` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_APPM_VISIBILITY_APPM_APPLICATION1`
  FOREIGN KEY (`APPLICATION_ID`)
  REFERENCES `APPM_APPLICATION` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE )
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `APPM_SUBSCRIPTION_PROPERTIES`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `APPM_SUBSCRIPTION_PROPERTIES` (
  `PROP_KEY` VARCHAR(255) NOT NULL,
  `PROP_VALUE` TEXT NULL,
  `APPM_SUBSCRIPTION_ID` INT NOT NULL,
  PRIMARY KEY (`PROP_KEY`, `APPM_SUBSCRIPTION_ID`),
  INDEX `FK_SUBSCRIPTION_PROPERTIES_SUBSCRIPTION` (`APPM_SUBSCRIPTION_ID` ASC),
  CONSTRAINT `fk_APPM_SUBSCRIPTION_PROPERTIES_APPM_SUBSCRIPTION1`
  FOREIGN KEY (`APPM_SUBSCRIPTION_ID`)
  REFERENCES `APPM_SUBSCRIPTION` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `APPM_DEVICE_APPLICATION_MAPPING`
-- -----------------------------------------------------
-- CREATE TABLE IF NOT EXISTS `APPM_DEVICE_APPLICATION_MAPPING` (
--  `ID` INT AUTO_INCREMENT NOT NULL,
--  `DEVICE_IDENTIFIER`  VARCHAR(255) NOT NULL,
--  `APPLICATION_UUID` VARCHAR(100) NOT NULL,
--  `INSTALLED` BOOLEAN NOT NULL,
--  `SENT_AT` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
--  PRIMARY KEY (ID),
--  CONSTRAINT `fk_appm_application` FOREIGN KEY (`APPLICATION_UUID`) REFERENCES
--  APPM_APPLICATION (`UUID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
--  UNIQUE KEY `device_app_mapping` (`DEVICE_IDENTIFIER`, `APPLICATION_UUID`)
-- ) ENGINE = InnoDB;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
