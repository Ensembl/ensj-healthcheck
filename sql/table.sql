# Table structure for healthcheck output database

# Healthcheck running sessions.

CREATE TABLE session (

  session_id                            INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  db_release                            INT(10) NOT NULL,
  host					VARCHAR(255),
  config                                TEXT,

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
  timestamp				DATETIME,
  testcase				VARCHAR(255),
  result				ENUM("PROBLEM", "CORRECT", "WARNING", "INFO"),
  text					VARCHAR(255),
  team_responsible                      VARCHAR(255),
  
  PRIMARY KEY (report_id),
  KEY first_session_idx(first_session_id),
  KEY last_session_idx(last_session_id),
  KEY testcase_idx(testcase),
  KEY database_name_idx(database_name),
  KEY species_idx(species),
  KEY result_idx(result)

);


# Store annotations about healthcheck results

CREATE TABLE annotation (

  annotation_id               INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  report_id					INT(10) UNSIGNED NOT NULL,
  person 					VARCHAR(255),
  action						ENUM("manual_ok", "under_review", "note", "healthcheck_bug", "manual_ok_all_releases", "manual_ok_this_assembly"),
  comment					VARCHAR(255),
  created_at                 	TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',
  modified_at      			TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by					VARCHAR(255),
  modified_by				VARCHAR(255),
  
  PRIMARY KEY (annotation_id),
  KEY action_idx (action),
  KEY report_idx (report_id)
  
);

# Most recent session
CREATE VIEW recent_session AS 
  SELECT s.*,
	MIN(r.timestamp) AS start_time, 
  	MAX(r.timestamp) AS end_time, 
  	TIMEDIFF(MAX(r.timestamp), MIN(r.timestamp)) AS duration 
   FROM session s, report r 
  WHERE s.session_id=(SELECT MAX(session_id) FROM session)
    AND r.last_session_id=s.session_id
    AND r.text LIKE '#%'
  GROUP BY r.last_session_id;

# View for derived data about sessions 

CREATE VIEW session_v AS 
  SELECT s.*, 
  MIN(r.timestamp) AS start_time, 
  MAX(r.timestamp) AS end_time, 
  TIMEDIFF(MAX(r.timestamp), MIN(r.timestamp)) AS duration 
  FROM session s, report r 
  WHERE s.session_id=r.last_session_id 
  AND r.text LIKE '#%' 
  GROUP BY r.last_session_id;

# View for derived data about reports

CREATE VIEW timings AS

  SELECT last_session_id, database_name, species, database_type, testcase,
  MIN(timestamp) AS start_time, 
  MAX(timestamp) AS end_time, 
  TIMEDIFF(MAX(timestamp), MIN(timestamp)) AS duration
  FROM report 
  WHERE text LIKE '#%' 
  GROUP BY last_session_id, database_name, testcase;

