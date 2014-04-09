package tw.g35gtw.cms.test.gps_nmea;

import java.util.Iterator;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.os.Build;

/* 日期：2014/04/09
 * 功能：讀取內建GPS的NMEA資料
 * 作者：Danny
 * 網站：http://cms.35g.tw/coding
 */
/* date：2014/04/09
 * description：read nmea data from internal GPS
 * author：Danny
 * website：http://cms.35g.tw/coding
 */


public class MainActivity extends Activity {
	private LocationManager locationManager=null;
	private Criteria criteria=null;
	private LocationListener locationListener = null;
	private GpsStatus.NmeaListener nmeaListener = null;
	private GpsStatus.Listener gpsStatusListener = null; 	
	private TextView txtGPS_Quality = null;
	private TextView txtGPS_Location = null;
	private TextView txtGPS_Satellites = null;
	//
	private Handler mHandler = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		

		//
		txtGPS_Quality = (TextView) findViewById(R.id.textGPS_Quality);
		txtGPS_Location = (TextView) findViewById(R.id.textGPS_Location);
		txtGPS_Satellites = (TextView) findViewById(R.id.textGPS_Satellites);
		registerHandler();
		registerListener();	
		locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
		locationManager.addNmeaListener(nmeaListener);
		//
		//
		
	

	}

	
@Override
protected void onDestroy() {
	// TODO Auto-generated method stub
	super.onDestroy();
	locationManager.removeUpdates(locationListener);
	locationManager.removeNmeaListener(nmeaListener);
}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		//if (id == R.id.action_settings) {
		//	return true;
		//}
		return super.onOptionsItemSelected(item);
	}
	
	private void registerListener(){
		locationListener=new LocationListener(){

			@Override
			public void onLocationChanged(Location loc) {
				// TODO Auto-generated method stub
				//定位資料更新時會回呼
                Log.d("GPS-NMEA", loc.getLatitude() + "," +  loc.getLongitude());
			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
				//定位提供者如果關閉時會回呼，並將關閉的提供者傳至provider字串中
			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				//定位提供者如果開啟時會回呼，並將開啟的提供者傳至provider字串中
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				// TODO Auto-generated method stub
				Log.d("GPS-NMEA", provider + "");
				//GPS狀態提供，這只有提供者為gps時才會動作
			    switch (status) {
			    case LocationProvider.OUT_OF_SERVICE:
			    	Log.d("GPS-NMEA","OUT_OF_SERVICE");
			        break;
			    case LocationProvider.TEMPORARILY_UNAVAILABLE:
			    	Log.d("GPS-NMEA"," TEMPORARILY_UNAVAILABLE");
			        break;
			    case LocationProvider.AVAILABLE:
			    	Log.d("GPS-NMEA","" + provider + "");

			        break;
			    }

			}
			
		};
//
	    nmeaListener = new GpsStatus.NmeaListener() {
	        public void onNmeaReceived(long timestamp, String nmea) {
	        	//check nmea's checksum
	        	if (isValidForNmea(nmea)){
	        		nmeaProgress(nmea);
	        		Log.d("GPS-NMEA", nmea);
	        	}
	        	
	        }
	};	
//
	gpsStatusListener = new GpsStatus.Listener(){
		public void onGpsStatusChanged(int event) {
			// TODO Auto-generated method stub
			GpsStatus gpsStatus;
			gpsStatus= locationManager.getGpsStatus(null);
			
			switch(event)
		     { 
		     case GpsStatus.GPS_EVENT_FIRST_FIX:
		              //
		              gpsStatus.getTimeToFirstFix();
		              Log.d("GPS-NMEA","GPS_EVENT_FIRST_FIX");
		      break;	      
		     case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
	
				Iterable<GpsSatellite> allSatellites = gpsStatus.getSatellites();   
				Iterator<GpsSatellite> it=allSatellites.iterator();
				
				int count = 0;
				while(it.hasNext())   
				{
					GpsSatellite gsl=(GpsSatellite)it.next();
					
					if (gsl.getSnr()>0.0){
						count++;
					}
					
				}
				

		      break;		      
		     case GpsStatus.GPS_EVENT_STARTED:
		             //Event sent when the GPS system has started.
		    	 Log.d("GPS-NMEA","GPS_EVENT_STARTED");
		     break;		      
		     case GpsStatus.GPS_EVENT_STOPPED:
		            //Event sent when the GPS system has stopped.
		    	 Log.d("GPS-NMEA","GPS_EVENT_STOPPED");
		      break;		      
		     default :
		      break;
		     }
		}
		
	};	
		
	}
	
private void registerHandler(){
	/*
	GGA Global Positioning System Fix Data. Time, Position and fix related data for a GPS receiver
	11
	1 2 34 5678 910|121314 15
	||||||||||||||| $--GGA,hhmmss.ss,llll.ll,a,yyyyy.yy,a,x,xx,x.x,x.x,M,x.x,M,x.x,xxxx*hh
	1) Time (UTC)
	2) Latitude
	3) N or S (North or South)
	4) Longitude
	5) E or W (East or West)
	6) GPS Quality Indicator,
	0 - fix not available, 
	1 - GPS fix,
	2 - Differential GPS fix
	7) Number of satellites in view, 00 - 12
	8) Horizontal Dilution of precision
	9) Antenna Altitude above/below mean-sea-level (geoid)
	10) Units of antenna altitude, meters
	11) Geoidal separation, the difference between the WGS-84 earth
	ellipsoid and mean-sea-level (geoid), "-" means mean-sea-level below ellipsoid
	12) Units of geoidal separation, meters
	13) Age of differential GPS data, time in seconds since last SC104
	type 1 or 9 update, null field when DGPS is not used
	14) Differential reference station ID, 0000-1023
	15) Checksum
		 */	
	mHandler = new Handler() {
		public void handleMessage(Message msg) {
			
			String str = (String) msg.obj;
			String[] rawNmeaSplit = str.split(",");
			txtGPS_Quality.setText(rawNmeaSplit[6]);
			txtGPS_Location.setText(rawNmeaSplit[2] + " " + rawNmeaSplit[3] + "," + rawNmeaSplit[4] + " " + rawNmeaSplit[5]);
			txtGPS_Satellites.setText(rawNmeaSplit[7]);

		}
		};

		
}
//custom
//取得nmea資料的callback
	private void nmeaProgress(String rawNmea){
		
		String[] rawNmeaSplit = rawNmea.split(",");
		
		if (rawNmeaSplit[0].equalsIgnoreCase("$GPGGA")){
			//send GGA nmea data to handler
			Message msg = new Message();
			msg.obj = rawNmea;
			mHandler.sendMessage(msg);
		}
		
	}

	
    private boolean isValidForNmea(String rawNmea){
        boolean valid = true;
        byte[] bytes = rawNmea.getBytes();
        int checksumIndex = rawNmea.indexOf("*");
        //NMEA 星號後為checksum number
        byte checksumCalcValue = 0;
        int checksumValue;
        
        //檢查開頭是否為$
        if ((rawNmea.charAt(0) != '$') || (checksumIndex==-1)){
            valid = false;
        }
        //
        if (valid){
            String val = rawNmea.substring(checksumIndex + 1, rawNmea.length()).trim();
            checksumValue = Integer.parseInt(val, 16);
            for (int i = 1; i < checksumIndex; i++){
                checksumCalcValue = (byte) (checksumCalcValue ^ bytes[i]);
            }
            if (checksumValue != checksumCalcValue){
                valid = false;
            }
        }        
        return valid;
    }	
}
