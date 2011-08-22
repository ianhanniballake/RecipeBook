package com.ianhanniballake.recipebook.ui;

import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Activity controlling the recipe list
 */
public class RecipeListActivity extends FragmentActivity implements
		OnRecipeSelectedListener, OnRecipeEditListener
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
	 * Handler for asynchronous updates of recipes
	 */
	private AsyncQueryHandler queryHandler;

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data)
	{
		if (requestCode == ADD_RECIPE && resultCode == RESULT_OK)
		{
			final long recipeId = ContentUris.parseId(data.getData());
			onRecipeSelected(recipeId);
		}
		else if (requestCode == VIEW_DETAILS
				&& resultCode == RecipeDetailActivity.RESULT_DELETED)
		{
			final long recipeId = ContentUris.parseId(data.getData());
			onRecipeDeleted(recipeId);
		}
		else
			super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe_list);
		final View detailsFrame = findViewById(R.id.details);
		isDualPane = detailsFrame != null
				&& detailsFrame.getVisibility() == View.VISIBLE;
		queryHandler = new AsyncQueryHandler(getContentResolver())
		{
			@Override
			protected void onDeleteComplete(final int token,
					final Object cookie, final int result)
			{
				Toast.makeText(RecipeListActivity.this,
						getText(R.string.deleted), Toast.LENGTH_SHORT);
				// Execute a transaction, replacing any existing fragment
				// with this one inside the frame.
				final FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				ft.replace(R.id.details, null);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				ft.commit();
			}

			@Override
			protected void onInsertComplete(final int token,
					final Object cookie, final Uri uri)
			{
				Toast.makeText(RecipeListActivity.this,
						getText(R.string.saved), Toast.LENGTH_SHORT);
				getSupportFragmentManager().popBackStack();
			}
		};
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.fragment_recipe_list, menu);
		return super.onCreateOptionsMenu(menu);
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
	public void onRecipeDeleted(final long recipeId)
	{
		Toast.makeText(this, getText(R.string.deleting), Toast.LENGTH_LONG);
		final Uri deleteUri = ContentUris.withAppendedId(
				RecipeContract.Recipes.CONTENT_ID_URI_PATTERN, recipeId);
		queryHandler.startDelete(0, null, deleteUri, null, null);
	}

	@Override
	public void onRecipeEditCancelled()
	{
		getSupportFragmentManager().popBackStack();
	}

	@Override
	public void onRecipeEditSave(final long recipeId, final ContentValues values)
	{
		Toast.makeText(this, getText(R.string.saving), Toast.LENGTH_LONG);
		final Uri updateUri = ContentUris.withAppendedId(
				RecipeContract.Recipes.CONTENT_ID_URI_PATTERN, recipeId);
		queryHandler.startUpdate(0, null, updateUri, values, null, null);
	}

	@Override
	public void onRecipeEditStarted(final long recipeId)
	{
		final Fragment editFragment = new RecipeEditFragment();
		final Bundle args = new Bundle();
		args.putLong(BaseColumns._ID, recipeId);
		editFragment.setArguments(args);
		// Execute a transaction, replacing any existing fragment
		// with this one inside the frame.
		final FragmentTransaction ft = getSupportFragmentManager()
				.beginTransaction();
		ft.replace(R.id.details, editFragment);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.addToBackStack(null);
		ft.commit();
	}

	@Override
	public void onRecipeSelected(final long recipeId)
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
}