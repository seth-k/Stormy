package io.seth_k.stormy.datasource;

import io.seth_k.stormy.weather.Forecast;

/**
 * Provides callback methods for WeatherSource.getForecast() to call after attempting to
 * retrieve weather data in the background.
 */
public interface WeatherSourceCallback {
    /**
     * Called when the forecast is successfully retrieved and parsed.
     * @param forecast The forecast that was retrieved by the WeatherSource in the background.
     */
    void onSuccess(Forecast forecast);

    /**
     *  Called if we weren't able to retrieve the weather forecast for whatever reason.
     */
    void onFailure(Exception e);
}
