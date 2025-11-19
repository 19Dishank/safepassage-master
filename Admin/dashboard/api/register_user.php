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
    // Log the raw input for debugging
    $rawInput = file_get_contents('php://input');
    error_log("User Registration API called - Raw input: " . $rawInput);
    
    $input = json_decode($rawInput, true);
    
    if (!$input) {
        // Fallback to form-encoded
        $input = $_POST;
        error_log("User Registration API - Using POST data: " . print_r($_POST, true));
    }
    
    error_log("User Registration API - Parsed input: " . print_r($input, true));

    $name = isset($input['name']) ? trim($input['name']) : '';
    $email = isset($input['email']) ? trim($input['email']) : '';
    $password = isset($input['password']) ? trim($input['password']) : '';
    $userId = isset($input['userId']) ? trim($input['userId']) : '';

    if ($name === '' || $email === '') {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'name and email are required']);
        exit;
    }

    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'invalid email']);
        exit;
    }

    // Generate userId if not provided
    if (empty($userId)) {
        $userId = uniqid('user_', true);
    }

    // Hash password if provided
    $passwordHash = null;
    if (!empty($password)) {
        $passwordHash = password_hash($password, PASSWORD_BCRYPT);
    }

    try {
        // Insert into users table with password
        if ($passwordHash) {
            $stmt = $pdo->prepare('INSERT INTO users (user_id, user_name, email, password_hash) VALUES (?, ?, ?, ?)');
            $stmt->execute([$userId, $name, $email, $passwordHash]);
        } else {
            $stmt = $pdo->prepare('INSERT INTO users (user_id, user_name, email) VALUES (?, ?, ?)');
            $stmt->execute([$userId, $name, $email]);
        }

        // Log a notification: new user registered
        try {
            $pdo->exec("CREATE TABLE IF NOT EXISTS notifications (
                id INT AUTO_INCREMENT PRIMARY KEY,
                type VARCHAR(50) NOT NULL,
                title VARCHAR(200) NOT NULL,
                message VARCHAR(500) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            $insertNotif = $pdo->prepare("INSERT INTO notifications (type, title, message) VALUES (?, ?, ?)");
            $insertNotif->execute([
                'new_user',
                'New user registered',
                $name . ' (' . $email . ')'
            ]);
        } catch (Throwable $e) {
            // ignore notification failures
        }

        echo json_encode([
            'success' => true, 
            'message' => 'User registered successfully',
            'userId' => $userId,
            'hasPassword' => !empty($passwordHash)
        ]);

    } catch (PDOException $e) {
        if ($e->getCode() === '23000') {
            // Duplicate entry
            if (strpos($e->getMessage(), 'users.email') !== false) {
                http_response_code(409);
                echo json_encode(['success' => false, 'message' => 'Email already exists']);
            } else {
                http_response_code(409);
                echo json_encode(['success' => false, 'message' => 'User already exists']);
            }
        } else {
            http_response_code(500);
            echo json_encode(['success' => false, 'message' => 'Server error: ' . $e->getMessage()]);
        }
    }

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Server error: ' . $e->getMessage()]);
}
?>


