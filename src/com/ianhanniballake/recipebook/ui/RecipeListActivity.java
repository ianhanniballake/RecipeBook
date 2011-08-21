package com.ianhanniballake.recipebook.ui;

import android.content.Intent;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.ianhanniballake.recipebook.R;

/**
 * Activity controlling the recipe list
 */
public class RecipeListActivity extends FragmentActivity implements
		OnRecipeSelectedListener
{
	/**
	 * Whether we currently are displaying both the list and details fragments
	 */
	private boolean isDualPane;

	/**
	 * Sets the main layout
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe_list);
		final View detailsFrame = findViewById(R.id.details);
		isDualPane = detailsFrame != null
				&& detailsFrame.getVisibility() == View.VISIBLE;
	}

	/**
	 * Handles the selection of a recipe
	 * 
	 * @see com.ianhanniballake.recipebook.ui.OnRecipeSelectedListener#onRecipeSelected(int)
	 */
	@Override
	public void onRecipeSelected(final int recipeId)
	{
		if (isDualPane)
		{
			RecipeDetailFragment details = (RecipeDetailFragment) getSupportFragmentManager()
					.findFragmentById(R.id.details);
			if (details == null || details.getRecipeId() != recipeId)
			{
				// Make new fragment to show this selection.
				details = new RecipeDetailFragment();
				final Bundle args = new Bundle();
				args.putInt(BaseColumns._ID, recipeId);
				details.setArguments(args);
				// Execute a transaction, replacing any existing fragment
				// with this one inside the frame.
				final FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				ft.replace(R.id.details, details);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				ft.commit();
			}
		}
		else
		{
			// We need to launch a new activity to display the details
			final Intent intent = new Intent();
			intent.setClass(this, RecipeDetailActivity.class);
			intent.putExtra(BaseColumns._ID, recipeId);
			startActivity(intent);
		}
	}
}