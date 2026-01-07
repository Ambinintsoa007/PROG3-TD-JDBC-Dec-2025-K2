package Classes;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            DataRetriever retriever = new DataRetriever();

            System.out.println("\n TEST 1: findDishById avec getDishCost ");

            System.out.println(" ***Plat avec quantités définies*** ");
            try {
                Dish dish1 = retriever.findDishById(1);
                System.out.println("- Plat: " + dish1.toString());

                double cost = dish1.getDishCost();
                System.out.println("- Coût total du plat: " + cost);
            } catch (RuntimeException e) {
                System.out.println("Exception levée: " + e.getMessage());
            }



            System.out.println(" ***Plat avec quantité inconnue*** ");
            try {
                Dish dish4 = retriever.findDishById(4);
                System.out.println("- Plat: " + dish4.getNom());
                System.out.println("- Ingrédients:");
                for (Ingredient ing : dish4.getIngredient()) {
                    System.out.println("  - " + ing.getName() +
                            " (prix: " + ing.getPrice() +
                            ", quantité: " + ing.getRequiredQuantity() + ")");
                }
                double cost = dish4.getDishCost();
                System.out.println("Coût total du plat: " + cost);
            } catch (RuntimeException e) {
                System.out.println("Exception levée: " + e.getMessage());
            }

            System.out.println("\n TEST 2: saveDish ");

            List<Ingredient> newIngredients = new ArrayList<>();
            Ingredient ing1 = new Ingredient(1, "Laitue", 800.0, CategoryEnum.VEGETABLE, null, 1.0);
            newIngredients.add(ing1);

            Dish newDish = new Dish(20, "Nouvelle salade rouge test", DishTypeEnum.START, newIngredients);
            Dish savedDish = retriever.saveDish(newDish);

            System.out.println("- Plat créé: " + savedDish.getNom());
            System.out.println("- ID: " + savedDish.getId());
            System.out.println("- Coût du plat: " + savedDish.getDishCost());

            System.out.println("\n TEST 3: saveDish ");



            Dish updatedDish = new Dish(20, "Salade modifiée", DishTypeEnum.START, newIngredients);
            Dish savedUpdatedDish = retriever.saveDish(updatedDish);

            System.out.println("- Plat mis à jour: " + savedUpdatedDish.getNom());
            System.out.println("- ID: " + savedUpdatedDish.getId());
            System.out.println("- Coût du plat: " + savedUpdatedDish.getDishCost());


            Dish verifyDish = retriever.findDishById(20);
            System.out.println("--Vérification depuis la base:");
            System.out.println("--Nom: " + verifyDish.getNom());
            System.out.println("--Coût: " + verifyDish.getDishCost());

        } catch (SQLException e) {
            throw new RuntimeException("sql error : " + e.getMessage());
        }
    }
}