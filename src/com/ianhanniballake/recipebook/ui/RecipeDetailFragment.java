package com.ianhanniballake.recipebook.ui;

import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
	 * Listener that handles recipe edit events
	 */
	private OnRecipeEditListener recipeEditListener;

	/**
	 * Getter for the ID associated with the currently displayed recipe
	 * 
	 * @return ID for the currently displayed recipe
	 */
	public long getRecipeId()
	{
		if (getArguments() == null
				|| !getArguments().containsKey(BaseColumns._ID))
			return 0;
		return getArguments().getLong(BaseColumns._ID);
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
		if (getRecipeId() != 0)
			getLoaderManager().initLoader(0, null, this);
	}

	/**
	 * Attaches to the parent activity, saving a reference to it to call back
	 * recipe edit events
	 * 
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(final Activity activity)
	{
		super.onAttach(activity);
		try
		{
			recipeEditListener = (OnRecipeEditListener) activity;
		} catch (final ClassCastException e)
		{
			throw new ClassCastException(activity.toString()
					+ " must implement OnRecipeEditListener");
		}
	}

	/**
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
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
		final Uri recipeUri = ContentUris.withAppendedId(
				RecipeContract.Recipes.CONTENT_ID_URI_PATTERN, getRecipeId());
		return new CursorLoader(getActivity(), recipeUri, null, null, null,
				null);
	}

	/**
	 * Adds Edit option to the menu
	 * 
	 * @see android.support.v4.app.Fragment#onCreateOptionsMenu(android.view.Menu,
	 *      android.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater)
	{
		inflater.inflate(R.menu.fragment_recipe_detail, menu);
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
		if (getRecipeId() == 0)
			return inflater.inflate(
					R.layout.fragment_recipe_detail_placeholder, container,
					false);
		return inflater.inflate(R.layout.fragment_recipe_detail, container,
				false);
	}

	/**
	 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.support.v4.content.Loader)
	 */
	@Override
	public void onLoaderReset(final Loader<Cursor> data)
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
		if (!data.moveToFirst())
			return;
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

	/**
	 * Handles selection of a menu item
	 * 
	 * @see android.support.v4.app.Fragment#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.edit:
				recipeEditListener.onRecipeEditStarted(getRecipeId());
				return true;
			case R.id.delete:
				recipeEditListener.onRecipeDeleted(getRecipeId());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Hides the edit option if this is not currently showing a valid recipe
	 * 
	 * @see android.support.v4.app.Fragment#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public void onPrepareOptionsMenu(final Menu menu)
	{
		menu.findItem(R.id.edit).setVisible(getRecipeId() != 0);
		menu.findItem(R.id.delete).setVisible(getRecipeId() != 0);
	}
}
