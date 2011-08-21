package com.ianhanniballake.recipebook.ui;

/**
 * Listener which handles recipe selection events
 */
public interface OnRecipeSelectedListener
{
	/**
	 * Handles selection of a given recipe
	 * 
	 * @param recipeId
	 *            Id of the selected recipe
	 */
	public void onRecipeSelected(long recipeId);
}
