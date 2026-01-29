package Classes;

import Classes.Dish;

public class Main {
    public static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();
        Dish saladeVerte = dataRetriever.findDishById(1);
        System.out.println(saladeVerte);

        Dish poulet = dataRetriever.findDishById(2);
        System.out.println(poulet);

        Dish rizLegume = dataRetriever.findDishById(3);
        rizLegume.setPrice(100.0);
        Dish newRizLegume = dataRetriever.saveDish(rizLegume);
        System.out.println(newRizLegume); // Should not throw exception


//        Dish rizLegumeAgain = dataRetriever.findDishById(3);
//        rizLegumeAgain.setPrice(null);
//        Dish savedNewRizLegume = dataRetriever.saveDish(rizLegume);
//        System.out.println(savedNewRizLegume); // Should throw exception

        Ingredient laitue = dataRetriever.findIngredientById(1);
        System.out.println(laitue);

    }
}
