package com.aquamet.saramap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.aquamet.objects.Ship;
import com.aquamet.objects.Simulation;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SimulationActivity extends Activity {

	/* Hidden and messenger variables. */
	public final static String MAP = "com.aquamet.saramap.MAP";
	public final static String SHIP = "com.aquamet.saramap.SHIP";
	
	/* Activity variables */
	String map_descriptor;
	Ship ship = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_simulation);
		
		Intent intent = getIntent();
		map_descriptor = intent.getStringExtra(MAP);
		ship = (Ship) intent.getParcelableExtra(SHIP);
		
		updateViews();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_simulation, menu);
		return true;
	}

	private void updateViews() {
		TextView name_view = (TextView) findViewById(R.id.shipname);
		String name_text = String.format("%s\t\t\t", ship.getName());
		name_view.setText(name_text);
		
		TextView lat_view = (TextView) findViewById(R.id.latitude);
		String lat_text = String.format("%f\t\t\t", ship.getLat());
		lat_view.setText(lat_text);
		
		TextView longi_view = (TextView) findViewById(R.id.longitude);
		String longi_text = String.format("%f\t\t\t", ship.getLongi());
		longi_view.setText(longi_text);	
		
		TextView map_view = (TextView) findViewById(R.id.map_d);
		String map_text = String.format("%s\t\t\t", map_descriptor);
		map_view.setText(map_text);	
	}
	
	public void submitSimulation(View view) {
		// TODO Add server algorithm web query.
		Context context = getApplicationContext();
		TextView vol_view = (TextView) findViewById(R.id.volume);
		String volume = vol_view.getText().toString();
		TextView name_view = (TextView) findViewById(R.id.shipname);
		String name = name_view.getText().toString();
		
		// Check if memory is available first.
		if (memoryAvailable()) {
			Simulation simulation = new Simulation(volume, map_descriptor, name);
			String status = String.format("Simulação salva com nome:\n\n%s", saveToFile(simulation));
			
			Toast toast = Toast.makeText(context, status, Toast.LENGTH_LONG);
			toast.show();
		}
		
		return;
	}
	
	private boolean memoryAvailable() {
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageWriteable = false;
		}
		
		return mExternalStorageWriteable;
	}
	
	private String saveToFile(Simulation sim) {
		String filename = sim.getName();
		File file = new File(getExternalFilesDir("/simulations"), filename);
		try {
			FileOutputStream os = new FileOutputStream(file);
			ObjectOutputStream save = new ObjectOutputStream(os);
			save.writeObject(sim);
			save.close();
			
		} catch (FileNotFoundException e) {
			Log.w("ExternalStorage", "Error writing " + file, e);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return filename;
	}
}
