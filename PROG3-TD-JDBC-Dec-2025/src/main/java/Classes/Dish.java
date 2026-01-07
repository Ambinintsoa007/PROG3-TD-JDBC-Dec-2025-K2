package Classes;
import lombok.*;

import java.util.List;

@Getter
@EqualsAndHashCode
@Setter

public class Dish {
    private int id;
    private String nom;
    private DishTypeEnum type;
    private List<Ingredient> ingredient;

    public Dish(int id, String nom, DishTypeEnum type, List<Ingredient> ingredient) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.ingredient = ingredient;
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", type=" + type +
                ", ingredient=" + ingredient +
                '}';
    }

    /*public double getDishCost() {
        double totalCost = 0;
        if (this.ingredient != null) {
            for (Ingredient ingredient : this.ingredient) {
                totalCost += ingredient.getPrice();
            }
        }
        return totalCost;
    }*/
    public Double getDishCost() {
        if (this.ingredient == null || this.ingredient.isEmpty()) {
            return 0.0;
        }

        double totalCost = 0.0;

        for (Ingredient ingredient : this.ingredient) {
            if (ingredient.getRequiredQuantity() == null) {
                throw new RuntimeException("Impossible de calculer le coût : la quantité nécessaire de l'ingrédient '"
                        + ingredient.getName() + "' est inconnue");
            }
            totalCost += ingredient.getPrice() * ingredient.getRequiredQuantity();
        }

        return totalCost;
    }
}
