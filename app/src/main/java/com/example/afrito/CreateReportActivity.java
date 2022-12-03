package com.example.afrito;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
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
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager;
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions;

import java.util.List;

public class CreateReportActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener {

    private MapboxMap mapboxMap;
    private MapView mapView;
    private CircleManager circleManager;
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    private Style mapboxStyle;

    private double[] latLng = {-1, -1};
    private int type = 0;
    ActivityResultLauncher<Intent> arl;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_report);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            latLng = extras.getDoubleArray("latLng");
        }

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        mapView = findViewById(R.id.viewReportMapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        EditText title = findViewById(R.id.title);
        EditText desc = findViewById(R.id.desc);
        RadioButton POI = findViewById(R.id.POI);
        RadioButton ENV = findViewById(R.id.ENV);
        RadioButton HZD = findViewById(R.id.HZD);
        RadioButton INF = findViewById(R.id.INF);
        POI.setChecked(true);
        ((RadioGroup)findViewById(R.id.reportTypeRadioGroup)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch(checkedId) {
                    case R.id.POI:
                        type = 0;
                        break;
                    case R.id.ENV:
                        type = 1;
                        break;
                    case R.id.HZD:
                        type = 2;
                        break;
                    case R.id.INF:
                        type = 3;
                        break;
                }
                updateLocation();
            }
        });

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

        findViewById(R.id.imageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                arl.launch(cameraIntent);
            }
        });

        findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String t = title.getText().toString();
                String d = desc.getText().toString();
                //using sample coord & null image for now
                Report r = new Report(t, d, type, new double[]{49.916333351789525, -119.4833972102201}, null );
                finish();
            }
        });

        findViewById(R.id.setLocationCurrent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLocationToCurrent();
                updateLocation();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.OUTDOORS,
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        mapboxStyle = style;
                        circleManager = new CircleManager(mapView, mapboxMap, style);
                        locationComponent = getLocationComponect();
                        if(latLng[0] == -1) {
                            setLocationToCurrent();
                        }
                        updateLocation();
                    }
                });
    }

    private void updateLocation(){
        circleManager.deleteAll();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
    private void setLocationToCurrent() {
        if(locationComponent != null) {
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, mapboxStyle).build());
            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);
            latLng[0] = locationComponent.getLastKnownLocation().getLatitude();
            latLng[1] = locationComponent.getLastKnownLocation().getLongitude();
            // Set the component's render mode
            locationComponent.setLocationComponentEnabled(false);
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
                    setLocationToCurrent();
                }
            });
        } else {
            Toast.makeText(this, "must allow location access", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}