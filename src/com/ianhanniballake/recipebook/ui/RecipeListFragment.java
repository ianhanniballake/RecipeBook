package com.ianhanniballake.recipebook.ui;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ianhanniballake.recipebook.R;

/**
 * Fragment which displays the list of recipes and triggers recipe selection
 * events
 */
public class RecipeListFragment extends ListFragment
{
	/**
	 * @see android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater,
	 *      android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(final LayoutInflater inflator,
			final ViewGroup container, final Bundle savedInstanceState)
	{
		return inflator
				.inflate(R.layout.fragment_recipe_list, container, false);
	}
}
