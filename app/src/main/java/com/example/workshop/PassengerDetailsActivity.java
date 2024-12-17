package com.example.workshop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.workshop.databinding.ActivityPassengerDetailsBinding;

public class PassengerDetailsActivity extends AppCompatActivity {

    ActivityPassengerDetailsBinding binding;
    DatabaseHelper databaseHelper;
    private double startLatitude, startLongitude, endLatitude, endLongitude;
    float passengerRating;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPassengerDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        String email = getIntent().getStringExtra("email");

        passengerRating = databaseHelper.getPassengerRatingByEmail(email);
        binding.ratingText.setText("Rating: " + passengerRating);

        binding.enterStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMapForResult("from");
            }
        });

        binding.enterEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMapForResult("to");
            }
        });
        binding.searchRides.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = getIntent().getStringExtra("email");
                Intent intent = new Intent(PassengerDetailsActivity.this, RV_Rides.class);
                intent.putExtra("filterForPassenger", true);
                intent.putExtra("startLatitude", startLatitude);
                intent.putExtra("startLongitude", startLongitude);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });

        binding.myRidesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = getIntent().getStringExtra("email");
                Intent intent = new Intent(PassengerDetailsActivity.this, RV_Rides.class);
                intent.putExtra("email", email);
                intent.putExtra("filterForPassenger", true);
                intent.putExtra("history", true);
                startActivity(intent);
            }
        });
    }

    private void openMapForResult(String type) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("locationType", type);
        if (type.equals("from")) {
            startActivityForResult(intent, 100);
        } else if (type.equals("to")) {
            startActivityForResult(intent, 200);
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            double lat = data.getDoubleExtra("latitude", 0);
            double lon = data.getDoubleExtra("longitude", 0);

            if (requestCode == 100) {
                startLatitude = lat;
                startLongitude = lon;
                binding.startLocationCoordinates.setText("Start Coordinates: " + startLatitude + ", " + startLongitude);
                Toast.makeText(this, "Start Location Selected", Toast.LENGTH_SHORT).show();
            } else if (requestCode == 200) {
                endLatitude = lat;
                endLongitude = lon;
                binding.endLocationCoordinates.setText("End Coordinates: " + endLatitude + ", " + endLongitude);
                Toast.makeText(this, "End Location Selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
