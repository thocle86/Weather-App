package com.thocle.android.weatherapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.thocle.android.weatherapp.R;
import com.thocle.android.weatherapp.databinding.ActivityMainBinding;
import com.thocle.android.weatherapp.models.City;
import com.thocle.android.weatherapp.utils.Util;

import org.json.JSONException;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Context mContext;
    private OkHttpClient mOkHttpClient;
    private Handler mHandler;
    private City mCurrentCity;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private static final int REQUEST_CODE = 1;
    private String mCurrentLatitude;
    private String mCurrentLongitude;
    private ActivityMainBinding binding;
    private static final String SHARED_PREFERENCES = "SHARED_PREFERENCES";
    private static final String SHARED_PREFERENCES_STRING_1 = "SHARED_PREFERENCES_STRING_1";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mContext = this;
        mOkHttpClient = new OkHttpClient();
        mHandler = new Handler();

        // initialisation de la météo courante avec l'apel API et les permissions
        checkPermissions();
        updateCurrentWeather();

        // initialisation du bouton pour accèder aux favoris
        binding.mainButtonFavoriteCities.setOnClickListener(view -> {
            Intent intent = new Intent(this, FavoriteActivity.class);
            startActivity(intent);
        });

//        SharedPreferences sh = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
//        SharedPreferences.Editor editor = sh.edit();
//        editor.putString(SHARED_PREFERENCES_STRING_1, "Ma string numéro 1");
//        editor.apply();

        SharedPreferences sh = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);

        String string1 = sh.getString(SHARED_PREFERENCES_STRING_1, null);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initLocationListener() {
        Log.d("TAG", "initLocationListener");
        mLocationListener = location -> {
            mCurrentLatitude = String.valueOf(location.getLatitude());
            mCurrentLongitude = String.valueOf(location.getLongitude());
            updateCurrentWeather();
            mLocationManager.removeUpdates(mLocationListener);
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d("TAG", "onRequestPermissionsResult");
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("TAG", "onRequestPermissionsResult => REQUEST_CODE = 0");
                checkPermissions();
            } else {
                Log.d("TAG", "onRequestPermissionsResult => Accès au GPS refusé");
                Toast.makeText(MainActivity.this, "Accès au GPS refusé", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("TAG", "onRequestPermissionsResult => requestCode != REQUEST_CODE");
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissions() {
        Log.d("TAG", "checkPermissions");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TAG", "checkPermissions => permission refusée");
            // Ouverture de la fenêtre de dialogue pour activer la géolocalisation
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
        } else {
            Log.d("TAG", "checkPermissions => permission aceptée");
            initLocationListener();
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        }
    }

    public void renderCurrentWeather(String jsonResponse) {
        try {
            mCurrentCity = new City(jsonResponse);
            binding.mainTextViewCurrentCityName.setText(mCurrentCity.mName);
            binding.mainTextViewCurrentCityDescription.setText(mCurrentCity.mDescription);
            binding.mainImageViewCurrentCityIcon.setImageResource(mCurrentCity.mWeatherResIconWhite);
            binding.mainTextViewCurrentCityTemperature.setText(mCurrentCity.mTemperature);
        } catch (JSONException e) {
            Log.d("TAG", e.getMessage());
        }
    }

    public void updateCurrentWeather() {
        Log.d("TAG", "updateCurrentWeather");
        String baseUrl = "https://api.openweathermap.org/data/2.5/weather";
        String metric = "units=metric";
        String lang = "lang=fr";
        String apiKey = "appid=ceeb6b13b0e3d8fe341af1908c7c92f9";
        String url = String.format("%s?lat=%s&lon=%s&%s&%s&%s", baseUrl, mCurrentLatitude, mCurrentLongitude, metric, lang, apiKey);
//        Log.d("TAG", url);
        Request request = new Request.Builder().url(url).build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("TAG", "ERROR !!!");
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String stringJson = Objects.requireNonNull(response.body()).string();
                    mHandler.post(() -> {
                        renderCurrentWeather(stringJson);
                    });
                } else {
                    Log.d("TAG", "NO DATA !!!");
                }
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onResume() {
        super.onResume();
        showViewBasedOnInternetConnection(binding.mainLinearLayoutInternetDisabled, binding.mainRelativeLayoutInternetEnabled);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void showViewBasedOnInternetConnection(View pViewIsConnected, View pViewIsNotConnected) {
        if (Util.isNetworkConnected(mContext)) {
            pViewIsConnected.setVisibility(View.INVISIBLE);
            pViewIsNotConnected.setVisibility(View.VISIBLE);
        } else {
            pViewIsConnected.setVisibility(View.VISIBLE);
            pViewIsNotConnected.setVisibility(View.INVISIBLE);

            openSettingsToEnableInternetConnection();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void openSettingsToEnableInternetConnection() {
        binding.mainTextViewEnabledInternet.setOnClickListener(view -> {
            Intent intent = new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
            startActivity(intent);
        });
    }

//    @RequiresApi(api = Build.VERSION_CODES.M)
//    private boolean isNetworkConnected() {
//        ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);
//        Network currentNetwork = connectivityManager.getActiveNetwork();
//        NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(currentNetwork);
//        return currentNetwork != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
//    }

}