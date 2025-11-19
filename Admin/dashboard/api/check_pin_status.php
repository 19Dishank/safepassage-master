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

    if ($userId === '') {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'userId is required']);
        exit;
    }

    try {
        // Check if user exists and has PIN
        $stmt = $pdo->prepare('
            SELECT u.user_id, u.user_name, u.email, 
                   CASE WHEN p.user_id IS NOT NULL THEN 1 ELSE 0 END as has_pin
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

        echo json_encode([
            'success' => true, 
            'message' => 'PIN status retrieved successfully',
            'user' => [
                'userId' => $user['user_id'],
                'userName' => $user['user_name'],
                'email' => $user['email'],
                'hasPin' => (bool)$user['has_pin']
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
