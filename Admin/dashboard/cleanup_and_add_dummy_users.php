<?php
require_once 'config/db.php';

echo "<h2>SafePassage Users Table Cleanup and Dummy Data Setup</h2>";

try {
    // First, let's see what users currently exist
    echo "<h3>Current Users:</h3>";
    $stmt = $pdo->query("SELECT user_id, user_name, email, is_active, last_login, login_count, created_at FROM users ORDER BY id");
    $currentUsers = $stmt->fetchAll();
    
    if (empty($currentUsers)) {
        echo "<p>No users found in database.</p>";
    } else {
        echo "<table border='1' style='border-collapse: collapse; width: 100%;'>";
        echo "<tr><th>User ID</th><th>Name</th><th>Email</th><th>Status</th><th>Last Login</th><th>Login Count</th><th>Joined</th></tr>";
        foreach ($currentUsers as $user) {
            $status = $user['is_active'] ? 'Active' : 'Inactive';
            $lastLogin = $user['last_login'] ? date('Y-m-d H:i', strtotime($user['last_login'])) : 'Never';
            $joined = date('Y-m-d H:i', strtotime($user['created_at']));
            echo "<tr>";
            echo "<td>{$user['user_id']}</td>";
            echo "<td>{$user['user_name']}</td>";
            echo "<td>{$user['email']}</td>";
            echo "<td>{$status}</td>";
            echo "<td>{$lastLogin}</td>";
            echo "<td>{$user['login_count']}</td>";
            echo "<td>{$joined}</td>";
            echo "</tr>";
        }
        echo "</table>";
    }
    
    // Delete all existing users except test50@gmail.com to test72@gmail.com range
    echo "<h3>Cleaning up users table...</h3>";
    
    // First, delete users that are NOT in the test50-test72 range
    $stmt = $pdo->prepare("DELETE FROM users WHERE email NOT LIKE 'test%' OR 
                          CAST(SUBSTRING(email, 5, 2) AS UNSIGNED) < 50 OR 
                          CAST(SUBSTRING(email, 5, 2) AS UNSIGNED) > 72");
    $stmt->execute();
    $deletedCount = $stmt->rowCount();
    echo "<p>Deleted {$deletedCount} users outside the test50-test72 range.</p>";
    
    // Now add dummy users with different coordinates for presentation
    echo "<h3>Adding dummy users with varied data for presentation...</h3>";
    
                   $dummyUsers = [
          // test50@gmail.com to test72@gmail.com (23 users total)
          // Format: [email, name, join_date, login_count, last_login, is_active]
          // Join dates spread across recent months/weeks/days for better graph representation
          // Using current Indian date: 29-08-2025
          ['test50@gmail.com', 'John Smith', '2025-06-15 09:30:00', 8, '2025-08-29 14:15:00', true],
          ['test51@gmail.com', 'Emma Johnson', '2025-06-18 11:45:00', 5, '2025-08-28 16:30:00', true],
          ['test52@gmail.com', 'Michael Brown', '2025-06-22 08:20:00', 9, '2025-08-27 10:45:00', true],
          ['test53@gmail.com', 'Sarah Davis', '2025-06-25 13:15:00', 3, '2025-08-26 12:20:00', false],
          ['test54@gmail.com', 'David Wilson', '2025-06-28 15:40:00', 7, '2025-08-25 09:10:00', true],
          ['test55@gmail.com', 'Lisa Anderson', '2025-07-01 10:25:00', 6, '2025-08-24 17:35:00', true],
          ['test56@gmail.com', 'Robert Taylor', '2025-07-05 12:50:00', 4, '2025-08-23 11:55:00', true],
          ['test57@gmail.com', 'Jennifer Martinez', '2025-07-08 14:30:00', 8, '2025-08-22 13:40:00', true],
          ['test58@gmail.com', 'Christopher Garcia', '2025-07-12 09:15:00', 2, '2025-08-21 15:25:00', false],
          ['test59@gmail.com', 'Amanda Rodriguez', '2025-07-15 16:45:00', 9, '2025-08-20 08:50:00', true],
          ['test60@gmail.com', 'James Lopez', '2025-07-19 11:20:00', 5, '2025-08-19 14:15:00', true],
          ['test61@gmail.com', 'Michelle White', '2025-07-22 13:55:00', 7, '2025-08-18 16:30:00', true],
          ['test62@gmail.com', 'Daniel Lee', '2025-07-26 08:40:00', 6, '2025-08-17 10:45:00', true],
          ['test63@gmail.com', 'Jessica Hall', '2025-08-01 15:10:00', 4, '2025-08-16 12:20:00', false],
          ['test64@gmail.com', 'Matthew Allen', '2025-08-05 12:35:00', 3, '2025-08-15 17:35:00', true],
          ['test65@gmail.com', 'Nicole Young', '2025-08-08 10:50:00', 8, '2025-08-14 11:55:00', true],
          ['test66@gmail.com', 'Andrew King', '2025-08-12 14:25:00', 5, '2025-08-13 13:40:00', true],
          ['test67@gmail.com', 'Stephanie Wright', '2025-08-15 16:40:00', 7, '2025-08-12 15:25:00', true],
          ['test68@gmail.com', 'Kevin Scott', '2025-08-18 09:30:00', 6, '2025-08-11 08:50:00', false],
          ['test69@gmail.com', 'Rachel Green', '2025-08-21 11:45:00', 4, '2025-08-10 14:15:00', true],
          ['test70@gmail.com', 'Brian Baker', '2025-08-24 13:20:00', 9, '2025-08-09 16:30:00', true],
          ['test71@gmail.com', 'Lauren Adams', '2025-08-27 15:55:00', 5, '2025-08-08 10:45:00', true],
          ['test72@gmail.com', 'Steven Nelson', '2025-08-29 08:10:00', 7, '2025-08-07 12:20:00', true]
      ];
    
    $insertedCount = 0;
    foreach ($dummyUsers as $user) {
        $email = $user[0];
        $name = $user[1];
        $joinedDate = $user[2];
        $loginCount = $user[3];
        $lastLogin = $user[4];
        $isActive = $user[5];
        
        // Generate unique userId
        $userId = 'user_' . time() . '_' . rand(1000, 9999);
        
        // Hash password (using Test1234 for all users)
        $password = password_hash('Test1234', PASSWORD_DEFAULT);
        
        // Check if user already exists
        $checkStmt = $pdo->prepare("SELECT id FROM users WHERE email = ?");
        $checkStmt->execute([$email]);
        
        if ($checkStmt->rowCount() == 0) {
            // Insert new user
            $insertStmt = $pdo->prepare("INSERT INTO users (user_id, user_name, email, password_hash, is_active, last_login, login_count, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            $insertStmt->execute([$userId, $name, $email, $password, $isActive, $lastLogin, $loginCount, $joinedDate]);
            $insertedCount++;
            
            // Add PIN for new user (PIN: 1234)
            $pinHash = password_hash('1234', PASSWORD_DEFAULT);
            $pinStmt = $pdo->prepare("INSERT INTO pins (user_id, pin_hash, created_at) VALUES (?, ?, ?)");
            $pinStmt->execute([$userId, $pinHash, $joinedDate]);
        } else {
            // Update existing user
            $updateStmt = $pdo->prepare("UPDATE users SET user_name = ?, is_active = ?, last_login = ?, login_count = ?, created_at = ? WHERE email = ?");
            $updateStmt->execute([$name, $isActive, $lastLogin, $loginCount, $joinedDate, $email]);
            
            // Get the user_id for existing user to add/update PIN
            $getUserIdStmt = $pdo->prepare("SELECT user_id FROM users WHERE email = ?");
            $getUserIdStmt->execute([$email]);
            $existingUserId = $getUserIdStmt->fetchColumn();
            
            // Check if PIN exists for this user
            $checkPinStmt = $pdo->prepare("SELECT id FROM pins WHERE user_id = ?");
            $checkPinStmt->execute([$existingUserId]);
            
            if ($checkPinStmt->rowCount() == 0) {
                // Add PIN for existing user (PIN: 1234)
                $pinHash = password_hash('1234', PASSWORD_DEFAULT);
                $pinStmt = $pdo->prepare("INSERT INTO pins (user_id, pin_hash, created_at) VALUES (?, ?, ?)");
                $pinStmt->execute([$existingUserId, $pinHash, $joinedDate]);
            } else {
                // Update existing PIN (PIN: 1234)
                $pinHash = password_hash('1234', PASSWORD_DEFAULT);
                $pinUpdateStmt = $pdo->prepare("UPDATE pins SET pin_hash = ? WHERE user_id = ?");
                $pinUpdateStmt->execute([$pinHash, $existingUserId]);
            }
        }
    }
    
    echo "<p>Successfully processed {$insertedCount} new users.</p>";
    
    // Show final results
    echo "<h3>Final Users Table:</h3>";
    $stmt = $pdo->query("SELECT user_id, user_name, email, is_active, last_login, login_count, created_at FROM users ORDER BY email");
    $finalUsers = $stmt->fetchAll();
    
    echo "<table border='1' style='border-collapse: collapse; width: 100%;'>";
    echo "<tr><th>User ID</th><th>Name</th><th>Email</th><th>Status</th><th>Last Login</th><th>Login Count</th><th>Joined</th></tr>";
    foreach ($finalUsers as $user) {
        $status = $user['is_active'] ? 'Active' : 'Inactive';
        $lastLogin = $user['last_login'] ? date('Y-m-d H:i', strtotime($user['last_login'])) : 'Never';
        $joined = date('Y-m-d H:i', strtotime($user['created_at']));
        echo "<tr>";
        echo "<td>{$user['user_id']}</td>";
        echo "<td>{$user['user_name']}</td>";
        echo "<td>{$user['email']}</td>";
        echo "<td>{$status}</td>";
        echo "<td>{$lastLogin}</td>";
        echo "<td>{$user['login_count']}</td>";
        echo "<td>{$joined}</td>";
        echo "</tr>";
    }
    echo "</table>";
    
    echo "<h3>Summary:</h3>";
    echo "<ul>";
    echo "<li>Total users: " . count($finalUsers) . "</li>";
    echo "<li>Active users: " . count(array_filter($finalUsers, function($u) { return $u['is_active']; })) . "</li>";
    echo "<li>Inactive users: " . count(array_filter($finalUsers, function($u) { return !$u['is_active']; })) . "</li>";
    echo "<li>Date range: " . date('Y-m-d', strtotime($finalUsers[0]['created_at'])) . " to " . date('Y-m-d', strtotime(end($finalUsers)['created_at'])) . "</li>";
    echo "</ul>";
    
    echo "<p><strong>✅ Database cleanup and dummy data setup completed successfully!</strong></p>";
    echo "<p>Your users table now contains 23 users (test50@gmail.com to test72@gmail.com) with varied data perfect for presentation graphs.</p>";
    echo "<p><strong>🔐 Login Credentials:</strong></p>";
    echo "<ul>";
    echo "<li><strong>Password for all users:</strong> Test1234</li>";
    echo "<li><strong>PIN for all users:</strong> 1234</li>";
    echo "<li><strong>Email range:</strong> test50@gmail.com to test72@gmail.com</li>";
    echo "</ul>";
    echo "<p>You can now login with any user using these credentials!</p>";
    
} catch (Exception $e) {
    echo "<p style='color: red;'>Error: " . $e->getMessage() . "</p>";
}
?>

<style>
body { font-family: Arial, sans-serif; margin: 20px; }
table { margin: 10px 0; }
th { background-color: #f0f0f0; padding: 8px; }
td { padding: 6px; }
h2, h3 { color: #333; }
</style>
