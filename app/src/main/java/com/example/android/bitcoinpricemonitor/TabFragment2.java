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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabFragment2 extends Fragment {

    View rootView;
    TableViewAdapter adapter;
    private SwipeRefreshLayout swipeContainer;

    public TabFragment2() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.tab_fragment2, container, false);

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Context context = getActivity();
                CharSequence text = "Refreshing bids data...";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                fetchUpdatedData();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        checkConnection();

        // call AsynTask to perform network operation on separate thread
        new HttpAsyncTask().execute("https://www.bitstamp.net/api/v2/order_book/btcusd/");

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

            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);
            } else {
                result = "Did not work!";
            }

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";

        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();

        return result;

    }

    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    private void checkConnection() {
        if(isConnected()){
            Log.d("TabFragment2", "Connected");
        } else {
            Log.d("TabFragment2", "NOT connected");
        }
    }

    public void fetchUpdatedData() {
        new HttpAsyncTask().execute("https://www.bitstamp.net/api/v2/order_book/btcusd/");
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            List<TransactionModel> data = new ArrayList<>();

            try {
                JSONObject json = new JSONObject(result);
                JSONArray bids = json.getJSONArray("bids");

                for (int i = 0; i < bids.length(); i++) {
                    int tempPrice = bids.getJSONArray(i).getInt(0);
                    double tempAmount = bids.getJSONArray(i).getDouble(1);
                    double tempAmountRoundOff = Math.round(tempAmount * 100000000.0) / 100000000.0;
                    double tempValue = tempPrice * tempAmount;
                    double tempValueRoundOff = Math.round(tempValue * 100.0) / 100.0;
                    data.add(new TransactionModel(tempPrice, tempAmountRoundOff, tempValueRoundOff));
                }

            } catch (JSONException e) {
                Log.e("TabFragment2", "unexpected JSON exception", e);
            }

            if (!swipeContainer.isRefreshing()) {
                initRecyclerView(data);
            } else {
                // clear out old items before appending in the new ones
                adapter.clear();
                // data has come back, add new items to your adapter...
                adapter.addAll(data);
                // call setRefreshing(false) to signal refresh has finished
                swipeContainer.setRefreshing(false);
            }
        }
    }

    private void initRecyclerView(List<TransactionModel> data) {
        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerViewDeliveryProductList);
        adapter = new TableViewAdapter(data);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }
}
