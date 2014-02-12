package com.ssegment7.typestop;

import java.util.HashSet;
import java.util.Set;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class SpeedTracker extends Service implements LocationListener {
	private LocationManager locationManager;
	private boolean networkEnabled = false;
	private boolean gpsEnabled = false;
	
	private final long MIN_TIME_BW_UPDATES = 100;// 0.1 seconds
	private final float MIN_DIST_BW_UPDATES = 3;
	
	private final float MAX_ALLOWED_SPEED = 2.2352f; //5 miles per hour
	
	private Set<Integer> inputComponentIds = new HashSet<Integer>();
	
	///////////////////////////// Service Related Methods /////////////////////
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//We're being started by some app that want's to be safe, 
		//Get Input Text id
		Integer inputId = intent.getIntExtra(Constants.INPUT_COMPONENT_ID_KEY, -1);
		if(inputId != -1) {
			this.inputComponentIds.add(inputId);
		}
		//TODO: Figure out how we can be called by multiple apps and run only one service
		
		//1. register for geo location changes and detect speed
		startLocationService();
		
	    // We want this service to continue running until it is explicitly
	    // we stop it (which is likely never)
	    return START_REDELIVER_INTENT;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	////////////////////Location Change related methods ///////////////////
	public void onLocationChanged(Location loc) {
		float speed = loc.getSpeed();
		
		if(speed > MAX_ALLOWED_SPEED) {
			InputMethodManager imm = 
					(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			for(Integer id : inputComponentIds) {
				View v = findViewById(R.id)
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			}
		}
	}

	public void onProviderDisabled(String provider) {
		startLocationService();		
	}

	public void onProviderEnabled(String provider) {
		startLocationService();
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		if(LocationProvider.AVAILABLE != status)
			throw new NoLocationServiceException();
	}

	///////////////////////////private methods //////////////////////
	private void startLocationService() throws NoLocationServiceException {
		try {
			locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
			// getting GPS status
			gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			// getting network status
			networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			if (gpsEnabled == false && networkEnabled == false) {
				throw new NoLocationServiceException(); // no network provider is enabled
			} else {
				if (gpsEnabled) {
					locationManager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER,
							MIN_TIME_BW_UPDATES, MIN_DIST_BW_UPDATES, this);
				} else if (networkEnabled) {
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							MIN_TIME_BW_UPDATES, MIN_DIST_BW_UPDATES, this);
				}
			}
		} catch (Exception e) {
			throw new NoLocationServiceException();
		}
	}
}
