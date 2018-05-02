package com.khushali.placessearch;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Set;

public class LoadData extends AsyncTask<Void, Void, String> {

    Callback mCallback;
    String url;
    HashMap<String, String> parameters;

    LoadData(String url, HashMap<String, String> parameters, Callback callback) {
        this.mCallback = callback;
        this.url = url;
        this.parameters = parameters;
    }

    @Override
    protected String doInBackground(Void... voids) {
        HttpURLConnection con = null;

        try {
            StringBuilder builtURL = new StringBuilder(url);
            String finalURL = builtURL.toString();

            if(parameters != null) {
                Set<String> keySet = parameters.keySet();
                builtURL.append("?");
                for (String key : keySet) {
                    builtURL.append(URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(parameters.get(key), "UTF-8") + "&");
                }
                finalURL = builtURL.substring(0, builtURL.length() - 1);
            }


            System.out.println(finalURL);

            URL obj = new URL(finalURL);
            con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            //System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            System.out.println(response.toString());
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (con != null)
                con.disconnect();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        mCallback.getData(s);
    }
}
