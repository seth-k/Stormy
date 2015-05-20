package io.seth_k.stormy.datasource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.seth_k.stormy.weather.Current;
import io.seth_k.stormy.weather.Day;
import io.seth_k.stormy.weather.Forecast;
import io.seth_k.stormy.weather.Hour;

/**
 * Gets weather forecasts from forecast.io.
 *
 *
 */
public class WeatherFromForecastIO extends WeatherSource {
//    public static final String TAG = WeatherFromForecastIO.class.getSimpleName();

    public WeatherFromForecastIO(WeatherSourceCallback callback) {
        super(callback);
    }

    @Override
    protected String getForecastUrl(double latitude, double longitude) {
        return "https://api.forecast.io/forecast/" + ApiKey.getApiKey() +
                "/" + latitude + "," + longitude +
                "?exclude=minutely&units=auto";
    }

    /**
     * Builds the Forecast from the data retrieved from API.
     * @param forecastData The forecast data as retrieved from the source in String form.
     * @return The complete complete current, hourly and daily forecast.
     * @throws WeatherSourceException
     */
    @Override
    protected Forecast parseForecastDetails(String forecastData) throws WeatherSourceException {
        Forecast forecast = new Forecast();

        try {
            forecast.setCurrent(getCurrentDetails(forecastData));
            forecast.setHourlyForecast(getHourlyForecast(forecastData));
            forecast.setDailyForecast(getDailyForecast(forecastData));
        } catch (JSONException e) {
            throw new WeatherSourceException(e);
        }

        return forecast;
    }

    /**
     * Parses forecast data into a list of daily forecasts in chronological order.
     * @param jsonData The forecast data as retrieved from the source in String form.
     * @return An array of Daily forecasts. The number of days returned depends on the API.
     * @throws JSONException
     */
    protected Day[] getDailyForecast(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");

        Day[] days = new Day[data.length()];
        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonDay = data.getJSONObject(i);
            Day day = new Day();
            day.setSummary(jsonDay.getString("summary"));
            day.setTime(jsonDay.getLong("time"));
            day.setIcon(jsonDay.getString("icon"));
            day.setTemperatureMax(jsonDay.getDouble("temperatureMax"));
            day.setTimezone(timezone);

            days[i] = day;
        }
        return days;
    }

    /**
     * Parses forecast data into a list of hourly forecasts in chronological order.
     * @param jsonData The forecast data as retrieved from the source in String form.
     * @return An array of Hourly forecasts. The number of hours returned depends on the API.
     * @throws JSONException
     */
    protected Hour[] getHourlyForecast(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");

        Hour[] hours = new Hour[data.length()];
        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonHour = data.getJSONObject(i);
            Hour hour = new Hour();
            hour.setSummary(jsonHour.getString("summary"));
            hour.setTime(jsonHour.getLong("time"));
            hour.setIcon(jsonHour.getString("icon"));
            hour.setTemperature(jsonHour.getDouble("temperature"));
            hour.setTimezone(timezone);

            hours[i] = hour;
        }
        return hours;
    }

    /**
     * Parses forecast data for the current weather conditions.
     * @param jsonData The forecast data as retrieved from the source in String form.
     * @return The Current weather conditions.
     * @throws JSONException
     */
    protected Current getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");

        JSONObject currently = forecast.getJSONObject("currently");
        Current current = new Current();
        current.setHumidity(currently.getDouble("humidity"));
        current.setTime(currently.getLong("time"));
        current.setIcon(currently.getString("icon"));
        current.setPrecipChance(currently.getDouble("precipProbability"));
        current.setSummary(currently.getString("summary"));
        current.setTemperature(currently.getDouble("temperature"));
        current.setTimeZone(timezone);

        return current;
    }
}