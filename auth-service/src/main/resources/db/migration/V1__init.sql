CREATE TABLE auth.users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name          VARCHAR(100) NOT NULL,
    avatar_url    VARCHAR(500),
    language      VARCHAR(10)  NOT NULL DEFAULT 'es',
    timezone      VARCHAR(50)  NOT NULL DEFAULT 'Europe/Madrid',
    verified      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_users_email ON auth.users(email);

CREATE TABLE auth.households (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(100) NOT NULL,
    type          VARCHAR(20)  NOT NULL,
    invite_code   CHAR(6)      NOT NULL UNIQUE DEFAULT upper(substring(gen_random_uuid()::text, 1, 6)),
    invite_active BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX idx_households_invite_code ON auth.households(invite_code);

CREATE TABLE auth.memberships (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID        NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    household_id UUID        NOT NULL REFERENCES auth.households(id) ON DELETE CASCADE,
    role         VARCHAR(20) NOT NULL DEFAULT 'member',
    color        VARCHAR(7)  NOT NULL DEFAULT '#6366F1',
    joined_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, household_id)
);
CREATE INDEX idx_memberships_user      ON auth.memberships(user_id);
CREATE INDEX idx_memberships_household ON auth.memberships(household_id);

CREATE TABLE auth.invitations (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id UUID         NOT NULL REFERENCES auth.households(id) ON DELETE CASCADE,
    invited_by   UUID         NOT NULL REFERENCES auth.users(id),
    email        VARCHAR(255) NOT NULL,
    token        VARCHAR(255) NOT NULL UNIQUE,
    expires_at   TIMESTAMPTZ  NOT NULL,
    accepted_at  TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_invitations_token ON auth.invitations(token);
CREATE INDEX idx_invitations_email ON auth.invitations(email);

CREATE TABLE auth.join_requests (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id UUID        NOT NULL REFERENCES auth.households(id) ON DELETE CASCADE,
    user_id      UUID        NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    status       VARCHAR(20) NOT NULL DEFAULT 'pending',
    reviewed_by  UUID        REFERENCES auth.users(id),
    reviewed_at  TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (household_id, user_id)
);
CREATE INDEX idx_join_requests_household ON auth.join_requests(household_id);
CREATE INDEX idx_join_requests_user      ON auth.join_requests(user_id);

CREATE TABLE auth.sessions (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID         NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    refresh_token VARCHAR(255) NOT NULL UNIQUE,
    expires_at    TIMESTAMPTZ  NOT NULL,
    revoked_at    TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_sessions_user          ON auth.sessions(user_id);
CREATE INDEX idx_sessions_refresh_token ON auth.sessions(refresh_token);

CREATE TABLE auth.verification_tokens (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    token      VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ  NOT NULL,
    used_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_verification_tokens_token ON auth.verification_tokens(token);
