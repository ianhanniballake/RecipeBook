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
import com.ianhanniballake.recipebook.model.Ingredient;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Fragment which displays the list of ingredients for a given recipe
 */
public class RecipeDetailIngredientFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>
{
	/**
	 * Manages the list of ingredients as an Array backed list
	 */
	public class IngredientArrayAdapter extends ArrayAdapter<Ingredient>
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
		public IngredientArrayAdapter(final Context context, final int resource, final int textViewResourceId)
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
			if (!Intent.ACTION_VIEW.equals(getActivity().getIntent().getAction()))
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
						getItem(savedPosition).setFromRaw(view.getResources(), s.toString());
					}
				});
				final ImageButton deleteIngredient = (ImageButton) view.findViewById(R.id.delete_ingredient);
				deleteIngredient.setOnClickListener(new OnClickListener()
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
	private IngredientArrayAdapter adapter;
	/**
	 * Current list of ingredients
	 */
	List<Ingredient> ingredients = new ArrayList<Ingredient>();

	/**
	 * Gets a ContentValues object for each ingredient
	 * 
	 * @return ContentValues for each ingredient
	 */
	public ContentValues[] getContentValuesArray()
	{
		final long recipeId = ContentUris.parseId(getActivity().getIntent().getData());
		final int ingredientCount = adapter.getCount();
		final List<ContentValues> ingredientContentValuesList = new ArrayList<ContentValues>();
		for (int position = 0; position < ingredientCount; position++)
		{
			final Ingredient ingredient = adapter.getItem(position);
			if (!ingredient.toString().isEmpty())
				ingredientContentValuesList.add(ingredient.toContentValues(recipeId));
		}
		final ContentValues[] ingredientContentValuesArray = new ContentValues[ingredientContentValuesList.size()];
		return ingredientContentValuesList.toArray(ingredientContentValuesArray);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		final boolean isView = Intent.ACTION_VIEW.equals(getActivity().getIntent().getAction());
		final int layoutId = isView ? R.layout.list_item_ingredient : R.layout.list_item_ingredient_edit;
		setHasOptionsMenu(!isView);
		adapter = new IngredientArrayAdapter(getActivity(), layoutId, R.id.raw);
		setListAdapter(adapter);
		getListView().setChoiceMode(AbsListView.CHOICE_MODE_NONE);
		if (savedInstanceState != null)
		{
			// No longer need the loader as we have valid local copies (which may have changes) from now on
			getLoaderManager().destroyLoader(0);
			final List<String> rawIngredients = savedInstanceState
					.getStringArrayList(RecipeContract.Ingredients.COLUMN_NAME_ITEM);
			for (final String rawText : rawIngredients)
				ingredients.add(new Ingredient(getResources(), rawText));
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
		return new CursorLoader(getActivity(), RecipeContract.Ingredients.CONTENT_URI, null,
				RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID + "=?", new String[] { Long.toString(recipeId) }, null);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.ingredient_edit, menu);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_ingredient_detail, container, false);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader)
	{
		ingredients.clear();
		updateViews();
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data)
	{
		ingredients.clear();
		data.moveToPosition(-1);
		while (data.moveToNext())
			ingredients.add(new Ingredient(data));
		updateViews();
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.add_ingredient:
				final Ingredient newIngredient = new Ingredient(getResources(), "");
				ingredients.add(newIngredient);
				adapter.add(newIngredient);
				getListView().setSelection(adapter.getPosition(newIngredient));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle outState)
	{
		super.onSaveInstanceState(outState);
		final int ingredientCount = adapter.getCount();
		final ArrayList<String> rawIngredients = new ArrayList<String>();
		for (int position = 0; position < ingredientCount; position++)
		{
			final String rawIngredient = adapter.getItem(position).toString();
			if (!rawIngredient.isEmpty())
				rawIngredients.add(rawIngredient);
		}
		outState.putStringArrayList(RecipeContract.Ingredients.COLUMN_NAME_ITEM, rawIngredients);
	}

	private void updateViews()
	{
		adapter.setNotifyOnChange(false);
		adapter.clear();
		adapter.addAll(ingredients);
		adapter.notifyDataSetChanged();
	}
}
