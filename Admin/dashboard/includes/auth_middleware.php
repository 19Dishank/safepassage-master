<?php
// Start session if not already started
if (session_status() === PHP_SESSION_NONE) {
    session_start();
}

// Include message handler if available
if (file_exists(__DIR__ . '/message_handler.php')) {
    require_once __DIR__ . '/message_handler.php';
}

// Check if admin is authenticated
function requireAdminAuth() {
    if (!isset($_SESSION['admin_id']) || empty($_SESSION['admin_id'])) {
        // Redirect to login page with return URL
        $currentPage = $_SERVER['REQUEST_URI'];
        $returnUrl = urlencode($currentPage);
        header('Location: index.php?return_url=' . $returnUrl);
        exit;
    }
}

// Check if admin is already logged in (for login page)
function redirectIfAuthenticated() {
    if (isset($_SESSION['admin_id']) && !empty($_SESSION['admin_id'])) {
        header('Location: dashboard.php');
        exit;
    }
}

// Get current admin info
function getCurrentAdmin() {
    if (!isset($_SESSION['admin_id'])) {
        return null;
    }
    
    return [
        'id' => $_SESSION['admin_id'],
        'username' => $_SESSION['admin_username'] ?? 'Unknown',
        'name' => $_SESSION['admin_name'] ?? $_SESSION['admin_username'] ?? 'Unknown'
    ];
}

// Logout function
function adminLogout() {
    $_SESSION = [];
    if (ini_get('session.use_cookies')) {
        $params = session_get_cookie_params();
        setcookie(session_name(), '', time() - 42000,
            $params['path'], $params['domain'], $params['secure'], $params['httponly']
        );
    }
    session_destroy();
    
    // Use message handler if available, otherwise fall back to URL parameter
    if (class_exists('MessageHandler')) {
        MessageHandler::redirect('index.php', 'Successfully logged out', MessageHandler::SUCCESS);
    } else {
        header('Location: index.php?message=' . urlencode('Successfully logged out'));
        exit;
    }
}
?>
