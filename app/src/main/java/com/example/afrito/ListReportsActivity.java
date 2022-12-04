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
import java.util.Collections;
import java.util.Comparator;

public class ListReportsActivity extends AppCompatActivity {
    private String distance;
    private ArrayList<Report> reports;
    private double[] userLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_reports);

        userLatLng = getIntent().getExtras().getDoubleArray("lastKnownLatLng");

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
        ((LinearLayout) findViewById(R.id.listReportsScrollView)).removeAllViews();
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        distance = spinner.getSelectedItem().toString();
        double distanceDouble = Double.parseDouble(distance);
        reports = getIntent().getExtras().getParcelableArrayList("reports");
        Collections.sort(reports, new reportDistComparator(userLatLng));
        boolean empty = true;
        for(Report r : reports){
            ViewGroup layout = (ViewGroup) findViewById(R.id.linearLayout);
            View reportListItemView = LayoutInflater.from(this).inflate(R.layout.report_list_view, null);
            Double userDistance = calculateDistance(userLatLng, r.getLatLng());
            if(distanceDouble - userDistance > 0) {
                ((TextView)findViewById(R.id.yourReportsEmptyMessageTextView)).setText("");
                layout.setVisibility(View.VISIBLE);
                empty = false;
                reportListItemView.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
                ((TextView) reportListItemView.findViewById(R.id.reportListTitleTextView)).setText(r.getTitle());
                ((TextView) reportListItemView.findViewById(R.id.reportListDistTextView)).setText(Math.round(userDistance * 100)/100.0  + "km");
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
                        Intent intent = new Intent(ListReportsActivity.this, ViewReportActivity.class);
                        intent.putExtra("report", r);
                        startActivity(intent);
                    }
                });
            }
        }
        if(empty){
            ((TextView)findViewById(R.id.yourReportsEmptyMessageTextView)).setText("No reports near you.");
        }

    }

    public class reportDistComparator implements Comparator<Report>{
        double[] userLatLng;

        reportDistComparator(double[] userLatLng){
            this.userLatLng = userLatLng;
        }

        @Override
        public int compare(Report r1, Report r2) {
            System.out.println((int)(calculateDistance(userLatLng, r1.getLatLng()) - calculateDistance(userLatLng, r2.getLatLng())));
            return (int)(calculateDistance(userLatLng, r1.getLatLng())*100 - calculateDistance(userLatLng, r2.getLatLng())*100);
        }
    }

    private Double calculateDistance(double[] latLng1, double[] latLng2){

        double lon1 = Math.toRadians(latLng1[1]);
        double lon2 = Math.toRadians(latLng2[1]);
        double lat1 = Math.toRadians(latLng1[0]);
        double lat2 = Math.toRadians(latLng2[0]);

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