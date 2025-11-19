<?php
require_once __DIR__ . '/../includes/auth_middleware.php';
require __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/message_handler.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
  header('Location: index.php');
  exit;
}

$username = isset($_POST['username']) ? trim($_POST['username']) : '';
$password = isset($_POST['password']) ? (string)$_POST['password'] : '';

if ($username === '' || $password === '') {
  MessageHandler::redirect('index.php', 'Username and password are required.', MessageHandler::ERROR);
}

$stmt = $pdo->prepare('SELECT id, username, name, password_hash FROM admins WHERE username = ? LIMIT 1');
$stmt->execute([$username]);
$admin = $stmt->fetch();

if (!$admin || !password_verify($password, $admin['password_hash'])) {
  MessageHandler::redirect('index.php', 'Invalid credentials.', MessageHandler::ERROR);
}

$_SESSION['admin_id'] = $admin['id'];
$_SESSION['admin_username'] = $admin['username'];
$_SESSION['admin_name'] = $admin['name'] ?? $admin['username'];

// Redirect to return URL if provided, otherwise to dashboard
$returnUrl = isset($_POST['return_url']) ? $_POST['return_url'] : 'dashboard.php';
if (empty($returnUrl) || $returnUrl === 'index.php') {
    $returnUrl = 'dashboard.php';
}

header('Location: ' . $returnUrl);
exit;


