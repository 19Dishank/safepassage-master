-- SafePassage MySQL schema
-- Import this in phpMyAdmin or MySQL client

CREATE DATABASE IF NOT EXISTS `safepassage` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `safepassage`;

-- Admins table (passwords are bcrypt hashes; seed via setup_db.php for admin/admin)
CREATE TABLE IF NOT EXISTS `admins` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(100) NOT NULL UNIQUE,
  `password_hash` VARCHAR(255) NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- App users table used by dashboard
CREATE TABLE IF NOT EXISTS `my_apps_table` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(150) NOT NULL,
  `email` VARCHAR(190) NOT NULL UNIQUE,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sample data for dashboard display
INSERT INTO `my_apps_table` (`name`, `email`) VALUES
('John Doe', 'john@example.com'),
('Jane Smith', 'jane@example.com')
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- Note: Run /Admin/dashboard/setup_db.php once to seed default admin (admin/admin)


