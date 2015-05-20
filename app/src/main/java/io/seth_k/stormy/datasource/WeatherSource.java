package io.seth_k.stormy.datasource;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import io.seth_k.stormy.weather.Forecast;

/**
 * Base class for getting weather data from various sources on the net.
 * Provides a default implementation that gets data from a REST API in the background.
 * Subclasses need to provide the URL to call for a specific API and functionality to parse
 * the response from the API into a Forecast object, which is returned through
 * the onSuccess() callback.
 */
public abstract class WeatherSource {
    //public static final String TAG = WeatherSource.class.getSimpleName();

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
                mCallback.onFailure(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    String forecastData = response.body().string();
                    if (response.isSuccessful()) {
                        Forecast forecast = parseForecastDetails(forecastData);
                        mCallback.onSuccess(forecast);
                    } else {
                        mCallback.onFailure(new WeatherSourceException("Request from forecast service was not successful."));
                    }
                } catch (IOException | WeatherSourceException e) {
                    mCallback.onFailure(e);
                }
            }
        });
    }

    /**
     * Builds the Forecast from the data retrieved from API.
     * @param forecastData The forecast data as retrieved from the source in String form.
     * @return The complete complete current, hourly and daily forecast.
     * @throws WeatherSourceException
     */
    protected abstract Forecast parseForecastDetails(String forecastData) throws WeatherSourceException;

    /**
     * Builds a URL appropriate for the API of the site you're using for forecast data.
     * @param latitude Latitude of location you are requesting the forecast for.
     * @param longitude Latitude of location you are requesting the forecast for.
     * @return URL for a request that will return forecast data for the location in JSON format.
     */
    protected abstract String getForecastUrl(double latitude, double longitude);

}
