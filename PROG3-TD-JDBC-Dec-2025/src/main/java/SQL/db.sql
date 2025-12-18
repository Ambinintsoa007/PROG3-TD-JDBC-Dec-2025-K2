CREATE DATABASE mini_dish_db;

CREATE USER mini_dish_db_manager WITH PASSWORD '123456';

--autoriser la connexion à la base
GRANT CONNECT ON DATABASE mini_dish_db TO mini_dish_db_manager;

-- \c mini_dish_db

--Accès au schéma + droit de créer tables/types
GRANT CREATE ON SCHEMA public TO mini_dish_db_manager;
--Droits CRUD sur toutes les tables existantes
GRANT
      SELECT, INSERT, UPDATE, DELETE
ON ALL TABLES IN SCHEMA public TO mini_dish_db_manager;
--Accorder les privilèges CRUD sur toutes les futures tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO mini_dish_db_manager;

--Accorder l'usage des séquences
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO mini_dish_db_manager;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO mini_dish_db_manager;