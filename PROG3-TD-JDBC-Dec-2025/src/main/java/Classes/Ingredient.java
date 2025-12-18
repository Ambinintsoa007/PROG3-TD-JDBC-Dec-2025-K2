package Classes;

import lombok.Getter;

@Getter
public class Ingredient {
    private int id;
    private String name;
    private CategoryEnum category;
    private Dish dish;

    public Ingredient(int id, String name, CategoryEnum category, Dish dish) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.dish = dish;
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", dish=" + dish +
                '}';
    }

    public String getDishName() {
        throw new RuntimeException("Not implemented yet");
    }
}
