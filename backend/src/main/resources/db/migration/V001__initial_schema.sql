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

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);

CREATE TABLE IF NOT EXISTS roadmaps (
    id BIGSERIAL PRIMARY KEY,
    project_name VARCHAR(255) NOT NULL,
    create_date TIMESTAMP NOT NULL,
    update_date TIMESTAMP,
    author_id BIGINT NOT NULL,
    mission TEXT,
    description TEXT,
    FOREIGN KEY (author_id) REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_roadmaps_author_id ON roadmaps(author_id);
CREATE INDEX IF NOT EXISTS idx_roadmaps_project_name ON roadmaps(project_name);

CREATE TABLE IF NOT EXISTS roadmap_users (
    id BIGSERIAL PRIMARY KEY,
    roadmap_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (roadmap_id) REFERENCES roadmaps(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE (roadmap_id, user_id)
);
CREATE INDEX IF NOT EXISTS idx_roadmap_users_roadmap_id ON roadmap_users(roadmap_id);
CREATE INDEX IF NOT EXISTS idx_roadmap_users_user_id ON roadmap_users(user_id);

CREATE TABLE IF NOT EXISTS functional_areas (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    create_date TIMESTAMP NOT NULL,
    update_date TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_functional_areas_name ON functional_areas(name);

CREATE TABLE IF NOT EXISTS features (
    id BIGSERIAL PRIMARY KEY,
    year BIGINT NOT NULL,
    quarter VARCHAR(2) NOT NULL,
    sprint BIGINT,
    release VARCHAR(255),
    summary VARCHAR(255),
    description TEXT,
    create_date TIMESTAMP NOT NULL,
    update_date TIMESTAMP,
    author_id BIGINT NOT NULL,
    functional_area_ids BIGINT[],
    FOREIGN KEY (author_id) REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_features_year ON features(year);
CREATE INDEX IF NOT EXISTS idx_features_quarter ON features(quarter);
CREATE INDEX IF NOT EXISTS idx_features_year_quarter ON features(year, quarter);
CREATE INDEX IF NOT EXISTS idx_features_author_id ON features(author_id);
CREATE INDEX IF NOT EXISTS idx_features_functional_area_ids ON features USING GIN(functional_area_ids);

CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    feature_id BIGINT NOT NULL,
    roadmap_id BIGINT NOT NULL,
    summary VARCHAR(255),
    description TEXT,
    create_date TIMESTAMP NOT NULL,
    update_date TIMESTAMP,
    author_id BIGINT NOT NULL,
    FOREIGN KEY (feature_id) REFERENCES features(id),
    FOREIGN KEY (roadmap_id) REFERENCES roadmaps(id),
    FOREIGN KEY (author_id) REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_tasks_feature_id ON tasks(feature_id);
CREATE INDEX IF NOT EXISTS idx_tasks_roadmap_id ON tasks(roadmap_id);
CREATE INDEX IF NOT EXISTS idx_tasks_author_id ON tasks(author_id);

INSERT INTO users (id, email, pass_hash)
VALUES (
        1,
        'admin',
        '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918'
    ) ON CONFLICT (id) DO NOTHING;

INSERT INTO roles (id, name, authorities)
VALUES (
        1,
        'SUPERADMIN',
        'USER_VIEW,USER_EDIT,ROLE_VIEW,ROLE_EDIT,ROADMAP_VIEW,ROADMAP_EDIT,FEATURE_VIEW,FEATURE_EDIT,TASK_VIEW,TASK_EDIT,FA_VIEW,FA_EDIT'
    ) ON CONFLICT (id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
VALUES (1, 1) ON CONFLICT (user_id, role_id) DO NOTHING;

SELECT setval('users_id_seq', 10, true);
SELECT setval('roles_id_seq', 10, true);
SELECT setval('roadmaps_id_seq', 10, true);
SELECT setval('functional_areas_id_seq', 10, true);
SELECT setval('features_id_seq', 10, true);
SELECT setval('tasks_id_seq', 10, true);

