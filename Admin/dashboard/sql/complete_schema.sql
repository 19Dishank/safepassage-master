-- Complete SafePassage Database Schema
-- Based on Android Room database models

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS `safepassage` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `safepassage`;

-- Admin users table
CREATE TABLE IF NOT EXISTS `admins` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(100) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Users table (main app users)
CREATE TABLE IF NOT EXISTS `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` VARCHAR(255) NOT NULL UNIQUE, -- Firebase-style unique ID
    `email` VARCHAR(190) NOT NULL UNIQUE,
    `user_name` VARCHAR(150) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Categories table
CREATE TABLE IF NOT EXISTS `categories` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `cat_id` INT NOT NULL UNIQUE, -- Android app's catId
    `cat_name` VARCHAR(100) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Passwords table
CREATE TABLE IF NOT EXISTS `passwords` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `pass_id` INT NOT NULL UNIQUE, -- Android app's passId
    `user_id` VARCHAR(255) NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `user_name` VARCHAR(150),
    `email` VARCHAR(190),
    `phone` VARCHAR(20),
    `password` TEXT, -- Encrypted password
    `url` VARCHAR(500),
    `package_name` VARCHAR(200),
    `note` TEXT,
    `cat_id` INT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `last_used` TIMESTAMP NULL,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE CASCADE,
    FOREIGN KEY (`cat_id`) REFERENCES `categories`(`cat_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Credit cards table
CREATE TABLE IF NOT EXISTS `cards` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `car_id` INT NOT NULL UNIQUE, -- Android app's carId
    `user_id` VARCHAR(255) NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `card_holder_name` VARCHAR(150),
    `card_number` VARCHAR(255), -- Encrypted card number
    `expiration_date` VARCHAR(10),
    `cvv` VARCHAR(255), -- Encrypted CVV
    `pin` VARCHAR(255), -- Encrypted PIN
    `note` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Documents table
CREATE TABLE IF NOT EXISTS `documents` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `document_id` INT NOT NULL UNIQUE, -- Android app's documentId
    `user_id` VARCHAR(255) NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `file_name` VARCHAR(255) NOT NULL,
    `file_path` VARCHAR(500) NOT NULL,
    `file_size` BIGINT NOT NULL,
    `mime_type` VARCHAR(100) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- PIN table for app security
CREATE TABLE IF NOT EXISTS `pins` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` VARCHAR(255) NOT NULL UNIQUE,
    `encrypted_pin` VARCHAR(255) NOT NULL,
    `created_at` BIGINT NOT NULL, -- Unix timestamp
    FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Legacy table for backward compatibility (can be removed later)
CREATE TABLE IF NOT EXISTS `my_apps_table` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(150) NOT NULL,
    `email` VARCHAR(190) NOT NULL UNIQUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert default admin user
INSERT IGNORE INTO `admins` (`username`, `password_hash`) VALUES 
('admin', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'); -- password: admin

-- Insert default categories
INSERT IGNORE INTO `categories` (`cat_id`, `cat_name`) VALUES 
(1, 'Social Media'),
(2, 'Banking'),
(3, 'Shopping'),
(4, 'Work'),
(5, 'Personal'),
(6, 'Gaming'),
(7, 'Email'),
(8, 'Other');

-- Create indexes for better performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_user_id ON users(user_id);
CREATE INDEX idx_passwords_user_id ON passwords(user_id);
CREATE INDEX idx_cards_user_id ON cards(user_id);
CREATE INDEX idx_documents_user_id ON documents(user_id);
CREATE INDEX idx_passwords_cat_id ON passwords(cat_id);
