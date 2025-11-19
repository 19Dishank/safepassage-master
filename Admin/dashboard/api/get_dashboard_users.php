<?php
/**
 * API endpoint to get users for dashboard display
 * Used for auto-refreshing user list without page reload
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');
header('Access-Control-Allow-Headers: Content-Type');

require_once __DIR__ . '/../config/db.php';

try {
    // Get user statistics from users table - ALL users for scrolling
    $usersStmt = $pdo->query('SELECT user_name, email, created_at FROM users ORDER BY id DESC');
    $dashboardUsers = $usersStmt->fetchAll();
    
    // Get total user count
    $userCountStmt = $pdo->query('SELECT COUNT(*) as total FROM users');
    $userCount = $userCountStmt->fetch()['total'];
    
    // Get active user count
    $activeUserStmt = $pdo->query('SELECT COUNT(*) as active FROM users WHERE is_active = 1');
    $activeUserCount = $activeUserStmt->fetch()['active'];
    
    // Get today's new users
    $todayUsersStmt = $pdo->query("SELECT COUNT(*) as today FROM users WHERE DATE(created_at) = CURDATE()");
    $todayUsers = $todayUsersStmt->fetch()['today'];
    
    $response = [
        'success' => true,
        'users' => $dashboardUsers,
        'stats' => [
            'total_users' => $userCount,
            'active_users' => $activeUserCount,
            'today_users' => $todayUsers
        ],
        'timestamp' => date('Y-m-d H:i:s')
    ];
    
    echo json_encode($response);
    
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'error' => 'Database error: ' . $e->getMessage()
    ]);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'error' => 'Server error: ' . $e->getMessage()
    ]);
}
?>
