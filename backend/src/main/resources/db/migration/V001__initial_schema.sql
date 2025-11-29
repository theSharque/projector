CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    authorities TEXT
);

CREATE INDEX IF NOT EXISTS idx_roles_name ON roles(name);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    pass_hash VARCHAR(255) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

INSERT INTO users (id, email, pass_hash)
VALUES (1, 'admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918')
ON CONFLICT (id) DO NOTHING;

SELECT setval('users_id_seq', 10, true);
SELECT setval('roles_id_seq', 10, true);

