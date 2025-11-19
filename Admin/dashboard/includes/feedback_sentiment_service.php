<?php

require_once __DIR__ . '/openai_client.php';

class FeedbackSentimentService
{
    private PDO $pdo;
    private FeedbackSentimentAnalyzer $analyzer;
    private bool $schemaEnsured = false;

    public function __construct(PDO $pdo, ?FeedbackSentimentAnalyzer $analyzer = null)
    {
        $this->pdo = $pdo;
        $this->analyzer = $analyzer ?? new FeedbackSentimentAnalyzer();
    }

    public function getAnalyzer(): FeedbackSentimentAnalyzer
    {
        return $this->analyzer;
    }

    /**
     * Ensure sentiment-related columns exist on the feedbacks table.
     */
    public function ensureSchema(): void
    {
        if ($this->schemaEnsured) {
            return;
        }

        try {
            $columns = $this->fetchExistingColumns();
            $alterStatements = [];

            if (!isset($columns['sentiment_label'])) {
                $alterStatements[] = 'ADD COLUMN `sentiment_label` VARCHAR(20) NULL AFTER `feedback_text`';
            }

            if (!isset($columns['sentiment_confidence'])) {
                $alterStatements[] = 'ADD COLUMN `sentiment_confidence` DECIMAL(5,4) NULL AFTER `sentiment_label`';
            }

            if (!isset($columns['sentiment_reason'])) {
                $alterStatements[] = 'ADD COLUMN `sentiment_reason` VARCHAR(255) NULL AFTER `sentiment_confidence`';
            }

            if (!isset($columns['sentiment_category'])) {
                $alterStatements[] = 'ADD COLUMN `sentiment_category` VARCHAR(20) NULL AFTER `sentiment_reason`';
            }

            if (!isset($columns['sentiment_updated_at'])) {
                $alterStatements[] = 'ADD COLUMN `sentiment_updated_at` TIMESTAMP NULL DEFAULT NULL AFTER `sentiment_category`';
            }

            if (!empty($alterStatements)) {
                $sql = 'ALTER TABLE `feedbacks` ' . implode(', ', $alterStatements);
                $this->pdo->exec($sql);
            }

            $this->schemaEnsured = true;
        } catch (Throwable $e) {
            error_log('[FeedbackSentimentService] Failed to ensure schema: ' . $e->getMessage());
            // Do not throw further to avoid breaking page rendering.
        }
    }

    /**
     * Analyze unclassified feedbacks and persist sentiment metadata.
     */
    public function analyzePending(int $batchSize = 5): void
    {
        $this->ensureSchema();

        $limit = max(1, min(50, (int)$batchSize));

        $sql = "
            SELECT id, feedback_text
            FROM feedbacks
            WHERE sentiment_label IS NULL OR sentiment_label = ''
            ORDER BY created_at DESC
            LIMIT {$limit}
        ";

        try {
            $rows = $this->pdo->query($sql)->fetchAll(PDO::FETCH_ASSOC);
        } catch (Throwable $e) {
            error_log('[FeedbackSentimentService] Failed to fetch pending feedbacks: ' . $e->getMessage());
            return;
        }

        if (empty($rows)) {
            return;
        }

        $updateStmt = $this->pdo->prepare("
            UPDATE feedbacks
            SET sentiment_label = :label,
                sentiment_confidence = :confidence,
                sentiment_reason = :reason,
                sentiment_category = :category,
                sentiment_updated_at = NOW()
            WHERE id = :id
        ");

        foreach ($rows as $row) {
            $analysis = $this->analyzer->analyze($row['feedback_text'] ?? '');

            try {
                $updateStmt->execute([
                    ':label' => $analysis['sentiment'],
                    ':confidence' => $analysis['confidence'],
                    ':reason' => $analysis['reason'],
                    ':category' => $analysis['category'],
                    ':id' => $row['id'],
                ]);
            } catch (Throwable $e) {
                error_log('[FeedbackSentimentService] Failed to update sentiment for feedback ID ' . $row['id'] . ': ' . $e->getMessage());
            }
        }
    }

    /**
     * Returns aggregated sentiment counts.
     *
     * @return array{positive:int, negative:int, neutral:int, suggestion:int, unclassified:int}
     */
    public function getSentimentCounts(): array
    {
        $this->ensureSchema();

        $counts = [
            'positive' => 0,
            'negative' => 0,
            'neutral' => 0,
            'suggestion' => 0,
            'unclassified' => 0,
        ];

        try {
            $stmt = $this->pdo->query("
                SELECT COALESCE(NULLIF(sentiment_category, ''), sentiment_label) AS category, COUNT(*) AS total
                FROM feedbacks
                GROUP BY COALESCE(NULLIF(sentiment_category, ''), sentiment_label)
            ");

            foreach ($stmt->fetchAll(PDO::FETCH_ASSOC) as $row) {
                $label = $row['category'];
                $total = (int)$row['total'];

                if ($label === null || $label === '') {
                    $counts['unclassified'] = $total;
                    continue;
                }

                $label = strtolower($label);
                if (isset($counts[$label])) {
                    $counts[$label] = $total;
                }
            }
        } catch (Throwable $e) {
            error_log('[FeedbackSentimentService] Failed to compute sentiment counts: ' . $e->getMessage());
        }

        return $counts;
    }

    /**
     * @return array<string, true>
     */
    private function fetchExistingColumns(): array
    {
        $columns = [];

        try {
            $stmt = $this->pdo->query('SHOW COLUMNS FROM `feedbacks`');
            foreach ($stmt->fetchAll(PDO::FETCH_ASSOC) as $column) {
                if (isset($column['Field'])) {
                    $columns[$column['Field']] = true;
                }
            }
        } catch (Throwable $e) {
            error_log('[FeedbackSentimentService] Failed to inspect columns: ' . $e->getMessage());
        }

        return $columns;
    }
}


