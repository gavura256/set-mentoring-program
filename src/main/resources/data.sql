INSERT INTO USER_STORY (jira_id,summary) VALUES
	 ('PMCCP-91316','Login to app'),
	 ('PMCCP-12345','Implement possibility to register user'),
	 ('PMCCP-913167','Add button +'),
     ('PMCCP-123458','Add button -'),
     ('PMCCP-913169','Add button /'),
     ('PMCCP-123450','Add button *');

INSERT INTO feature_file (`path`,filename) VALUES
	 ('/automation/educator-ui-automation/src/main/resources/features/sift/LoginEventForSiftValidation.feature','LoginEventForSiftValidation.feature'),
	 ('/automation/educator-ui-automation/src/main/resources/features/sift/Registration.feature','Registration.feature'),
     ('/automation/educator-ui-automation/src/main/resources/features/sift/Calculator.feature','Calculator.feature');


INSERT INTO test_scenario (data_rows,is_manual,is_wiped,priority,summary,feature_file_id) VALUES
	 (1,0,0,'MEDIUM','Login event is sent to the sift console after success login on react page',1),
	 (1,0,0,'HIGH','Login successful',1),
	 (1,0,0,'MEDIUM','Register new user',2),
	 (1,0,0,'MEDIUM','Register new user with invalid age',2),
	 (1,0,0,'MEDIUM','Test for button +',3),
	 (2,1,0,'LOW','Test for button -',3),
	 (5,0,1,'HIGH','Test for button *',3),
	 (4,0,1,'HIGH','Test for button /',3),
	 (3,0,0,'HIGH','Test for division by zero /',3);


INSERT INTO story_scenario (scenario_id,story_id) VALUES
	 (1,1),
	 (2,1),
	 (3,2),
	 (4,2),
	 (4,3),
	 (5,3),
	 (6,4),
	 (7,5),
	 (8,6),
	 (9,6);

INSERT INTO countries (scenario_id, country_name) VALUES
     (1, 'USA'),
     (1, 'CANADA'),
     (2, 'USA'),
     (2, 'UK'),
     (3, 'USA'),
     (4, 'CANADA'),
     (4, 'USA'),
     (4, 'UK')
