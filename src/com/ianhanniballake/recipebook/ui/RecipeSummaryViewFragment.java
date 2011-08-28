package com.ianhanniballake.recipebook.ui;

import android.os.Bundle;
import android.support.v4.view.MenuCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Fragment which displays the details of a single recipe
 */
public class RecipeSummaryViewFragment extends RecipeDetailFragment
{
	@Override
	protected SimpleCursorAdapter createAdapter()
	{
		return new SimpleCursorAdapter(getActivity(),
				R.layout.fragment_recipe_detail, null, new String[] {
						RecipeContract.Recipes.COLUMN_NAME_TITLE,
						RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION },
				new int[] { R.id.title, R.id.description }, 0);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
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
		inflater.inflate(R.menu.fragment_recipe_detail, menu);
		MenuCompat.setShowAsAction(menu.findItem(R.id.edit), 2);
		MenuCompat.setShowAsAction(menu.findItem(R.id.delete), 2);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_recipe_detail, container,
				false);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.edit:
				recipeEditListener.onRecipeEditStarted(getRecipeId());
				return true;
			case R.id.delete:
				recipeEditListener.onRecipeDeleted(getRecipeId());
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
		menu.findItem(R.id.edit).setVisible(getRecipeId() != 0);
		menu.findItem(R.id.delete).setVisible(getRecipeId() != 0);
	}
}
