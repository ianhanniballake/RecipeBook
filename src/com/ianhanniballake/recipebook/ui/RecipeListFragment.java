package com.ianhanniballake.recipebook.ui;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Fragment which displays the list of recipes and triggers recipe selection events
 */
public class RecipeListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>
{
	/**
	 * Adapter to display the list's data
	 */
	private SimpleCursorAdapter adapter;
	/**
	 * Listener that handles recipe selection events
	 */
	private OnRecipeSelectedListener recipeSelectedListener;

	@SuppressWarnings("static-access")
	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		setEmptyText(getText(R.string.empty_recipe_list));
		adapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item_recipe, null,
				new String[] { RecipeContract.Recipes.COLUMN_NAME_TITLE }, new int[] { R.id.title }, 0);
		setListAdapter(adapter);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		getLoaderManager().initLoader(0, null, this);
	}

	/**
	 * Attaches to the parent activity, saving a reference to it to call back recipe selection events
	 * 
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(final Activity activity)
	{
		super.onAttach(activity);
		try
		{
			recipeSelectedListener = (OnRecipeSelectedListener) activity;
		} catch (final ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement OnRecipeSelectedListener");
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args)
	{
		return new CursorLoader(getActivity(), RecipeContract.Recipes.CONTENT_ID_URI_BASE, null, null, null, null);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id)
	{
		recipeSelectedListener.onRecipeSelected(id);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader)
	{
		adapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data)
	{
		adapter.swapCursor(data);
	}
}
