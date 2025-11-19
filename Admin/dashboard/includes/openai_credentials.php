<?php
/**
 * Optional override credentials for the OpenAI/OpenRouter client.
 *
 * ⚠️ SECURITY NOTE:
 * Storing API keys directly in source control is risky. Prefer environment variables
 * whenever possible. If you must keep the key in source, ensure the repository is private.
 *
 * Usage:
 *   define('OPENAI_API_KEY_OVERRIDE', 'sk-or-your-openrouter-key');
 *   define('OPENAI_BASE_URI_OVERRIDE', 'https://openrouter.ai/api/v1');
 *   define('OPENAI_HTTP_REFERER_OVERRIDE', 'https://yourdomain.com');
 *   define('OPENAI_APP_TITLE_OVERRIDE', 'SafePassage Admin');
 *
 * Leave values empty to fall back to environment variables.
 */

if (!defined('OPENAI_API_KEY_OVERRIDE')) {
    define('OPENAI_API_KEY_OVERRIDE', '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');
}

if (!defined('OPENAI_BASE_URI_OVERRIDE')) {
    define('OPENAI_BASE_URI_OVERRIDE', '');
}

if (!defined('OPENAI_HTTP_REFERER_OVERRIDE')) {
    define('OPENAI_HTTP_REFERER_OVERRIDE', '');
}

if (!defined('OPENAI_APP_TITLE_OVERRIDE')) {
    define('OPENAI_APP_TITLE_OVERRIDE', '');
}

