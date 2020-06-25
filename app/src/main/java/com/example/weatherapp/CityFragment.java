package com.example.weatherapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weatherapp.Common.Common;
import com.example.weatherapp.Model.WeatherResult;
import com.example.weatherapp.Retrofit.IOopenWeatherMap;
import com.example.weatherapp.Retrofit.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.label305.asynctask.SimpleAsyncTask;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class CityFragment extends Fragment {
   public List<String> suggest = new ArrayList<>();
private List<String> lstcities;
private MaterialSearchBar searchBar;
    ImageView img_weather;
    TextView txt_city_name,txt_humidity,txt_pressure,txt_temperature,txt_description,txt_date_time,txt_wind,txt_geo_coord;
    LinearLayout weather_panel;
    ProgressBar loading;
    CompositeDisposable compositeDisposable;
    IOopenWeatherMap mService;
    static CityFragment instance;

    public static CityFragment getInstance() {
        if(instance==null)
            instance=new CityFragment();
        return instance;
    }


    public CityFragment() {

        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IOopenWeatherMap.class);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemview= inflater.inflate(R.layout.fragment_city, container, false);

        img_weather = (ImageView)itemview.findViewById(R.id.img_weather);
        txt_city_name = (TextView)itemview.findViewById(R.id.txt_city_name);
        txt_humidity = (TextView)itemview.findViewById(R.id.txt_humidity);
        txt_pressure = (TextView)itemview.findViewById(R.id.txt_pressure);
        txt_temperature = (TextView)itemview.findViewById(R.id.txt_temperature);
        txt_description = (TextView)itemview.findViewById(R.id.text_description);
        txt_date_time = (TextView)itemview.findViewById(R.id.txt_date_time);
        txt_wind = (TextView)itemview.findViewById(R.id.txt_wind);
        txt_geo_coord = (TextView)itemview.findViewById(R.id.txt_geo_coord);

        weather_panel =(LinearLayout)itemview.findViewById(R.id.weather_panel);
        loading = (ProgressBar)itemview.findViewById(R.id.loading);

searchBar = (MaterialSearchBar)itemview.findViewById(R.id.search_bar);
        searchBar.setEnabled(false);

        new Loadcities().execute();

        return itemview;
    }

    private class Loadcities extends SimpleAsyncTask<List<String>> {


        @Override
        protected List<String> doInBackgroundSimple() {
            lstcities = new ArrayList<>();
try{

    StringBuilder builder = new StringBuilder();
    InputStream is = getResources().openRawResource(R.raw.city);
    GZIPInputStream gzipInputStream = new GZIPInputStream(is);
    InputStreamReader reader = new InputStreamReader(gzipInputStream);
    BufferedReader in = new BufferedReader(reader);
    String readed;
    while((readed = in.readLine()) != null)
         builder.append(readed);
    lstcities = new Gson().fromJson(builder.toString(),new TypeToken<List<String>>(){}.getType());

} catch (IOException e) {
    e.printStackTrace();
}

            return lstcities;
        }

        @Override
        protected void onSuccess(final List<String> listcity) {
            super.onSuccess(listcity);

            searchBar.setEnabled(true);
            searchBar.addTextChangeListener(new TextWatcher() {
              @Override
              public void beforeTextChanged(CharSequence s, int start, int count, int after) {

              }

              @Override
              public void onTextChanged(CharSequence s, int start, int before, int count) {
//List<String> suggest = new ArrayList<>();
for(String search : listcity)
{
    if(search.toLowerCase().contains(searchBar.getText().toLowerCase()))
        suggest.add(search);
}
searchBar.setLastSuggestions(suggest);
              }

              @Override
              public void afterTextChanged(Editable s) {

              }
          });
            searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                @Override
                public void onSearchStateChanged(boolean enabled) {

                }

                @Override
                public void onSearchConfirmed(CharSequence text) {
                    getWeatherInformation(text.toString());
searchBar.setLastSuggestions(listcity);

                }

                @Override
                public void onButtonClicked(int buttonCode) {

                }
            });

             searchBar.setLastSuggestions(listcity);
           loading.setVisibility(View.GONE);


        }




    }

    private void getWeatherInformation(String cityName) {
        compositeDisposable.add(mService.getWeatherByCityName(cityName,
                Common.APP_ID,
                "metric")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<WeatherResult>() {
                            @Override
                            public void accept(WeatherResult weatherResult) throws Exception {

//load image
                                Picasso.get().load(new StringBuilder("https://openweathermap.org/img/w")
                                        .append(weatherResult.getWeather().get(0).getIcon())
                                        .append(".png").toString()).into(img_weather);

                                //ინფორმაციის ჩატვირთვა
                                txt_city_name.setText(weatherResult.getName());
                                txt_description.setText(new StringBuilder("ქალაქი: ").append(weatherResult.getName()).toString());
                                txt_temperature.setText(new StringBuilder(
                                        String.valueOf(weatherResult.getMain().getTemp())).append("°C").toString());
                                txt_date_time.setText(Common.convertUnixTodate(weatherResult.getDt()));
                                txt_pressure.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getPressure())).append(" hpa").toString());
                                txt_humidity.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getHumidity())).append(" %").toString());

                                txt_geo_coord.setText(new StringBuilder(" ").append(weatherResult.getCoord().toString()).append(" ").toString());

//გამოტანა
                                weather_panel.setVisibility(View.VISIBLE);
                                loading.setVisibility(View.GONE);


                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Toast.makeText(getActivity(),""+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        })


        );





    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

}
