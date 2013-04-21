package com.ianhanniballake.recipebook.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.model.Instruction;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Fragment which displays the list of instructions for a given recipe
 */
public class RecipeDetailInstructionFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>
{
	/**
	 * Manages the list of instructions as an Array backed list
	 */
	public class InstructionArrayAdapter extends ArrayAdapter<Instruction>
	{
		private final int textViewResourceId;

		/**
		 * Constructor
		 * 
		 * @param context
		 *            The current context.
		 * @param resource
		 *            The resource ID for a layout file containing a layout to use when instantiating views.
		 * @param textViewResourceId
		 *            The id of the TextView within the layout resource to be populated
		 */
		public InstructionArrayAdapter(final Context context, final int resource, final int textViewResourceId)
		{
			super(context, resource, textViewResourceId);
			this.textViewResourceId = textViewResourceId;
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent)
		{
			// Need to ensure the correct position is set before super.getView sets the text
			if (convertView != null)
				convertView.setTag(position);
			final View view = super.getView(position, convertView, parent);
			final String action = getActivity().getIntent().getAction();
			final boolean isView = Intent.ACTION_VIEW.equals(action) || Intent.ACTION_SEARCH.equals(action);
			if (!isView)
			{
				final EditText editText = (EditText) view.findViewById(textViewResourceId);
				editText.addTextChangedListener(new TextWatcher()
				{
					@Override
					public void afterTextChanged(final Editable s)
					{
						// Nothing to do
					}

					@Override
					public void beforeTextChanged(final CharSequence s, final int start, final int count,
							final int after)
					{
						// Nothing to do
					}

					@Override
					public void onTextChanged(final CharSequence s, final int start, final int before, final int count)
					{
						final int savedPosition = view.getTag() == null ? position : (Integer) view.getTag();
						getItem(savedPosition).setInstruction(s.toString());
					}
				});
				final ImageButton deleteInstruction = (ImageButton) view.findViewById(R.id.delete_instruction);
				deleteInstruction.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(final View v)
					{
						final int savedPosition = view.getTag() == null ? position : (Integer) view.getTag();
						remove(getItem(savedPosition));
					}
				});
			}
			return view;
		}
	}

	/**
	 * Adapter to display the list's data
	 */
	private InstructionArrayAdapter adapter;
	/**
	 * Current list of instructions
	 */
	List<Instruction> instructions = new ArrayList<Instruction>();

	/**
	 * Gets a ContentValues object for each instruction
	 * 
	 * @return ContentValues for each instruction
	 */
	public ContentValues[] getContentValuesArray()
	{
		final long recipeId = ContentUris.parseId(getActivity().getIntent().getData());
		final int instructionCount = adapter.getCount();
		final List<ContentValues> instructionContentValuesList = new ArrayList<ContentValues>();
		for (int position = 0; position < instructionCount; position++)
		{
			final Instruction instruction = adapter.getItem(position);
			if (!instruction.toString().isEmpty())
				instructionContentValuesList.add(instruction.toContentValues(recipeId));
		}
		final ContentValues[] instructionContentValuesArray = new ContentValues[instructionContentValuesList.size()];
		return instructionContentValuesList.toArray(instructionContentValuesArray);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		final String action = getActivity().getIntent().getAction();
		final boolean isView = Intent.ACTION_VIEW.equals(action) || Intent.ACTION_SEARCH.equals(action);
		final int layoutId = isView ? R.layout.list_item_instruction : R.layout.list_item_instruction_edit;
		setHasOptionsMenu(!isView);
		adapter = new InstructionArrayAdapter(getActivity(), layoutId, R.id.instruction);
		setListAdapter(adapter);
		getListView().setChoiceMode(AbsListView.CHOICE_MODE_NONE);
		if (savedInstanceState != null)
		{
			// No longer need the loader as we have valid local copies (which may have changes) from now on
			getLoaderManager().destroyLoader(0);
			final List<String> instructionList = savedInstanceState
					.getStringArrayList(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION);
			for (final String instruction : instructionList)
				instructions.add(new Instruction(instruction));
			updateViews();
		}
		else if (!Intent.ACTION_INSERT.equals(getActivity().getIntent().getAction()))
			getLoaderManager().initLoader(0, null, this);
		else
			updateViews();
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
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.instruction_edit, menu);
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
		updateViews();
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data)
	{
		final int instructionColumnIndex = data.getColumnIndex(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION);
		instructions.clear();
		data.moveToPosition(-1);
		while (data.moveToNext())
			instructions.add(new Instruction(data.getString(instructionColumnIndex)));
		updateViews();
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.add_instruction:
				final Instruction newInstruction = new Instruction("");
				instructions.add(newInstruction);
				adapter.add(newInstruction);
				getListView().setSelection(adapter.getPosition(newInstruction));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle outState)
	{
		super.onSaveInstanceState(outState);
		final int instructionCount = adapter.getCount();
		final ArrayList<String> instructionList = new ArrayList<String>();
		for (int position = 0; position < instructionCount; position++)
		{
			final String rawInstruction = adapter.getItem(position).toString();
			if (!rawInstruction.isEmpty())
				instructionList.add(rawInstruction);
		}
		outState.putStringArrayList(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION, instructionList);
	}

	private void updateViews()
	{
		adapter.setNotifyOnChange(false);
		adapter.clear();
		adapter.addAll(instructions);
		adapter.notifyDataSetChanged();
	}
}
