package com.ianhanniballake.recipebook.ui;

import android.content.ContentUris;
import android.content.Intent;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ianhanniballake.recipebook.R;

/**
 * Activity controlling the recipe list
 */
public class RecipeListActivity extends FragmentActivity implements
		OnRecipeSelectedListener
{
	/**
	 * Request Code associated with adding a new recipe
	 */
	private static final int ADD_RECIPE = 0;
	/**
	 * Request Code associated with viewing the details of a recipe
	 */
	private static final int VIEW_DETAILS = 1;
	/**
	 * Whether we currently are displaying both the list and details fragments
	 */
	private boolean isDualPane;
	/**
	 * Saves the currently selected position
	 */
	private long selectedId = -1;

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data)
	{
		if (requestCode == ADD_RECIPE && resultCode == RESULT_OK)
		{
			final long recipeId = ContentUris.parseId(data.getData());
			selectedId = recipeId;
		}
		else if (requestCode == VIEW_DETAILS
				&& resultCode == RecipeDetailActivity.RESULT_DELETED)
			selectedId = -1;
		else
			super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe_list);
		final View detailsFrame = findViewById(R.id.details);
		isDualPane = detailsFrame != null
				&& detailsFrame.getVisibility() == View.VISIBLE;
		if (savedInstanceState != null && isDualPane)
			// Restore last state for checked position.
			selectedId = savedInstanceState.getLong("selectedId", -1);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.fragment_recipe_list, menu);
		MenuCompat.setShowAsAction(menu.findItem(R.id.add), 2);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.add:
				final Intent intent = new Intent(this, RecipeAddActivity.class);
				startActivityForResult(intent, ADD_RECIPE);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onRecipeSelected(final long recipeId)
	{
		selectedId = recipeId;
		if (isDualPane)
		{
			RecipeDetailFragment details = (RecipeDetailFragment) getSupportFragmentManager()
					.findFragmentById(R.id.details);
			if (details == null || details.getRecipeId() != recipeId)
			{
				// Make new fragment to show this selection.
				details = new RecipeDetailFragment();
				final Bundle args = new Bundle();
				args.putLong(BaseColumns._ID, recipeId);
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
			startActivityForResult(intent, VIEW_DETAILS);
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if (isDualPane && selectedId != -1)
			onRecipeSelected(selectedId);
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putLong("selectedId", selectedId);
	}
}