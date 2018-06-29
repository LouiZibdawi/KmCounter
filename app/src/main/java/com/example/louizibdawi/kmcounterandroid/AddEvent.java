package com.example.louizibdawi.kmcounterandroid;

import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.RadioButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;

/**
 * Created by louizibdawi on 2018-06-03.
 */

public class AddEvent extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //Global Variables
    private static final String API_KEY = "AIzaSyBzGxIpPlLhivJfvHDjlWkcBagfQYhlk8o";
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));
    private double numKmsPerTrip, numKmsPerYear;
    private String startAd, endAd;
    private EventDb db;
    private int oneWayValue;

    //Widgets
    private EditText eventName;
    private AutoCompleteTextView startAddress, destAddress;
    private NumberPicker numPicker, datePicker;
    private RadioButton oneWay;
    private Button submit;
    private ImageButton swap;
    private TextInputLayout name_layout, start_layout, dest_layout;

    //Vars
    private PlaceAutocompleteAdapter addressAdapter;
    private GeoDataClient mGeoDataClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_event);

        //Initializing page elements
        eventName = (EditText) findViewById(R.id.eventName);
        startAddress = (AutoCompleteTextView) findViewById(R.id.startAddress);
        destAddress = (AutoCompleteTextView) findViewById(R.id.destAddress);
        oneWay = (RadioButton) findViewById(R.id.oneWay);
        submit = (Button) findViewById(R.id.submit);
        swap = (ImageButton) findViewById(R.id.swapImageButton);

        //Initializing database
        db = MyDb.getDB(this);

        //Setting number wheel for how often with values 1 to 50
        numPicker = findViewById(R.id.numberPicker);
        numPicker.setMinValue(1);
        numPicker.setMaxValue(50);

        //Setting date wheel with Week Month and Year values
        datePicker = findViewById(R.id.datePicker);
        datePicker.setMinValue(0);
        datePicker.setMaxValue(2);
        datePicker.setDisplayedValues(new String[]{"Week", "Month", "Year"});

        getPassedInValues(savedInstanceState);

        init();

        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(oneWay.isChecked())
                    oneWayValue = 1;
                else
                    oneWayValue = 0;
                
                int errors = errorCheck();

                if(errors == 0) {
                    //Getting distance for single (1 or 2 way) trip
                    getNumKmsPerTrip(startAddress.getText().toString(),
                            destAddress.getText().toString(),
                            oneWay.isChecked());
                    //Getting total distance in a year based on amount of trips
                    getNumKmsPerYear(numPicker.getValue(), datePicker.getValue());

                    db.addEvent(eventName.getText().toString(),
                            startAddress.getText().toString(),
                            destAddress.getText().toString(),
                            numPicker.getValue(),
                            datePicker.getValue(),
                            numKmsPerTrip,
                            numKmsPerYear,
                            oneWayValue
                    );

                    setResult(RESULT_OK, null);
                    finish();

                }
            }
        });

        swap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startAd = startAddress.getText().toString();
                endAd = destAddress.getText().toString();

                startAddress.setText(endAd);
                destAddress.setText(startAd);
            }

        });

        oneWay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(oneWay.isChecked())
                    oneWay.setChecked(false);
                else
                    oneWay.setChecked(true);
            }
        });

    }

    private void getPassedInValues(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                eventName.setText(extras.getString("name"));
                startAddress.setText(extras.getString("start"));
                destAddress.setText(extras.getString("dest"));
                numPicker.setValue(extras.getInt("numPicker"));
                datePicker.setValue(extras.getInt("datePicker"));
                if(extras.getInt("oneWay") == 1)
                    oneWay.setChecked(true);
            }
        } else {
            eventName.setText((String) savedInstanceState.getSerializable("name"));
            startAddress.setText((String) savedInstanceState.getSerializable("start"));
            destAddress.setText((String) savedInstanceState.getSerializable("dest"));
            numPicker.setValue((int) savedInstanceState.getSerializable("numPicker"));
            datePicker.setValue((int) savedInstanceState.getSerializable("datePicker"));
            if((int) savedInstanceState.getSerializable("oneWay") == 1)
                oneWay.setChecked(true);
        }
    }

    private int errorCheck() {
        int numErrors = 0;

        name_layout = (TextInputLayout) findViewById(R.id.name_layout);
        start_layout = (TextInputLayout) findViewById(R.id.start_layout);
        dest_layout = (TextInputLayout) findViewById(R.id.dest_layout);

        if (eventName.getText().toString().trim().equals("")) {
            numErrors++;
            name_layout.setError("Event name can not be blank");
        }
        else
            name_layout.setErrorEnabled(false);
        
        if(startAddress.getText().toString().trim().equals("")) {
            numErrors++;
            start_layout.setError("Start address can not be blank");
        }
        else
            start_layout.setErrorEnabled(false);
        
        if(destAddress.getText().toString().trim().equals("")) {
            numErrors++;
            dest_layout.setError("Destination address can not be blank");
        }
        else
            dest_layout.setErrorEnabled(false);

        return numErrors;
        
    }

    private void init() {

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        addressAdapter = new PlaceAutocompleteAdapter(AddEvent.this, mGeoDataClient, LAT_LNG_BOUNDS, null);

        startAddress.setAdapter(addressAdapter);
        destAddress.setAdapter(addressAdapter);

    }

    private void getNumKmsPerTrip(String start, String dest, boolean oneWay) {
        String startStr = startAddress.getText().toString().replaceAll("\\s+", "");
        String endStr = destAddress.getText().toString().replaceAll("\\s+", "");

        String urlStr = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + startStr
                + "&destinations=" + endStr + "&key=" + API_KEY;

        System.out.println("URL: " + urlStr);

        try {
            new JSONTask().execute(urlStr).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        //If not a one way trip, multiple the distance by 2
        if (!oneWay) {
            numKmsPerTrip = numKmsPerTrip * 2;
        }

        System.out.println("Total kms is " + numKmsPerTrip);
    }

    private void getNumKmsPerYear(int numTimes, int weekMonthYear) {
        double totalTimes = 1;
        switch(weekMonthYear) {
            case 0: //"Week"
                totalTimes = numTimes * 52.1429;
                break;
            case 1: //"Month"
                totalTimes = numTimes * 12;
                break;
            case 2: //"Year"
                totalTimes = numTimes;
                break;
            default:
                break;
        }

        numKmsPerYear = totalTimes * numKmsPerTrip;

        System.out.println("Total kms per year " + numKmsPerYear);
    }

    //JSONTask
    //Calls google maps url with two locations and parses the JSON that is returned to get the total distance
    public class JSONTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                URLConnection uc = url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));

                String inputLine;
                String input = "";
                while ((inputLine = in.readLine()) != null)
                    input = input + inputLine;
                in.close();

                JSONObject jsonObj = new JSONObject(input);
                JSONArray rows = jsonObj.getJSONArray("rows");
                JSONObject elements = rows.getJSONObject(0);
                JSONArray elementsArray = elements.getJSONArray("elements");
                JSONObject firstElement = elementsArray.getJSONObject(0);
                JSONObject distance = firstElement.getJSONObject("distance");

                //Converting "127 kms" to "127" so we can convert to int
                String kms = distance.getString("text").replaceAll("[^0-9/.]", "");

                numKmsPerTrip = Double.parseDouble(kms);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
}