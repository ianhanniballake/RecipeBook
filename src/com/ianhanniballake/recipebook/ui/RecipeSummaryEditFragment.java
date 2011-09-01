package com.ianhanniballake.recipebook.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.MenuCompat;
import android.support.v4.widget.SimpleCursorAdapter;
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
public class RecipeSummaryEditFragment extends RecipeSummaryFragment
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

	@Override
	protected SimpleCursorAdapter createAdapter()
	{
		return new SimpleCursorAdapter(getActivity(),
				R.layout.fragment_recipe_summary, null, new String[] {
						RecipeContract.Recipes.COLUMN_NAME_TITLE,
						RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION },
				new int[] { R.id.title, R.id.description }, 0);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
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
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		final TextView title = (TextView) getActivity()
				.findViewById(R.id.title);
		final TextView description = (TextView) getActivity().findViewById(
				R.id.description);
		switch (item.getItemId())
		{
			case R.id.save:
				hideKeyboard.onFocusChange(title, false);
				hideKeyboard.onFocusChange(description, false);
				recipeEditListener.onRecipeEditSave(getRecipeId(),
						getContentValues());
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
