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
  door_open BOOLEAN NOT NULL DEFAULT FALSE,
  occupied  BOOLEAN NOT NULL DEFAULT FALSE,
  door_ptnr BINARY(16) NOT NULL,
  PRIMARY KEY (door_id),
  CONSTRAINT fk_ptnr
    FOREIGN KEY (door_ptnr) REFERENCES partners (ptnr_id)
    ON DELETE CASCADE
    ON UPDATE RESTRICT  
);

CREATE TABLE packages (
  pkg_id BINARY(16) NOT NULL,
  pkg_status ENUM('ARRIVING','IN_STORAGE','RETRIVED') NOT NULL,
  pkg_user BINARY(16) NOT NULL,
  pkg_door INT unsigned NOT NULL,
  PRIMARY KEY (pkg_id),
  CONSTRAINT fk_door
    FOREIGN KEY (pkg_door) REFERENCES doors (door_id)
    ON DELETE CASCADE
    ON UPDATE RESTRICT,
  CONSTRAINT fk_user
    FOREIGN KEY (pkg_user) REFERENCES users (user_id)
    ON DELETE CASCADE
    ON UPDATE RESTRICT
);