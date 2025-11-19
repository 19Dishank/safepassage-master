<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
  http_response_code(204);
  exit;
}

require __DIR__ . '/../config/db.php';

// Ensure notifications table exists (lightweight schema)
try {
  $pdo->exec("CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
} catch (Throwable $e) {
  // ignore
}

try {
  $notifications = [];

  // Only fetch specified types from notifications table
  $stmt = $pdo->prepare("
    SELECT type, title, message, created_at
    FROM notifications
    WHERE type IN ('new_user', 'blocked_login')
    ORDER BY created_at DESC
  ");
  $stmt->execute();
  $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);
  foreach ($rows as $r) {
    $icon = $r['type'] === 'new_user' ? 'fa-user-plus' : 'fa-user-lock';
    $notifications[] = [
      'type' => $r['type'],
      'icon' => $icon,
      'title' => $r['title'],
      'message' => $r['message'],
      'time' => $r['created_at'],
    ];
  }

  // No dummy/fallback notifications; return empty list if none

  echo json_encode([
    'success' => true,
    'count' => count($notifications),
    'notifications' => $notifications,
  ]);

} catch (Throwable $e) {
  http_response_code(500);
  echo json_encode([
    'success' => false,
    'message' => $e->getMessage(),
  ]);
}

