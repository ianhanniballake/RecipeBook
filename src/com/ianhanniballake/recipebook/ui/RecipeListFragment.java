package com.ianhanniballake.recipebook.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Fragment which displays the list of recipes and triggers recipe selection
 * events
 */
public class RecipeListFragment extends ListFragment
{
	/**
	 * Adapter to display the list's data
	 */
	private SimpleCursorAdapter adapter;
	/**
	 * Listener that handles recipe selection events
	 */
	private OnRecipeSelectedListener recipeSelectedListener;

	/**
	 * Creates the list adapter
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		setEmptyText(getText(R.string.empty_recipe_list));
		adapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_list_item_1, null,
				new String[] { RecipeContract.Recipes.COLUMN_NAME_TITLE },
				new int[] { android.R.id.text1 }, 0);
		setListAdapter(adapter);
	}

	/**
	 * Attaches to the parent activity, saving a reference to it to call back
	 * recipe selection events
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
			throw new ClassCastException(activity.toString()
					+ " must implement OnRecipeSelectedListener");
		}
	}

	/**
	 * Handles clicking on a list item
	 * 
	 * @see android.support.v4.app.ListFragment#onListItemClick(android.widget.ListView,
	 *      android.view.View, int, long)
	 */
	@Override
	public void onListItemClick(final ListView l, final View v,
			final int position, final long id)
	{
		recipeSelectedListener.onRecipeSelected(id);
	}
}
