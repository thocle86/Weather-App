package com.thocle.android.weatherapp.models;

import com.thocle.android.weatherapp.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class City {

    public String mStringJson;
    public Long mLongitude;
    public Long mLatitude;
    public int mWeatherResIconWhite;
    public int mWeatherResIconGrey;
    public String mDescription;
    public String mTemperature;
    public int mPressure;
    public int mHumidity;
    public int mIdCity;
    public String mName;

    public City(String jsonResponse) throws JSONException {
        mStringJson = jsonResponse;
        JSONObject json = new JSONObject(mStringJson);

        JSONObject coord = json.getJSONObject("coord");
        mLongitude = coord.getLong("lon");
        mLatitude = coord.getLong("lat");

        JSONObject weather = json.getJSONArray("weather").getJSONObject(0);
        mWeatherResIconWhite = Util.setWeatherIcon(weather.getInt("id"), json.getJSONObject("sys").getLong("sunrise") * 1000, json.getJSONObject("sys").getLong("sunset") * 1000);
        mWeatherResIconGrey = Util.setWeatherIcon(weather.getInt("id"));
        mDescription = Util.capitalize(weather.getString("description"));

        JSONObject main = json.getJSONObject("main");
        mTemperature = String.format(Locale.FRENCH, "%.0f °C", main.getDouble("temp"));
        mPressure = main.getInt("pressure");
        mHumidity = main.getInt("humidity");

        mIdCity = json.getInt("id");
        mName = json.getString("name");

    }
}
