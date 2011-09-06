package com.ianhanniballake.recipebook.ui;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Activity responsible for creating new Recipes
 */
public class RecipeAddEditActivity extends FragmentActivity implements
		OnRecipeEditFinishListener
{
	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe_edit);
		long recipeId = 0;
		if (getIntent() != null && getIntent().getExtras() != null)
			recipeId = getIntent().getExtras().getLong(BaseColumns._ID, 0);
		final RecipeDetailEditFragment edit = new RecipeDetailEditFragment();
		final Bundle args = new Bundle();
		args.putLong(BaseColumns._ID, recipeId);
		edit.setArguments(args);
		// Execute a transaction, replacing any existing fragment
		// with this one inside the frame.
		final FragmentTransaction ft = getSupportFragmentManager()
				.beginTransaction();
		ft.replace(R.id.edit, edit);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
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