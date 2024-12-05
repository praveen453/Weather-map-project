package com.example.weathermap;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private Spinner cityDropdown;
    private TextView weatherInfo;
    private ImageView weatherImage;

    private String selectedCity = "";
    private final String API_KEY = "c06353f97ed6135d2074d67e5ac62585";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Status bar styling
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.parseColor("#001e33"));
        }

        // Initialize Views
        cityDropdown = findViewById(R.id.cityDropdown);
        weatherInfo = findViewById(R.id.weatherInfo);
        weatherImage = findViewById(R.id.weatherImage);

        // City dropdown setup
        String[] cities = {"New York", "London", "Tokyo", "Colombo", "Dubai"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cityDropdown.setAdapter(adapter);

        cityDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCity = cities[position];
                fetchWeatherData(selectedCity);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCity = "";
            }
        });

        // Apply animations
        applyAnimations();
    }

    private void applyAnimations() {
        // Load animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);

        // Apply fade-in animation to TextView and ImageView
        weatherInfo.startAnimation(fadeIn);
        weatherImage.startAnimation(fadeIn);

        // Apply slide-down animation to Spinner
        cityDropdown.startAnimation(slideDown);
    }

    private void fetchWeatherData(String city) {
        new Thread(() -> {
            try {
                String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_KEY + "&units=metric";
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                String temperature = jsonResponse.getJSONObject("main").getString("temp") + "°C";
                String description = jsonResponse.getJSONArray("weather").getJSONObject(0).getString("description");
                String weatherCondition = description.toLowerCase();
                String windSpeed = jsonResponse.getJSONObject("wind").getString("speed") + " m/s";

                int imageResource = getImageResourceForWeather(weatherCondition);

                String weatherDetails = "City: " + city + "\n" +
                        "Temperature: " + temperature + "\n" +
                        "Description: " + description + "\n" +
                        "Wind Speed: " + windSpeed;

                runOnUiThread(() -> {
                    weatherInfo.setText(weatherDetails);
                    weatherInfo.setVisibility(View.VISIBLE);

                    if (imageResource != 0) {
                        weatherImage.setImageResource(imageResource);
                        weatherImage.setVisibility(View.VISIBLE);
                    } else {
                        weatherImage.setVisibility(View.GONE);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> weatherInfo.setText("Error fetching weather data!"));
            }
        }).start();
    }

    private int getImageResourceForWeather(String weatherCondition) {
        if (weatherCondition.contains("clear")) {
            return R.drawable.clear;
        } else if (weatherCondition.contains("rain")) {
            return R.drawable.rain;
        } else if (weatherCondition.contains("cloud")) {
            return R.drawable.overcast_cloudy;
        } else if (weatherCondition.contains("snow")) {
            return R.drawable.snowy;
        } else if (weatherCondition.contains("thunderstorm")) {
            return R.drawable.thunderstorm;
        } else {
            return R.drawable.default_weather;
        }
    }
}
