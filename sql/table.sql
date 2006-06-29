# Table structure for healthcheck output database

# Healthcheck running sessions.

CREATE TABLE session (

  session_id					INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  start_time					DATETIME,
  end_time					DATETIME,
  release					INT,
  
  PRIMARY KEY (session_id),
  KEY release_idx(release)
  
);

# Individual healthcheck reports

CREATE TABLE report (

  report_id					INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  session_id					INT(10) UNSIGNED NOT NULL,
  species					VARCHAR(255),
  testcase					VARCHAR(255),
  result						ENUM("PROBLEM", "CORRECT", "WARNING", "INFO"),
  text						VARCHAR(255),
  
  PRIMARY KEY (report_id),
  KEY session_idx(session_id)
  
);

# Store annotations about healthcheck results

CREATE TABLE annotation (

  report_id					INT(10) UNSIGNED NOT NULL,
  person						VARCHAR(255),
  action						ENUM("ignore", "normal", "flag"),
  reason						ENUM("not relevant", "will be fixed", "healthcheck bug"),
  comment					VARCHAR(255)
  
);

