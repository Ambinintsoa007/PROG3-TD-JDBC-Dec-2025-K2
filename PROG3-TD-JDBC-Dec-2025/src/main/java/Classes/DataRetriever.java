package Classes;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    Dish findDishById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        try {
            String sql = """
                    SELECT dish.id as dish_id, 
                           dish.name as dish_name, 
                           dish_type, 
                           dish.selling_price as dish_price
                    FROM dish
                    WHERE dish.id = ?;
                    """;

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Dish dish = new Dish();
                dish.setId(resultSet.getInt("dish_id"));
                dish.setName(resultSet.getString("dish_name"));
                dish.setDishType(DishTypeEnum.valueOf(resultSet.getString("dish_type")));
                dish.setPrice(resultSet.getObject("dish_price") == null
                        ? null : resultSet.getDouble("dish_price"));

                //  récupération via la table de jointure
                dish.setIngredients(findIngredientByDishId(id));

                dbConnection.closeConnection(connection);
                return dish;
            }

            dbConnection.closeConnection(connection);
            throw new RuntimeException("Dish not found " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    Dish saveDish(Dish toSave) {
        String upsertDishSql = """
                INSERT INTO dish (id, selling_price, name, dish_type)
                VALUES (?, ?, ?, ?::dish_type_enum)
                ON CONFLICT (id) DO UPDATE
                SET name = EXCLUDED.name,
                    dish_type = EXCLUDED.dish_type,
                    selling_price = EXCLUDED.selling_price
                RETURNING id
                """;

        try (Connection conn = new DBConnection().getConnection()) {
            conn.setAutoCommit(false);
            Integer dishId;

            try (PreparedStatement ps = conn.prepareStatement(upsertDishSql)) {
                if (toSave.getId() != null) {
                    ps.setInt(1, toSave.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "dish", "id"));
                }

                if (toSave.getPrice() != null) {
                    ps.setDouble(2, toSave.getPrice());
                } else {
                    ps.setNull(2, Types.DOUBLE);
                }

                ps.setString(3, toSave.getName());
                ps.setString(4, toSave.getDishType().name());

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    dishId = rs.getInt(1);
                }
            }

            //  gestion via la table de jointure DishIngredient
            List<Ingredient> newIngredients = toSave.getIngredients();
            detachIngredients(conn, dishId, newIngredients);
            attachIngredients(conn, dishId, newIngredients);

            conn.commit();
            return findDishById(dishId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //  Récupère les ingrédients d'un plat via la table de jointure
    private List<Ingredient> findIngredientByDishId(Integer idDish) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        List<Ingredient> ingredients = new ArrayList<>();

        try {
            //  JOIN avec dishIngredient au lieu de WHERE id_dish = ?
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                    SELECT i.id, i.name, i.price, i.category, 
                           di.quantity_required, di.unit
                    FROM ingredient i
                    INNER JOIN dishIngredient di ON i.id = di.id_ingredient
                    WHERE di.id_dish = ?;
                    """);
            preparedStatement.setInt(1, idDish);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(resultSet.getInt("id"));
                ingredient.setName(resultSet.getString("name"));
                ingredient.setPrice(resultSet.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));

                // récupération de quantity et unit depuis dishIngredient
                ingredient.setQuantity(resultSet.getDouble("quantity_required"));
                ingredient.setUnit(UnitTypeEnum.valueOf(resultSet.getString("unit")));

                ingredients.add(ingredient);
            }

            dbConnection.closeConnection(connection);
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //  Crée des ingrédients (sans lien avec plat)
    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        if (newIngredients == null || newIngredients.isEmpty()) {
            return List.of();
        }

        List<Ingredient> savedIngredients = new ArrayList<>();
        DBConnection dbConnection = new DBConnection();
        Connection conn = dbConnection.getConnection();

        try {
            conn.setAutoCommit(false);

            //  Plus de required_quantity dans ingredient
            String insertSql = """
                    INSERT INTO ingredient (id, name, category, price)
                    VALUES (?, ?, ?::ingredient_category, ?)
                    RETURNING id
                    """;

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (Ingredient ingredient : newIngredients) {
                    if (ingredient.getId() != null) {
                        ps.setInt(1, ingredient.getId());
                    } else {
                        ps.setInt(1, getNextSerialValue(conn, "ingredient", "id"));
                    }
                    ps.setString(2, ingredient.getName());
                    ps.setString(3, ingredient.getCategory().name());
                    ps.setDouble(4, ingredient.getPrice());

                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        int generatedId = rs.getInt(1);
                        ingredient.setId(generatedId);
                        savedIngredients.add(ingredient);
                    }
                }
                conn.commit();
                return savedIngredients;
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    // detachIngredients maintenant gère la table dishIngredient
    private void detachIngredients(Connection conn, Integer dishId, List<Ingredient> ingredients)
            throws SQLException {

        if (ingredients == null || ingredients.isEmpty()) {
            // Supprimer toutes les relations pour ce plat
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM dishIngredient WHERE id_dish = ?")) {
                ps.setInt(1, dishId);
                ps.executeUpdate();
            }
            return;
        }

        // Supprimer les relations qui ne sont plus dans la liste
        String baseSql = """
                DELETE FROM dishIngredient
                WHERE id_dish = ? AND id_ingredient NOT IN (%s)
                """;

        String inClause = ingredients.stream()
                .map(i -> "?")
                .collect(java.util.stream.Collectors.joining(","));

        String sql = String.format(baseSql, inClause);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dishId);
            int index = 2;
            for (Ingredient ingredient : ingredients) {
                ps.setInt(index++, ingredient.getId());
            }
            ps.executeUpdate();
        }
    }

    //  attachIngredients maintenant insère/met à jour dans dishIngredient
    private void attachIngredients(Connection conn, Integer dishId, List<Ingredient> ingredients)
            throws SQLException {

        if (ingredients == null || ingredients.isEmpty()) {
            return;
        }

        // UPSERT dans dishIngredient avec quantity_required et unit
        String upsertSql = """
                INSERT INTO dishIngredient (id, id_dish, id_ingredient, quantity_required, unit)
                VALUES (?, ?, ?, ?, ?::unit_type)
                ON CONFLICT (id_dish, id_ingredient) 
                DO UPDATE SET 
                    quantity_required = EXCLUDED.quantity_required,
                    unit = EXCLUDED.unit
                """;

        try (PreparedStatement ps = conn.prepareStatement(upsertSql)) {
            for (Ingredient ingredient : ingredients) {
                ps.setInt(1, getNextSerialValue(conn, "dishingredient", "id"));
                ps.setInt(2, dishId);
                ps.setInt(3, ingredient.getId());
                ps.setDouble(4, ingredient.getQuantity());
                ps.setString(5, ingredient.getUnit().name());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    DishIngredient findDishIngredientById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();

        try {
            String sql = """
                    SELECT id, id_dish, id_ingredient, quantity_required, unit
                    FROM dishIngredient
                    WHERE id = ?;
                    """;

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                DishIngredient dishIngredient = new DishIngredient();
                dishIngredient.setId(resultSet.getInt("id"));
                dishIngredient.setIdDish(resultSet.getInt("id_dish"));
                dishIngredient.setIdIngredient(resultSet.getInt("id_ingredient"));
                dishIngredient.setQuantityRequired(resultSet.getDouble("quantity_required"));
                dishIngredient.setUnit(UnitTypeEnum.valueOf(resultSet.getString("unit")));

                dbConnection.closeConnection(connection);
                return dishIngredient;
            }

            dbConnection.closeConnection(connection);
            throw new RuntimeException("DishIngredient not found " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    DishIngredient saveDishIngredient(DishIngredient toSave) {
        String upsertDishIngredientSql = """
                INSERT INTO dishIngredient (id, id_dish, id_ingredient, quantity_required, unit)
                VALUES (?, ?, ?, ?, ?::unit_type)
                ON CONFLICT (id) DO UPDATE
                SET id_dish = EXCLUDED.id_dish,
                    id_ingredient = EXCLUDED.id_ingredient,
                    quantity_required = EXCLUDED.quantity_required,
                    unit = EXCLUDED.unit
                RETURNING id
                """;

        try (Connection conn = new DBConnection().getConnection()) {
            conn.setAutoCommit(false);
            Integer dishIngredientId;

            try (PreparedStatement ps = conn.prepareStatement(upsertDishIngredientSql)) {
                if (toSave.getId() != null) {
                    ps.setInt(1, toSave.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "dishingredient", "id"));
                }

                ps.setInt(2, toSave.getIdDish());
                ps.setInt(3, toSave.getIdIngredient());
                ps.setDouble(4, toSave.getQuantityRequired());
                ps.setString(5, toSave.getUnit().name());

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    dishIngredientId = rs.getInt(1);
                }
            }

            conn.commit();
            return findDishIngredientById(dishIngredientId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    List<DishIngredient> findDishIngredientsByDishId(Integer dishId) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        List<DishIngredient> dishIngredients = new ArrayList<>();

        try {
            String sql = """
                    SELECT id, id_dish, id_ingredient, quantity_required, unit
                    FROM dishIngredient
                    WHERE id_dish = ?;
                    """;

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, dishId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                DishIngredient dishIngredient = new DishIngredient();
                dishIngredient.setId(resultSet.getInt("id"));
                dishIngredient.setIdDish(resultSet.getInt("id_dish"));
                dishIngredient.setIdIngredient(resultSet.getInt("id_ingredient"));
                dishIngredient.setQuantityRequired(resultSet.getDouble("quantity_required"));
                dishIngredient.setUnit(UnitTypeEnum.valueOf(resultSet.getString("unit")));

                dishIngredients.add(dishIngredient);
            }

            dbConnection.closeConnection(connection);
            return dishIngredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getSerialSequenceName(Connection conn, String tableName, String columnName)
            throws SQLException {
        String sql = "SELECT pg_get_serial_sequence(?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    private int getNextSerialValue(Connection conn, String tableName, String columnName)
            throws SQLException {
        String sequenceName = getSerialSequenceName(conn, tableName, columnName);

        if (sequenceName == null) {
            throw new IllegalArgumentException(
                    "Any sequence found for " + tableName + "." + columnName
            );
        }

        updateSequenceNextValue(conn, tableName, columnName, sequenceName);

        String nextValSql = "SELECT nextval(?)";

        try (PreparedStatement ps = conn.prepareStatement(nextValSql)) {
            ps.setString(1, sequenceName);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void updateSequenceNextValue(Connection conn, String tableName, String columnName, String sequenceName)
            throws SQLException {
        String setValSql = String.format(
                "SELECT setval('%s', (SELECT COALESCE(MAX(%s), 0) FROM %s))",
                sequenceName, columnName, tableName
        );

        try (PreparedStatement ps = conn.prepareStatement(setValSql)) {
            ps.executeQuery();
        }
    }
}