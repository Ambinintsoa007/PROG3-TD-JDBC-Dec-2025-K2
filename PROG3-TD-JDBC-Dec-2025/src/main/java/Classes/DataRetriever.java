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
}
