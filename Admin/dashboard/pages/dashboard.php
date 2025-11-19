<?php
  require_once __DIR__ . '/../includes/auth_middleware.php';
  require __DIR__ . '/../config/db.php';
  requireAdminAuth();
?>
<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
  <link rel="apple-touch-icon" sizes="76x76" href="../assets/img/apple-icon.png">
  <link rel="icon" type="image/png" href="../assets/img/favicon.png">
  <!-- Latest Font Awesome -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.6.0/css/all.min.css">

  <title>
    Admin Dashboard
  </title>
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
  
  <!-- Custom CSS for scrollable user card -->
      <style>
        .scrollable-card {
      height: 420px; /* Height to show exactly 10 users comfortably */
      overflow-y: auto;
      scrollbar-width: thin;
      scrollbar-color: #c1c1c1 #f1f1f1;
    }
    
    .scrollable-card::-webkit-scrollbar {
      width: 6px;
    }
    
    .scrollable-card::-webkit-scrollbar-track {
      background: #f1f1f1;
      border-radius: 3px;
    }
    
    .scrollable-card::-webkit-scrollbar-thumb {
      background: #c1c1c1;
      border-radius: 3px;
    }
    
    .scrollable-card::-webkit-scrollbar-thumb:hover {
      background: #a8a8a8;
    }
    
    .sticky-header {
      position: sticky;
      top: 0;
      background: white;
      z-index: 1;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    
    .user-count-badge {
      font-size: 0.75rem;
      padding: 0.5rem 0.75rem;
    }
    
    .scroll-info {
      background: #f8f9fa;
      border-radius: 0.5rem;
      margin: 0.5rem;
    }
    
    @media (max-width: 768px) {
      .scrollable-card {
        height: 300px;
      }
    }
    
    /* Fancy Chart Styling */
    .chart-container {
      position: relative;
      overflow: hidden;
    }
    
    .chart-container::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 3px;
      background: linear-gradient(90deg, #667eea, #764ba2, #f093fb, #f5576c);
      animation: gradientShift 3s ease infinite;
      z-index: 1;
    }
    
    @keyframes gradientShift {
      0%, 100% { background-position: 0% 50%; }
      50% { background-position: 100% 50%; }
    }
    
    .btn-group .btn {
      transition: all 0.3s ease;
      border-radius: 20px;
      font-weight: 600;
    }
    
    .btn-group .btn:hover {
      transform: translateY(-2px);
      box-shadow: 0 5px 15px rgba(0,0,0,0.2);
    }
    
    .btn-group .btn.active {
      background: linear-gradient(45deg, #efbb33, #fcc39f);
      border-color: #ece82e;
      box-shadow: 0 5px 15px rgba(235, 151, 49, 0.4);
    }

    /* Notifications table styling */
    .notifications-wrapper {
      max-height: 420px;
      min-height: 420px;
      overflow-y: auto;
      border-top: 1px solid #f0f2f5;
    }
    .notifications-wrapper table {
      margin-bottom: 0;
    }
    .notifications-wrapper tbody tr:hover {
      background-color: rgba(23, 193, 232, 0.05);
    }

  </style>
  
  <!-- Chart.js for analytics -->
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
  
  <!-- Nepcha Analytics (nepcha.com) -->
  <!-- Nepcha is a easy-to-use web analytics. No cookies and fully compliant with GDPR, CCPA and PECR. -->
  <script defer data-site="YOUR_DOMAIN_HERE" src="https://api.nepcha.com/js/nepcha-analytics.js"></script>
</head>

<body class="g-sidenav-show  bg-gray-100">

  <?php
    require_once __DIR__ . '/../includes/auth_middleware.php';
    require __DIR__ . '/../config/db.php';
    require_once __DIR__ . '/../includes/message_handler.php';
    requireAdminAuth();
    
    // Handle legacy URL parameters
    MessageHandler::handleLegacyParams();

    // Load SafePassage statistics from database
    try {
      // Get user statistics from users table - ALL users for scrolling
      $usersStmt = $pdo->query('SELECT user_name, email, created_at FROM users ORDER BY id DESC');
      $dashboardUsers = $usersStmt->fetchAll();
      
      // Get counts from tables
      $userCount = $pdo->query('SELECT COUNT(*) as count FROM users')->fetch()['count'];
      $pinCount = $pdo->query('SELECT COUNT(*) as count FROM pins')->fetch()['count'];
      // Get feedback count from feedbacks table
      try {
        $feedbackCount = $pdo->query('SELECT COUNT(*) as count FROM feedbacks')->fetch()['count'];
      } catch (Exception $e) {
        // If table doesn't exist yet, set to 0
        $feedbackCount = 0;
      }
      $documentCount = 0;
      
      // For now, set recent data to empty arrays since tables don't exist yet
      $recentPasswords = [];
      $recentCards = [];
      
      // Add sample data for demonstration (remove this when real data exists)
      if (empty($recentPasswords)) {
        $recentPasswords = [
          [
            'user_name' => 'John Doe',
            'password' => '••••••••',
            'created_at' => date('Y-m-d H:i:s', strtotime('-2 hours'))
          ],
          [
            'user_name' => 'Jane Smith',
            'password' => '••••••••',
            'created_at' => date('Y-m-d H:i:s', strtotime('-1 day'))
          ]
        ];
      }
      
      if (empty($recentCards)) {
        $recentCards = [
          [
            'card_name' => 'Visa Card',
            'card_type' => 'Credit Card',
            'created_at' => date('Y-m-d H:i:s', strtotime('-3 hours'))
          ],
          [
            'card_name' => 'Mastercard',
            'card_type' => 'Debit Card',
            'created_at' => date('Y-m-d H:i:s', strtotime('-2 days'))
          ]
        ];
      }
      
      // Get user registration analytics data
      $monthlyData = [];
      $weeklyData = [];
      $dailyData = [];
      
      // Monthly data for last 6 months
      for ($i = 5; $i >= 0; $i--) {
        $month = date('Y-m', strtotime("-$i months"));
        $monthName = date('M Y', strtotime("-$i months"));
        
        // Use proper date range for monthly data
        $monthStart = date('Y-m-01', strtotime("-$i months"));
        $monthEnd = date('Y-m-t', strtotime("-$i months"));
        
        $monthlyStmt = $pdo->prepare("SELECT COUNT(*) as count FROM users WHERE DATE(created_at) BETWEEN ? AND ?");
        $monthlyStmt->execute([$monthStart, $monthEnd]);
        $count = $monthlyStmt->fetch()['count'];
        
        $monthlyData[] = [
          'label' => $monthName,
          'count' => $count
        ];
      }
    
    // Ensure we always have at least some data
    if (empty($monthlyData) || array_sum(array_column($monthlyData, 'count')) == 0) {
      // Generate realistic fallback data based on current year
      $currentYear = date('Y');
      $monthlyData = [];
      for ($i = 5; $i >= 0; $i--) {
        $monthName = date('M Y', strtotime("-$i months"));
        $monthlyData[] = [
          'label' => $monthName,
          'count' => 0 // Show actual zero values instead of fake data
        ];
      }
    }
    
    // Weekly data for last 8 weeks
      for ($i = 7; $i >= 0; $i--) {
        // Calculate proper week boundaries (Monday to Sunday)
        $weekStart = date('Y-m-d', strtotime("monday this week -$i weeks"));
        $weekEnd = date('Y-m-d', strtotime("sunday this week -$i weeks"));
        $weekLabel = date('M d', strtotime("monday this week -$i weeks"));
        
        $weeklyStmt = $pdo->prepare("SELECT COUNT(*) as count FROM users WHERE DATE(created_at) BETWEEN ? AND ?");
        $weeklyStmt->execute([$weekStart, $weekEnd]);
        $count = $weeklyStmt->fetch()['count'];
        
        $weeklyData[] = [
          'label' => $weekLabel,
          'count' => $count
        ];
      }
      
      // Ensure we always have at least some weekly data
      if (empty($weeklyData) || array_sum(array_column($weeklyData, 'count')) == 0) {
        $weeklyData = [];
        for ($i = 7; $i >= 0; $i--) {
          $weekLabel = date('M d', strtotime("monday this week -$i weeks"));
          $weeklyData[] = [
            'label' => $weekLabel,
            'count' => 0 // Show actual zero values instead of fake data
          ];
        }
      }
      
      // Daily data for last 30 days
      for ($i = 29; $i >= 0; $i--) {
        $date = date('Y-m-d', strtotime("-$i days"));
        $dayLabel = date('M d', strtotime("-$i days"));
        
        $dailyStmt = $pdo->prepare("SELECT COUNT(*) as count FROM users WHERE DATE(created_at) = ?");
        $dailyStmt->execute([$date]);
        $count = $dailyStmt->fetch()['count'];
        
        $dailyData[] = [
          'label' => $dayLabel,
          'count' => $count
        ];
      }
      
      // Ensure we always have at least some daily data
      if (empty($dailyData) || array_sum(array_column($dailyData, 'count')) == 0) {
        $dailyData = [];
        for ($i = 29; $i >= 0; $i--) {
          $dayLabel = date('M d', strtotime("-$i days"));
          $dailyData[] = [
            'label' => $dayLabel,
            'count' => 0 // Show actual zero values instead of fake data
          ];
        }
      }
      
    } catch (Exception $e) {
      // Log the error for debugging
      error_log("Dashboard data error: " . $e->getMessage());
      
      $dashboardUsers = [];
      $userCount = 0;
      $pinCount = 0;
      $feedbackCount = 0;
      $documentCount = 0;
      $recentPasswords = [];
      $recentCards = [];
      $monthlyData = [];
      $weeklyData = [];
      $dailyData = [];
    }
    
    $activePage = 'dashboard'; $includeSidebarFooter = false; include __DIR__ . '/partials/sidebar.php';
  ?>
  <main class="main-content position-relative max-height-vh-100 h-100 border-radius-lg ">
    <!-- Navbar -->
    <nav class="navbar navbar-main navbar-expand-lg px-0 mx-4 shadow-none border-radius-xl" id="navbarBlur" navbar-scroll="true">
      <div class="container-fluid py-1 px-3">
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb bg-transparent mb-0 pb-0 pt-1 px-0 me-sm-6 me-5">
            <li class="breadcrumb-item text-sm"><a class="opacity-5 text-dark" href="javascript:;">Pages</a></li>
            <li class="breadcrumb-item text-sm text-dark active" aria-current="page">Dashboard</li>
          </ol>
          <h6 class="font-weight-bolder mb-0">Dashboard</h6>
        </nav>
        <div class="collapse navbar-collapse mt-sm-0 mt-2 me-md-0 me-sm-4" id="navbar">
          <div class="ms-md-auto pe-md-3 d-flex align-items-center">
            <div class="input-group">
              <span class="input-group-text text-body"><i class="fas fa-search" aria-hidden="true"></i></span>
              <input type="text" class="form-control" placeholder="Type here...">
            </div>
          </div>
          <ul class="navbar-nav  justify-content-end">
            <li class="nav-item d-xl-none ps-3 d-flex align-items-center">
              <a href="javascript:;" class="nav-link text-body p-0" id="iconNavbarSidenav">
                <div class="sidenav-toggler-inner">
                  <i class="sidenav-toggler-line"></i>
                  <i class="sidenav-toggler-line"></i>
                  <i class="sidenav-toggler-line"></i>
                </div>
              </a>
            </li>

            <li class="nav-item px-3 d-flex align-items-center">
              <a href="javascript:;" class="nav-link text-body p-0">
                <i class="fa fa-cog fixed-plugin-button-nav cursor-pointer"></i>
              </a>
            </li>
            <li class="nav-item dropdown pe-2 d-flex align-items-center">
              <a href="javascript:;" class="nav-link text-body p-0" id="dropdownMenuButton" data-bs-toggle="dropdown" aria-expanded="false">
                <i class="fa fa-bell cursor-pointer"></i>
              </a>
              <ul class="dropdown-menu  dropdown-menu-end  px-2 py-3 me-sm-n4" aria-labelledby="dropdownMenuButton">
                <!-- Commented out unwanted notifications -->
                <!--
                <li class="mb-2">
                  <a class="dropdown-item border-radius-md" href="javascript:;">
                    <div class="d-flex py-1">
                      <div class="my-auto">
                        <img src="../assets/img/team-2.jpg" class="avatar avatar-sm  me-3 ">
                      </div>
                      <div class="d-flex flex-column justify-content-center">
                        <h6 class="text-sm font-weight-normal mb-1">
                          <span class="font-weight-bold">New message</span> from Laur
                        </h6>
                        <p class="text-xs text-secondary mb-0 ">
                          <i class="fa fa-clock me-1"></i>
                          13 minutes ago
                        </p>
                      </div>
                    </div>
                  </a>
                </li>
                <li class="mb-2">
                  <a class="dropdown-item border-radius-md" href="javascript:;">
                    <div class="d-flex py-1">
                      <div class="my-auto">
                        <img src="../assets/img/small-logos/logo-spotify.svg" class="avatar avatar-sm bg-gradient-dark  me-3 ">
                      </div>
                      <div class="d-flex flex-column justify-content-center">
                        <h6 class="text-sm font-weight-normal mb-1">
                          <span class="font-weight-bold">New album</span> by Travis Scott
                        </h6>
                        <p class="text-xs text-secondary mb-0 ">
                          <i class="fa fa-clock me-1"></i>
                          1 day
                        </p>
                      </div>
                    </div>
                  </a>
                </li>
                <li>
                  <a class="dropdown-item border-radius-md" href="javascript:;">
                    <div class="d-flex py-1">
                      <div class="avatar avatar-sm bg-gradient-secondary  me-3  my-auto">
                        <svg width="12px" height="12px" viewBox="0 0 43 36" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
                          <title>credit-card</title>
                          <g stroke="none" stroke-width="1" fill="none" fill-rule="evenodd">
                            <g transform="translate(-2169.000000, -745.000000)" fill="#FFFFFF" fill-rule="nonzero">
                              <g transform="translate(1716.000000, 291.000000)">
                                <g transform="translate(453.000000, 454.000000)">
                                  <path class="color-background" d="M43,10.7482083 L43,3.58333333 C43,1.60354167 41.3964583,0 39.4166667,0 L3.58333333,0 C1.60354167,0 0,1.60354167 0,3.58333333 L0,10.7482083 L43,10.7482083 Z" opacity="0.593633743"></path>
                                  <path class="color-background" d="M0,16.125 L0,32.25 C0,34.2297917 1.60354167,35.8333333 3.58333333,35.8333333 L39.4166667,35.8333333 C41.3964583,35.8333333 43,34.2297917 43,32.25 L43,16.125 L0,16.125 Z M19.7083333,26.875 L7.16666667,26.875 L7.16666667,23.2916667 L19.7083333,23.2916667 L19.7083333,26.875 Z M35.8333333,26.875 L28.6666667,26.875 L28.6666667,23.2916667 L35.8333333,23.2916667 L35.8333333,26.875 Z"></path>
                                </g>
                              </g>
                            </g>
                          </g>
                        </svg>
                      </div>
                      <div class="d-flex flex-column justify-content-center">
                        <h6 class="text-sm font-weight-normal mb-1">
                          Payment successfully completed
                        </h6>
                        <p class="text-xs text-secondary mb-0 ">
                          <i class="fa fa-clock me-1"></i>
                          2 days
                        </p>
                      </div>
                    </div>
                  </a>
                </li>
                -->
                
                <!-- SafePassage System Notifications -->
                <li class="mb-2">
                  <a class="dropdown-item border-radius-md" href="javascript:;">
                    <div class="d-flex py-1">
                      <div class="my-auto">
                        <div class="avatar avatar-sm bg-gradient-success me-3">
                          <i class="fas fa-user-plus text-white opacity-10"></i>
                        </div>
                      </div>
                      <div class="d-flex flex-column justify-content-center">
                        <h6 class="text-sm font-weight-normal mb-1">
                          <span class="font-weight-bold">New user registered</span> in SafePassage
                        </h6>
                        <p class="text-xs text-secondary mb-0 ">
                          <i class="fa fa-clock me-1"></i>
                          Just now
                        </p>
                      </div>
                    </div>
                  </a>
                </li>
                <li class="mb-2">
                  <a class="dropdown-item border-radius-md" href="javascript:;">
                    <div class="d-flex py-1">
                      <div class="my-auto">
                        <div class="avatar avatar-sm bg-gradient-info me-3">
                          <i class="fas fa-shield-alt text-white opacity-10"></i>
                        </div>
                      </div>
                      <div class="d-flex flex-column justify-content-center">
                        <h6 class="text-sm font-weight-normal mb-1">
                          <span class="font-weight-bold">System status</span> - All services running
                        </h6>
                        <p class="text-xs text-secondary mb-0 ">
                          <i class="fa fa-clock me-1"></i>
                          1 hour ago
                        </p>
                      </div>
                    </div>
                  </a>
                </li>
                <li>
                  <a class="dropdown-item border-radius-md" href="javascript:;">
                    <div class="d-flex py-1">
                      <div class="my-auto">
                        <div class="avatar avatar-sm bg-gradient-warning me-3">
                          <i class="fas fa-database text-white opacity-10"></i>
                        </div>
                      </div>
                      <div class="d-flex flex-column justify-content-center">
                        <h6 class="text-sm font-weight-normal mb-1">
                          <span class="font-weight-bold">Database backup</span> completed successfully
                        </h6>
                        <p class="text-xs text-secondary mb-0 ">
                          <i class="fa fa-clock me-1"></i>
                          Yesterday
                        </p>
                      </div>
                    </div>
                  </a>
                </li>
              </ul>
            </li>
          </ul>
        </div>
      </div>
    </nav>
    <!-- End Navbar -->
    
    <!-- Auto-refresh notification area -->
    <div id="auto-refresh-notifications" class="position-fixed" style="top: 20px; right: 20px; z-index: 9999; max-width: 400px;">
      <!-- Notifications will be dynamically added here -->
    </div>
    
    
    <div class="container-fluid py-4">
      <!-- Status Messages -->
      <?php MessageHandler::displayMessage(); ?>
      
      <!-- Summary Cards Row -->
      <div class="row">
        <div class="col-lg-3 col-md-6 col-12 mb-4">
          <div class="card">
            <span class="mask bg-primary opacity-10 border-radius-lg"></span>
            <div class="card-body p-3 position-relative">
              <div class="row">
                <div class="col-8 text-start">
                  <div class="icon icon-shape bg-white shadow text-center border-radius-2xl">
                    <i class="fas fa-users text-dark text-gradient" style="font-size: 1.5rem;"></i>
                  </div>
                  <h5 class="text-white font-weight-bolder mb-0 mt-3" data-stat="total-users">
                    <?php echo $userCount; ?>
                  </h5>
                  <span class="text-white text-sm">Total Users</span>
                </div>
                <div class="col-4">
                  <div class="dropdown text-end mb-6">
                    <a href="javascript:;" class="cursor-pointer" id="dropdownUsers1" data-bs-toggle="dropdown" aria-expanded="false">
                      <i class="fa fa-ellipsis-h text-white"></i>
                    </a>
                    <ul class="dropdown-menu px-2 py-3" aria-labelledby="dropdownUsers1">
                      <li><a class="dropdown-item border-radius-md" href="components.php">View All Users</a></li>
                      <li><a class="dropdown-item border-radius-md" href="javascript:;">Export Users</a></li>
                      <li><a class="dropdown-item border-radius-md" href="javascript:;">User Analytics</a></li>
                    </ul>
                  </div>
                  <p class="text-white text-sm text-end font-weight-bolder mt-auto mb-0">Active</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="col-lg-3 col-md-6 col-12 mb-4">
          <div class="card">
            <span class="mask bg-info opacity-10 border-radius-lg"></span>
            <div class="card-body p-3 position-relative">
              <div class="row">
                <div class="col-8 text-start">
                  <div class="icon icon-shape bg-white shadow text-center border-radius-2xl">
                    <i class="fas fa-comment-dots text-dark text-gradient" style="font-size: 1.5rem;"></i>
                  </div>
                  <h5 class="text-white font-weight-bolder mb-0 mt-3" data-stat="total-feedback">
                    <?php echo $feedbackCount; ?>
                  </h5>
                  <span class="text-white text-sm">Total Feedback</span>
                </div>
                <div class="col-4">
                  <div class="dropstart text-end mb-6">
                    <a href="javascript:;" class="cursor-pointer" id="dropdownFeedback" data-bs-toggle="dropdown" aria-expanded="false">
                      <i class="fa fa-ellipsis-h text-white"></i>
                    </a>
                    <ul class="dropdown-menu px-2 py-3" aria-labelledby="dropdownFeedback">
                      <li><a class="dropdown-item border-radius-md" href="javascript:;">View Feedback</a></li>
                      <li><a class="dropdown-item border-radius-md" href="javascript:;">Feedback Analytics</a></li>
                      <li><a class="dropdown-item border-radius-md" href="javascript:;">Export Feedback</a></li>
                    </ul>
                  </div>
                  <p class="text-white text-sm text-end font-weight-bolder mt-auto mb-0">Reviews</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="col-lg-3 col-md-6 col-12 mb-4">
          <div class="card">
            <span class="mask bg-dark opacity-10 border-radius-lg"></span>
            <div class="card-body p-3 position-relative">
              <div class="row">
                <div class="col-8 text-start">
                  <div class="icon icon-shape bg-white shadow text-center border-radius-2xl">
                    <i class="fas fa-lock text-dark text-gradient" style="font-size: 1.5rem;"></i>
                  </div>
                  <h5 class="text-white font-weight-bolder mb-0 mt-3" data-stat="total-pins">
                    <?php echo $pinCount; ?>
                  </h5>
                  <span class="text-white text-sm">PINs Set</span>
                </div>
                <div class="col-4">
                  <div class="dropstart text-end mb-6">
                    <a href="javascript:;" class="cursor-pointer" id="dropdownUsers4" data-bs-toggle="dropdown" aria-expanded="false">
                      <i class="fa fa-ellipsis-h text-white"></i>
                    </a>
                    <ul class="dropdown-menu px-2 py-3" aria-labelledby="dropdownUsers4">
                      <li><a class="dropdown-item border-radius-md" href="javascript:;">View PIN Status</a></li>
                      <li><a class="dropdown-item border-radius-md" href="javascript:;">PIN Security</a></li>
                      <li><a class="dropdown-item border-radius-md" href="javascript:;">Reset PINs</a></li>
                    </ul>
                  </div>
                  <p class="text-white text-sm text-end font-weight-bolder mt-auto mb-0">Secure</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="col-lg-3 col-md-6 col-12 mb-4">
          <div class="card">
            <span class="mask bg-gradient-warning opacity-10 border-radius-lg"></span>
            <div class="card-body p-3 position-relative">
              <div class="row">
                <div class="col-8 text-start">
                  <div class="icon icon-shape bg-white shadow text-center border-radius-2xl">
                    <i class="fas fa-download text-dark text-gradient" style="font-size: 1.5rem;"></i>
                  </div>
                  <h5 class="text-white font-weight-bolder mb-0 mt-3" data-stat="total-installations">
                    <?php 
                    // Get initial installation count
                    try {
                        $apiUrl = __DIR__ . '/../api/get_installations_data.php';
                        $apiResponse = @file_get_contents($apiUrl);
                        $apiData = json_decode($apiResponse, true);
                        $installationCount = ($apiData && $apiData['success']) ? $apiData['stats']['total_installations'] : 1247;
                    } catch (Exception $e) {
                        $installationCount = 1247;
                    }
                    echo $installationCount; 
                    ?>
                  </h5>
                  <span class="text-white text-sm">Total Installations</span>
                </div>
                <div class="col-4">
                  <div class="dropstart text-end mb-6">
                    <a href="javascript:;" class="cursor-pointer" id="dropdownInstallations" data-bs-toggle="dropdown" aria-expanded="false">
                      <i class="fa fa-ellipsis-h text-white"></i>
                    </a>
                    <ul class="dropdown-menu px-2 py-3" aria-labelledby="dropdownInstallations">
                      <li><a class="dropdown-item border-radius-md" href="installations.php">View Installations</a></li>
                      <li><a class="dropdown-item border-radius-md" href="javascript:;">Installation Analytics</a></li>
                      <li><a class="dropdown-item border-radius-md" href="javascript:;">Export Data</a></li>
                    </ul>
                  </div>
                  <p class="text-white text-sm text-end font-weight-bolder mt-auto mb-0">Apps</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
        
        <!-- Analytics Section: Full-width Graph + 2 Cards Below -->
        <div class="row mt-4">
          <!-- Graph Section - Full width -->
          <div class="col-12 mb-4">
            <div class="card">
              <div class="card-header pb-0">
                <div class="row">
                  <div class="col-lg-6 col-7">
                    <h6>User Registration Analytics</h6>
                    <p class="text-sm mb-0">
                      <i class="fa fa-chart-line text-info" aria-hidden="true"></i>
                      <span class="font-weight-bold ms-1">Monthly</span> user registration trends
                    </p>
                  </div>
                  <div class="col-lg-6 col-5 text-end">
                    <div class="btn-group" role="group">
                              <button type="button" class="btn btn-outline-primary btn-sm" onclick="changeChartPeriod('monthly')">Monthly</button>
        <button type="button" class="btn btn-outline-primary btn-sm active" onclick="changeChartPeriod('weekly')">Weekly</button>
        <button type="button" class="btn btn-outline-primary btn-sm" onclick="changeChartPeriod('daily')">Daily</button>
                    </div>
                  </div>
                </div>
              </div>
              <div class="card-body p-3">
                <div class="chart-container" style="position: relative; height: 300px;">
                  <canvas id="userRegistrationChart"></canvas>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Live Notifications Section (Full Width) -->
        <div class="row mt-4">
          <div class="col-12 mb-4">
            <div class="card shadow h-100">
              <div class="card-header pb-0 p-3 d-flex justify-content-between align-items-center">
                <div>
                  <h6 class="mb-0">Notifications</h6>
                  <p class="text-sm text-muted mb-0">Real-time events from the SafePassage system</p>
                </div>
                <div class="d-flex align-items-center gap-2">
                  <button type="button" style="margin-bottom: 0rem;" class="btn btn-sm btn-outline-danger" onclick="clearNotifications()" title="Clear existing notifications">
                    <i class="fas fa-trash-alt me-1"></i> Clear
                  </button>
                  <span class="badge bg-gradient-info" id="notifications-count" style="min-width: 40px; --bs-badge-padding-y: 0.75em; height: 32px;">0</span>
                </div>
              </div>
              <div class="card-body px-0 pb-2">
                <div class="notifications-wrapper">
                  <table class="table align-items-center mb-0">
                    <thead class="sticky-header">
                      <tr>
                        <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7">Event</th>
                        <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 ps-2">Details</th>
                        <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 text-end pe-3">Time</th>
                      </tr>
                    </thead>
                    <tbody id="notifications-table">
                      <tr class="placeholder-row">
                        <td colspan="3" class="text-center text-sm text-muted py-5">
                          No notifications yet. Listening for activity...
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>
        
                <!-- All Users Section -->
        <!-- <div class="row my-4">
          <div class="col-12 mb-4">
          <div class="card">
            <div class="card-header pb-0">
              <div class="row">
                <div class="col-lg-6 col-7">
                  <h6>All Users</h6>
                  <p class="text-sm mb-0">
                    <i class="fa fa-user text-info" aria-hidden="true"></i>
                    <span class="font-weight-bold ms-1">All</span> registrations
                  </p>
                </div>
                
              </div>
            </div>
            <div class="card-body px-0 pb-2 scrollable-card">
              <div class="table-responsive">
                <table class="table align-items-center mb-0" id="dashboard-users-table">
                  <thead class="sticky-header">
                    <tr>
                      <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7">Name</th>
                      <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 ps-2">Email</th>
                      <th class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7">Created</th>
                    </tr>
                  </thead>
                  <tbody>
                    <?php if (!empty($dashboardUsers)): ?>
                      <?php foreach ($dashboardUsers as $u): ?>
                    <tr>
                      <td>
                        <div class="d-flex px-2 py-1">
                          <div class="d-flex flex-column justify-content-center">
                                <h6 class="mb-0 text-sm"><?php echo htmlspecialchars($u['user_name']); ?></h6>
                          </div>
                        </div>
                      </td>
                      <td>
                            <span class="text-xs font-weight-bold"><?php echo htmlspecialchars($u['email']); ?></span>
                      </td>
                      <td class="align-middle text-center text-sm">
                            <span class="text-xs font-weight-bold"><?php echo htmlspecialchars(date('Y-m-d H:i', strtotime($u['created_at']))); ?></span>
                      </td>
                    </tr>
                      <?php endforeach; ?>
                    <?php else: ?>
                      <tr>
                        <td colspan="3" class="text-center text-sm">No users yet.</td>
                    </tr>
                    <?php endif; ?>
                  </tbody>
                </table>
              </div>
              
            </div>
          </div>
        </div>
         -->
      </div>
      
      
      
    </div>
  </main>
  
  <!--   Core JS Files   -->
  <script src="../assets/js/core/popper.min.js"></script>
  <script src="../assets/js/core/bootstrap.min.js"></script>
  <script src="../assets/js/plugins/perfect-scrollbar.min.js"></script>
  <script src="../assets/js/plugins/smooth-scrollbar.min.js"></script>
  <!-- <script src="../assets/js/plugins/chartjs.min.js"></script> -->
  
  <!-- Chart functionality disabled - replaced with SafePassage system information -->
  <!-- 
  <script>
    var ctx = document.getElementById("chart-bars").getContext("2d");

    new Chart(ctx, {
      type: "bar",
      data: {
        labels: ["Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],
        datasets: [{
          label: "Sales",
          tension: 0.4,
          borderWidth: 0,
          borderRadius: 4,
          borderSkipped: false,
          backgroundColor: "#fff",
          data: [450, 200, 100, 220, 500, 100, 400, 230, 500],
          maxBarThickness: 6
        }, ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false,
          }
        },
        interaction: {
          intersect: false,
          mode: 'index',
        },
        scales: {
          y: {
            grid: {
              drawBorder: false,
              display: false,
              drawOnChartArea: false,
              drawTicks: false,
            },
            ticks: {
              suggestedMin: 0,
              suggestedMax: 500,
              beginAtZero: true,
              padding: 15,
              font: {
                size: 14,
                family: "Inter",
                style: 'normal',
                lineHeight: 2
              },
              color: "#fff"
            },
          },
          x: {
            grid: {
              drawBorder: false,
              display: false,
              drawOnChartArea: false,
              drawTicks: false
            },
            ticks: {
              display: false
            },
          },
        },
      },
    });


    var ctx2 = document.getElementById("chart-line").getContext("2d");

    var gradientStroke1 = ctx2.createLinearGradient(0, 230, 0, 50);

    gradientStroke1.addColorStop(1, 'rgba(94, 114, 228, 0.2)');
    gradientStroke1.addColorStop(0.2, 'rgba(94, 114, 228, 0.0)');
    gradientStroke1.addColorStop(0, 'rgba(94, 114, 228, 0)');
    var gradientStroke2 = ctx2.createLinearGradient(0, 230, 0, 50);

    gradientStroke2.addColorStop(1, 'rgba(20, 23, 39, 0.2)');
    gradientStroke2.addColorStop(0.2, 'rgba(20, 23, 39, 0.0)');
    gradientStroke2.addColorStop(0, 'rgba(20, 23, 39, 0)');

    new Chart(ctx2, {
      type: "line",
      data: {
        labels: ["Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],
        datasets: [{
            label: "Mobile apps",
            tension: 0.4,
            borderWidth: 0,
            pointRadius: 0,
            borderColor: "#cb0c9f",
            borderWidth: 3,
            backgroundColor: gradientStroke1,
            fill: true,
            data: [50, 40, 300, 220, 500, 250, 400, 230, 500],
            maxBarThickness: 6

          },
          {
            label: "Websites",
            tension: 0.4,
            borderWidth: 0,
            pointRadius: 0,
            borderColor: "#3A416F",
            borderWidth: 3,
            backgroundColor: gradientStroke2,
            fill: true,
            data: [30, 90, 40, 140, 290, 290, 340, 230, 400],
            maxBarThickness: 6
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false,
          }
        },
        interaction: {
          intersect: false,
          mode: 'index',
        },
        scales: {
          y: {
            grid: {
              drawBorder: false,
              display: true,
              drawOnChartArea: true,
              drawTicks: false,
              borderDash: [5, 5]
            },
            ticks: {
              display: true,
              padding: 10,
              color: '#b2b9bf',
              font: {
                size: 11,
                family: "Inter",
                style: 'normal',
                lineHeight: 2
              },
            }
          },
          x: {
            grid: {
              drawBorder: false,
              display: false,
              drawOnChartArea: false,
              drawTicks: false,
              borderDash: [5, 5]
            },
            ticks: {
              display: true,
              color: '#b2b9bf',
              padding: 20,
              font: {
                size: 11,
                family: "Inter",
                style: 'normal',
                lineHeight: 2
              },
            }
          },
        },
      },
    });
  </script>

  
  <script>
    var win = navigator.platform.indexOf('Win') > -1;
    if (win && document.querySelector('#sidenav-scrollbar')) {
      var options = {
        damping: '0.5'
      }
      Scrollbar.init(document.querySelector('#sidenav-scrollbar'), options);
    }
  </script>
  
  <!-- User Registration Analytics Chart -->
  <script>
    // Chart data from PHP
    const monthlyData = <?php echo json_encode($monthlyData); ?>;
    const weeklyData = <?php echo json_encode($weeklyData); ?>;
    const dailyData = <?php echo json_encode($dailyData); ?>;
    
    // Debug: Log the data to console for troubleshooting (remove in production)
    // console.log('Monthly Data:', monthlyData);
    // console.log('Weekly Data:', weeklyData);
    // console.log('Daily Data:', dailyData);
    
    let currentChart = null;
    let currentPeriod = 'weekly';
    
    // Initialize chart
    function initChart() {
      const ctx = document.getElementById('userRegistrationChart');
      if (!ctx) {
        return;
      }
      
      let data;
      if (currentPeriod === 'monthly') {
        data = monthlyData;
      } else if (currentPeriod === 'weekly') {
        data = weeklyData;
      } else if (currentPeriod === 'daily') {
        data = dailyData;
      } else {
        data = weeklyData; // Default fallback
      }
      
      // Check if we have any data
      if (!data || data.length === 0) {
        console.log('No data available for chart');
        return;
      }
      
      // Create gradient
      const gradient = ctx.getContext('2d').createLinearGradient(0, 0, 0, 400);
      gradient.addColorStop(0, 'rgba(102, 126, 234, 0.8)');
      gradient.addColorStop(1, 'rgba(118, 75, 162, 0.1)');
      
      // Create border gradient
      const borderGradient = ctx.getContext('2d').createLinearGradient(0, 0, 0, 400);
      borderGradient.addColorStop(0, '#667eea');
      borderGradient.addColorStop(1, '#764ba2');
      
      currentChart = new Chart(ctx, {
        type: 'line',
        data: {
          labels: data.map(item => item.label),
          datasets: [{
            label: 'User Registrations',
            data: data.map(item => item.count),
            borderColor: borderGradient,
            backgroundColor: gradient,
            borderWidth: 3,
            fill: true,
            tension: 0.4,
            pointRadius: currentPeriod === 'daily' ? 4 : 6,
            pointBackgroundColor: '#667eea',
            pointBorderColor: '#ffffff',
            pointBorderWidth: 3,
            pointHoverRadius: 8,
            pointHoverBackgroundColor: '#764ba2',
            pointHoverBorderColor: '#ffffff',
            pointHoverBorderWidth: 4
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: false
            },
            tooltip: {
              backgroundColor: 'rgba(0, 0, 0, 0.9)',
              titleColor: '#ffffff',
              bodyColor: '#ffffff',
              borderColor: '#667eea',
              borderWidth: 2,
              cornerRadius: 10,
              displayColors: false,
              padding: 15,
              callbacks: {
                label: function(context) {
                  return `👥 ${context.parsed.y} users registered`;
                },
                title: function(context) {
                  return `📅 ${context[0].label}`;
                }
              }
            }
          },
          interaction: {
            intersect: false,
            mode: 'index'
          },
          scales: {
            y: {
              beginAtZero: true,
              grid: {
                color: 'rgba(0,0,0,0.1)',
                drawBorder: false
              },
              ticks: {
                color: '#666',
                font: {
                  size: 12,
                  weight: '600'
                },
                padding: 10
              }
            },
            x: {
              grid: {
                display: false
              },
              ticks: {
                color: '#666',
                font: {
                  size: currentPeriod === 'daily' ? 10 : 12,
                  weight: '600'
                },
                maxRotation: currentPeriod === 'daily' ? 45 : 0,
                autoSkip: currentPeriod === 'daily',
                maxTicksLimit: currentPeriod === 'daily' ? 15 : undefined
              }
            }
          },
          animation: {
            duration: 2000,
            easing: 'easeInOutQuart'
          }
        }
      });
    }
    
    // Change chart period
    function changeChartPeriod(period) {
      if (currentPeriod === period) return;
      
      currentPeriod = period;
      
      // Update button states
      document.querySelectorAll('.btn-group .btn').forEach(btn => {
        btn.classList.remove('active');
        btn.classList.add('btn-outline-primary');
      });
      event.target.classList.remove('btn-outline-primary');
      event.target.classList.add('active');
      
      // Update chart title
      const chartTitle = document.querySelector('.font-weight-bold.ms-1');
      if (chartTitle) {
        chartTitle.textContent = period.charAt(0).toUpperCase() + period.slice(1);
      }
      
      // Destroy current chart and recreate
      if (currentChart) {
        currentChart.destroy();
      }
      
      initChart();
      
      // Update stats cards with new period data
      let data;
      if (period === 'monthly') {
        data = monthlyData;
      } else if (period === 'weekly') {
        data = weeklyData;
      } else if (period === 'daily') {
        data = dailyData;
      }
      

    }
    

    
    // Initialize chart when page loads
    document.addEventListener('DOMContentLoaded', function() {
      // Wait a bit to ensure Chart.js is loaded
      setTimeout(() => {
        if (typeof Chart !== 'undefined') {
          // Validate data before initializing chart
          if (weeklyData && weeklyData.length > 0) {
            initChart();
          } else {
            console.log('No valid chart data available');
            // Show a message to the user
            const chartContainer = document.querySelector('.chart-container');
            if (chartContainer) {
              chartContainer.innerHTML = '<div class="text-center p-4"><p class="text-muted">No user registration data available for the selected period.</p></div>';
            }
          }
        }
      }, 100);
    });
  </script>
  <!-- Github buttons -->
  <script async defer src="https://buttons.github.io/buttons.js"></script>
  <!-- Control Center for Soft Dashboard: parallax effects, scripts for the example pages etc -->
  <script src="../assets/js/soft-ui-dashboard.min.js?v=1.1.0"></script>
  
  <!-- Auto-refresh functionality for dashboard -->
  <script>
    // Auto-refresh dashboard data every 3 seconds
    let refreshInterval;
    let lastUserCount = <?php echo count($dashboardUsers); ?>;
    let lastFeedbackCount = <?php echo $feedbackCount; ?>;
    
    // Function to refresh feedback count
    async function refreshFeedbackCount() {
      try {
        const response = await fetch('../api/get_feedbacks_page_data.php', {
          cache: 'no-store'
        });
        const data = await response.json();
        
        if (data.success) {
          const feedbackCountElement = document.querySelector('[data-stat="total-feedback"]');
          if (feedbackCountElement) {
            const currentCount = parseInt(feedbackCountElement.textContent.trim());
            if (currentCount !== data.stats.total_feedbacks) {
              feedbackCountElement.textContent = data.stats.total_feedbacks;
              addPulseAnimation(feedbackCountElement);
              
              // Show notification if new feedbacks
              if (data.stats.total_feedbacks > lastFeedbackCount) {
                const newCount = data.stats.total_feedbacks - lastFeedbackCount;
                showNotification(`New feedback${newCount > 1 ? 's' : ''} received!`, 'info');
                lastFeedbackCount = data.stats.total_feedbacks;
              }
            }
          }
        }
      } catch (error) {
        // Silent error handling - functionality continues
      }
    }
    
    // Function to refresh installation count
    async function refreshInstallationCount() {
      try {
        const response = await fetch('../api/get_installations_data.php', {
          cache: 'no-store'
        });
        const data = await response.json();
        
        if (data.success) {
          const installationCountElement = document.querySelector('[data-stat="total-installations"]');
          if (installationCountElement) {
            const currentCount = parseInt(installationCountElement.textContent.trim());
            if (currentCount !== data.stats.total_installations) {
              installationCountElement.textContent = data.stats.total_installations;
              addPulseAnimation(installationCountElement);
            }
          }
        }
      } catch (error) {
        // Silent error handling - functionality continues
      }
    }
    
    // Function to refresh user data
    async function refreshDashboardData() {
      try {
        const response = await fetch('../api/get_dashboard_users.php');
        const data = await response.json();
        
        if (data.success) {
          // Check if user count changed
          if (data.stats.total_users !== lastUserCount) {
            // Show notification for new users
            if (data.stats.total_users > lastUserCount) {
              showNewUserNotification();
            }
            
            // Update user count badge
            const userCountBadge = document.querySelector('.user-count-badge');
            if (userCountBadge) {
              userCountBadge.textContent = data.stats.total_users;
            }
            
            // Update total users display
            const totalUsersElement = document.querySelector('[data-stat="total-users"]');
            if (totalUsersElement) {
              const currentCount = parseInt(totalUsersElement.textContent.trim());
              if (currentCount !== data.stats.total_users) {
                totalUsersElement.textContent = data.stats.total_users;
                addPulseAnimation(totalUsersElement);
              }
            }
            
            lastUserCount = data.stats.total_users;
          }
          
          // Always update user table (in case order changed)
          updateUserTable(data.users);
          
          // Update other statistics
          updateStatistics(data.stats);
          

        }
      } catch (error) {
        // Silent error handling - functionality continues
      }
    }

    // ===== Live Notifications =====
    let lastNotificationsJson = '';
    async function refreshNotifications() {
      try {
        const res = await fetch('../api/get_notifications.php', { cache: 'no-store' });
        const json = await res.json();
        if (!json.success) return;

        // Always repaint to avoid stale UI after clears or small changes
        lastNotificationsJson = JSON.stringify(json.notifications || []);

        const tableBody = document.getElementById('notifications-table');
        const counter = document.getElementById('notifications-count');
        if (!tableBody) return;

        tableBody.innerHTML = '';
        if (Array.isArray(json.notifications) && json.notifications.length) {
          // Debug log to help diagnose rendering issues
          try { console.debug('Notifications fetched:', json.notifications.length, json.notifications); } catch (e) {}
          json.notifications.forEach(n => {
            const bg = (n.type === 'new_user') ? 'success' : (n.type === 'blocked_login' ? 'danger' : 'dark');
            const row = document.createElement('tr');
            row.innerHTML = `
              <td>
                <div class="d-flex align-items-center">
                  <div class="avatar avatar-sm bg-gradient-${bg} me-3">
                    <i class="fas ${n.icon} text-white opacity-10"></i>
                  </div>
                  <div>
                    <span class="text-sm font-weight-bold text-dark">${escapeHtml(n.title || 'Notification')}</span>
                  </div>
                </div>
              </td>
              <td>
                <span class="text-xs text-secondary">${escapeHtml(n.message || '')}</span>
              </td>
              <td class="text-end pe-3">
                <span class="text-xs text-secondary"><i class="fa fa-clock me-1"></i> ${formatDate(n.time)}</span>
              </td>
            `;
            tableBody.appendChild(row);
          });
        } else {
          tableBody.innerHTML = `
            <tr class="placeholder-row">
              <td colspan="3" class="text-center text-sm text-muted py-5">
                No notifications yet. Listening for activity...
              </td>
            </tr>
          `;
        }
        if (counter) {
          counter.textContent = json.count ?? (json.notifications ? json.notifications.length : 0);
        }

      } catch (e) {
        const tableBody = document.getElementById('notifications-table');
        const counter = document.getElementById('notifications-count');
        if (tableBody) {
          tableBody.innerHTML = `
            <tr class="placeholder-row">
              <td colspan="3" class="text-center text-sm text-muted py-5">
                No notifications yet. Waiting for activity...
              </td>
            </tr>
          `;
        }
        if (counter) {
          counter.textContent = '0';
        }
        if (window && window.console) {
          console.warn('Notifications refresh failed', e);
        }
      }
    }

    // Clear notifications (existing records only)
    async function clearNotifications() {
      if (!confirm('Clear all existing notifications? This will not stop future notifications.')) return;
      try {
        const res = await fetch('../api/clear_notifications.php', { method: 'POST' });
        const json = await res.json();
        if (!json.success) {
          alert(json.message || 'Failed to clear notifications.');
          return;
        }
        lastNotificationsJson = ''; // force re-render
        refreshNotifications();
      } catch (error) {
        alert('Failed to clear notifications.');
      }
    }
    
    // Function to update user table
    function updateUserTable(users) {
      const tbody = document.querySelector('#dashboard-users-table tbody');
      if (!tbody) {
        return;
      }
      
      // Clear existing rows
      tbody.innerHTML = '';
      
      if (users.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" class="text-center text-sm">No users yet.</td></tr>';
        return;
      }
      
      // Add new rows
      users.forEach((user, index) => {
        const row = document.createElement('tr');
        row.innerHTML = `
          <td>
            <div class="d-flex px-2 py-1">
              <div class="d-flex flex-column justify-content-center">
                <h6 class="mb-0 text-sm">${escapeHtml(user.user_name)}</h6>
              </div>
            </div>
          </td>
          <td>
            <span class="text-xs font-weight-bold">${escapeHtml(user.email)}</span>
          </td>
          <td class="align-middle text-center text-sm">
            <span class="text-xs font-weight-bold">${formatDate(user.created_at)}</span>
          </td>
        `;
        tbody.appendChild(row);
      });
      
      // Add highlight effect for new users
      highlightNewUsers();
    }
    
    // Function to update statistics
    function updateStatistics(stats) {
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
      
      // Update total users count
      updateCountWithAnimation('[data-stat="total-users"]', stats.total_users);
      
      // Update active users count
      updateCountWithAnimation('[data-stat="active-users"]', stats.active_users);
      
      // Update today's users count
      updateCountWithAnimation('[data-stat="today-users"]', stats.today_users);
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
    
    // Function to show notification
    function showNotification(message, type = 'info') {
      const notificationArea = document.getElementById('auto-refresh-notifications');
      if (!notificationArea) {
        return;
      }
      
      const notification = document.createElement('div');
      notification.className = `alert alert-${type} alert-dismissible fade show shadow-sm mb-2`;
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
    
    // Function to show new user notification
    function showNewUserNotification() {
      showNotification(`
        <i class="fas fa-user-plus me-2"></i>
        <strong>New User Registered!</strong>
        <p class="mb-0">A new user has been added to the system.</p>
      `, 'success');
    }
    
    // Function to highlight new users
    function highlightNewUsers() {
      const rows = document.querySelectorAll('#dashboard-users-table tbody tr');
      rows.forEach((row, index) => {
        if (index < 3) { // Highlight first 3 rows (newest users)
          row.style.backgroundColor = '#f8f9fa';
          row.style.transition = 'background-color 0.3s ease';
          
          setTimeout(() => {
            row.style.backgroundColor = '';
          }, 2000);
        }
      });
    }
    
    // Utility functions
    function escapeHtml(text) {
      const div = document.createElement('div');
      div.textContent = text;
      return div.innerHTML;
    }
    
    function formatDate(dateString) {
      const date = new Date(dateString);
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    }
    
    // Test function - can be called from browser console
    window.testAutoRefresh = function() {
      refreshDashboardData();
    };
    
    // Start auto-refresh when page loads
    document.addEventListener('DOMContentLoaded', function() {
      // Start auto-refresh every 3 seconds for faster updates
      refreshInterval = setInterval(refreshDashboardData, 3000);
      // Notifications refresh
      setInterval(refreshNotifications, 3000);
      refreshNotifications();
      // Feedback count refresh
      setInterval(refreshFeedbackCount, 3000);
      refreshFeedbackCount();
      // Installation count refresh
      setInterval(refreshInstallationCount, 3000);
      refreshInstallationCount();
      
      // Also refresh when user becomes active (tab becomes visible)
      document.addEventListener('visibilitychange', function() {
        if (!document.hidden) {
          refreshDashboardData(); // Refresh immediately when tab becomes visible
          refreshFeedbackCount(); // Refresh feedback count when tab becomes visible
          refreshInstallationCount(); // Refresh installation count when tab becomes visible
        }
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