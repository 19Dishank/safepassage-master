<?php
require __DIR__ . '/config/db.php';

try {
    // Test monthly data generation
    echo "<h2>Testing Chart Data Generation</h2>";
    
    $monthlyData = [];
    $weeklyData = [];
    $dailyData = [];
    
         // Monthly data for last 6 months
     echo "<h3>Monthly Data:</h3>";
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
        
        echo "Month: $monthName ($monthStart to $monthEnd) - Count: $count<br>";
    }
    
    // Weekly data for last 8 weeks
    echo "<h3>Weekly Data:</h3>";
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
        
        echo "Week: $weekLabel ($weekStart to $weekEnd) - Count: $count<br>";
    }
    
    // Daily data for last 30 days
    echo "<h3>Daily Data (Last 10 days):</h3>";
    for ($i = 9; $i >= 0; $i--) {
        $date = date('Y-m-d', strtotime("-$i days"));
        $dayLabel = date('M d', strtotime("-$i days"));
        
        $dailyStmt = $pdo->prepare("SELECT COUNT(*) as count FROM users WHERE DATE(created_at) = ?");
        $dailyStmt->execute([$date]);
        $count = $dailyStmt->fetch()['count'];
        
        $dailyData[] = [
            'label' => $dayLabel,
            'count' => $count
        ];
        
        echo "Day: $dayLabel ($date) - Count: $count<br>";
    }
    
    echo "<h3>JSON Output:</h3>";
    echo "<pre>";
    echo "Monthly: " . json_encode($monthlyData, JSON_PRETTY_PRINT) . "\n\n";
    echo "Weekly: " . json_encode($weeklyData, JSON_PRETTY_PRINT) . "\n\n";
    echo "Daily: " . json_encode($dailyData, JSON_PRETTY_PRINT);
    echo "</pre>";
    
} catch (Exception $e) {
    echo "Error: " . $e->getMessage();
}
?>
