package com.ianhanniballake.recipebook.ui;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Activity responsible for creating new Recipes
 */
public class RecipeAddActivity extends FragmentActivity implements
		OnRecipeEditFinishListener
{
	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe_edit);
	}

	@Override
	public void onRecipeEditCancelled()
	{
		setResult(RESULT_CANCELED);
		finish();
	}

	@Override
	public void onRecipeEditSave(final long recipeId)
	{
		final Uri uri = ContentUris.withAppendedId(
				RecipeContract.Recipes.CONTENT_ID_URI_BASE, recipeId);
		setResult(RESULT_OK, new Intent(Intent.ACTION_PICK, uri));
		finish();
	}
}