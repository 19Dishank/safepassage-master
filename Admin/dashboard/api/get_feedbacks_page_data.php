<?php
/**
 * API endpoint to get feedbacks data for feedbacks.php page
 * Used for auto-refreshing feedbacks list without page reload
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');
header('Access-Control-Allow-Headers: Content-Type');

require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/feedback_sentiment_service.php';

$sentimentService = new FeedbackSentimentService($pdo);
$sentimentService->ensureSchema();
$sentimentService->analyzePending(3);

try {
    // Get all feedbacks (same query as feedbacks.php)
    $feedbacksQuery = "
        SELECT 
            id,
            user_id,
            user_name,
            user_email,
            feedback_text,
            sentiment_label,
            sentiment_confidence,
            sentiment_reason,
            sentiment_category,
            created_at
        FROM feedbacks
        ORDER BY created_at DESC
    ";
    
    $feedbacksStmt = $pdo->query($feedbacksQuery);
    $feedbacks = $feedbacksStmt->fetchAll();
    
    // Get counts for summary cards
    $totalFeedbacks = count($feedbacks);
    $todayFeedbacks = count(array_filter($feedbacks, function($f) { 
        return date('Y-m-d', strtotime($f['created_at'])) === date('Y-m-d'); 
    }));
    $thisWeekFeedbacks = count(array_filter($feedbacks, function($f) { 
        $weekStart = date('Y-m-d', strtotime('monday this week'));
        return date('Y-m-d', strtotime($f['created_at'])) >= $weekStart; 
    }));
    
    $sentimentCounts = $sentimentService->getSentimentCounts();

    $response = [
        'success' => true,
        'feedbacks' => $feedbacks,
        'stats' => [
            'total_feedbacks' => $totalFeedbacks,
            'today_feedbacks' => $todayFeedbacks,
            'this_week_feedbacks' => $thisWeekFeedbacks,
            'sentiment_counts' => $sentimentCounts,
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

