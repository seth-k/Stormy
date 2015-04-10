package io.seth_k.stormy.datasource;

import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;

import java.io.IOException;

import io.seth_k.stormy.weather.Current;
import io.seth_k.stormy.weather.Day;
import io.seth_k.stormy.weather.Forecast;
import io.seth_k.stormy.weather.Hour;

/**
 * Base adapter class for getting weather data from various sources on the net.
 */
public abstract class WeatherSource {
    public static final String TAG = WeatherSource.class.getSimpleName();

    protected final WeatherSourceCallback mCallback;

    public WeatherSource(WeatherSourceCallback callback) {
        mCallback = callback;
    }

    /**
     * Gets forecast data from the Internet in the background.
     * @param latitude  Latitude of location you are requesting the forecast for.
     * @param longitude Longitude of location you are requesting the forecast for.
     */
    public void getForecast(double latitude, double longitude) {
        String forecastUrl = getForecastUrl(latitude, longitude);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(forecastUrl).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                mCallback.onFailure();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    String forecastData = response.body().string();
                    Log.v(TAG, forecastData);
                    if (response.isSuccessful()) {
                        Forecast forecast = parseForecastDetails(forecastData);
                        mCallback.onSuccess(forecast);
                    } else {
                        mCallback.onFailure();
                    }
                } catch (IOException | JSONException e) {
                    Log.e(WeatherFromForecastIO.TAG, "Exception Caught: ", e);
                    mCallback.onFailure();
                }
            }
        });
    }

    /**
     * Builds the Forecast from the data retrieved from API.
     * @param forecastData The forecast data as retrieved from the source in String form.
     * @return The complete complete current, hourly and daily forecast.
     * @throws JSONException
     */
    public Forecast parseForecastDetails(String forecastData) throws JSONException {
        Forecast forecast = new Forecast();

        forecast.setCurrent(getCurrentDetails(forecastData));
        forecast.setHourlyForecast(getHourlyForecast(forecastData));
        forecast.setDailyForecast(getDailyForecast(forecastData));

        return forecast;
    }

    /**
     * Builds a URL appropriate for the API of the site you're using for forecast data.
     * @param latitude Latitude of location you are requesting the forecast for.
     * @param longitude Latitude of location you are requesting the forecast for.
     * @return URL for a request that will return forecast data for the location in JSON format.
     */
    protected abstract String getForecastUrl(double latitude, double longitude);

    /**
     * Parses forecast data into a list of daily forecasts in chronological order.
     * @param forecastData The forecast data as retrieved from the source in String form.
     * @return An array of Daily forecasts. The number of days returned depends on the API.
     * @throws JSONException
     */
    public abstract Day[] getDailyForecast(String forecastData) throws JSONException;


    /**
     * Parses forecast data into a list of hourly forecasts in chronological order.
     * @param forecastData The forecast data as retrieved from the source in String form.
     * @return An array of Hourly forecasts. The number of hours returned depends on the API.
     * @throws JSONException
     */
    public abstract Hour[] getHourlyForecast(String forecastData) throws JSONException;

    /**
     * Parses forecast data for the current weather conditions.
     * @param forecastData The forecast data as retrieved from the source in String form.
     * @return The Current weather conditions.
     * @throws JSONException
     */
    public abstract Current getCurrentDetails(String forecastData) throws JSONException;
}
