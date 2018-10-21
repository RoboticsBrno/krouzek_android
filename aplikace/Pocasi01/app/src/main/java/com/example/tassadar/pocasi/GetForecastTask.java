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
        InputStream body = null;
        try {
            URL url = new URL(urls[0]);
            URLConnection conn = url.openConnection();
            body = conn.getInputStream();

            int read;
            byte[] buf = new byte[4096];
            ByteArrayOutputStream downloaded = new ByteArrayOutputStream();
            while((read = body.read(buf)) > 0) {
                downloaded.write(buf, 0, read);
            }
            return downloaded.toString("utf-8");
        } catch(IOException ex) {
            ex.printStackTrace();
        } finally {
            if(body != null) {
                try {
                    body.close();
                } catch (IOException ignored) { }
            }
        }
        return null;
    }

    protected void onPostExecute(String result) {
        m_activity.onForecastLoaded(result);
    }
}
