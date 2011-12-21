package com.scada.client;

import java.util.Vector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.scada.utils.Command;
import com.scada.utils.ProtocolUtils;

public class TimelineActivity extends BaseActivity {
	IntentFilter filter;
	TimelineReceiver receiver;
	public static final String TAG = TimelineActivity.class.getSimpleName();
	
	StreamManager streamManager;
	
	Button testButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timeline);
		
		filter = new IntentFilter("com.scada.client.NEW_STATUS");
		receiver = new TimelineReceiver();
		
		testButton = (Button) findViewById(R.id.button1);
		testButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Vector<Command> test = new Vector<Command>();
    			for( int i = 0; i < 5; i++)
    			{
    				test.add(new Command(ProtocolUtils.COMMAND_SYSINFO));
    			}
    			((DSCApplication)getApplication()).addCommandCollectionToQueue(test);
            }
        });
		
		//TODO: Stream initialization should be moved to service in order to avoid hang in UI in case there is connection issues 
		streamManager = ((DSCApplication)getApplication()).getStreamManager("10.0.0.135", 12111);
		startService(new Intent(this, MessageProcessorService.class));	
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
				
		registerReceiver(receiver, filter);
	}
			
	class TimelineReceiver extends BroadcastReceiver {		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("TimelineReceiver" , "onReceived - number of new statuses is " + intent.getIntExtra(Constants.NEW_STATUS_EXTRA_COUNT, 0));
		}
	}

}
