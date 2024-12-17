package com.example.workshop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder> {
    private final List<Ride> rideList;
    private final Context context;
    private final DatabaseHelper databaseHelper;
    private final String email;
    private final boolean history;
    private double userLatitude;
    private double userLongitude;

    public RideAdapter(List<Ride> rideList, Context context, DatabaseHelper databaseHelper, String email, boolean history, double userLatitude, double userLongitude) {
        this.rideList = rideList;
        this.context = context;
        this.databaseHelper = databaseHelper;
        this.email = email;
        this.history = history;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ride, parent, false);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        Ride ride = rideList.get(position);
        double distance = calculateDistance(userLatitude, userLongitude, ride.getStartLatitude(), ride.getStartLongitude());


        String driverEmail = databaseHelper.getDriverEmailByRideId(ride.getId());
        String driverDisplayName = databaseHelper.getDisplayNameByEmail(driverEmail);
        holder.driverDisplayName.setText("Driver: " + driverDisplayName);

        float rating = databaseHelper.getDriverRatingByEmail(driverEmail);
        holder.driverRating.setText("Rating: " + rating);

        String vehicleType = databaseHelper.getVehicleTypeByEmail(driverEmail);
        holder.vehicleType.setText("Vehicle: " + vehicleType);
        String licensePlate = databaseHelper.getLicencePlateByEmail(driverEmail);
        holder.licensePlate.setText("License Plate: " + licensePlate);

        holder.bind(ride);
        int uT = databaseHelper.getUserType(email);

        if (uT==1)
        {
            holder.rideDistanceText.setVisibility(View.INVISIBLE);

        }
        holder.startLocationCoordinates.setVisibility(View.INVISIBLE);
        holder.endLocationCoordinates.setVisibility(View.INVISIBLE);
        holder.rideDistanceText.setText(String.format("Distance: %.2f km", distance));

        holder.startLocationButton.setOnClickListener(v -> openMap("start", ride.getStartLatitude(), ride.getStartLongitude()));
        holder.endLocationButton.setOnClickListener(v -> openMap("end", ride.getEndLatitude(), ride.getEndLongitude()));

        holder.finishRideButton.setVisibility(View.GONE);
        holder.rateDriverButton.setVisibility(View.GONE);
        holder.ratePassengersButton.setVisibility(View.GONE);

        if (uT == 1) {
            holder.finishRideButton.setVisibility(View.VISIBLE);
            holder.ratePassengersButton.setVisibility(View.VISIBLE);


            holder.finishRideButton.setEnabled(ride.getRideFinished() == 0);
            holder.ratePassengersButton.setEnabled(ride.getPassengersRated() == 0);


            holder.finishRideButton.setOnClickListener(v -> {
                if (ride.getRideFinished() == 0) {
                    databaseHelper.markRideAsFinished(ride.getId());
                    ride.setRideFinished();
                    notifyItemChanged(holder.getAdapterPosition());
                }
            });


            holder.ratePassengersButton.setOnClickListener(v -> {
                if (ride.getPassengersRated() == 0) {
                    showRatePassengersDialog(context, ride.getId());
                    databaseHelper.markPassengersAsRated(ride.getId());
                    ride.setPassengersRated();
                    notifyItemChanged(holder.getAdapterPosition());
                }
            });
        }

        if (uT == 0 && ride.getRideFinished() == 1 && history) {
            holder.rateDriverButton.setVisibility(View.VISIBLE);
            holder.rateDriverButton.setEnabled(ride.getDriverRated() == 0);


            holder.rateDriverButton.setOnClickListener(v -> {
                if (ride.getDriverRated() == 0) {
                    showRatingDriverDialog(context, ride.getId());
                    databaseHelper.markDriverAsRated(ride.getId());
                    ride.setDriverRated();
                    notifyItemChanged(holder.getAdapterPosition());
                }
            });
        }




        if (uT == 0 && !history) {
            holder.itemView.setOnClickListener(v -> showConfirmationDialog(ride.getId(), position));
        }

    }
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Returns distance in km
    }


    private void showRatePassengersDialog(Context context, int rideId) {

        List<String> passengerEmails = getPassengerEmailsForRide(rideId);


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Rate Passengers");


        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, passengerEmails) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {

                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
                }


                String email = getItem(position);


                String displayName = databaseHelper.getDisplayNameByEmail(email);


                TextView textView = convertView.findViewById(android.R.id.text1);
                textView.setText(displayName != null ? displayName : email);

                return convertView;
            }
        };


        builder.setAdapter(adapter, (dialog, which) -> {
            String selectedEmail = passengerEmails.get(which);
            String displayName = databaseHelper.getDisplayNameByEmail(selectedEmail);
            showRatingDialog(context, displayName != null ? displayName : selectedEmail, selectedEmail);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private List<String> getPassengerEmailsForRide(int rideId) {
        List<String> emails = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();


        try (Cursor cursor = db.rawQuery("SELECT passengerList FROM rides WHERE id = ?", new String[]{String.valueOf(rideId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") String passengerList = cursor.getString(cursor.getColumnIndex("passengerList"));
                if (passengerList != null && !passengerList.trim().isEmpty()) {

                    String[] emailsArray = passengerList.split(",");
                    for (String email : emailsArray) {
                        emails.add(email.trim());
                    }
                }
            }
        } catch (Exception e) {
            Log.e("getPassengerEmailsForRide", "Error fetching passenger emails", e);
        }
        return emails;
    }

    private void showRatingDialog(Context context, String passengerDisplayName, String passengerEmail) {
        final EditText ratingInput = new EditText(context);
        ratingInput.setHint("Enter rating (1-5)");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Rate " + passengerDisplayName)
                .setView(ratingInput)
                .setPositiveButton("Rate", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String ratingStr = ratingInput.getText().toString();
                        if (!ratingStr.isEmpty()) {
                            float rating = Float.parseFloat(ratingStr);
                            if (rating >= 1 && rating <= 5) {
                                // Update the passenger's rating in the database
                                databaseHelper.updatePassengerRating(context,passengerEmail, rating);
                                Toast.makeText(context, "Rated " + passengerDisplayName, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Please enter a rating between 1 and 5", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(context, "Rating cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public int getItemCount() {
        return rideList.size();
    }
    private void showConfirmationDialog(int rideId, int position) {
        new AlertDialog.Builder(context)
                .setMessage("Do you want to add yourself to this ride?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {

                    databaseHelper.addPassengerToRide(context, rideId,email);
                    Toast.makeText(context, "You have been added to the ride!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }
    private void showRatingDriverDialog(Context context, int rideId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Rate Driver");

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter rating (0-5)");
        builder.setView(input);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String ratingInput = input.getText().toString();
            try {
                float rating = Float.parseFloat(ratingInput);
                if (rating >= 0 && rating <= 5) {
                    databaseHelper.updateDriverRating(context, rideId, rating);
                } else {
                    Toast.makeText(context, "Rating must be between 0 and 5", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Invalid rating", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
    private void openMap(String type, double latitude, double longitude) {
        String geoUri = String.format("https://www.openstreetmap.org/?mlat=%f&mlon=%f&zoom=18", latitude, longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
        context.startActivity(intent);
    }

    public static class RideViewHolder extends RecyclerView.ViewHolder {
        private final TextView startTime, price;
        public TextView rideDistanceText, driverDisplayName, driverRating, vehicleType, licensePlate, startLocationCoordinates, endLocationCoordinates;
        private final Button finishRideButton, startLocationButton, endLocationButton, ratePassengersButton, rateDriverButton;
        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            startTime = itemView.findViewById(R.id.rideStartTime);
            price = itemView.findViewById(R.id.ridePrice);
            finishRideButton = itemView.findViewById(R.id.finishRideButton);
            startLocationButton = itemView.findViewById(R.id.startLocationButton);
            endLocationButton = itemView.findViewById(R.id.endLocationButton);
            ratePassengersButton = itemView.findViewById(R.id.ratePassengersButton);
            rateDriverButton = itemView.findViewById(R.id.rateDriverButton);
            rideDistanceText = itemView.findViewById(R.id.rideDistanceText);
            rideDistanceText = itemView.findViewById(R.id.rideDistanceText);
            driverDisplayName = itemView.findViewById(R.id.driverDisplayName);
            driverRating = itemView.findViewById(R.id.driverRating);
            vehicleType = itemView.findViewById(R.id.vehicleType);
            licensePlate = itemView.findViewById(R.id.licensePlate);
            startLocationCoordinates = itemView.findViewById(R.id.startLocationCoordinates);
            endLocationCoordinates = itemView.findViewById(R.id.endLocationCoordinates);

        }

        public void bind(Ride ride) {
            startTime.setText(ride.getStartTime());
            price.setText(String.format("$%s", ride.getPrice()));
        }
    }

}




