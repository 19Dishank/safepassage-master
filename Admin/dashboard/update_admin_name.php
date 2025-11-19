<?php
require_once 'config/db.php';

echo "<h2>Update Admin Name</h2>";

try {
    // First, let's see the current admin
    echo "<h3>Current Admin:</h3>";
    $stmt = $pdo->query("SELECT id, username, created_at FROM admins ORDER BY id");
    $currentAdmin = $stmt->fetch();
    
    if ($currentAdmin) {
        echo "<p><strong>Current Username:</strong> " . htmlspecialchars($currentAdmin['username']) . "</p>";
        echo "<p><strong>Admin ID:</strong> " . $currentAdmin['id'] . "</p>";
        echo "<p><strong>Created:</strong> " . $currentAdmin['created_at'] . "</p>";
    } else {
        echo "<p>No admin found in database.</p>";
        exit;
    }
    
    // Update the admin username to 'Dishank Patel'
    echo "<h3>Updating Admin Name...</h3>";
    $stmt = $pdo->prepare("UPDATE admins SET username = ? WHERE id = ?");
    $stmt->execute(['Dishank Patel', $currentAdmin['id']]);
    
    if ($stmt->rowCount() > 0) {
        echo "<p style='color: green;'><strong>✅ Success!</strong> Admin username updated to 'Dishank Patel'</p>";
    } else {
        echo "<p style='color: orange;'>No changes made (username might already be 'Dishank Patel')</p>";
    }
    
    // Show the updated admin
    echo "<h3>Updated Admin:</h3>";
    $stmt = $pdo->query("SELECT id, username, created_at FROM admins ORDER BY id");
    $updatedAdmin = $stmt->fetch();
    
    if ($updatedAdmin) {
        echo "<p><strong>New Username:</strong> " . htmlspecialchars($updatedAdmin['username']) . "</p>";
        echo "<p><strong>Admin ID:</strong> " . $updatedAdmin['id'] . "</p>";
        echo "<p><strong>Created:</strong> " . $updatedAdmin['created_at'] . "</p>";
    }
    
    echo "<p><strong>🎉 Admin name successfully updated to 'Dishank Patel'!</strong></p>";
    echo "<p>You can now login with username: <strong>Dishank Patel</strong></p>";
    
} catch (Exception $e) {
    echo "<p style='color: red;'>Error: " . $e->getMessage() . "</p>";
}
?>

<style>
body { font-family: Arial, sans-serif; margin: 20px; }
h2, h3 { color: #333; }
p { margin: 10px 0; }
</style>
