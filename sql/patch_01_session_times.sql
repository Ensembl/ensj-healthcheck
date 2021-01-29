-- Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
-- Copyright [2016-2021] EMBL-European Bioinformatics Institute
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

alter table session add column end_time DATETIME after db_release;
alter table session add column start_time DATETIME after db_release;

drop view recent_session;
drop view session_v;

-- Most recent session
CREATE VIEW recent_session AS
  SELECT s.*,
  	TIMEDIFF(end_time, start_time) AS duration 
   FROM session s, report r 
  WHERE s.session_id=(SELECT MAX(session_id) FROM session)
    AND r.last_session_id=s.session_id
    AND r.text LIKE '#%'
  GROUP BY r.last_session_id;

-- View for derived data about sessions 

CREATE VIEW session_v AS 
  SELECT s.*, 
  TIMEDIFF(end_time, start_time) AS duration 
  FROM session s, report r 
  WHERE s.session_id=r.last_session_id 
  AND r.text LIKE '#%' 
  GROUP BY r.last_session_id;

