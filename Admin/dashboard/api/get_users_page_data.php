<?php
/**
 * API endpoint to get detailed user data for users.php page
 * Used for auto-refreshing user list without page reload
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');
header('Access-Control-Allow-Headers: Content-Type');

require_once __DIR__ . '/../config/db.php';

try {
    // Get all users with their statistics (same query as users.php)
    $usersQuery = "
        SELECT 
            u.user_id,
            u.user_name,
            u.email,
            u.created_at,
            u.is_active,
            u.last_login,
            u.login_count,
            COALESCE(pin_count.count, 0) as pin_count,
            COALESCE(card_count.count, 0) as card_count,
            COALESCE(password_count.count, 0) as password_count,
            COALESCE(document_count.count, 0) as document_count
        FROM users u
        LEFT JOIN (
            SELECT user_id, COUNT(*) as count FROM pins GROUP BY user_id
        ) pin_count ON u.user_id = pin_count.user_id
        LEFT JOIN (
            SELECT user_id, COUNT(*) as count FROM cards GROUP BY user_id
        ) card_count ON u.user_id = card_count.user_id
        LEFT JOIN (
            SELECT user_id, COUNT(*) as count FROM passwords GROUP BY user_id
        ) password_count ON u.user_id = password_count.user_id
        LEFT JOIN (
            SELECT user_id, COUNT(*) as count FROM documents GROUP BY user_id
        ) document_count ON u.user_id = document_count.user_id
        ORDER BY u.created_at DESC
    ";
    
    $usersStmt = $pdo->query($usersQuery);
    $users = $usersStmt->fetchAll();
    
    // Get counts for summary cards
    $totalUsers = count($users);
    $activeUsers = count(array_filter($users, function($u) { return $u['is_active']; }));
    $inactiveUsers = $totalUsers - $activeUsers;
    $thisMonth = count(array_filter($users, function($u) {
        return date('Y-m', strtotime($u['created_at'])) === date('Y-m');
    }));
    
    $response = [
        'success' => true,
        'users' => $users,
        'stats' => [
            'total_users' => $totalUsers,
            'active_users' => $activeUsers,
            'inactive_users' => $inactiveUsers,
            'new_this_month' => $thisMonth
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
