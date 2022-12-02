package com.example.afrito;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import android.widget.RadioButton;

import java.io.File;

public class CreateReportActivity extends AppCompatActivity {
    int type;
    ActivityResultLauncher<Intent> arl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                cam.setVisibility(View.INVISIBLE);
                if(result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ImageView imageView = findViewById(R.id.imageView);
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
                Report r = new Report(t, d, type, new double[]{49.916333351789525, -119.4833972102201}, null );
                finish();
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


}