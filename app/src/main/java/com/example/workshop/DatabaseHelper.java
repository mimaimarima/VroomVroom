package com.example.workshop;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(@Nullable Context context) {
        super(context, "SignLog.db", null, 2);

    }
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @Override
    public void onCreate(SQLiteDatabase MyDatabase) {
        MyDatabase.execSQL("CREATE TABLE users(email TEXT PRIMARY KEY, password TEXT, userType INTEGER, displayName TEXT)");
        MyDatabase.execSQL("CREATE TABLE driver_details(email TEXT UNIQUE, ratingDriving FLOAT, timesDRated INTEGER default 0, licencePlate TEXT, vehicleType TEXT, FOREIGN KEY(email) REFERENCES users(email))");
        MyDatabase.execSQL("CREATE TABLE passenger_details(email TEXT, destination TEXT, rating FLOAT default 0, timesRated INTEGER default 0, FOREIGN KEY(email) REFERENCES users(email))");
        MyDatabase.execSQL("CREATE TABLE rides (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "displayName TEXT, " +
                "startTime TEXT, " +
                "startLatitude DOUBLE," +
                "startLongitude DOUBLE," +
                "endLatitude DOUBLE," +
                "endLongitude DOUBLE," +
                "numberOfPlacesTaken INTEGER DEFAULT 0," +
                "numberOfPlacesAvailable INTEGER, " +
                "passengerList TEXT DEFAULT NULL, " +
                "driverRated REAL DEFAULT 0, " +
                "passengersRated REAL DEFAULT 0, " +
                "price STRING, " +
                "licencePlate TEXT, " +
                "vehicleType TEXT, " +
                "rideFinished INTEGER," +
                "email TEXT, " +
                "FOREIGN KEY(email) REFERENCES driver_details(email)" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase MyDB, int i, int i1) {
        MyDB.execSQL("drop Table if exists users");
        MyDB.execSQL("drop Table if exists driver_details");
        MyDB.execSQL("drop Table if exists passenger_details");
        MyDB.execSQL("drop Table if exists rides");
        onCreate(MyDB);
    }

    public Boolean insertData(String email, String password, int userType, String displayName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("userType", userType);
        contentValues.put("email", email);
        contentValues.put("password", password);
        contentValues.put("displayName", displayName);
        long result = db.insert("users", null, contentValues);
        if (userType == 0) {
            ContentValues passengerValues = new ContentValues();
            passengerValues.put("email", email);
            passengerValues.put("destination", "");  // Set a default destination if needed
            passengerValues.put("rating", 0.0);
            long passengerResult = db.insert("passenger_details", null, passengerValues);

            if (passengerResult == -1) {
                Log.e("DB_INSERT", "Failed to insert passenger details.");
                return false;
            }
        }
        return result != -1;
    }

    public boolean insertDriverDetails(String email, String licencePlate, String vehicleType) {
        if (email == null || email.isEmpty()) {
            Log.e("DatabaseHelper", "Email cannot be null or empty");
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("email", email);
        contentValues.put("ratingDriving", 0);
        contentValues.put("licencePlate", licencePlate);
        contentValues.put("vehicleType", vehicleType);
        long result = db.insertWithOnConflict("driver_details", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);

        return result != -1;
    }
    public Boolean checkEmail(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from users where email = ?", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public Boolean checkEmailPassword(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from users where email = ? and password = ?", new String[]{email, password});
        boolean valid = cursor.getCount() > 0;
        cursor.close();
        return valid;
    }

    public int getUserType(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT userType FROM users WHERE email = ?", new String[]{email});

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int userType = cursor.getInt(cursor.getColumnIndex("userType"));
            cursor.close();
            return userType;
        } else {
            return -1;
        }
    }
    @SuppressLint("Range")
    public String getDisplayNameByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT displayName FROM users WHERE email = ?", new String[]{email});
        String displayName = null;
        if (cursor.moveToFirst()) {
            displayName = cursor.getString(cursor.getColumnIndex("displayName"));
        }
        cursor.close();
        return displayName;
    }
    public boolean insertRide(String email, String startTime, double startLatitude, double startLongitude, double endLatitude, double endLongitude,
                              int numberOfPlacesAvailable,
                               String price, String licencePlate, String vehicleType) {
        String displayName = getDisplayNameByEmail(email);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("displayName", displayName);
        contentValues.put("email", email);
        contentValues.put("startTime", startTime);
        contentValues.put("startLatitude", startLatitude);
        contentValues.put("endLatitude", endLatitude);
        contentValues.put("startLongitude", startLongitude);
        contentValues.put("endLongitude", endLongitude);
        contentValues.put("numberOfPlacesTaken", 0);
        contentValues.put("numberOfPlacesAvailable", numberOfPlacesAvailable);
        contentValues.put("price", price);
        contentValues.put("licencePlate", licencePlate);
        contentValues.put("vehicleType", vehicleType);
        contentValues.put("rideFinished", 0);
        long result = db.insert("rides", null, contentValues);
        return result != -1;
    }

    public List<Ride> getAllRidesForDriver(String email) {
        List<Ride> rides = new ArrayList<>();

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT * FROM rides WHERE email = ?", new String[]{email})) {

            if (cursor != null && cursor.moveToFirst()) {
                do {

                    int idIndex = cursor.getColumnIndex("id");
                    int displayNameIndex = cursor.getColumnIndex("displayName");
                    int startTimeIndex = cursor.getColumnIndex("startTime");
                    int startLatIndex = cursor.getColumnIndex("startLatitude");
                    int startLongIndex = cursor.getColumnIndex("startLongitude");
                    int endLatIndex = cursor.getColumnIndex("endLatitude");
                    int endLongIndex = cursor.getColumnIndex("endLongitude");
                    int priceIndex = cursor.getColumnIndex("price");
                    int rideFinishedIndex = cursor.getColumnIndex("rideFinished");
                    int passengersRatedIndex = cursor.getColumnIndex("passengersRated");
                    int driverRatedIndex = cursor.getColumnIndex("driverRated");

                    if (idIndex == -1 || displayNameIndex == -1 || startTimeIndex == -1 ||
                            startLatIndex == -1 || startLongIndex == -1 || endLatIndex == -1 ||
                            endLongIndex == -1 || priceIndex == -1 || rideFinishedIndex == -1 ||
                            passengersRatedIndex == -1 || driverRatedIndex == -1) {
                        Log.e("DatabaseError", "One or more columns not found in the query result.");
                        continue;
                    }

                    int id = cursor.getInt(idIndex);
                    String displayName = cursor.getString(displayNameIndex);
                    String startTime = cursor.getString(startTimeIndex);
                    double startLatitude = cursor.getDouble(startLatIndex);
                    double startLongitude = cursor.getDouble(startLongIndex);
                    double endLatitude = cursor.getDouble(endLatIndex);
                    double endLongitude = cursor.getDouble(endLongIndex);
                    String price = cursor.getString(priceIndex);
                    int rideFinished = cursor.getInt(rideFinishedIndex);
                    int passengersRated = cursor.getInt(passengersRatedIndex);
                    int driverRated = cursor.getInt(driverRatedIndex);

                    Ride ride = new Ride(id, displayName, startTime, startLatitude, startLongitude,
                            endLatitude, endLongitude, price, rideFinished,driverRated,passengersRated);
                    rides.add(ride);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseError", "Error fetching rides for driver", e);
        }

        return rides;
    }


    public void markRideAsFinished(int rideId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("rideFinished", 1); // Set rideFinished to true (1)
        db.update("rides", contentValues, "id = ?", new String[]{String.valueOf(rideId)});
    }
    public void markPassengersAsRated(int rideId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("passengersRated", 1);
        db.update("rides", values, "id = ?", new String[]{String.valueOf(rideId)});
    }

    public void markDriverAsRated(int rideId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("driverRated", 1);
        db.update("rides", values, "id = ?", new String[]{String.valueOf(rideId)});
    }
    public String getDriverEmailByRideId(int rideId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("rides", new String[]{"email"}, "id = ?",
                new String[]{String.valueOf(rideId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex("email"));
            cursor.close();
            return email;
        } else {
            cursor.close();
            return null;
        }
    }


    @SuppressLint("Range")
        public String getVehicleTypeByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT vehicleType FROM driver_details WHERE email = ?", new String[]{email});
        String vehicleType = null;
        if (cursor.moveToFirst()) {
            vehicleType = cursor.getString(cursor.getColumnIndex("vehicleType"));
        }
        cursor.close();
        return vehicleType;
    }

    @SuppressLint("Range")
    public String getLicencePlateByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT licencePlate FROM driver_details WHERE email = ?", new String[]{email});
        String licencePlate = null;
        if (cursor.moveToFirst()) {
            licencePlate = cursor.getString(cursor.getColumnIndex("licencePlate"));
        }
        cursor.close();
        return licencePlate;
    }

    public List<Ride> getAllRides() {
        List<Ride> rides = new ArrayList<>();

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT * FROM rides", null)) {

            if (cursor != null && cursor.moveToFirst()) {
                do {

                    int idIndex = cursor.getColumnIndex("id");
                    int displayNameIndex = cursor.getColumnIndex("displayName");
                    int startTimeIndex = cursor.getColumnIndex("startTime");
                    int startLatIndex = cursor.getColumnIndex("startLatitude");
                    int startLongIndex = cursor.getColumnIndex("startLongitude");
                    int endLatIndex = cursor.getColumnIndex("endLatitude");
                    int endLongIndex = cursor.getColumnIndex("endLongitude");
                    int priceIndex = cursor.getColumnIndex("price");
                    int rideFinishedIndex = cursor.getColumnIndex("rideFinished");
                    int passengersRatedIndex = cursor.getColumnIndex("passengersRated");
                    int driverRatedIndex = cursor.getColumnIndex("driverRated");

                    if (idIndex == -1 || displayNameIndex == -1 || startTimeIndex == -1 ||
                            startLatIndex == -1 || startLongIndex == -1 || endLatIndex == -1 ||
                            endLongIndex == -1 || priceIndex == -1 || rideFinishedIndex == -1
                    || passengersRatedIndex == -1 || driverRatedIndex == -1) {
                        Log.e("DatabaseError", "One or more columns not found in the query result.");
                        continue;
                    }

                    int id = cursor.getInt(idIndex);
                    String displayName = cursor.getString(displayNameIndex);
                    String startTime = cursor.getString(startTimeIndex);
                    double startLatitude = cursor.getDouble(startLatIndex);
                    double startLongitude = cursor.getDouble(startLongIndex);
                    double endLatitude = cursor.getDouble(endLatIndex);
                    double endLongitude = cursor.getDouble(endLongIndex);
                    String price = cursor.getString(priceIndex);
                    int rideFinished = cursor.getInt(rideFinishedIndex);
                    int passengersRated = cursor.getInt(passengersRatedIndex);
                    int driverRated = cursor.getInt(driverRatedIndex);
                    Ride ride = new Ride(id, displayName, startTime, startLatitude, startLongitude,
                            endLatitude, endLongitude, price, rideFinished,driverRated,passengersRated);
                    rides.add(ride);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseError", "Error fetching all rides", e);
        }

        return rides;
    }
    void addPassengerToRide(Context context, int rideId, String email) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT passengerList, numberOfPlacesTaken, numberOfPlacesAvailable FROM rides WHERE id = ?", new String[]{String.valueOf(rideId)});
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String passengerList = cursor.getString(cursor.getColumnIndex("passengerList"));
            @SuppressLint("Range") int numberOfPlacesTaken = cursor.getInt(cursor.getColumnIndex("numberOfPlacesTaken"));
            @SuppressLint("Range") int numberOfPlacesAvailable = cursor.getInt(cursor.getColumnIndex("numberOfPlacesAvailable"));

            if (passengerList == null || passengerList.isEmpty()) {
                passengerList = email;
            } else {
                if (!passengerList.contains(email)) {
                    passengerList += "," + email;
                }
            }

            if (numberOfPlacesAvailable > numberOfPlacesTaken) {
                numberOfPlacesTaken++;
            } else {
                Toast.makeText(context, "No available places left", Toast.LENGTH_SHORT).show();
                return;
            }
            ContentValues values = new ContentValues();
            values.put("passengerList", passengerList);
            values.put("numberOfPlacesTaken", numberOfPlacesTaken);

            db.update("rides", values, "id = ?", new String[]{String.valueOf(rideId)});
        }

        if (cursor != null) cursor.close();
    }

    public List<Ride> getAllRidesByPassengerEmail(String email) {
        List<Ride> rides = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM rides WHERE passengerList LIKE ?", new String[]{"%" + email + "%"});


        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                @SuppressLint("Range") String displayName = cursor.getString(cursor.getColumnIndex("displayName"));
                @SuppressLint("Range") String startTime = cursor.getString(cursor.getColumnIndex("startTime"));
                @SuppressLint("Range") double startLatitude = cursor.getDouble(cursor.getColumnIndex("startLatitude"));
                @SuppressLint("Range") double startLongitude = cursor.getDouble(cursor.getColumnIndex("startLongitude"));
                @SuppressLint("Range") double endLatitude = cursor.getDouble(cursor.getColumnIndex("endLatitude"));
                @SuppressLint("Range") double endLongitude = cursor.getDouble(cursor.getColumnIndex("endLongitude"));
                @SuppressLint("Range") String price = cursor.getString(cursor.getColumnIndex("price"));
                @SuppressLint("Range") int rideFinished = cursor.getInt(cursor.getColumnIndex("rideFinished"));

                Ride ride = new Ride(id, displayName, startTime, startLatitude, startLongitude, endLatitude, endLongitude, price, rideFinished,0,0);
                rides.add(ride);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return rides;
    }
    public void updateDriverRating(Context context, int rideId, float rating) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT email FROM rides WHERE id = ?", new String[]{String.valueOf(rideId)});
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String driverEmail = cursor.getString(cursor.getColumnIndex("email"));

            Cursor driverCursor = db.rawQuery("SELECT ratingDriving, timesDRated FROM driver_details WHERE email = ?", new String[]{driverEmail});
            if (driverCursor != null && driverCursor.moveToFirst()) {
                @SuppressLint("Range") float currentRating = driverCursor.getFloat(driverCursor.getColumnIndex("ratingDriving"));
                @SuppressLint("Range") int timesRated = driverCursor.getInt(driverCursor.getColumnIndex("timesDRated"));
                float newRating = (timesRated == 0) ? rating : ((currentRating * timesRated) + rating) / (timesRated + 1);
                int newTimesRated = timesRated + 1;


                ContentValues values = new ContentValues();
                values.put("ratingDriving", newRating);
                values.put("timesDRated", newTimesRated);


                db.update("driver_details", values, "email = ?", new String[]{driverEmail});
                Toast.makeText(context, "Driver rating updated", Toast.LENGTH_SHORT).show();
            }
            if (driverCursor != null) {
                driverCursor.close();
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }


    public float getDriverRatingByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT ratingDriving FROM driver_details WHERE email = ?", new String[]{email});

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") float rating = cursor.getFloat(cursor.getColumnIndex("ratingDriving"));
            cursor.close();
            return rating;
        }

        cursor.close();
        return 0;
    }
    public void updatePassengerRating(Context context, String email, float rating) {
        SQLiteDatabase db = this.getWritableDatabase();
        float currentRating = 0;
        int timesRated = 0;
        boolean recordExists = false;

        Log.d("DB_DEBUG", "Email provided: " + email);


        Cursor cursor = db.rawQuery("SELECT rating, timesRated FROM passenger_details WHERE email = ?", new String[]{email});

        if (cursor != null && cursor.moveToFirst()) {
            currentRating = cursor.getFloat(cursor.getColumnIndexOrThrow("rating"));
            timesRated = cursor.getInt(cursor.getColumnIndexOrThrow("timesRated"));
            recordExists = true;
            cursor.close();
        }

        if (recordExists) {

            float newRating = (timesRated == 0) ? rating : ((currentRating * timesRated) + rating) / (timesRated + 1);
            int newTimesRated = timesRated + 1;


            ContentValues values = new ContentValues();
            values.put("rating", newRating);
            values.put("timesRated", newTimesRated);


            int rowsAffected = db.update("passenger_details", values, "email = ?", new String[]{email});

            if (rowsAffected > 0) {
                Log.d("DB_UPDATE", "Rating updated successfully for email: " + email);
                Toast.makeText(context, "Passenger rating updated", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("DB_UPDATE", "Failed to update rating for email: " + email);
            }
        } else {
            Log.e("DB_UPDATE", "Passenger with email " + email + " not found.");
        }

        db.close();
    }
        public float getPassengerRatingByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT rating FROM passenger_details WHERE email = ?", new String[]{email});

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") float rating = cursor.getFloat(cursor.getColumnIndex("rating"));
            cursor.close();
            return rating;
        }
        cursor.close();
        return 0;
    }
}