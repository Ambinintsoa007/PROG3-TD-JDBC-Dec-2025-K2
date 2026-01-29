create type unit_type as enum (
    'PCS',
    'KG',
    'L'
);

create table dish_ingredient (
    id SERIAL primary key,
    id_dish int,
    id_ingredient int,
    quantity_required numeric(10,2),
    unit unit_type
);

alter table ingredient drop column id_dish;

alter table ingredient drop column required_quantity;

INSERT INTO dish_ingredient (id_dish, id_ingredient, quantity_required, unit)
VALUES
    (1, 1, 0.20, 'KG'),
    (1, 2, 0.15, 'KG'),
    (2, 3, 1.00, 'KG'),
    (4, 4, 0.30, 'KG'),
    (4, 5, 0.20, 'KG');

alter table dish add column selling_price numeric(10,2);

UPDATE dish SET selling_price = 3500.00 WHERE id = 1;
UPDATE dish SET selling_price = 12000.00 WHERE id = 2;
UPDATE dish SET selling_price = NULL WHERE id = 3;
UPDATE dish SET selling_price = 8000.00 WHERE id = 4;
UPDATE dish SET selling_price = NULL WHERE id = 5;