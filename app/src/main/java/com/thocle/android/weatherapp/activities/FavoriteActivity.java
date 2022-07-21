package com.thocle.android.weatherapp.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.thocle.android.weatherapp.R;
import com.thocle.android.weatherapp.adapters.FavoriteAdapter;
import com.thocle.android.weatherapp.databinding.ActivityFavoriteBinding;
import com.thocle.android.weatherapp.models.City;
import com.thocle.android.weatherapp.utils.Util;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.platform.android.UtilKt;

public class FavoriteActivity extends AppCompatActivity {

    private ActivityFavoriteBinding binding;
    private List<City> mCityList;
    private RecyclerView mRecyclerViewCityList;
    private RecyclerView.Adapter mAdapter;
    private OkHttpClient mOkHttpClient;
    private Handler mHandler;
    private City mFavoriteCity;
    private City mCityRemoved;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        binding = ActivityFavoriteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
        toolBarLayout.setTitle(getTitle());

        FloatingActionButton fab = binding.fab;
        fab.setOnClickListener(view -> addCity());

        mCityList = new ArrayList<>();
        mCityList = Util.initFavoriteCities(mContext);

        mRecyclerViewCityList = findViewById(R.id.recycler_view_city_list);
        mRecyclerViewCityList.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new FavoriteAdapter(this, mCityList);
        mRecyclerViewCityList.setAdapter(mAdapter);

        mOkHttpClient = new OkHttpClient();
        mHandler = new Handler();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAbsoluteAdapterPosition();
                mCityRemoved = mCityList.remove(position);
                Util.saveFavoriteCities(mContext, mCityList);
                mAdapter.notifyDataSetChanged();

                Snackbar.make(findViewById(R.id.favorite_coordinator_layout), mCityRemoved.mName + " est supprimé", Snackbar.LENGTH_LONG )
                        .setAction( "Annuler" , v -> {
                            mCityList.add(position, mCityRemoved);
                            Util.saveFavoriteCities(mContext, mCityList);
                            mAdapter.notifyDataSetChanged();
                        })
                        .show();
            }
        });

        itemTouchHelper.attachToRecyclerView(mRecyclerViewCityList);

    }

    public void callApi(String pCityName) {
        String baseUrl = "https://api.openweathermap.org/data/2.5/weather?";
        String cityName = pCityName.trim().toLowerCase();
        String metric = "units=metric";
        String lang = "lang=fr";
        String apiKey = "appid=ceeb6b13b0e3d8fe341af1908c7c92f9";
        String url = String.format("%sq=%s&%s&%s&%s", baseUrl, cityName, metric, lang, apiKey);
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
                    Log.d("TAG", stringJson);
                    mHandler.post(() -> {
                        try {
                            mFavoriteCity = new City(stringJson);
                            mCityList.add(mFavoriteCity);
                            Util.saveFavoriteCities(mContext, mCityList);
                            mAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    Log.d("TAG", "NO DATA !!!");
                }
            }
        });
    }

    public void addCity() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_favorite, null);
        final LinearLayout linearLayoutAddCity = view.findViewById(R.id.favorite_linear_layout_add_city);
        final EditText editTextAddCity = view.findViewById(R.id.favorite_edit_text_add_city_with_name);
        final TextView textViewAddCity = view.findViewById(R.id.favorite_button_add_city_with_map);

        textViewAddCity.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        });

        builder.setView(view);

        builder.setTitle("Ajouter une ville");
        builder.setMessage("Saisissez le nom d'une ville ou sélectionner sur la carte !");
        builder.setPositiveButton(android.R.string.ok,
                (dialog, id) -> {
                    callApi(editTextAddCity.getText().toString());
                });
        builder.setNegativeButton(android.R.string.cancel,
                (dialog, id) -> {

                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
