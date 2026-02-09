-- Enable pgcrypto extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    level INTEGER NOT NULL,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by_user UUID,
    updated_by_user UUID
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    cpf_cnpj VARCHAR(14) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by_user UUID,
    updated_by_user UUID
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    street VARCHAR(255) NOT NULL,
    number VARCHAR(50) NOT NULL,
    complement VARCHAR(100),
    neighborhood VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(2) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by_user UUID,
    updated_by_user UUID
);

CREATE TABLE health_units (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    address_id UUID NOT NULL UNIQUE,
    cnpj VARCHAR(18) UNIQUE,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by_user UUID,
    updated_by_user UUID,
    CONSTRAINT fk_unit_address FOREIGN KEY (address_id) REFERENCES addresses (id)
);

CREATE TABLE health_unit_contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(20) NOT NULL,
    value VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    unit_id UUID NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by_user UUID,
    updated_by_user UUID,
    CONSTRAINT fk_contact_unit FOREIGN KEY (unit_id) REFERENCES health_units(id)
);

CREATE TABLE specialties (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by_user UUID,
    updated_by_user UUID
);

CREATE TABLE shifts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unit_id UUID NOT NULL,
    specialty_id UUID NOT NULL,
    capacity INTEGER NOT NULL,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by_user UUID,
    updated_by_user UUID,
    CONSTRAINT fk_shift_unit FOREIGN KEY (unit_id) REFERENCES health_units (id),
    CONSTRAINT fk_shift_specialty FOREIGN KEY (specialty_id) REFERENCES specialties (id)
);

CREATE TABLE doctors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    crm VARCHAR(50) NOT NULL UNIQUE,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by_user UUID,
    updated_by_user UUID
);

CREATE TABLE doctors_specialties (
    doctor_id UUID NOT NULL,
    specialty_id UUID NOT NULL,
    PRIMARY KEY (doctor_id, specialty_id),
    CONSTRAINT fk_doctors_spec_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_doctors_spec_specialty FOREIGN KEY (specialty_id) REFERENCES specialties (id)
);

CREATE TABLE shifts_doctors (
    shift_id UUID NOT NULL,
    doctor_id UUID NOT NULL,
    PRIMARY KEY (shift_id, doctor_id),
    CONSTRAINT fk_shifts_doctors_shift FOREIGN KEY (shift_id) REFERENCES shifts (id),
    CONSTRAINT fk_shifts_doctors_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id)
);

CREATE TABLE user_units (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    unit_id UUID NOT NULL,
    role_id UUID NOT NULL,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by_user UUID,
    updated_by_user UUID,
    CONSTRAINT fk_user_unit_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_unit_unit FOREIGN KEY (unit_id) REFERENCES health_units (id),
    CONSTRAINT fk_user_unit_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE INDEX idx_addresses_lat_long ON addresses (latitude, longitude);
CREATE INDEX idx_health_units_name ON health_units (name);
CREATE INDEX idx_health_units_cnpj ON health_units (cnpj);
