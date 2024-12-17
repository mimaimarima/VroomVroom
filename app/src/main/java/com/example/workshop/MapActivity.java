package com.example.workshop;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.MapEventsOverlay;

public class MapActivity extends Activity {
    private MapView mapView;
    private GeoPoint selectedPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(new GeoPoint(48.8588443, 2.2943506)); // Default to Eiffel Tower

        Button selectLocationButton = findViewById(R.id.selectLocationButton);


        MapEventsOverlay overlay = new MapEventsOverlay(this, new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                // Add a marker to the selected location
                mapView.getOverlays().clear();
                Marker marker = new Marker(mapView);
                marker.setPosition(p);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapView.getOverlays().add(marker);
                mapView.invalidate();

                selectedPoint = p;
                return true;
            }
            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        });
        mapView.getOverlays().add(overlay);
        selectLocationButton.setOnClickListener(v -> {
            if (selectedPoint != null) {

                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", selectedPoint.getLatitude());
                resultIntent.putExtra("longitude", selectedPoint.getLongitude());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}

