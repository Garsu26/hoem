ALTER TABLE auth.verification_tokens
    ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'email_verification';
