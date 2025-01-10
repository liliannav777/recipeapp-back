package com.example.recipeapp.service;

import com.example.recipeapp.model.Recipe;
import com.example.recipeapp.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecipeApiService {

    @Value("${spoonacular.api.key}")
    private String apiKey;  // Clé API récupérée du fichier application.properties

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public RecipeApiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Recipe> getRecipesByIngredients(String ingredients) {
        try {
            String encodedIngredients = URLEncoder.encode(ingredients, "UTF-8");
            String url = String.format("https://api.spoonacular.com/recipes/findByIngredients?ingredients=%s&apiKey=%s",
                    encodedIngredients, apiKey);

            // Envoie la requête à l'API et récupère la réponse
            String jsonResponse = restTemplate.getForObject(url, String.class);

            // Désérialisation de la réponse JSON en une liste de recettes
            List<Map<String, Object>> recipesList = objectMapper.readValue(jsonResponse, new TypeReference<>() {
            });

            // Crée une liste de recettes à partir de la réponse
            List<Recipe> recipes = new ArrayList<>();
            for (Map<String, Object> recipeData : recipesList) {
                Recipe recipe = new Recipe();
                recipe.setId(Long.valueOf((Integer) recipeData.get("id")));
                recipe.setName((String) recipeData.get("title"));
                recipe.setImageUrl((String) recipeData.get("image"));
                recipe.setDescription("Description from API"); // Une description générique
                recipe.setSteps(List.of("Steps not provided by this API endpoint")); // Placeholder pour les étapes
                recipe.setLikes((Integer) recipeData.get("likes"));

                // Récupération des ingrédients utilisés (usedIngredients)
                List<Map<String, Object>> usedIngredients = (List<Map<String, Object>>) recipeData.get("usedIngredients");
                List<String> usedIngredientsList = new ArrayList<>();
                for (Map<String, Object> ingredient : usedIngredients) {
                    usedIngredientsList.add((String) ingredient.get("original"));
                }
                recipe.setIngredients(usedIngredientsList);

                // Récupération des ingrédients manquants (missedIngredients)
                List<Map<String, Object>> missedIngredients = (List<Map<String, Object>>) recipeData.get("missedIngredients");
                List<String> missedIngredientsList = new ArrayList<>();
                for (Map<String, Object> ingredient : missedIngredients) {
                    missedIngredientsList.add((String) ingredient.get("original"));
                }

                // Ajout des ingrédients manquants à la liste complète
                recipe.getIngredients().addAll(missedIngredientsList);

                recipes.add(recipe);
            }

            return recipes; // Retourne la liste des recettes récupérées
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();  // En cas d'erreur, retourne une liste vide
        }
    }

    public Recipe getRecipeByIdFromApi(Long id) {
        try {
            String url = String.format("https://api.spoonacular.com/recipes/%d/information?apiKey=%s", id, apiKey);
            String jsonResponse = restTemplate.getForObject(url, String.class);

            if (jsonResponse == null || jsonResponse.isEmpty()) {
                return null;
            }

            // Désérialisation de la réponse JSON directement dans un Map
            Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});

            // Créer une instance de Recipe et remplir les champs correspondants
            Recipe recipe = new Recipe();

            // Assigner uniquement les champs que tu veux garder dans l'entité
            recipe.setId(Long.valueOf((Integer) responseMap.get("id")));
            recipe.setName((String) responseMap.get("title"));
            recipe.setImageUrl((String) responseMap.get("image"));
            recipe.setDescription((String) responseMap.get("summary"));
            recipe.setLikes((Integer) responseMap.get("aggregateLikes"));

            // Récupérer la liste des ingrédients
            List<Map<String, Object>> ingredientsList = (List<Map<String, Object>>) responseMap.get("extendedIngredients");
            List<String> ingredients = new ArrayList<>();
            for (Map<String, Object> ingredient : ingredientsList) {
                ingredients.add((String) ingredient.get("original"));
            }
            recipe.setIngredients(ingredients);

            // Récupérer les étapes de préparation
            List<Map<String, Object>> instructionsList = (List<Map<String, Object>>) responseMap.get("analyzedInstructions");
            List<String> steps = new ArrayList<>();
            if (instructionsList != null && !instructionsList.isEmpty()) {
                for (Map<String, Object> instruction : instructionsList) {
                    List<Map<String, Object>> stepsList = (List<Map<String, Object>>) instruction.get("steps");
                    for (Map<String, Object> stepData : stepsList) {
                        steps.add((String) stepData.get("step"));
                    }
                }
            }
            recipe.setSteps(steps);

            // Retourner la recette avec uniquement les informations pertinentes
            return recipe;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<Recipe> getRandomRecipes(int number) {
        try {
            String url = String.format("https://api.spoonacular.com/recipes/random?number=%d&apiKey=%s", number, apiKey);
            String jsonResponse = restTemplate.getForObject(url, String.class);

            Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
            List<Map<String, Object>> recipesData = (List<Map<String, Object>>) responseMap.get("recipes");

            List<Recipe> recipes = new ArrayList<>();
            for (Map<String, Object> recipeData : recipesData) {
                Recipe recipe = mapRecipeData(recipeData);
                recipes.add(recipe);
            }

            return recipes;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Recipe> searchRecipesByName(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String url = String.format("https://api.spoonacular.com/recipes/complexSearch?query=%s&apiKey=%s", encodedQuery, apiKey);
            String jsonResponse = restTemplate.getForObject(url, String.class);

            Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
            List<Map<String, Object>> resultsData = (List<Map<String, Object>>) responseMap.get("results");

            List<Recipe> recipes = new ArrayList<>();
            for (Map<String, Object> recipeData : resultsData) {
                Recipe recipe = new Recipe();
                recipe.setId(Long.valueOf((Integer) recipeData.get("id")));
                recipe.setName((String) recipeData.get("title"));
                recipe.setImageUrl((String) recipeData.get("image"));
                recipes.add(recipe);
            }

            return recipes;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private Recipe mapRecipeData(Map<String, Object> recipeData) {
        Recipe recipe = new Recipe();
        recipe.setId(Long.valueOf((Integer) recipeData.get("id")));
        recipe.setName((String) recipeData.get("title"));
        recipe.setImageUrl((String) recipeData.get("image"));
        recipe.setDescription((String) recipeData.get("summary"));
        recipe.setLikes((Integer) recipeData.get("aggregateLikes"));

        List<String> ingredients = ((List<Map<String, Object>>) recipeData.get("extendedIngredients"))
                .stream()
                .map(ing -> (String) ing.get("original"))
                .collect(Collectors.toList());
        recipe.setIngredients(ingredients);

        List<String> steps = ((List<Map<String, Object>>) recipeData.get("analyzedInstructions"))
                .stream()
                .flatMap(instruction -> ((List<Map<String, Object>>) instruction.get("steps")).stream())
                .map(step -> (String) step.get("step"))
                .collect(Collectors.toList());
        recipe.setSteps(steps);

        return recipe;
    }

}
