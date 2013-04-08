package com.ianhanniballake.recipebook.auth;

import android.content.Context;

import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.PlusClient;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

public class Auth
{
	/**
	 * Intent action to start a sync request.
	 */
	public final static String ACTION_SYNC = "com.ianhanniballake.recipebook.SYNC";
	static final String APPDATA_DEFAULT_ID = "appdata";
	private final static String DRIVE_APPDATA = "https://www.googleapis.com/auth/drive.appdata";
	static final String PREF_DRIVE_APPDATA_ID = "DRIVE_APPDATA_ID";

	public static GoogleAccountCredential getCredentialFromPlusClient(final Context context, final PlusClient plusClient)
	{
		final GoogleAccountCredential credential = new GoogleAccountCredential(context, DRIVE_APPDATA);
		credential.setSelectedAccountName(plusClient.getAccountName());
		return credential;
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
