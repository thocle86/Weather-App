package com.thocle.android.weatherapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thocle.android.weatherapp.R;
import com.thocle.android.weatherapp.models.City;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    private Context mContext;
    private List<City> mCityList ;

    public FavoriteAdapter(Context pContext, List<City> pCityList) {
        mContext = pContext;
        mCityList = pCityList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_favorite_city, parent, false );
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        City city = mCityList.get(position);
        holder.mTextViewName.setText(city.mName);
        holder.mTextViewDescription.setText(city.mDescription);
        holder.mImageViewWeatherIcon.setImageResource(city.mWeatherResIconWhite);
        holder.mTextViewTemperature.setText(city.mTemperature);
        holder.mCity = city;
    }

    @Override
    public int getItemCount() {
        return mCityList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        public TextView mTextViewName;
        public TextView mTextViewDescription;
        public ImageView mImageViewWeatherIcon;
        public TextView mTextViewTemperature;
        public City mCity;

        public ViewHolder(View view) {
            super (view);
            mTextViewName = (TextView) view.findViewById(R.id.text_view_city_name);
            mTextViewDescription = (TextView) view.findViewById(R.id.text_view_city_description);
            mImageViewWeatherIcon = (ImageView) view.findViewById(R.id.image_view_city_weather_icon);
            mTextViewTemperature = (TextView) view.findViewById(R.id.text_view_city_temperature);
            view.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Supprimer une ville");
            builder.setMessage("Voulez-vous supprimer cette ville ?");
            builder.setPositiveButton(android.R.string.ok,
                    (dialog, id) -> {
                        mCityList.remove(getAbsoluteAdapterPosition());
                        notifyDataSetChanged();
                    });
            builder.setNegativeButton(android.R.string.cancel,
                    (dialog, id) -> {

                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
        }
    }

}
