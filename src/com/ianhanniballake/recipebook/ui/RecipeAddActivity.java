package com.ianhanniballake.recipebook.ui;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Activity responsible for creating new Recipes
 */
public class RecipeAddActivity extends FragmentActivity implements
		OnRecipeEditListener
{
	/**
	 * Sets the main layout
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe_edit);
	}

	/**
	 * Handles edit cancel events by finishing this activity
	 * 
	 * @see com.ianhanniballake.recipebook.ui.OnRecipeEditListener#onRecipeEditCancelled()
	 */
	@Override
	public void onRecipeEditCancelled()
	{
		finish();
	}

	/**
	 * Handles edit save events
	 * 
	 * @see com.ianhanniballake.recipebook.ui.OnRecipeEditListener#onRecipeEditSave(long,
	 *      android.content.ContentValues)
	 */
	@Override
	public void onRecipeEditSave(final long recipeId, final ContentValues values)
	{
		getContentResolver().insert(RecipeContract.Recipes.CONTENT_ID_URI_BASE,
				values);
		// TODO Add switch from add activity
	}

	/**
	 * Not used
	 * 
	 * @see com.ianhanniballake.recipebook.ui.OnRecipeEditListener#onRecipeEditStarted(long)
	 */
	@Override
	public void onRecipeEditStarted(final long recipeId)
	{
		// Not used
	}
}