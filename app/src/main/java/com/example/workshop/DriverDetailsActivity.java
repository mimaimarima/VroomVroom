package com.example.workshop;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.workshop.databinding.ActivityDriverDetailsBinding;
import com.example.workshop.databinding.ActivityPassengerDetailsBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DriverDetailsActivity extends AppCompatActivity {

    ActivityDriverDetailsBinding binding;
    DatabaseHelper databaseHelper;
    private double startLatitude, startLongitude, endLatitude, endLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        databaseHelper = new DatabaseHelper(this);

        String email = getIntent().getStringExtra("email");

        float driverRating = databaseHelper.getDriverRatingByEmail(email);
        binding.ratingText.setText("Rating: " + driverRating);

        binding.openVehicleDetailsDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVehicleDetailsDialog(email);
            }
        });
        binding.addRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String licenceplate = databaseHelper.getLicencePlateByEmail(email);
                String vehicleType = databaseHelper.getVehicleTypeByEmail(email);
                showRideDetailsDialog(email, licenceplate, vehicleType);
            }
        });
        binding.myRidesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = getIntent().getStringExtra("email");
                Intent intent = new Intent(DriverDetailsActivity.this, RV_Rides.class);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });
    }

    private void showVehicleDetailsDialog(String email) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_vehicledetails, null);
        dialogBuilder.setView(dialogView);

        EditText licencePlateEditText = dialogView.findViewById(R.id.dialogLicencePlate);
        EditText vehicleTypeEditText = dialogView.findViewById(R.id.dialogVehicleType);

        dialogBuilder.setTitle("Enter Vehicle Details");
        dialogBuilder.setCancelable(false);
        dialogBuilder.setPositiveButton("Save", (dialog, which) -> {
            String licencePlate = licencePlateEditText.getText().toString();
            String vehicleType = vehicleTypeEditText.getText().toString();

            if (licencePlate.isEmpty() || vehicleType.isEmpty()) {
                Toast.makeText(DriverDetailsActivity.this, "Please enter both vehicle details", Toast.LENGTH_SHORT).show();
            } else {
                // Insert the details into the database
                Boolean insert = databaseHelper.insertDriverDetails(email, licencePlate, vehicleType);
                if (insert) {
                    Toast.makeText(DriverDetailsActivity.this, "Vehicle details saved successfully", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(DriverDetailsActivity.this, "Failed to save vehicle details", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialogBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void showRideDetailsDialog(String email, String licencePlate, String vehicleType) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_addride, null);
        dialogBuilder.setView(dialogView);

        TextView dialogStartTime = dialogView.findViewById(R.id.dialogStartTime);
        EditText dialogNumberOfPlaces = dialogView.findViewById(R.id.dialogNumberOfPlaces);
        EditText dialogRidePrice = dialogView.findViewById(R.id.dialogRidePrice);
        AlertDialog alertDialog = dialogBuilder.create();

        dialogView.findViewById(R.id.selectFromLocationButton).setOnClickListener(v -> openMapForResult("from"));
        dialogView.findViewById(R.id.selectToLocationButton).setOnClickListener(v -> openMapForResult("to"));

        dialogStartTime.setOnClickListener(v -> showDateTimePicker(dialogStartTime));

        dialogView.findViewById(R.id.confirmRideButton).setOnClickListener(v -> {
            String startTime = dialogStartTime.getText().toString();
            String numberOfPlaces = dialogNumberOfPlaces.getText().toString();
            String ridePrice = dialogRidePrice.getText().toString();


            if (startTime.isEmpty() || numberOfPlaces.isEmpty() || ridePrice.isEmpty() ||
                    startLatitude == 0 || startLongitude == 0 || endLatitude == 0 || endLongitude == 0) {
                Toast.makeText(this, "All fields are mandatory, including locations", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save Ride in Database
            boolean insertRide = databaseHelper.insertRide(
                    email, startTime, startLatitude, startLongitude, endLatitude, endLongitude,
                    Integer.parseInt(numberOfPlaces),
                    ridePrice, licencePlate, vehicleType
            );

            if (insertRide) {
                Toast.makeText(this, "Ride added successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to add ride", Toast.LENGTH_SHORT).show();
            }

            alertDialog.dismiss();
        });


        dialogView.findViewById(R.id.cancelRideButton).setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.show();
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

            if (requestCode == 100) {  // Start location result
                startLatitude = lat;
                startLongitude = lon;
                binding.startLocationCoordinates.setText("Start Coordinates: " + startLatitude + ", " + startLongitude);
                Toast.makeText(this, "Start Location Selected", Toast.LENGTH_SHORT).show();
            } else if (requestCode == 200) {  // End location result
                endLatitude = lat;
                endLongitude = lon;
                binding.endLocationCoordinates.setText("End Coordinates: " + endLatitude + ", " + endLongitude);
                Toast.makeText(this, "End Location Selected", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void showDateTimePicker(TextView targetView) {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);


            TimePickerDialog timePicker = new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);


                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                targetView.setText(dateFormat.format(calendar.getTime()));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

            timePicker.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePicker.show();
    }
}




