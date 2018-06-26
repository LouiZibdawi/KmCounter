package com.example.louizibdawi.kmcounterandroid;

import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

    //Widgets
    private EditText eventName;
    private AutoCompleteTextView startAddress;
    private AutoCompleteTextView endAddress;
    private NumberPicker num, date;
    private RadioButton oneWay;
    private Button submit;
    private ImageButton swap;

    //Vars
    private PlaceAutocompleteAdapter addressAdapter;
    private GeoDataClient mGeoDataClient;
    private Geocoder geocoder;
    private final String USER_AGENT = "Mozilla/5.0";
    protected GoogleApiClient mGoogleApiClient;
    private static final int GOOGLE_API_CLIENT_ID = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_event);

        //Initializing page elements
        eventName = (EditText) findViewById(R.id.eventName);
        startAddress = (AutoCompleteTextView) findViewById(R.id.startAddress);
        endAddress = (AutoCompleteTextView) findViewById(R.id.destAddress);
        oneWay = (RadioButton) findViewById(R.id.oneWay);
        submit = (Button) findViewById(R.id.submit);
        swap = (ImageButton) findViewById(R.id.swapImageButton);

        //Initializing database
        db = MyDb.getDB(this);

        //Initializing geocoder
        geocoder = new Geocoder(this);

        //Setting number wheel for how often with values 1 to 50
        num = findViewById(R.id.numberPicker);
        num.setMinValue(1);
        num.setMaxValue(50);

        //Setting date wheel with Week Month and Year values
        date = findViewById(R.id.datePicker);
        date.setMinValue(0);
        date.setMaxValue(2);
        date.setDisplayedValues(new String[]{"Week", "Month", "Year"});

        init();

        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Getting distance for single (1 or 2 way) trip
                getNumKmsPerTrip(startAddress.getText().toString(),
                        endAddress.getText().toString(),
                        oneWay.isChecked());
                getNumKmsPerYear(num.getValue(), date.getValue());

                db.addEvent(eventName.getText().toString(),
                        startAddress.getText().toString(),
                        endAddress.getText().toString(),
                        numKmsPerTrip,
                        numKmsPerYear
                );

                setResult(RESULT_OK, null);
                finish();

            }
        });

        swap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startAd = startAddress.getText().toString();
                endAd = endAddress.getText().toString();

                startAddress.setText(endAd);
                endAddress.setText(startAd);
            }

        });

    }

    private void init() {

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        addressAdapter = new PlaceAutocompleteAdapter(AddEvent.this, mGeoDataClient, LAT_LNG_BOUNDS, null);

        startAddress.setAdapter(addressAdapter);
        endAddress.setAdapter(addressAdapter);

    }

    private void getNumKmsPerTrip(String start, String dest, boolean oneWay) {
        String startStr = startAddress.getText().toString().replaceAll("\\s+", "");
        String endStr = endAddress.getText().toString().replaceAll("\\s+", "");

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