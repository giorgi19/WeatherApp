package com.example.weatherapp.Retrofit;

import com.example.weatherapp.Model.WeatherForecastResult;
import com.example.weatherapp.Model.WeatherResult;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IOopenWeatherMap {
    @GET("weather")
    Observable<WeatherResult> getWeatherByCityName(@Query("q")String cityName,
                                                 @Query("appid") String appdis,
                                                 @Query("units") String unit
                                                 );
    @GET("weather")
    Observable<WeatherResult> getWeatherBylatLng(@Query("lat")String lat,
                                                 @Query("lon") String lng,
                                                 @Query("appid") String appdis,
                                                 @Query("units") String unit
    );
    @GET("forecast")
    Observable<WeatherForecastResult> getForecastWeatherBylatLng(@Query("lat")String lat,
                                                                 @Query("lon") String lng,
                                                                 @Query("appid") String appdis,
                                                                 @Query("units") String unit
    );
}
