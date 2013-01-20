package com.ianhanniballake.recipebook.ui;

import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Fragment which displays the details of a single recipe for editing
 */
public class RecipeSummaryEditFragment extends RecipeSummaryFragment
{
	@Override
	protected SimpleCursorAdapter createAdapter()
	{
		return new SimpleCursorAdapter(getActivity(), R.layout.fragment_recipe_summary, null, new String[] {
				RecipeContract.Recipes.COLUMN_NAME_TITLE, RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION }, new int[] {
				R.id.title, R.id.description }, 0);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_recipe_edit, container, false);
	}
}
