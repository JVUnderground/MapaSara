package com.aquamet.objects;

import android.os.Parcel;
import android.os.Parcelable;

/** Ship is a class that represents MMSI code transmitting ships. 
 * @author jhc @ AQUAMET
 * @version 0.0.1 (pre-alpha)
 * 
 * */
public class Ship implements Parcelable {
	
	/** Ship has the following properties
	 *  */
	private String shipname = "Nome Desconhecido";
	private String MMSI = "XXXX";
	private double[] loc = new double[2];
	
	public Ship(String _shipname, String _MMSI, double[] _loc) {
		shipname = _shipname;
		MMSI = _MMSI;
		loc[0] = _loc[0]; // Latitude
		loc[1] = _loc[1]; // Longitude
		
	}
	public String getName() {
		return shipname;
	}
	public String getMMSI() {
		return MMSI;
	}
	public double getLat() {
		return loc[0];
	}
	public double getLongi() {
		return loc[1];
	}
	
	// Required Parcelable implementations. Should not be used manually.
	public int describeContents() {
        return 0;
    }
	public void writeToParcel(Parcel out, int flags) {
        out.writeString(shipname);
        out.writeString(MMSI);
        out.writeDoubleArray(loc);
    }
	public static final Parcelable.Creator<Ship> CREATOR = new Creator<Ship>() {
		public Ship createFromParcel(Parcel in) {
			return new Ship(in);
		}
		public Ship[] newArray(int size) {
			return new Ship[size];
		}
	};
	private Ship(Parcel in) {
        shipname = in.readString();
        MMSI = in.readString();
        loc = in.createDoubleArray();
    }
}
