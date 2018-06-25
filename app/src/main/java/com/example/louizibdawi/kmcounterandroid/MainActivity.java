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

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EventDb db;

    private static int SCREEN_HEIGHT;
    private static int SCREEN_WIDTH;


    private RelativeLayout relativeLayoutMain;
    private RelativeLayout relativeLayoutA;

    private TableLayout tableLayoutA;

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

        relativeLayoutA.setLayoutParams(new RelativeLayout.LayoutParams(
                (int)(SCREEN_WIDTH/1.2), (int)(SCREEN_HEIGHT/1.5)));
        this.relativeLayoutMain.addView(relativeLayoutA);
    }

    private void initializeTableLayout() {
        tableLayoutA= new TableLayout(getApplicationContext());
        tableLayoutA.setPadding(0,0,0,0);
        TableLayout.LayoutParams layoutParamsTableLayoutA= new TableLayout.LayoutParams(
                (int)(SCREEN_WIDTH/1.2), (int)(SCREEN_HEIGHT/1.5));
        tableLayoutA.setLayoutParams(layoutParamsTableLayoutA);
        tableLayoutA.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        this.relativeLayoutA.addView(tableLayoutA);
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
            initializeRowForTable(i);
            addColumnToTable(i, event.name);
            addColumnToTable(i, event.start);
            addColumnToTable(i, event.end);
            addColumnToTable(i, Integer.toString(event.kpy));
            i=i+1;
        }
    }

    private void addColumnToTable(int rowPos, String text) {
        TableRow tableRowAdd= (TableRow) this.tableLayoutA.getChildAt(rowPos);
        tableRow= new TableRow(getApplicationContext());
        TableRow.LayoutParams layoutParamsTableRow= new TableRow.LayoutParams(
                (int)(SCREEN_WIDTH/1.2), (int)(SCREEN_HEIGHT/1.5));
        tableRow.setPadding(3,3,3,4);
        //tableRow.setBackground(getResources().getDrawable(R.drawable.cellbackground));
        tableRow.setLayoutParams(layoutParamsTableRow);

        TextView label_date = new TextView(getApplicationContext());

        label_date.setText(text);
        label_date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tableRow.setTag(label_date);
        this.tableRow.addView(label_date);

        tableRowAdd.addView(tableRow);
    }

    private void initializeRowForTable(int pos) {
        TableRow tableRowB= new TableRow(getApplicationContext());
        TableRow.LayoutParams layoutParamsTableRow= new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, (int)(SCREEN_HEIGHT/20));
        tableRowB.setPadding(0,0,0,0);
        tableRowB.setLayoutParams(layoutParamsTableRow);
        this.tableLayoutA.addView(tableRowB, pos);
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
