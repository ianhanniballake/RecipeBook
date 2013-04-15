package com.ianhanniballake.recipebook.auth;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;

import com.google.android.gms.plus.PlusClient;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Changes;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.ChangeList;
import com.google.api.services.drive.model.File;
import com.google.gson.Gson;
import com.ianhanniballake.recipebook.BuildConfig;
import com.ianhanniballake.recipebook.model.Ingredient;
import com.ianhanniballake.recipebook.model.Instruction;
import com.ianhanniballake.recipebook.model.Recipe;
import com.ianhanniballake.recipebook.provider.RecipeContract;

class SyncDriveAsyncTask extends AsyncTask<PlusClient, Void, Long>
{
	private final WeakReference<Context> contextWeakRef;

	public SyncDriveAsyncTask(final Context context)
	{
		contextWeakRef = new WeakReference<Context>(context);
	}

	@Override
	protected Long doInBackground(final PlusClient... params)
	{
		final Context context = contextWeakRef.get();
		if (context == null)
			return null;
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		final String appDataId = sharedPreferences.getString(Auth.PREF_DRIVE_APPDATA_ID, Auth.APPDATA_DEFAULT_ID);
		final Long startChangeId = sharedPreferences.getLong(Auth.PREF_DRIVE_START_CHANGE_ID, (Long) null);
		final Drive driveService = Auth.getDriveFromPlusClient(context, params[0]);
		final List<Change> changeList = null;
		final Long newChangeId = -1L;
		try
		{
			final Changes.List request = driveService.changes().list();
			if (startChangeId != null)
				request.setStartChangeId(startChangeId);
			do
			{
				final ChangeList changes = request.execute();
				newChangeId = Math.max(newChangeId, changes.getLargestChangeId());
				changeList.addAll(changes.getItems());
				request.setPageToken(changes.getNextPageToken());
			} while (request.getPageToken() != null && request.getPageToken().length() > 0);
			if (BuildConfig.DEBUG)
			{
				Log.d(SyncDriveAsyncTask.class.getSimpleName(), "Found " + changeList.size() + " changes on Drive");
				for (final Change change : changeList)
					Log.d(SyncDriveAsyncTask.class.getSimpleName(), change.getFile().getTitle());
			}
		} catch (final IOException e)
		{
			Log.e(SyncDriveAsyncTask.class.getSimpleName(), "Error getting changes", e);
			return newChangeId;
		}
		final HashMap<String, File> fileMap = new HashMap<String, File>();
		for (final File file : driveFiles)
			fileMap.put(file.getTitle(), file);
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
			if (fileMap.containsKey(recipe.getTitle()))
			{
				if (BuildConfig.DEBUG)
					Log.d(SyncDriveAsyncTask.class.getSimpleName(), "Found " + recipe.getTitle() + " on drive");
			}
			else
			{
				if (BuildConfig.DEBUG)
					Log.d(SyncDriveAsyncTask.class.getSimpleName(), "Did not find " + recipe.getTitle() + " on drive");
				final String recipeJson = gson.toJson(recipe);
				Log.d(SyncDriveAsyncTask.class.getSimpleName(), "To JSON: " + recipeJson);
				Log.d(SyncDriveAsyncTask.class.getSimpleName(), "From JSON: " + gson.fromJson(recipeJson, Recipe.class));
			}
		}
		cursor.close();
		return newChangeId;
	}

	@Override
	protected void onPostExecute(final Long newChangeId)
	{
		final Context context = contextWeakRef.get();
		if (context == null)
		{
			if (BuildConfig.DEBUG)
				Log.d(SyncDriveAsyncTask.class.getSimpleName(), "Sync completed, but context was null");
			return;
		}
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		final Long startChangeId = sharedPreferences.getLong(Auth.PREF_DRIVE_START_CHANGE_ID, (Long) null);
		final boolean changeSuccessful = newChangeId != null && (startChangeId == null || newChangeId > startChangeId);
		if (BuildConfig.DEBUG)
			Log.d(SyncDriveAsyncTask.class.getSimpleName(), "Sync completed "
					+ (changeSuccessful ? "successfully" : "unsuccessfully"));
		if (newChangeId != null && (startChangeId == null || newChangeId > startChangeId))
			sharedPreferences.edit().putLong(Auth.PREF_DRIVE_START_CHANGE_ID, newChangeId).apply();
	}

	@Override
	protected void onPreExecute()
	{
		if (BuildConfig.DEBUG)
			Log.d(SyncDriveAsyncTask.class.getSimpleName(), "Starting Sync");
	}
}