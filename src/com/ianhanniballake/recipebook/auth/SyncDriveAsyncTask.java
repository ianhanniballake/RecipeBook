package com.ianhanniballake.recipebook.auth;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.plus.PlusClient;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.ianhanniballake.recipebook.BuildConfig;

class SyncDriveAsyncTask extends AsyncTask<PlusClient, Void, List<File>>
{
	private final WeakReference<Context> contextWeakRef;

	public SyncDriveAsyncTask(final Context context)
	{
		contextWeakRef = new WeakReference<Context>(context);
	}

	@Override
	protected List<File> doInBackground(final PlusClient... params)
	{
		final Context context = contextWeakRef.get();
		if (context == null)
			return null;
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		final String appDataId = sharedPreferences.getString(Auth.PREF_DRIVE_APPDATA_ID, Auth.APPDATA_DEFAULT_ID);
		final Drive driveService = Auth.getDriveFromPlusClient(context, params[0]);
		return listFilesInApplicationDataFolder(driveService, appDataId);
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
		return result;
	}

	@Override
	protected void onPostExecute(final List<File> fileList)
	{
		if (BuildConfig.DEBUG)
		{
			Log.d(SyncDriveAsyncTask.class.getSimpleName(), "Found " + fileList.size() + " files on Drive");
			for (final File file : fileList)
				Log.d(SyncDriveAsyncTask.class.getSimpleName(), file.getTitle());
		}
	}

	@Override
	protected void onPreExecute()
	{
		if (BuildConfig.DEBUG)
			Log.d(SyncDriveAsyncTask.class.getSimpleName(), "Starting Sync");
	}
}