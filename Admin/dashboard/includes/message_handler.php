<?php
// Centralized message handling for clean URLs
class MessageHandler {
    
    // Message types
    const SUCCESS = 'success';
    const ERROR = 'error';
    const WARNING = 'warning';
    const INFO = 'info';
    
    /**
     * Set a success message in session
     */
    public static function setSuccess($message, $page = null) {
        $_SESSION['admin_message'] = [
            'type' => self::SUCCESS,
            'message' => $message,
            'page' => $page ?? self::getCurrentPage()
        ];
    }
    
    /**
     * Set an error message in session
     */
    public static function setError($message, $page = null) {
        $_SESSION['admin_message'] = [
            'type' => self::ERROR,
            'message' => $message,
            'page' => $page ?? self::getCurrentPage()
        ];
    }
    
    /**
     * Set a warning message in session
     */
    public static function setWarning($message, $page = null) {
        $_SESSION['admin_message'] = [
            'type' => self::WARNING,
            'message' => $message,
            'page' => $page ?? self::getCurrentPage()
        ];
    }
    
    /**
     * Set an info message in session
     */
    public static function setInfo($message, $page = null) {
        $_SESSION['admin_message'] = [
            'type' => self::INFO,
            'message' => $message,
            'page' => $page ?? self::getCurrentPage()
        ];
    }
    
    /**
     * Get and clear message for current page
     */
    public static function getMessage() {
        if (!isset($_SESSION['admin_message'])) {
            return null;
        }
        
        $message = $_SESSION['admin_message'];
        $currentPage = self::getCurrentPage();
        
        // Only show message if it's for the current page
        if ($message['page'] === $currentPage) {
            unset($_SESSION['admin_message']);
            return $message;
        }
        
        return null;
    }
    
    /**
     * Display message if exists
     */
    public static function displayMessage() {
        $message = self::getMessage();
        
        if (!$message) {
            return;
        }
        
        $type = $message['type'];
        $text = htmlspecialchars($message['message']);
        
        $iconMap = [
            self::SUCCESS => 'check-circle',
            self::ERROR => 'exclamation-triangle',
            self::WARNING => 'exclamation-triangle',
            self::INFO => 'info-circle'
        ];
        
        $icon = $iconMap[$type] ?? 'info-circle';
        
        echo "<div class='alert alert-{$type} alert-dismissible fade show' role='alert'>";
        echo "<i class='fas fa-{$icon} me-2'></i>";
        echo $text;
        echo "<button type='button' class='btn-close' data-bs-dismiss='alert'></button>";
        echo "</div>";
    }
    
    /**
     * Redirect with clean URL and session message
     */
    public static function redirect($url, $message = null, $type = self::SUCCESS) {
        if ($message) {
            if ($type === self::SUCCESS) {
                self::setSuccess($message);
            } elseif ($type === self::ERROR) {
                self::setError($message);
            } elseif ($type === self::WARNING) {
                self::setWarning($message);
            } elseif ($type === self::INFO) {
                self::setInfo($message);
            }
        }
        
        header('Location: ' . $url);
        exit;
    }
    
    /**
     * Get current page name
     */
    private static function getCurrentPage() {
        $script = $_SERVER['SCRIPT_NAME'];
        $parts = explode('/', $script);
        $filename = end($parts);
        return str_replace('.php', '', $filename);
    }
    
    /**
     * Handle legacy URL parameters and convert to session messages
     */
    public static function handleLegacyParams() {
        // Handle success parameter
        if (isset($_GET['success'])) {
            self::setSuccess($_GET['success']);
            self::cleanURL();
        }
        
        // Handle error parameter
        if (isset($_GET['error'])) {
            self::setError($_GET['error']);
            self::cleanURL();
        }
        
        // Handle message parameter (for logout)
        if (isset($_GET['message'])) {
            self::setSuccess($_GET['message']);
            self::cleanURL();
        }
    }
    
    /**
     * Clean URL by removing message parameters
     */
    private static function cleanURL() {
        $url = parse_url($_SERVER['REQUEST_URI']);
        $path = $url['path'];
        
        // Remove message parameters from URL
        $params = [];
        if (isset($url['query'])) {
            parse_str($url['query'], $params);
            unset($params['success'], $params['error'], $params['message']);
        }
        
        $newURL = $path;
        if (!empty($params)) {
            $newURL .= '?' . http_build_query($params);
        }
        
        // Redirect to clean URL
        header('Location: ' . $newURL);
        exit;
    }
}
?>
