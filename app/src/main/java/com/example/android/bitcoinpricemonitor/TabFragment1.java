/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bitcoinpricemonitor;


import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabFragment1 extends Fragment {


    public TabFragment1() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.tab_fragment1, container, false);

        Button mButton = (Button) rootView.findViewById(R.id.angry_btn);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getActivity();
                CharSequence text = "Refreshing history data...";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                checkConnection();
                new HttpAsyncTask().execute("https://www.bitstamp.net/api/v2/transactions/btcusd/");

            }
        });

        checkConnection();

        // call AsynTask to perform network operation on separate thread
        new HttpAsyncTask().execute("https://www.bitstamp.net/api/v2/transactions/btcusd/");

        // Inflate the layout for this fragment.
        return rootView;
    }

    public static String GET(String urlReceived){
        URL url;
        HttpURLConnection urlConnection = null;

        InputStream inputStream = null;
        String result = "";

        try {

            url = new URL(urlReceived);

            urlConnection = (HttpURLConnection) url
                    .openConnection();

            inputStream = urlConnection.getInputStream();

//            InputStreamReader isw = new InputStreamReader(in);
//
//            // create HttpClient
//            HttpClient httpclient = new DefaultHttpClient();
//
//            // make GET request to the given URL
//            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
//
//            // receive response as inputStream
//            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);
            } else {
                result = "Did not work!";
            }

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        Log.d("zachary GET result", result);

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();

        Log.d("zachary convert", result);

        return result;

    }

    private boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    private void checkConnection() {
        if(isConnected()){
            Log.d("TabFragment1", "Connected");
        } else {
            Log.d("TabFragment1", "NOT connected");
        }
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            Log.d("zachary onPostExecute", result);

            LineChart lineChart = (LineChart) getView().findViewById(R.id.chart);
            // creating list of entry
            ArrayList<Entry> entries = new ArrayList<>();
            // creating labels
            ArrayList<String> labels = new ArrayList<>();

            Float sumPrice = 0f;
            Float avgPrice = 0f;
            Float minPrice = Float.MAX_VALUE;
            Float maxPrice = Float.MIN_VALUE;

            try {
                JSONArray jsonArray = new JSONArray(result);

                for (int i = jsonArray.length() - 1; i >= 0; i--) {
                    String priceString = jsonArray.getJSONObject(i).getString("price");
                    Float price = Float.parseFloat(priceString);
                    sumPrice += price;
                    if (price < minPrice) {
                        minPrice = price;
                    }
                    if (price > maxPrice) {
                        maxPrice = price;
                    }
                    entries.add(new Entry(price, jsonArray.length() - 1 - i));
                    String date = jsonArray.getJSONObject(i).getString("date");

                    long batch_date = Long.parseLong(date);
                    Date dt = new Date (batch_date * 1000);

                    SimpleDateFormat sfd = new SimpleDateFormat("HH:mm:ss");

                    labels.add(sfd.format(dt));
                }

                avgPrice = sumPrice / jsonArray.length();

                System.out.println("zachary avgPrice");
                System.out.println(avgPrice);

            } catch (JSONException e) {
                Log.e("TabFragment1", "unexpected JSON exception", e);
            }

            LineDataSet dataset = new LineDataSet(entries, "Data entries");

            LineData data = new LineData(labels, dataset);

            lineChart.setData(data); // set the data and list of lables into chart
            // remove legend
            lineChart.getLegend().setEnabled(false);

            lineChart.setDescription("Price info in the past 1 hr");
            // make curve smooth
            dataset.setDrawCubic(true);
            //to remove the cricle from the graph
            dataset.setDrawCircles(false);
            // set line color
            dataset.setColor(Color.rgb(0, 255, 0));

            dataset.setDrawFilled(true);

            dataset.setFillColor(Color.rgb(0, 200, 0));
            // to make the swipe between tabs smooth
            lineChart.setDragEnabled(false);
            // remove two lines showing the touching position
            lineChart.getData().setHighlightEnabled(false);
            // reset the zoom scale
            lineChart.fitScreen();
            // zoom in
            lineChart.zoom(0f, minPrice/1000f, 0f, minPrice);
            // refresh
            lineChart.invalidate();
        }
    }

}
