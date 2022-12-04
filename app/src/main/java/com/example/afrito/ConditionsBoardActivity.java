package com.example.afrito;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class ConditionsBoardActivity extends AppCompatActivity {
    String distance;
    ArrayList<Report> reports;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conditions_board);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.distance, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                populateReports();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });
    }

    public void populateReports(){
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        distance = spinner.getSelectedItem().toString();
        double distanceDouble = Double.parseDouble(distance);
        reports = new ArrayList<Report>();
        reports = getIntent().getExtras().getParcelableArrayList("reports");
        boolean empty = true;
        double userLat = getIntent().getDoubleExtra("lastKnownLat", 0);
        double userLong = getIntent().getDoubleExtra("lastKnownLong", 0);
        for(Report r : reports){
            double reportLat = r.getLatLng()[0];
            double reportLong = r.getLatLng()[1];
            ViewGroup layout = (ViewGroup) findViewById(R.id.linearLayout);
            View reportListItemView = LayoutInflater.from(this).inflate(R.layout.report_list_view, null);
            Double userDistance = calculateDistance(reportLat, userLat, reportLong, userLong);
            if(distanceDouble - userDistance > 0) {
                ((TextView)findViewById(R.id.yourReportsEmptyMessageTextView)).setText("");
                layout.setVisibility(View.VISIBLE);
                if (!r.getUserCreated()) {
                    empty = false;
                    reportListItemView.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
                    ((TextView) reportListItemView.findViewById(R.id.reportListTitleTextView)).setText(r.getTitle());
                    int type = r.getType();
                    String[] colors = {"#ad9a1a", "#216b26", "#eb1d0e", "#0e1deb"};
                    String[] types = {"Point of Interest", "Environmental Conditions", "Hazard", "Information"};
                    ((TextView) reportListItemView.findViewById(R.id.reportListTypeTextview)).setText(types[type]);
                    ((TextView) reportListItemView.findViewById(R.id.reportListTypeTextview)).setTextColor(Color.parseColor(colors[type]));
                    ((TextView) reportListItemView.findViewById(R.id.reportListDescTextview)).setText(r.getDesc());
                    ((LinearLayout) findViewById(R.id.listReportsScrollView)).addView(reportListItemView);
                    reportListItemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(ConditionsBoardActivity.this, ViewReportActivity.class);
                            intent.putExtra("report", r);
                            startActivity(intent);
                        }
                    });
                }
            }
        else{
            ((TextView)findViewById(R.id.yourReportsEmptyMessageTextView)).setText("No reports near you.");
            layout.setVisibility(View.INVISIBLE);
        }

        }
        if(empty){
            ((TextView)findViewById(R.id.yourReportsEmptyMessageTextView)).setText("No reports near you.");
        }

    }
    private Double calculateDistance(double lat1, double lat2, double lon1, double lon2){

        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2),2);

        double c = 2 * Math.asin(Math.sqrt(a));

        double r = 6371;

        return(c * r);
    }

    public void home(View view){
        finish();
    }
}