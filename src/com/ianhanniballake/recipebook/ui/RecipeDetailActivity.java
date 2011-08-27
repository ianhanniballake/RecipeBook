package com.ianhanniballake.recipebook.ui;

import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Activity which displays only the Recipe details
 */
public class RecipeDetailActivity extends FragmentActivity implements
		OnRecipeEditListener
{
	/**
	 * Result indicating a recipe deletion
	 */
	public static final int RESULT_DELETED = Integer.MIN_VALUE;
	/**
	 * Handler for asynchronous updates of recipes
	 */
	private AsyncQueryHandler queryHandler;

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
		if (savedInstanceState == null)
		{
			// During initial setup, plug in the details fragment.
			final RecipeDetailFragment details = new RecipeDetailFragment();
			details.setArguments(getIntent().getExtras());
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.details, details).commit();
		}
		queryHandler = new AsyncQueryHandler(getContentResolver())
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
	}

	@Override
	public void onRecipeDeleted(final long recipeId)
	{
		getSupportFragmentManager().popBackStack();
		final Uri deleteUri = ContentUris.withAppendedId(
				RecipeContract.Recipes.CONTENT_ID_URI_PATTERN, recipeId);
		queryHandler.startDelete(0, null, deleteUri, null, null);
	}

	@Override
	public void onRecipeEditCancelled()
	{
		getSupportFragmentManager().popBackStack();
	}

	@Override
	public void onRecipeEditSave(final long recipeId, final ContentValues values)
	{
		getSupportFragmentManager().popBackStack();
		final Uri updateUri = ContentUris.withAppendedId(
				RecipeContract.Recipes.CONTENT_ID_URI_PATTERN, recipeId);
		queryHandler.startUpdate(0, null, updateUri, values, null, null);
	}

	@Override
	public void onRecipeEditStarted(final long recipeId)
	{
		final Fragment editFragment = new RecipeEditFragment();
		final Bundle args = new Bundle();
		args.putLong(BaseColumns._ID, recipeId);
		editFragment.setArguments(args);
		// Execute a transaction, replacing any existing fragment
		// with this one inside the frame.
		final FragmentTransaction ft = getSupportFragmentManager()
				.beginTransaction();
		ft.replace(R.id.details, editFragment);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.addToBackStack(null);
		ft.commit();
	}
}
