package com.example.afrito;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private double[] latLng;
    private int type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_report);
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            Report report = extras.getParcelable("report");
            ((TextView)findViewById(R.id.reportTitle)).setText(report.getTitle());
            type = report.getType();
            String[] colors = {"#ad9a1a", "#216b26", "#eb1d0e", "#0e1deb"};
            String[] types = {"Point of Interest", "Environmental Conditions", "Hazard", "Information"};
            ((TextView)findViewById(R.id.typeText)).setText(types[type]);
            ((TextView)findViewById(R.id.typeText)).setTextColor(Color.parseColor(colors[type]));
            ((TextView)findViewById(R.id.descText)).setText(report.getDesc());

            for(int i = 0; i < report.getImgs().length; i++) {
                LinearLayout imageScroller = (LinearLayout) findViewById(R.id.viewReportImageScroll);
                ImageView imageView = new ImageView(ViewReportActivity.this);
                float factor = ViewReportActivity.this.getResources().getDisplayMetrics().density;
                imageView.setLayoutParams(new LinearLayout.LayoutParams((int) (200 * factor), (int) (200 * factor)));
                imageScroller.addView(imageView);
                imageView.setImageBitmap(report.getImgs()[i]);
            }

            latLng = report.getLatLng();

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
                        String[] colors = {"yellow", "green", "red", "blue"};
                        CircleOptions circleOptions = new CircleOptions()
                                .withLatLng(new LatLng(latLng[0], latLng[1]))
                                .withCircleRadius(12f)
                                .withCircleColor(colors[type]);
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