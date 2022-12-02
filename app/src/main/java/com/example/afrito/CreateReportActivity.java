package com.example.afrito;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager;
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions;

import java.io.File;
import java.util.ArrayList;

public class CreateReportActivity extends AppCompatActivity implements
        OnMapReadyCallback{

    private MapboxMap mapboxMap;
    private MapView mapView;
    private CircleManager circleManager;

    int type;
    ActivityResultLauncher<Intent> arl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
//        mapView = findViewById(R.id.selectLocationMapView);
//        mapView.onCreate(savedInstanceState);
//        mapView.getMapAsync(this);

        setContentView(R.layout.activity_create_report);

        EditText title = findViewById(R.id.title);
        EditText desc = findViewById(R.id.desc);
        RadioButton POI = findViewById(R.id.POI);
        RadioButton ENV = findViewById(R.id.ENV);
        RadioButton HZD = findViewById(R.id.HZD);
        RadioButton INF = findViewById(R.id.INF);

        Button create = findViewById(R.id.create);

        ImageButton cam = findViewById(R.id.imageButton);

        arl = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK && result.getData() != null) {
                    LinearLayout imageScroller = (LinearLayout) findViewById(R.id.photoScroll);
                    ImageView imageView = new ImageView(CreateReportActivity.this);
                    float factor = CreateReportActivity.this.getResources().getDisplayMetrics().density;
                    imageView.setLayoutParams(new LinearLayout.LayoutParams((int)(150 * factor), (int)(150 * factor)));
                    imageScroller.addView(imageView);
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    imageView.setImageBitmap(photo);
                }
            }
        });

        cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                arl.launch(cameraIntent);
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String t = title.getText().toString();
                String d = desc.getText().toString();
                onRadioButtonClicked(POI);
                onRadioButtonClicked(ENV);
                onRadioButtonClicked(HZD);
                onRadioButtonClicked(INF);
                //using sample coord & null image for now
                ArrayList<Report> reports = getIntent().getParcelableArrayListExtra("reports");
                reports.add(new Report(t, d, type, new double[]{49.916333351789525, -119.4833972102201}, null ));
                Intent intent = new Intent(CreateReportActivity.this, MainActivity.class);
                intent.putExtra("reports", reports);
                startActivity(intent);
            }
        });
    }


    public void onRadioButtonClicked(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.POI:
                if(checked)
                    type = 0;
                break;
            case R.id.ENV:
                if(checked)
                    type = 1;
                break;
            case R.id.HZD:
                if(checked)
                    type = 2;
                break;
            case R.id.INF:
                if(checked)
                    type = 3;
                break;
        }
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.OUTDOORS,
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        CreateReportActivity.this.mapboxMap = mapboxMap;

                        LocationComponent locationComponent = mapboxMap.getLocationComponent();
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
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}