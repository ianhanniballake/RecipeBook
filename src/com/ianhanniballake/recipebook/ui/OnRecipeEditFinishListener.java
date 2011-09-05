package com.ianhanniballake.recipebook.ui;

/**
 * Listener which handles recipe edit finish events
 */
public interface OnRecipeEditFinishListener
{
	/**
	 * Handles edit cancel events
	 */
	public void onRecipeEditCancelled();

	/**
	 * Handles edit save events
	 * 
	 * @param recipeId
	 *            Id of the recipe to save
	 */
	public void onRecipeEditSave(long recipeId);
}
