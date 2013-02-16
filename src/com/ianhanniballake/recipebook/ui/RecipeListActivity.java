package com.ianhanniballake.recipebook.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * An activity representing a list of Recipes. This activity has different presentations for handset and tablet-size
 * devices. On handsets, the activity presents a list of items, which when touched, lead to a
 * {@link RecipeDetailActivity} representing item details. On tablets, the activity presents the list of items and item
 * details side-by-side using two vertical panes.
 */
public class RecipeListActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
	/**
	 * The serialization (saved instance state) Bundle key representing the activated item position. Only used on
	 * tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";
	/**
	 * Adapter to display the list's data
	 */
	private SimpleCursorAdapter adapter;
	/**
	 * The current activated item position. Only used on tablets.
	 */
	int mActivatedPosition = AdapterView.INVALID_POSITION;
	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
	 */
	boolean mTwoPane;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe_list);
		final AbsListView listView = (AbsListView) findViewById(android.R.id.list);
		if (findViewById(R.id.recipe_detail_summary) != null)
		{
			// The detail container view will be present only in the large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the activity should be in two-pane mode.
			mTwoPane = true;
			// In two-pane mode, list items should be given the 'activated' state when touched.
			listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		}
		adapter = new SimpleCursorAdapter(this, R.layout.list_item_recipe, null, new String[] {
				RecipeContract.Recipes.COLUMN_NAME_TITLE, RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION }, new int[] {
				R.id.title, R.id.description }, 0);
		listView.setAdapter(adapter);
		listView.setEmptyView(findViewById(android.R.id.empty));
		listView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
			{
				if (mTwoPane)
				{
					mActivatedPosition = position;
					// In two-pane mode, show the detail view in this activity by adding or replacing the detail
					// fragment using a fragment transaction.
					final RecipeDetailSummaryFragment summaryFragment = RecipeDetailSummaryFragment.newInstance(id);
					final RecipeDetailIngredientFragment ingredientFragment = RecipeDetailIngredientFragment
							.newInstance(id);
					final RecipeDetailInstructionFragment instructionFragment = RecipeDetailInstructionFragment
							.newInstance(id);
					final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					ft.replace(R.id.recipe_detail_summary, summaryFragment);
					ft.replace(R.id.recipe_detail_ingredient, ingredientFragment);
					ft.replace(R.id.recipe_detail_instruction, instructionFragment);
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					ft.commit();
					invalidateOptionsMenu();
				}
				else
				{
					final TextView titleView = (TextView) view.findViewById(R.id.title);
					// In single-pane mode, simply start the detail activity for the selected item ID.
					final Intent detailIntent = new Intent(RecipeListActivity.this, RecipeDetailActivity.class);
					detailIntent.putExtra(BaseColumns._ID, id);
					detailIntent.putExtra(RecipeContract.Recipes.COLUMN_NAME_TITLE, titleView.getText());
					startActivity(detailIntent);
				}
			}
		});
		getSupportLoaderManager().initLoader(0, null, this);
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION))
		{
			final int position = savedInstanceState.getInt(STATE_ACTIVATED_POSITION);
			if (position == AdapterView.INVALID_POSITION)
				listView.setItemChecked(mActivatedPosition, false);
			else
				listView.setItemChecked(position, true);
			mActivatedPosition = position;
		}
		// TODO: If exposing deep links into your app, handle intents here.
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args)
	{
		return new CursorLoader(this, RecipeContract.Recipes.CONTENT_ID_URI_BASE, null, null, null, null);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.recipe_list, menu);
		getMenuInflater().inflate(R.menu.recipe_detail, menu);
		return true;
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader)
	{
		adapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data)
	{
		adapter.swapCursor(data);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.add:
				// TODO: Launch Add Activity
				Toast.makeText(this, R.string.add, Toast.LENGTH_SHORT).show();
				return true;
			case R.id.edit:
				final Intent editIntent = new Intent(this, RecipeEditActivity.class);
				final AbsListView listView = (AbsListView) findViewById(android.R.id.list);
				final long id = listView.getItemIdAtPosition(mActivatedPosition);
				editIntent.putExtra(BaseColumns._ID, id);
				startActivity(editIntent);
				return true;
			case R.id.delete:
				Toast.makeText(this, R.string.delete, Toast.LENGTH_SHORT).show();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		final AbsListView listView = (AbsListView) findViewById(android.R.id.list);
		final boolean isItemSelected = listView.getCheckedItemCount() > 0;
		final MenuItem editMenuItem = menu.findItem(R.id.edit);
		editMenuItem.setVisible(isItemSelected);
		final MenuItem deleteMenuItem = menu.findItem(R.id.delete);
		deleteMenuItem.setVisible(isItemSelected);
		return true;
	}

	@Override
	public void onSaveInstanceState(final Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != AdapterView.INVALID_POSITION)
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
	}
}
