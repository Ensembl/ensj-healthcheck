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

