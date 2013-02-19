package com.aquamet.saramap;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aquamet.objects.Ship;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class MapActivity extends FragmentActivity {
	
	/* --------------------------------------------------------------------------------------------------------------------------------------------- */
	/* ------------------------------------------------------------- Global Variables -------------------------------------------------------------- */
	/* --------------------------------------------------------------------------------------------------------------------------------------------- */
	
	/* Messenger variables. */
	/* --------------------------------------------------------------------------------------------------------------------------------------------- */
	public final static String MAP = "com.aquamet.saramap.MAP";
	public final static String SHIP = "com.aquamet.saramap.SHIP";
	public final static String ERROR = "com.aquamet.saramap.ERROR";

	
	/*  Activity variables. */
	/* --------------------------------------------------------------------------------------------------------------------------------------------- */
	private static final String MT_ID = "7YDE2KMJFKU7TRHHDBBFI84R"; // Marine Traffic Developer's ID, need to change for production code.
	private LatLng MAP_CENTER = new LatLng(-22.82334,-43.144684); // Default RIO
	private LatLng NE_BOUND = new LatLng(-22.66649545062797, -42.993621826171875); // Default RIO NE
	private LatLng SW_BOUND = new LatLng(-23.0272915621104, -43.29780578613281); // Default RIO SW
	private LatLngBounds MAP_BOUNDS = new LatLngBounds(SW_BOUND,NE_BOUND);
	private String map_descriptor;
	private GoogleMap map;
	
	String[] mmsi;
	private ArrayList<Ship> ships = new ArrayList<Ship>();
	
	/* Error Constants. */
	/* --------------------------------------------------------------------------------------------------------------------------------------------- */
	private static final int BAD_CONNECTIVITY = 1;
	private static float ZOOM_LEVEL = (float) 10.5;
	
	/* Loader variables. */
	/* --------------------------------------------------------------------------------------------------------------------------------------------- */
	private ProgressDialog progressDialog;

	
	/* --------------------------------------------------------------------------------------------------------------------------------------------- */
	/* --------------------------------------------------------------- Main Program ---------------------------------------------------------------- */
	/* --------------------------------------------------------------------------------------------------------------------------------------------- */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Initiate loader, so that files can be acquired in background thread.
		new Loader().execute();   
	}
	
	/* onClick methods. */
	public void submitSimulation(String map_descriptor, Ship ship) {
		Intent intent = new Intent(this, SimulationActivity.class);
 		intent.putExtra(MAP, map_descriptor);
		intent.putExtra(SHIP, ship);
		
		startActivity(intent);
	}
	
	/* --------------------------------------------------------------------------------------------------------------------------------------------- */
	/* --------------------------------------------------------------- MMSI Helpers ---------------------------------------------------------------- */
	/* --------------------------------------------------------------------------------------------------------------------------------------------- */
	
	
	/* MMSI auxiliary methods.
	 * 
	 * public String[] getMMSI()
	 * public Ship parseMMSI()
	 * 
	 */
	
	/** From a resource found in <i>assets/mmsi.txt</i>, this method returns a string of MMSI codes.
	 * @param None
	 * @return <b>String[]</b> mmsi 
	 * @throws IOException Thrown if file <i>assets/mmsi.txt</i> isn't found.*/
	public String[] getMMSI() throws IOException {
		AssetManager am = this.getAssets();
		ArrayList<String> mmsi_list = new ArrayList<String>();
		BufferedReader br = null;
		String line = null;
		
		// Try to open assets/mmsi.txt, fails if exception thrown.
		try {
			InputStream  file_stream = am.open("mmsi.txt");
			br = new BufferedReader(new InputStreamReader(file_stream), 8192);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// Adds to list until empty.
		for (; (line = br.readLine()) != null;) {
				mmsi_list.add(line);
		}
		
		// Attempts to close stream, fails if exception thrown.
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Creates mmsi String array from String list.
		String[] mmsi = new String[mmsi_list.size()];
		for (int i = 0; i < mmsi.length; i++) {
		    mmsi[i] = mmsi_list.get(i);
		}
		return mmsi;
	}
	
	/** This method parses a web response from <i>MarineTraffic.com</i> with parameter mmsi and returns a <b>Ship</b> object with appropriate properties.
	 * @param <b>String</b> mmsi
	 * @return <b>Ship</b> ship
	 * */
	public Ship parseMMSI(String mmsi) throws Exception {
 		double[] location = new double[2];
		String str = String.format("http://www.marinetraffic.com/ais/getvesselxml.aspx?mmsi=%s&id=%s", mmsi, MT_ID);
		URL url = new URL(str);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		String inputLine;
		String shipname = "Nome Desconhecido";
		
		/*
		 * The following XML string is an example output from the web query above.
		 * <VESSEL>
			<V_POS MMSI="710000532" SHIPNAME="ROMULO ALMEIDA" LAT="-23.56147" LON="-43.74778" SPEED="131" HEADING="226" SHIPTYPE="8" LENGTH="182" WIDTH="31" DESTINATION="RIO DE JANEIRO" FLAG="BR" TIMESTAMP="2013-01-24T16:51:00"/>
			</VESSEL>
		 * 
		 */
        while ((inputLine = in.readLine()) != null) {
            Pattern pattern_latd = Pattern.compile("LAT=\"[+-]*\\d+\\.\\d+\"");
            Matcher matcher_latd = pattern_latd.matcher(inputLine);
            
            Pattern pattern_longid = Pattern.compile("LON=\"[+-]*\\d+\\.\\d+\"");
            Matcher matcher_longid = pattern_longid.matcher(inputLine);
            
            Pattern pattern_named = Pattern.compile("SHIPNAME=\"[\\w\\s]*\"");
            Matcher matcher_named = pattern_named.matcher(inputLine);
            
            // Extrai a latitude do descritor da latitude.
            while(matcher_latd.find()) {
            	String lat_descriptor = matcher_latd.group();
            	Pattern pattern_lat = Pattern.compile("[+-]*\\d+\\.\\d+");
            	Matcher matcher_lat = pattern_lat.matcher(lat_descriptor);
            	
            	while(matcher_lat.find()) {
            		location[0] = Double.parseDouble(matcher_lat.group());
            	}
            }
            
            // Extrai a longitude do descritor da longitude.
            while(matcher_longid.find()) {
            	String longi_descriptor = matcher_longid.group();
            	Pattern pattern_longi = Pattern.compile("[+-]*\\d+\\.\\d+");
            	Matcher matcher_longi = pattern_longi.matcher(longi_descriptor);
            	
            	while(matcher_longi.find()) {
            		location[1] = Double.parseDouble(matcher_longi.group());
            	}
            }
            
            // Extrai o nome do navio.
            while(matcher_named.find()) {
            	String name_descriptor = matcher_named.group();
            	Pattern pattern_name = Pattern.compile("\"([\\w\\s]*)\"");
            	Matcher matcher_name = pattern_name.matcher(name_descriptor);
            	
            	while(matcher_name.find()) {
            		shipname = matcher_name.group(1);
            	}
            }
        }
        in.close();
        Ship ship = new Ship(shipname, mmsi, location);
		return ship;
	}
	
	public Ship getShipFromMarker(Marker marker) {
		String mmsi = marker.getSnippet();
		Ship ship_found = null;
		
		for(Ship ship : ships) {
			if(ship.getMMSI().equals(mmsi)) {
				ship_found = ship;
				break;
			}
		}
		return ship_found;
	}
	
	/* --------------------------------------------------------------------------------------------------------------------------------------------- */
	/* ------------------------------------------------------------ GoogleMap Helpers -------------------------------------------------------------- */
	/* --------------------------------------------------------------------------------------------------------------------------------------------- */
	
	/* GoogleMap auxiliary functions
	 * 
	 * public GoogleMap setupMap(GoogleMap map)
	 * public void setupRegion(GoogleMap map)
	 * public LatLng  map2LatLng(String map_descriptor)
	 * 
	 * */
	
	/** This method takes in as a parameter a GoogleMap and prepares it for use.
	 * @param <b>GoogleMap</b> map
	 * @return <b>GoogleMap</b> map
	 * */
	public GoogleMap setupMap(GoogleMap map) {
		if (map == null) {
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Just in case null map was accidently passed to setupMap.
        }
		map.setMapType(3); // TERRAIN
		CameraPosition cameraPosition = new CameraPosition.Builder()
	    .target(MAP_CENTER)      			
	    .zoom(ZOOM_LEVEL)
	    .build();
		
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		map.setOnCameraChangeListener(new OnCameraChangeListener() {
	        public void onCameraChange(CameraPosition cameraPosition) {
	        		ZOOM_LEVEL = cameraPosition.zoom;
	                recenterMap(ZOOM_LEVEL);
	        }
		});
		
		return map;
	}
	
	protected void recenterMap(float zoom) {
		if(MAP_BOUNDS.contains(map.getCameraPosition().target) == false) {
			CameraPosition cameraPosition = new CameraPosition.Builder()
		    .target(MAP_CENTER)      
		    .zoom(zoom)
		    .build();     
			
			map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
			
		} else {
			MAP_CENTER = map.getCameraPosition().target;
		}
	}

	/** This method takes in a GoogleMap as a parameter and adds appropriate markers such as MMSI transmitting ships.
	 * @param <b>GoogleMap</b> map
	 * @return <b>void</b>
	 * Exception thrown if ship MMSI is erroneous. 
	 *  */
	public void setupRegion(GoogleMap map, ArrayList<Ship> ships) throws IOException {
	    for(Ship ship : ships) {
	    	map.addMarker(new MarkerOptions()
	    	   .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_red))
		       .position(new LatLng(ship.getLat(), ship.getLongi()))
		       .title(ship.getName())
		       .snippet(ship.getMMSI()));
	    }
	}
	
	/** This method takes as a parameter a map_descriptor (String) and returns the correct LatLng center for the described map.
	 * @param <b>String</b> map_descriptor
	 * @return <b>LatLng</b> map_center
	 * */
	public LatLng map2LatLng(String map_descriptor) {
		LatLng map_center = null;
		if (map_descriptor.equals("Baia de Guanabara")) {
			LatLng RIO = new LatLng(-22.82334,-43.144684);
			map_center = RIO;
		}
		else if (map_descriptor.equals("RJ-2")) {
			// Do nothing for now.
		}
		else if (map_descriptor.equals("ES-1")) {
			// Do nothing for now.
		}
		else if (map_descriptor.equals(null)) {
			map_center = MAP_CENTER; // Returns Rio de Janeiro
			// Default behavior. Maybe better return to past screen?
		}
		return map_center;
	}

	/* --------------------------------------------------------------------------------------------------------------------------------------------- */
	/* --------------------------------------------------------------- Loader Class ---------------------------------------------------------------- */
	/* --------------------------------------------------------------------------------------------------------------------------------------------- */
	
    private class Loader extends AsyncTask<Void, Integer, Void>  {  
        //To use AsyncTask, it is necessary to @Override the following methods.

		@Override  
        protected void onPreExecute()  
        {  
            //Create a new progress dialog  
            progressDialog = new ProgressDialog(MapActivity.this);  
            //Set the progress dialog to display a horizontal progress bar  
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);  
            //Set the dialog title to 'Loading...'  
            progressDialog.setTitle("Carregando...");  
            //Set the dialog message to 'Loading application View, please wait...'  
            progressDialog.setMessage("Carregando dados da MarineTraffic , favor esperar...");  
            //This dialog can be canceled by pressing the back key  
            progressDialog.setCancelable(true);  
            //This dialog isn't indeterminate  
            progressDialog.setIndeterminate(false);  
            //The maximum number of items is 100  
            progressDialog.setMax(100);  
            //Set the current progress to zero  
            progressDialog.setProgress(0);  
            //Display the progress dialog  
            progressDialog.show();  
        }  
  
        //The code to be executed in a background thread.  
        @Override  
        protected Void doInBackground(Void... params)  
        {  
        	// Test Internet connection first.
        	if(!isOnline()) {
        		returnError(BAD_CONNECTIVITY);
        		return null;
        	}
            //Get the current thread's token  
			synchronized (this)  
			{  
				// Get ship MMSI codes from assets/mmsi.txt. Not registering on progressDialog since this is much faster than the next process.
				try {
					mmsi = getMMSI();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				// Download XML files from MarineTraffic and publishProgress to processDialog.
				int mmsi_size = mmsi.length;
				for(int i = 0; i < mmsi_size; i++) {
					try {
						ships.add(parseMMSI(mmsi[i]));
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					publishProgress(100/(mmsi_size)*i);
				}
			}  
            return null;  
        }  
  
        //Update the progress  
        @Override  
        protected void onProgressUpdate(Integer... values)  
        {  
            //set the current progress of the progress dialog  
            progressDialog.setProgress(values[0]);  
        }  
  
        //after executing the code in the thread  
        @Override  
        protected void onPostExecute(Void result)  
        {  
            //close the progress dialog  
            progressDialog.dismiss();  
            //initialize the View  
            setContentView(R.layout.activity_main);
            
         // Recovers map choice from previous activity.
    		Intent intent = getIntent();
    		map_descriptor = intent.getStringExtra(MAP);
    		MAP_CENTER = map2LatLng(map_descriptor);
    		
    		// Creates map and sets markers.
    		map = setupMap(map);
    		try {
    			setupRegion(map,ships);
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    		
    		// Make markers clickable.
    		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
    	        public void onInfoWindowClick(Marker marker) {
    	        	Ship ship = getShipFromMarker(marker);
    	            submitSimulation(map_descriptor, ship);
    	        }
    	    });
        }  
        
    	/* --------------------------------------------------------------------------------------------------------------------------------------------- */
    	/* ------------------------------------------------------------ AsyncTask Helpers -------------------------------------------------------------- */
    	/* --------------------------------------------------------------------------------------------------------------------------------------------- */
        
        // Checks Internet connectivity before attempting to download files.
        public boolean isOnline() {
            ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return true;
            }
            return false;
        }
    }

	public void returnError(int error_code) {
		Intent intent = new Intent(this, MainActivity.class);
		
		intent.putExtra(ERROR, error_code);
		startActivity(intent);
	}
}