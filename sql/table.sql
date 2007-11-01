# Table structure for healthcheck output database

# Healthcheck running sessions.

CREATE TABLE session (

  session_id                            INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  db_release                            INT(10) NOT NULL,
  host					VARCHAR(255),
  groups				VARCHAR(255),
  database_regexp			VARCHAR(255),
  
  PRIMARY KEY (session_id)
  
);

# Individual healthcheck reports

CREATE TABLE report (

  report_id				INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  first_session_id			INT(10) UNSIGNED NOT NULL,
  last_session_id			INT(10) UNSIGNED NOT NULL,
  database_name				VARCHAR(255),
  species				VARCHAR(255),    # ENUM?
  database_type				VARCHAR(255),    # ENUM?
  start_time				DATETIME,
  end_time				DATETIME,
  testcase				VARCHAR(255),
  result				ENUM("PROBLEM", "CORRECT", "WARNING", "INFO"),
  text					VARCHAR(255),
  
  PRIMARY KEY (report_id),
  KEY first_session_idx(first_session_id),
  KEY last_session_idx(last_session_id)
  
);

# Store annotations about healthcheck results

CREATE TABLE annotation (

  annotation_id               INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  report_id					INT(10) UNSIGNED NOT NULL,
  person 					VARCHAR(255),
  action						ENUM("manual_ok", "under_review", "note", "healthcheck_bug", "manual_ok_all_releases"),
  comment					VARCHAR(255),
  created_at                 	TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',
  modified_at      			TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by					VARCHAR(255),
  modified_by				VARCHAR(255),
  
  PRIMARY KEY (annotation_id)
  
);

