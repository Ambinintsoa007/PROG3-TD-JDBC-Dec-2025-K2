-- Création de l'enum movement_type
CREATE TYPE movement_type AS ENUM ('IN', 'OUT');

-- Création de la table StockMovement
CREATE TABLE stockmovement (
                               id SERIAL PRIMARY KEY,
                               id_ingredient INT NOT NULL,
                               quantity NUMERIC(10,2) NOT NULL,
                               type movement_type NOT NULL,
                               unit unit_type NOT NULL,
                               creation_datetime TIMESTAMP NOT NULL,
                               CONSTRAINT fk_ingredient FOREIGN KEY (id_ingredient) REFERENCES ingredient(id)
);

-- Insertion des données StockMovement (d'après l'image 3)
INSERT INTO stockmovement (id, id_ingredient, quantity, type, unit, creation_datetime) VALUES
    (1, 1, 5.0, 'IN', 'KG', '2024-01-05 08:00:00'),
    (2, 1, 0.2, 'OUT', 'KG', '2024-01-06 12:00:00'),
    (3, 2, 4.0, 'IN', 'KG', '2024-01-05 08:00:00'),
    (4, 2, 0.15, 'OUT', 'KG', '2024-01-06 12:00:00'),
    (5, 3, 10.0, 'IN', 'KG', '2024-01-04 09:00:00'),
    (6, 3, 1.0, 'OUT', 'KG', '2024-01-06 13:00:00'),
    (7, 4, 3.0, 'IN', 'KG', '2024-01-05 10:00:00'),
    (8, 4, 0.3, 'OUT', 'KG', '2024-01-06 14:00:00'),
    (9, 5, 2.5, 'IN', 'KG', '2024-01-05 10:00:00'),
    (10, 5, 0.2, 'OUT', 'KG', '2024-01-06 14:00:00');