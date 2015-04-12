package io.seth_k.stormy.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesWithFallbackProvider;
import io.nlopez.smartlocation.location.providers.LocationManagerProvider;
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
    public static final String LOCATION_NAME = "LOCATION_NAME";
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
    private double mLatitude = 45.5132;
    private double mLongitude = -122.6711;
    private String mLocationName = "";


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        mProgressBar.setVisibility(View.INVISIBLE);

//        refreshForecast(mRefreshImageView); // Load the forecast for the first time.
        Log.d(TAG, "Main UI Thread is running");
    }

    @Override
    protected void onResume() {
        super.onResume();

        //start location service
        Log.d(TAG, "Starting location service....");
        SmartLocation
                .with(this)
                .location()
                .provider(new LocationGooglePlayServicesWithFallbackProvider(this))
                .config(LocationParams.LAZY)
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {
                        mLatitude = location.getLatitude();
                        mLongitude = location.getLongitude();
                        mLocationName = getLocationName(mLatitude, mLongitude);
                        Log.d(TAG, "New Location: " +
                                mLocationName +
                                " Lat: " + mLatitude +
                                " Long: " + mLongitude);
                        // Load forecast only if it hasn't been loaded before (ie. showing
                        // placeholder text); otherwise wait for refresh button.
                        if (mTemperatureLabel.getText().toString().equals(getString(R.string.temperature_loading))) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    refreshForecast(mRefreshImageView);
                                }
                            });
                        }
                    }
                });

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Stopping location service....");
        SmartLocation.with(this).location().stop();
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
        mHumidityValue.setText(current.getHumidity() + "%");
        mPrecipValue.setText(current.getPrecipChance() + "%");
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
        mLocationLabel.setText(mLocationName);

        if (isNetworkAvailable()) {
            toggleRefresh();
            mWeatherSource.getForecast(mLatitude, mLongitude);
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

    /**
     * Get the name of the city at the given map coordinates.
     *
     * @param latitude Latitude of the location.
     * @param longitude Longitude of the location.
     * @return The localized name of the city.  If a geocoder isn't implemented on the device,
     * returns "Not Available". If the geocoder is implemented but fails to get an address,
     * returns "Not Found".
     */
    public String getLocationName(double latitude, double longitude) {

        String cityName = "Not Found";
        if (Geocoder.isPresent())
        {
            Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                if (addresses.size() > 0) {
                    Address address = addresses.get(0);
                    cityName = address.getLocality(); // + ", " + address.getAdminArea();
                    Log.d(TAG, "City: " + cityName);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            cityName = "Not Available";
        }
        return cityName;
    }

    @OnClick(R.id.dailyButton)
    public void startDailyActivity(View view) {
        Intent intent = new Intent(this, DailyForecastActivity.class);
        intent.putExtra(DAILY_FORECAST, mForecast.getDailyForecast());
        intent.putExtra(LOCATION_NAME, mLocationName);
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
