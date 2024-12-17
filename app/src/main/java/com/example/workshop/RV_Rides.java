package com.example.workshop;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RV_Rides extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RideAdapter rideAdapter;
    private DatabaseHelper databaseHelper;
    private List<Ride> rideList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rv_rides);

        recyclerView = findViewById(R.id.recyclerViewRides);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        databaseHelper = new DatabaseHelper(this);

        String email = getIntent().getStringExtra("email");
        boolean filterForPassenger = getIntent().getBooleanExtra("filterForPassenger", false);
        boolean history = getIntent().getBooleanExtra("history", false);


        double startLatitude = getIntent().getDoubleExtra("startLatitude", 0);
        double startLongitude = getIntent().getDoubleExtra("startLongitude", 0);


        if (filterForPassenger && !history) {
            rideList = databaseHelper.getAllRides();
            rideList = filterRidesByLocation(rideList, startLatitude, startLongitude, 0.05); // 0.05 degrees (~5km)
        } else if (filterForPassenger && history) {
            rideList = databaseHelper.getAllRidesByPassengerEmail(email);
        } else {
            rideList = databaseHelper.getAllRidesForDriver(email);
        }

        if (rideList == null) {
            rideList = new ArrayList<>();
        }


        rideAdapter = new RideAdapter(rideList, this, databaseHelper, email, history, startLatitude, startLongitude);
        recyclerView.setAdapter(rideAdapter);
    }


    private List<Ride> filterRidesByLocation(List<Ride> rides, double latitude, double longitude, double radius) {
        List<Ride> filteredRides = new ArrayList<>();
        for (Ride ride : rides) {
            double distanceLat = Math.abs(ride.getStartLatitude() - latitude);
            double distanceLon = Math.abs(ride.getStartLongitude() - longitude);
            int ifFinished = ride.getRideFinished();

            if (distanceLat <= radius && distanceLon <= radius && ifFinished == 0) {
                filteredRides.add(ride);
            }
        }
        return filteredRides;
    }
}


