-- ============================================================
-- V3 – Add onboarding_completed flag to users
-- ============================================================

BEGIN;

ALTER TABLE users
    ADD COLUMN onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE;

-- Users that already have a non-placeholder password (active users created
-- before this migration) should be marked as having completed onboarding.
UPDATE users
SET onboarding_completed = TRUE
WHERE active = TRUE;

COMMIT;
