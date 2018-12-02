package com.example.tassadar.pocasi;

import android.os.AsyncTask;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;

public class GetForecastTask extends AsyncTask<String, Void, String> {
    private MainActivity m_activity;

    public GetForecastTask(MainActivity act) {
        m_activity = act;
    }

    @Override
    protected String doInBackground(String... urls) {
        try {
            URL url = new URL(urls[0]);
            URLConnection conn = url.openConnection();

            InputStream body = conn.getInputStream();

            byte[] buffer = new byte[4096];
            ByteArrayOutputStream downloaded = new ByteArrayOutputStream();

            while(true) {
                int readCount = body.read(buffer);
                if(readCount <= 0)
                    break;
                downloaded.write(buffer, 0, readCount);
            }

            body.close();

            return downloaded.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(String result) {
        m_activity.onForecastLoaded(result);
    }
}
