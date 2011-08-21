package com.ianhanniballake.recipebook.ui;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;

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
}
