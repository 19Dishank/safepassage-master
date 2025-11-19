<?php
declare(strict_types=1);

require_once __DIR__ . '/../includes/openai_client.php';

header('Content-Type: text/html; charset=utf-8');

$client = new OpenAIClient();
$analyzer = new FeedbackSentimentAnalyzer($client);

$inputText = '';
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $inputText = isset($_POST['sample_text']) ? trim((string)$_POST['sample_text']) : '';
}
if ($inputText === '') {
    $inputText = 'I love using SafePassage, it is fantastic!';
}

$results = [
    'timestamp' => date('c'),
    'is_configured' => $client->isConfigured(),
    'base_uri' => null,
    'model' => null,
    'success' => false,
    'response' => null,
    'error' => null,
    'input_text' => $inputText,
];

try {
    $reflection = new ReflectionClass(OpenAIClient::class);
    foreach (['baseUri' => 'base_uri', 'model' => 'model'] as $property => $key) {
        if ($reflection->hasProperty($property)) {
            $prop = $reflection->getProperty($property);
            $prop->setAccessible(true);
            $results[$key] = $prop->getValue($client);
        }
    }
} catch (Throwable $e) {
    $results['error'] = 'Reflection error: ' . $e->getMessage();
}

if ($results['is_configured']) {
    try {
        $analysis = $analyzer->analyze($inputText);
        $results['success'] = true;
        $results['response'] = $analysis;
    } catch (Throwable $e) {
        $results['error'] = 'Request failed: ' . $e->getMessage();
    }
} else {
    $results['error'] = 'API client is not configured. Ensure your API key is set.';
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>OpenRouter API Debug</title>
    <style>
        body {
            font-family: "Segoe UI", sans-serif;
            background: #f5f6fa;
            margin: 0;
            padding: 2rem;
            color: #2f3640;
        }
        .card {
            background: #fff;
            border-radius: 10px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.07);
            max-width: 720px;
            margin: 0 auto;
            padding: 2rem;
        }
        form textarea {
            width: 100%;
            min-height: 120px;
            padding: 0.8rem;
            border-radius: 8px;
            border: 1px solid #dcdde1;
            font-family: inherit;
            font-size: 1rem;
            resize: vertical;
            margin-bottom: 1rem;
        }
        form button {
            background: #0984e3;
            border: none;
            color: #fff;
            padding: 0.6rem 1.2rem;
            border-radius: 6px;
            cursor: pointer;
            font-weight: 600;
        }
        form button:hover {
            background: #74b9ff;
        }
        h1 {
            font-size: 1.8rem;
            margin-bottom: 1rem;
        }
        pre {
            background: #2d3436;
            color: #dfe6e9;
            padding: 1rem;
            border-radius: 6px;
            overflow: auto;
        }
        .status {
            display: inline-block;
            padding: 0.4rem 0.8rem;
            border-radius: 999px;
            font-size: 0.9rem;
            margin-bottom: 1rem;
        }
        .status.success {
            background: #dff9fb;
            color: #0abde3;
        }
        .status.error {
            background: #fab1a0;
            color: #d63031;
        }
        .metadata {
            margin-bottom: 1rem;
        }
        .metadata dt {
            font-weight: 600;
        }
        .metadata dd {
            margin: 0 0 0.6rem 0;
        }
        .note {
            margin-top: 1.5rem;
            font-size: 0.9rem;
            color: #636e72;
        }
    </style>
</head>
<body>
    <div class="card">
        <h1>OpenRouter API Debug</h1>
        <div class="status <?php echo $results['success'] ? 'success' : 'error'; ?>">
            <?php echo $results['success'] ? 'Success' : 'Failure'; ?>
        </div>

        <form method="post">
            <label for="sample_text"><strong>Enter text to analyze:</strong></label>
            <textarea id="sample_text" name="sample_text" placeholder="Type your feedback here..."><?php echo htmlspecialchars($results['input_text']); ?></textarea>
            <button type="submit">Analyze Sentiment</button>
        </form>

        <dl class="metadata">
            <dt>Timestamp</dt>
            <dd><?php echo htmlspecialchars($results['timestamp']); ?></dd>

            <dt>Client configured</dt>
            <dd><?php echo $results['is_configured'] ? 'Yes' : 'No'; ?></dd>

            <dt>Base URI</dt>
            <dd><?php echo htmlspecialchars((string)$results['base_uri']); ?></dd>

            <dt>Model</dt>
            <dd><?php echo htmlspecialchars((string)$results['model']); ?></dd>
        </dl>

        <?php if ($results['success']): ?>
            <h2>Latest analysis</h2>
            <pre><?php echo htmlspecialchars(json_encode($results['response'], JSON_PRETTY_PRINT)); ?></pre>
        <?php endif; ?>

        <?php if (!$results['success'] && empty($results['error'])): ?>
            <h2>Info</h2>
            <pre>Awaiting input. Submit a sample text above to trigger sentiment analysis.</pre>
        <?php endif; ?>

        <?php if (!empty($results['error'])): ?>
            <h2>Error details</h2>
            <pre><?php echo htmlspecialchars($results['error']); ?></pre>
        <?php endif; ?>

        <p class="note">
            Reload this page after updating your key. It sends a single sentiment request, so avoid excessive refreshes.
        </p>
    </div>
</body>
</html>

