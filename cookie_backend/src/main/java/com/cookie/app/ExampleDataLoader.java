package com.cookie.app;

import com.cookie.app.model.entity.Product;
import com.cookie.app.model.entity.Recipe;
import com.cookie.app.model.entity.RecipeProduct;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.enums.Category;
import com.cookie.app.model.enums.MealType;
import com.cookie.app.model.enums.Unit;
import com.cookie.app.model.request.RegistrationRequest;
import com.cookie.app.repository.RecipeRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.LoginService;
import com.cookie.app.util.ImageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Component
@Profile("test")
public class ExampleDataLoader implements CommandLineRunner {
    private final UserRepository userRepository;
    private final LoginService loginService;
    private final RecipeRepository recipeRepository;

    @Override
    public void run(String... args) throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "testUser",
                "test@gmail.com",
                "ZAQ!2wsx",
                LocalDate.now().minusYears(20),
                null
        );
        this.loginService.userRegistration(request);
        User user = this.userRepository.findByUsername("testUser").get();
        ClassLoader classLoader = getClass().getClassLoader();

        Product product = new Product(0L, "Ready-made pizza crust", Category.BREAD_AND_BAKERY);
        RecipeProduct recipeProduct = new RecipeProduct(0L, product, 1, Unit.PIECES);
        Product product2 = new Product(0L, "Tomato Sauce", Category.CANNED_GOODS);
        RecipeProduct recipeProduct2 = new RecipeProduct(0L, product2, 100, Unit.GRAMS);
        Product product3 = new Product(0L, "Mozzarella cheese", Category.DAIRY);
        RecipeProduct recipeProduct3 = new RecipeProduct(0L, product3, 100, Unit.GRAMS);
        Product product4 = new Product(0L, "Salami", Category.MEAT);
        RecipeProduct recipeProduct4 = new RecipeProduct(0L, product4, 100, Unit.GRAMS);

        Recipe recipe;
        try (InputStream input = classLoader.getResourceAsStream("example_images/pizza_salami.jpg")) {
            recipe = new Recipe(
                    0L,
                    "Pizza salami",
                    """
                    Preheat the oven to 220°C. Roll out the pizza dough on a lightly floured surface into a circle about 0.5 cm thick, 
                    then transfer it to a baking sheet lined with parchment paper or to a round pizza pan lightly greased with olive oil. 
                    Spread the tomato sauce evenly over the dough, leaving the edges untouched, and sprinkle the shredded mozzarella 
                    cheese over the sauce. Arrange the salami slices on top, optionally adding chopped olives for extra flavor. 
                    Sprinkle the oregano over the entire pizza and drizzle lightly with olive oil. 
                    Bake the pizza in the preheated oven for about 12-15 minutes, 
                    until the edges of the crust are golden brown and the cheese is melted and lightly browned. Once baked, 
                    remove the pizza from the oven and let it cool slightly for a few minutes before slicing and serving warm.
                    """,
                    120,
                    MealType.SNACK,
                    "Italian",
                    2,
                    ImageUtil.compressImage(input.readAllBytes()),
                    user,
                    List.of(recipeProduct, recipeProduct2, recipeProduct3, recipeProduct4)
                    );
            this.recipeRepository.save(recipe);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }

        Product product5 = new Product(0L, "Spaghetti", Category.PASTA);
        RecipeProduct recipeProduct5 = new RecipeProduct(0L, product5, 400, Unit.GRAMS);
        Product product6 = new Product(0L, "Ground beef", Category.MEAT);
        RecipeProduct recipeProduct6 = new RecipeProduct(0L, product6, 100, Unit.GRAMS);
        Product product7 = new Product(0L, "Onion", Category.VEGETABLES);
        RecipeProduct recipeProduct7 = new RecipeProduct(0L, product7, 1, Unit.PIECES);
        Product product8 = new Product(0L, "Garlic", Category.VEGETABLES);
        RecipeProduct recipeProduct8 = new RecipeProduct(0L, product8, 10, Unit.GRAMS);
        Product product9 = new Product(0L, "Canned crushed tomatoes", Category.CANNED_GOODS);
        RecipeProduct recipeProduct9 = new RecipeProduct(0L, product9, 400, Unit.GRAMS);

        Recipe recipe2;
        try (InputStream input = classLoader.getResourceAsStream("example_images/spaghetti.jpg")) {
            recipe2 = new Recipe(
                    0L,
                    "Spaghetti Bolognese",
                    """
                    Begin by cooking the spaghetti according to package instructions, then drain and set aside. 
                    Next, in a large skillet over medium heat, brown the ground beef until cooked through. 
                    Add the chopped onion and minced garlic to the skillet, cooking until softened. 
                    Stir in the crushed tomatoes, seasoning with dried oregano, basil, 
                    salt, and pepper to taste. Allow the sauce to simmer for about 15-20 minutes until it thickens. 
                    Finally, serve the spaghetti topped with the bolognese sauce and grated Parmesan cheese.
                    """,
                    45,
                    MealType.DINNER,
                    "Italian",
                    4,
                    ImageUtil.compressImage(input.readAllBytes()),
                    user,
                    List.of(recipeProduct5, recipeProduct6, recipeProduct7, recipeProduct8, recipeProduct9)
            );
            this.recipeRepository.save(recipe2);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }

        Product product10 = new Product(0L, "Flour", Category.BAKING_GOODS);
        RecipeProduct recipeProduct10 = new RecipeProduct(0L, product10, 100, Unit.GRAMS);
        Product product11 = new Product(0L, "Egg", Category.ANIMAL_PRODUCTS);
        RecipeProduct recipeProduct11 = new RecipeProduct(0L, product11, 1, Unit.PIECES);
        Product product12 = new Product(0L, "Milk", Category.DAIRY);
        RecipeProduct recipeProduct12 = new RecipeProduct(0L, product12, 150, Unit.MILLILITERS);
        Product product13 = new Product(0L, "Sugar", Category.BAKING_GOODS);
        RecipeProduct recipeProduct13 = new RecipeProduct(0L, product13, 10, Unit.GRAMS);
        Product product14 = new Product(0L, "Salt", Category.BAKING_GOODS);
        RecipeProduct recipeProduct14 = new RecipeProduct(0L, product14, 2, Unit.GRAMS);
        Product product15 = new Product(0L, "Butter", Category.BAKING_GOODS);
        RecipeProduct recipeProduct15 = new RecipeProduct(0L, product15, 10, Unit.GRAMS);

        Recipe recipe3;
        try (InputStream input = classLoader.getResourceAsStream("example_images/pancake.jpg")) {
            recipe3 = new Recipe(
                    0L,
                    "Pancakes",
                    """
                    Begin by cooking the spaghetti according to package instructions, then drain and set aside. 
                    Next, in a large skillet over medium heat, brown the ground beef until cooked through. 
                    Add the chopped onion and minced garlic to the skillet, cooking until softened. 
                    Stir in the crushed tomatoes, seasoning with dried oregano, basil, 
                    salt, and pepper to taste. Allow the sauce to simmer for about 15-20 minutes until it thickens. 
                    Finally, serve the spaghetti topped with the bolognese sauce and grated Parmesan cheese.
                    """,
                    30,
                    MealType.BREAKFAST,
                    null,
                    2,
                    ImageUtil.compressImage(input.readAllBytes()),
                    user,
                    List.of(recipeProduct10, recipeProduct11, recipeProduct12, recipeProduct13, recipeProduct14, recipeProduct15)
            );
            this.recipeRepository.save(recipe3);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }
}
