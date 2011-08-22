package com.ianhanniballake.recipebook.ui;

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Activity responsible for creating new Recipes
 */
public class RecipeAddActivity extends FragmentActivity implements
		OnRecipeEditListener
{
	/**
	 * Handler for asynchronous inserts of new recipes
	 */
	private AsyncQueryHandler insertHandler;

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe_edit);
		insertHandler = new AsyncQueryHandler(getContentResolver())
		{
			@Override
			protected void onInsertComplete(final int token,
					final Object cookie, final Uri uri)
			{
				Toast.makeText(RecipeAddActivity.this, getText(R.string.saved),
						Toast.LENGTH_SHORT);
				setResult(RESULT_OK, new Intent(Intent.ACTION_PICK, uri));
				finish();
			}
		};
	}

	@Override
	public void onRecipeDeleted(final long recipeId)
	{
		// Not used
	}

	@Override
	public void onRecipeEditCancelled()
	{
		setResult(RESULT_CANCELED);
		finish();
	}

	@Override
	public void onRecipeEditSave(final long recipeId, final ContentValues values)
	{
		Toast.makeText(this, getText(R.string.saving), Toast.LENGTH_LONG);
		insertHandler.startInsert(0, null,
				RecipeContract.Recipes.CONTENT_ID_URI_BASE, values);
	}

	@Override
	public void onRecipeEditStarted(final long recipeId)
	{
		// Not used
	}
}