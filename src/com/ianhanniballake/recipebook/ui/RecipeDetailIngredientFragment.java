package com.ianhanniballake.recipebook.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.model.Ingredient;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Fragment which displays the list of ingredients for a given recipe
 */
public class RecipeDetailIngredientFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>
{
	/**
	 * Cursor Adapter which handles the unique binding of ingredients to a single TextView
	 */
	private class IngredientCursorAdapter extends ResourceCursorAdapter
	{
		/**
		 * Standard constructor.
		 * 
		 * @param context
		 *            The context where the ListView associated with this adapter is running
		 * @param layout
		 *            Resource identifier of a layout file that defines the views for this list item. Unless you
		 *            override them later, this will define both the item views and the drop down views.
		 * @param c
		 *            The cursor from which to get the data.
		 * @param flags
		 *            Flags used to determine the behavior of the adapter, as per
		 *            {@link CursorAdapter#CursorAdapter(Context, Cursor, int)}.
		 */
		public IngredientCursorAdapter(final Context context, final int layout, final Cursor c, final int flags)
		{
			super(context, layout, c, flags);
		}

		@Override
		public void bindView(final View view, final Context context, final Cursor cursor)
		{
			final TextView raw = (TextView) view.findViewById(R.id.raw);
			raw.setText(new Ingredient(cursor).toString());
		}
	}

	/**
	 * Create a new instance of this fragment for the given recipe id
	 * 
	 * @param recipeId
	 *            Recipe ID to display ingredients for
	 * @return A valid instance of this fragment
	 */
	public static RecipeDetailIngredientFragment newInstance(final long recipeId)
	{
		final Bundle arguments = new Bundle();
		arguments.putLong(BaseColumns._ID, recipeId);
		final RecipeDetailIngredientFragment ingredientFragment = new RecipeDetailIngredientFragment();
		ingredientFragment.setArguments(arguments);
		return ingredientFragment;
	}

	/**
	 * Adapter to display the list's data
	 */
	private IngredientCursorAdapter adapter;

	/**
	 * Getter for the ID associated with the currently displayed recipe
	 * 
	 * @return ID for the currently displayed recipe
	 */
	protected long getRecipeId()
	{
		if (getArguments() == null)
			return AdapterView.INVALID_ROW_ID;
		return getArguments().getLong(BaseColumns._ID, AdapterView.INVALID_ROW_ID);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		setEmptyText(getText(R.string.empty_ingredient_list));
		adapter = new IngredientCursorAdapter(getActivity(), R.layout.list_item_ingredient, null, 0);
		setListAdapter(adapter);
		getListView().setChoiceMode(AbsListView.CHOICE_MODE_NONE);
		if (getRecipeId() != AdapterView.INVALID_ROW_ID)
			getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args)
	{
		return new CursorLoader(getActivity(), RecipeContract.Ingredients.CONTENT_URI, null,
				RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID + "=?", new String[] { Long.toString(getRecipeId()) },
				null);
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
