package com.ianhanniballake.recipebook.auth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;
import com.ianhanniballake.recipebook.BuildConfig;
import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Activity class that manages user authorization
 */
public class AuthorizedActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener
{
	private static final int REQUEST_ACCOUNT_RESOLUTION = 401;
	private ConnectionResult latestResult = null;
	private PlusClient plusClient = null;
	private boolean resolveOnFail = false;
	/**
	 * BroadcastReceiver listening for SYNC actions
	 */
	private final BroadcastReceiver syncBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(final Context context, final Intent intent)
		{
			final String action = intent.getAction();
			if (BuildConfig.DEBUG)
				Log.d(AuthorizedActivity.this.getClass().getSimpleName(),
						"SyncBR Received " + action + ": " + intent.getData());
			if (TextUtils.equals(Auth.ACTION_SYNC, action))
				sync();
		}
	};

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
	{
		if (BuildConfig.DEBUG)
			Log.d(getClass().getSimpleName(), "onActivityResult: " + resultCode);
		switch (requestCode)
		{
			case REQUEST_ACCOUNT_RESOLUTION:
				if (resultCode == RESULT_OK)
				{
					resolveOnFail = true;
					plusClient.connect();
				}
				return;
			default:
				super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onConnected()
	{
		if (BuildConfig.DEBUG)
			Log.d(getClass().getSimpleName(), "onConnected");
		resolveOnFail = false;
		invalidateOptionsMenu();
		new InitializeDriveAsyncTask(this).execute(plusClient);
	}

	@Override
	public void onConnectionFailed(final ConnectionResult result)
	{
		if (BuildConfig.DEBUG)
			Log.d(getClass().getSimpleName(), "onConnectionFailed: " + result.getErrorCode() + " " + result.toString());
		if (result.hasResolution())
		{
			latestResult = result;
			if (resolveOnFail)
				startResolution();
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		plusClient = Auth.getPlusClient(this, this, this);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.auth, menu);
		final MenuItem signIn = menu.findItem(R.id.sign_in);
		final SignInButton signInButton = (SignInButton) signIn.getActionView();
		if (signInButton != null)
			signInButton.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(final View v)
				{
					onSignInClicked();
				}
			});
		return true;
	}

	@Override
	public void onDisconnected()
	{
		invalidateOptionsMenu();
		plusClient.connect();
	}

	/**
	 * Starts a sync operation if there is a pending sync operation
	 */
	public void onInitializeDriveComplete()
	{
		if (BuildConfig.DEBUG)
			Log.d(getClass().getSimpleName(), "Drive Initialization complete, starting sync");
		sync();
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.sign_in:
				onSignInClicked();
				return true;
			case R.id.sign_out:
				plusClient.clearDefaultAccount();
				plusClient.disconnect();
				invalidateOptionsMenu();
				plusClient.connect();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		final boolean isConnected = plusClient.isConnected();
		final MenuItem signIn = menu.findItem(R.id.sign_in);
		signIn.setVisible(!isConnected);
		final MenuItem signOut = menu.findItem(R.id.sign_out);
		signOut.setVisible(isConnected);
		return true;
	}

	/**
	 * Callback for user initiated sign in process
	 */
	void onSignInClicked()
	{
		if (latestResult != null)
			startResolution();
	}

	@Override
	protected void onStart()
	{
		if (BuildConfig.DEBUG)
			Log.d(getClass().getSimpleName(), "onStart");
		plusClient.connect();
		super.onStart();
		final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Auth.ACTION_SYNC);
		try
		{
			intentFilter.addDataType(RecipeContract.Recipes.CONTENT_TYPE);
			intentFilter.addDataType(RecipeContract.Recipes.CONTENT_ITEM_TYPE);
			intentFilter.addDataType(RecipeContract.Ingredients.CONTENT_TYPE);
			intentFilter.addDataType(RecipeContract.Ingredients.CONTENT_ITEM_TYPE);
			intentFilter.addDataType(RecipeContract.Instructions.CONTENT_TYPE);
			intentFilter.addDataType(RecipeContract.Instructions.CONTENT_ITEM_TYPE);
		} catch (final MalformedMimeTypeException e)
		{
			Log.e(getClass().getSimpleName(), "Error adding data types", e);
		}
		localBroadcastManager.registerReceiver(syncBroadcastReceiver, intentFilter);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
		localBroadcastManager.unregisterReceiver(syncBroadcastReceiver);
	}

	private void startResolution()
	{
		if (BuildConfig.DEBUG)
			Log.d(getClass().getSimpleName(), "startResolution");
		try
		{
			latestResult.startResolutionForResult(this, REQUEST_ACCOUNT_RESOLUTION);
		} catch (final SendIntentException e)
		{
			Log.e(getClass().getSimpleName(), "Error resolving result " + latestResult.getErrorCode(), e);
			// Try, try again
			latestResult = null;
			plusClient.connect();
		}
	}

	/**
	 * Starts sync with Google Drive
	 */
	void sync()
	{
		if (BuildConfig.DEBUG)
			Log.d(getClass().getSimpleName(), "Sync Starting, connected: " + plusClient.isConnected());
		if (!plusClient.isConnected())
			return;
		new SyncDriveAsyncTask(this).execute(plusClient);
	}
}
