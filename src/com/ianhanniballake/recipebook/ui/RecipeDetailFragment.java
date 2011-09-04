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
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Activity which displays only the Recipe details
 */
public class RecipeDetailFragment extends Fragment implements
		OnBackStackChangedListener
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
				final RecipeSummaryFragment summary;
				if (isEditing)
					summary = new RecipeSummaryEditFragment();
				else
					summary = new RecipeSummaryViewFragment();
				final Bundle args = new Bundle();
				args.putLong(BaseColumns._ID, getRecipeId());
				summary.setArguments(args);
				summaryFragment = summary;
				return summary;
			}
			else if (position == 1)
			{
				final RecipeIngredientListFragment ingredients;
				if (isEditing)
					ingredients = new RecipeIngredientListEditFragment();
				else
					ingredients = new RecipeIngredientListViewFragment();
				final Bundle args = new Bundle();
				args.putLong(BaseColumns._ID, getRecipeId());
				ingredients.setArguments(args);
				ingredientFragment = ingredients;
				return ingredients;
			}
			return null;
		}

		@Override
		public int getItemPosition(final Object object)
		{
			if (object instanceof RecipeSummaryEditFragment)
				return isEditing ? POSITION_UNCHANGED : POSITION_NONE;
			if (object instanceof RecipeSummaryViewFragment)
				return isEditing ? POSITION_NONE : POSITION_UNCHANGED;
			if (object instanceof RecipeIngredientListEditFragment)
				return isEditing ? POSITION_UNCHANGED : POSITION_NONE;
			if (object instanceof RecipeIngredientListViewFragment)
				return isEditing ? POSITION_NONE : POSITION_UNCHANGED;
			return POSITION_UNCHANGED;
		}
	}

	/**
	 * Result indicating a recipe deletion
	 */
	public static final int RESULT_DELETED = Integer.MIN_VALUE;
	/**
	 * PagerAdapter to use to access recipe detail fragments
	 */
	private RecipePagerAdapter adapter;
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
	private RecipeIngredientListFragment ingredientFragment;
	/**
	 * Handler for asynchronous updates of ingredients
	 */
	private AsyncQueryHandler ingredientQueryHandler;
	/**
	 * Whether we are currently editing the recipe
	 */
	private boolean isEditing = false;
	/**
	 * Listener that handles recipe delete events
	 */
	private OnRecipeDeleteListener recipeDeleteListener;
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
		if (savedInstanceState != null)
			isEditing = savedInstanceState.getBoolean("isEditing", false);
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
		try
		{
			recipeDeleteListener = (OnRecipeDeleteListener) activity;
		} catch (final ClassCastException e)
		{
			throw new ClassCastException(activity.toString()
					+ " must implement OnRecipeDeleteListener");
		}
	}

	@Override
	public void onBackStackChanged()
	{
		getActivity().findViewById(android.R.id.content).post(new Runnable()
		{
			@Override
			public void run()
			{
				isEditing = !isEditing;
				adapter.notifyDataSetChanged();
			}
		});
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
			protected void onDeleteComplete(final int token,
					final Object cookie, final int result)
			{
				Toast.makeText(getActivity(), getText(R.string.deleted),
						Toast.LENGTH_SHORT).show();
				recipeDeleteListener.onRecipeDeleted();
			}

			@Override
			protected void onUpdateComplete(final int token,
					final Object cookie, final int result)
			{
				Toast.makeText(getActivity(), getText(R.string.saved),
						Toast.LENGTH_SHORT).show();
			}
		};
		ingredientQueryHandler = new AsyncQueryHandler(getActivity()
				.getContentResolver())
		{
			@Override
			protected void onDeleteComplete(final int token,
					final Object cookie, final int result)
			{
				final List<ContentValues> allContentValues = ingredientFragment
						.getContentValues();
				int currentToken = 0;
				for (final ContentValues contentValues : allContentValues)
					ingredientQueryHandler.startInsert(++currentToken, null,
							RecipeContract.Ingredients.CONTENT_URI,
							contentValues);
				getActivity().getSupportFragmentManager().popBackStack(
						"toEdit", FragmentManager.POP_BACK_STACK_INCLUSIVE);
			}

			@Override
			protected void onUpdateComplete(final int token,
					final Object cookie, final int result)
			{
				// TODO count number of returned updates to check when we can
				// return
			}
		};
		getActivity().getSupportFragmentManager()
				.addOnBackStackChangedListener(this);
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
		if (isEditing)
		{
			inflater.inflate(R.menu.fragment_recipe_edit, menu);
			MenuCompat.setShowAsAction(menu.findItem(R.id.save), 2);
			MenuCompat.setShowAsAction(menu.findItem(R.id.cancel), 2);
		}
		else
		{
			inflater.inflate(R.menu.fragment_recipe_summary, menu);
			MenuCompat.setShowAsAction(menu.findItem(R.id.edit), 2);
			MenuCompat.setShowAsAction(menu.findItem(R.id.delete), 2);
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState)
	{
		final View v = inflater.inflate(R.layout.fragment_recipe_detail,
				container, false);
		final ViewPager pager = (ViewPager) v.findViewById(R.id.pager);
		adapter = new RecipePagerAdapter(getActivity()
				.getSupportFragmentManager());
		pager.setAdapter(adapter);
		return v;
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
			case R.id.edit:
				onRecipeEditStarted();
				return true;
			case R.id.delete:
				onRecipeDeleted();
				return true;
			case R.id.save:
				hideKeyboard.onFocusChange(title, false);
				hideKeyboard.onFocusChange(description, false);
				onRecipeEditSave();
				return true;
			case R.id.cancel:
				hideKeyboard.onFocusChange(title, false);
				hideKeyboard.onFocusChange(description, false);
				onRecipeEditCancelled();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Hides the edit and delete items if this is not currently showing a valid
	 * recipe
	 * 
	 * @see android.support.v4.app.Fragment#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public void onPrepareOptionsMenu(final Menu menu)
	{
		menu.findItem(R.id.edit).setVisible(getRecipeId() != 0);
		menu.findItem(R.id.delete).setVisible(getRecipeId() != 0);
	}

	/**
	 * Handles recipe delete events
	 */
	public void onRecipeDeleted()
	{
		final Uri deleteUri = ContentUris.withAppendedId(
				RecipeContract.Recipes.CONTENT_ID_URI_PATTERN, getRecipeId());
		recipeQueryHandler.startDelete(0, null, deleteUri, null, null);
	}

	/**
	 * Handles recipe edit cancellation events
	 */
	public void onRecipeEditCancelled()
	{
		getActivity().getSupportFragmentManager().popBackStack("toEdit",
				FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}

	/**
	 * Handles recipe save events
	 */
	public void onRecipeEditSave()
	{
		final Uri updateUri = ContentUris.withAppendedId(
				RecipeContract.Recipes.CONTENT_ID_URI_PATTERN, getRecipeId());
		recipeQueryHandler.startUpdate(0, null, updateUri,
				summaryFragment.getContentValues(), null, null);
		ingredientQueryHandler.startDelete(0, null,
				RecipeContract.Ingredients.CONTENT_URI,
				RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID + "=?",
				new String[] { Long.toString(getRecipeId()) });
	}

	/**
	 * Handles recipe edit start events
	 */
	public void onRecipeEditStarted()
	{
		final FragmentTransaction ft = getActivity()
				.getSupportFragmentManager().beginTransaction();
		ft.addToBackStack("toEdit");
		ft.commit();
	}

	@Override
	public void onSaveInstanceState(final Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putBoolean("isEditing", isEditing);
	}
}
