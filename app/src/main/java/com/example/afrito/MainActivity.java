package com.example.afrito;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;

import java.security.Permissions;

public class MainActivity extends AppCompatActivity {

    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        mapboxMap = mapView.getMapboxMap();
        mapboxMap.loadStyleUri(Style.OUTDOORS);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    public void listReports(View view){
        Intent intent = new Intent(this, ListReportsActivity.class);
        startActivity(intent);
    }

    public void createReport(View view){
        Intent intent = new Intent(this, CreateReportActivity.class);
        startActivity(intent);
    }

    public void conditionsBoard(View view){
        Intent intent = new Intent(this, ConditionsBoardActivity.class);
        startActivity(intent);
    }
}