package com.example.afrito;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Circle;
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager;
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions;
import com.mapbox.mapboxsdk.plugins.annotation.OnCircleClickListener;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener {

    private static Boolean firstLoad = true;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private MapView mapView;
    private Style style;

    private MarkerViewManager markerViewManager;
    private CircleManager circleManager;
    private ArrayList<Circle> markers;
    private MarkerView currentMarkerInfoBox;
    private int selectedReport = -1;

    public static ArrayList<Report> reports;

    ActivityResultLauncher<Intent> createReportResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(firstLoad){
            Report.clearImages(this);
            firstLoad = false;
        }

        reports = new ArrayList<Report>();
        createExampleReports(reports);
        markers = new ArrayList<Circle>();


        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        createReportResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Report report = data.getExtras().getParcelable("report");
                    reports.add(report);
                    addMarker(report);
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        MainActivity.this.mapboxMap = mapboxMap;
        mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
            @Override
            public boolean onMapClick(@NonNull LatLng point) {
                if(selectedReport != -1){
                    markerViewManager.removeMarker(currentMarkerInfoBox);
                    currentMarkerInfoBox = null;
                    selectedReport = -1;
                }
                return false;
            }
        });
        mapboxMap.setStyle(Style.OUTDOORS,
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        MainActivity.this.style = style;
                        enableLocationComponent(style);
                        centerOnUser(null);

                        circleManager = new CircleManager(mapView, mapboxMap, style);
                        markerViewManager = new MarkerViewManager(mapView, mapboxMap);

                        loadCircles();
                    }
                });
    }

    private void loadCircles(){
        for(int i = 0; i < reports.size(); i++){
            addMarker(reports.get(i));
        }

        circleManager.addClickListener(new OnCircleClickListener() {
            @Override
            public boolean onAnnotationClick(Circle circle) {
                selectedReport = (int) circle.getId();
                currentMarkerInfoBox = addInfoBox(selectedReport);
                markerViewManager.addMarker(currentMarkerInfoBox);
                return false;
            }
        });
    }

    private void addMarker(Report report){
        double[] latLng = report.getLatLng();
        String[] colors = {"yellow", "green", "red", "blue"};
        CircleOptions circleOptions = new CircleOptions()
                .withLatLng(new LatLng(latLng[0], latLng[1]))
                .withCircleRadius(12f)
                .withCircleColor(colors[report.getType()]);
        markers.add(circleManager.create(circleOptions));
    }

    private MarkerView addInfoBox(int i){
        Report report = reports.get(i);
        View reportInfoView = LayoutInflater.from(MainActivity.this).inflate(R.layout.report_info_marker_view, null);
        reportInfoView.setLayoutParams(new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        ((TextView)reportInfoView.findViewById(R.id.marker_window_title)).setText(report.getTitle());
        ((TextView)reportInfoView.findViewById(R.id.marker_window_snippet)).setText("click to view");
        reportInfoView.setTag(i);
        reportInfoView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                int reportId = (int)view.getTag();
                Intent intent = new Intent(MainActivity.this, ViewReportActivity.class);
                intent.putExtra("reportId", i);
                startActivity(intent);
            }
        });
        return new MarkerView(new LatLng(report.getLatLng()[0], report.getLatLng()[1]), reportInfoView);
    }

    @SuppressWarnings( {"MissingPermission"})
    private LocationComponent getLocationComponect(){
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            return mapboxMap.getLocationComponent();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
            return null;
        }
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        LocationComponent locationComponent = getLocationComponect();
        if(locationComponent != null) {
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());
            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "Requires location", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, "must allow location access", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mapView.onResume();
        if(selectedReport != -1) {
            selectedReport = -1;
            markerViewManager.removeMarker(currentMarkerInfoBox);
            currentMarkerInfoBox = null;
        }
        circleManager.deleteAll();
        circleManager = new CircleManager(mapView, mapboxMap, style);
        loadCircles();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (markerViewManager != null) {
            markerViewManager.onDestroy();
        }
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public void listReports(View view){
        Intent intent = new Intent(this, ListMyReportsActivity.class);
        startActivity(intent);
    }

    public void createReport(View view){
        Intent intent = new Intent(this, CreateReportActivity.class);
        intent.putExtra("latLng", new double[] {49.916333351789525, -119.4833972102201});
        createReportResultLauncher.launch(intent);
    }

    public void ReportsList(View view){
        LocationComponent loc = mapboxMap.getLocationComponent();
        assert loc.getLastKnownLocation() != null;
        Intent intent = new Intent(this, ListReportsActivity.class);
        intent.putExtra("lastKnownLatLng", new double[] {loc.getLastKnownLocation().getLatitude(), loc.getLastKnownLocation().getLongitude()});
        startActivity(intent);
    }

    public void centerOnUser(View view){
        LocationComponent locationComponent = getLocationComponect();
        if(locationComponent != null){
            double lng = locationComponent.getLastKnownLocation().getLongitude();
            double lat = locationComponent.getLastKnownLocation().getLatitude();
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(lat, lng))
                    .zoom(13)
                    .tilt(10)
                    .build();
            mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
        }
    }

    private void createExampleReports(ArrayList<Report> reports) {
        reports.add(new Report(
                "Trails are muddy",
                "It's wet out here",
                Report.ENV,
                new double[] {49.904678312972024, -119.48708391177118},
                new Bitmap[] {
                        BitmapFactory.decodeResource(this.getResources(), R.drawable.mud),
                },
                this,
                false
        ));
        reports.add(new Report(
                "Trail is closed",
                "Trail work on going for the next month ",
                Report.INF,
                new double[] {49.907225395275944, -119.47146570338202},
                new Bitmap[] {
                        BitmapFactory.decodeResource(this.getResources(), R.drawable.trailwork1),
                        BitmapFactory.decodeResource(this.getResources(), R.drawable.trailwork2),
                        BitmapFactory.decodeResource(this.getResources(), R.drawable.trailwork3),
                },
                this,
                false
        ));
        reports.add(new Report(
                "Fallen tree",
                "Watch out for a fallen tree blocking the trail. It's right after a blind corner",
                Report.HZD,
                new double[] {49.916333351789525, -119.4833972102201},
                new Bitmap[] {},
                this,
                false
        ));
        reports.add(new Report(
                "Great Viewpoint",
                "turn left after the big rock. Brings you to a great spot to watch the sunset",
                Report.POI,
                new double[] {49.91067885137928, -119.49023436582392},
                new Bitmap[] {
                        BitmapFactory.decodeResource(this.getResources(), R.drawable.view),
                },
                this,
                false
        ));
    }
}