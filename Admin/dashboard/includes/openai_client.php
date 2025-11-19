<?php
/**
 * Lightweight OpenAI client and sentiment analyzer wrapper.
 *
 * This file provides two classes:
 *  - OpenAIClient: minimal HTTP wrapper around the OpenAI (and OpenRouter) Chat Completions API
 *  - FeedbackSentimentAnalyzer: sentiment classification with OpenAI fallback heuristics
 *
 * The API key is read from environment variables or optional override config by default.
 */

require_once __DIR__ . '/openai_credentials.php';

class OpenAIClient
{
    private string $apiKey;
    private string $baseUri;
    private string $model;
    private ?string $httpReferer;
    private ?string $appTitle;

    public function __construct(
        ?string $apiKey = null,
        ?string $baseUri = null,
        ?string $model = null,
        ?string $httpReferer = null,
        ?string $appTitle = null
    ) {
        $this->apiKey = $this->resolveApiKey($apiKey);
        $this->baseUri = $this->resolveBaseUri($baseUri, $this->apiKey);
        $this->model = $model ?? $this->resolveDefaultModel($this->baseUri);
        $this->httpReferer = $httpReferer ?? $this->resolveHttpReferer();
        $this->appTitle = $appTitle ?? $this->resolveAppTitle();
    }

    public function isConfigured(): bool
    {
        return $this->apiKey !== '';
    }

    /**
     * Calls the OpenAI API to classify sentiment for the provided text.
     *
     * @return array<string, mixed>|null
     */
    public function classifySentiment(string $text): ?array
    {
        if (!$this->isConfigured()) {
            return null;
        }

        $payload = [
            'model' => $this->model,
            'messages' => [
                [
                    'role' => 'system',
                    'content' => implode(' ', [
                        'You are a precise sentiment analysis assistant.',
                        'Reply ONLY with a single JSON object containing the keys',
                        '"sentiment", "confidence", "reason", and "category".',
                        '"sentiment" must be one of: "positive", "negative", "neutral".',
                        '"category" must be one of: "positive", "negative", "neutral", "suggestion".',
                        'If the feedback primarily requests improvements or features, set "category" to "suggestion".',
                        'When category differs from sentiment, choose the most appropriate sentiment tone.',
                        '"confidence" must be a number between 0 and 1.',
                        '"reason" should be a short explanation under 40 words.',
                        'Return valid JSON without Markdown fencing or additional text.',
                    ]),
                ],
                [
                    'role' => 'user',
                    'content' => $text,
                ],
            ],
            'temperature' => 0,
            'max_tokens' => 200,
        ];

        $response = $this->post('/chat/completions', $payload);

        if (!is_array($response) || !isset($response['choices'][0]['message']['content'])) {
            return null;
        }

        $content = trim($response['choices'][0]['message']['content']);
        $decoded = $this->decodeJsonContent($content);

        if (!is_array($decoded)) {
            return null;
        }

        $sentiment = strtolower((string)($decoded['sentiment'] ?? ''));
        if (!in_array($sentiment, ['positive', 'negative', 'neutral'], true)) {
            return null;
        }

        $category = strtolower((string)($decoded['category'] ?? $sentiment));
        if (!in_array($category, ['positive', 'negative', 'neutral', 'suggestion'], true)) {
            $category = $sentiment;
        }

        $confidence = isset($decoded['confidence']) ? (float)$decoded['confidence'] : null;
        if ($confidence !== null) {
            $confidence = max(0.0, min(1.0, $confidence));
        }

        $reason = $decoded['reason'] ?? null;
        if (is_string($reason)) {
            $reason = trim($reason);
        } else {
            $reason = null;
        }

        return [
            'sentiment' => $sentiment,
            'confidence' => $confidence,
            'reason' => $reason,
            'category' => $category,
        ];
    }

    /**
     * Creates a generic chat completion with custom messages.
     *
     * @param array<int, array{role:string, content:string}> $messages
     * @param array<string, mixed> $options
     * @return array{content:string, usage:array<string, mixed>|null, raw:array<string, mixed>}|null
     */
    public function createChatCompletion(array $messages, array $options = []): ?array
    {
        if (!$this->isConfigured()) {
            return null;
        }

        $payload = array_merge([
            'model' => $this->model,
            'messages' => $messages,
            'temperature' => 0.25,
            'max_tokens' => 900,
        ], $options);

        $response = $this->post('/chat/completions', $payload);

        if (!is_array($response) || !isset($response['choices'][0]['message']['content'])) {
            return null;
        }

        return [
            'content' => trim((string)$response['choices'][0]['message']['content']),
            'usage' => $response['usage'] ?? null,
            'raw' => $response,
        ];
    }

    /**
     * Performs a POST request to the OpenAI API.
     *
     * @param array<string, mixed> $payload
     * @return array<string, mixed>|null
     */
    private function post(string $path, array $payload): ?array
    {
        $url = rtrim($this->baseUri, '/') . $path;

        $headers = [
            'Content-Type: application/json',
            'Authorization: Bearer ' . $this->apiKey,
        ];

        if ($this->isOpenRouter()) {
            if (!empty($this->httpReferer)) {
                $headers[] = 'HTTP-Referer: ' . $this->httpReferer;
            }
            if (!empty($this->appTitle)) {
                $headers[] = 'X-Title: ' . $this->appTitle;
            }
        }

        $ch = curl_init($url);
        curl_setopt_array($ch, [
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_POST => true,
            CURLOPT_HTTPHEADER => $headers,
            CURLOPT_POSTFIELDS => json_encode($payload, JSON_THROW_ON_ERROR),
            CURLOPT_TIMEOUT => 25,
        ]);

        $result = curl_exec($ch);
        $error = curl_error($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        curl_close($ch);

        if ($result === false) {
            error_log('[OpenAIClient] cURL error: ' . $error);
            return null;
        }

        if ($httpCode >= 400) {
            error_log(sprintf('[OpenAIClient] HTTP %d response: %s', $httpCode, $result));
            return null;
        }

        try {
            /** @var array<string, mixed>|null */
            $decoded = json_decode($result, true, 512, JSON_THROW_ON_ERROR);
            return $decoded;
        } catch (Throwable $e) {
            error_log('[OpenAIClient] Failed to decode response: ' . $e->getMessage());
            return null;
        }
    }

    /**
     * Attempts to decode a JSON string, optionally extracting the first JSON object if needed.
     *
     * @return array<string, mixed>|null
     */
    private function decodeJsonContent(string $content): ?array
    {
        try {
            /** @var array<string, mixed> */
            $decoded = json_decode($content, true, 512, JSON_THROW_ON_ERROR);
            return $decoded;
        } catch (Throwable $e) {
            // Try to extract JSON object from the text
            if (preg_match('/\{.+\}/s', $content, $matches)) {
                try {
                    /** @var array<string, mixed> */
                    $fallbackDecoded = json_decode($matches[0], true, 512, JSON_THROW_ON_ERROR);
                    return $fallbackDecoded;
                } catch (Throwable $inner) {
                    return null;
                }
            }
            return null;
        }
    }

    private function resolveApiKey(?string $input): string
    {
        $candidates = [
            $input,
            (defined('OPENAI_API_KEY_OVERRIDE') && OPENAI_API_KEY_OVERRIDE !== '') ? OPENAI_API_KEY_OVERRIDE : null,
            getenv('OPENAI_API_KEY') ?: null,
            getenv('OPENROUTER_API_KEY') ?: null,
        ];

        foreach ($candidates as $candidate) {
            if (is_string($candidate) && trim($candidate) !== '') {
                return trim($candidate);
            }
        }

        return '';
    }

    private function resolveBaseUri(?string $input, string $apiKey): string
    {
        $candidates = [
            $input,
            (defined('OPENAI_BASE_URI_OVERRIDE') && OPENAI_BASE_URI_OVERRIDE !== '') ? OPENAI_BASE_URI_OVERRIDE : null,
            getenv('OPENAI_BASE_URI') ?: null,
            getenv('OPENAI_API_BASE') ?: null,
        ];

        foreach ($candidates as $candidate) {
            if (is_string($candidate) && trim($candidate) !== '') {
                return rtrim(trim($candidate), '/');
            }
        }

        if ($this->looksLikeOpenRouterKey($apiKey)) {
            return 'https://openrouter.ai/api/v1';
        }

        if (getenv('OPENROUTER_API_KEY')) {
            return 'https://openrouter.ai/api/v1';
        }

        return 'https://api.openai.com/v1';
    }

    private function resolveDefaultModel(string $baseUri): string
    {
        if (getenv('OPENAI_MODEL')) {
            return getenv('OPENAI_MODEL');
        }

        if ($this->isKnownOpenRouterBase($baseUri)) {
            return 'openai/gpt-4o-mini';
        }

        return 'gpt-4o-mini';
    }

    private function resolveHttpReferer(): ?string
    {
        $candidates = [
            (defined('OPENAI_HTTP_REFERER_OVERRIDE') && OPENAI_HTTP_REFERER_OVERRIDE !== '') ? OPENAI_HTTP_REFERER_OVERRIDE : null,
            getenv('OPENAI_HTTP_REFERER') ?: null,
        ];

        foreach ($candidates as $candidate) {
            if (is_string($candidate) && trim($candidate) !== '') {
                return trim($candidate);
            }
        }

        if (isset($_SERVER['HTTP_HOST'])) {
            $scheme = isset($_SERVER['REQUEST_SCHEME']) ? $_SERVER['REQUEST_SCHEME'] : (isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off' ? 'https' : 'http');
            return $scheme . '://' . $_SERVER['HTTP_HOST'];
        }

        return null;
    }

    private function resolveAppTitle(): ?string
    {
        $candidates = [
            (defined('OPENAI_APP_TITLE_OVERRIDE') && OPENAI_APP_TITLE_OVERRIDE !== '') ? OPENAI_APP_TITLE_OVERRIDE : null,
            getenv('OPENAI_APP_TITLE') ?: null,
        ];

        foreach ($candidates as $candidate) {
            if (is_string($candidate) && trim($candidate) !== '') {
                return trim($candidate);
            }
        }

        return 'SafePassage Admin';
    }

    private function looksLikeOpenRouterKey(string $key): bool
    {
        return stripos($key, 'sk-or-') === 0;
    }

    private function isKnownOpenRouterBase(string $baseUri): bool
    {
        return stripos($baseUri, 'openrouter.ai') !== false;
    }

    private function isOpenRouter(): bool
    {
        return $this->isKnownOpenRouterBase($this->baseUri);
    }
}

class FeedbackSentimentAnalyzer
{
    private OpenAIClient $client;

    /** @var string[] */
    private array $negativeWords = [
        'bad', 'terrible', 'awful', 'poor', 'hate', 'angry', 'disappointed', 'issue', 'problem',
        'worst', 'negative', 'bug', 'broken', 'unhappy', 'frustrated', 'annoyed', 'slow', 'crash',
        'fail', 'failure', 'complaint', 'confusing', 'difficult',
    ];

    /** @var string[] */
    private array $positiveWords = [
        'good', 'great', 'excellent', 'awesome', 'love', 'happy', 'satisfied', 'amazing', 'wonderful',
        'best', 'positive', 'enjoy', 'smooth', 'helpful', 'nice', 'fantastic', 'fast', 'perfect',
        'recommend', 'appreciate',
    ];

    /** @var string[] */
    private array $suggestionWords = [
        'need', 'should', 'could', 'would like', 'feature', 'improve', 'suggest', 'request', 'add',
        'better', 'more options', 'more feature', 'enhance', 'upgrade', 'wish', 'hope', 'consider',
    ];

    public function __construct(?OpenAIClient $client = null)
    {
        $this->client = $client ?? new OpenAIClient();
    }

    public function isConfigured(): bool
    {
        return $this->client->isConfigured();
    }

    /**
     * Analyze sentiment for the provided feedback text.
     *
     * @return array{sentiment: string, confidence: float|null, reason: string|null}
     */
    public function analyze(string $text): array
    {
        $cleanText = trim($text);
        if ($cleanText === '') {
            return [
                'sentiment' => 'neutral',
                'confidence' => 0.0,
                'reason' => 'Empty feedback text',
                'category' => 'neutral',
            ];
        }

        $aiResult = $this->client->classifySentiment($cleanText);
        if (is_array($aiResult)) {
            return [
                'sentiment' => $aiResult['sentiment'],
                'confidence' => $aiResult['confidence'],
                'reason' => $aiResult['reason'],
                'category' => $aiResult['category'] ?? $aiResult['sentiment'],
            ];
        }

        // Fallback heuristic when AI is unavailable or fails.
        return $this->fallbackHeuristic($cleanText);
    }

    /**
     * Basic heuristic sentiment analysis used as fallback.
     *
     * @return array{sentiment: string, confidence: float|null, reason: string|null}
     */
    private function fallbackHeuristic(string $text): array
    {
        $normalized = $this->normalizeText($text);
        $tokens = $this->tokenize($normalized);

        $posHits = $this->countMatches($tokens, $this->positiveWords);
        $negHits = $this->countMatches($tokens, $this->negativeWords);

        $suggestionHits = $this->countSuggestionHits($normalized);

        if ($suggestionHits > max($posHits, $negHits)) {
            return [
                'sentiment' => $negHits > $posHits ? 'negative' : ($posHits > $negHits ? 'positive' : 'neutral'),
                'confidence' => 0.45,
                'reason' => 'Suggestion-oriented feedback detected',
                'category' => 'suggestion',
            ];
        }

        if ($posHits === 0 && $negHits === 0) {
            return [
                'sentiment' => 'neutral',
                'confidence' => 0.25,
                'reason' => 'No strong sentiment keywords detected',
                'category' => 'neutral',
            ];
        }

        if ($negHits > $posHits) {
            $confidence = min(1.0, 0.4 + ($negHits - $posHits) * 0.1);
            return [
                'sentiment' => 'negative',
                'confidence' => round($confidence, 2),
                'reason' => 'Negative keywords detected',
                'category' => 'negative',
            ];
        }

        if ($posHits > $negHits) {
            $confidence = min(1.0, 0.4 + ($posHits - $negHits) * 0.1);
            return [
                'sentiment' => 'positive',
                'confidence' => round($confidence, 2),
                'reason' => 'Positive keywords detected',
                'category' => 'positive',
            ];
        }

        return [
            'sentiment' => 'neutral',
            'confidence' => 0.35,
            'reason' => 'Balanced positive and negative keywords',
            'category' => 'neutral',
        ];
    }

    private function normalizeText(string $text): string
    {
        if (function_exists('mb_strtolower')) {
            return mb_strtolower($text, 'UTF-8');
        }
        return strtolower($text);
    }

    /**
     * @return string[]
     */
    private function tokenize(string $text): array
    {
        $tokens = preg_split('/[^a-z0-9]+/i', $text, -1, PREG_SPLIT_NO_EMPTY);
        return array_map('strtolower', $tokens ?? []);
    }

    /**
     * @param string[] $tokens
     * @param string[] $dictionary
     */
    private function countMatches(array $tokens, array $dictionary): int
    {
        $dict = array_fill_keys($dictionary, true);
        $count = 0;
        foreach ($tokens as $token) {
            if (isset($dict[$token])) {
                $count++;
            }
        }
        return $count;
    }

    private function countSuggestionHits(string $text): int
    {
        $count = 0;
        foreach ($this->suggestionWords as $phrase) {
            if (mb_stripos($text, $phrase) !== false) {
                $count++;
            }
        }
        return $count;
    }
}


