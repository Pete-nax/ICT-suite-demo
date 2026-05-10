/**
 * KenIT API Configuration
 *
 * LOCAL DEV : leave window.KENIT_API_URL as empty string — falls back to localhost:8080
 *
 * RENDER PROD: After kenit-api finishes its first deploy on Render,
 *              copy its URL (e.g. https://kenit-api.onrender.com)
 *              paste it below, then push — kenit-ui will redeploy in ~30s.
 */
window.KENIT_API_URL = "https://kenit-api-ub1w.onrender.com";   // ← paste Render API URL here for prod
