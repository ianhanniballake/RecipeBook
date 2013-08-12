package com.ianhanniballake.recipebook.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.ianhanniballake.recipebook.BuildConfig;
import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.auth.AuthorizedActivity;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * An activity representing a list of Recipes. This activity has different presentations for handset and tablet-size
 * devices. On handsets, the activity presents a list of items, which when touched, lead to a
 * {@link RecipeDetailActivity} representing item details. On tablets, the activity presents the list of items and item
 * details side-by-side using two vertical panes.
 */
public class RecipeListActivity extends AuthorizedActivity implements LoaderManager.LoaderCallbacks<Cursor>
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
	private AsyncQueryHandler recipeDeleteHandler;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// Ensure ACTION_SEARCH suggestions from Quick Search Box are interpreted as ACTION_VIEW
		if (Intent.ACTION_SEARCH.equals(getIntent().getAction())
				&& getIntent().getStringExtra(SearchManager.QUERY).startsWith(
						RecipeContract.Recipes.CONTENT_ID_URI_BASE.toString()))
			setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(getIntent().getStringExtra(SearchManager.QUERY))));
		if (BuildConfig.DEBUG)
		{
			Log.d(RecipeListActivity.class.getSimpleName(), "onCreate Intent: " + getIntent().getAction());
			if (getIntent().getExtras() != null)
				for (final String key : getIntent().getExtras().keySet())
					Log.d(RecipeListActivity.class.getSimpleName(), "onCreate Intent Extra " + key + ": "
							+ getIntent().getExtras().get(key));
		}
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
				mActivatedPosition = position;
				showDetails(ContentUris.withAppendedId(RecipeContract.Recipes.CONTENT_ID_URI_BASE, id), false);
			}
		});
		recipeDeleteHandler = new AsyncQueryHandler(getContentResolver())
		{
			@Override
			protected void onDeleteComplete(final int token, final Object cookie, final int result)
			{
				if (mTwoPane)
				{
					getIntent().setData(null);
					mActivatedPosition = AdapterView.INVALID_POSITION;
					final FragmentManager fragmentManager = getFragmentManager();
					final FragmentTransaction ft = getFragmentManager().beginTransaction();
					ft.remove(fragmentManager.findFragmentById(R.id.recipe_detail_summary));
					ft.remove(fragmentManager.findFragmentById(R.id.recipe_detail_ingredient));
					ft.remove(fragmentManager.findFragmentById(R.id.recipe_detail_instruction));
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					ft.commit();
					invalidateOptionsMenu();
				}
				Toast.makeText(RecipeListActivity.this, R.string.deleted, Toast.LENGTH_SHORT).show();
			}
		};
		getLoaderManager().initLoader(0, null, this);
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION))
		{
			final int position = savedInstanceState.getInt(STATE_ACTIVATED_POSITION);
			if (position == AdapterView.INVALID_POSITION)
				listView.setItemChecked(mActivatedPosition, false);
			else
				listView.setItemChecked(position, true);
			mActivatedPosition = position;
		}
		else if (Intent.ACTION_VIEW.equals(getIntent().getAction()))
			showDetails(getIntent().getData(), true);
		// TODO: If exposing deep links into your app, handle intents here.
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args)
	{
		if (BuildConfig.DEBUG)
		{
			Log.d(RecipeListActivity.class.getSimpleName(), "onCreateLoader Intent: " + getIntent().getAction());
			if (getIntent().getExtras() != null)
				for (final String key : getIntent().getExtras().keySet())
					Log.d(RecipeListActivity.class.getSimpleName(), "onCreateLoader Intent Extra " + key + ": "
							+ getIntent().getExtras().get(key));
		}
		if (Intent.ACTION_SEARCH.equals(getIntent().getAction()))
		{
			final CharSequence userQuery = getIntent().getCharSequenceExtra(SearchManager.USER_QUERY);
			if (!TextUtils.isEmpty(userQuery))
			{
				final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, RecipeContract.AUTHORITY,
						RecipeContract.Recipes.SEARCH_MODE);
				suggestions.saveRecentQuery(userQuery.toString(), null);
			}
			final String query = getIntent().getStringExtra(SearchManager.QUERY);
			final String selection = RecipeContract.Recipes.COLUMN_NAME_TITLE + " LIKE ? OR "
					+ RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION + " LIKE ?";
			final String[] selectionArgs = { "%" + query + "%", "%" + query + "%" };
			return new CursorLoader(this, RecipeContract.Recipes.CONTENT_URI, null, selection, selectionArgs, null);
		}
		return new CursorLoader(this, RecipeContract.Recipes.CONTENT_URI, null, null, null, null);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.recipe_list, menu);
		getMenuInflater().inflate(R.menu.recipe_detail, menu);
		// Set up search
		final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		final MenuItem searchItem = menu.findItem(R.id.search);
		searchItem.setOnActionExpandListener(new OnActionExpandListener()
		{
			@Override
			public boolean onMenuItemActionCollapse(final MenuItem item)
			{
				getIntent().setAction(null);
				getIntent().removeExtra(SearchManager.QUERY);
				getIntent().removeExtra(SearchManager.USER_QUERY);
				getLoaderManager().restartLoader(0, null, RecipeListActivity.this);
				return true;
			}

			@Override
			public boolean onMenuItemActionExpand(final MenuItem item)
			{
				return true;
			}
		});
		final SearchView searchView = (SearchView) searchItem.getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
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
		if (mTwoPane && getIntent().getData() != null)
			selectPositionFromUri(getIntent().getData());
	}

	@Override
	public void onNewIntent(final Intent intent)
	{
		if (BuildConfig.DEBUG)
		{
			Log.d(RecipeListActivity.class.getSimpleName(), "onNewIntent Intent: " + getIntent().getAction());
			if (getIntent().getExtras() != null)
				for (final String key : getIntent().getExtras().keySet())
					Log.d(RecipeListActivity.class.getSimpleName(), "onNewIntent Intent Extra " + key + ": "
							+ getIntent().getExtras().get(key));
		}
		setIntent(intent);
		getLoaderManager().restartLoader(0, null, this);
		if (Intent.ACTION_VIEW.equals(intent.getAction()))
			showDetails(intent.getData(), true);
		else
			invalidateOptionsMenu();
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.add:
				final Intent addIntent = new Intent(Intent.ACTION_INSERT, RecipeContract.Recipes.CONTENT_URI);
				startActivity(addIntent);
				return true;
			case R.id.clear_search_history:
				final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, RecipeContract.AUTHORITY,
						RecipeContract.Recipes.SEARCH_MODE);
				suggestions.clearHistory();
				return true;
			case R.id.edit:
				findViewById(android.R.id.list);
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
		final MenuItem searchItem = menu.findItem(R.id.search);
		final SearchView searchView = (SearchView) searchItem.getActionView();
		final CharSequence userQuery = getIntent().getCharSequenceExtra(SearchManager.USER_QUERY);
		final String query = TextUtils.isEmpty(userQuery) ? getIntent().getStringExtra(SearchManager.QUERY) : userQuery
				.toString();
		if (!TextUtils.isEmpty(query))
		{
			searchItem.expandActionView();
			searchView.setQuery(query, false);
		}
		searchView.clearFocus();
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

	private void selectPositionFromUri(final Uri recipeUri)
	{
		final AbsListView listView = (AbsListView) findViewById(android.R.id.list);
		final long recipeId = ContentUris.parseId(recipeUri);
		final int count = adapter.getCount();
		for (int position = 0; position < count; position++)
		{
			final boolean foundMatch = adapter.getItemId(position) == recipeId;
			listView.setItemChecked(position, foundMatch);
			if (foundMatch)
				mActivatedPosition = position;
		}
	}

	/**
	 * Show the details of the given Recipe. If in two pane mode, show it inline. Otherwise, launch a detail activity
	 * 
	 * @param recipeUri
	 *            Recipe to show
	 * @param findAndSelectPosition
	 *            Whether we should search the adapter and select the position associated with this recipe
	 */
	void showDetails(final Uri recipeUri, final boolean findAndSelectPosition)
	{
		if (mTwoPane)
		{
			// In two-pane mode, show the detail view in this activity by adding or replacing the detail
			// fragment using a fragment transaction.
			getIntent().setData(recipeUri);
			if (findAndSelectPosition)
				selectPositionFromUri(recipeUri);
			final RecipeDetailSummaryFragment summaryFragment = new RecipeDetailSummaryFragment();
			final RecipeDetailIngredientFragment ingredientFragment = new RecipeDetailIngredientFragment();
			final RecipeDetailInstructionFragment instructionFragment = new RecipeDetailInstructionFragment();
			final FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.recipe_detail_summary, summaryFragment);
			ft.replace(R.id.recipe_detail_ingredient, ingredientFragment);
			ft.replace(R.id.recipe_detail_instruction, instructionFragment);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
			invalidateOptionsMenu();
		}
		else
			// In single-pane mode, simply start the detail activity for the selected item ID.
			startActivity(new Intent(Intent.ACTION_VIEW, recipeUri));
	}
}
