package com.example.tassadar.senzory;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.tassadar.senzory.sensors.Compass;
import com.example.tassadar.senzory.sensors.Temperature;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavView;
    private BaseSensor mActiveSensor;

    private static Class<?>[] sensorClasses = new Class<?>[]{
            Compass.class,
            Temperature.class,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        mNavView = findViewById(R.id.nav_view);
        mNavView.setNavigationItemSelectedListener(this);

        Toolbar bar = findViewById(R.id.toolbar);
        setSupportActionBar(bar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        initSensors();

        if (savedInstanceState != null && savedInstanceState.containsKey("sensor")) {
            setActiveSensor(savedInstanceState.getString("sensor"));
        } else {
            SharedPreferences pref = getSharedPreferences("", MODE_PRIVATE);
            if(pref.contains("sensor")) {
                setActiveSensor(pref.getString("sensor", ""));
            }
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        mActiveSensor = null;
    }

    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        if (mActiveSensor != null)
            state.putString("sensor", mActiveSensor.getClass().getSimpleName());
    }

    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor e = getSharedPreferences("", MODE_PRIVATE).edit();
        if (mActiveSensor != null) {
            e.putString("sensor", mActiveSensor.getClass().getSimpleName());
        } else {
            e.remove("sensor");
        }
        e.apply();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setActiveSensor(int index) {
        String name = sensorClasses[index].getSimpleName();
        FragmentManager man = getSupportFragmentManager();
        BaseSensor fragment = (BaseSensor) man.findFragmentByTag(name);
        if (fragment == null || fragment == mActiveSensor)
            return;

        mNavView.getMenu().getItem(index).setChecked(true);

        FragmentTransaction tr = man.beginTransaction();
        if (mActiveSensor != null)
            tr.hide(mActiveSensor);
        tr.show(fragment);
        tr.commit();

        getSupportActionBar().setTitle(name);
        mActiveSensor = fragment;
    }

    private void setActiveSensor(String name) {
        for (int i = 0; i < sensorClasses.length; ++i) {
            if (sensorClasses[i].getSimpleName().equals(name)) {
                setActiveSensor(i);
                return;
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        mDrawerLayout.closeDrawers();
        setActiveSensor(menuItem.getItemId());
        return true;
    }

    private BaseSensor initOneSensor(FragmentManager man, FragmentTransaction tr, Class<?> cls) {
        try {
            BaseSensor res = (BaseSensor) man.findFragmentByTag(cls.getSimpleName());
            if (res != null)
                return res;

            res = (BaseSensor) cls.newInstance();
            tr.add(R.id.content_frame, res, cls.getSimpleName());
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean hasSensors(int[] types) {
        SensorManager man = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        for (int typ : types) {
            if (man.getDefaultSensor(typ) == null)
                return false;
        }
        return true;
    }

    private void initSensors() {
        Menu menu = mNavView.getMenu();

        int firstValidSensor = -1;

        FragmentManager man = getSupportFragmentManager();
        FragmentTransaction tr = man.beginTransaction();
        for (int i = 0; i < sensorClasses.length; ++i) {
            Class<?> cls = sensorClasses[i];
            BaseSensor s = initOneSensor(man, tr, cls);
            if (s == null) {
                continue;
            }

            tr.hide(s);

            MenuItem it = menu.add(1, i, i, cls.getSimpleName());
            it.setIcon(s.getIcon());

            if (!hasSensors(s.getRequiredSensors())) {
                it.setEnabled(false);
                tr.remove(s);
            } else if (firstValidSensor == -1) {
                firstValidSensor = i;
            }
        }
        tr.commitNow();

        menu.setGroupCheckable(1, true, true);

        if (firstValidSensor != -1){
            setActiveSensor(firstValidSensor);
        } else {
            findViewById(R.id.no_sensors).setVisibility(View.VISIBLE);
        }
    }

}
