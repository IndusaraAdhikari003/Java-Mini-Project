DROP DATABASE IF EXISTS lms_db;
CREATE DATABASE lms_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lms_db;

SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE users (
    user_id      INT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50) UNIQUE NOT NULL,
    password     VARCHAR(255) NOT NULL,
    role         ENUM('ADMIN','LECTURER','TECH_OFFICER','UNDERGRADUATE') NOT NULL,
    full_name    VARCHAR(100) NOT NULL,
    email        VARCHAR(100),
    phone        VARCHAR(20),
    profile_pic  VARCHAR(255),
    department   VARCHAR(100),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE undergraduates (
    ug_id        INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT NOT NULL UNIQUE,
    reg_number   VARCHAR(20) UNIQUE NOT NULL,
    batch        VARCHAR(20) NOT NULL,
    is_repeat    TINYINT(1) DEFAULT 0,
    batch_missed TINYINT(1) DEFAULT 0,
    CONSTRAINT fk_undergraduates_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE courses (
    course_id     INT AUTO_INCREMENT PRIMARY KEY,
    course_code   VARCHAR(20) UNIQUE NOT NULL,
    course_name   VARCHAR(150) NOT NULL,
    credits       INT NOT NULL DEFAULT 1,
    has_theory    TINYINT(1) DEFAULT 1,
    has_practical TINYINT(1) DEFAULT 0,
    department    VARCHAR(100),
    lecturer_id   INT,
    CONSTRAINT fk_courses_lecturer
        FOREIGN KEY (lecturer_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE attendance (
    att_id        INT AUTO_INCREMENT PRIMARY KEY,
    ug_id         INT NOT NULL,
    course_id     INT NOT NULL,
    session_date  DATE NOT NULL,
    session_type  ENUM('THEORY','PRACTICAL') NOT NULL,
    is_present    TINYINT(1) DEFAULT 0,
    CONSTRAINT fk_attendance_ug
        FOREIGN KEY (ug_id) REFERENCES undergraduates(ug_id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_course
        FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE medicals (
    medical_id    INT AUTO_INCREMENT PRIMARY KEY,
    ug_id         INT NOT NULL,
    from_date     DATE NOT NULL,
    to_date       DATE NOT NULL,
    reason        TEXT,
    doc_path      VARCHAR(255),
    is_approved   TINYINT(1) DEFAULT 0,
    status        ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    submitted_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_medicals_ug
        FOREIGN KEY (ug_id) REFERENCES undergraduates(ug_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE marks (
    mark_id      INT AUTO_INCREMENT PRIMARY KEY,
    ug_id        INT NOT NULL,
    course_id    INT NOT NULL,
    exam_type    ENUM('CA1','CA2','CA3','ASSIGNMENT','FINAL') NOT NULL,
    marks_value  DECIMAL(5,2) NOT NULL,
    uploaded_by  INT,
    uploaded_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_marks_ug
        FOREIGN KEY (ug_id) REFERENCES undergraduates(ug_id) ON DELETE CASCADE,
    CONSTRAINT fk_marks_course
        FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    CONSTRAINT fk_marks_uploaded_by
        FOREIGN KEY (uploaded_by) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE notices (
    notice_id   INT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    content     TEXT,
    created_by  INT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notices_created_by
        FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE timetables (
    tt_id        INT AUTO_INCREMENT PRIMARY KEY,
    course_id    INT NOT NULL,
    day_of_week  ENUM('MON','TUE','WED','THU','FRI','SAT') NOT NULL,
    start_time   TIME NOT NULL,
    end_time     TIME NOT NULL,
    location     VARCHAR(100),
    session_type ENUM('THEORY','PRACTICAL') NOT NULL,
    CONSTRAINT fk_timetables_course
        FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE course_materials (
    material_id   INT AUTO_INCREMENT PRIMARY KEY,
    course_id     INT NOT NULL,
    title         VARCHAR(200) NOT NULL,
    file_path     VARCHAR(255) NOT NULL,
    uploaded_by   INT,
    uploaded_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course_materials_course
        FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    CONSTRAINT fk_course_materials_uploaded_by
        FOREIGN KEY (uploaded_by) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

SET FOREIGN_KEY_CHECKS = 1;

-- Login passwords:
-- admin        -> admin123
-- lecturers    -> lec123
-- tech officers-> to123
-- students     -> stu123

INSERT INTO users (username,password,role,full_name,email,phone,profile_pic,department) VALUES
('admin',
 'pbkdf2$65536$IXBZlekpPqE+mX5IJ4dz4g==$jy7Am0MhVWH+ol9qxtxGTDxCGXy+ZSfDRiq2UJJmeZ4=',
 'ADMIN','System Admin','admin@tech.lk','0711000000',NULL,'Administration'),

('lec_silva',
 'pbkdf2$65536$soQPaJvfjOl9dWUyOEu33g==$axP2PQp7dqagV5ForJptrBglP2qqigWd5dYm0OOdCEc=',
 'LECTURER','Dr. K. Silva','silva@tech.lk','0712000001','profile_pictures/user_2_profile.png','Information & Communication Technology'),

('lec_perera',
 'pbkdf2$65536$soQPaJvfjOl9dWUyOEu33g==$axP2PQp7dqagV5ForJptrBglP2qqigWd5dYm0OOdCEc=',
 'LECTURER','Dr. S. Perera','perera@tech.lk','0712000002',NULL,'Information & Communication Technology'),

('lec_jayantha',
 'pbkdf2$65536$soQPaJvfjOl9dWUyOEu33g==$axP2PQp7dqagV5ForJptrBglP2qqigWd5dYm0OOdCEc=',
 'LECTURER','Mr. A. Jayantha','jayantha@tech.lk','0712000003',NULL,'Bio System Technology'),

('lec_kumari',
 'pbkdf2$65536$soQPaJvfjOl9dWUyOEu33g==$axP2PQp7dqagV5ForJptrBglP2qqigWd5dYm0OOdCEc=',
 'LECTURER','Ms. R. Kumari','kumari@tech.lk','0712000004',NULL,'Engineering Technology'),

('lec_bandara',
 'pbkdf2$65536$soQPaJvfjOl9dWUyOEu33g==$axP2PQp7dqagV5ForJptrBglP2qqigWd5dYm0OOdCEc=',
 'LECTURER','Dr. M. Bandara','bandara@tech.lk','0712000005',NULL,'Multidisciplinary Studies'),

('to_nimal',
 'pbkdf2$65536$dR22hUK1X2hupUYR3sY92w==$ahq76KImUhvwp7CNkHQtkRhYc/Sase2r6iRc0GpN07Q=',
 'TECH_OFFICER','Mr. Nimal','nimal@tech.lk','0713000001','profile_pictures/user_7_profile.png','Information & Communication Technology'),

('to_kumara',
 'pbkdf2$65536$dR22hUK1X2hupUYR3sY92w==$ahq76KImUhvwp7CNkHQtkRhYc/Sase2r6iRc0GpN07Q=',
 'TECH_OFFICER','Mr. Kumara','kumara@tech.lk','0713000002',NULL,'Bio System Technology'),

('to_saman',
 'pbkdf2$65536$dR22hUK1X2hupUYR3sY92w==$ahq76KImUhvwp7CNkHQtkRhYc/Sase2r6iRc0GpN07Q=',
 'TECH_OFFICER','Mr. Saman','saman@tech.lk','0713000003',NULL,'Engineering Technology'),

('to_dilani',
 'pbkdf2$65536$dR22hUK1X2hupUYR3sY92w==$ahq76KImUhvwp7CNkHQtkRhYc/Sase2r6iRc0GpN07Q=',
 'TECH_OFFICER','Ms. Dilani','dilani@tech.lk','0713000004',NULL,'Multidisciplinary Studies'),

('TG231001',
 'pbkdf2$65536$F5mS6XLAvsRc0Hsa3L/6jg==$m0bMLnJRTVfVfb6Jf6YKRT6f9GzxccXYrYpJL25OAIk=',
 'UNDERGRADUATE','Ashan Fernando','ashan@student.lk','0771001001',NULL,'Information & Communication Technology'),
('TG231002',
 'pbkdf2$65536$F5mS6XLAvsRc0Hsa3L/6jg==$m0bMLnJRTVfVfb6Jf6YKRT6f9GzxccXYrYpJL25OAIk=',
 'UNDERGRADUATE','Bimal Perera','bimal@student.lk','0771001002',NULL,'Information & Communication Technology'),
('TG231003',
 'pbkdf2$65536$F5mS6XLAvsRc0Hsa3L/6jg==$m0bMLnJRTVfVfb6Jf6YKRT6f9GzxccXYrYpJL25OAIk=',
 'UNDERGRADUATE','Chamari Silva','chamari@student.lk','0771001003',NULL,'Information & Communication Technology'),
('TG231004',
 'pbkdf2$65536$F5mS6XLAvsRc0Hsa3L/6jg==$m0bMLnJRTVfVfb6Jf6YKRT6f9GzxccXYrYpJL25OAIk=',
 'UNDERGRADUATE','Dinesh Kumar','dinesh@student.lk','0771001004',NULL,'Information & Communication Technology'),
('TG231005',
 'pbkdf2$65536$F5mS6XLAvsRc0Hsa3L/6jg==$m0bMLnJRTVfVfb6Jf6YKRT6f9GzxccXYrYpJL25OAIk=',
 'UNDERGRADUATE','Eranga Weerasinghe','eranga@student.lk','0771001005',NULL,'Information & Communication Technology'),
('TG231006',
 'pbkdf2$65536$F5mS6XLAvsRc0Hsa3L/6jg==$m0bMLnJRTVfVfb6Jf6YKRT6f9GzxccXYrYpJL25OAIk=',
 'UNDERGRADUATE','Nipun Madushanka','nipun@student.lk','0771001009',NULL,'Information & Communication Technology'),
('TG221006',
 'pbkdf2$65536$F5mS6XLAvsRc0Hsa3L/6jg==$m0bMLnJRTVfVfb6Jf6YKRT6f9GzxccXYrYpJL25OAIk=',
 'UNDERGRADUATE','Fathima Rizvi','fathima@student.lk','0771001006',NULL,'Information & Communication Technology'),
('TG221007',
 'pbkdf2$65536$F5mS6XLAvsRc0Hsa3L/6jg==$m0bMLnJRTVfVfb6Jf6YKRT6f9GzxccXYrYpJL25OAIk=',
 'UNDERGRADUATE','Gayan Mendis','gayan@student.lk','0771001007',NULL,'Information & Communication Technology'),
('TG211008',
 'pbkdf2$65536$F5mS6XLAvsRc0Hsa3L/6jg==$m0bMLnJRTVfVfb6Jf6YKRT6f9GzxccXYrYpJL25OAIk=',
 'UNDERGRADUATE','Rashmi Gunawardena','rashmi@student.lk','0771001008',NULL,'Information & Communication Technology'),

('TG232001',
 'pbkdf2$65536$F5mS6XLAvsRc0Hsa3L/6jg==$m0bMLnJRTVfVfb6Jf6YKRT6f9GzxccXYrYpJL25OAIk=',
 'UNDERGRADUATE','Hiruni Jayasekara','hiruni@student.lk','0771002001',NULL,'Bio System Technology'),
('TG232002',
 'pbkdf2$65536$F5mS6XLAvsRc0Hsa3L/6jg==$m0bMLnJRTVfVfb6Jf6YKRT6f9GzxccXYrYpJL25OAIk=',
 'UNDERGRADUATE','Isuru Pathirana','isuru@student.lk','0771002002',NULL,'Bio System Technology'),
('TG232003',
 'pbkdf2$65536$F5mS6XLAvsRc0Hsa3L/6jg==$m0bMLnJRTVfVfb6Jf6YKRT6f9GzxccXYrYpJL25OAIk=',
 'UNDERGRADUATE','Janaki Dissanayake','janaki@student.lk','0771002003',NULL,'Bio System Technology'),
('TG232004',
 'pbkdf2$65536$F5mS6XLAvsRc0Hsa3L/6jg==$m0bMLnJRTVfVfb6Jf6YKRT6f9GzxccXYrYpJL25OAIk=',
 'UNDERGRADUATE','Piumi Nethmini','piumi@student.lk','0771002005',NULL,'Bio System Technology'),
('TG212004',
 'pbkdf2$65536$F5mS6XLAvsRc0Hsa3L/6jg==$m0bMLnJRTVfVfb6Jf6YKRT6f9GzxccXYrYpJL25OAIk=',
 'UNDERGRADUATE','Sachini Ranaweera','sachini@student.lk','0771002004',NULL,'Bio System Technology'),

('TG233001',
 'pbkdf2$65536$F5mS6XLAvsRc0Hsa3L/6jg==$m0bMLnJRTVfVfb6Jf6YKRT6f9GzxccXYrYpJL25OAIk=',
 'UNDERGRADUATE','Kasun Rajapaksa','kasun@student.lk','0771003001',NULL,'Engineering Technology'),
('TG233002',
 'pbkdf2$65536$F5mS6XLAvsRc0Hsa3L/6jg==$m0bMLnJRTVfVfb6Jf6YKRT6f9GzxccXYrYpJL25OAIk=',
 'UNDERGRADUATE','Lakmali Senanayake','lakmali@student.lk','0771003002',NULL,'Engineering Technology'),
('TG233003',
 'pbkdf2$65536$F5mS6XLAvsRc0Hsa3L/6jg==$m0bMLnJRTVfVfb6Jf6YKRT6f9GzxccXYrYpJL25OAIk=',
 'UNDERGRADUATE','Malith Gunasekara','malith@student.lk','0771003003',NULL,'Engineering Technology'),
('TG233004',
 'pbkdf2$65536$F5mS6XLAvsRc0Hsa3L/6jg==$m0bMLnJRTVfVfb6Jf6YKRT6f9GzxccXYrYpJL25OAIk=',
 'UNDERGRADUATE','Nadeesha Wijeratne','nadeesha@student.lk','0771003004',NULL,'Engineering Technology'),
('TG233006',
 'pbkdf2$65536$F5mS6XLAvsRc0Hsa3L/6jg==$m0bMLnJRTVfVfb6Jf6YKRT6f9GzxccXYrYpJL25OAIk=',
 'UNDERGRADUATE','Ravindu Hasaranga','ravindu@student.lk','0771003006',NULL,'Engineering Technology'),
('TG213005',
 'pbkdf2$65536$F5mS6XLAvsRc0Hsa3L/6jg==$m0bMLnJRTVfVfb6Jf6YKRT6f9GzxccXYrYpJL25OAIk=',
 'UNDERGRADUATE','Thilina Madushanka','thilina@student.lk','0771003005',NULL,'Engineering Technology');

INSERT INTO undergraduates (user_id,reg_number,batch,is_repeat,batch_missed) VALUES
((SELECT user_id FROM users WHERE username='TG231001'),'TG/2023/1001','2023',0,0),
((SELECT user_id FROM users WHERE username='TG231002'),'TG/2023/1002','2023',0,0),
((SELECT user_id FROM users WHERE username='TG231003'),'TG/2023/1003','2023',0,0),
((SELECT user_id FROM users WHERE username='TG231004'),'TG/2023/1004','2023',0,0),
((SELECT user_id FROM users WHERE username='TG231005'),'TG/2023/1005','2023',0,0),
((SELECT user_id FROM users WHERE username='TG231006'),'TG/2023/1006','2023',0,0),
((SELECT user_id FROM users WHERE username='TG221006'),'TG/2022/1006','2022',0,1),
((SELECT user_id FROM users WHERE username='TG221007'),'TG/2022/1007','2022',0,1),
((SELECT user_id FROM users WHERE username='TG211008'),'TG/2021/1008','2021',1,0),
((SELECT user_id FROM users WHERE username='TG232001'),'TG/2023/2001','2023',0,0),
((SELECT user_id FROM users WHERE username='TG232002'),'TG/2023/2002','2023',0,0),
((SELECT user_id FROM users WHERE username='TG232003'),'TG/2023/2003','2023',0,0),
((SELECT user_id FROM users WHERE username='TG232004'),'TG/2023/2004','2023',0,0),
((SELECT user_id FROM users WHERE username='TG212004'),'TG/2021/2004','2021',1,0),
((SELECT user_id FROM users WHERE username='TG233001'),'TG/2023/3001','2023',0,0),
((SELECT user_id FROM users WHERE username='TG233002'),'TG/2023/3002','2023',0,0),
((SELECT user_id FROM users WHERE username='TG233003'),'TG/2023/3003','2023',0,0),
((SELECT user_id FROM users WHERE username='TG233004'),'TG/2023/3004','2023',0,0),
((SELECT user_id FROM users WHERE username='TG233006'),'TG/2023/3006','2023',0,0),
((SELECT user_id FROM users WHERE username='TG213005'),'TG/2021/3005','2021',1,0);

INSERT INTO courses (course_code,course_name,credits,has_theory,has_practical,department,lecturer_id) VALUES
('ICT3101','Object Oriented Programming',3,1,1,'Information & Communication Technology',(SELECT user_id FROM users WHERE username='lec_silva')),
('ICT3102','Database Management Systems',3,1,1,'Information & Communication Technology',(SELECT user_id FROM users WHERE username='lec_perera')),
('ICT3103','Computer Networks',3,1,0,'Information & Communication Technology',(SELECT user_id FROM users WHERE username='lec_silva')),
('BIO3101','Biotechnology Fundamentals',3,1,1,'Bio System Technology',(SELECT user_id FROM users WHERE username='lec_jayantha')),
('BIO3102','Microbiology',3,1,1,'Bio System Technology',(SELECT user_id FROM users WHERE username='lec_jayantha')),
('ENG3101','Engineering Mathematics',3,1,0,'Engineering Technology',(SELECT user_id FROM users WHERE username='lec_kumari')),
('ENG3102','Circuit Analysis',3,1,1,'Engineering Technology',(SELECT user_id FROM users WHERE username='lec_kumari')),
('MDS3101','Research Methodology',2,1,0,'Multidisciplinary Studies',(SELECT user_id FROM users WHERE username='lec_bandara')),
('MDS3102','Statistics for Technology',2,1,0,'Multidisciplinary Studies',(SELECT user_id FROM users WHERE username='lec_bandara'));

INSERT INTO course_materials (course_id,title,file_path,uploaded_by)
SELECT course_id,'OOP Introduction Slides','course_materials/course_1_1713771000000_oop_intro.pdf',
       (SELECT user_id FROM users WHERE username='lec_silva')
FROM courses WHERE course_code='ICT3101';

INSERT INTO course_materials (course_id,title,file_path,uploaded_by)
SELECT course_id,'Java Inheritance Notes','course_materials/course_1_1713771000001_inheritance_notes.pdf',
       (SELECT user_id FROM users WHERE username='lec_silva')
FROM courses WHERE course_code='ICT3101';

INSERT INTO course_materials (course_id,title,file_path,uploaded_by)
SELECT course_id,'DBMS ER Modeling','course_materials/course_2_1713771000002_er_modeling.pdf',
       (SELECT user_id FROM users WHERE username='lec_perera')
FROM courses WHERE course_code='ICT3102';

INSERT INTO course_materials (course_id,title,file_path,uploaded_by)
SELECT course_id,'Biotechnology Lab Guide','course_materials/course_4_1713771000003_lab_guide.pdf',
       (SELECT user_id FROM users WHERE username='lec_jayantha')
FROM courses WHERE course_code='BIO3101';

INSERT INTO course_materials (course_id,title,file_path,uploaded_by)
SELECT course_id,'Circuit Analysis Workbook','course_materials/course_7_1713771000004_workbook.pdf',
       (SELECT user_id FROM users WHERE username='lec_kumari')
FROM courses WHERE course_code='ENG3102';

INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by)
SELECT ug.ug_id,c.course_id,'CA1',ROUND(55+RAND()*40,1),(SELECT user_id FROM users WHERE username='lec_silva')
FROM undergraduates ug, courses c WHERE c.course_code='ICT3101';
INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by)
SELECT ug.ug_id,c.course_id,'CA2',ROUND(50+RAND()*45,1),(SELECT user_id FROM users WHERE username='lec_silva')
FROM undergraduates ug, courses c WHERE c.course_code='ICT3101';
INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by)
SELECT ug.ug_id,c.course_id,'CA3',ROUND(50+RAND()*45,1),(SELECT user_id FROM users WHERE username='lec_silva')
FROM undergraduates ug, courses c WHERE c.course_code='ICT3101';
INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by)
SELECT ug.ug_id,c.course_id,'FINAL',ROUND(48+RAND()*48,1),(SELECT user_id FROM users WHERE username='lec_silva')
FROM undergraduates ug, courses c WHERE c.course_code='ICT3101';

INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by)
SELECT ug.ug_id,c.course_id,'CA1',ROUND(52+RAND()*43,1),(SELECT user_id FROM users WHERE username='lec_perera')
FROM undergraduates ug, courses c WHERE c.course_code='ICT3102';
INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by)
SELECT ug.ug_id,c.course_id,'CA2',ROUND(50+RAND()*45,1),(SELECT user_id FROM users WHERE username='lec_perera')
FROM undergraduates ug, courses c WHERE c.course_code='ICT3102';
INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by)
SELECT ug.ug_id,c.course_id,'FINAL',ROUND(45+RAND()*50,1),(SELECT user_id FROM users WHERE username='lec_perera')
FROM undergraduates ug, courses c WHERE c.course_code='ICT3102';

INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by)
SELECT ug.ug_id,c.course_id,'CA1',ROUND(50+RAND()*45,1),(SELECT user_id FROM users WHERE username='lec_silva')
FROM undergraduates ug, courses c WHERE c.course_code='ICT3103';
INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by)
SELECT ug.ug_id,c.course_id,'FINAL',ROUND(45+RAND()*50,1),(SELECT user_id FROM users WHERE username='lec_silva')
FROM undergraduates ug, courses c WHERE c.course_code='ICT3103';

INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by)
SELECT ug.ug_id,c.course_id,'CA1',ROUND(55+RAND()*40,1),(SELECT user_id FROM users WHERE username='lec_jayantha')
FROM undergraduates ug, courses c WHERE c.course_code='BIO3101';
INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by)
SELECT ug.ug_id,c.course_id,'CA2',ROUND(50+RAND()*45,1),(SELECT user_id FROM users WHERE username='lec_jayantha')
FROM undergraduates ug, courses c WHERE c.course_code='BIO3101';
INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by)
SELECT ug.ug_id,c.course_id,'FINAL',ROUND(48+RAND()*47,1),(SELECT user_id FROM users WHERE username='lec_jayantha')
FROM undergraduates ug, courses c WHERE c.course_code='BIO3101';

INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by)
SELECT ug.ug_id,c.course_id,'CA1',ROUND(50+RAND()*45,1),(SELECT user_id FROM users WHERE username='lec_jayantha')
FROM undergraduates ug, courses c WHERE c.course_code='BIO3102';
INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by)
SELECT ug.ug_id,c.course_id,'FINAL',ROUND(45+RAND()*50,1),(SELECT user_id FROM users WHERE username='lec_jayantha')
FROM undergraduates ug, courses c WHERE c.course_code='BIO3102';

INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by)
SELECT ug.ug_id,c.course_id,'CA1',ROUND(52+RAND()*43,1),(SELECT user_id FROM users WHERE username='lec_kumari')
FROM undergraduates ug, courses c WHERE c.course_code='ENG3101';
INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by)
SELECT ug.ug_id,c.course_id,'CA2',ROUND(50+RAND()*45,1),(SELECT user_id FROM users WHERE username='lec_kumari')
FROM undergraduates ug, courses c WHERE c.course_code='ENG3101';
INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by)
SELECT ug.ug_id,c.course_id,'FINAL',ROUND(45+RAND()*50,1),(SELECT user_id FROM users WHERE username='lec_kumari')
FROM undergraduates ug, courses c WHERE c.course_code='ENG3101';

INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by)
SELECT ug.ug_id,c.course_id,'CA1',ROUND(50+RAND()*45,1),(SELECT user_id FROM users WHERE username='lec_kumari')
FROM undergraduates ug, courses c WHERE c.course_code='ENG3102';
INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by)
SELECT ug.ug_id,c.course_id,'FINAL',ROUND(45+RAND()*50,1),(SELECT user_id FROM users WHERE username='lec_kumari')
FROM undergraduates ug, courses c WHERE c.course_code='ENG3102';

INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by)
SELECT ug.ug_id,c.course_id,'CA1',ROUND(55+RAND()*40,1),(SELECT user_id FROM users WHERE username='lec_bandara')
FROM undergraduates ug, courses c WHERE c.course_code='MDS3101';
INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by)
SELECT ug.ug_id,c.course_id,'FINAL',ROUND(48+RAND()*47,1),(SELECT user_id FROM users WHERE username='lec_bandara')
FROM undergraduates ug, courses c WHERE c.course_code='MDS3101';
INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by)
SELECT ug.ug_id,c.course_id,'CA1',ROUND(50+RAND()*45,1),(SELECT user_id FROM users WHERE username='lec_bandara')
FROM undergraduates ug, courses c WHERE c.course_code='MDS3102';
INSERT INTO marks (ug_id,course_id,exam_type,marks_value,uploaded_by)
SELECT ug.ug_id,c.course_id,'FINAL',ROUND(45+RAND()*50,1),(SELECT user_id FROM users WHERE username='lec_bandara')
FROM undergraduates ug, courses c WHERE c.course_code='MDS3102';

INSERT INTO attendance (ug_id,course_id,session_date,session_type,is_present)
SELECT ug.ug_id,c.course_id,'2026-01-05','THEORY',1
FROM undergraduates ug, courses c WHERE c.course_code='ICT3101';
INSERT INTO attendance (ug_id,course_id,session_date,session_type,is_present)
SELECT ug.ug_id,c.course_id,'2026-01-07','THEORY',1
FROM undergraduates ug, courses c WHERE c.course_code='ICT3101';
INSERT INTO attendance (ug_id,course_id,session_date,session_type,is_present)
SELECT ug.ug_id,c.course_id,'2026-01-09','THEORY',1
FROM undergraduates ug, courses c WHERE c.course_code='ICT3101';
INSERT INTO attendance (ug_id,course_id,session_date,session_type,is_present)
SELECT ug.ug_id,c.course_id,'2026-01-12','THEORY',0
FROM undergraduates ug
JOIN (SELECT ug_id FROM undergraduates ORDER BY ug_id LIMIT 5) t ON ug.ug_id=t.ug_id,
courses c WHERE c.course_code='ICT3101';
INSERT INTO attendance (ug_id,course_id,session_date,session_type,is_present)
SELECT ug.ug_id,c.course_id,'2026-01-14','PRACTICAL',1
FROM undergraduates ug, courses c WHERE c.course_code='ICT3101';
INSERT INTO attendance (ug_id,course_id,session_date,session_type,is_present)
SELECT ug.ug_id,c.course_id,'2026-01-16','PRACTICAL',1
FROM undergraduates ug, courses c WHERE c.course_code='ICT3101';
INSERT INTO attendance (ug_id,course_id,session_date,session_type,is_present)
SELECT ug.ug_id,c.course_id,'2026-01-19','PRACTICAL',0
FROM undergraduates ug
JOIN (SELECT ug_id FROM undergraduates ORDER BY ug_id LIMIT 3) t ON ug.ug_id=t.ug_id,
courses c WHERE c.course_code='ICT3101';
INSERT INTO attendance (ug_id,course_id,session_date,session_type,is_present)
SELECT ug.ug_id,c.course_id,'2026-01-21','THEORY',1
FROM undergraduates ug, courses c WHERE c.course_code='ICT3102';
INSERT INTO attendance (ug_id,course_id,session_date,session_type,is_present)
SELECT ug.ug_id,c.course_id,'2026-01-23','THEORY',1
FROM undergraduates ug, courses c WHERE c.course_code='ICT3102';
INSERT INTO attendance (ug_id,course_id,session_date,session_type,is_present)
SELECT ug.ug_id,c.course_id,'2026-01-28','PRACTICAL',1
FROM undergraduates ug, courses c WHERE c.course_code='ICT3102';
INSERT INTO attendance (ug_id,course_id,session_date,session_type,is_present)
SELECT ug.ug_id,c.course_id,'2026-02-02','THEORY',1
FROM undergraduates ug, courses c WHERE c.course_code='BIO3101';
INSERT INTO attendance (ug_id,course_id,session_date,session_type,is_present)
SELECT ug.ug_id,c.course_id,'2026-02-04','THEORY',1
FROM undergraduates ug, courses c WHERE c.course_code='ENG3101';
INSERT INTO attendance (ug_id,course_id,session_date,session_type,is_present)
SELECT ug.ug_id,c.course_id,'2026-02-06','THEORY',1
FROM undergraduates ug, courses c WHERE c.course_code='MDS3101';

INSERT INTO timetables (course_id,day_of_week,start_time,end_time,location,session_type)
SELECT course_id,'MON','08:00:00','10:00:00','Hall A - Block 1','THEORY' FROM courses WHERE course_code='ICT3101';
INSERT INTO timetables (course_id,day_of_week,start_time,end_time,location,session_type)
SELECT course_id,'MON','10:00:00','12:00:00','Lab 01 - Block 2','PRACTICAL' FROM courses WHERE course_code='ICT3101';
INSERT INTO timetables (course_id,day_of_week,start_time,end_time,location,session_type)
SELECT course_id,'TUE','08:00:00','10:00:00','Hall B - Block 1','THEORY' FROM courses WHERE course_code='ICT3102';
INSERT INTO timetables (course_id,day_of_week,start_time,end_time,location,session_type)
SELECT course_id,'TUE','13:00:00','15:00:00','Lab 02 - Block 2','PRACTICAL' FROM courses WHERE course_code='ICT3102';
INSERT INTO timetables (course_id,day_of_week,start_time,end_time,location,session_type)
SELECT course_id,'WED','08:00:00','10:00:00','Hall C - Block 1','THEORY' FROM courses WHERE course_code='ICT3103';
INSERT INTO timetables (course_id,day_of_week,start_time,end_time,location,session_type)
SELECT course_id,'WED','10:00:00','12:00:00','Hall D - Block 3','THEORY' FROM courses WHERE course_code='BIO3101';
INSERT INTO timetables (course_id,day_of_week,start_time,end_time,location,session_type)
SELECT course_id,'WED','13:00:00','15:00:00','Lab 03 - Block 3','PRACTICAL' FROM courses WHERE course_code='BIO3101';
INSERT INTO timetables (course_id,day_of_week,start_time,end_time,location,session_type)
SELECT course_id,'THU','08:00:00','10:00:00','Hall E - Block 2','THEORY' FROM courses WHERE course_code='BIO3102';
INSERT INTO timetables (course_id,day_of_week,start_time,end_time,location,session_type)
SELECT course_id,'THU','13:00:00','15:00:00','Lab 04 - Block 2','PRACTICAL' FROM courses WHERE course_code='BIO3102';
INSERT INTO timetables (course_id,day_of_week,start_time,end_time,location,session_type)
SELECT course_id,'MON','13:00:00','15:00:00','Hall F - Block 4','THEORY' FROM courses WHERE course_code='ENG3101';
INSERT INTO timetables (course_id,day_of_week,start_time,end_time,location,session_type)
SELECT course_id,'TUE','10:00:00','12:00:00','Hall G - Block 4','THEORY' FROM courses WHERE course_code='ENG3102';
INSERT INTO timetables (course_id,day_of_week,start_time,end_time,location,session_type)
SELECT course_id,'TUE','15:00:00','17:00:00','Lab 05 - Block 4','PRACTICAL' FROM courses WHERE course_code='ENG3102';
INSERT INTO timetables (course_id,day_of_week,start_time,end_time,location,session_type)
SELECT course_id,'FRI','08:00:00','10:00:00','Hall H - Block 5','THEORY' FROM courses WHERE course_code='MDS3101';
INSERT INTO timetables (course_id,day_of_week,start_time,end_time,location,session_type)
SELECT course_id,'FRI','10:00:00','12:00:00','Hall H - Block 5','THEORY' FROM courses WHERE course_code='MDS3102';

INSERT INTO notices (title,content,created_by) VALUES
('Semester Exam Schedule Released',
 'The final examination schedule for Semester 1 2026 has been released. Please check the notice board for your exam dates and venues. All students must carry their student ID card.',
 (SELECT user_id FROM users WHERE username='admin')),
('Medical Submission Deadline',
 'All medical certificates for the current semester must be submitted to the Technical Officer before 30th April 2026. Late submissions will not be accepted.',
 (SELECT user_id FROM users WHERE username='admin')),
('Library Closure Notice',
 'The university library will be closed from 15th to 18th April 2026 for annual maintenance. Online resources remain available through the student portal.',
 (SELECT user_id FROM users WHERE username='admin')),
('OOP Lab Sessions Rescheduled',
 'The Object Oriented Programming practical sessions scheduled for 22nd April have been moved to 24th April 2026 in Lab 01. Please update your timetable accordingly.',
 (SELECT user_id FROM users WHERE username='lec_silva')),
('CA Marks Released - ICT3102',
 'CA1 and CA2 marks for Database Management Systems have been uploaded. Students can view their grades through the student portal.',
 (SELECT user_id FROM users WHERE username='lec_perera'));

INSERT INTO medicals (ug_id,from_date,to_date,reason,doc_path,is_approved,status) VALUES
((SELECT ug_id FROM undergraduates WHERE reg_number='TG/2023/1001'),'2026-01-15','2026-01-17','[MED/2026/001] Fever and flu symptoms','medical_uploads/MED_TG231001_001.pdf',1,'APPROVED'),
((SELECT ug_id FROM undergraduates WHERE reg_number='TG/2023/1001'),'2026-02-10','2026-02-10','[MED/2026/002] Dental appointment','medical_uploads/MED_TG231001_002.pdf',1,'APPROVED'),
((SELECT ug_id FROM undergraduates WHERE reg_number='TG/2023/1002'),'2026-03-05','2026-03-06','[MED/2026/003] Eye infection treatment','medical_uploads/MED_TG231002_001.pdf',0,'PENDING'),
((SELECT ug_id FROM undergraduates WHERE reg_number='TG/2023/1003'),'2026-02-18','2026-02-19','[MED/2026/004] Stomach pain','medical_uploads/MED_TG231003_001.pdf',0,'PENDING'),
((SELECT ug_id FROM undergraduates WHERE reg_number='TG/2023/2001'),'2026-02-20','2026-02-21','[MED/2026/005] Food poisoning','medical_uploads/MED_TG232001_001.pdf',1,'APPROVED'),
((SELECT ug_id FROM undergraduates WHERE reg_number='TG/2023/3001'),'2026-03-10','2026-03-11','[MED/2026/006] High fever','medical_uploads/MED_TG233001_001.pdf',0,'PENDING'),
((SELECT ug_id FROM undergraduates WHERE reg_number='TG/2021/1008'),'2026-01-20','2026-01-22','[MED/2026/007] Chronic back pain treatment','medical_uploads/MED_TG211008_001.pdf',1,'APPROVED');

SELECT 'users' AS table_name, COUNT(*) AS row_count FROM users
UNION ALL SELECT 'undergraduates', COUNT(*) FROM undergraduates
UNION ALL SELECT 'courses', COUNT(*) FROM courses
UNION ALL SELECT 'course_materials', COUNT(*) FROM course_materials
UNION ALL SELECT 'marks', COUNT(*) FROM marks
UNION ALL SELECT 'attendance', COUNT(*) FROM attendance
UNION ALL SELECT 'timetables', COUNT(*) FROM timetables
UNION ALL SELECT 'notices', COUNT(*) FROM notices
UNION ALL SELECT 'medicals', COUNT(*) FROM medicals;
