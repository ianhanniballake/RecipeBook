package com.ianhanniballake.recipebook.model;

import java.util.List;

import android.database.Cursor;

import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Class that manages the information associated with a recipe
 */
public class Recipe
{
	private String description;
	private List<Ingredient> ingredients;
	private List<Instruction> instructions;
	private String title;

	/**
	 * Creates a new, empty recipe
	 */
	public Recipe()
	{
	}

	/**
	 * Creates a new Recipe from a cursor representation
	 * 
	 * @param cursor
	 *            Cursor representation to load from
	 * @param ingredients
	 *            ingredients for this recipe
	 * @param instructions
	 *            instructions for this recipe
	 */
	public Recipe(final Cursor cursor, final List<Ingredient> ingredients, final List<Instruction> instructions)
	{
		title = cursor.getString(cursor.getColumnIndex(RecipeContract.Recipes.COLUMN_NAME_TITLE));
		description = cursor.getString(cursor.getColumnIndex(RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION));
		this.ingredients = ingredients;
		this.instructions = instructions;
	}

	/**
	 * Getter for the title
	 * 
	 * @return title of this recipe
	 */
	public String getTitle()
	{
		return title;
	}

	@Override
	public String toString()
	{
		return title + ": " + description + "; Ingredients: " + ingredients + ", Instructions: " + instructions;
	}
}
