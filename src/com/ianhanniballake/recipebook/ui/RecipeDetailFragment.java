package com.ianhanniballake.recipebook.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Fragment which displays the details of a single recipe
 */
public class RecipeDetailFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor>
{
	/**
	 * Getter for the ID associated with the currently displayed recipe
	 * 
	 * @return ID for the currently displayed recipe
	 */
	public int getRecipeId()
	{
		return getArguments().getInt(BaseColumns._ID);
	}

	/**
	 * Loads the data from the RecipeProvider
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		if (getArguments() != null
				&& getArguments().containsKey(BaseColumns._ID))
			getLoaderManager().initLoader(0, null, this);
	}

	/**
	 * Creates the loader to read the Recipe from the RecipeProvider
	 * 
	 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int,
	 *      android.os.Bundle)
	 */
	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args)
	{
		final Uri recipeUri = Uri.withAppendedPath(
				RecipeContract.Recipes.CONTENT_ID_URI_PATTERN,
				Integer.toString(getRecipeId()));
		return new CursorLoader(getActivity(), recipeUri, null, null, null,
				null);
	}

	/**
	 * Creates the view for this fragment
	 * 
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 *      android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_recipe_detail, container,
				false);
	}

	/**
	 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.support.v4.content.Loader)
	 */
	@Override
	public void onLoaderReset(final Loader<Cursor> arg0)
	{
		// Nothing to do
	}

	/**
	 * Called when the loader finishes its query to the RecipeProvider
	 * 
	 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android.support.v4.content.Loader,
	 *      java.lang.Object)
	 */
	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data)
	{
		final TextView title = (TextView) getActivity()
				.findViewById(R.id.title);
		title.setText(data.getString(data
				.getColumnIndex(RecipeContract.Recipes.COLUMN_NAME_TITLE)));
		final TextView description = (TextView) getActivity().findViewById(
				R.id.description);
		description
				.setText(data.getString(data
						.getColumnIndex(RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION)));
	}
}
