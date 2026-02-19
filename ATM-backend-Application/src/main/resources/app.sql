CREATE DATABASE atmdb;
USE atmdb;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    pin_hash VARCHAR(255) NOT NULL,   
    role VARCHAR(20) NOT NULL,        -- USER / ADMIN
    balance DOUBLE DEFAULT 0.0
);

CREATE TABLE transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL,        -- deposit / withdraw
    amount DOUBLE NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE atm (
    id INT PRIMARY KEY AUTO_INCREMENT,
    total_balance DOUBLE NOT NULL
);

INSERT INTO atm (total_balance) VALUES (50000.0);
