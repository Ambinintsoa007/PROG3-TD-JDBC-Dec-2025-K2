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
}
