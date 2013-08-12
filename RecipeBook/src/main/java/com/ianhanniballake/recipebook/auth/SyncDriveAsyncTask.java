package com.ianhanniballake.recipebook.auth;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.util.Log;

import com.google.android.gms.plus.PlusClient;
import com.google.gson.Gson;
import com.ianhanniballake.recipebook.BuildConfig;
import com.ianhanniballake.recipebook.model.Ingredient;
import com.ianhanniballake.recipebook.model.Instruction;
import com.ianhanniballake.recipebook.model.Recipe;
import com.ianhanniballake.recipebook.provider.RecipeContract;

class SyncDriveAsyncTask extends AsyncTask<PlusClient, Void, Void>
{
	private final WeakReference<Context> contextWeakRef;

	public SyncDriveAsyncTask(final Context context)
	{
		contextWeakRef = new WeakReference<Context>(context);
	}

	@Override
	protected Void doInBackground(final PlusClient... params)
	{
		final Context context = contextWeakRef.get();
		if (context == null)
			return null;
		final Gson gson = new Gson();
		final Cursor cursor = context.getContentResolver().query(RecipeContract.Recipes.CONTENT_URI, null, null, null,
				null);
		while (cursor.moveToNext())
		{
			final Long recipeId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
			final String[] recipeIdArgs = { Long.toString(recipeId) };
			final Cursor ingredientCursor = context.getContentResolver().query(RecipeContract.Ingredients.CONTENT_URI,
					null, RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID + "=?", recipeIdArgs, null);
			final List<Ingredient> ingredients = new ArrayList<Ingredient>();
			while (ingredientCursor.moveToNext())
				ingredients.add(new Ingredient(ingredientCursor));
			ingredientCursor.close();
			final Cursor instructionCursor = context.getContentResolver().query(
					RecipeContract.Instructions.CONTENT_URI, null,
					RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID + "=?", recipeIdArgs, null);
			final List<Instruction> instructions = new ArrayList<Instruction>();
			while (instructionCursor.moveToNext())
			{
				final String instruction = instructionCursor.getString(instructionCursor
						.getColumnIndex(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION));
				instructions.add(new Instruction(instruction));
			}
			instructionCursor.close();
			final Recipe recipe = new Recipe(cursor, ingredients, instructions);
			if (BuildConfig.DEBUG)
				Log.d(SyncDriveAsyncTask.class.getSimpleName(), "Recipe " + recipe.getTitle());
			final String recipeJson = gson.toJson(recipe);
			Log.d(SyncDriveAsyncTask.class.getSimpleName(), "To JSON: " + recipeJson);
			Log.d(SyncDriveAsyncTask.class.getSimpleName(), "From JSON: " + gson.fromJson(recipeJson, Recipe.class));
		}
		cursor.close();
		return null;
	}

	@Override
	protected void onPostExecute(final Void params)
	{
		final Context context = contextWeakRef.get();
		if (context == null)
		{
			if (BuildConfig.DEBUG)
				Log.d(SyncDriveAsyncTask.class.getSimpleName(), "Sync completed, but context was null");
			return;
		}
		if (BuildConfig.DEBUG)
			Log.d(SyncDriveAsyncTask.class.getSimpleName(), "Sync completed successfully");
	}

	@Override
	protected void onPreExecute()
	{
		if (BuildConfig.DEBUG)
			Log.d(SyncDriveAsyncTask.class.getSimpleName(), "Starting Sync");
	}
}