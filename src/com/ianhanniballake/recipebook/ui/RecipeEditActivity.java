package com.ianhanniballake.recipebook.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * An activity for editing recipes
 */
public class RecipeEditActivity extends FragmentActivity
{
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages. Supports
	 * using a ViewPager if R.id.pager is found or a three pane detail setup
	 */
	public static class RecipeEditTabsAdapter extends FragmentPagerAdapter implements ActionBar.TabListener,
			ViewPager.OnPageChangeListener
	{
		private final ActionBar actionBar;
		private final FragmentActivity activity;
		private final ViewPager pager;

		/**
		 * Manages the set of static fragments. Make sure you call setup()!
		 * 
		 * @param activity
		 *            Activity to show the summary/ingredient/instruction fragments
		 */
		public RecipeEditTabsAdapter(final FragmentActivity activity)
		{
			super(activity.getSupportFragmentManager());
			this.activity = activity;
			actionBar = activity.getActionBar();
			pager = (ViewPager) activity.findViewById(R.id.pager);
		}

		@Override
		public int getCount()
		{
			// Show 3 total pages.
			return 3;
		}

		/**
		 * Returns the current recipe ingredient fragment
		 * 
		 * @return The current recipe ingredient fragment
		 */
		public RecipeDetailIngredientFragment getIngredientFragment()
		{
			if (pager == null)
				return (RecipeDetailIngredientFragment) activity.getSupportFragmentManager().findFragmentById(
						R.id.recipe_detail_ingredient);
			return (RecipeDetailIngredientFragment) activity.getSupportFragmentManager().findFragmentByTag(
					"android:switcher:" + pager.getId() + ":1");
		}

		/**
		 * Returns the current recipe instruction fragment
		 * 
		 * @return The current recipe instruction fragment
		 */
		public RecipeDetailInstructionFragment getInstructionFragment()
		{
			if (pager == null)
				return (RecipeDetailInstructionFragment) activity.getSupportFragmentManager().findFragmentById(
						R.id.recipe_detail_instruction);
			return (RecipeDetailInstructionFragment) activity.getSupportFragmentManager().findFragmentByTag(
					"android:switcher:" + pager.getId() + ":2");
		}

		@Override
		public Fragment getItem(final int position)
		{
			switch (position)
			{
				case 0:
					return new RecipeDetailSummaryFragment();
				case 1:
					return new RecipeDetailIngredientFragment();
				case 2:
					return new RecipeDetailInstructionFragment();
				default:
					return null;
			}
		}

		@Override
		public CharSequence getPageTitle(final int position)
		{
			final Locale l = Locale.getDefault();
			switch (position)
			{
				case 0:
					return activity.getString(R.string.title_summary).toUpperCase(l);
				case 1:
					return activity.getString(R.string.title_ingredient_list).toUpperCase(l);
				case 2:
					return activity.getString(R.string.title_instruction_list).toUpperCase(l);
				default:
					return null;
			}
		}

		/**
		 * Returns the current recipe summary fragment
		 * 
		 * @return The current recipe summary fragment
		 */
		public RecipeDetailSummaryFragment getSummaryFragment()
		{
			if (pager == null)
				return (RecipeDetailSummaryFragment) activity.getSupportFragmentManager().findFragmentById(
						R.id.recipe_detail_summary);
			return (RecipeDetailSummaryFragment) activity.getSupportFragmentManager().findFragmentByTag(
					"android:switcher:" + pager.getId() + ":0");
		}

		@Override
		public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels)
		{
			// Nothing to do
		}

		@Override
		public void onPageScrollStateChanged(final int state)
		{
			// Nothing to do
		}

		@Override
		public void onPageSelected(final int position)
		{
			actionBar.setSelectedNavigationItem(position);
		}

		@Override
		public void onTabReselected(final ActionBar.Tab tab, final FragmentTransaction fragmentTransaction)
		{
			// Nothing to do
		}

		@Override
		public void onTabSelected(final ActionBar.Tab tab, final FragmentTransaction fragmentTransaction)
		{
			// When the given tab is selected, switch to the corresponding page in the ViewPager.
			pager.setCurrentItem(tab.getPosition());
		}

		@Override
		public void onTabUnselected(final ActionBar.Tab tab, final FragmentTransaction fragmentTransaction)
		{
			// Nothing to do
		}

		/**
		 * Ties the pager and tabs together
		 */
		public void setup()
		{
			if (pager == null)
			{
				final FragmentManager fragmentManager = activity.getSupportFragmentManager();
				if (fragmentManager.findFragmentByTag("summary") == null)
				{
					final android.support.v4.app.FragmentTransaction ft = fragmentManager.beginTransaction();
					ft.replace(R.id.recipe_detail_summary, getItem(0), "summary");
					ft.replace(R.id.recipe_detail_ingredient, getItem(1), "ingredient");
					ft.replace(R.id.recipe_detail_instruction, getItem(2), "instruction");
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					ft.commit();
				}
			}
			else
			{
				pager.setAdapter(this);
				pager.setOnPageChangeListener(this);
				actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
				// For each of the sections in the app, add a tab to the action bar.
				for (int i = 0; i < getCount(); i++)
					actionBar.addTab(actionBar.newTab().setText(getPageTitle(i)).setTabListener(this));
			}
		}
	}

	private static class SaveAsyncTask extends AsyncTask<Fragment, Void, Long>
	{
		private final WeakReference<FragmentActivity> activityRef;

		SaveAsyncTask(final FragmentActivity activity)
		{
			activityRef = new WeakReference<FragmentActivity>(activity);
		}

		@Override
		protected Long doInBackground(final Fragment... params)
		{
			final FragmentActivity activity = activityRef.get();
			if (activity == null)
				return -1L;
			final ContentResolver resolver = activity.getContentResolver();
			final RecipeDetailSummaryFragment summaryFragment = (RecipeDetailSummaryFragment) params[0];
			final ContentValues recipeValues = summaryFragment.getContentValues();
			final Uri recipeUri = activity.getIntent().getData();
			long recipeId = recipeUri == null ? -1 : ContentUris.parseId(recipeUri);
			if (Intent.ACTION_INSERT.equals(activity.getIntent().getAction()))
			{
				final Uri newRow = resolver.insert(RecipeContract.Recipes.CONTENT_ID_URI_BASE, recipeValues);
				if (newRow == null)
					return -1L;
				recipeId = ContentUris.parseId(newRow);
				activity.getIntent().setData(newRow);
			}
			else
				resolver.update(recipeUri, recipeValues, null, null);
			// Insert ingredients
			final String ingredientSelection = RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID + "=?";
			final String[] ingredientSelectionArgs = { Long.toString(recipeId) };
			resolver.delete(RecipeContract.Ingredients.CONTENT_ID_URI_BASE, ingredientSelection,
					ingredientSelectionArgs);
			final RecipeDetailIngredientFragment ingredientFragment = (RecipeDetailIngredientFragment) params[1];
			final ContentValues[] ingredientValuesArray = ingredientFragment.getContentValuesArray();
			final int insertedIngredientCount = resolver.bulkInsert(RecipeContract.Ingredients.CONTENT_ID_URI_BASE,
					ingredientValuesArray);
			if (insertedIngredientCount != ingredientValuesArray.length)
				return -1L;
			// Insert instructions
			final String instructionSelection = RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID + "=?";
			final String[] instructionSelectionArgs = { Long.toString(recipeId) };
			resolver.delete(RecipeContract.Instructions.CONTENT_ID_URI_BASE, instructionSelection,
					instructionSelectionArgs);
			final RecipeDetailInstructionFragment instructionFragment = (RecipeDetailInstructionFragment) params[2];
			final ContentValues[] instructionValuesArray = instructionFragment.getContentValuesArray();
			final int insertedInstructionCount = resolver.bulkInsert(RecipeContract.Instructions.CONTENT_ID_URI_BASE,
					instructionValuesArray);
			if (insertedInstructionCount != instructionValuesArray.length)
				return -1L;
			return recipeId;
		}

		@Override
		protected void onPostExecute(final Long result)
		{
			final FragmentActivity activity = activityRef.get();
			if (result != -1 && activity != null)
			{
				Toast.makeText(activity, R.string.saved, Toast.LENGTH_SHORT).show();
				activity.finish();
			}
		}
	}

	/**
	 * Manages the fragments associated with this activity
	 */
	RecipeEditTabsAdapter fragmentAdapter;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe_detail);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		// Create the adapter that will return a fragment for each of the three tabs
		fragmentAdapter = new RecipeEditTabsAdapter(this);
		fragmentAdapter.setup();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.recipe_edit, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				// This ID represents the Home or Up button. In the case of this activity, the Up button is shown.
				// In our case, we always get launched from the parent, so it is okay to just finish();
				finish();
				return true;
			case R.id.save:
				final List<Fragment> fragments = new ArrayList<Fragment>();
				fragments.add(fragmentAdapter.getSummaryFragment());
				fragments.add(fragmentAdapter.getIngredientFragment());
				fragments.add(fragmentAdapter.getInstructionFragment());
				new SaveAsyncTask(this).execute(fragmentAdapter.getSummaryFragment(),
						fragmentAdapter.getIngredientFragment(), fragmentAdapter.getInstructionFragment());
				return true;
			case R.id.cancel:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
