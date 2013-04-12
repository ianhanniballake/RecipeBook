package com.ianhanniballake.recipebook.auth;

import java.io.IOException;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.plus.PlusClient;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.ianhanniballake.recipebook.BuildConfig;

class InitializeDriveAsyncTask extends AsyncTask<PlusClient, Void, String>
{
	private final WeakReference<AuthorizedActivity> contextWeakRef;

	public InitializeDriveAsyncTask(final AuthorizedActivity authorizedActivity)
	{
		contextWeakRef = new WeakReference<AuthorizedActivity>(authorizedActivity);
	}

	@Override
	protected String doInBackground(final PlusClient... params)
	{
		final Context context = contextWeakRef.get();
		if (context == null)
			return null;
		final Drive driveService = Auth.getDriveFromPlusClient(context, params[0]);
		try
		{
			final File file = driveService.files().get(Auth.APPDATA_DEFAULT_ID).execute();
			return file.getId();
		} catch (final IOException e)
		{
			Log.e(InitializeDriveAsyncTask.class.getSimpleName(), "Error getting appdata folder", e);
		}
		return null;
	}

	@Override
	protected void onPostExecute(final String appDataId)
	{
		final AuthorizedActivity authorizedActivity = contextWeakRef.get();
		if (appDataId == null || authorizedActivity == null)
			return;
		if (BuildConfig.DEBUG)
			Log.d(InitializeDriveAsyncTask.class.getSimpleName(), "Received appDataId: " + appDataId);
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(authorizedActivity);
		sharedPreferences.edit().putString(Auth.PREF_DRIVE_APPDATA_ID, appDataId).commit();
		authorizedActivity.onInitializeDriveComplete();
	}

	@Override
	protected void onPreExecute()
	{
		if (BuildConfig.DEBUG)
			Log.d(InitializeDriveAsyncTask.class.getSimpleName(), "Starting initialization");
	}
}