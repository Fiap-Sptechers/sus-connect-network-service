-- Seed Roles with Levels
INSERT INTO roles (id, name, level) VALUES 
('00000000-0000-0000-0000-000000000001', 'ADMIN', 100), 
('00000000-0000-0000-0000-000000000002', 'MANAGER', 50),
('00000000-0000-0000-0000-000000000003', 'OPERATOR', 10)
ON CONFLICT (name) DO NOTHING;

-- Admin User Seed (Password: admin)
INSERT INTO users (id, name, cpf_cnpj, password) VALUES 
('19944722-fb48-4a6d-b292-ca3ae4719828', 'Administrator', 'admin', '$2a$10$2hJT/5EK1f0cNTQt0piHQe9adMeLerSq0AMEK8eIO8Glq5MsbM.qu')
ON CONFLICT (cpf_cnpj) DO NOTHING;

-- Link Admin to ADMIN role
INSERT INTO user_roles (user_id, role_id) VALUES (
  '19944722-fb48-4a6d-b292-ca3ae4719828',
  '00000000-0000-0000-0000-000000000001'
)
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Seed Specialties
INSERT INTO specialties (name, created_by_user) VALUES 
('CLINICA_GERAL', '19944722-fb48-4a6d-b292-ca3ae4719828'), 
('PEDIATRIA', '19944722-fb48-4a6d-b292-ca3ae4719828'), 
('ORTOPEDIA', '19944722-fb48-4a6d-b292-ca3ae4719828'), 
('CARDIOLOGIA', '19944722-fb48-4a6d-b292-ca3ae4719828'), 
('GINECOLOGIA', '19944722-fb48-4a6d-b292-ca3ae4719828')
ON CONFLICT (name) DO NOTHING;

