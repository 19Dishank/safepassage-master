<?php
require_once __DIR__ . '/../includes/auth_middleware.php';
require __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/message_handler.php';
requireAdminAuth();

// Handle legacy URL parameters
MessageHandler::handleLegacyParams();

// Get installation data (dummy data for now)
try {
    // Load initial data from API
    $apiUrl = __DIR__ . '/../api/get_installations_data.php';
    $apiResponse = file_get_contents($apiUrl);
    $apiData = json_decode($apiResponse, true);
    
    if ($apiData && $apiData['success']) {
        $totalInstallations = $apiData['stats']['total_installations'];
        $todayInstallations = $apiData['stats']['today_installations'];
        $thisWeekInstallations = $apiData['stats']['this_week_installations'];
        $thisMonthInstallations = $apiData['stats']['this_month_installations'];
    } else {
        // Fallback values
        $totalInstallations = 1247;
        $todayInstallations = 18;
        $thisWeekInstallations = 87;
        $thisMonthInstallations = 245;
    }
} catch (Exception $e) {
    $totalInstallations = 1247;
    $todayInstallations = 18;
    $thisWeekInstallations = 87;
    $thisMonthInstallations = 245;
}

$activePage = 'installations';
$includeSidebarFooter = false;
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <link rel="apple-touch-icon" sizes="76x76" href="../assets/img/apple-icon.png">
    <link rel="icon" type="image/png" href="../assets/img/favicon.png">
    <title>Installations - SafePassage Admin</title>
    
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
    
    <!-- Chart.js for analytics -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    
    <!-- Custom CSS for installations page -->
    <style>
        body {
            opacity: 1;
            transition: opacity 0.3s ease;
        }
        
        .chart-container {
            position: relative;
            overflow: hidden;
            height: 400px;
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
        
        .pulse-animation {
            animation: pulse 0.5s ease-in-out;
        }
        
        @keyframes pulse {
            0%, 100% { transform: scale(1); }
            50% { transform: scale(1.05); }
        }
    </style>
</head>

<body class="g-sidenav-show  bg-gray-100">

    <?php $activePage = 'installations'; $includeSidebarFooter = false; include __DIR__ . '/partials/sidebar.php'; ?>

    <main class="main-content position-relative max-height-vh-100 h-100 border-radius-lg ">
        <!-- Navbar -->
        <nav class="navbar navbar-main navbar-expand-lg px-0 mx-4 shadow-none border-radius-xl" id="navbarBlur" navbar-scroll="true">
            <div class="container-fluid py-1 px-3">
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb bg-transparent mb-0 pb-0 pt-1 px-0 me-sm-6 me-5">
                        <li class="breadcrumb-item text-sm"><a class="opacity-5 text-dark" href="dashboard.php">Dashboard</a></li>
                        <li class="breadcrumb-item text-sm text-dark active" aria-current="page">Installations</li>
                    </ol>
                    <h6 class="font-weight-bolder mb-0">App Installations</h6>
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
                <div class="col-xl-3 col-sm-6 mb-4">
                    <div class="card">
                        <span class="mask bg-primary opacity-10 border-radius-lg"></span>
                        <div class="card-body p-3 position-relative">
                            <div class="row">
                                <div class="col-8 text-start">
                                    <div class="icon icon-shape bg-white shadow text-center border-radius-2xl">
                                        <i class="fas fa-download text-dark text-gradient" style="font-size: 1.5rem;"></i>
                                    </div>
                                    <h5 class="text-white font-weight-bolder mb-0 mt-3" data-stat="total-installations">
                                        <?php echo $totalInstallations; ?>
                                    </h5>
                                    <span class="text-white text-sm">Total Installations</span>
                                </div>
                                <div class="col-4">
                                    <p class="text-white text-sm text-end font-weight-bolder mt-auto mb-0">All</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="col-xl-3 col-sm-6 mb-4">
                    <div class="card">
                        <span class="mask bg-success opacity-10 border-radius-lg"></span>
                        <div class="card-body p-3 position-relative">
                            <div class="row">
                                <div class="col-8 text-start">
                                    <div class="icon icon-shape bg-white shadow text-center border-radius-2xl">
                                        <i class="fas fa-calendar-day text-dark text-gradient" style="font-size: 1.5rem;"></i>
                                    </div>
                                    <h5 class="text-white font-weight-bolder mb-0 mt-3" data-stat="today-installations">
                                        <?php echo $todayInstallations; ?>
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
                
                <div class="col-xl-3 col-sm-6 mb-4">
                    <div class="card">
                        <span class="mask bg-info opacity-10 border-radius-lg"></span>
                        <div class="card-body p-3 position-relative">
                            <div class="row">
                                <div class="col-8 text-start">
                                    <div class="icon icon-shape bg-white shadow text-center border-radius-2xl">
                                        <i class="fas fa-calendar-week text-dark text-gradient" style="font-size: 1.5rem;"></i>
                                    </div>
                                    <h5 class="text-white font-weight-bolder mb-0 mt-3" data-stat="week-installations">
                                        <?php echo $thisWeekInstallations; ?>
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
                
                <div class="col-xl-3 col-sm-6 mb-4">
                    <div class="card">
                        <span class="mask bg-warning opacity-10 border-radius-lg"></span>
                        <div class="card-body p-3 position-relative">
                            <div class="row">
                                <div class="col-8 text-start">
                                    <div class="icon icon-shape bg-white shadow text-center border-radius-2xl">
                                        <i class="fas fa-calendar-alt text-dark text-gradient" style="font-size: 1.5rem;"></i>
                                    </div>
                                    <h5 class="text-white font-weight-bolder mb-0 mt-3" data-stat="month-installations">
                                        <?php echo $thisMonthInstallations; ?>
                                    </h5>
                                    <span class="text-white text-sm">This Month</span>
                                </div>
                                <div class="col-4">
                                    <p class="text-white text-sm text-end font-weight-bolder mt-auto mb-0">Monthly</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- Chart Section -->
            <div class="row mb-4">
                <div class="col-12">
                    <div class="card">
                        <div class="card-header pb-0">
                            <div class="d-flex justify-content-between align-items-center">
                                <div>
                                    <h6>Installation Trends</h6>
                                    <p class="text-sm mb-0">
                                        <i class="fa fa-chart-line text-info" aria-hidden="true"></i>
                                        <span class="font-weight-bold ms-1">Installation</span> analytics
                                    </p>
                                </div>
                                <div class="btn-group" role="group">
                                    <button type="button" class="btn btn-sm btn-outline-primary active" onclick="switchChartPeriod('daily', this)">Daily</button>
                                    <button type="button" class="btn btn-sm btn-outline-primary" onclick="switchChartPeriod('weekly', this)">Weekly</button>
                                    <button type="button" class="btn btn-sm btn-outline-primary" onclick="switchChartPeriod('monthly', this)">Monthly</button>
                                </div>
                            </div>
                        </div>
                        <div class="card-body">
                            <div class="chart-container">
                                <canvas id="installationsChart"></canvas>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- Info Card -->
            <div class="row">
                <div class="col-12">
                    <div class="card">
                        <div class="card-body">
                            <div class="alert alert-info mb-0">
                                <i class="fas fa-info-circle me-2"></i>
                                <strong>Note:</strong> Currently showing dummy data. When the app is deployed, this page will display real-time installation statistics from the database.
                            </div>
                        </div>
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
    
    <!-- Installations Management JavaScript -->
    <script>
        let installationsChart = null;
        let currentPeriod = 'daily';
        let chartData = {
            daily: [],
            weekly: [],
            monthly: []
        };
        
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
        
        // Function to initialize chart
        function initChart() {
            const ctx = document.getElementById('installationsChart');
            if (!ctx) return;
            
            const data = chartData[currentPeriod] || [];
            
            installationsChart = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: data.map(item => item.label),
                    datasets: [{
                        label: 'Installations',
                        data: data.map(item => item.count),
                        borderColor: 'rgb(23, 193, 232)',
                        backgroundColor: 'rgba(23, 193, 232, 0.1)',
                        borderWidth: 3,
                        fill: true,
                        tension: 0.4,
                        pointRadius: 4,
                        pointHoverRadius: 6,
                        pointBackgroundColor: 'rgb(23, 193, 232)',
                        pointBorderColor: '#fff',
                        pointBorderWidth: 2
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
                            backgroundColor: 'rgba(0, 0, 0, 0.8)',
                            padding: 12,
                            titleFont: {
                                size: 14,
                                weight: 'bold'
                            },
                            bodyFont: {
                                size: 13
                            },
                            callbacks: {
                                label: function(context) {
                                    return 'Installations: ' + context.parsed.y;
                                }
                            }
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            grid: {
                                color: 'rgba(0, 0, 0, 0.05)'
                            },
                            ticks: {
                                stepSize: 10
                            }
                        },
                        x: {
                            grid: {
                                display: false
                            }
                        }
                    }
                }
            });
        }
        
        // Function to switch chart period
        function switchChartPeriod(period, button) {
            currentPeriod = period;
            
            // Update button states
            document.querySelectorAll('.btn-group .btn').forEach(btn => {
                btn.classList.remove('active');
            });
            button.classList.add('active');
            
            // Update chart
            if (installationsChart) {
                const data = chartData[period] || [];
                installationsChart.data.labels = data.map(item => item.label);
                installationsChart.data.datasets[0].data = data.map(item => item.count);
                installationsChart.update();
            }
        }
        
        // Track last chart data to detect changes
        let lastChartDataHash = '';
        
        // Function to hash chart data for comparison
        function hashChartData(data) {
            return JSON.stringify(data);
        }
        
        // Function to refresh installations data
        async function refreshInstallationsData() {
            try {
                const response = await fetch('../api/get_installations_data.php', {
                    cache: 'no-store'
                });
                const data = await response.json();
                
                if (data.success) {
                    // Always update summary cards with animation (total increases, others may change)
                    updateCountWithAnimation('[data-stat="total-installations"]', data.stats.total_installations);
                    updateCountWithAnimation('[data-stat="today-installations"]', data.stats.today_installations);
                    updateCountWithAnimation('[data-stat="week-installations"]', data.stats.this_week_installations);
                    updateCountWithAnimation('[data-stat="month-installations"]', data.stats.this_month_installations);
                    
                    // Check if chart data has changed (every 3 minutes)
                    const currentChartDataHash = hashChartData(data.chart_data);
                    if (currentChartDataHash !== lastChartDataHash) {
                        // Chart data has changed, update it
                        chartData = data.chart_data;
                        lastChartDataHash = currentChartDataHash;
                        
                        // Update chart if it exists
                        if (installationsChart) {
                            const currentData = chartData[currentPeriod] || [];
                            installationsChart.data.labels = currentData.map(item => item.label);
                            installationsChart.data.datasets[0].data = currentData.map(item => item.count);
                            installationsChart.update(); // Animated update when data changes
                        }
                    }
                }
            } catch (error) {
                console.error('Error refreshing installations data:', error);
            }
        }
        
        // Start auto-refresh when page loads
        document.addEventListener('DOMContentLoaded', function() {
            // Initialize chart with initial data
            refreshInstallationsData().then(() => {
                initChart();
            });
            
            // Start auto-refresh every 3 seconds
            setInterval(refreshInstallationsData, 3000);
            
            // Also refresh when user becomes active (tab becomes visible)
            document.addEventListener('visibilitychange', function() {
                if (!document.hidden) {
                    refreshInstallationsData();
                }
            });
        });
        
        // Auto-hide alerts after 5 seconds
        setTimeout(function() {
            const alerts = document.querySelectorAll('.alert');
            alerts.forEach(alert => {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            });
        }, 5000);
    </script>
</body>
</html>

