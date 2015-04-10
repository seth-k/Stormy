package io.seth_k.stormy.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.seth_k.stormy.R;
import io.seth_k.stormy.datasource.WeatherFromForecastIO;
import io.seth_k.stormy.datasource.WeatherSource;
import io.seth_k.stormy.datasource.WeatherSourceCallback;
import io.seth_k.stormy.weather.Current;
import io.seth_k.stormy.weather.Forecast;


public class MainActivity extends ActionBarActivity  implements WeatherSourceCallback {
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String DAILY_FORECAST = "DAILY_FORECAST";
    public static final String HOURLY_FORECAST = "HOURLY_FORECAST";
    private final WeatherSource mWeatherSource = new WeatherFromForecastIO(this);

    private Forecast mForecast;
    @InjectView(R.id.locationLabel) TextView mLocationLabel;
    @InjectView(R.id.timeLabel) TextView mTimeLabel;
    @InjectView(R.id.temperatureLabel) TextView mTemperatureLabel;
    @InjectView(R.id.humidityValue) TextView mHumidityValue;
    @InjectView(R.id.precipValue) TextView mPrecipValue;
    @InjectView(R.id.summaryLabel) TextView mSummaryLabel;
    @InjectView(R.id.iconImageView) ImageView mIconImageView;
    @InjectView(R.id.refreshImageView) ImageView mRefreshImageView;
    @InjectView(R.id.progressBar) ProgressBar mProgressBar;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        mProgressBar.setVisibility(View.INVISIBLE);

        refreshForecast(mRefreshImageView); // Load the forecast for the first time.
        Log.d(TAG, "Main UI Thread is running");
    }

    public void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    public void updateDisplay() {
        Current current = mForecast.getCurrent();
        mTemperatureLabel.setText(current.getTemperature() + "");
        mTimeLabel.setText("At " + current.getFormatedTime() + " it will be");
        mHumidityValue.setText(current.getHumidity()+"");
        mPrecipValue.setText(current.getPrecipChance()+"%");
        mSummaryLabel.setText(current.getSummary());
        Drawable drawable = getResources().getDrawable((int) current.getIconId());
        mIconImageView.setImageDrawable(drawable);
    }

    public void alertUserAboutError(){
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

    @OnClick(R.id.refreshImageView)
    public void refreshForecast(View v) {
        double latitude = 45.5132;
        double longitude = -122.6711;

        if (isNetworkAvailable()) {
            toggleRefresh();
            mWeatherSource.getForecast(latitude, longitude);
        } else {
            Toast.makeText(this, getString(R.string.network_unavailable), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSuccess(Forecast forecast) {
        setForecast(forecast);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toggleRefresh();
                updateDisplay();
            }
        });
    }

    @Override
    public void onFailure() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toggleRefresh();
                alertUserAboutError();
            }
        });
    }

    @OnClick(R.id.dailyButton)
    public void startDailyActivity(View view) {
        Intent intent = new Intent(this, DailyForecastActivity.class);
        intent.putExtra(DAILY_FORECAST, mForecast.getDailyForecast());
        startActivity(intent);
    }

    @OnClick(R.id.hourlyButton)
    public void startHourlyActivity(View view){
        Intent intent = new Intent(this, HourlyForecastActivity.class);
        intent.putExtra(HOURLY_FORECAST, mForecast.getHourlyForecast());
        startActivity(intent);
    }

    public Forecast getForecast() {
        return mForecast;
    }

    public void setForecast(Forecast forecast) {
        mForecast = forecast;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }
}
