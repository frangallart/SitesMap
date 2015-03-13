package com.example.fran.sitesmap.Models;
/**
 * Created by Fran on 12/03/2015.
 */

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Classe que modela les dades d'un punt en el mapa
 * <p/>
 */
public class MapPoint implements Parcelable {
    private int _id;
    private String name, city;
    private float latitude, longitude;

    /**
     * Constructor per defecte
     */
    public MapPoint() {
    }

    /**
     * Constructor amb paràmetres
     *
     * @param _id
     * @param nom
     * @param ciutat
     * @param latitud
     * @param longitud
     */
    public MapPoint(int _id, String nom, String ciutat, float latitud, float longitud) {
        this._id = _id;
        this.name = nom;
        this.city = ciutat;
        this.latitude = latitud;
        this.longitude = longitud;
    }

    //Getters i Setters
    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String nom) {
        this.name = nom;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String ciutat) {
        this.city = ciutat;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitud) {
        this.latitude = latitud;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitud) {
        this.longitude = longitud;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeString(name);
        dest.writeFloat(latitude);
        dest.writeFloat(longitude);
        dest.writeString(city);
    }

    public static final Parcelable.Creator<MapPoint> CREATOR = new Parcelable.Creator<MapPoint>() {
        public MapPoint createFromParcel(Parcel in) {
            return new MapPoint(in);
        }

        public MapPoint[] newArray(int size) {
            return new MapPoint[size];
        }
    };

    private MapPoint(Parcel dest) {
        _id = dest.readInt();
        name = dest.readString();
        latitude = dest.readFloat();
        longitude = dest.readFloat();
        city = dest.readString();
    }
}
