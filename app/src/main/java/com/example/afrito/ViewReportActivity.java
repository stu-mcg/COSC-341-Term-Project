package com.example.afrito;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager;
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions;

public class ViewReportActivity extends AppCompatActivity implements
        OnMapReadyCallback {

    private MapView mapView;
    private CircleManager circleManager;
    private String title;
    private String desc;
    private int type;
    private double[] latLng;
    private Image img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_report);
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            title = extras.getString("title");
            desc = extras.getString("desc");
            type = extras.getInt("type");
            latLng = extras.getDoubleArray("latLng");
            //img = (Image)extras.get("img");
            ((TextView)findViewById(R.id.reportTitle)).setText(title);
            ((TextView)findViewById(R.id.reportDesc)).setText("Description:\n" + desc);
        }

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        mapView = findViewById(R.id.viewReportMapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.OUTDOORS,
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        circleManager = new CircleManager(mapView, mapboxMap, style);
                        CircleOptions circleOptions = new CircleOptions()
                                .withLatLng(new LatLng(latLng[0], latLng[1]))
                                .withCircleRadius(12f)
                                .withCircleColor("red");
                        circleManager.create(circleOptions);
                        CameraPosition position = new CameraPosition.Builder()
                                .target(new LatLng(latLng[0], latLng[1]))
                                .zoom(13)
                                .tilt(10)
                                .build();
                        mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
                    }
                });
    }

    public void home(View view){
        finish();
    }
}