package io.seth_k.stormy.datasource;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.seth_k.stormy.weather.Current;
import io.seth_k.stormy.weather.Day;
import io.seth_k.stormy.weather.Hour;

/**
 * Gets weather forecasts from forecast.io.
 *
 *
 */
public class WeatherFromForecastIO extends WeatherSource {
    public static final String TAG = WeatherFromForecastIO.class.getSimpleName();

    public WeatherFromForecastIO(WeatherSourceCallback callback) {
        super(callback);
    }

    private String getApiKey() {
        return ApiKey.getApiKey();
    }

    @Override
    protected String getForecastUrl(double latitude, double longitude) {
        return "https://api.forecast.io/forecast/" + ApiKey.getApiKey() +
                "/" + latitude + "," + longitude;
    }

    @Override
    public Day[] getDailyForecast(String jsonData) throws JSONException {
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

    @Override
    public Hour[] getHourlyForecast(String jsonData) throws JSONException {
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

    @Override
    public Current getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG, "From JSON: " + timezone);

        JSONObject currently = forecast.getJSONObject("currently");
        Current current = new Current();
        current.setHumidity(currently.getDouble("humidity"));
        current.setTime(currently.getLong("time"));
        current.setIcon(currently.getString("icon"));
        current.setPrecipChance(currently.getDouble("precipProbability"));
        current.setSummary(currently.getString("summary"));
        current.setTemperature(currently.getDouble("temperature"));
        current.setTimeZone(timezone);

        Log.d(TAG, current.getFormatedTime());

        return current;
    }
}