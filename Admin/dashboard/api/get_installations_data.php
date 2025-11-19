<?php
/**
 * API endpoint to get installation data for installations.php page
 * Used for auto-refreshing installation list without page reload
 * Currently returns dummy data since app is not deployed
 * 
 * Logic:
 * - Total installations only increases (never decreases)
 * - Daily/Weekly/Monthly data changes randomly every 3 minutes
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');
header('Access-Control-Allow-Headers: Content-Type');

require_once __DIR__ . '/../config/db.php';

// File to store installation state
$stateFile = __DIR__ . '/installations_state.json';

// Initialize or load state
function loadState($file) {
    if (file_exists($file)) {
        $data = json_decode(file_get_contents($file), true);
        if ($data) {
            return $data;
        }
    }
    // Initial state
    return [
        'total_installations' => 1247,
        'last_total_update' => time(),
        'last_chart_update' => 0,
        'daily_data' => null,
        'weekly_data' => null,
        'monthly_data' => null
    ];
}

function saveState($file, $state) {
    file_put_contents($file, json_encode($state, JSON_PRETTY_PRINT));
}

try {
    $state = loadState($stateFile);
    $currentTime = time();
    
    // Update total installations - only increase, add 1-3 every 30 seconds
    $timeSinceLastTotalUpdate = $currentTime - $state['last_total_update'];
    if ($timeSinceLastTotalUpdate >= 30) {
        $increment = rand(1, 3); // Add 1-3 installations
        $state['total_installations'] += $increment;
        $state['last_total_update'] = $currentTime;
    }
    
    // Update chart data every 3 minutes (180 seconds)
    $timeSinceLastChartUpdate = $currentTime - $state['last_chart_update'];
    $shouldUpdateChart = $timeSinceLastChartUpdate >= 180;
    
    if ($shouldUpdateChart || $state['daily_data'] === null) {
        // Generate new random daily data for last 30 days
        $dailyData = [];
        for ($i = 29; $i >= 0; $i--) {
            $date = date('Y-m-d', strtotime("-$i days"));
            $dayLabel = date('M d', strtotime("-$i days"));
            $count = rand(8, 35); // Random installations per day
            
            $dailyData[] = [
                'label' => $dayLabel,
                'count' => $count,
                'date' => $date
            ];
        }
        
        // Generate new random weekly data for last 12 weeks
        $weeklyData = [];
        for ($i = 11; $i >= 0; $i--) {
            $weekStart = date('Y-m-d', strtotime("monday this week -$i weeks"));
            $weekLabel = date('M d', strtotime("monday this week -$i weeks"));
            $count = rand(50, 150); // Random installations per week
            
            $weeklyData[] = [
                'label' => $weekLabel,
                'count' => $count,
                'week_start' => $weekStart
            ];
        }
        
        // Generate new random monthly data for last 12 months
        $monthlyData = [];
        for ($i = 11; $i >= 0; $i--) {
            $month = date('Y-m', strtotime("-$i months"));
            $monthName = date('M Y', strtotime("-$i months"));
            $count = rand(200, 450); // Random installations per month
            
            $monthlyData[] = [
                'label' => $monthName,
                'count' => $count,
                'month' => $month
            ];
        }
        
        $state['daily_data'] = $dailyData;
        $state['weekly_data'] = $weeklyData;
        $state['monthly_data'] = $monthlyData;
        $state['last_chart_update'] = $currentTime;
    }
    
    // Calculate today, this week, and this month from daily data
    $todayDate = date('Y-m-d');
    $todayInstallations = 0;
    foreach ($state['daily_data'] as $day) {
        if ($day['date'] === $todayDate) {
            $todayInstallations = $day['count'];
            break;
        }
    }
    
    // Calculate this week from daily data
    $weekStart = date('Y-m-d', strtotime('monday this week'));
    $thisWeekInstallations = 0;
    foreach ($state['daily_data'] as $day) {
        if ($day['date'] >= $weekStart) {
            $thisWeekInstallations += $day['count'];
        }
    }
    
    // Calculate this month from daily data
    $monthStart = date('Y-m-01');
    $thisMonthInstallations = 0;
    foreach ($state['daily_data'] as $day) {
        if ($day['date'] >= $monthStart) {
            $thisMonthInstallations += $day['count'];
        }
    }
    
    // Save updated state
    saveState($stateFile, $state);
    
    // Use stored chart data
    $dailyData = $state['daily_data'];
    $weeklyData = $state['weekly_data'];
    $monthlyData = $state['monthly_data'];
    
    $response = [
        'success' => true,
        'stats' => [
            'total_installations' => $state['total_installations'],
            'today_installations' => $todayInstallations,
            'this_week_installations' => $thisWeekInstallations,
            'this_month_installations' => $thisMonthInstallations
        ],
        'chart_data' => [
            'daily' => $dailyData,
            'weekly' => $weeklyData,
            'monthly' => $monthlyData
        ],
        'timestamp' => date('Y-m-d H:i:s'),
        'next_chart_update' => $state['last_chart_update'] + 180 - $currentTime // Seconds until next update
    ];
    
    echo json_encode($response);
    
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'error' => 'Database error: ' . $e->getMessage()
    ]);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'error' => 'Server error: ' . $e->getMessage()
    ]);
}
?>

