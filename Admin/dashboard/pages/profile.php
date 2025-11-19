
<!DOCTYPE html>
<html lang="en">
<!-- Users Active,Click Events,Purchases,Likes icons are mot visible, add online library and make visible -->
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
  <link rel="apple-touch-icon" sizes="76x76" href="../assets/img/apple-icon.png">
  <link rel="icon" type="image/png" href="../assets/img/favicon.png">
  <!-- Latest Font Awesome -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.6.0/css/all.min.css">

  <title>
   Admin Profile
  </title>
  <!--     Fonts and icons     -->
  <link href="https://fonts.googleapis.com/css?family=Inter:300,400,500,600,700,800" rel="stylesheet" />
  <!-- Nucleo Icons -->
  <link href="https://demos.creative-tim.com/soft-ui-dashboard/assets/css/nucleo-icons.css" rel="stylesheet" />
  <link href="https://demos.creative-tim.com/soft-ui-dashboard/assets/css/nucleo-svg.css" rel="stylesheet" />
  <!-- Font Awesome Icons -->
  <script src="https://kit.fontawesome.com/42d5adcbca.js" crossorigin="anonymous"></script>
  <!-- CSS Files -->
  <link id="pagestyle" href="../assets/css/soft-ui-dashboard.css?v=1.1.0" rel="stylesheet" />
  <!-- Nepcha Analytics (nepcha.com) -->
  <!-- Nepcha is a easy-to-use web analytics. No cookies and fully compliant with GDPR, CCPA and PECR. -->
  <script defer data-site="YOUR_DOMAIN_HERE" src="https://api.nepcha.com/js/nepcha-analytics.js"></script>
</head>

<body class="g-sidenav-show bg-gray-100">
  <?php
    require_once __DIR__ . '/../includes/auth_middleware.php';
    require __DIR__ . '/../config/db.php';
    require_once __DIR__ . '/../includes/message_handler.php';
    requireAdminAuth();
    
    // Handle legacy URL parameters
    MessageHandler::handleLegacyParams();
    
    // Get current admin information
    $currentAdmin = getCurrentAdmin();
    
    // Use display name if available, otherwise fall back to username
    $displayName = $currentAdmin['name'];
    
    $activePage = 'profile'; 
    $includeSidebarFooter = true; 
    include __DIR__ . '/partials/sidebar.php';
  ?>
  <div class="main-content position-relative max-height-vh-100 h-100">
   
    <div class="container-fluid">
      <!-- Status Messages -->
      <?php MessageHandler::displayMessage(); ?>
      
      <div class="page-header min-height-250 border-radius-lg mt-4 d-flex flex-column justify-content-end">
        <span class="mask bg-primary opacity-9"></span>
        <div class="w-100 position-relative p-3">
            <div class="d-flex align-items-center">
              <div class="avatar avatar-xl position-relative me-3">
                <div class="w-100 border-radius-lg shadow-sm bg-white d-flex align-items-center justify-content-center" style="width: 80px; height: 80px;">
                  <i class="fas fa-user-shield text-primary" style="font-size: 2.5rem;"></i>
                </div>
              </div>
              <div>
              <h5 class="mb-1 text-white font-weight-bolder"><?php echo htmlspecialchars($displayName); ?></h5>
              <p class="mb-0 text-white text-sm">System Administrator</p>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="container-fluid py-4">
      <div class="row">
        <div class="col-12 col-xl-6">
          <div class="card h-100">
            <div class="card-header pb-0 p-3">
              <h6 class="mb-0">Profile Information</h6>
            </div>
            <div class="card-body p-3">
              <div class="d-flex align-items-center mb-3">
                <div class="avatar avatar-xl me-3">
                  <div class="w-100 border-radius-lg shadow-sm bg-gradient-primary d-flex align-items-center justify-content-center" style="width: 80px; height: 80px;">
                    <i class="fas fa-user-shield text-white" style="font-size: 2.5rem;"></i>
                  </div>
                </div>
                <div>
                  <h6 class="mb-1"><?php echo htmlspecialchars($displayName); ?></h6>
                  <p class="text-sm mb-0">System Administrator</p>
                </div>
              </div>
              
              <form method="post" action="update_profile.php">
                <div class="row">
                <div class="col-12">
                  <div class="form-group">
                    <label class="form-control-label">Display Name</label>
                    <input class="form-control" type="text" id="displayName" name="displayName" value="<?php echo htmlspecialchars($displayName); ?>">
                    <small class="form-text text-muted">This is the name that will be displayed throughout the admin panel.</small>
                  </div>
                </div>
                <div class="col-12">
                  <div class="form-group">
                    <label class="form-control-label">Login Username</label>
                    <input class="form-control" type="text" value="<?php echo htmlspecialchars($currentAdmin['username']); ?>" readonly>
                    <small class="form-text text-muted">This is your login username and cannot be changed.</small>
                  </div>
                </div>
                <div class="col-12">
                  <div class="form-group">
                    <label class="form-control-label">Role</label>
                    <input class="form-control" type="text" value="System Administrator" readonly>
                  </div>
                </div>
                </div>
                
                <div class="row mt-3">
                  <div class="col-12">
                    <button type="submit" class="btn btn-primary">
                      <i class="fas fa-save me-2"></i>
                      Save Changes
                    </button>
                  </div>
                </div>
              </form>
            </div>
          </div>
        </div>
        
        <div class="col-12 col-xl-6">
          <div class="card h-100">
            <div class="card-header pb-0 p-3">
              <h6 class="mb-0">Account Actions</h6>
            </div>
            <div class="card-body p-3">
              <div class="row">
                <div class="col-12">
                  <div class="alert alert-info" role="alert">
                    <i class="fas fa-info-circle me-2"></i>
                    <strong>Welcome to SafePassage Admin Panel!</strong><br>
                    You have full administrative access to manage users, view analytics, and control system settings.
                  </div>
                </div>
                <div class="col-12">
                  <div class="d-grid gap-2">
                    <a href="logout.php" class="btn btn-danger btn-lg">
                      <i class="fas fa-sign-out-alt me-2"></i>
                      Logout
                    </a>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
        
      </div>
      
    </div>
  </div>
  
  <!--   Core JS Files   -->
  <script src="../assets/js/core/popper.min.js"></script>
  <script src="../assets/js/core/bootstrap.min.js"></script>
  <script src="../assets/js/plugins/perfect-scrollbar.min.js"></script>
  <script src="../assets/js/plugins/smooth-scrollbar.min.js"></script>
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
  

</body>

</html>