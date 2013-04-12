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
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.ianhanniballake.recipebook.BuildConfig;
import com.ianhanniballake.recipebook.model.Ingredient;
import com.ianhanniballake.recipebook.model.Instruction;
import com.ianhanniballake.recipebook.model.Recipe;
import com.ianhanniballake.recipebook.provider.RecipeContract;

class SyncDriveAsyncTask extends AsyncTask<PlusClient, Void, Boolean>
{
	private final WeakReference<Context> contextWeakRef;

	public SyncDriveAsyncTask(final Context context)
	{
		contextWeakRef = new WeakReference<Context>(context);
	}

	@Override
	protected Boolean doInBackground(final PlusClient... params)
	{
		final Context context = contextWeakRef.get();
		if (context == null)
			return false;
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		final String appDataId = sharedPreferences.getString(Auth.PREF_DRIVE_APPDATA_ID, Auth.APPDATA_DEFAULT_ID);
		final Drive driveService = Auth.getDriveFromPlusClient(context, params[0]);
		final List<File> driveFiles = listFilesInApplicationDataFolder(driveService, appDataId);
		final HashMap<String, File> fileMap = new HashMap<String, File>();
		for (final File file : driveFiles)
			fileMap.put(file.getTitle(), file);
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
			else if (BuildConfig.DEBUG)
				Log.d(SyncDriveAsyncTask.class.getSimpleName(), "Did not find " + recipe.getTitle() + " on drive");
		}
		cursor.close();
		return true;
	}

	/**
	 * List all files contained in the Application Data folder.
	 * 
	 * @param service
	 *            Drive API service instance.
	 * @param appDataId
	 *            AppData folder id to use
	 * @return List of File resources.
	 */
	private List<File> listFilesInApplicationDataFolder(final Drive service, final String appDataId)
	{
		final List<File> result = new ArrayList<File>();
		Files.List request;
		try
		{
			request = service.files().list();
		} catch (final IOException e)
		{
			Log.e(getClass().getSimpleName(), "Error getting request list", e);
			return result;
		}
		request.setQ("'" + appDataId + "' in parents");
		do
			try
			{
				final FileList files = request.execute();
				result.addAll(files.getItems());
				request.setPageToken(files.getNextPageToken());
			} catch (final IOException e)
			{
				Log.e(getClass().getSimpleName(), "Error getting files", e);
				request.setPageToken(null);
			}
		while (request.getPageToken() != null && request.getPageToken().length() > 0);
		if (BuildConfig.DEBUG)
		{
			Log.d(SyncDriveAsyncTask.class.getSimpleName(), "Found " + result.size() + " files on Drive");
			for (final File file : result)
				Log.d(SyncDriveAsyncTask.class.getSimpleName(), file.getTitle());
		}
		return result;
	}

	@Override
	protected void onPostExecute(final Boolean result)
	{
		if (BuildConfig.DEBUG)
			Log.d(SyncDriveAsyncTask.class.getSimpleName(), "Sync completed "
					+ (result ? "successfully" : "unsuccessfully"));
	}

	@Override
	protected void onPreExecute()
	{
		if (BuildConfig.DEBUG)
			Log.d(SyncDriveAsyncTask.class.getSimpleName(), "Starting Sync");
	}
}