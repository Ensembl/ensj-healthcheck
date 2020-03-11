-- Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
-- Copyright [2016-2020] EMBL-European Bioinformatics Institute
-- 
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--      http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.


CREATE TABLE session (

  session_id                            INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  db_release                            INT(10) NOT NULL,
  host					VARCHAR(255),
  config                                TEXT,

  PRIMARY KEY (session_id)
  
);


CREATE TABLE report (

  report_id				INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  first_session_id			INT(10) UNSIGNED NOT NULL,
  last_session_id			INT(10) UNSIGNED NOT NULL,
  database_name				VARCHAR(255),
  species				VARCHAR(255),
  database_type				VARCHAR(255),
  timestamp				DATETIME,
  testcase				VARCHAR(255),
  result				ENUM("PROBLEM", "CORRECT", "WARNING", "INFO"),
  text					TEXT,
  team_responsible                      VARCHAR(255),
  created                               DATETIME,
  
  PRIMARY KEY (report_id),
  KEY first_session_idx(first_session_id),
  KEY last_session_idx(last_session_id),
  KEY testcase_idx(testcase),
  KEY database_name_idx(database_name),
  KEY species_idx(species),
  KEY result_idx(result),
  KEY text_idx(text(255))

);



CREATE TABLE annotation (

  annotation_id               INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  report_id					INT(10) UNSIGNED NOT NULL,
  person 					VARCHAR(255),
  action						ENUM("manual_ok", "under_review", "note", "healthcheck_bug", "manual_ok_all_releases", "manual_ok_this_assembly", "manual_ok_this_genebuild"),
  comment					VARCHAR(255),
  created_at                 	TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',
  modified_at      			TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by					VARCHAR(255),
  modified_by				VARCHAR(255),
  
  PRIMARY KEY (annotation_id),
  KEY action_idx (action),
  KEY report_idx (report_id)
  
);

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


CREATE VIEW session_v AS 
  SELECT s.*, 
  MIN(r.timestamp) AS start_time, 
  MAX(r.timestamp) AS end_time, 
  TIMEDIFF(MAX(r.timestamp), MIN(r.timestamp)) AS duration 
  FROM session s, report r 
  WHERE s.session_id=r.last_session_id 
  AND r.text LIKE '#%' 
  GROUP BY r.last_session_id;

CREATE VIEW session_v2 AS 
 SELECT s.session_id, 
  MAX(r.timestamp) AS end_time
  FROM session s, report r 
  WHERE s.session_id=r.last_session_id 
  AND r.text LIKE '#%' 
  GROUP BY r.last_session_id;


CREATE VIEW timings AS

  SELECT last_session_id, database_name, species, database_type, testcase,
  MIN(timestamp) AS start_time, 
  MAX(timestamp) AS end_time, 
  TIMEDIFF(MAX(timestamp), MIN(timestamp)) AS duration
  FROM report 
  WHERE text LIKE '#%' 
  GROUP BY last_session_id, database_name, testcase;

