package com.aquamet.saramap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.aquamet.objects.Simulation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;

public class OldSimulationsActivity extends SherlockListActivity {

	private static final int MYMAPS = 1;
	private ArrayList<Simulation> sims = new ArrayList<Simulation>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_old_simulations);
		
		getSimulations();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
	    super.onCreateOptionsMenu(menu);

	    menu.add(0,MYMAPS,0,"Meus Mapas")
	    	.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	    return true;
	}

	public boolean onOptionsItemSelected (MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case MYMAPS:
	            openMain();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	private void openMain() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}
	
	private void getSimulations() {
		
		if(!memoryAvailable()) {
			return;
		}
		
		File dir = getExternalFilesDir("/simulations");
		File[] files = dir.listFiles();
		
		for(File file : files) {
			try {
				FileInputStream is = new FileInputStream(file);
				ObjectInputStream open = new ObjectInputStream(is);
				sims.add((Simulation) open.readObject());
				open.close();
				
			} catch (FileNotFoundException e) {
				Log.w("ExternalStorage", "Error reading " + file, e);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
		}
		
		String[] values = new String[sims.size()];
		for (int i = 0; i < values.length; i++) {
		    values[i] = sims.get(i).getName();
		    values[i] += "\nProgresso: " + sims.get(i).progress + "%";
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		        android.R.layout.simple_list_item_1, values);
		    setListAdapter(adapter);
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

}
