package Classes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TestStockMovement {
    public static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();

        // Instant de test : 2024-01-06 12:00:00
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 6, 12, 0, 0);
        Instant testInstant = dateTime.toInstant(ZoneOffset.UTC);

        System.out.println("=== Test Stock Values at " + dateTime + " ===\n");

        // Tester pour les ingrédients 1 à 5
        for (int i = 1; i <= 5; i++) {
            try {
                Ingredient ingredient = dataRetriever.findIngredientById(i);
                StockValue stockValue = ingredient.getStockValueAt(testInstant);

                System.out.println("Ingredient " + i + " (" + ingredient.getName() + "):");
                System.out.println("  Stock: " + stockValue.getQuantity() + " " + stockValue.getUnit());
                System.out.println();
            } catch (Exception e) {
                System.out.println("Erreur pour ingredient " + i + ": " + e.getMessage());
            }
        }

        System.out.println("\n=== Résultats Attendus ===");
        System.out.println("Ingredient 1 (Laitue): 4.8 KG");
        System.out.println("Ingredient 2 (Tomate): 3.85 KG");
        System.out.println("Ingredient 3 (Poulet): 9.0 KG");
        System.out.println("Ingredient 4 (Carotte): 2.7 KG");
        System.out.println("Ingredient 5 (Oignon): 2.3 KG");
    }
}