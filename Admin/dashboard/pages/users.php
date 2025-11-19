<?php
require_once __DIR__ . '/../includes/auth_middleware.php';
require __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/message_handler.php';
requireAdminAuth();

// Handle legacy URL parameters
MessageHandler::handleLegacyParams();

// Handle user activation/deactivation
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['toggle_user_status'])) {
    $userId = $_POST['user_id'];
    $newStatus = $_POST['new_status'];
    
    try {
        $stmt = $pdo->prepare("UPDATE users SET is_active = ? WHERE user_id = ?");
        $stmt->execute([$newStatus, $userId]);
        
        if ($stmt->rowCount() > 0) {
            $message = $newStatus ? "User activated successfully!" : "User deactivated successfully!";
            MessageHandler::setSuccess($message, 'users');
        } else {
            MessageHandler::setInfo("No changes made.", 'users');
        }
    } catch (Exception $e) {
        MessageHandler::setError("Error updating user status: " . $e->getMessage(), 'users');
    }
}

// Get all users with their statistics
try {
    $usersQuery = "
        SELECT 
            u.user_id,
            u.user_name,
            u.email,
            u.created_at,
            u.is_active,
            u.last_login,
            u.login_count
        FROM users u
        ORDER BY u.created_at DESC
    ";
    
    $usersStmt = $pdo->query($usersQuery);
    $users = $usersStmt->fetchAll();
    
    // Get counts for summary cards
    $totalUsers = count($users);
    $activeUsers = count(array_filter($users, function($u) { return $u['is_active']; }));
    $inactiveUsers = $totalUsers - $activeUsers;
    
} catch (Exception $e) {
    $users = [];
    $totalUsers = 0;
    $activeUsers = 0;
    $inactiveUsers = 0;
    MessageHandler::setError("Database error: " . $e->getMessage(), 'users');
}

    $activePage = 'users';
    $includeSidebarFooter = false;
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <link rel="apple-touch-icon" sizes="76x76" href="../assets/img/apple-icon.png">
    <link rel="icon" type="image/png" href="../assets/img/favicon.png">
    <title>Users Management - SafePassage Admin</title>
    
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
    
    <!-- Custom CSS for users page -->
    <style>
        /* Ensure content is visible while CSS loads */
        body {
            opacity: 1;
            transition: opacity 0.3s ease;
        }
        
        /* Loading state */
        .loading {
            opacity: 0.7;
        }
        .user-status-toggle {
            cursor: pointer;
            transition: all 0.3s ease;
            border: none;
            background: none;
            padding: 0.5rem;
            border-radius: 0.5rem;
        }
        
        .user-status-toggle:hover {
            background-color: rgba(0,0,0,0.05);
            transform: scale(1.1);
        }
        
        .user-status-toggle.active {
            color: #17c1e8;
        }
        
        .user-status-toggle.inactive {
            color: #ea0606;
        }
        
        .search-box {
            background: rgba(255,255,255,0.8);
            backdrop-filter: blur(10px);
            border: 1px solid rgba(255,255,255,0.2);
        }
        
        .filter-buttons .btn {
            border-radius: 20px;
            padding: 0.5rem 1.5rem;
            font-weight: 500;
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
        
        .user-info {
            display: flex;
            align-items: center;
            gap: 0.75rem;
        }
        
        .user-details h6 {
            margin: 0;
            font-weight: 600;
            color: #344767;
        }
        
        .user-details small {
            color: #6c757d;
            font-size: 0.875rem;
        }
        
        .status-badge {
            padding: 0.375rem 0.75rem;
            border-radius: 0.5rem;
            font-size: 0.75rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        
                 
        
        .last-login {
            font-size: 0.875rem;
            color: #6c757d;
        }
        
                 .join-date {
             font-size: 0.875rem;
             color: #6c757d;
         }
         
         .stat-circle {
             width: 50px;
             height: 50px;
             border-radius: 50%;
             color: white;
             display: flex;
             align-items: center;
             justify-content: center;
             font-weight: bold;
             font-size: 1.1rem;
             margin: 0 auto;
             box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
         }
         
         .card {
             overflow: hidden;
         }
         
         .mask {
             position: absolute;
             top: 0;
             left: 0;
             width: 100%;
             height: 100%;
             z-index: 1;
         }
         
         .card-body.position-relative {
             z-index: 2;
         }
         
                 .activity-column {
            min-width: 180px;
        }
        
        .activity-column {
            min-width: 180px;
        }
    </style>
    
    <!-- Nepcha Analytics (nepcha.com) -->
    <!-- Nepcha is a easy-to-use web analytics. No cookies and fully compliant with GDPR, CCPA and PECR. -->
    <script defer data-site="YOUR_DOMAIN_HERE" src="https://api.nepcha.com/js/nepcha-analytics.js"></script>
</head>

<body class="g-sidenav-show  bg-gray-100">

    <?php  $activePage = 'users'; $includeSidebarFooter = false; include __DIR__ . '/partials/sidebar.php'; ?>

    <main class="main-content position-relative max-height-vh-100 h-100 border-radius-lg ">
        <!-- Navbar -->
        <nav class="navbar navbar-main navbar-expand-lg px-0 mx-4 shadow-none border-radius-xl" id="navbarBlur" navbar-scroll="true">
            <div class="container-fluid py-1 px-3">
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb bg-transparent mb-0 pb-0 pt-1 px-0 me-sm-6 me-5">
                        <li class="breadcrumb-item text-sm"><a class="opacity-5 text-dark" href="dashboard.php">Dashboard</a></li>
                        <li class="breadcrumb-item text-sm text-dark active" aria-current="page">Users Management</li>
                    </ol>
                    <h6 class="font-weight-bolder mb-0">Users Management</h6>
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
                                         <i class="fas fa-users text-dark text-gradient" style="font-size: 1.5rem;"></i>
                                     </div>
                                    <h5 class="text-white font-weight-bolder mb-0 mt-3" data-stat="total-users">
                                        <?php echo $totalUsers; ?>
                                    </h5>
                                    <span class="text-white text-sm">Total Users</span>
                                 </div>
                                 <div class="col-4">
                                     <p class="text-white text-sm text-end font-weight-bolder mt-auto mb-0">Active</p>
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
                                         <i class="fas fa-user-check text-dark text-gradient" style="font-size: 1.5rem;"></i>
                                     </div>
                                    <h5 class="text-white font-weight-bolder mb-0 mt-3" data-stat="active-users">
                                        <?php echo $activeUsers; ?>
                                    </h5>
                                    <span class="text-white text-sm">Active Users</span>
                                 </div>
                                 <div class="col-4">
                                     <p class="text-white text-sm text-end font-weight-bolder mt-auto mb-0">Online</p>
                                 </div>
                             </div>
                         </div>
                     </div>
                 </div>
                
                                 <div class="col-xl-3 col-sm-6 mb-4">
                     <div class="card">
                         <span class="mask bg-danger opacity-10 border-radius-lg"></span>
                         <div class="card-body p-3 position-relative">
                             <div class="row">
                                 <div class="col-8 text-start">
                                     <div class="icon icon-shape bg-white shadow text-center border-radius-2xl">
                                         <i class="fas fa-user-times text-dark text-gradient" style="font-size: 1.5rem;"></i>
                                     </div>
                                    <h5 class="text-white font-weight-bolder mb-0 mt-3" data-stat="blocked-users">
                                        <?php echo $inactiveUsers; ?>
                                    </h5>
                                    <span class="text-white text-sm">Blocked Users</span>
                                 </div>
                                 <div class="col-4">
                                     <p class="text-white text-sm text-end font-weight-bolder mt-auto mb-0">Offline</p>
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
                                         <i class="fas fa-user-plus text-dark text-gradient" style="font-size: 1.5rem;"></i>
                                     </div>
                                    <h5 class="text-white font-weight-bolder mb-0 mt-3" data-stat="new-this-month">
                                        <?php 
                                        $thisMonth = count(array_filter($users, function($u) {
                                            return date('Y-m', strtotime($u['created_at'])) === date('Y-m');
                                        }));
                                        echo $thisMonth;
                                        ?>
                                    </h5>
                                    <span class="text-white text-sm">New This Month</span>
                                 </div>
                                 <div class="col-4">
                                     <p class="text-white text-sm text-end font-weight-bolder mt-auto mb-0">Recent</p>
                                 </div>
                             </div>
                         </div>
                     </div>
                 </div>
            </div>
            
            <!-- Search and Filters -->
            <div class="card mb-4">
                <div class="card-body p-3">
                    <div class="row align-items-center">
                        <div class="col-md-6">
                            <div class="input-group search-box">
                                <span class="input-group-text"><i class="fas fa-search"></i></span>
                                <input type="text" class="form-control" id="searchUsers" placeholder="Search users by name or email...">
                            </div>
                        </div>
                        <div class="col-md-6 text-end">
                            <div class="filter-buttons">
                                <button class="btn btn-outline-success btn-sm" onclick="filterUsers('active', this)">Active</button>
                                <button class="btn btn-outline-danger btn-sm" onclick="filterUsers('inactive', this)">Blocked</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- Users Table -->
            <div class="card">
                <div class="card-header pb-0">
                    <h6>All Users</h6>
                    <p class="text-sm mb-0">
                        <i class="fa fa-user text-info" aria-hidden="true" ></i>
                        <span class="font-weight-bold ms-1">All</span> registrations 
                    </p>
                </div>
                <div class="card-body px-0 pb-2">
                    <div class="table-responsive">
                                                 <table class="table align-items-center mb-0" style="min-width: 800px;">
                                                         <thead>
                                <tr>
                                    <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7" style="width: 30%;">User</th>
                                    <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 ps-2" style="width: 35%;">Activity</th>
                                    <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 ps-2" style="width: 15%;">Status</th>
                                    <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 ps-2" style="width: 10%;">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <?php foreach ($users as $user): ?>
                                <tr class="user-item" 
                                    data-status="<?php echo $user['is_active'] ? 'active' : 'inactive'; ?>"
                                    data-name="<?php echo strtolower($user['user_name']); ?>"
                                    data-email="<?php echo strtolower($user['email']); ?>">
                                    
                                    <td>
                                        <div class="user-info">
                                            <div class="user-avatar bg-gradient-<?php echo $user['is_active'] ? 'primary' : 'secondary'; ?>">
                                                <?php echo strtoupper(substr($user['user_name'], 0, 1)); ?>
                                            </div>
                                            <div class="user-details">
                                                <h6><?php echo htmlspecialchars($user['user_name']); ?></h6>
                                                <small><?php echo htmlspecialchars($user['email']); ?></small>
                                            </div>
                                        </div>
                                    </td>
                                    
                                                                        <td class="activity-column">
                                         <div class="d-flex flex-column">
                                            <div class="last-login">
                                                <strong>Login Count:</strong> <?php echo $user['login_count'] ?? 0; ?>
                                            </div>
                                            <div class="last-login">
                                                <strong>Last Login:</strong> 
                                                <?php echo $user['last_login'] ? date('M d, Y', strtotime($user['last_login'])) : 'Never'; ?>
                                            </div>
                                            <div class="join-date">
                                                <strong>Joined:</strong> <?php echo date('M d, Y', strtotime($user['created_at'])); ?>
                                            </div>
                                        </div>
                                    </td>
                                    
                                    <td>
                                        <span class="status-badge bg-<?php echo $user['is_active'] ? 'success' : 'danger'; ?>">
                                            <?php echo $user['is_active'] ? 'Active' : 'Blocked'; ?>
                                        </span>
                                    </td>
                                    
                                    <td>
                                        <form method="POST" style="display: inline;">
                                            <input type="hidden" name="user_id" value="<?php echo $user['user_id']; ?>">
                                            <input type="hidden" name="new_status" value="<?php echo $user['is_active'] ? '0' : '1'; ?>">
                                            <button type="submit" name="toggle_user_status" 
                                                    class="user-status-toggle <?php echo $user['is_active'] ? 'active' : 'inactive'; ?>" 
                                                    title="<?php echo $user['is_active'] ? 'Deactivate User' : 'Activate User'; ?>">
                                                <i class="fas fa-toggle-<?php echo $user['is_active'] ? 'on' : 'off'; ?>" 
                                                   style="font-size: 1.25rem;"></i>
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                                <?php endforeach; ?>
                            </tbody>
                        </table>
                    </div>
                    
                    
            </div>
            
            <!-- No Users Message -->
            <?php if (empty($users)): ?>
            <div class="text-center py-5">
                <i class="fas fa-users fa-3x text-muted mb-3" style="width: 5px; height: 10px;"></i>
                <h5 class="text-muted">No users found</h5>
                <p class="text-muted">Users will appear here once they register.</p>
            </div>
            <?php endif; ?>
        </div>
    </main>
    
    <!-- Core JS Files -->
    <script src="../assets/js/core/popper.min.js?v=1.1.0"></script>
    <script src="../assets/js/core/bootstrap.min.js?v=1.1.0"></script>
    <script src="../assets/js/plugins/perfect-scrollbar.min.js?v=1.1.0"></script>
    <script src="../assets/js/plugins/smooth-scrollbar.min.js?v=1.1.0"></script>
    <script src="../assets/js/plugins/buttons.js?v=1.1.0"></script>
    
    <script>
        // Persisted UI state
        let currentFilter = 'all';
        let currentSearch = '';
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
    
    <!-- Users Management JavaScript -->
    <script>
        // Search functionality
        document.getElementById('searchUsers').addEventListener('input', function(e) {
            const searchTerm = e.target.value.toLowerCase();
            currentSearch = searchTerm;
            const userItems = document.querySelectorAll('.user-item');
            
            userItems.forEach(item => {
                const name = item.dataset.name;
                const email = item.dataset.email;
                
                if (name.includes(searchTerm) || email.includes(searchTerm)) {
                    item.style.display = 'table-row';
                } else {
                    item.style.display = 'none';
                }
            });
        });
        
        // Filter functionality
        function filterUsers(status, targetBtn) {
            currentFilter = status;
            const userItems = document.querySelectorAll('.user-item');
            
            userItems.forEach(item => {
                if (status === 'all' || item.dataset.status === status) {
                    item.style.display = 'table-row';
                } else {
                    item.style.display = 'none';
                }
            });
            
            // Update active filter button
            document.querySelectorAll('.filter-buttons .btn').forEach(btn => {
                btn.classList.remove('btn-primary');
                btn.classList.add('btn-outline-primary');
            });
            if (targetBtn) {
                targetBtn.classList.remove('btn-outline-primary');
                targetBtn.classList.add('btn-primary');
            }
        }
        
        // Auto-hide alerts after 5 seconds
        setTimeout(function() {
            const alerts = document.querySelectorAll('.alert');
            alerts.forEach(alert => {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            });
        }, 5000);
    </script>
    
    <!-- Auto-refresh functionality for users page -->
    <script>
        // Auto-refresh users data every 3 seconds
        let refreshInterval;
        let lastUserCount = <?php echo count($users); ?>;
        
        // Function to refresh users data
        async function refreshUsersData() {
            try {
                const response = await fetch('../api/get_users_page_data.php');
                const data = await response.json();
                
                if (data.success) {
                    // Check if user count changed
                    if (data.stats.total_users !== lastUserCount) {
                        // Show notification for new users
                        if (data.stats.total_users > lastUserCount) {
                            showNewUserNotification();
                        }
                        
                        lastUserCount = data.stats.total_users;
                    }
                    
                    // Update user table
                    updateUsersTable(data.users);
                    
                    // Update statistics
                    updateStatistics(data.stats);
                }
            } catch (error) {
                // Silent error handling - functionality continues
            }
        }
        
        // Function to update users table
        function updateUsersTable(users) {
            const tbody = document.querySelector('tbody');
            if (!tbody) {
                return;
            }
            
            // Clear existing rows
            tbody.innerHTML = '';
            
            if (users.length === 0) {
                tbody.innerHTML = '<tr><td colspan="4" class="text-center text-sm">No users found</td></tr>';
                return;
            }
            
            // Add new rows
            users.forEach((user, index) => {
                const row = document.createElement('tr');
                row.className = 'user-item';
                row.dataset.status = user.is_active ? 'active' : 'inactive';
                row.dataset.name = user.user_name.toLowerCase();
                row.dataset.email = user.email.toLowerCase();
                
                row.innerHTML = `
                    <td>
                        <div class="user-info">
                            <div class="user-avatar bg-gradient-${user.is_active ? 'primary' : 'secondary'}">
                                ${user.user_name.charAt(0).toUpperCase()}
                            </div>
                            <div class="user-details">
                                <h6>${escapeHtml(user.user_name)}</h6>
                                <small>${escapeHtml(user.email)}</small>
                            </div>
                        </div>
                    </td>
                    <td class="activity-column">
                        <div class="d-flex flex-column">
                            <div class="last-login">
                                <strong>Login Count:</strong> ${user.login_count || 0}
                            </div>
                            <div class="last-login">
                                <strong>Last Login:</strong> 
                                ${user.last_login ? formatDate(user.last_login) : 'Never'}
                            </div>
                            <div class="join-date">
                                <strong>Joined:</strong> ${formatDate(user.created_at)}
                            </div>
                        </div>
                    </td>
                    <td>
                        <span class="status-badge bg-${user.is_active ? 'success' : 'danger'}">
                            ${user.is_active ? 'Active' : 'Blocked'}
                        </span>
                    </td>
                    <td>
                        <form method="POST" style="display: inline;">
                            <input type="hidden" name="user_id" value="${user.user_id}">
                            <input type="hidden" name="new_status" value="${user.is_active ? '0' : '1'}">
                            <button type="submit" name="toggle_user_status" 
                                    class="user-status-toggle ${user.is_active ? 'active' : 'inactive'}" 
                                    title="${user.is_active ? 'Deactivate User' : 'Activate User'}">
                                <i class="fas fa-toggle-${user.is_active ? 'on' : 'off'}" 
                                   style="font-size: 1.25rem;"></i>
                            </button>
                        </form>
                    </td>
                `;
                tbody.appendChild(row);
            });
            
            // Add highlight effect for new users
            highlightNewUsers();

            // Re-apply current search and filter after refresh
            if (currentSearch) {
                const searchInput = document.getElementById('searchUsers');
                if (searchInput && searchInput.value.toLowerCase() !== currentSearch) {
                    // keep UI in sync; do not fire event
                    searchInput.value = currentSearch;
                }
                const rows = document.querySelectorAll('.user-item');
                rows.forEach(item => {
                    const name = item.dataset.name;
                    const email = item.dataset.email;
                    if (name.includes(currentSearch) || email.includes(currentSearch)) {
                        // keep, will be further filtered by status below
                    } else {
                        item.style.display = 'none';
                    }
                });
            }
            if (currentFilter && currentFilter !== 'all') {
                // Apply filter without changing button styles
                const rows = document.querySelectorAll('.user-item');
                rows.forEach(item => {
                    if (item.dataset.status === currentFilter) {
                        if (item.style.display !== 'none') {
                            item.style.display = 'table-row';
                        }
                    } else {
                        item.style.display = 'none';
                    }
                });
            }
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
        
        // Function to update statistics
        function updateStatistics(stats) {
            // Update total users count
            updateCountWithAnimation('[data-stat="total-users"]', stats.total_users);
            
            // Update active users count
            updateCountWithAnimation('[data-stat="active-users"]', stats.active_users);
            
            // Update inactive users count
            updateCountWithAnimation('[data-stat="blocked-users"]', stats.inactive_users);
            
            // Update new this month count
            if (stats.new_this_month !== undefined) {
                updateCountWithAnimation('[data-stat="new-this-month"]', stats.new_this_month);
            }
        }
        
        // Function to show new user notification
        function showNewUserNotification() {
            const notification = document.createElement('div');
            notification.className = 'alert alert-success alert-dismissible fade show mb-2';
            notification.innerHTML = `
                <i class="fas fa-user-plus me-2"></i>
                <strong>New User Registered!</strong>
                <p class="mb-0">A new user has been added to the system.</p>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            `;
            
            const notificationArea = document.getElementById('auto-refresh-notifications');
            if (notificationArea) {
                notificationArea.appendChild(notification);
                
                setTimeout(() => {
                    if (notification.parentNode) {
                        notification.remove();
                    }
                }, 5000);
            }
        }
        
        // Function to highlight new users
        function highlightNewUsers() {
            const rows = document.querySelectorAll('tbody tr');
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
                month: 'short',
                day: 'numeric'
            });
        }
        
        // Test function - can be called from browser console
        window.testUsersAutoRefresh = function() {
            refreshUsersData();
        };
        
        // Start auto-refresh when page loads
        document.addEventListener('DOMContentLoaded', function() {
            // Start auto-refresh every 3 seconds
            refreshInterval = setInterval(refreshUsersData, 3000);
            
            // Also refresh when user becomes active (tab becomes visible)
            document.addEventListener('visibilitychange', function() {
                if (!document.hidden) {
                    refreshUsersData();
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
