package Classes;

import lombok.*;

import java.util.List;

@Getter
@ToString
@EqualsAndHashCode
@Setter
@AllArgsConstructor

public class Dish {
    private int id;
    private String nom;
    private DishTypeEnum type;
    private List<Ingredient> ingredient;

    public double getDishPrice() {
        throw new RuntimeException("Not implemented yet");
    }
}
