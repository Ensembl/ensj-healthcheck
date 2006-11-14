# Table structure for healthcheck output database

# Healthcheck running sessions.

CREATE TABLE session (

  session_id					INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  start_time					DATETIME,
  end_time					DATETIME,
  host						VARCHAR(255),
  groups						VARCHAR(255),
  database_regexp			VARCHAR(255),
  
  PRIMARY KEY (session_id)
  
);

# Individual healthcheck reports

CREATE TABLE report (

  report_id					INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  first_session_id			INT(10) UNSIGNED NOT NULL,
  last_session_id			INT(10) UNSIGNED NOT NULL,
  database_name				VARCHAR(255),
  species					VARCHAR(255),    # ENUM?
  database_type				VARCHAR(255),    # ENUM?
  testcase					VARCHAR(255),
  result						ENUM("PROBLEM", "CORRECT", "WARNING", "INFO"),
  text						VARCHAR(255),
  
  PRIMARY KEY (report_id),
  KEY first_session_idx(first_session_id),
  KEY last_session_idx(last_session_id)
  
);

# Store annotations about healthcheck results

CREATE TABLE annotation (

  annotation_id               INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  report_id					INT(10) UNSIGNED NOT NULL,
  user_id 					VARCHAR(255),
  action						ENUM("manual_ok", "under_review", "note", "irrelevant", "healthcheck_bug", "manual_ok_all_releases"),
  comment					VARCHAR(255),
  
  PRIMARY KEY (annotation_id)
  
);

