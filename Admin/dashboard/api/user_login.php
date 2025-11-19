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
    error_log("User Login API called - Raw input: " . $rawInput);
    
    $input = json_decode($rawInput, true);
    
    if (!$input) {
        // Fallback to form-encoded
        $input = $_POST;
        error_log("User Login API - Using POST data: " . print_r($_POST, true));
    }
    
    error_log("User Login API - Parsed input: " . print_r($input, true));

    $email = isset($input['email']) ? trim($input['email']) : '';
    $password = isset($input['password']) ? trim($input['password']) : '';

    if ($email === '' || $password === '') {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Email and password are required']);
        exit;
    }

    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Invalid email format']);
        exit;
    }

    try {
        // Check if user exists and get user info
        $stmt = $pdo->prepare('SELECT user_id, user_name, email, password_hash, is_active FROM users WHERE email = ?');
        $stmt->execute([$email]);
        $user = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$user) {
            http_response_code(401);
            echo json_encode(['success' => false, 'message' => 'Invalid email or password']);
            exit;
        }

        // Check if user is active
        if (!$user['is_active']) {
            // Log a notification: blocked user attempted login
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
                    'blocked_login',
                    'Blocked user attempted login',
                    'Email: ' . $email
                ]);
            } catch (Throwable $e) {
                // ignore notification failures
            }

            http_response_code(403);
            echo json_encode([
                'success' => false, 
                'message' => 'Account is deactivated. Please contact administrator.',
                'code' => 'ACCOUNT_DEACTIVATED'
            ]);
            exit;
        }

        // Verify password if password_hash exists
        if ($user['password_hash']) {
            if (!password_verify($password, $user['password_hash'])) {
                http_response_code(401);
                echo json_encode(['success' => false, 'message' => 'Invalid email or password']);
                exit;
            }
        } else {
            // For users without password (legacy accounts), allow login with any password
            // This maintains backward compatibility
            error_log("User Login API - Legacy user login (no password hash): " . $email);
        }

        // Update login information
        $updateStmt = $pdo->prepare('
            UPDATE users 
            SET last_login = CURRENT_TIMESTAMP, 
                login_count = login_count + 1 
            WHERE user_id = ?
        ');
        $updateStmt->execute([$user['user_id']]);

        // Get updated user info
        $stmt = $pdo->prepare('SELECT user_id, user_name, email, is_active, last_login, login_count FROM users WHERE user_id = ?');
        $stmt->execute([$user['user_id']]);
        $updatedUser = $stmt->fetch(PDO::FETCH_ASSOC);

        echo json_encode([
            'success' => true,
            'message' => 'Login successful',
            'user' => [
                'userId' => $updatedUser['user_id'],
                'userName' => $updatedUser['user_name'],
                'email' => $updatedUser['email'],
                'isActive' => (bool)$updatedUser['is_active'],
                'lastLogin' => $updatedUser['last_login'],
                'loginCount' => $updatedUser['login_count']
            ]
        ]);

        error_log("User Login API - Successful login for user: " . $email);

    } catch (PDOException $e) {
        error_log("User Login API - Database error: " . $e->getMessage());
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Server error: ' . $e->getMessage()]);
    }

} catch (Exception $e) {
    error_log("User Login API - General error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Server error: ' . $e->getMessage()]);
}
?>
