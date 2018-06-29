package com.example.louizibdawi.kmcounterandroid;

import android.app.usage.UsageEvents;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EventDb db;

    private int totalKpy, numEvents;
    private String kmView;

    private RadioButton kpw,kpm,kpy;

    private TableLayout tableLayout, finalRowTable;
    private List<EventDb.EventRecord> events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("KmCounter");

        tableLayout = (TableLayout) findViewById(R.id.table_body_layout);
        finalRowTable = (TableLayout) findViewById(R.id.table_final_row);

        //Reseting Db
        //MyDb.resetRB(this);

        //Getting Db
        db = MyDb.getDB(this);

        getKmView(savedInstanceState);

        initializeRadioButtons();

        displayEvents();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddEvent.class);
                startActivityForResult(intent, 1);
            }
        });
    }
    /*
     * Based on the extras passed into the current intent, this method will set
     * the string "KmView" to the appropriate view for the page.
     *
     * @param savedInstanceState A reference to a Bundle object that is passed into the activity
     */

    private void getKmView(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                kmView = "kpy";
            } else {
                kmView = extras.getString("KmView");
            }
        } else {
            kmView = (String) savedInstanceState.getSerializable("KmView");
        }
        System.out.println("Looking for: " +kmView);
    }

    /*
     * Initializes the Radio Buttons at the bottom of the home page.
     * It checks the current radio button selected by the user based
     * on the current "kmView" which is passed as an extra every time
     * the activity refreshes
     *
     * Default kmView is "kpy"
     */
    private void initializeRadioButtons() {
        kpw = (RadioButton) findViewById(R.id.kpw);
        kpm = (RadioButton) findViewById(R.id.kpm);
        kpy = (RadioButton) findViewById(R.id.kpy);

        switch(kmView) {
            case "kpw":
                kpw.setChecked(true);
                break;
            case "kpm":
                kpm.setChecked(true);
                break;
            case "kpy":
                kpy.setChecked(true);
                break;
        }
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        Intent refresh = new Intent(this, MainActivity.class);

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.kpw:
                if (checked)
                    refresh.putExtra("KmView", "kpw");
                break;
            case R.id.kpm:
                if (checked)
                    refresh.putExtra("KmView", "kpm");
                break;
            case R.id.kpy:
                if (checked)
                    refresh.putExtra("KmView", "kpy");
                break;
        }
        startActivity(refresh);
        this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            Intent refresh = new Intent(this, MainActivity.class);
            refresh.putExtra("KmView", "kpy");
            startActivity(refresh);
            this.finish();
        }
    }

    private void displayEvents() {
        events = db.getEvents();
        Collections.sort(events, new Comparator<EventDb.EventRecord>() {

            public int compare(EventDb.EventRecord o1, EventDb.EventRecord o2) {
                return o2.kpy - o1.kpy;
            }
        });

        numEvents = 0;

        for(EventDb.EventRecord event : events) {
            //Changing total kilometers from year to month or week if that button is checked
            int kms = getKmsForView(event.kpy, kmView);
            numEvents= numEvents + 1;

            addRowToTable(event, kms);

            totalKpy = totalKpy + kms;
        }
        if(events.size() > 0)
            addFinalRow(totalKpy);
    }

    private int getKmsForView(int kms, String kmView) {
        switch(kmView) {
            case "kpw":
                return (int)((double)kms / 52.1429);
            case "kpm":
                return kms / 12;
        }
        //Return passed in value if kmView is neither week or month because it is year
        return kms;
    }

    private synchronized void addRowToTable(EventDb.EventRecord event, int totalKms) {

        //Initializing Row
        TableRow tr = new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT,
                1.0f);
        tr.setLayoutParams(lp);
        tr.setBackground(ContextCompat.getDrawable(this, R.drawable.row_background));

        //Creating columns
        TextView name = createColumn(event.name, lp);
        TextView start = createColumn(event.start, lp);
        TextView end = createColumn(event.dest, lp);
        TextView kpt = createColumn(Integer.toString(event.kpt) +"km", lp);
        TextView kms = createColumn(Integer.toString(totalKms) + "km", lp);

        //Adding columns to row
        tr.addView(name);
        tr.addView(start);
        tr.addView(end);
        tr.addView(kpt);
        tr.addView(kms);

        //Adding link to full table row
        makeTableRowClickable(tr, event);

        //If even number event
        if(numEvents % 2 == 0) {
            tr.setBackgroundColor(Color.parseColor("#E8E8E8"));
        }

        this.tableLayout.addView(tr);
    }

    private void makeTableRowClickable(TableRow tr, EventDb.EventRecord event) {
        tr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddEvent.class);
                intent.putExtra("name", event.name);
                intent.putExtra("start", event.start);
                intent.putExtra("dest", event.dest);
                intent.putExtra("numPicker", event.numPicker);
                intent.putExtra("datePicker", event.datePicker);
                intent.putExtra("oneWay", event.oneWay);
                MainActivity.this.startActivityForResult(intent, 1);
            }
        });
    }

    private TextView createColumn(String text, TableRow.LayoutParams lp) {

        TextView tv = new TextView(this);
        tv.setLayoutParams(lp);
        tv.setText(text);
        tv.setTextSize(15);
        tv.setEms(2);
        tv.setGravity(Gravity.CENTER);
        tv.setMaxLines(3);
        tv.setEllipsize(TextUtils.TruncateAt.END);

        return tv;
    }

    private void addFinalRow(int totalKpy) {
        //Initializing Row
        TableRow tr = new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT,
                1.0f);
        tr.setLayoutParams(lp);

        //Creating columns
        TextView blank = createColumn("",lp);
        TextView blank2 = createColumn("",lp);
        TextView blank3 = createColumn("",lp);

        TextView text = null;
        switch (kmView) {
            case "kpw":
                text = createColumn("Weekly Kms\n", lp);
                break;
            case "kpm":
                text = createColumn("Monthly Kms\n", lp);
                break;
            case "kpy":
                text = createColumn("Yearly Kms\n", lp);
                break;
        }
        text.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        text.setLines(2);
        text.setBackground(ContextCompat.getDrawable(this, R.drawable.cell_background));

        TextView totalKms = createColumn(Integer.toString(totalKpy) + "km\n", lp);
        totalKms.setLines(2);
        totalKms.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        totalKms.setBackground(ContextCompat.getDrawable(this, R.drawable.cell_background));

        tr.addView(blank);
        tr.addView(blank2);
        tr.addView(blank3);
        tr.addView(text);
        tr.addView(totalKms);

        this.finalRowTable.addView(tr);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
