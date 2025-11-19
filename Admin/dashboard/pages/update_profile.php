<?php
require_once __DIR__ . '/../includes/auth_middleware.php';
require __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/message_handler.php';

// Ensure admin is authenticated
requireAdminAuth();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: profile.php');
    exit;
}

$displayName = isset($_POST['displayName']) ? trim($_POST['displayName']) : '';

// Validate input
if (empty($displayName)) {
    MessageHandler::redirect('profile.php', 'Display name cannot be empty.', MessageHandler::ERROR);
}

if (strlen($displayName) > 255) {
    MessageHandler::redirect('profile.php', 'Display name is too long (maximum 255 characters).', MessageHandler::ERROR);
}

try {
    // Update the admin name in database
    $stmt = $pdo->prepare("UPDATE admins SET name = ? WHERE id = ?");
    $stmt->execute([$displayName, $_SESSION['admin_id']]);
    
    if ($stmt->rowCount() > 0) {
        // Update session with new name
        $_SESSION['admin_name'] = $displayName;
        
        MessageHandler::redirect('profile.php', 'Display name updated successfully!', MessageHandler::SUCCESS);
    } else {
        MessageHandler::redirect('profile.php', 'No changes were made.', MessageHandler::ERROR);
    }
} catch (Exception $e) {
    MessageHandler::redirect('profile.php', 'Failed to update display name. Please try again.', MessageHandler::ERROR);
}

exit;
?>
