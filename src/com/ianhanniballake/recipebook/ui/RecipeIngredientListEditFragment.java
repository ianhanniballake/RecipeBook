package com.ianhanniballake.recipebook.ui;

import com.ianhanniballake.recipebook.R;

/**
 * Fragment which displays an editable list of ingredients for a given recipe
 */
public class RecipeIngredientListEditFragment extends
		RecipeIngredientListFragment
{
	@Override
	protected int getListItemLayout()
	{
		return R.layout.item_ingredient_list_edit;
	}
}