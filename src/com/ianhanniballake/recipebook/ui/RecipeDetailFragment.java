package com.ianhanniballake.recipebook.ui;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * A fragment representing a single Recipe detail screen. This fragment is either contained in a
 * {@link RecipeListActivity} in two-pane mode (on tablets) or a {@link RecipeDetailActivity} on handsets.
 */
public class RecipeDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
	/**
	 * Adapter to display the detailed data
	 */
	private CursorAdapter adapter;
	private long recipeId;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation
	 * changes).
	 */
	public RecipeDetailFragment()
	{
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
				final TextView titleView = (TextView) view.findViewById(R.id.title);
				final int titleColumnIndex = cursor.getColumnIndex(RecipeContract.Recipes.COLUMN_NAME_TITLE);
				final String title = cursor.getString(titleColumnIndex);
				titleView.setText(title);
				final TextView descriptionView = (TextView) view.findViewById(R.id.description);
				final int descriptionColumnIndex = cursor
						.getColumnIndex(RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION);
				final String description = cursor.getString(descriptionColumnIndex);
				descriptionView.setText(description);
			}

			@Override
			public View newView(final Context context, final Cursor cursor, final ViewGroup parent)
			{
				// View is already inflated in onCreateView
				return null;
			}
		};
		if (getArguments().containsKey(BaseColumns._ID))
		{
			recipeId = getArguments().getLong(BaseColumns._ID, 0);
			getLoaderManager().initLoader(0, null, this);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args)
	{
		final Uri recipeUri = ContentUris.withAppendedId(RecipeContract.Recipes.CONTENT_ID_URI_PATTERN, recipeId);
		return new CursorLoader(getActivity(), recipeUri, null, null, null, null);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_recipe_summary, container, false);
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
