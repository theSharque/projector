-- Create roles table
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    authorities TEXT
);

-- Create index on name for faster lookups
CREATE INDEX IF NOT EXISTS idx_roles_name ON roles(name);

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    pass_hash VARCHAR(255) NOT NULL
);

-- Create index on email for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Insert admin user (password: admin, hashed with SHA256)
-- Password hash for 'admin' = SHA256('admin') = 8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918
INSERT INTO users (id, email, pass_hash)
VALUES (1, 'admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918')
ON CONFLICT (id) DO NOTHING;

-- Reset sequence to avoid conflicts
SELECT setval('users_id_seq', COALESCE((SELECT MAX(id) FROM users), 1), true);

