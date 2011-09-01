package com.ianhanniballake.recipebook.ui;

import java.util.List;

import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Activity which displays only the Recipe details
 */
public class RecipeDetailActivity extends FragmentActivity implements
		OnRecipeEditListener, OnBackStackChangedListener
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
				final RecipeDetailFragment details;
				if (isEditing)
					details = new RecipeSummaryEditFragment();
				else
					details = new RecipeSummaryViewFragment();
				final Bundle args = new Bundle();
				args.putLong(BaseColumns._ID, getRecipeId());
				details.setArguments(args);
				return details;
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
	 * Handler for asynchronous updates of recipes
	 */
	private AsyncQueryHandler recipeQueryHandler;

	/**
	 * Getter for the ID associated with the currently displayed recipe
	 * 
	 * @return ID for the currently displayed recipe
	 */
	private long getRecipeId()
	{
		if (getIntent() == null || getIntent().getExtras() == null)
			return 0;
		return getIntent().getExtras().getLong(BaseColumns._ID, 0);
	}

	@Override
	public void onBackStackChanged()
	{
		findViewById(android.R.id.content).post(new Runnable()
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
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe_detail);
		if (findViewById(R.id.details) == null)
		{
			// A null details view means we no longer need this activity
			finish();
			return;
		}
		final ViewPager pager = (ViewPager) findViewById(R.id.details);
		adapter = new RecipePagerAdapter(getSupportFragmentManager());
		pager.setAdapter(adapter);
		recipeQueryHandler = new AsyncQueryHandler(getContentResolver())
		{
			@Override
			protected void onDeleteComplete(final int token,
					final Object cookie, final int result)
			{
				Toast.makeText(RecipeDetailActivity.this,
						getText(R.string.deleted), Toast.LENGTH_SHORT).show();
				setResult(RESULT_DELETED);
				finish();
			}

			@Override
			protected void onUpdateComplete(final int token,
					final Object cookie, final int result)
			{
				Toast.makeText(RecipeDetailActivity.this,
						getText(R.string.saved), Toast.LENGTH_SHORT).show();
			}
		};
		ingredientQueryHandler = new AsyncQueryHandler(getContentResolver())
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
				getSupportFragmentManager().popBackStack("toEdit",
						FragmentManager.POP_BACK_STACK_INCLUSIVE);
			}

			@Override
			protected void onUpdateComplete(final int token,
					final Object cookie, final int result)
			{
				// TODO count number of returned updates to check when we can
				// return
			}
		};
		getSupportFragmentManager().addOnBackStackChangedListener(this);
	}

	@Override
	public void onRecipeDeleted(final long recipeId)
	{
		final Uri deleteUri = ContentUris.withAppendedId(
				RecipeContract.Recipes.CONTENT_ID_URI_PATTERN, recipeId);
		recipeQueryHandler.startDelete(0, null, deleteUri, null, null);
	}

	@Override
	public void onRecipeEditCancelled()
	{
		getSupportFragmentManager().popBackStack("toEdit",
				FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}

	@Override
	public void onRecipeEditSave(final long recipeId, final ContentValues values)
	{
		final Uri updateUri = ContentUris.withAppendedId(
				RecipeContract.Recipes.CONTENT_ID_URI_PATTERN, recipeId);
		recipeQueryHandler.startUpdate(0, null, updateUri, values, null, null);
		recipeQueryHandler.startDelete(0, null,
				RecipeContract.Ingredients.CONTENT_URI,
				RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID + "=?",
				new String[] { Long.toString(recipeId) });
	}

	@Override
	public void onRecipeEditStarted(final long recipeId)
	{
		final FragmentTransaction ft = getSupportFragmentManager()
				.beginTransaction();
		ft.addToBackStack("toEdit");
		ft.commit();
	}

	@Override
	public void onRestoreInstanceState(final Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		isEditing = savedInstanceState.getBoolean("isEditing", false);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putBoolean("isEditing", isEditing);
	}
}
