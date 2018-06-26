package com.example.louizibdawi.kmcounterandroid;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

        relativeLayoutMain= (RelativeLayout)findViewById(R.id.relativeLayoutMain);
        getScreenDimension();
        initializeRelativeLayout();
        initializeTableLayout();


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

    private void initializeRelativeLayout() {
        relativeLayoutA= new RelativeLayout(getApplicationContext());
        relativeLayoutA.setId(R.id.relativeLayoutA);
        relativeLayoutA.setPadding(0,0,0,0);

        RelativeLayout.LayoutParams layoutParams= new RelativeLayout.LayoutParams(
                (int)(SCREEN_WIDTH/1.2), (int)(SCREEN_HEIGHT/1.5));

        layoutParams.addRule(RelativeLayout.BELOW, R.id.appBar);

        relativeLayoutA.setLayoutParams(layoutParams);

        this.relativeLayoutMain.addView(relativeLayoutA);
    }

    private void initializeTableLayout() {
        tableLayout = new TableLayout(getApplicationContext());
        tableLayout.setPadding(0,10,0,0);
        TableLayout.LayoutParams layoutParamsTableLayoutA= new TableLayout.LayoutParams(
                (int)(SCREEN_WIDTH/1.2), (int)(SCREEN_HEIGHT/1.5));
        tableLayout.setLayoutParams(layoutParamsTableLayoutA);
        tableLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        this.relativeLayoutA.addView(tableLayout);
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
        int i = 0;
        for(EventDb.EventRecord event : events) {
            addRowToTable(i, event);
            i=i+1;
        }
    }

//    private synchronized void addColumnToRow(int rowPos, String text) {
//        TableRow tableRowAdd= (TableRow) this.tableLayout.getChildAt(rowPos);
//        tableRow = new TableRow(getApplicationContext());
//        TableRow.LayoutParams layoutParamsTableRow= new TableRow.LayoutParams(
//                (int)(SCREEN_WIDTH/1.2), (int)(SCREEN_HEIGHT/1.5));
//        tableRow.setPadding(3,3,3,4);
//        tableRow.setBackground(getResources().getDrawable(R.drawable.cellbackground));
//        tableRow.setLayoutParams(layoutParamsTableRow);
//
//        TextView label_date = new TextView(getApplicationContext());
//
//        label_date.setText(text);
//        label_date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
//        tableRow.setTag(label_date);
//        this.tableRow.addView(label_date);
//
//        tableRowAdd.addView(tableRow);
//        this.tableLayout.addView(tableRow, pos);
//    }

    private synchronized void addRowToTable(int pos, EventDb.EventRecord event) {
        tableRow= new TableRow(getApplicationContext());
        TableRow.LayoutParams layoutParamsTableRow= new TableRow.LayoutParams(
                (int)(SCREEN_WIDTH/1.2), SCREEN_HEIGHT/20);
        tableRow.setPadding(3,3,3,4);
        tableRow.setBackground(getResources().getDrawable(R.drawable.cellbackground));
        tableRow.setLayoutParams(layoutParamsTableRow);

        //Adding columns
        TextView name=new TextView(this.getApplicationContext());
        TextView start=new TextView(this.getApplicationContext());
        TextView end=new TextView(this.getApplicationContext());
        TextView kms=new TextView(this.getApplicationContext());

        name.setText(event.name);
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        name.setMaxWidth(200);
        tableRow.addView(name);
        start.setText(event.start);
        start.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        start.setMaxWidth(500);
        tableRow.addView(start);
        end.setText(event.end);
        end.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        end.setMaxWidth(500);
        tableRow.addView(end);
        kms.setText(Integer.toString(event.kpy));
        kms.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        kms.setMaxWidth(100);
        tableRow.addView(kms);


        this.tableLayout.addView(tableRow, pos);
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
