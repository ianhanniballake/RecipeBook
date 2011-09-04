package com.ianhanniballake.recipebook.ui;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Fragment which displays the details of a single recipe
 */
public abstract class RecipeSummaryFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor>
{
	/**
	 * Adapter to display the detailed data
	 */
	private SimpleCursorAdapter adapter;

	/**
	 * Creates the appropriate adapter for this fragment. Will be used to bind
	 * the loaded view when the CursorLoader returns.
	 * 
	 * @return CursorAdapter that will be used to bind the view
	 */
	protected abstract SimpleCursorAdapter createAdapter();

	/**
	 * Gets the content values associated with this detail fragment, which can
	 * be used for update statements
	 * 
	 * @return current values in this fragment
	 */
	public ContentValues getContentValues()
	{
		final TextView title = (TextView) getActivity()
				.findViewById(R.id.title);
		final TextView description = (TextView) getActivity().findViewById(
				R.id.description);
		final ContentValues values = new ContentValues();
		values.put(RecipeContract.Recipes.COLUMN_NAME_TITLE, title.getText()
				.toString());
		values.put(RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION, description
				.getText().toString());
		return values;
	}

	/**
	 * Getter for the ID associated with the currently displayed recipe
	 * 
	 * @return ID for the currently displayed recipe
	 */
	protected long getRecipeId()
	{
		if (getArguments() == null)
			return 0;
		return getArguments().getLong(BaseColumns._ID, 0);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		adapter = createAdapter();
		if (getRecipeId() != 0)
			getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args)
	{
		final Uri recipeUri = ContentUris.withAppendedId(
				RecipeContract.Recipes.CONTENT_ID_URI_PATTERN, getRecipeId());
		return new CursorLoader(getActivity(), recipeUri, null, null, null,
				null);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> data)
	{
		adapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data)
	{
		if (!data.moveToFirst() || getView() == null)
			return;
		adapter.swapCursor(data);
		adapter.bindView(getView(), getActivity(), data);
	}
}
