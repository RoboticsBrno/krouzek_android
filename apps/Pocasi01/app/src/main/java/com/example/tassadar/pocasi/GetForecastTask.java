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

public class GetForecastTask extends AsyncTask<Pair<Double, Double>, Void, JSONObject> {
    private static final String BASE_URL = "http://aladin.spekacek.com/meteorgram/endpoint-v2/getWeatherInfo";

    private WeakReference<MainActivity> m_activityReference;

    public GetForecastTask(MainActivity act) {
        m_activityReference = new WeakReference<>(act);
    }

    @Override
    protected JSONObject doInBackground(Pair... coord) {
        InputStream body = null;
        try {
            URL url = new URL(String.format("%s?latitude=%f&longitude=%f", BASE_URL,
                    coord[0].first, coord[0].second));
            URLConnection conn = url.openConnection();
            body = conn.getInputStream();

            int read;
            byte[] buf = new byte[4096];
            ByteArrayOutputStream downloaded = new ByteArrayOutputStream();
            while((read = body.read(buf)) > 0) {
                downloaded.write(buf, 0, read);
            }
            return new JSONObject(downloaded.toString("utf-8"));
        } catch(IOException ex) {
            ex.printStackTrace();
        } catch(JSONException ex) {
            ex.printStackTrace();
        } finally {
            closeStream(body);
        }
        return null;
    }

    protected void onPostExecute(JSONObject result) {
        MainActivity act = m_activityReference.get();
        if(act != null) {
            act.onForecastLoaded(result);
        }
    }

    private void closeStream(Closeable str) {
        if(str != null) {
            try {
                str.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
