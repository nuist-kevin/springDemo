USE `springdemo`;
DROP procedure IF EXISTS `insert_jobs_proc`;

DELIMITER $$
USE `springdemo`$$
CREATE  PROCEDURE `insert_jobs_proc` (IN input_user_id int, IN input_user_name varchar(45) , IN input_user_password  varchar(45))
BEGIN
    INSERT INTO users(id, username, password) VALUES
		(input_user_id ,input_user_name,input_user_password);  
END;$$

DELIMITER ;