-- Create Database
CREATE DATABASE IF NOT EXISTS crm;
USE crm;

-- =========================
-- LEADS TABLE
-- =========================
CREATE TABLE leads (
 id INT AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(100) NOT NULL,
 phone VARCHAR(15),
 email VARCHAR(100),
 city VARCHAR(50),
 requirement TEXT,
 status VARCHAR(50) DEFAULT 'NEW', -- NEW, CONTACTED, WON, LOST
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- TASKS (FOLLOW UPS)
-- =========================
CREATE TABLE tasks (
 id INT AUTO_INCREMENT PRIMARY KEY,
 lead_id INT,
 task_description TEXT,
 due_date DATE,
 status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, COMPLETED
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 
 FOREIGN KEY (lead_id) REFERENCES leads(id) ON DELETE CASCADE
);

-- =========================
-- CHAT HISTORY (AI MEMORY )
-- =========================
CREATE TABLE chat_messages (
 id INT AUTO_INCREMENT PRIMARY KEY,
 session_id VARCHAR(100),
 role VARCHAR(20), -- user / assistant
 message TEXT,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);