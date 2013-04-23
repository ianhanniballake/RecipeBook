package com.ianhanniballake.recipebook.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * A fragment representing a single Recipe detail screen. This fragment is either contained in a
 * {@link RecipeListActivity} in two-pane mode (on tablets) or a {@link RecipeDetailActivity} on handsets.
 */
public class RecipeDetailSummaryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
	/**
	 * Adapter to display the detailed data
	 */
	private CursorAdapter adapter;
	/**
	 * Current description for the recipe
	 */
	String description;
	/**
	 * Current title for the recipe
	 */
	String title;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation
	 * changes).
	 */
	public RecipeDetailSummaryFragment()
	{
	}

	private void clear()
	{
		title = "";
		description = "";
	}

	/**
	 * Gets the current values shown in this fragment
	 * 
	 * @return The current values
	 */
	public ContentValues getContentValues()
	{
		final ContentValues contentValues = new ContentValues();
		title = ((TextView) getView().findViewById(R.id.title)).getText().toString();
		contentValues.put(RecipeContract.Recipes.COLUMN_NAME_TITLE, title);
		description = ((TextView) getView().findViewById(R.id.description)).getText().toString();
		contentValues.put(RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION, description);
		return contentValues;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		adapter = new CursorAdapter(getActivity(), null, 0)
		{
			@Override
			public void bindView(final View view, final Context context, final Cursor cursor)
			{
				final int titleColumnIndex = cursor.getColumnIndex(RecipeContract.Recipes.COLUMN_NAME_TITLE);
				title = cursor.getString(titleColumnIndex);
				final int descriptionColumnIndex = cursor
						.getColumnIndex(RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION);
				description = cursor.getString(descriptionColumnIndex);
			}

			@Override
			public View newView(final Context context, final Cursor cursor, final ViewGroup parent)
			{
				// View is already inflated in onCreateView
				return null;
			}
		};
		if (savedInstanceState != null)
		{
			title = savedInstanceState.getString(RecipeContract.Recipes.COLUMN_NAME_TITLE);
			description = savedInstanceState.getString(RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION);
			// No longer need the loader as we have valid local copies (which may have changes) from now on
			getLoaderManager().destroyLoader(0);
			updateViews();
		}
		else if (!Intent.ACTION_INSERT.equals(getActivity().getIntent().getAction()))
			getLoaderManager().initLoader(0, null, this);
		else
		{
			clear();
			updateViews();
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args)
	{
		return new CursorLoader(getActivity(), getActivity().getIntent().getData(), null, null, null, null);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		final String action = getActivity().getIntent().getAction();
		final boolean isEdit = Intent.ACTION_INSERT.equals(action) || Intent.ACTION_EDIT.equals(action);
		final int layoutId = isEdit ? R.layout.fragment_summary_edit : R.layout.fragment_summary_detail;
		return inflater.inflate(layoutId, container, false);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> data)
	{
		adapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data)
	{
		adapter.swapCursor(data);
		if (data.moveToFirst())
			adapter.bindView(getView(), getActivity(), data);
		else
			clear();
		updateViews();
	}

	@Override
	public void onSaveInstanceState(final Bundle outState)
	{
		super.onSaveInstanceState(outState);
		title = ((TextView) getView().findViewById(R.id.title)).getText().toString();
		outState.putString(RecipeContract.Recipes.COLUMN_NAME_TITLE, title);
		description = ((TextView) getView().findViewById(R.id.description)).getText().toString();
		outState.putString(RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION, description);
	}

	private void updateViews()
	{
		final View view = getView();
		final TextView titleView = (TextView) view.findViewById(R.id.title);
		titleView.setText(title);
		final TextView descriptionView = (TextView) view.findViewById(R.id.description);
		descriptionView.setText(description);
	}
}
