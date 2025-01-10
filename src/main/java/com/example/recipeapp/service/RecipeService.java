package com.example.recipeapp.service;

import com.example.recipeapp.model.Recipe;
import com.example.recipeapp.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepository;

    @Autowired
    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public List<Recipe> searchByIngredients(List<String> ingredients) {
        return recipeRepository.findAll().stream()
                .filter(recipe -> ingredients.stream().allMatch(recipe.getIngredients()::contains))
                .toList();
    }
}
