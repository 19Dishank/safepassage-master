<?php
require_once 'config/db.php';

echo "<h2>Update Admin Table and Credentials</h2>";

try {
    // First, let's see the current admin table structure
    echo "<h3>Current Admin Table Structure:</h3>";
    $stmt = $pdo->query("DESCRIBE admins");
    $columns = $stmt->fetchAll();
    
    echo "<table border='1' style='border-collapse: collapse; width: 100%; margin: 10px 0;'>";
    echo "<tr><th>Field</th><th>Type</th><th>Null</th><th>Key</th><th>Default</th><th>Extra</th></tr>";
    foreach ($columns as $column) {
        echo "<tr>";
        echo "<td>{$column['Field']}</td>";
        echo "<td>{$column['Type']}</td>";
        echo "<td>{$column['Null']}</td>";
        echo "<td>{$column['Key']}</td>";
        echo "<td>{$column['Default']}</td>";
        echo "<td>{$column['Extra']}</td>";
        echo "</tr>";
    }
    echo "</table>";
    
    // Check if 'name' column already exists
    $nameColumnExists = false;
    foreach ($columns as $column) {
        if ($column['Field'] === 'name') {
            $nameColumnExists = true;
            break;
        }
    }
    
    // Add 'name' column if it doesn't exist
    if (!$nameColumnExists) {
        echo "<h3>Adding 'name' column to admins table...</h3>";
        $stmt = $pdo->prepare("ALTER TABLE admins ADD COLUMN name VARCHAR(255) AFTER username");
        $stmt->execute();
        echo "<p style='color: green;'><strong>✅ Success!</strong> 'name' column added to admins table</p>";
    } else {
        echo "<p style='color: orange;'>'name' column already exists in admins table</p>";
    }
    
    // Get current admin information
    echo "<h3>Current Admin Information:</h3>";
    $stmt = $pdo->query("SELECT id, username, name, created_at FROM admins ORDER BY id");
    $currentAdmin = $stmt->fetch();
    
    if ($currentAdmin) {
        echo "<p><strong>Current Username:</strong> " . htmlspecialchars($currentAdmin['username']) . "</p>";
        echo "<p><strong>Current Name:</strong> " . (empty($currentAdmin['name']) ? 'NULL' : htmlspecialchars($currentAdmin['name'])) . "</p>";
        echo "<p><strong>Admin ID:</strong> " . $currentAdmin['id'] . "</p>";
        echo "<p><strong>Created:</strong> " . $currentAdmin['created_at'] . "</p>";
    } else {
        echo "<p>No admin found in database.</p>";
        exit;
    }
    
    // Update admin name and password
    echo "<h3>Updating Admin Information...</h3>";
    
    // Hash the new password (admin/admin)
    $newPasswordHash = password_hash('admin', PASSWORD_DEFAULT);
    
    // Update admin with name and new password
    $stmt = $pdo->prepare("UPDATE admins SET name = ?, password_hash = ? WHERE id = ?");
    $stmt->execute(['Dishank Patel', $newPasswordHash, $currentAdmin['id']]);
    
    if ($stmt->rowCount() > 0) {
        echo "<p style='color: green;'><strong>✅ Success!</strong> Admin information updated</p>";
        echo "<ul>";
        echo "<li><strong>Display Name:</strong> Dishank Patel</li>";
        echo "<li><strong>Username:</strong> " . htmlspecialchars($currentAdmin['username']) . " (unchanged)</li>";
        echo "<li><strong>Password:</strong> admin (updated)</li>";
        echo "</ul>";
    } else {
        echo "<p style='color: orange;'>No changes made to admin information</p>";
    }
    
    // Show the updated admin
    echo "<h3>Updated Admin Information:</h3>";
    $stmt = $pdo->query("SELECT id, username, name, created_at FROM admins ORDER BY id");
    $updatedAdmin = $stmt->fetch();
    
    if ($updatedAdmin) {
        echo "<p><strong>Username:</strong> " . htmlspecialchars($updatedAdmin['username']) . "</p>";
        echo "<p><strong>Display Name:</strong> " . htmlspecialchars($updatedAdmin['name']) . "</p>";
        echo "<p><strong>Admin ID:</strong> " . $updatedAdmin['id'] . "</p>";
        echo "<p><strong>Created:</strong> " . $updatedAdmin['created_at'] . "</p>";
    }
    
    // Show updated table structure
    echo "<h3>Updated Admin Table Structure:</h3>";
    $stmt = $pdo->query("DESCRIBE admins");
    $updatedColumns = $stmt->fetchAll();
    
    echo "<table border='1' style='border-collapse: collapse; width: 100%; margin: 10px 0;'>";
    echo "<tr><th>Field</th><th>Type</th><th>Null</th><th>Key</th><th>Default</th><th>Extra</th></tr>";
    foreach ($updatedColumns as $column) {
        echo "<tr>";
        echo "<td>{$column['Field']}</td>";
        echo "<td>{$column['Type']}</td>";
        echo "<td>{$column['Null']}</td>";
        echo "<td>{$column['Key']}</td>";
        echo "<td>{$column['Default']}</td>";
        echo "<td>{$column['Extra']}</td>";
        echo "</tr>";
    }
    echo "</table>";
    
    echo "<h3>🎉 Summary:</h3>";
    echo "<div style='background-color: #d4edda; border: 1px solid #c3e6cb; border-radius: 5px; padding: 15px; margin: 10px 0;'>";
    echo "<p><strong>✅ Database Changes Completed:</strong></p>";
    echo "<ul>";
    echo "<li><strong>Table Structure:</strong> Added 'name' column to admins table</li>";
    echo "<li><strong>Display Name:</strong> Set to 'Dishank Patel'</li>";
    echo "<li><strong>Username:</strong> Remains unchanged</li>";
    echo "<li><strong>Password:</strong> Updated to 'admin'</li>";
    echo "</ul>";
    echo "<p><strong>🔐 New Login Credentials:</strong></p>";
    echo "<ul>";
    echo "<li><strong>Username:</strong> " . htmlspecialchars($updatedAdmin['username']) . "</li>";
    echo "<li><strong>Password:</strong> admin</li>";
    echo "</ul>";
    echo "</div>";
    
} catch (Exception $e) {
    echo "<p style='color: red;'>Error: " . $e->getMessage() . "</p>";
}
?>

<style>
body { font-family: Arial, sans-serif; margin: 20px; }
h2, h3 { color: #333; }
p { margin: 10px 0; }
table { margin: 10px 0; }
th { background-color: #f0f0f0; padding: 8px; }
td { padding: 6px; }
ul { margin: 10px 0; }
</style>
