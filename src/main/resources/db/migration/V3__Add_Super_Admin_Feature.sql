-- 1. Add 'admin' column
ALTER TABLE roles ADD COLUMN admin BOOLEAN DEFAULT FALSE NOT NULL;

-- 2. Create SUPER_ADMIN role
INSERT INTO roles (name, level, admin) VALUES ('SUPER_ADMIN', 1000, TRUE)
ON CONFLICT (name) DO NOTHING;

-- Assign SUPER_ADMIN role to 'admin' user
INSERT INTO user_roles (user_id, role_id) VALUES (
  (SELECT id FROM users WHERE cpf_cnpj = 'admin'),
  (SELECT id FROM roles WHERE name = 'SUPER_ADMIN')
)
ON CONFLICT (user_id, role_id) DO NOTHING;
