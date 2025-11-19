<?php
/**
 * API endpoint to submit user feedback
 * Accepts POST requests with JSON payload containing feedback data
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once __DIR__ . '/../config/db.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode([
        'success' => false,
        'message' => 'Method not allowed'
    ]);
    exit;
}

try {
    // Get JSON input
    $json = file_get_contents('php://input');
    $data = json_decode($json, true);
    
    if (!$data) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'Invalid JSON data'
        ]);
        exit;
    }
    
    // Validate required fields
    $userName = isset($data['userName']) ? trim($data['userName']) : '';
    $userEmail = isset($data['userEmail']) ? trim($data['userEmail']) : '';
    $feedbackText = isset($data['text']) ? trim($data['text']) : '';
    $userId = isset($data['userId']) ? trim($data['userId']) : null;
    
    if (empty($userName) || empty($userEmail) || empty($feedbackText)) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'Missing required fields: userName, userEmail, and text are required'
        ]);
        exit;
    }
    
    // Insert feedback into database
    $stmt = $pdo->prepare("
        INSERT INTO feedbacks (user_id, user_name, user_email, feedback_text, created_at) 
        VALUES (?, ?, ?, ?, NOW())
    ");
    
    $stmt->execute([$userId, $userName, $userEmail, $feedbackText]);
    
    echo json_encode([
        'success' => true,
        'message' => 'Feedback submitted successfully',
        'feedback_id' => $pdo->lastInsertId()
    ]);
    
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Database error: ' . $e->getMessage()
    ]);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Server error: ' . $e->getMessage()
    ]);
}
?>

