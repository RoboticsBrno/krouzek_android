package com.example.tassadar.runnergame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private GameData mData;
    private GameThread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN);

        mData = new GameData();

        GameView view = findViewById(R.id.gameView);
        view.setData(mData);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mThread.isPaused()) {
                    mThread.setPaused(false);
                }
            }
        });

        mThread = new GameThread(mData, view);
        mThread.start();
        mThread.setPaused(false);
    }

    protected void onPause() {
        super.onPause();
        mThread.setPaused(true);
    }

    protected void onDestroy() {
        super.onDestroy();
        mThread.interrupt();
    }
}
