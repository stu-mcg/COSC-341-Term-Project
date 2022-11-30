package com.example.afrito;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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
import com.mapbox.mapboxsdk.plugins.annotation.Circle;
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager;
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions;
import com.mapbox.mapboxsdk.plugins.annotation.OnCircleClickListener;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager;

public class ViewReportActivity extends AppCompatActivity implements
        OnMapReadyCallback {

    private MapboxMap mapboxMap;
    private MapView mapView;
    private CircleManager circleManager;
    private double[] latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_report);
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            int reportId = extras.getInt("reportId");
            latLng = extras.getDoubleArray("latLng");
            ((TextView)findViewById(R.id.reportTitle)).setText("Report " + reportId);
            ((TextView)findViewById(R.id.reportDesc)).setText("Description: Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus eros arcu, posuere ac diam quis, mattis semper ligula. Aenean in odio non ex dignissim semper ut in libero. Nam dapibus quam id luctus feugiat. In malesuada metus eget enim cursus, sit amet maximus tellus facilisis. Aliquam efficitur, dolor sed venenatis cursus, nibh nisi dapibus nisl, at lacinia sapien sem eu lectus. Ut vehicula faucibus justo, eget accumsan diam egestas nec. Ut sed lacus venenatis, vulputate enim vel, pellentesque metus. Morbi nunc risus, tempor eu gravida fringilla, laoreet ac nisl. Curabitur ultrices lectus sed sapien consectetur, in placerat risus placerat. Donec posuere felis non turpis sodales ullamcorper.");
        }

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        mapView = findViewById(R.id.miniMapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        ViewReportActivity.this.mapboxMap = mapboxMap;
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