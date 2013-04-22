package com.ianhanniballake.recipebook.ui;

import java.util.Locale;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.auth.AuthorizedActivity;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * An activity representing a single Recipe detail screen. This activity is only used on handset devices. On tablet-size
 * devices, item details are presented side-by-side with a list of items in a {@link RecipeListActivity}.
 */
public class RecipeDetailActivity extends AuthorizedActivity
{
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will keep every loaded fragment in memory.
	 * If this becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	public static class RecipeDetailTabsAdapter extends FragmentPagerAdapter implements ActionBar.TabListener,
			ViewPager.OnPageChangeListener
	{
		private final ActionBar actionBar;
		private final Context context;
		private final ViewPager pager;

		/**
		 * Manages the set of static tabs
		 * 
		 * @param activity
		 *            Activity showing these swipeable tabs
		 * @param pager
		 *            Pager displaying the tabs
		 */
		public RecipeDetailTabsAdapter(final FragmentActivity activity, final ViewPager pager)
		{
			super(activity.getSupportFragmentManager());
			context = activity;
			actionBar = activity.getActionBar();
			this.pager = pager;
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
					return context.getString(R.string.title_summary).toUpperCase(l);
				case 1:
					return context.getString(R.string.title_ingredient_list).toUpperCase(l);
				case 2:
					return context.getString(R.string.title_instruction_list).toUpperCase(l);
				default:
					return null;
			}
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
			pager.setAdapter(this);
			pager.setOnPageChangeListener(this);
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			// For each of the sections in the app, add a tab to the action bar.
			for (int i = 0; i < getCount(); i++)
				actionBar.addTab(actionBar.newTab().setText(getPageTitle(i)).setTabListener(this));
		}
	}

	private AsyncQueryHandler recipeDeleteHandler;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe_detail);
		// Query for the recipe's title
		new AsyncQueryHandler(getContentResolver())
		{
			@Override
			protected void onQueryComplete(final int token, final Object cookie, final Cursor cursor)
			{
				final int titleColumnIndex = cursor.getColumnIndex(RecipeContract.Recipes.COLUMN_NAME_TITLE);
				setTitle(cursor.getString(titleColumnIndex));
			}
		}.startQuery(0, 0, getIntent().getData(), new String[] { RecipeContract.Recipes.COLUMN_NAME_TITLE }, null,
				null, null);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		final ViewPager pager = (ViewPager) findViewById(R.id.pager);
		// Create the adapter that will return a fragment for each of the three tabs
		final RecipeDetailTabsAdapter tabsAdapter = new RecipeDetailTabsAdapter(this, pager);
		tabsAdapter.setup();
		recipeDeleteHandler = new AsyncQueryHandler(getContentResolver())
		{
			@Override
			protected void onDeleteComplete(final int token, final Object cookie, final int result)
			{
				Toast.makeText(RecipeDetailActivity.this, R.string.deleted, Toast.LENGTH_SHORT).show();
				finish();
			}
		};
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.recipe_detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				// This ID represents the Home or Up button. In the case of this activity, the Up button is shown. Use
				// NavUtils to allow users to navigate up one level in the application structure.
				NavUtils.navigateUpTo(this, new Intent(this, RecipeListActivity.class));
				return true;
			case R.id.edit:
				final Intent editIntent = new Intent(Intent.ACTION_EDIT, getIntent().getData());
				startActivity(editIntent);
				return true;
			case R.id.delete:
				recipeDeleteHandler.startDelete(0, null, getIntent().getData(), null, null);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
