-- Create roles table
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    authorities TEXT
);

-- Create index on name for faster lookups
CREATE INDEX IF NOT EXISTS idx_roles_name ON roles(name);

