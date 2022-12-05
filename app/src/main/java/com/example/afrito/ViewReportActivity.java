package com.example.afrito;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    private MapboxMap mapboxMap;
    private CircleManager circleManager;
    private double[] latLng;
    private int type;
    private Report report;
    private int reportId;
    ActivityResultLauncher<Intent> editReportResultLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_report);
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            report = MainActivity.reports.get(extras.getInt("reportId"));

        }

        if(!report.getUserCreated()){
            ((Button)findViewById(R.id.editReportButton)).setVisibility(View.INVISIBLE);
        }

        loadReport();

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        mapView = findViewById(R.id.viewReportMapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        editReportResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Report editedReport = data.getExtras().getParcelable("report");
                    report.set(editedReport);
                    loadReport();
                    circleManager.deleteAll();
                    onMapReady(mapboxMap);
                }
            }
        });
    }

    private void loadReport(){
        ((TextView)findViewById(R.id.reportTitle)).setText(report.getTitle());
        type = report.getType();
        String[] colors = {"#ad9a1a", "#216b26", "#eb1d0e", "#0e1deb"};
        String[] types = {"Point of Interest", "Environmental Conditions", "Hazard", "Information"};
        ((TextView)findViewById(R.id.typeText)).setText(types[type]);
        ((TextView)findViewById(R.id.typeText)).setTextColor(Color.parseColor(colors[type]));
        ((TextView)findViewById(R.id.descText)).setText(report.getDesc());

        LinearLayout imageScroller = (LinearLayout) findViewById(R.id.viewReportImageScroll);
        imageScroller.removeAllViews();
        for(int i = 0; i < report.getImgs().length; i++) {
            ImageView imageView = new ImageView(ViewReportActivity.this);
            float factor = ViewReportActivity.this.getResources().getDisplayMetrics().density;
            imageView.setLayoutParams(new LinearLayout.LayoutParams((int) (200 * factor), (int) (200 * factor)));
            imageScroller.addView(imageView);
            imageView.setImageBitmap(report.getImgs()[i]);
        }
        latLng = report.getLatLng();
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        ViewReportActivity.this.mapboxMap = mapboxMap;
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

    public void back(View view){
        finish();
    }

    public void edit(View view){
        Intent intent = new Intent(this, CreateReportActivity.class);
        intent.putExtra("edit", true);
        intent.putExtra("report", report);
        editReportResultLauncher.launch(intent);
    }
}