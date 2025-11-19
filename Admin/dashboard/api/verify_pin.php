<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(204);
    exit;
}

require __DIR__ . '/../config/db.php';

try {
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input) {
        // Fallback to form-encoded
        $input = $_POST;
    }

    $userId = isset($input['userId']) ? trim($input['userId']) : '';
    $pin = isset($input['pin']) ? trim($input['pin']) : '';

    if ($userId === '' || $pin === '') {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'userId and pin are required']);
        exit;
    }

    // Validate PIN format (4-6 digits)
    if (!preg_match('/^\d{4,6}$/', $pin)) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'PIN must be 4-6 digits']);
        exit;
    }

    try {
        // Get user and PIN information
        $stmt = $pdo->prepare('
            SELECT u.user_id, u.user_name, u.email, p.pin_hash 
            FROM users u 
            LEFT JOIN pins p ON u.user_id = p.user_id 
            WHERE u.user_id = ?
        ');
        $stmt->execute([$userId]);
        $user = $stmt->fetch();

        if (!$user) {
            http_response_code(404);
            echo json_encode(['success' => false, 'message' => 'User not found']);
            exit;
        }

        if (!$user['pin_hash']) {
            http_response_code(401);
            echo json_encode(['success' => false, 'message' => 'PIN not set for this user']);
            exit;
        }

        // Verify PIN
        if (!password_verify($pin, $user['pin_hash'])) {
            http_response_code(401);
            echo json_encode(['success' => false, 'message' => 'Invalid PIN']);
            exit;
        }

        // PIN is valid - return user information
        echo json_encode([
            'success' => true, 
            'message' => 'PIN verified successfully',
            'user' => [
                'userId' => $user['user_id'],
                'userName' => $user['user_name'],
                'email' => $user['email']
            ]
        ]);

    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Server error: ' . $e->getMessage()]);
    }

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Server error: ' . $e->getMessage()]);
}
