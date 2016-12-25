package com.example.weather;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.weather.sync.WeatherSyncAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by 刘灿锐 on 2016/10/18 0018.
 */

class MyTask extends AsyncTask<String, Integer, String> {

    private Context mContext;

    MyTask(Context context) {
        mContext = context;
    }

    private final String LOG_TAG = WeatherSyncAdapter.class.getSimpleName();

    //onPreExecute方法用于在执行后台任务前做一些UI操作
    @Override
    protected void onPreExecute() {
        Log.i(LOG_TAG, "onPreExecute() called");

    }

    //doInBackground方法内部执行后台任务,不可在此方法内修改UI
    @Override
    protected String doInBackground(String... params) {
        String Translation = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr;

        String keyfrom = "Weather123456";
        String key = "1345103671";
        String type = "data";
        String doctype = "json";
        String version = "1.1";
        final String OWM_TRANSLATION = "translation";


        try {
            //checking the last update and notify if it' the first of the day
            final String FORECAST_BASE_URL =
                    "http://fanyi.youdao.com/openapi.do?";

            final String KEYFROM_PARAM = "keyfrom";
            final String KEY_PARAM = "key";
            final String TYPE_PARAM = "type";
            final String DOCTYPE_PARAM = "doctype";
            final String VERSION_PARAM = "version";
            final String Q_PARAM = "q";


            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(KEYFROM_PARAM, keyfrom)
                    .appendQueryParameter(KEY_PARAM, key)
                    .appendQueryParameter(TYPE_PARAM, type)
                    .appendQueryParameter(DOCTYPE_PARAM, doctype)
                    .appendQueryParameter(VERSION_PARAM, version)
                    .appendQueryParameter(Q_PARAM, params[0])
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }

            forecastJsonStr = buffer.toString();

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray translationArray = forecastJson.getJSONArray(OWM_TRANSLATION);
            Translation = translationArray.getString(0);

            Log.i("aaa", Translation);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return Translation;
    }

    //onProgressUpdate方法用于更新进度信息
    @Override
    protected void onProgressUpdate(Integer... progresses) {

    }

    //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
    @Override
    protected void onPostExecute(String result) {
        System.out.println(result);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(mContext.getString(R.string.pref_location_key), result);
        editor.commit();

        System.out.println(Utility.getPreferredLocation(mContext));
    }

    //onCancelled方法用于在取消执行中的任务时更改UI
    @Override
    protected void onCancelled() {

    }
}
