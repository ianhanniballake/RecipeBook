package com.ianhanniballake.recipebook.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.view.View;
import android.widget.TextView;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.model.Ingredient;

/**
 * Fragment which displays an editable list of ingredients for a given recipe
 */
public class RecipeIngredientListEditFragment extends RecipeIngredientListFragment
{
	/**
	 * Gets all of the current ingredient values
	 * 
	 * @return A ContentValues for each ingredient
	 */
	public List<ContentValues> getContentValues()
	{
		final int childCount = getListView().getChildCount();
		final ArrayList<ContentValues> allContentValues = new ArrayList<ContentValues>();
		for (int childIndex = 0; childIndex < childCount; childIndex++)
		{
			final View view = getListView().getChildAt(childIndex);
			final TextView raw = (TextView) view.findViewById(R.id.raw);
			final String rawText = raw.getText().toString();
			if (rawText.equals(""))
				continue;
			allContentValues.add(new Ingredient(getResources(), rawText).toContentValues(getRecipeId()));
		}
		return allContentValues;
	}

	@Override
	protected int getListItemLayout()
	{
		return R.layout.item_ingredient_list_edit;
	}
}