ALTER TABLE CODE_TEMPLATE RENAME TO OLD_CODE_TEMPLATE

CREATE TABLE CODE_TEMPLATE_LIBRARY
	(ID VARCHAR(255) NOT NULL PRIMARY KEY,
	NAME VARCHAR(255) NOT NULL UNIQUE,
	REVISION INTEGER,
	LIBRARY TEXT)

CREATE TABLE CODE_TEMPLATE
	(ID VARCHAR(255) NOT NULL PRIMARY KEY,
	NAME VARCHAR(255) NOT NULL,
	REVISION INTEGER,
	CODE_TEMPLATE TEXT)