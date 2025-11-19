<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(204);
    exit;
}

require __DIR__ . '/../config/db.php';

try {
    $input = [];
    
    if ($_SERVER['REQUEST_METHOD'] === 'POST') {
        $rawInput = file_get_contents('php://input');
        $input = json_decode($rawInput, true) ?: $_POST;
    } else {
        $input = $_GET;
    }
    
    error_log("Check User Status API called - Input: " . print_r($input, true));

    $userId = isset($input['userId']) ? trim($input['userId']) : '';
    $email = isset($input['email']) ? trim($input['email']) : '';

    if (empty($userId) && empty($email)) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'userId or email is required']);
        exit;
    }

    try {
        $stmt = null;
        $params = [];
        
        if (!empty($userId)) {
            $stmt = $pdo->prepare('SELECT user_id, user_name, email, is_active, last_login, login_count FROM users WHERE user_id = ?');
            $params = [$userId];
        } else {
            $stmt = $pdo->prepare('SELECT user_id, user_name, email, is_active, last_login, login_count FROM users WHERE email = ?');
            $params = [$email];
        }
        
        $stmt->execute($params);
        $user = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$user) {
            http_response_code(404);
            echo json_encode(['success' => false, 'message' => 'User not found']);
            exit;
        }

        echo json_encode([
            'success' => true,
            'user' => [
                'userId' => $user['user_id'],
                'userName' => $user['user_name'],
                'email' => $user['email'],
                'isActive' => (bool)$user['is_active'],
                'lastLogin' => $user['last_login'],
                'loginCount' => $user['login_count']
            ]
        ]);

    } catch (PDOException $e) {
        error_log("Check User Status API - Database error: " . $e->getMessage());
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Server error: ' . $e->getMessage()]);
    }

} catch (Exception $e) {
    error_log("Check User Status API - General error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Server error: ' . $e->getMessage()]);
}
?>
