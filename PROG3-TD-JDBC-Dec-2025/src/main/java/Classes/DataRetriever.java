package Classes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    public Dish findDishById(Integer id) throws SQLException {
        String dishQuery = "SELECT id, name, dish_type FROM Dish WHERE id = ?";
        String ingredientsQuery = "SELECT id, name, price, category, id_dish FROM Ingredient WHERE id_dish = ?";

        Connection conn = new DBConnection().getDBConnection();
        PreparedStatement dishStmt = conn.prepareStatement(dishQuery);
        dishStmt.setInt(1, id);
        ResultSet dishRs = dishStmt.executeQuery();

        if (!dishRs.next()) {
            dishRs.close();
            dishStmt.close();
            conn.close();
            return null;
        }

        int dishId = dishRs.getInt("id");
        String dishName = dishRs.getString("name");
        DishTypeEnum dishType = DishTypeEnum.valueOf(dishRs.getString("dish_type"));

        dishRs.close();
        dishStmt.close();

        PreparedStatement ingredientsStmt = conn.prepareStatement(ingredientsQuery);
        ingredientsStmt.setInt(1, id);
        ResultSet ingredientsRs = ingredientsStmt.executeQuery();

        List<Ingredient> ingredients = new ArrayList<>();

        while (ingredientsRs.next()) {
            Ingredient ingredient = new Ingredient(
                    ingredientsRs.getInt("id"),
                    ingredientsRs.getString("name"),
                    ingredientsRs.getDouble("price"),
                    CategoryEnum.valueOf(ingredientsRs.getString("category")),
                    null
            );
            ingredients.add(ingredient);
        }

        ingredientsRs.close();
        ingredientsStmt.close();
        conn.close();

        return new Dish(dishId, dishName, dishType, ingredients);
    }

    public List<Ingredient> findIngredients(int page, int size) throws SQLException {
        String query = "SELECT id, name, price, category, id_dish FROM Ingredient ORDER BY id LIMIT ? OFFSET ?";

        Connection connection = new DBConnection().getDBConnection();
        PreparedStatement pstmt = connection.prepareStatement(query);

        int offset = (page - 1) * size;
        pstmt.setInt(1, size);
        pstmt.setInt(2, offset);

        ResultSet rs = pstmt.executeQuery();
        List<Ingredient> ingredients = new ArrayList<>();

        while (rs.next()) {
            Ingredient ingredient = new Ingredient(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    CategoryEnum.valueOf(rs.getString("category")),
                    null
            );
            ingredients.add(ingredient);
        }

        rs.close();
        pstmt.close();
        connection.close();

        return ingredients;
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) throws SQLException {
        Connection connection = new DBConnection().getDBConnection();

        try {
            connection.setAutoCommit(false);

            String checkQuery = "SELECT COUNT(*) FROM Ingredient WHERE id = ?";
            String insertQuery = "INSERT INTO Ingredient (id, name, price, category, id_dish) VALUES (?, ?, ?, ?::ingredient_category, ?)";

            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            PreparedStatement insertStmt = connection.prepareStatement(insertQuery);

            List<Ingredient> createdIngredients = new ArrayList<>();

            for (Ingredient ingredient : newIngredients) {
                checkStmt.setInt(1, ingredient.getId());
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                int count = rs.getInt(1);
                rs.close();

                if (count > 0) {
                    throw new RuntimeException("Ingredient avec id " + ingredient.getId() + " existe déjà");
                }

                insertStmt.setInt(1, ingredient.getId());
                insertStmt.setString(2, ingredient.getName());
                insertStmt.setDouble(3, ingredient.getPrice());
                insertStmt.setString(4, ingredient.getCategory().name());

                if (ingredient.getDish() != null) {
                    insertStmt.setInt(5, ingredient.getDish().getId());
                } else {
                    insertStmt.setNull(5, java.sql.Types.INTEGER);
                }

                insertStmt.executeUpdate();
                createdIngredients.add(ingredient);
            }

            connection.commit();

            checkStmt.close();
            insertStmt.close();
            connection.close();

            return createdIngredients;

        } catch (Exception e) {
            connection.rollback();
            connection.close();
            throw new RuntimeException("Erreur lors de la création: " + e.getMessage());
        }
    }

    public Dish saveDish(Dish dishToSave) throws SQLException {
        Connection conn = new DBConnection().getDBConnection();

        try {
            conn.setAutoCommit(false);

            String checkQuery = "SELECT COUNT(*) FROM Dish WHERE id = ?";
            String insertDishQuery = "INSERT INTO Dish (id, name, dish_type) VALUES (?, ?, ?::dish_type_enum)";
            String updateDishQuery = "UPDATE Dish SET name = ?, dish_type = ?::dish_type_enum WHERE id = ?";
            String updateIngredientsQuery = "UPDATE Ingredient SET id_dish = ? WHERE id = ?";
            String dissociateQuery = "UPDATE Ingredient SET id_dish = NULL WHERE id_dish = ? AND id NOT IN (";

            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, dishToSave.getId());
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            rs.close();
            checkStmt.close();

            if (count == 0) {
                PreparedStatement insertStmt = conn.prepareStatement(insertDishQuery);
                insertStmt.setInt(1, dishToSave.getId());
                insertStmt.setString(2, dishToSave.getNom());
                insertStmt.setString(3, dishToSave.getType().name());
                insertStmt.executeUpdate();
                insertStmt.close();
            } else {
                PreparedStatement updateStmt = conn.prepareStatement(updateDishQuery);
                updateStmt.setString(1, dishToSave.getNom());
                updateStmt.setString(2, dishToSave.getType().name());
                updateStmt.setInt(3, dishToSave.getId());
                updateStmt.executeUpdate();
                updateStmt.close();
            }

            if (dishToSave.getIngredient() != null && !dishToSave.getIngredient().isEmpty()) {
                PreparedStatement updateIngrStmt = conn.prepareStatement(updateIngredientsQuery);

                StringBuilder dissociateBuilder = new StringBuilder(dissociateQuery);
                for (int i = 0; i < dishToSave.getIngredient().size(); i++) {
                    if (i > 0) dissociateBuilder.append(",");
                    dissociateBuilder.append("?");
                }
                dissociateBuilder.append(")");

                PreparedStatement dissociateStmt = conn.prepareStatement(dissociateBuilder.toString());
                dissociateStmt.setInt(1, dishToSave.getId());

                int index = 2;
                for (Ingredient ingredient : dishToSave.getIngredient()) {
                    updateIngrStmt.setInt(1, dishToSave.getId());
                    updateIngrStmt.setInt(2, ingredient.getId());
                    updateIngrStmt.executeUpdate();

                    dissociateStmt.setInt(index++, ingredient.getId());
                }

                dissociateStmt.executeUpdate();
                dissociateStmt.close();
                updateIngrStmt.close();
            } else {
                PreparedStatement dissociateAllStmt = conn.prepareStatement("UPDATE Ingredient SET id_dish = NULL WHERE id_dish = ?");
                dissociateAllStmt.setInt(1, dishToSave.getId());
                dissociateAllStmt.executeUpdate();
                dissociateAllStmt.close();
            }

            conn.commit();
            conn.close();

            return dishToSave;

        } catch (Exception e) {
            conn.rollback();
            conn.close();
            throw new SQLException("Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    public List<Dish> findDishsByIngredientName(String ingredientName) throws SQLException {
        String query = "SELECT DISTINCT d.id, d.name, d.dish_type " +
                "FROM Dish d " +
                "JOIN Ingredient i ON d.id = i.id_dish " +
                "WHERE i.name LIKE ?";

        Connection connection = new DBConnection().getDBConnection();
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, "%" + ingredientName + "%");

        ResultSet rs = stmt.executeQuery();
        List<Dish> dishes = new ArrayList<>();

        while (rs.next()) {
            int dishId = rs.getInt("id");
            String dishName = rs.getString("name");
            DishTypeEnum dishType = DishTypeEnum.valueOf(rs.getString("dish_type"));

            String ingredientsQuery = "SELECT * FROM Ingredient WHERE id_dish = ?";
            PreparedStatement ingredientsStmt = connection.prepareStatement(ingredientsQuery);
            ingredientsStmt.setInt(1, dishId);
            ResultSet ingredientsRs = ingredientsStmt.executeQuery();

            List<Ingredient> ingredients = new ArrayList<>();
            while (ingredientsRs.next()) {
                Ingredient ingredient = new Ingredient(
                        ingredientsRs.getInt("id"),
                        ingredientsRs.getString("name"),
                        ingredientsRs.getDouble("price"),
                        CategoryEnum.valueOf(ingredientsRs.getString("category")),
                        null
                );
                ingredients.add(ingredient);
            }

            ingredientsRs.close();
            ingredientsStmt.close();

            Dish dish = new Dish(dishId, dishName, dishType, ingredients);
            dishes.add(dish);
        }

        rs.close();
        stmt.close();
        connection.close();

        return dishes;
    }

    public List<Ingredient> findIngredientsByCriteria(String ingredientName, CategoryEnum category, String dishName, int page, int size) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT i.id, i.name, i.price, i.category, i.id_dish FROM Ingredient i ");
        List<String> conditions = new ArrayList<>();

        if (dishName != null && !dishName.isEmpty()) {
            query.append("JOIN Dish d ON i.id_dish = d.id ");
        }

        query.append("WHERE 1=1 ");

        if (ingredientName != null && !ingredientName.isEmpty()) {
            conditions.add("i.name LIKE ?");
        }

        if (category != null) {
            conditions.add("i.category = ?::ingredient_category");
        }

        if (dishName != null && !dishName.isEmpty()) {
            conditions.add("d.name LIKE ?");
        }

        for (String condition : conditions) {
            query.append("AND ").append(condition).append(" ");
        }

        query.append("ORDER BY i.id LIMIT ? OFFSET ?");

        Connection connection = new DBConnection().getDBConnection();
        PreparedStatement stmt = connection.prepareStatement(query.toString());

        int paramtIndex = 1;

        if (ingredientName != null && !ingredientName.isEmpty()) {
            stmt.setString(paramtIndex++, "%" + ingredientName + "%");
        }

        if (category != null) {
            stmt.setString(paramtIndex++, category.name());
        }

        if (dishName != null && !dishName.isEmpty()) {
            stmt.setString(paramtIndex++, "%" + dishName + "%");
        }

        int offset = (page - 1) * size;
        stmt.setInt(paramtIndex++, size);
        stmt.setInt(paramtIndex, offset);

        ResultSet rs = stmt.executeQuery();
        List<Ingredient> ingredients = new ArrayList<>();

        while (rs.next()) {
            Ingredient ingredient = new Ingredient(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    CategoryEnum.valueOf(rs.getString("category")),
                    null
            );
            ingredients.add(ingredient);
        }

        rs.close();
        stmt.close();
        connection.close();

        return ingredients;
    }
}
