-- Building Maintenance Ticket System Database Schema
-- MySQL 8.0+ compatible

-- Create database
CREATE DATABASE IF NOT EXISTS maintenance_system
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE maintenance_system;

-- Users table for authentication and user management
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role ENUM('ADMIN', 'TECHNICIAN', 'TENANT') NOT NULL DEFAULT 'TENANT',
    phone_number VARCHAR(20),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Buildings table for building management
CREATE TABLE buildings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    zip_code VARCHAR(10) NOT NULL,
    manager_id BIGINT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_manager_id (manager_id),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Rooms table for room/area management within buildings
CREATE TABLE rooms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    building_id BIGINT NOT NULL,
    floor_number INT NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    room_type VARCHAR(50),
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (building_id) REFERENCES buildings(id) ON DELETE CASCADE,
    UNIQUE KEY unique_room (building_id, floor_number, room_number),
    INDEX idx_building_id (building_id),
    INDEX idx_floor_number (floor_number),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Ticket categories for classification
CREATE TABLE ticket_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    default_priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') NOT NULL DEFAULT 'MEDIUM',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_name (name),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default ticket categories
INSERT INTO ticket_categories (name, description, default_priority) VALUES
('PLUMBING', 'Issues related to water systems, pipes, fixtures', 'HIGH'),
('ELECTRICAL', 'Electrical systems, lighting, power outlets', 'HIGH'),
('HVAC', 'Heating, ventilation, and air conditioning', 'MEDIUM'),
('CLEANING', 'Cleaning and sanitation issues', 'MEDIUM'),
('SECURITY', 'Security systems, locks, access control', 'HIGH'),
('STRUCTURAL', 'Building structure, walls, floors, ceilings', 'HIGH'),
('PEST_CONTROL', 'Pest and insect control', 'MEDIUM'),
('LANDSCAPING', 'Outdoor maintenance and landscaping', 'LOW'),
('EQUIPMENT', 'Equipment maintenance and repair', 'MEDIUM'),
('OTHER', 'Other maintenance issues not categorized', 'MEDIUM');

-- Main tickets table
CREATE TABLE tickets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    category_id BIGINT NOT NULL,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') NOT NULL DEFAULT 'MEDIUM',
    status ENUM('OPEN', 'IN_PROGRESS', 'ON_HOLD', 'RESOLVED', 'CLOSED') NOT NULL DEFAULT 'OPEN',
    reporter_id BIGINT NOT NULL,
    assignee_id BIGINT,
    building_id BIGINT NOT NULL,
    room_id BIGINT,
    estimated_completion DATETIME,
    actual_completion DATETIME,
    resolution_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (category_id) REFERENCES ticket_categories(id),
    FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (assignee_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (building_id) REFERENCES buildings(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL,
    INDEX idx_reporter_id (reporter_id),
    INDEX idx_assignee_id (assignee_id),
    INDEX idx_building_id (building_id),
    INDEX idx_room_id (room_id),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_created_at (created_at),
    INDEX idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Comments table for ticket discussions
CREATE TABLE comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticket_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_internal BOOLEAN DEFAULT false, -- Internal notes vs public comments
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_ticket_id (ticket_id),
    INDEX idx_author_id (author_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Attachments table for file uploads
CREATE TABLE attachments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticket_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    uploaded_by BIGINT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_ticket_id (ticket_id),
    INDEX idx_uploaded_by (uploaded_by),
    INDEX idx_uploaded_at (uploaded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Ticket status history tracking
CREATE TABLE ticket_status_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticket_id BIGINT NOT NULL,
    old_status ENUM('OPEN', 'IN_PROGRESS', 'ON_HOLD', 'RESOLVED', 'CLOSED'),
    new_status ENUM('OPEN', 'IN_PROGRESS', 'ON_HOLD', 'RESOLVED', 'CLOSED') NOT NULL,
    changed_by BIGINT NOT NULL,
    change_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_ticket_id (ticket_id),
    INDEX idx_changed_by (changed_by),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Notification settings for users
CREATE TABLE notification_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    email_notifications BOOLEAN DEFAULT true,
    ticket_assigned BOOLEAN DEFAULT true,
    ticket_status_changed BOOLEAN DEFAULT true,
    ticket_comment_added BOOLEAN DEFAULT true,
    ticket_overdue BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_settings (user_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Email notifications queue
CREATE TABLE email_notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recipient_email VARCHAR(100) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    status ENUM('PENDING', 'SENT', 'FAILED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP NULL,
    error_message TEXT,
    
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- System configuration table
CREATE TABLE system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default system configurations
INSERT INTO system_config (config_key, config_value, description) VALUES
('max_file_upload_size', '10485760', 'Maximum file upload size in bytes (10MB)'),
('allowed_file_types', 'image/jpeg,image/png,image/gif,application/pdf,text/plain', 'Allowed file types for uploads'),
('ticket_auto_close_days', '7', 'Number of days after resolution to auto-close tickets'),
('notification_email_from', 'noreply@maintenance.system', 'From email address for notifications'),
('system_name', 'Building Maintenance System', 'Display name of the system');

-- Create views for common queries
CREATE VIEW ticket_summary AS
SELECT 
    t.id,
    t.title,
    t.description,
    t.priority,
    t.status,
    tc.name as category_name,
    CONCAT(rp.first_name, ' ', rp.last_name) as reporter_name,
    rp.email as reporter_email,
    CONCAT(asg.first_name, ' ', asg.last_name) as assignee_name,
    asg.email as assignee_email,
    b.name as building_name,
    CONCAT(r.floor_number, '-', r.room_number) as room_location,
    t.created_at,
    t.updated_at,
    t.estimated_completion,
    t.actual_completion,
    CASE 
        WHEN t.status = 'OPEN' AND t.estimated_completion < NOW() THEN 'OVERDUE'
        WHEN t.status = 'IN_PROGRESS' AND t.estimated_completion < NOW() THEN 'OVERDUE'
        ELSE 'ON_TIME'
    END as timeline_status
FROM tickets t
LEFT JOIN ticket_categories tc ON t.category_id = tc.id
LEFT JOIN users rp ON t.reporter_id = rp.id
LEFT JOIN users asg ON t.assignee_id = asg.id
LEFT JOIN buildings b ON t.building_id = b.id
LEFT JOIN rooms r ON t.room_id = r.id;

CREATE VIEW user_activity_summary AS
SELECT 
    u.id,
    u.username,
    u.email,
    u.first_name,
    u.last_name,
    u.role,
    u.is_active,
    (SELECT COUNT(*) FROM tickets WHERE reporter_id = u.id) as tickets_reported,
    (SELECT COUNT(*) FROM tickets WHERE assignee_id = u.id) as tickets_assigned,
    (SELECT COUNT(*) FROM comments WHERE author_id = u.id) as comments_made,
    u.created_at,
    u.updated_at
FROM users u;

-- Create stored procedures for common operations
DELIMITER //

-- Procedure to create a new ticket with validation
CREATE PROCEDURE create_ticket(
    IN p_title VARCHAR(200),
    IN p_description TEXT,
    IN p_category_id BIGINT,
    IN p_priority VARCHAR(10),
    IN p_reporter_id BIGINT,
    IN p_building_id BIGINT,
    IN p_room_id BIGINT,
    IN p_estimated_completion DATETIME
)
BEGIN
    DECLARE v_category_exists INT DEFAULT 0;
    DECLARE v_reporter_exists INT DEFAULT 0;
    DECLARE v_building_exists INT DEFAULT 0;
    DECLARE v_room_exists INT DEFAULT 0;
    
    -- Validate category
    SELECT COUNT(*) INTO v_category_exists FROM ticket_categories WHERE id = p_category_id AND is_active = true;
    IF v_category_exists = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid category ID';
    END IF;
    
    -- Validate reporter
    SELECT COUNT(*) INTO v_reporter_exists FROM users WHERE id = p_reporter_id AND is_active = true;
    IF v_reporter_exists = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid reporter ID';
    END IF;
    
    -- Validate building
    SELECT COUNT(*) INTO v_building_exists FROM buildings WHERE id = p_building_id AND is_active = true;
    IF v_building_exists = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid building ID';
    END IF;
    
    -- Validate room if provided
    IF p_room_id IS NOT NULL THEN
        SELECT COUNT(*) INTO v_room_exists FROM rooms WHERE id = p_room_id AND building_id = p_building_id AND is_active = true;
        IF v_room_exists = 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid room ID for the specified building';
        END IF;
    END IF;
    
    -- Insert the ticket
    INSERT INTO tickets (
        title, description, category_id, priority, reporter_id, 
        building_id, room_id, estimated_completion
    ) VALUES (
        p_title, p_description, p_category_id, p_priority, p_reporter_id,
        p_building_id, p_room_id, p_estimated_completion
    );
    
    SELECT LAST_INSERT_ID() as ticket_id;
END//

-- Procedure to update ticket status with history tracking
CREATE PROCEDURE update_ticket_status(
    IN p_ticket_id BIGINT,
    IN p_new_status VARCHAR(20),
    IN p_changed_by BIGINT,
    IN p_change_reason TEXT
)
BEGIN
    DECLARE v_current_status VARCHAR(20);
    DECLARE v_ticket_exists INT DEFAULT 0;
    
    -- Check if ticket exists
    SELECT COUNT(*) INTO v_ticket_exists FROM tickets WHERE id = p_ticket_id;
    IF v_ticket_exists = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ticket not found';
    END IF;
    
    -- Get current status
    SELECT status INTO v_current_status FROM tickets WHERE id = p_ticket_id;
    
    -- Update ticket status
    UPDATE tickets SET 
        status = p_new_status,
        updated_at = CURRENT_TIMESTAMP,
        actual_completion = CASE WHEN p_new_status = 'RESOLVED' THEN CURRENT_TIMESTAMP ELSE actual_completion END
    WHERE id = p_ticket_id;
    
    -- Record status change history
    INSERT INTO ticket_status_history (ticket_id, old_status, new_status, changed_by, change_reason)
    VALUES (p_ticket_id, v_current_status, p_new_status, p_changed_by, p_change_reason);
    
    -- Return updated ticket info
    SELECT * FROM ticket_summary WHERE id = p_ticket_id;
END//

DELIMITER ;

-- Create triggers for automatic timestamp updates
DELIMITER //

-- Trigger to automatically update updated_at timestamp
CREATE TRIGGER update_users_timestamp 
BEFORE UPDATE ON users 
FOR EACH ROW 
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END//

CREATE TRIGGER update_buildings_timestamp 
BEFORE UPDATE ON buildings 
FOR EACH ROW 
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END//

CREATE TRIGGER update_rooms_timestamp 
BEFORE UPDATE ON rooms 
FOR EACH ROW 
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END//

CREATE TRIGGER update_tickets_timestamp 
BEFORE UPDATE ON tickets 
FOR EACH ROW 
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END//

CREATE TRIGGER update_comments_timestamp 
BEFORE UPDATE ON comments 
FOR EACH ROW 
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END//

CREATE TRIGGER update_notification_settings_timestamp 
BEFORE UPDATE ON notification_settings 
FOR EACH ROW 
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END//

CREATE TRIGGER update_system_config_timestamp 
BEFORE UPDATE ON system_config 
FOR EACH ROW 
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END//

DELIMITER ;

-- Grant appropriate permissions (adjust according to your security requirements)
-- CREATE USER 'maint_app'@'%' IDENTIFIED BY 'your_secure_password_here';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON maintenance_system.* TO 'maint_app'@'%';
-- FLUSH PRIVILEGES;