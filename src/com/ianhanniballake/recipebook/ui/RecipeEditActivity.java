package com.ianhanniballake.recipebook.ui;

import java.util.Locale;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
				final android.support.v4.app.FragmentTransaction ft = activity.getSupportFragmentManager()
						.beginTransaction();
				ft.replace(R.id.recipe_detail_summary, getItem(0));
				ft.replace(R.id.recipe_detail_ingredient, getItem(1));
				ft.replace(R.id.recipe_detail_instruction, getItem(2));
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				ft.commit();
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

	private RecipeEditTabsAdapter fragmentAdapter;
	private AsyncQueryHandler recipeQueryHandler;

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
		recipeQueryHandler = new AsyncQueryHandler(getContentResolver())
		{
			@Override
			protected void onInsertComplete(final int token, final Object cookie, final Uri uri)
			{
				Toast.makeText(RecipeEditActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
				finish();
			}

			@Override
			protected void onUpdateComplete(final int token, final Object cookie, final int result)
			{
				Toast.makeText(RecipeEditActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
				finish();
			}
		};
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
				final ContentValues recipeValues = fragmentAdapter.getSummaryFragment().getContentValues();
				if (Intent.ACTION_INSERT.equals(getIntent().getAction()))
					recipeQueryHandler.startInsert(0, null, RecipeContract.Recipes.CONTENT_ID_URI_BASE, recipeValues);
				else
					recipeQueryHandler.startUpdate(0, null, getIntent().getData(), recipeValues, null, null);
				return true;
			case R.id.cancel:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
