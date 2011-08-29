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
import android.widget.ListView;
import android.widget.TextView;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Fragment which displays the list of ingredients for a given recipe
 */
public class RecipeIngredientListFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor>
{
	/**
	 * Cursor Adapter which handles the unique binding of ingredients to a
	 * single TextView
	 */
	private class IngredientCursorAdapter extends ResourceCursorAdapter
	{
		/**
		 * Standard constructor.
		 * 
		 * @param context
		 *            The context where the ListView associated with this
		 *            adapter is running
		 * @param layout
		 *            Resource identifier of a layout file that defines the
		 *            views for this list item. Unless you override them later,
		 *            this will define both the item views and the drop down
		 *            views.
		 * @param c
		 *            The cursor from which to get the data.
		 * @param flags
		 *            Flags used to determine the behavior of the adapter, as
		 *            per
		 *            {@link CursorAdapter#CursorAdapter(Context, Cursor, int)}.
		 */
		public IngredientCursorAdapter(final Context context, final int layout,
				final Cursor c, final int flags)
		{
			super(context, layout, c, flags);
		}

		@Override
		public void bindView(final View view, final Context context,
				final Cursor cursor)
		{
			final TextView raw = (TextView) view.findViewById(R.id.raw);
			raw.setText(convertToRaw(cursor));
		}

		/**
		 * Converts a cursor pointing at an ingredient to a raw formatted string
		 * 
		 * @param cursor
		 *            An ingredient cursor
		 * @return Formatted raw string
		 */
		private CharSequence convertToRaw(final Cursor cursor)
		{
			final StringBuilder sb = new StringBuilder();
			final int quantity = cursor
					.getInt(cursor
							.getColumnIndex(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY));
			if (quantity > 0)
				sb.append(quantity);
			final int numerator = cursor
					.getInt(cursor
							.getColumnIndex(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR));
			final int denominator = cursor
					.getInt(cursor
							.getColumnIndex(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR));
			if (numerator > 0)
			{
				if (sb.length() > 0)
					sb.append(' ');
				sb.append(numerator);
				sb.append('/');
				sb.append(denominator);
			}
			final String unit = cursor
					.getString(cursor
							.getColumnIndex(RecipeContract.Ingredients.COLUMN_NAME_UNIT));
			if (!unit.equals(""))
			{
				if (sb.length() > 0)
					sb.append(' ');
				sb.append(unit);
			}
			final String item = cursor
					.getString(cursor
							.getColumnIndex(RecipeContract.Ingredients.COLUMN_NAME_ITEM));
			if (sb.length() > 0)
				sb.append(' ');
			sb.append(item);
			final String preparation = cursor
					.getString(cursor
							.getColumnIndex(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION));
			if (!preparation.equals(""))
			{
				if (sb.length() > 0)
					sb.append(" - ");
				sb.append(preparation);
			}
			return sb;
		}
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
	public long getRecipeId()
	{
		if (getArguments() == null)
			return 0;
		return getArguments().getLong(BaseColumns._ID, 0);
	}

	@SuppressWarnings("static-access")
	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		setEmptyText(getText(R.string.empty_recipe_list));
		adapter = new IngredientCursorAdapter(getActivity(),
				R.layout.item_ingredient_list, null, 0);
		setListAdapter(adapter);
		getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
		if (getRecipeId() != 0)
			getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args)
	{
		return new CursorLoader(getActivity(),
				RecipeContract.Ingredients.CONTENT_ID_URI_BASE, null,
				RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID + "=?",
				new String[] { Long.toString(getRecipeId()) }, null);
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
