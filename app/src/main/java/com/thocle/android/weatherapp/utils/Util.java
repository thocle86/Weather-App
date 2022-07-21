package com.thocle.android.weatherapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.thocle.android.weatherapp.R;
import com.thocle.android.weatherapp.models.City;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Util {

    public static final String KEY_MESSAGE = "key_message";

    private static final String SHARED_PREFERENCES = "SHARED_PREFERENCES";
    private static final String SHARED_FAVORITE_CITIES = "SHARED_FAVORITE_CITIES";
    private static final String SHARED_CITY_SELECTED_LAT = "SHARED_PREFS_CURRENT_LAT";
    private static final String SHARED_CITY_SELECTED_LON = "SHARED_PREFS_CURRENT_LON";

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = context.getSystemService(ConnectivityManager.class);
        Network currentNetwork = connectivityManager.getActiveNetwork();
        NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(currentNetwork);
        return currentNetwork != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    /*
     * Méthode qui initialise l'icon blanc présent dans la MainActivity
     * */
    public static int setWeatherIcon(int actualId, long sunrise, long sunset) {

        int id = actualId / 100;
        int icon = R.drawable.weather_sunny_white;

        if (actualId == 800) {
            long currentTime = new Date().getTime();

            if (currentTime >= sunrise && currentTime < sunset) {
                icon = R.drawable.weather_sunny_white;
            } else {
                icon = R.drawable.weather_clear_night_white;
            }
        } else {
            switch (id) {
                case 2:
                    icon = R.drawable.weather_thunder_white;
                    break;
                case 3:
                    icon = R.drawable.weather_drizzle_white;
                    break;
                case 7:
                    icon = R.drawable.weather_foggy_white;
                    break;
                case 8:
                    icon = R.drawable.weather_cloudy_white;
                    break;
                case 6:
                    icon = R.drawable.weather_snowy_white;
                    break;
                case 5:
                    icon = R.drawable.weather_rainy_white;
                    break;
            }
        }

        return icon;
    }

    /*
     * Méthode qui initialise l'icon gris présent dans le forecast et dans la liste des favoris.
     * */
    public static int setWeatherIcon(int actualId) {

        int id = actualId / 100;
        int icon = R.drawable.weather_sunny_grey;

        if (actualId != 800) {
            switch (id) {
                case 2:
                    icon = R.drawable.weather_thunder_grey;
                    break;
                case 3:
                    icon = R.drawable.weather_drizzle_grey;
                    break;
                case 7:
                    icon = R.drawable.weather_foggy_grey;
                    break;
                case 8:
                    icon = R.drawable.weather_cloudy_grey;
                    break;
                case 6:
                    icon = R.drawable.weather_snowy_grey;
                    break;
                case 5:
                    icon = R.drawable.weather_rainy_grey;
                    break;
            }
        }

        return icon;
    }


    public static String setDay(long ts) {

        String day = "";

        Date date = new Date(ts * 1000);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int numDay = calendar.get(Calendar.DAY_OF_WEEK);

        switch (numDay) {
            case Calendar.SUNDAY:
                day = "dim.";
                break;
            case Calendar.MONDAY:
                day = "lun.";
                break;
            case Calendar.TUESDAY:
                day = "mar.";
                break;
            case Calendar.WEDNESDAY:
                day = "mer.";
                break;
            case Calendar.THURSDAY:
                day = "jeu.";
                break;
            case Calendar.FRIDAY:
                day = "ven.";
                break;
            case Calendar.SATURDAY:
                day = "sam.";
                break;
        }

        return day;
    }

    public static String capitalize(String s) {
        if (s == null) return null;
        if (s.length() == 1) {
            return s.toUpperCase();
        }
        if (s.length() > 1) {
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        }
        return "";
    }

    public static void saveFavoriteCities(Context context, List<City> cities) {
        JSONArray jsonArrayCities = new JSONArray();
        for (int i = 0; i < cities.size(); i++) {
            jsonArrayCities.put(cities.get(i).mStringJson);
        }
        context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .edit()
                .putString(SHARED_FAVORITE_CITIES, jsonArrayCities.toString())
                .apply();
    }

    public static List<City> initFavoriteCities(Context context) {
        List<City> cities = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE)
                    .getString(SHARED_FAVORITE_CITIES, ""));

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObjectCity = new JSONObject(jsonArray.getString(i));
                City city = new City(jsonObjectCity.toString());
                cities.add(city);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return cities;
    }

    public static void saveCitySelected(Context context, float latitude, float longitude) {

        context.getSharedPreferences(SHARED_CITY_SELECTED_LAT, Context.MODE_PRIVATE)
                .edit()
                .putFloat(SHARED_CITY_SELECTED_LAT, latitude)
                .apply();

        context.getSharedPreferences(SHARED_CITY_SELECTED_LON, Context.MODE_PRIVATE)
                .edit()
                .putFloat(SHARED_CITY_SELECTED_LON, longitude)
                .apply();
    }

    public static void getCitySelected(Context context) {
        context.getSharedPreferences(SHARED_CITY_SELECTED_LAT, Context.MODE_PRIVATE)
                .getFloat(SHARED_CITY_SELECTED_LAT, 0f);

        context.getSharedPreferences(SHARED_CITY_SELECTED_LON, Context.MODE_PRIVATE)
                .getFloat(SHARED_CITY_SELECTED_LON, 0f);
    }

}
