package com.example.louizibdawi.kmcounterandroid;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EventDb db;

    private static int SCREEN_HEIGHT;
    private static int SCREEN_WIDTH;


    private RelativeLayout relativeLayoutMain;
    private RelativeLayout relativeLayoutA;

    private TableLayout tableLayout;

    private TableRow tableRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Reseting Db
        //MyDb.resetRB(this);
        db = MyDb.getDB(this);

        displayEvents();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEventPage();
            }
        });
    }

    private void getScreenDimension(){
        WindowManager wm= (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        SCREEN_WIDTH= size.x;
        SCREEN_HEIGHT = size.y;
    }

    private void addEventPage() {
        Intent intent = new Intent(MainActivity.this, AddEvent.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            Intent refresh = new Intent(this, MainActivity.class);
            startActivity(refresh);
            this.finish();
        }
    }

    private void displayEvents() {
        List<EventDb.EventRecord> events = db.getEvents();
        int i = 3;
        for(EventDb.EventRecord event : events) {
            addRowToTable(i, event);
            i=i+1;
        }
    }


    private synchronized void addRowToTable(int pos, EventDb.EventRecord event) {

        //Initializing table layout
        tableLayout = (TableLayout) findViewById(R.id.table_layout);

        //Initializing Row
        TableRow tr = new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT,
                1.0f);
        tr.setLayoutParams(lp);

        //Creating columns
        TextView name = createColumn(event.name, lp);
        TextView start = createColumn(event.start, lp);
        TextView end = createColumn(event.end, lp);
        TextView kms = createColumn(Integer.toString(event.kpy), lp);

//        //Initializing Horizontal lines
//        View rowDivider = new View(this);
//        rowDivider.setLayoutParams(new ViewGroup.LayoutParams(3, ViewGroup.LayoutParams.MATCH_PARENT));
//        rowDivider.setBackgroundColor(Color.parseColor("#686868"));
//
//        //Initializing Verticle lines
//        View columnDivider = new View(this);
//        columnDivider.setLayoutParams(new ViewGroup.LayoutParams(35, 3));
//        columnDivider.setBackgroundColor(Color.parseColor("#686868"));
//
//        View columnDivider2 = new View(this);
//        columnDivider.setLayoutParams(new ViewGroup.LayoutParams(35, 3));
//        columnDivider.setBackgroundColor(Color.parseColor("#686868"));
//
//        View columnDivider3 = new View(this);
//        columnDivider.setLayoutParams(new ViewGroup.LayoutParams(35, 3));
//        columnDivider.setBackgroundColor(Color.parseColor("#686868"));

        //Adding columns to row
        tr.addView(name);
//        tr.addView(columnDivider);
        tr.addView(start);
//        tr.addView(columnDivider2);
        tr.addView(end);
//        tr.addView(columnDivider3);
        tr.addView(kms);
//        tr.addView(rowDivider);

        this.tableLayout.addView(tr);
    }

    private TextView createColumn(String text, TableRow.LayoutParams lp) {

        TextView tv = new TextView(this);
        tv.setLayoutParams(lp);
        tv.setBackgroundColor(Color.WHITE);
        tv.setFontFeatureSettings("serif");
        tv.setText(text);
        tv.setTextSize(15);
        tv.setEms(2);
        tv.setMaxLines(3);
        tv.setEllipsize(TextUtils.TruncateAt.END);

        return tv;
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
