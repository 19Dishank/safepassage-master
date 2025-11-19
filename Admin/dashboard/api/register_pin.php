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
    error_log("PIN Registration API called - Raw input: " . $rawInput);
    
    $input = json_decode($rawInput, true);
    
    if (!$input) {
        // Fallback to form-encoded
        $input = $_POST;
        error_log("PIN Registration API - Using POST data: " . print_r($_POST, true));
    }
    
    error_log("PIN Registration API - Parsed input: " . print_r($input, true));

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

    // Check if user exists
    $stmt = $pdo->prepare('SELECT user_id FROM users WHERE user_id = ?');
    $stmt->execute([$userId]);
    if (!$stmt->fetch()) {
        http_response_code(404);
        echo json_encode(['success' => false, 'message' => 'User not found']);
        exit;
    }

    // Hash the PIN for security
    $pinHash = password_hash($pin, PASSWORD_BCRYPT);

    // Check if PIN already exists for this user
    $stmt = $pdo->prepare('SELECT id FROM pins WHERE user_id = ?');
    $stmt->execute([$userId]);
    $existingPin = $stmt->fetch();
    
    if ($existingPin) {
        // PIN exists - update it instead of blocking
        error_log("PIN Registration API - PIN exists for user: $userId, updating...");
        
        try {
            $stmt = $pdo->prepare('UPDATE pins SET pin_hash = ?, created_at = NOW() WHERE user_id = ?');
            $stmt->execute([$pinHash, $userId]);
            
            error_log("PIN Registration API - PIN updated successfully for user: $userId");
            
            echo json_encode([
                'success' => true, 
                'message' => 'PIN updated successfully',
                'userId' => $userId,
                'action' => 'updated'
            ]);
            exit;
            
        } catch (PDOException $e) {
            error_log("PIN Registration API - Failed to update PIN for user: $userId - " . $e->getMessage());
            http_response_code(500);
            echo json_encode(['success' => false, 'message' => 'Failed to update PIN: ' . $e->getMessage()]);
            exit;
        }
    } else {
        try {
            // Insert PIN into database
            $stmt = $pdo->prepare('INSERT INTO pins (user_id, pin_hash) VALUES (?, ?)');
            $stmt->execute([$userId, $pinHash]);

            // Log successful insertion
            error_log("PIN Registration API - PIN inserted successfully for user: $userId");

            echo json_encode([
                'success' => true, 
                'message' => 'PIN registered successfully',
                'userId' => $userId
            ]);

        } catch (PDOException $e) {
            // Log the detailed error
            error_log("PIN Registration API - Database error: " . $e->getMessage() . " (Code: " . $e->getCode() . ")");
            error_log("PIN Registration API - Failed to insert PIN for user: $userId");
            
            if ($e->getCode() === '23000') {
                http_response_code(409);
                echo json_encode(['success' => false, 'message' => 'PIN already exists for this user']);
            } else {
                http_response_code(500);
                echo json_encode(['success' => false, 'message' => 'Server error: ' . $e->getMessage()]);
            }
        }
    }

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Server error: ' . $e->getMessage()]);
}
