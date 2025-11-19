<?php
require_once __DIR__ . '/../includes/auth_middleware.php';
require __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/message_handler.php';
require_once __DIR__ . '/../includes/feedback_sentiment_service.php';
requireAdminAuth();

// Handle legacy URL parameters
MessageHandler::handleLegacyParams();

// Function to mask user name (show only first and last letter)
function maskUserName($name) {
    $name = trim($name);
    $length = mb_strlen($name);
    
    if ($length <= 2) {
        // If name is 2 characters or less, show as is
        return $name;
    } else {
        // Show first letter, mask middle, show last letter
        $first = mb_substr($name, 0, 1);
        $last = mb_substr($name, -1);
        $masked = str_repeat('*', $length - 2);
        return $first . $masked . $last;
    }
}

function getSentimentBadge(string $label): array {
    $normalized = strtolower(trim($label));
    
    switch ($normalized) {
        case 'positive':
            return ['Positive', 'bg-gradient-success'];
        case 'negative':
            return ['Negative', 'bg-gradient-danger'];
        case 'neutral':
            return ['Neutral', 'bg-gradient-info'];
        case 'suggestion':
            return ['Suggestion', 'bg-gradient-warning text-dark'];
        default:
            return ['Needs review', 'bg-gradient-secondary'];
    }
}

// Handle feedback deletion
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['delete_feedback'])) {
    $feedbackId = $_POST['feedback_id'];
    
    try {
        $stmt = $pdo->prepare("DELETE FROM feedbacks WHERE id = ?");
        $stmt->execute([$feedbackId]);
        
        if ($stmt->rowCount() > 0) {
            MessageHandler::setSuccess("Feedback deleted successfully!", 'feedbacks');
        } else {
            MessageHandler::setInfo("Feedback not found.", 'feedbacks');
        }
    } catch (Exception $e) {
        MessageHandler::setError("Error deleting feedback: " . $e->getMessage(), 'feedbacks');
    }
}

// Set up AI sentiment service
$sentimentService = new FeedbackSentimentService($pdo);
$sentimentService->ensureSchema();
$sentimentService->analyzePending(3);
$sentimentCounts = [
    'positive' => 0,
    'negative' => 0,
    'neutral' => 0,
    'suggestion' => 0,
    'unclassified' => 0,
];

// Get all feedbacks
try {
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
    $sentimentCounts = $sentimentService->getSentimentCounts();
    
    // Get counts for summary cards
    $totalFeedbacks = count($feedbacks);
    $todayFeedbacks = count(array_filter($feedbacks, function($f) { 
        return date('Y-m-d', strtotime($f['created_at'])) === date('Y-m-d'); 
    }));
    $thisWeekFeedbacks = count(array_filter($feedbacks, function($f) { 
        $weekStart = date('Y-m-d', strtotime('monday this week'));
        return date('Y-m-d', strtotime($f['created_at'])) >= $weekStart; 
    }));
    
} catch (Exception $e) {
    $feedbacks = [];
    $totalFeedbacks = 0;
    $todayFeedbacks = 0;
    $thisWeekFeedbacks = 0;
    $sentimentCounts = [
        'positive' => 0,
        'negative' => 0,
        'neutral' => 0,
        'suggestion' => 0,
        'unclassified' => 0,
    ];
    MessageHandler::setError("Database error: " . $e->getMessage(), 'feedbacks');
}

$activePage = 'feedbacks';
$includeSidebarFooter = false;
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <link rel="apple-touch-icon" sizes="76x76" href="../assets/img/apple-icon.png">
    <link rel="icon" type="image/png" href="../assets/img/favicon.png">
    <title>Feedbacks Management - SafePassage Admin</title>
    
    <!-- Latest Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.6.0/css/all.min.css">
    
    <!--     Fonts and icons     -->
    <link href="https://fonts.googleapis.com/css?family=Inter:300,400,500,600,700,800" rel="stylesheet" />
    <!-- Nucleo Icons -->
    <link href="https://demos.creative-tim.com/soft-ui-dashboard/assets/css/nucleo-icons.css" rel="stylesheet" />
    <link href="https://demos.creative-tim.com/soft-ui-dashboard/assets/css/nucleo-svg.css" rel="stylesheet" />
    <!-- Font Awesome CSS (CDN) for reliable icon rendering -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css" integrity="sha512-dym6B1tZ9E+uQpXzZ9b0QMBbYzKQ3iJtVnZ4m2qH7K8A2kN1hU4b8Zl8B9Lw3m1bq2q0lqL7pH2fJZlF3dY5oQ==" crossorigin="anonymous" referrerpolicy="no-referrer" />
    <!-- Font Awesome Icons -->
    <script src="https://kit.fontawesome.com/42d5adcbca.js" crossorigin="anonymous"></script>
    <!-- CSS Files -->
    <link id="pagestyle" href="../assets/css/soft-ui-dashboard.css?v=1.1.0" rel="stylesheet" />
    
    <!-- Custom CSS for feedbacks page -->
    <style>
        body {
            opacity: 1;
            transition: opacity 0.3s ease;
        }
        
        .feedback-card {
            border-left: 4px solid #17c1e8;
            transition: all 0.3s ease;
        }
        
        .feedback-card:hover {
            box-shadow: 0 8px 16px rgba(0,0,0,0.1);
            transform: translateY(-2px);
        }
        
        .feedback-text {
            color: #344767;
            line-height: 1.6;
            white-space: pre-wrap;
            word-wrap: break-word;
        }
        
        .user-avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
            color: white;
            font-size: 1.1rem;
        }
        
        .delete-btn {
            transition: all 0.3s ease;
        }
        
        .delete-btn:hover {
            transform: scale(1.1);
            color: #ea0606 !important;
        }
        
        .table-responsive {
            border-radius: 0.75rem;
            overflow: hidden;
        }
        
        .table th {
            font-weight: 600;
            text-transform: uppercase;
            font-size: 0.75rem;
            letter-spacing: 0.5px;
            padding: 1rem 0.75rem;
            border-bottom: 1px solid #e9ecef;
        }
        
        .table td {
            padding: 1rem 0.75rem;
            vertical-align: middle;
            border-bottom: 1px solid #f8f9fa;
        }
        
        .table tbody tr:hover {
            background-color: rgba(23, 193, 232, 0.04);
        }

        .sentiment-filter-group .btn {
            min-width: 110px;
            transition: all 0.2s ease;
        }

        .sentiment-filter-group .btn.active {
            color: #fff;
            box-shadow: 0 4px 12px rgba(17, 113, 239, 0.2);
        }

        .sentiment-summary-row {
            display: flex;
            flex-wrap: wrap;
            gap: 1.25rem;
        }

        .sentiment-summary-card {
            flex: 1 1 calc(20% - 1.25rem);
            min-width: 180px;
            display: flex;
        }

        .sentiment-summary-card .card {
            width: 100%;
        }

        @media (max-width: 1199px) {
            .sentiment-summary-card {
                flex: 1 1 calc(33.333% - 1.25rem);
            }
        }

        @media (max-width: 767px) {
            .sentiment-summary-card {
                flex: 1 1 calc(50% - 1.25rem);
            }
        }

        @media (max-width: 575px) {
            .sentiment-summary-card {
                flex: 1 1 100%;
            }
        }
    </style>
</head>

<body class="g-sidenav-show  bg-gray-100">

    <?php $activePage = 'feedbacks'; $includeSidebarFooter = false; include __DIR__ . '/partials/sidebar.php'; ?>

    <main class="main-content position-relative max-height-vh-100 h-100 border-radius-lg ">
        <!-- Navbar -->
        <nav class="navbar navbar-main navbar-expand-lg px-0 mx-4 shadow-none border-radius-xl" id="navbarBlur" navbar-scroll="true">
            <div class="container-fluid py-1 px-3">
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb bg-transparent mb-0 pb-0 pt-1 px-0 me-sm-6 me-5">
                        <li class="breadcrumb-item text-sm"><a class="opacity-5 text-dark" href="dashboard.php">Dashboard</a></li>
                        <li class="breadcrumb-item text-sm text-dark active" aria-current="page">Feedbacks Management</li>
                    </ol>
                    <h6 class="font-weight-bolder mb-0">Feedbacks Management</h6>
                </nav>
            </div>
        </nav>
        
        <!-- Auto-refresh notification area -->
        <div id="auto-refresh-notifications" class="position-fixed" style="top: 20px; right: 20px; z-index: 9999; max-width: 400px;">
            <!-- Notifications will be dynamically added here -->
        </div>
        
        <div class="container-fluid py-4">
            <!-- Status Messages -->
            <?php MessageHandler::displayMessage(); ?>
            
            <!-- Summary Cards -->
            <div class="row mb-4">
                <div class="col-xl-4 col-sm-6 mb-4">
                    <div class="card">
                        <span class="mask bg-info opacity-10 border-radius-lg"></span>
                        <div class="card-body p-3 position-relative">
                            <div class="row">
                                <div class="col-8 text-start">
                                    <div class="icon icon-shape bg-white shadow text-center border-radius-2xl">
                                        <i class="fas fa-comment-dots text-dark text-gradient" style="font-size: 1.5rem;"></i>
                                    </div>
                                    <h5 class="text-white font-weight-bolder mb-0 mt-3" data-stat="total-feedbacks">
                                        <?php echo $totalFeedbacks; ?>
                                    </h5>
                                    <span class="text-white text-sm">Total Feedbacks</span>
                                </div>
                                <div class="col-4">
                                    <p class="text-white text-sm text-end font-weight-bolder mt-auto mb-0">All</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="col-xl-4 col-sm-6 mb-4">
                    <div class="card">
                        <span class="mask bg-success opacity-10 border-radius-lg"></span>
                        <div class="card-body p-3 position-relative">
                            <div class="row">
                                <div class="col-8 text-start">
                                    <div class="icon icon-shape bg-white shadow text-center border-radius-2xl">
                                        <i class="fas fa-calendar-day text-dark text-gradient" style="font-size: 1.5rem;"></i>
                                    </div>
                                    <h5 class="text-white font-weight-bolder mb-0 mt-3" data-stat="today-feedbacks">
                                        <?php echo $todayFeedbacks; ?>
                                    </h5>
                                    <span class="text-white text-sm">Today</span>
                                </div>
                                <div class="col-4">
                                    <p class="text-white text-sm text-end font-weight-bolder mt-auto mb-0">Recent</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="col-xl-4 col-sm-6 mb-4">
                    <div class="card">
                        <span class="mask bg-primary opacity-10 border-radius-lg"></span>
                        <div class="card-body p-3 position-relative">
                            <div class="row">
                                <div class="col-8 text-start">
                                    <div class="icon icon-shape bg-white shadow text-center border-radius-2xl">
                                        <i class="fas fa-calendar-week text-dark text-gradient" style="font-size: 1.5rem;"></i>
                                    </div>
                                    <h5 class="text-white font-weight-bolder mb-0 mt-3" data-stat="week-feedbacks">
                                        <?php echo $thisWeekFeedbacks; ?>
                                    </h5>
                                    <span class="text-white text-sm">This Week</span>
                                </div>
                                <div class="col-4">
                                    <p class="text-white text-sm text-end font-weight-bolder mt-auto mb-0">Weekly</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- Sentiment Summary -->
            <div class="sentiment-summary-row mb-4">
                <div class="sentiment-summary-card">
                    <div class="card border-0 shadow-sm">
                        <div class="card-body p-3">
                            <div class="d-flex align-items-center">
                                <div class="icon icon-shape bg-gradient-success shadow text-center border-radius-2xl me-3">
                                    <i class="fas fa-thumbs-up text-white" style="font-size: 1.3rem;"></i>
                                </div>
                                <div>
                                    <h6 class="text-sm mb-1 text-muted">Positive</h6>
                                    <h5 class="text-dark mb-0" data-sentiment-count="positive"><?php echo $sentimentCounts['positive'] ?? 0; ?></h5>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="sentiment-summary-card">
                    <div class="card border-0 shadow-sm">
                        <div class="card-body p-3">
                            <div class="d-flex align-items-center">
                                <div class="icon icon-shape bg-gradient-danger shadow text-center border-radius-2xl me-3">
                                    <i class="fas fa-thumbs-down text-white" style="font-size: 1.3rem;"></i>
                                </div>
                                <div>
                                    <h6 class="text-sm mb-1 text-muted">Negative</h6>
                                    <h5 class="text-dark mb-0" data-sentiment-count="negative"><?php echo $sentimentCounts['negative'] ?? 0; ?></h5>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="sentiment-summary-card">
                    <div class="card border-0 shadow-sm">
                        <div class="card-body p-3">
                            <div class="d-flex align-items-center">
                                <div class="icon icon-shape bg-gradient-info shadow text-center border-radius-2xl me-3">
                                    <i class="fas fa-balance-scale text-white" style="font-size: 1.3rem;"></i>
                                </div>
                                <div>
                                    <h6 class="text-sm mb-1 text-muted">Neutral</h6>
                                    <h5 class="text-dark mb-0" data-sentiment-count="neutral"><?php echo $sentimentCounts['neutral'] ?? 0; ?></h5>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="sentiment-summary-card">
                    <div class="card border-0 shadow-sm">
                        <div class="card-body p-3">
                            <div class="d-flex align-items-center">
                                <div class="icon icon-shape bg-gradient-warning shadow text-center border-radius-2xl me-3">
                                    <i class="fas fa-lightbulb text-white" style="font-size: 1.3rem;"></i>
                                </div>
                                <div>
                                    <h6 class="text-sm mb-1 text-muted">Suggestions</h6>
                                    <h5 class="text-dark mb-0" data-sentiment-count="suggestion"><?php echo $sentimentCounts['suggestion'] ?? 0; ?></h5>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="sentiment-summary-card">
                    <div class="card border-0 shadow-sm">
                        <div class="card-body p-3">
                            <div class="d-flex align-items-center">
                                <div class="icon icon-shape bg-gradient-secondary shadow text-center border-radius-2xl me-3">
                                    <i class="fas fa-search text-white" style="font-size: 1.3rem;"></i>
                                </div>
                                <div>
                                    <h6 class="text-sm mb-1 text-muted">Needs Review</h6>
                                    <h5 class="text-dark mb-0" data-sentiment-count="unclassified"><?php echo $sentimentCounts['unclassified'] ?? 0; ?></h5>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- Feedbacks Table -->
            <div class="card">
                <div class="card-header pb-0">
                    <div class="d-flex flex-column flex-lg-row align-items-lg-center justify-content-between gap-3">
                        <div>
                            <h6>All Feedbacks</h6>
                            <p class="text-sm mb-0">
                                <i class="fa fa-comment-dots text-info" aria-hidden="true"></i>
                                <span class="font-weight-bold ms-1">All</span> user feedbacks 
                            </p>
                        </div>
                        <div class="btn-group sentiment-filter-group" role="group" aria-label="Sentiment filter">
                            <button type="button" class="btn btn-sm btn-outline-secondary active" data-sentiment-filter="all"><i class="fas fa-list-ul me-1"></i>All</button>
                            <button type="button" class="btn btn-sm btn-outline-success" data-sentiment-filter="positive"><i class="fas fa-thumbs-up me-1"></i>Positive</button>
                            <button type="button" class="btn btn-sm btn-outline-danger" data-sentiment-filter="negative"><i class="fas fa-thumbs-down me-1"></i>Negative</button>
                            <button type="button" class="btn btn-sm btn-outline-info" data-sentiment-filter="neutral"><i class="fas fa-balance-scale me-1"></i>Neutral</button>
                            <button type="button" class="btn btn-sm btn-outline-warning text-dark" data-sentiment-filter="suggestion"><i class="fas fa-lightbulb me-1"></i>Suggestions</button>
                            <button type="button" class="btn btn-sm btn-outline-secondary" data-sentiment-filter="unclassified"><i class="fas fa-search me-1"></i>Needs Review</button>
                        </div>
                    </div>
                </div>
                <div class="card-body px-0 pb-2">
                    <div class="table-responsive">
                        <table class="table align-items-center mb-0">
                            <thead>
                                <tr>
                                    <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7" style="width: 20%;">User</th>
                                    <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 ps-2" style="width: 50%;">Feedback</th>
                                    <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 ps-2" style="width: 15%;">Sentiment</th>
                                    <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 ps-2" style="width: 10%;">Date</th>
                                    <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 ps-2" style="width: 5%;">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <?php if (empty($feedbacks)): ?>
                                <tr>
                                    <td colspan="5" class="text-center py-5">
                                        <i class="fas fa-comment-dots fa-3x text-muted mb-3"></i>
                                        <h5 class="text-muted">No feedbacks found</h5>
                                        <p class="text-muted">Feedbacks will appear here once users submit them.</p>
                                    </td>
                                </tr>
                                <?php else: ?>
                                <?php foreach ($feedbacks as $feedback): ?>
                                <?php
                                    $rawCategory = strtolower(trim((string)($feedback['sentiment_category'] ?? $feedback['sentiment_label'] ?? '')));
                                    $rawCategory = $rawCategory !== '' ? $rawCategory : 'unclassified';
                                    [$sentimentText, $sentimentClass] = getSentimentBadge($rawCategory);
                                ?>
                                <tr data-sentiment="<?php echo htmlspecialchars($rawCategory); ?>">
                                    <td>
                                        <div class="d-flex align-items-center">
                                            <div class="user-avatar bg-gradient-primary me-2">
                                                <?php echo strtoupper(mb_substr($feedback['user_name'], 0, 1)); ?>
                                            </div>
                                            <div>
                                                <h6 class="mb-0 text-sm"><?php echo htmlspecialchars(maskUserName($feedback['user_name'])); ?></h6>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <p class="text-sm mb-0 feedback-text"><?php echo htmlspecialchars($feedback['feedback_text']); ?></p>
                                    </td>
                                    <td>
                                        <span class="badge <?php echo $sentimentClass; ?> text-xs px-3 py-2">
                                            <?php echo htmlspecialchars($sentimentText); ?>
                                        </span>
                                        <?php if (!empty($feedback['sentiment_reason'])): ?>
                                            <div class="text-xxs text-muted mt-1">
                                                <?php echo htmlspecialchars($feedback['sentiment_reason']); ?>
                                            </div>
                                        <?php endif; ?>
                                    </td>
                                    <td>
                                        <span class="text-sm text-muted">
                                            <?php echo date('M d, Y', strtotime($feedback['created_at'])); ?><br>
                                            <small><?php echo date('h:i A', strtotime($feedback['created_at'])); ?></small>
                                        </span>
                                    </td>
                                    <td>
                                        <form method="POST" style="display: inline;" onsubmit="return confirm('Are you sure you want to delete this feedback?');">
                                            <input type="hidden" name="feedback_id" value="<?php echo $feedback['id']; ?>">
                                            <button type="submit" name="delete_feedback" class="btn btn-link text-danger delete-btn p-0" title="Delete Feedback">
                                                <i class="fas fa-trash-alt" style="font-size: 1.1rem;"></i>
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                                <?php endforeach; ?>
                                <?php endif; ?>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </main>
    
    <!-- Core JS Files -->
    <script src="../assets/js/core/popper.min.js?v=1.1.0"></script>
    <script src="../assets/js/core/bootstrap.min.js?v=1.1.0"></script>
    <script src="../assets/js/plugins/perfect-scrollbar.min.js?v=1.1.0"></script>
    <script src="../assets/js/plugins/smooth-scrollbar.min.js?v=1.1.0"></script>
    <script src="../assets/js/plugins/buttons.js?v=1.1.0"></script>
    
    <script>
        var win = navigator.platform.indexOf('Win') > -1;
        if (win && document.querySelector('#sidenav-scrollbar')) {
            var options = {
                damping: '0.5'
            }
            Scrollbar.init(document.querySelector('#sidenav-scrollbar'), options);
        }
    </script>
    
    <!-- Github buttons -->
    <script async defer src="https://buttons.github.io/buttons.js"></script>
    <!-- Control Center for Soft Dashboard: parallax effects, scripts for the example pages etc -->
    <script src="../assets/js/soft-ui-dashboard.min.js?v=1.1.0"></script>
    
    <!-- Auto-hide alerts after 5 seconds -->
    <script>
        setTimeout(function() {
            const alerts = document.querySelectorAll('.alert');
            alerts.forEach(alert => {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            });
        }, 5000);
    </script>
    
    <!-- Auto-refresh functionality for feedbacks page -->
    <script>
        // Function to mask user name (show only first and last letter)
        function maskUserName(name) {
            if (typeof name !== 'string') {
                return 'Unknown';
            }
            name = name.trim();
            if (name.length === 0) {
                return 'Unknown';
            }
            const length = name.length;
            
            if (length <= 2) {
                return name;
            } else {
                const first = name.charAt(0);
                const last = name.charAt(length - 1);
                const masked = '*'.repeat(length - 2);
                return first + masked + last;
            }
        }
        
        // Function to format date
        function formatDate(dateString) {
            const date = new Date(dateString);
            const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
            const month = months[date.getMonth()];
            const day = date.getDate();
            const year = date.getFullYear();
            const hours = date.getHours();
            const minutes = date.getMinutes();
            const ampm = hours >= 12 ? 'PM' : 'AM';
            const displayHours = hours % 12 || 12;
            const displayMinutes = minutes < 10 ? '0' + minutes : minutes;
            
            return `${month} ${day}, ${year}<br><small>${displayHours}:${displayMinutes} ${ampm}</small>`;
        }
        
        // Function to escape HTML
        function escapeHtml(text) {
            const div = document.createElement('div');
            div.textContent = text ?? '';
            return div.innerHTML;
        }
        
        // Function to add pulse animation to element
        function addPulseAnimation(element) {
            if (!element) return;
            const card = element.closest('.card-body') || element.parentElement;
            if (card) {
                card.classList.add('pulse-animation');
                setTimeout(() => {
                    card.classList.remove('pulse-animation');
                }, 1000);
            }
        }
        
        // Helper function to update count with animation
        function updateCountWithAnimation(selector, newValue) {
            const element = document.querySelector(selector);
            if (element) {
                const currentCount = parseInt(element.textContent.trim()) || 0;
                if (currentCount !== newValue) {
                    element.textContent = newValue;
                    addPulseAnimation(element);
                }
            }
        }
        
        // Auto-refresh variables
        let refreshInterval;
        let lastFeedbackCount = <?php echo count($feedbacks); ?>;
        let currentSentimentFilter = 'all';
        let feedbackCache = <?php echo json_encode($feedbacks, JSON_HEX_TAG | JSON_HEX_APOS | JSON_HEX_AMP | JSON_HEX_QUOT); ?>;
        if (!Array.isArray(feedbackCache)) {
            feedbackCache = [];
        }
        
        function setActiveFilterButton(filter) {
            const buttons = document.querySelectorAll('[data-sentiment-filter]');
            buttons.forEach(btn => {
                if (btn.dataset.sentimentFilter === filter) {
                    btn.classList.add('active');
                } else {
                    btn.classList.remove('active');
                }
            });
        }

        function applySentimentFilter(filter) {
            currentSentimentFilter = filter;
            setActiveFilterButton(filter);
            renderFeedbacksTable();
        }

        function renderFeedbacksTable() {
            const tbody = document.querySelector('tbody');
            if (!tbody) {
                return;
            }

            const filtered = feedbackCache.filter((feedback) => {
                if (currentSentimentFilter === 'all') {
                    return true;
                }
                const category = (feedback.sentiment_category || feedback.sentiment_label || '').toLowerCase().trim();
                const normalized = category !== '' ? category : 'unclassified';
                return normalized === currentSentimentFilter;
            });

            tbody.innerHTML = '';

            if (filtered.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="text-center py-5"><i class="fas fa-comment-dots fa-3x text-muted mb-3"></i><h5 class="text-muted">No feedbacks found</h5><p class="text-muted">Feedbacks will appear here once users submit them.</p></td></tr>';
                return;
            }

            filtered.forEach((feedback) => {
                const maskedName = maskUserName(feedback.user_name);
                const firstLetter = feedback.user_name && typeof feedback.user_name === 'string' && feedback.user_name.length > 0
                    ? feedback.user_name.charAt(0).toUpperCase()
                    : '?';
                const sentimentInfo = getSentimentBadge(feedback.sentiment_category || feedback.sentiment_label);
                const sentimentReason = feedback.sentiment_reason ? escapeHtml(feedback.sentiment_reason) : '';
                const normalizedSentiment = sentimentInfo.slug;

                const row = document.createElement('tr');
                row.setAttribute('data-sentiment', normalizedSentiment);
                row.innerHTML = `
                    <td>
                        <div class="d-flex align-items-center">
                            <div class="user-avatar bg-gradient-primary me-2">
                                ${firstLetter}
                            </div>
                            <div>
                                <h6 class="mb-0 text-sm">${escapeHtml(maskedName)}</h6>
                            </div>
                        </div>
                    </td>
                    <td>
                        <p class="text-sm mb-0 feedback-text">${escapeHtml(feedback.feedback_text)}</p>
                    </td>
                    <td>
                        <span class="badge ${sentimentInfo.badgeClass} text-xs px-3 py-2">
                            ${sentimentInfo.text}
                        </span>
                        ${sentimentReason ? `<div class="text-xxs text-muted mt-1">${sentimentReason}</div>` : ''}
                    </td>
                    <td>
                        <span class="text-sm text-muted">
                            ${formatDate(feedback.created_at)}
                        </span>
                    </td>
                    <td>
                        <form method="POST" style="display: inline;" onsubmit="return confirm('Are you sure you want to delete this feedback?');">
                            <input type="hidden" name="feedback_id" value="${feedback.id}">
                            <button type="submit" name="delete_feedback" class="btn btn-link text-danger delete-btn p-0" title="Delete Feedback">
                                <i class="fas fa-trash-alt" style="font-size: 1.1rem;"></i>
                            </button>
                        </form>
                    </td>
                `;
                tbody.appendChild(row);
            });
        }

        function getSentimentBadge(label) {
            const normalized = (label || '').toLowerCase().trim();
            switch (normalized) {
                case 'positive':
                    return { text: 'Positive', badgeClass: 'bg-gradient-success', slug: 'positive' };
                case 'negative':
                    return { text: 'Negative', badgeClass: 'bg-gradient-danger', slug: 'negative' };
                case 'neutral':
                    return { text: 'Neutral', badgeClass: 'bg-gradient-info', slug: 'neutral' };
                case 'suggestion':
                    return { text: 'Suggestion', badgeClass: 'bg-gradient-warning text-dark', slug: 'suggestion' };
                default:
                    return { text: 'Needs review', badgeClass: 'bg-gradient-secondary', slug: 'unclassified' };
            }
        }

        function updateSentimentCounts(counts) {
            const fallbackCounts = {
                positive: 0,
                negative: 0,
                neutral: 0,
                suggestion: 0,
                unclassified: 0,
            };
            const merged = Object.assign(fallbackCounts, counts || {});

            Object.entries(merged).forEach(([key, value]) => {
                const selector = `[data-sentiment-count="${key}"]`;
                updateCountWithAnimation(selector, value);
            });
        }

        // Function to refresh feedbacks data
        async function refreshFeedbacksData() {
            try {
                const response = await fetch('../api/get_feedbacks_page_data.php', {
                    cache: 'no-store'
                });
                const data = await response.json();
                
                if (data.success) {
                    // Update summary cards with animation
                    updateCountWithAnimation('[data-stat="total-feedbacks"]', data.stats.total_feedbacks);
                    updateCountWithAnimation('[data-stat="today-feedbacks"]', data.stats.today_feedbacks);
                    updateCountWithAnimation('[data-stat="week-feedbacks"]', data.stats.this_week_feedbacks);
                    updateSentimentCounts(data.stats.sentiment_counts);
                    
                    // Update feedbacks table
                    feedbackCache = data.feedbacks || [];
                    renderFeedbacksTable();
                    
                    // Check for new feedbacks
                    if (data.stats.total_feedbacks > lastFeedbackCount) {
                        const newCount = data.stats.total_feedbacks - lastFeedbackCount;
                        showNotification(`New feedback${newCount > 1 ? 's' : ''} received!`, 'success');
                        lastFeedbackCount = data.stats.total_feedbacks;
                    }
                }
            } catch (error) {
                console.error('Error refreshing feedbacks data:', error);
            }
        }
        
        // Function to show notification
        function showNotification(message, type = 'info') {
            const notificationArea = document.getElementById('auto-refresh-notifications');
            if (!notificationArea) {
                return;
            }
            
            const notification = document.createElement('div');
            notification.className = `alert alert-${type} alert-dismissible fade show shadow-sm`;
            notification.style.marginBottom = '10px';
            notification.innerHTML = `
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            `;
            
            notificationArea.appendChild(notification);
            
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.classList.remove('show');
                    setTimeout(() => notification.remove(), 150);
                }
            }, 3000);
        }
        
        // Start auto-refresh when page loads
        document.addEventListener('DOMContentLoaded', function() {
            // Start auto-refresh every 3 seconds
            refreshInterval = setInterval(refreshFeedbacksData, 3000);
            renderFeedbacksTable();
            updateSentimentCounts(<?php echo json_encode($sentimentCounts, JSON_HEX_TAG | JSON_HEX_APOS | JSON_HEX_AMP | JSON_HEX_QUOT); ?>);
            
            // Also refresh when user becomes active (tab becomes visible)
            document.addEventListener('visibilitychange', function() {
                if (!document.hidden) {
                    refreshFeedbacksData();
                }
            });

            document.querySelectorAll('[data-sentiment-filter]').forEach((button) => {
                button.addEventListener('click', () => {
                    const filter = button.dataset.sentimentFilter || 'all';
                    applySentimentFilter(filter);
                });
            });
        });
        
        // Clean up interval when page unloads
        window.addEventListener('beforeunload', function() {
            if (refreshInterval) {
                clearInterval(refreshInterval);
            }
        });
    </script>
    
    <style>
        .pulse-animation {
            animation: pulse 0.5s ease-in-out;
        }
        
        @keyframes pulse {
            0%, 100% { transform: scale(1); }
            50% { transform: scale(1.05); }
        }
    </style>
</body>
</html>

