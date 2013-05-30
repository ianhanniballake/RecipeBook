package com.ianhanniballake.recipebook.sync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Changes;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.ChangeList;
import com.google.api.services.drive.model.File;
import com.ianhanniballake.recipebook.BuildConfig;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Google Drive Sync Adapter
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter
{
	private final static String APP_NAME = "RecipeBook";
	private final static String APPDATA_DEFAULT_ID = "appdata";
	/**
	 * Scope for Drive AppData access
	 */
	public final static String DRIVE_APPDATA = "https://www.googleapis.com/auth/drive.appdata";
	private final static String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
	private final static String PREF_DRIVE_APPDATA_ID = "com.ianhanniballake.recipebook.DRIVE_APPDATA_ID";
	private final static String PREF_DRIVE_START_CHANGE_ID = "com.ianhanniballake.recipebook.DRIVE_START_CHANGE_ID";

	private static Drive getDriveFromAccount(final Context context, final Account account)
	{
		String accessToken = "";
		try
		{
			accessToken = GoogleAuthUtil.getToken(context, account.name, "oauth2:" + DRIVE_APPDATA);
		} catch (final IOException e)
		{
			Log.e(SyncAdapter.class.getSimpleName(), "Error getting token", e);
		} catch (final GoogleAuthException e)
		{
			Log.e(SyncAdapter.class.getSimpleName(), "Error getting token", e);
			// Disable syncing until the user relogs in
			ContentResolver.setIsSyncable(account, RecipeContract.AUTHORITY, 0);
		}
		if (BuildConfig.DEBUG)
			Log.d(SyncAdapter.class.getSimpleName(), "Token: " + accessToken);
		final GoogleCredential credential = new GoogleCredential();
		credential.setAccessToken(accessToken);
		return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
				.setApplicationName(APP_NAME).build();
	}

	/**
	 * Create a new SyncAdapter
	 * 
	 * @param context
	 *            Context to use
	 * @param autoInitialize
	 *            Whether this service should be auto initialized
	 */
	public SyncAdapter(final Context context, final boolean autoInitialize)
	{
		super(context, autoInitialize);
	}

	@Override
	public void onPerformSync(final Account account, final Bundle extras, final String authority,
			final ContentProviderClient provider, final SyncResult syncResult)
	{
		if (BuildConfig.DEBUG)
			Log.d(SyncAdapter.class.getSimpleName(), "onPerformSync");
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		final Drive driveService = getDriveFromAccount(getContext(), account);
		String appDataFolderId = sharedPreferences.getString(PREF_DRIVE_APPDATA_ID + "_" + account.name,
				APPDATA_DEFAULT_ID);
		if (APPDATA_DEFAULT_ID.equals(appDataFolderId))
			try
			{
				final File file = driveService.files().get(APPDATA_DEFAULT_ID).execute();
				appDataFolderId = file.getId();
				sharedPreferences.edit().putString(PREF_DRIVE_APPDATA_ID + "_" + account.name, appDataFolderId)
						.commit();
			} catch (final IOException e)
			{
				Log.e(SyncAdapter.class.getSimpleName(), "Error getting appdata folder", e);
				syncResult.stats.numIoExceptions++;
			}
		final Long startChangeId = sharedPreferences.getLong(PREF_DRIVE_START_CHANGE_ID + "_" + account.name, 0L);
		final List<Change> changeList = new ArrayList<Change>();
		Long newChangeId = -1L;
		try
		{
			final Changes.List request = driveService.changes().list();
			if (startChangeId != null && startChangeId > 0L)
				request.setStartChangeId(startChangeId);
			do
			{
				final ChangeList changes = request.execute();
				newChangeId = Math.max(newChangeId, changes.getLargestChangeId());
				changeList.addAll(changes.getItems());
				request.setPageToken(changes.getNextPageToken());
			} while (request.getPageToken() != null && request.getPageToken().length() > 0);
			final TreeSet<String> deletedFileIds = new TreeSet<String>();
			final List<File> remoteChanges = new ArrayList<File>();
			if (BuildConfig.DEBUG)
				Log.d(SyncAdapter.class.getSimpleName(), "Found " + changeList.size() + " change"
						+ (changeList.size() != 1 ? "s" : "") + " on Drive");
			for (final Change change : changeList)
				if (change.getDeleted())
					deletedFileIds.add(change.getFileId());
				else
				{
					final File changedFile = change.getFile();
					if (BuildConfig.DEBUG)
						Log.d(SyncAdapter.class.getSimpleName(), "Change of file named " + changedFile.getTitle());
					final boolean isFolder = FOLDER_MIME_TYPE.equals(changedFile.getMimeType());
					if (changedFile.getAppDataContents() && !isFolder)
					{
						remoteChanges.add(changedFile);
						if (BuildConfig.DEBUG)
							Log.d(SyncAdapter.class.getSimpleName(), "File " + changedFile.getTitle()
									+ " changed in AppData folder");
					}
				}
			Log.d(SyncAdapter.class.getSimpleName(), "Found " + deletedFileIds.size() + " deleted change"
					+ (deletedFileIds.size() != 1 ? "s" : ""));
		} catch (final IOException e)
		{
			Log.e(SyncAdapter.class.getSimpleName(), "Error getting changes", e);
			syncResult.stats.numIoExceptions++;
		}
	}
}
