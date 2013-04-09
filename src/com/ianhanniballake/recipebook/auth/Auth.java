package com.ianhanniballake.recipebook.auth;

import java.io.IOException;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.PlusClient;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.ianhanniballake.recipebook.BuildConfig;

public class Auth
{
	/**
	 * Intent action to start a sync request.
	 */
	public final static String ACTION_SYNC = "com.ianhanniballake.recipebook.SYNC";
	private final static String APP_NAME = "RecipeBook";
	static final String APPDATA_DEFAULT_ID = "appdata";
	private final static String DRIVE_APPDATA = "https://www.googleapis.com/auth/drive.appdata";
	static final String PREF_DRIVE_APPDATA_ID = "DRIVE_APPDATA_ID";

	public static Drive getDriveFromPlusClient(final Context context, final PlusClient plusClient)
	{
		String accessToken = "";
		try
		{
			accessToken = GoogleAuthUtil.getToken(context, plusClient.getAccountName(), "oauth2:" + DRIVE_APPDATA);
		} catch (final UserRecoverableAuthException e)
		{
			Log.e(Auth.class.getSimpleName(), "Error getting token", e);
		} catch (final IOException e)
		{
			Log.e(Auth.class.getSimpleName(), "Error getting token", e);
		} catch (final GoogleAuthException e)
		{
			Log.e(Auth.class.getSimpleName(), "Error getting token", e);
		}
		if (BuildConfig.DEBUG)
			Log.d(Auth.class.getSimpleName(), "Token: " + accessToken);
		final GoogleCredential credential = new GoogleCredential();
		credential.setAccessToken(accessToken);
		return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
				.setApplicationName(APP_NAME).build();
	}

	public static PlusClient getPlusClient(final Context context,
			final GooglePlayServicesClient.ConnectionCallbacks connectionCallbacks,
			final GooglePlayServicesClient.OnConnectionFailedListener connectionFailedListener)
	{
		return new PlusClient.Builder(context, connectionCallbacks, connectionFailedListener)
				.setVisibleActivities("http://schemas.google.com/CreateActivity")
				.setScopes(Scopes.PLUS_LOGIN, DRIVE_APPDATA).build();
	}

	private Auth()
	{
	}
}
