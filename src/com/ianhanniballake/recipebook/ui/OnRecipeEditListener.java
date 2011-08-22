package com.ianhanniballake.recipebook.ui;

import android.content.ContentValues;

/**
 * 
 * Listener which handles recipe start editing events
 */
public interface OnRecipeEditListener
{
	/**
	 * Handles recipe delete events
	 * 
	 * @param recipeId
	 *            Id of the recipe to delete
	 */
	public void onRecipeDeleted(long recipeId);

	/**
	 * Handles edit cancel events
	 */
	public void onRecipeEditCancelled();

	/**
	 * Handles edit save events
	 * 
	 * @param recipeId
	 *            Id of the recipe to save
	 * @param values
	 *            Values to save
	 */
	public void onRecipeEditSave(long recipeId, ContentValues values);

	/**
	 * Handles start editing event for a given recipe
	 * 
	 * @param recipeId
	 *            Id of the recipe to edit
	 */
	public void onRecipeEditStarted(long recipeId);
}
