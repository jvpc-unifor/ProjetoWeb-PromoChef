CREATE DATABASE IF NOT EXISTS promochef_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE promochef_db;

-- Tabela do usuário;
CREATE TABLE IF NOT EXISTS tb_usuario (
    id  BIGINT  NOT NULL AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    email   VARCHAR(150) NOT NULL,
    senha_hash  VARCHAR(255) NOT NULL,
    tipo    ENUM('ADMIN', 'GERENTE') NOT NULL,
    ativo   BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uq_usuario_email UNIQUE (email)
);

-- alimentar a tabela de usuário, a seed.
-- Senhas admin123 e gerente123 pelo https://bcrypt-generator.com/ com base 12;
INSERT INTO tb_usuario (nome, email, senha_hash, tipo, ativo)
VALUES ('Administrador',
        'admin@promochef.com',
        '$2a$12$CM.GgCRTnE/R8bdrBTUyLez0dOztEHzbsOACYBvVeQwxcG0Scvawu',
        'ADMIN',
        TRUE),
       ('Gerente test',
        'gerente@promochef.com',
        '$2a$12$tHAF6hTUBZTwhobYs7.RQ.SkmmR327d/0l8t1WHLp/cpecKZb9sea',
        'GERENTE',
        TRUE);

SELECT * FROM tb_usuario;
-- DELETE FROM tb_usuario;