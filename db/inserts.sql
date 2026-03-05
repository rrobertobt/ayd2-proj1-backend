INSERT INTO roles (code, name, description, active)
VALUES
    ('SYSTEM_ADMIN', 'System Administrator', 'Full access to all system features and settings.', TRUE),
    ('PROJECT_ADMIN', 'Project Administrator', 'Can manage projects and assign tasks.', TRUE),
    ('DEVELOPER', 'Developer', 'Can view and update assigned tasks.', TRUE);

INSERT INTO users (username, email, password_hash, role_id)
SELECT
    'superadmin',
    'superadmin@tracker.com',
    '$2a$12$QhpH.DIM3W1FuL/7JC/o0OO9YM5N.BDRn.KpKjD8MRPzftoBDyTZu',
    id
FROM roles
WHERE code = 'SYSTEM_ADMIN';

INSERT INTO employees (user_id, first_name, last_name, hourly_rate)
SELECT
    (SELECT id FROM users WHERE username = 'superadmin'),
    'Super',
    'Admin',
    0.00;
