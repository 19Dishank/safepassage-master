-- Update users table to add user management fields
-- Run this script to add missing fields for user activation and login tracking

USE `safepassage`;

-- Add missing fields to users table if they don't exist
ALTER TABLE `users` 
ADD COLUMN IF NOT EXISTS `is_active` TINYINT(1) DEFAULT 1 COMMENT '1 = active, 0 = inactive',
ADD COLUMN IF NOT EXISTS `last_login` TIMESTAMP NULL COMMENT 'Last login timestamp',
ADD COLUMN IF NOT EXISTS `login_count` INT DEFAULT 0 COMMENT 'Total number of logins',
ADD COLUMN IF NOT EXISTS `password_hash` VARCHAR(255) NULL COMMENT 'Hashed password for authentication';

-- Update existing users to be active by default
UPDATE `users` SET `is_active` = 1 WHERE `is_active` IS NULL;

-- Create index for better performance on user status queries
CREATE INDEX IF NOT EXISTS `idx_users_status` ON `users`(`is_active`);
CREATE INDEX IF NOT EXISTS `idx_users_last_login` ON `users`(`last_login`);

-- Show the updated table structure
DESCRIBE `users`;

-- Show sample data
SELECT user_id, user_name, email, is_active, last_login, login_count, created_at 
FROM `users` 
LIMIT 5;
