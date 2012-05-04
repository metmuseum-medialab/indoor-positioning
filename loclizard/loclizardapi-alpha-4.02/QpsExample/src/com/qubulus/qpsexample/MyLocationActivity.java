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

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MyLocationActivity extends MapActivity {

	private final static double E6_DOUBLE = 1000000.0;
	private final int ZOOM_LEVEL = 21;

	private MapView mMapView;
	private LocationOverlay mLocationOverlay;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mMapView = (MapView) findViewById(R.id.mapview);
		mMapView.setBuiltInZoomControls(true);

		mLocationOverlay = new LocationOverlay(getResources().getDrawable(
				R.drawable.pin_center));
		mMapView.getOverlays().add(mLocationOverlay);

		ImageView myLoc = (ImageView) findViewById(R.id.myloc);
		myLoc.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				GeoPoint p = mLocationOverlay.getGeoPoint();
				if (p != null) {
					MapController c = mMapView.getController();
					c.setZoom(ZOOM_LEVEL);
					c.animateTo(p);
				}
			}
		});
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	protected void setPosition(double latitude, double longitude) {
		int latitudeE6 = (int) Math.round(latitude * E6_DOUBLE);
		int longitudeE6 = (int) Math.round(longitude * E6_DOUBLE);
		mLocationOverlay.setGeoPoint(new GeoPoint(latitudeE6, longitudeE6));
		mMapView.postInvalidate();
	}

	protected void resetPosition() {
		mLocationOverlay.setGeoPoint(null);
		mMapView.postInvalidate();
	}

	private static class LocationOverlay extends ItemizedOverlay<OverlayItem> {

		private GeoPoint mGeoPoint;

		public LocationOverlay(Drawable defaultMarker) {
			super(boundCenterBottom(defaultMarker));
			populate();
		}

		@Override
		protected synchronized OverlayItem createItem(int i) {
			return new OverlayItem(mGeoPoint, null, null);
		}

		@Override
		public synchronized int size() {
			return mGeoPoint == null ? 0 : 1;
		}

		synchronized void setGeoPoint(GeoPoint geoPoint) {
			mGeoPoint = geoPoint;
			setLastFocusedIndex(-1);
			populate();
		}

		synchronized GeoPoint getGeoPoint() {
			return mGeoPoint;
		}
	}
}
