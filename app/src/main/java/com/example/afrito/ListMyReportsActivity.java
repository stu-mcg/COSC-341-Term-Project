package com.example.afrito;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ListMyReportsActivity extends AppCompatActivity {

    ArrayList<Report> reports;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_my_reports);

        reports = new ArrayList<Report>();
        reports = getIntent().getExtras().getParcelableArrayList("reports");
        boolean empty = true;
        for(Report r : reports){
            if(r.getUserCreated()){
                empty = false;
                View reportListItemView = LayoutInflater.from(this).inflate(R.layout.report_list_view, null);
                reportListItemView.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
                ((TextView)reportListItemView.findViewById(R.id.reportListTitleTextView)).setText(r.getTitle());
                int type = r.getType();
                String[] colors = {"#ad9a1a", "#216b26", "#eb1d0e", "#0e1deb"};
                String[] types = {"Point of Interest", "Environmental Conditions", "Hazard", "Information"};
                ((TextView)reportListItemView.findViewById(R.id.reportListTypeTextview)).setText(types[type]);
                ((TextView)reportListItemView.findViewById(R.id.reportListTypeTextview)).setTextColor(Color.parseColor(colors[type]));
                ((TextView)reportListItemView.findViewById(R.id.reportListDescTextview)).setText(r.getDesc());
                ((LinearLayout)findViewById(R.id.listReportsScrollView)).addView(reportListItemView);
                reportListItemView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ListMyReportsActivity.this, ViewReportActivity.class);
                        intent.putExtra("report", r);
                        startActivity(intent);
                    }
                });
            }
        }
        if(empty){
            ((TextView)findViewById(R.id.yourReportsEmptyMessageTextView)).setText("You haven't created any reports yet!");
        }
    }

    public void home(View view){
        finish();
    }
}