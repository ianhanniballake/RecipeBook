package com.ianhanniballake.recipebook;

import android.content.Intent;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.AdapterView;

/**
 * An activity representing a single Recipe detail screen. This activity is only used on handset devices. On tablet-size
 * devices, item details are presented side-by-side with a list of items in a {@link RecipeListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than a {@link RecipeDetailFragment}.
 */
public class RecipeDetailActivity extends FragmentActivity
{
	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe_detail);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		// savedInstanceState is non-null when there is fragment state saved from previous configurations of this
		// activity (e.g. when rotating the screen from portrait to landscape). In this case, the fragment will
		// automatically be re-added to its container so we don't need to manually add it.
		if (savedInstanceState == null)
		{
			// Create the detail fragment and add it to the activity using a fragment transaction.
			final Bundle arguments = new Bundle();
			arguments.putLong(BaseColumns._ID, getIntent().getLongExtra(BaseColumns._ID, AdapterView.INVALID_ROW_ID));
			final RecipeDetailFragment fragment = new RecipeDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction().add(R.id.recipe_detail_container, fragment).commit();
		}
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
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
