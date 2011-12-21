package com.scada.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

public class NetworkReceiver extends BroadcastReceiver {
	public static final String TAG = NetworkReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		boolean isNetworkDown = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

		if (isNetworkDown) {
			Log.d(TAG, "onReceive: NOT connected to Internet, stopping UpdaterService");
			context.stopService(new Intent(context, MessageProcessorService.class)); //
		} else {
			Log.d(TAG, "onReceive: Connected to Internet, starting UpdaterService");
			context.startService(new Intent(context, MessageProcessorService.class)); //
		}
	}
}
