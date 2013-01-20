package com.ianhanniballake.recipebook.ui;

import com.ianhanniballake.recipebook.R;

/**
 * Fragment which displays a read-only list of ingredients for a given recipe
 */
public class RecipeIngredientListViewFragment extends RecipeIngredientListFragment
{
	@Override
	protected int getListItemLayout()
	{
		return R.layout.item_ingredient_list;
	}
}