package com.ianhanniballake.recipebook.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentUris;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Fragment which displays the list of instructions for a given recipe
 */
public class RecipeDetailInstructionFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>
{
	/**
	 * Adapter to display the list's data
	 */
	private ArrayAdapter<String> adapter;

	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_instruction, R.id.instruction);
		setListAdapter(adapter);
		getListView().setChoiceMode(AbsListView.CHOICE_MODE_NONE);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args)
	{
		final long recipeId = ContentUris.parseId(getActivity().getIntent().getData());
		return new CursorLoader(getActivity(), RecipeContract.Instructions.CONTENT_URI, null,
				RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID + "=?", new String[] { Long.toString(recipeId) },
				null);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_instruction_detail, container, false);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader)
	{
		adapter.clear();
		adapter.notifyDataSetInvalidated();
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data)
	{
		final int instructionColumnIndex = data.getColumnIndex(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION);
		adapter.setNotifyOnChange(false);
		adapter.clear();
		final List<String> newInstructions = new ArrayList<String>();
		data.moveToPosition(-1);
		while (data.moveToNext())
			newInstructions.add(data.getString(instructionColumnIndex));
		adapter.addAll(newInstructions);
		adapter.notifyDataSetChanged();
	}
}
