package com.ianhanniballake.recipebook.ui;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Fragment which displays the details of a single recipe for editing
 */
public class RecipeEditFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor>
{
	/**
	 * Focus listener to automatically hide the soft keyboard when closing this
	 * fragment
	 */
	private final OnFocusChangeListener hideKeyboard = new OnFocusChangeListener()
	{
		@Override
		public void onFocusChange(final View v, final boolean hasFocus)
		{
			if (!hasFocus)
			{
				final InputMethodManager imm = (InputMethodManager) v
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			}
		}
	};
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
		if (getArguments() == null)
			return 0;
		return getArguments().getLong(BaseColumns._ID, 0);
	}

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

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

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
		inflater.inflate(R.menu.fragment_recipe_edit, menu);
		MenuCompat.setShowAsAction(menu.findItem(R.id.save), 2);
		MenuCompat.setShowAsAction(menu.findItem(R.id.cancel), 2);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState)
	{
		return inflater
				.inflate(R.layout.fragment_recipe_edit, container, false);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> arg0)
	{
		// Nothing to do
	}

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

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		final TextView title = (TextView) getActivity()
				.findViewById(R.id.title);
		final TextView description = (TextView) getActivity().findViewById(
				R.id.description);
		switch (item.getItemId())
		{
			case R.id.save:
				final ContentValues values = new ContentValues();
				values.put(RecipeContract.Recipes.COLUMN_NAME_TITLE, title
						.getText().toString());
				values.put(RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION,
						description.getText().toString());
				hideKeyboard.onFocusChange(title, false);
				hideKeyboard.onFocusChange(description, false);
				recipeEditListener.onRecipeEditSave(getRecipeId(), values);
				return true;
			case R.id.cancel:
				hideKeyboard.onFocusChange(title, false);
				hideKeyboard.onFocusChange(description, false);
				recipeEditListener.onRecipeEditCancelled();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
