package com.ianhanniballake.recipebook.ui;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuCompat;
import android.support.v4.view.PagerAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Activity which displays only the Recipe details
 */
public class RecipeDetailViewFragment extends RecipeDetailFragment
{
	/**
	 * Class which handles returning the appropriate page
	 */
	private class RecipePagerAdapter extends FragmentStatePagerAdapter
	{
		/**
		 * Creates a new adapter
		 * 
		 * @param fm
		 *            The FragmentManager used to store/retrieve fragments from
		 */
		public RecipePagerAdapter(final FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public int getCount()
		{
			return 2;
		}

		@Override
		public Fragment getItem(final int position)
		{
			if (position == 0)
			{
				final RecipeSummaryFragment summary = new RecipeSummaryViewFragment();
				final Bundle args = new Bundle();
				args.putLong(BaseColumns._ID, recipeId);
				summary.setArguments(args);
				return summary;
			}
			else if (position == 1)
			{
				final RecipeIngredientListFragment ingredients = new RecipeIngredientListViewFragment();
				final Bundle args = new Bundle();
				args.putLong(BaseColumns._ID, recipeId);
				ingredients.setArguments(args);
				return ingredients;
			}
			return null;
		}
	}

	/**
	 * Request Code associated with editing the details of a recipe
	 */
	private static final int EDIT_RECIPE = 1;
	/**
	 * Result indicating a recipe deletion
	 */
	public static final int RESULT_DELETED = Integer.MIN_VALUE;
	/**
	 * Listener that handles recipe delete events
	 */
	private OnRecipeDeleteListener recipeDeleteListener = null;
	/**
	 * Handler for asynchronous updates of recipes
	 */
	private AsyncQueryHandler recipeQueryHandler;

	@Override
	protected PagerAdapter getPagerAdapter(final FragmentManager fm)
	{
		return new RecipePagerAdapter(fm);
	}

	/**
	 * Attaches to the parent activity, saving a reference to it to call back
	 * recipe delete events
	 * 
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(final Activity activity)
	{
		super.onAttach(activity);
		if (activity instanceof OnRecipeDeleteListener)
			recipeDeleteListener = (OnRecipeDeleteListener) activity;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		recipeQueryHandler = new AsyncQueryHandler(getActivity()
				.getContentResolver())
		{
			@Override
			protected void onDeleteComplete(final int token,
					final Object cookie, final int result)
			{
				Toast.makeText(getActivity(), getText(R.string.deleted),
						Toast.LENGTH_SHORT).show();
				if (recipeDeleteListener != null)
					recipeDeleteListener.onRecipeDeleted();
			}
		};
	}

	/**
	 * Adds Edit option to the menu
	 * 
	 * @see android.support.v4.app.Fragment#onCreateOptionsMenu(android.view.Menu,
	 *      android.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_recipe_summary, menu);
		MenuCompat.setShowAsAction(menu.findItem(R.id.edit), 2);
		MenuCompat.setShowAsAction(menu.findItem(R.id.delete), 2);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.edit:
				onRecipeEditStarted();
				return true;
			case R.id.delete:
				onRecipeDeleted();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Hides the edit and delete items if this is not currently showing a valid
	 * recipe
	 * 
	 * @see android.support.v4.app.Fragment#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public void onPrepareOptionsMenu(final Menu menu)
	{
		menu.findItem(R.id.delete).setVisible(recipeId != 0);
	}

	/**
	 * Handles recipe delete events
	 */
	public void onRecipeDeleted()
	{
		final Uri deleteUri = ContentUris.withAppendedId(
				RecipeContract.Recipes.CONTENT_ID_URI_PATTERN, recipeId);
		recipeQueryHandler.startDelete(0, null, deleteUri, null, null);
	}

	/**
	 * Handles recipe edit start events
	 */
	public void onRecipeEditStarted()
	{
		final Intent intent = new Intent(getActivity(),
				RecipeAddEditActivity.class);
		intent.putExtra(BaseColumns._ID, recipeId);
		startActivityForResult(intent, EDIT_RECIPE);
	}
}
