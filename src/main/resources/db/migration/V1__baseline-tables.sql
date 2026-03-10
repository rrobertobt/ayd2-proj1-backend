-- ============================================================
-- Proyecto 1B - DB Schema (PostgreSQL)
-- ============================================================

BEGIN;

-- ----------------------------
-- 1) Catálogos base
-- ----------------------------

CREATE TABLE roles
(
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    email         VARCHAR(200) NOT NULL UNIQUE,
    password_hash TEXT         NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    role_id       BIGINT       NOT NULL REFERENCES roles (id),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE employees
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT UNIQUE REFERENCES users (id),
    first_name  VARCHAR(120)   NOT NULL,
    last_name   VARCHAR(120)   NOT NULL,
    hourly_rate NUMERIC(12, 2) NOT NULL DEFAULT 0,
    active      BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT employee_hourly_rate_nonneg CHECK (hourly_rate >= 0)
);

-- ----------------------------
-- 2) Proyectos y equipo
-- ----------------------------

CREATE TABLE projects
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT project_status_chk CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE project_admin_assignment
(
    id          BIGSERIAL PRIMARY KEY,
    project_id  BIGINT  NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    employee_id BIGINT  NOT NULL REFERENCES employees (id),
    start_date  DATE    NOT NULL DEFAULT CURRENT_DATE,
    end_date    DATE,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT paa_dates_chk CHECK (end_date IS NULL OR end_date >= start_date)
);

-- 1 admin activo por proyecto (Postgres partial unique index)
CREATE UNIQUE INDEX uq_project_admin_active
    ON project_admin_assignment (project_id) WHERE active = TRUE;

-- ----------------------------
-- 3) Tipos de caso y workflow
-- ----------------------------

CREATE TABLE case_types
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    description TEXT,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE case_type_stages
(
    id           BIGSERIAL PRIMARY KEY,
    case_type_id BIGINT       NOT NULL REFERENCES case_types (id) ON DELETE CASCADE,
    name         VARCHAR(150) NOT NULL,
    description  TEXT,
    stage_order  INT          NOT NULL,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT cts_stage_order_chk CHECK (stage_order >= 1)
);

CREATE UNIQUE INDEX uq_case_type_stage_order
    ON case_type_stages (case_type_id, stage_order);

-- ----------------------------
-- 4) Casos y ejecución
-- ----------------------------

CREATE TABLE case_tickets
(
    id                     BIGSERIAL PRIMARY KEY,
    project_id             BIGINT       NOT NULL REFERENCES projects (id),
    case_type_id           BIGINT       NOT NULL REFERENCES case_types (id),
    created_by_employee_id BIGINT       NOT NULL REFERENCES employees (id),
    title                  VARCHAR(250) NOT NULL,
    description            TEXT,
    status                 VARCHAR(30)  NOT NULL DEFAULT 'OPEN', 
    due_date               DATE         NOT NULL,
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    canceled_at            TIMESTAMPTZ,
    cancel_reason          TEXT,
    CONSTRAINT case_status_chk CHECK (status IN ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELED')),
    CONSTRAINT case_cancel_chk CHECK (
        status <> 'CANCELED'
            OR (canceled_at IS NOT NULL AND cancel_reason IS NOT NULL AND length(trim(cancel_reason)) > 0)
        )
);

CREATE TABLE case_steps
(
    id                   BIGSERIAL PRIMARY KEY,
    case_id              BIGINT      NOT NULL REFERENCES case_tickets (id) ON DELETE CASCADE,
    case_type_stage_id   BIGINT      NOT NULL REFERENCES case_type_stages (id),
    step_order           INT         NOT NULL,
    status               VARCHAR(30) NOT NULL DEFAULT 'PENDING', 
    assigned_employee_id BIGINT REFERENCES employees (id),
    assigned_at          TIMESTAMPTZ,
    started_at           TIMESTAMPTZ,
    submitted_at         TIMESTAMPTZ,
    approved_at          TIMESTAMPTZ,
    rejected_at          TIMESTAMPTZ,
    rejection_reason     TEXT,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT cs_step_order_chk CHECK (step_order >= 1),
    CONSTRAINT cs_status_chk CHECK (status IN
                                    ('PENDING', 'ASSIGNED', 'IN_PROGRESS', 'SUBMITTED', 'APPROVED', 'REJECTED')),
    CONSTRAINT cs_reject_chk CHECK (
        status <> 'REJECTED'
            OR (rejected_at IS NOT NULL AND rejection_reason IS NOT NULL AND length(trim(rejection_reason)) > 0)
        ),
    CONSTRAINT cs_approve_chk CHECK (
        status <> 'APPROVED'
            OR approved_at IS NOT NULL
        )
);

-- Un paso por orden por caso (Postgres partial unique index)
CREATE UNIQUE INDEX uq_case_step_order
    ON case_steps (case_id, step_order);

CREATE TABLE work_logs
(
    id           BIGSERIAL PRIMARY KEY,
    case_step_id BIGINT         NOT NULL REFERENCES case_steps (id) ON DELETE CASCADE,
    employee_id  BIGINT         NOT NULL REFERENCES employees (id),
    comment      TEXT           NOT NULL,
    hours_spent  NUMERIC(10, 2) NOT NULL,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT wl_hours_chk CHECK (hours_spent > 0),
    CONSTRAINT wl_comment_chk CHECK (length(trim(comment)) > 0)
);

-- ----------------------------
-- 5) Auditoría
-- ----------------------------

CREATE TABLE audit_logs
(
    id                   BIGSERIAL PRIMARY KEY,
    entity_type          VARCHAR(40) NOT NULL,
    entity_id            BIGINT      NOT NULL,
    action               VARCHAR(60) NOT NULL,
    performed_by_user_id BIGINT REFERENCES users (id),
    detail               JSONB,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


CREATE INDEX idx_case_ticket_project_status_due
    ON case_tickets (project_id, status, due_date);

CREATE INDEX idx_case_ticket_project_due
    ON case_tickets (project_id, due_date);

CREATE INDEX idx_case_step_assigned_status
    ON case_steps (assigned_employee_id, status);

CREATE INDEX idx_case_step_case
    ON case_steps (case_id);

CREATE INDEX idx_work_log_employee_created
    ON work_logs (employee_id, created_at);

CREATE INDEX idx_work_log_step
    ON work_logs (case_step_id);

CREATE INDEX idx_audit_entity
    ON audit_logs (entity_type, entity_id, created_at);

COMMIT;