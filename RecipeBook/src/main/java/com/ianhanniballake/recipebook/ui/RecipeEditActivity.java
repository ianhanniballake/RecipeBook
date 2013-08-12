package com.ianhanniballake.recipebook.ui;

import java.lang.ref.WeakReference;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.auth.AuthorizedActivity;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * An activity for editing recipes
 */
public class RecipeEditActivity extends AuthorizedActivity
{
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages. Supports
	 * using a ViewPager if R.id.pager is found or a three pane detail setup
	 */
	public static class RecipeEditTabsAdapter extends FragmentPagerAdapter implements ActionBar.TabListener,
			ViewPager.OnPageChangeListener
	{
		private final ActionBar actionBar;
		private final Activity activity;
		private final ViewPager pager;

		/**
		 * Manages the set of static fragments. Make sure you call setup()!
		 * 
		 * @param activity
		 *            Activity to show the summary/ingredient/instruction fragments
		 */
		public RecipeEditTabsAdapter(final Activity activity)
		{
			super(activity.getFragmentManager());
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
				return (RecipeDetailIngredientFragment) activity.getFragmentManager().findFragmentById(
						R.id.recipe_detail_ingredient);
			return (RecipeDetailIngredientFragment) activity.getFragmentManager().findFragmentByTag(
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
				return (RecipeDetailInstructionFragment) activity.getFragmentManager().findFragmentById(
						R.id.recipe_detail_instruction);
			return (RecipeDetailInstructionFragment) activity.getFragmentManager().findFragmentByTag(
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
				return (RecipeDetailSummaryFragment) activity.getFragmentManager().findFragmentById(
						R.id.recipe_detail_summary);
			return (RecipeDetailSummaryFragment) activity.getFragmentManager().findFragmentByTag(
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
				final FragmentManager fragmentManager = activity.getFragmentManager();
				if (fragmentManager.findFragmentByTag("summary") == null)
				{
					final FragmentTransaction ft = fragmentManager.beginTransaction();
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
				pager.setOffscreenPageLimit(2);
				actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
				// For each of the sections in the app, add a tab to the action bar.
				for (int i = 0; i < getCount(); i++)
					actionBar.addTab(actionBar.newTab().setText(getPageTitle(i)).setTabListener(this));
			}
		}
	}

	private static class SaveAsyncTask extends AsyncTask<Fragment, Void, Long>
	{
		private final WeakReference<Activity> activityRef;

		SaveAsyncTask(final Activity activity)
		{
			activityRef = new WeakReference<Activity>(activity);
		}

		@Override
		protected Long doInBackground(final Fragment... params)
		{
			final Activity activity = activityRef.get();
			if (activity == null)
				return -1L;
			final ContentResolver resolver = activity.getContentResolver();
			final RecipeDetailSummaryFragment summaryFragment = (RecipeDetailSummaryFragment) params[0];
			final ContentValues recipeValues = summaryFragment.getContentValues();
			final boolean isInsert = Intent.ACTION_INSERT.equals(activity.getIntent().getAction());
			long recipeId;
			if (isInsert)
			{
				final Uri newRow = resolver.insert(RecipeContract.Recipes.CONTENT_ID_URI_BASE, recipeValues);
				if (newRow == null)
					return -1L;
				recipeId = ContentUris.parseId(newRow);
				activity.getIntent().setData(newRow);
			}
			else
			{
				final Uri recipeUri = activity.getIntent().getData();
				recipeId = ContentUris.parseId(recipeUri);
				resolver.update(recipeUri, recipeValues, null, null);
			}
			// Insert ingredients
			final RecipeDetailIngredientFragment ingredientFragment = (RecipeDetailIngredientFragment) params[1];
			final ContentValues[] ingredientValuesArray = ingredientFragment.getContentValuesArray();
			final String ingredientSelection = RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID + "=?";
			final String[] ingredientSelectionArgs = { Long.toString(recipeId) };
			resolver.delete(RecipeContract.Ingredients.CONTENT_ID_URI_BASE, ingredientSelection,
					ingredientSelectionArgs);
			final int insertedIngredientCount = resolver.bulkInsert(RecipeContract.Ingredients.CONTENT_ID_URI_BASE,
					ingredientValuesArray);
			if (insertedIngredientCount != ingredientValuesArray.length)
				return -1L;
			// Insert instructions
			final RecipeDetailInstructionFragment instructionFragment = (RecipeDetailInstructionFragment) params[2];
			final ContentValues[] instructionValuesArray = instructionFragment.getContentValuesArray();
			final String instructionSelection = RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID + "=?";
			final String[] instructionSelectionArgs = { Long.toString(recipeId) };
			resolver.delete(RecipeContract.Instructions.CONTENT_ID_URI_BASE, instructionSelection,
					instructionSelectionArgs);
			final int insertedInstructionCount = resolver.bulkInsert(RecipeContract.Instructions.CONTENT_ID_URI_BASE,
					instructionValuesArray);
			if (insertedInstructionCount != instructionValuesArray.length)
				return -1L;
			return recipeId;
		}

		@Override
		protected void onPostExecute(final Long result)
		{
			final Activity activity = activityRef.get();
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
	public void onBackPressed()
	{
		new SaveAsyncTask(RecipeEditActivity.this).execute(fragmentAdapter.getSummaryFragment(),
				fragmentAdapter.getIngredientFragment(), fragmentAdapter.getInstructionFragment());
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe_detail);
		// Create the adapter that will return a fragment for each of the three tabs
		fragmentAdapter = new RecipeEditTabsAdapter(this);
		fragmentAdapter.setup();
		// Inflate a "Save" custom action bar view to serve as the "Up" affordance.
		final LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext().getSystemService(
				LAYOUT_INFLATER_SERVICE);
		final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_save, null);
		customActionBarView.findViewById(R.id.save).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				new SaveAsyncTask(RecipeEditActivity.this).execute(fragmentAdapter.getSummaryFragment(),
						fragmentAdapter.getIngredientFragment(), fragmentAdapter.getInstructionFragment());
			}
		});
		// Show the custom action bar view and hide the normal Home icon and title.
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM
				| ActionBar.DISPLAY_SHOW_TITLE);
		actionBar.setCustomView(customActionBarView);
		// Work around for https://code.google.com/p/android/issues/detail?id=36191
		// Can't hide the home button, so just disappear it
		final View homeButton = findViewById(android.R.id.home);
		((View) homeButton.getParent()).setVisibility(View.GONE);
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
			case R.id.discard:
				// We are always launched from the appropriate parent, so we can just finish() to return to it without
				// saving
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
