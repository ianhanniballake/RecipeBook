package com.ianhanniballake.recipebook.ui;

import java.util.List;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuCompat;
import android.support.v4.view.PagerAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Activity which displays only the Recipe details
 */
public class RecipeDetailEditFragment extends RecipeDetailFragment
{
	/**
	 * Class which handles returning the appropriate page
	 */
	private class RecipePagerAdapter extends FragmentStatePagerAdapter
	{
		/**
		 * Creates a new adapter
		 * 
		 * @param fm
		 *            The FragmentManager used to store/retrieve fragments from
		 */
		public RecipePagerAdapter(final FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public int getCount()
		{
			return 2;
		}

		@Override
		public Fragment getItem(final int position)
		{
			if (position == 0)
			{
				final RecipeSummaryFragment summary = new RecipeSummaryEditFragment();
				final Bundle args = new Bundle();
				args.putLong(BaseColumns._ID, recipeId);
				summary.setArguments(args);
				summaryFragment = summary;
				return summary;
			}
			else if (position == 1)
			{
				final RecipeIngredientListEditFragment ingredients = new RecipeIngredientListEditFragment();
				final Bundle args = new Bundle();
				args.putLong(BaseColumns._ID, recipeId);
				ingredients.setArguments(args);
				ingredientFragment = ingredients;
				return ingredients;
			}
			return null;
		}
	}

	/**
	 * Result indicating a recipe deletion
	 */
	public static final int RESULT_DELETED = Integer.MIN_VALUE;
	/**
	 * Current number of ingredients that have been inserted
	 */
	private int currentIngredientInsertCount = 0;
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
	 * Reference to the current ingredient fragment used in saving the updated
	 * recipe
	 */
	private RecipeIngredientListEditFragment ingredientFragment;
	/**
	 * Handler for asynchronous updates of ingredients
	 */
	private AsyncQueryHandler ingredientQueryHandler;
	/**
	 * Listener that handles recipe edit finish events
	 */
	private OnRecipeEditFinishListener recipeEditFinishListener = null;
	/**
	 * Handler for asynchronous updates of recipes
	 */
	private AsyncQueryHandler recipeQueryHandler;
	/**
	 * Reference to the current summary fragment used in saving the updated
	 * recipe
	 */
	private RecipeSummaryFragment summaryFragment;
	/**
	 * Total number of ingredients that need to be inserted
	 */
	private int totalIngredientCount = 0;

	@Override
	protected PagerAdapter getPagerAdapter(final FragmentManager fm)
	{
		return new RecipePagerAdapter(fm);
	}

	/**
	 * Attaches to the parent activity, saving a reference to it to call back
	 * recipe delete events
	 * 
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(final Activity activity)
	{
		super.onAttach(activity);
		if (activity instanceof OnRecipeEditFinishListener)
			recipeEditFinishListener = (OnRecipeEditFinishListener) activity;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		recipeQueryHandler = new AsyncQueryHandler(getActivity()
				.getContentResolver())
		{
			@Override
			protected void onInsertComplete(final int token,
					final Object cookie, final Uri uri)
			{
				// Set the newly created recipe id
				final long newRecipeId = ContentUris.parseId(uri);
				recipeId = newRecipeId;
				// Don't need to delete existing ingredients as this is a new
				// recipe
				startIngredientInsert();
			}

			@Override
			protected void onUpdateComplete(final int token,
					final Object cookie, final int result)
			{
				// Delete all existing ingredients in preparation for loading
				// the updated ingredients
				ingredientQueryHandler
						.startDelete(
								0,
								null,
								RecipeContract.Ingredients.CONTENT_URI,
								RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID
										+ "=?",
								new String[] { Long.toString(recipeId) });
			}
		};
		ingredientQueryHandler = new AsyncQueryHandler(getActivity()
				.getContentResolver())
		{
			@Override
			protected void onDeleteComplete(final int token,
					final Object cookie, final int result)
			{
				// After we've delete all existing ingredients, start putting in
				// the new ingredients
				startIngredientInsert();
			}

			@Override
			protected void onInsertComplete(final int token,
					final Object cookie, final Uri uri)
			{
				currentIngredientInsertCount++;
				if (currentIngredientInsertCount == totalIngredientCount)
					onIngredientInsertsComplete();
			}
		};
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
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_recipe_edit, menu);
		MenuCompat.setShowAsAction(menu.findItem(R.id.save), 2);
		MenuCompat.setShowAsAction(menu.findItem(R.id.cancel), 2);
	}

	/**
	 * Called when all ingredients have been inserted
	 */
	private void onIngredientInsertsComplete()
	{
		Toast.makeText(getActivity(), getText(R.string.saved),
				Toast.LENGTH_SHORT).show();
		if (recipeEditFinishListener != null)
			recipeEditFinishListener.onRecipeEditSave(recipeId);
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
				if (recipeId == 0)
					recipeQueryHandler.startInsert(0, null,
							RecipeContract.Recipes.CONTENT_ID_URI_BASE,
							summaryFragment.getContentValues());
				else
				{
					final Uri updateUri = ContentUris.withAppendedId(
							RecipeContract.Recipes.CONTENT_ID_URI_PATTERN,
							recipeId);
					recipeQueryHandler.startUpdate(0, null, updateUri,
							summaryFragment.getContentValues(), null, null);
				}
				return true;
			case R.id.cancel:
				hideKeyboard.onFocusChange(title, false);
				hideKeyboard.onFocusChange(description, false);
				getActivity().getSupportFragmentManager().popBackStack(
						"toEdit", FragmentManager.POP_BACK_STACK_INCLUSIVE);
				if (recipeEditFinishListener != null)
					recipeEditFinishListener.onRecipeEditCancelled();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Kicks off the insertion of all of the updated ingredients
	 */
	private void startIngredientInsert()
	{
		final List<ContentValues> allContentValues = ingredientFragment
				.getContentValues();
		int currentToken = 0;
		totalIngredientCount = allContentValues.size();
		if (totalIngredientCount == 0)
		{
			// onIngredientInsertsComplete();
			// return;
			// Insert some fake ingredients
			for (int h = 0; h < 50; h++)
			{
				final ContentValues contentValues = new ContentValues();
				contentValues.put(
						RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID,
						recipeId);
				allContentValues.add(contentValues);
				contentValues.put(
						RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, h + 1);
				if (h % 5 == 0)
				{
					contentValues
							.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR,
									1);
					contentValues
							.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR,
									h + 1);
				}
				contentValues.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT,
						"lbs");
				contentValues.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM,
						"top sirloin");
				contentValues.put(
						RecipeContract.Ingredients.COLUMN_NAME_PREPARATION,
						"grilled");
			}
			totalIngredientCount = allContentValues.size();
		}
		currentIngredientInsertCount = 0;
		for (final ContentValues contentValues : allContentValues)
			ingredientQueryHandler.startInsert(++currentToken, null,
					RecipeContract.Ingredients.CONTENT_URI, contentValues);
	}
}
