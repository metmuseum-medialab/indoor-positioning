/*
 * Copyright (C) 2011 Qubulus AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.qubulus.qpsexample;

import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.qubulus.qps.Intents;
import com.qubulus.qps.PositioningManager;

public class QpsExampleActivity extends MyLocationActivity {

	private static final String TAG = QpsExampleActivity.class.getSimpleName();

	private final Timer mTimer = new Timer();
	private TimerTask mTask;

	// Auxiliary views for demonstrating functionality. These can be removed
	// with no impact to your application
	EditText changeSiteIdInput;
	Button changeSiteId;
	TextView mapNameText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Register for position updates from QPS, whether a position estimate
		// was found or not
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intents.Position.ACTION);
		intentFilter.addAction(Intents.UnknwownPosition.ACTION);
		registerReceiver(mQpsReceiver, intentFilter);

		mapNameText = (TextView) findViewById(R.id.mapName);
		changeSiteIdInput = (EditText) findViewById(R.id.site_id_edit);
		changeSiteId = (Button) findViewById(R.id.submit);
		changeSiteId.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				PositioningManager manager = PositioningManager
						.get(QpsExampleActivity.this);
				manager.updateSite(changeSiteIdInput.getText().toString());
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Close the service together with the activity to release resources,
		// since no other client will issue requests
		PositioningManager manager = PositioningManager.get(this);
		manager.stopService();

		// Unregister position updates
		unregisterReceiver(mQpsReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Periodically ask for a position estimate
		mTask = new TimerTask() {
			@Override
			public void run() {
				PositioningManager manager = PositioningManager
						.get(QpsExampleActivity.this);
				manager.requestPosition();
			}
		};
		mTimer.schedule(mTask, 0, PositioningManager.POSITION_REQ_INTERVAL);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Suspend regular position estimate requests
		mTask.cancel();
	}

	private final BroadcastReceiver mQpsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			Bundle e = intent.getExtras();

			if (Intents.Position.ACTION.equals(action)) {
				// Got a position estimate

				double lat = 0;
				double lng = 0;
				String map = "";

				lat = e.getDouble(Intents.Position.Extras.LATITUDE.name());
				lng = e.getDouble(Intents.Position.Extras.LONGITUDE.name());
				map = e.getString(Intents.Position.Extras.MAP.name());

				Log.d(TAG, "Position: lat=" + lat + ",long=" + lng + ",map="
						+ map);

				setPosition(lat, lng);
				mapNameText.setText(map);

			} else if (Intents.UnknwownPosition.ACTION.equals(action)) {
				if (e != null) {
					// Could not estimate position because of a runtime error

					final String EXTRA_WIFI = Intents.UnknwownPosition.Extras.WIFI_OFF
							.name();
					final String EXTRA_ERROR = Intents.UnknwownPosition.Extras.ERROR
							.name();

					if (e.getBoolean(EXTRA_WIFI)) {
						Log.d(TAG, "Unknown position, wifi disabled");
					} else if (e.getString(EXTRA_ERROR) != null) {
						String msg = e.getString(EXTRA_ERROR);
						Log.d(TAG, "Unknown position, error:" + msg);
					}

				} else {
					// Impossible to estimate a current position, most likely
					// because the user is far from the site
					Log.d(TAG, "Unknown position");
				}

				resetPosition();
			}
		}
	};
}