package com.aquamet.objects;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Simulation implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Date date_in;
	public String volume;
	private String map_d;
	private String shipname;
	public double progress = 0;
	
	public Simulation(String _volume, String _map_d, String _shipname) {
		date_in = new Date();
		volume = _volume;
		map_d = _map_d;
		shipname = _shipname;
	}
	
	public String getDate() {
		String format = "HH:mm dd-MM-yyyy";
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.FRANCE);
		String date_d = sdf.format(date_in);
		
		return date_d;
	}
	
	public String getMap() {
		return map_d;
	}
	
	public String getName() {
		String name = getDate();
		name = shipname + "-" + name;
		name = name.replace(" ","_");
		name = name.replace(":","-");
		name = name.replace("\t\t\t",".");
		
		return name;
	}
}
