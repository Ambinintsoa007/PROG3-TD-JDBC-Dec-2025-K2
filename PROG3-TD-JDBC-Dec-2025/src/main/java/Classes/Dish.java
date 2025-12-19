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

    public double getDishPrice() {
        throw new RuntimeException("Not implemented yet");
    }
}
