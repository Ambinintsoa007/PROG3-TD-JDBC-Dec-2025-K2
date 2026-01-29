import Classes.DataRetriever;
import Classes.Dish;
import Classes.Ingredient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class DataRetrieverTest {
    @BeforeAll
    public static void setup() {
        System.setProperty("JDBC_URL", "jdbc:postgresql://localhost:5432/mini_dish_db");
        System.setProperty("USERNAME", "mini_dish_db_manager");
        System.setProperty("PASSWORD", "ton_mot_de_passe");
    }


}
