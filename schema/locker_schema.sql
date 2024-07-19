DROP DATABASE IF EXISTS ctt_locker;

CREATE DATABASE ctt_locker;

USE ctt_locker;

CREATE TABLE partners (
  ptnr_id BINARY(16) NOT NULL,
  ptnr_name CHAR(64) NOT NULL,
  PRIMARY KEY (ptnr_id)
);

CREATE TABLE users (
  user_id BINARY(16) NOT NULL,
  user_name CHAR(64) NOT NULL,
  PRIMARY KEY (user_id)
);

CREATE TABLE doors (
  door_id INT unsigned NOT NULL AUTO_INCREMENT,
  door_type ENUM('SMALL','MEDIUM','LARGE','EXTRA_LARGE') NOT NULL,
  door_ptnr BINARY(16) NOT NULL,
  PRIMARY KEY (door_id),
  CONSTRAINT fk_ptnr
    FOREIGN KEY (door_ptnr) REFERENCES partners (ptnr_id)
    ON DELETE CASCADE
    ON UPDATE RESTRICT  
);

CREATE TABLE reservations (
  rsv_id BINARY(16) NOT NULL,
  rsv_status ENUM('ARRIVING','IN_STORAGE','RETRIVED') NOT NULL,
  rsv_user BINARY(16) NOT NULL,
  rsv_door INT unsigned NOT NULL,
  rsv_dcode CHAR(65) NOT NULL,
  rsv_lcode CHAR(65) NOT NULL,
  rsv_ccode CHAR(65) NOT NULL,
  PRIMARY KEY (rsv_id),
  CONSTRAINT fk_door
    FOREIGN KEY (rsv_door) REFERENCES doors (door_id)
    ON DELETE CASCADE
    ON UPDATE RESTRICT,
  CONSTRAINT fk_user
    FOREIGN KEY (rsv_user) REFERENCES users (user_id)
    ON DELETE CASCADE
    ON UPDATE RESTRICT
);