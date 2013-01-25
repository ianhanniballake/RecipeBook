package com.ianhanniballake.recipebook.ui;

import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.ianhanniballake.recipebook.R;

/**
 * Activity which displays only the Recipe details
 */
public class RecipeDetailActivity extends FragmentActivity implements OnRecipeDeleteListener
{
	/**
	 * Result indicating a recipe deletion
	 */
	public static final int RESULT_DELETED = Integer.MIN_VALUE;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe_detail);
		if (findViewById(R.id.recipe_detail_summary) == null)
		{
			// A null details view means we no longer need this activity
			finish();
			return;
		}
		long recipeId = 0;
		if (getIntent() != null && getIntent().getExtras() != null)
			recipeId = getIntent().getExtras().getLong(BaseColumns._ID, 0);
		final RecipeDetailViewFragment details = new RecipeDetailViewFragment();
		final Bundle args = new Bundle();
		args.putLong(BaseColumns._ID, recipeId);
		details.setArguments(args);
		// Execute a transaction, replacing any existing fragment
		// with this one inside the frame.
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.recipe_detail_summary, details);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
	}

	@Override
	public void onRecipeDeleted()
	{
		setResult(RESULT_DELETED);
		finish();
	}
}
