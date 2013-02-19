package com.aquamet.saramap;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends SherlockActivity {

	/* Hidden and messenger variables. */
	/* --------------------------------------------------------------------------------------------------------------------------------------------- */
	public final static String MAP = "com.aquamet.saramap.MAP";
	public final static String ERROR = "com.aquamet.saramap.ERROR";
	
	/* Activity Constants */
	/* --------------------------------------------------------------------------------------------------------------------------------------------- */
	private static final int SIMULATIONS = 1;
	public final static int MAP_CODE = 0;
	
	/* Error Constants */
	/* --------------------------------------------------------------------------------------------------------------------------------------------- */
	private static final int BAD_CONNECTIVITY = 1;
	private static final int NO_SDSTORAGE = 2;
			
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_locate);
		
		// Populates spinner choices.
		Spinner spinner = (Spinner) findViewById(R.id.maps_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.maps_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		// End of spinner population inhabiter.
		
		// Handles intents.
		Intent intent = getIntent();
		int error_code = intent.getIntExtra(ERROR,0);
		
		handleError(error_code);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
	    super.onCreateOptionsMenu(menu);

	    menu.add(0,SIMULATIONS,0,"Minhas Simulações")
	    	.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	    return true;
	}
	
	public boolean onOptionsItemSelected (MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case SIMULATIONS:
	            openOldSimulations();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (Integer.parseInt(android.os.Build.VERSION.SDK) < 9
	            && keyCode == KeyEvent.KEYCODE_BACK
	            && event.getRepeatCount() == 0) {
	        Log.d("CDA", "onKeyDown Called");

	        onBackPressed();
	        return true;
	    }
	    //return super.onKeyDown(keyCode, event);
	    return true;
	}
	
	public void onBackPressed() {
		// Since this is the main activity, a back press should mean the end of the application run.
		moveTaskToBack(true);	
		return;
	}
	
	public void submitLocation(View view) {
		Spinner spinner = (Spinner) findViewById(R.id.maps_spinner);
		Intent intent = new Intent(this, MapActivity.class);
		String map_name = spinner.getSelectedItem().toString();
		
		intent.putExtra(MAP, map_name);
		startActivity(intent);
	}

	private void openOldSimulations() {
		Intent intent = new Intent(this, OldSimulationsActivity.class);
		startActivity(intent);	
	}
	
	private void handleError(int error_code) {	
		Context context = getApplicationContext();
		String error;
		
		switch(error_code) {
		case BAD_CONNECTIVITY:
			error = "Sem acesso à rede de internet.\n\n Verfique sua conexão e tente novamente.";
			break;
		case NO_SDSTORAGE:
			error = "Não foi detectado memória externa. \n\n Verifique que cartão SD está conectado.";
			break;
		default:
			return;
		}
		
		Toast toast = Toast.makeText(context, error, Toast.LENGTH_LONG);
		toast.show();
		
		return;
	}
}
