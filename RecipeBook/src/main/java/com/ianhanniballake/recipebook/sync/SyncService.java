package com.ianhanniballake.recipebook.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Service to handle sync with Drive. It instantiates the SyncAdapter and returns its IBinder.
 */
public class SyncService extends Service
{
	private static SyncAdapter sSyncAdapter = null;
	private static final Object sSyncAdapterLock = new Object();

	@Override
	public IBinder onBind(final Intent intent)
	{
		return sSyncAdapter.getSyncAdapterBinder();
	}

	@Override
	public void onCreate()
	{
		synchronized (sSyncAdapterLock)
		{
			if (sSyncAdapter == null)
				sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
		}
	}
}
