package com.example.recipeapp.controller;

import com.example.recipeapp.model.Recipe;
import com.example.recipeapp.service.RecipeApiService;
import com.example.recipeapp.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final RecipeApiService recipeApiService;

    @Autowired
    public RecipeController(RecipeService recipeService, RecipeApiService recipeApiService) {
        this.recipeService = recipeService;
        this.recipeApiService = recipeApiService;
    }

    // Méthode pour récupérer les recettes basées sur un ou plusieurs ingrédients
    @GetMapping("/searchByIngredients")
    public ResponseEntity<List<Recipe>> searchByIngredients(@RequestParam String ingredients) {
        // Appel à l'API Spoonacular pour récupérer les recettes basées sur les ingrédients
        List<Recipe> recipes = recipeApiService.getRecipesByIngredients(ingredients);
        return ResponseEntity.ok(recipes);  // Retourne la liste des recettes
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getRecipeById(@PathVariable Long id) {
        // Appel à la méthode du service pour récupérer la recette depuis l'API
        Recipe recipe = recipeApiService.getRecipeByIdFromApi(id);

        // Si la recette existe, retourne les détails, sinon retourne une réponse 404
        if (recipe != null) {
            return ResponseEntity.ok(recipe);
        } else {
            return ResponseEntity.status(404).body(null);  // En cas de recette non trouvée
        }
    }

    @GetMapping("/random")
    public ResponseEntity<List<Recipe>> getRandomRecipes(@RequestParam(defaultValue = "10") int number) {
        List<Recipe> recipes = recipeApiService.getRandomRecipes(number);
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Recipe>> searchRecipesByName(@RequestParam String query) {
        List<Recipe> recipes = recipeApiService.searchRecipesByName(query);
        return ResponseEntity.ok(recipes);
    }



}
