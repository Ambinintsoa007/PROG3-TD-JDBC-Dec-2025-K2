CREATE TYPE ingredient_category AS ENUM (
  'VEGETABLE',
  'ANIMAL',
  'MARINE',
  'DAIRY',
  'OTHER'
);

CREATE TYPE dish_type_enum AS ENUM (
  'START',
  'MAIN',
  'DESSERT'
);

CREATE TABLE dish (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    dish_type dish_type_enum NOT NULL
);

CREATE TABLE ingredient (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    category ingredient_category NOT NULL,
    id_dish INT,
    CONSTRAINT fk_dish
    FOREIGN KEY (id_dish)
    REFERENCES dish(id)
    ON DELETE SET NULL
);
