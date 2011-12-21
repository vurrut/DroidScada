package com.scada.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
* The base activity with common features shared by TimelineActivity and 
* * StatusActivity
*/
public class BaseActivity extends Activity {
	DSCApplication appObject;
	private static final String TAG = BaseActivity.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		appObject = (DSCApplication) getApplication();
	}
	
	// Called only once first time menu is clicked on
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
	
	// Called every time user clicks on a menu item
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.itemToggleService:
				if (appObject.isServiceRunning()) {
					stopService(new Intent(this, MessageProcessorService.class));
				} else {
					startService(new Intent(this, MessageProcessorService.class));
				}
				break;
			case R.id.itemTimeline:
				startActivity(new Intent(this, TimelineActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP).addFlags(
								Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
				break;
		}
		return true;
	}
	
	// Called every time menu is opened
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		MenuItem toggleItem = menu.findItem(R.id.itemToggleService);
		if (appObject.isServiceRunning()) {
			toggleItem.setTitle(R.string.titleServiceStop);
			toggleItem.setIcon(android.R.drawable.ic_media_pause);
		} else {
			toggleItem.setTitle(R.string.titleServiceStart);
			toggleItem.setIcon(android.R.drawable.ic_media_play);
		}
		return true;
	}
}