package io.seth_k.stormy.datasource;

/**
 * Created by Seth on 5/15/2015.
 */
public class WeatherSourceException extends Exception {
    public WeatherSourceException() {
    }

    public WeatherSourceException(String detailMessage) {
        super(detailMessage);
    }

    public WeatherSourceException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public WeatherSourceException(Throwable throwable) {
        super(throwable);
    }
}
