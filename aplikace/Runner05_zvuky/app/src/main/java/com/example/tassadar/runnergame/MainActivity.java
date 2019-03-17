package com.example.tassadar.runnergame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements GameData.GameListener {
    private GameData mData;
    private GameThread mThread;
    private GameView mGameView;
    private View mMenu;
    private TextView mMessage;
    private Button mBtnStart;
    private Button mBtnContinue;
    private Button mBtnRestart;

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

        mData = new GameData(this);

        mGameView = findViewById(R.id.gameView);
        mGameView.setData(mData);

        mThread = new GameThread(mData, mGameView);
        mThread.start();

        findViewById(R.id.btn_pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mData.collision) {
                    setPaused(!mThread.isPaused());
                }
            }
        });

        mMenu = findViewById(R.id.menu);
        mMessage = findViewById(R.id.message);
        mBtnStart = findViewById(R.id.btn_start);
        mBtnContinue = findViewById(R.id.btn_continue);
        mBtnRestart = findViewById(R.id.btn_restart);

        showMenu(true);
        mMessage.setText("Start new game?");
        mBtnRestart.setVisibility(View.GONE);
        mBtnContinue.setVisibility(View.GONE);
        mBtnStart.setVisibility(View.VISIBLE);

        mBtnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPaused(false);
            }
        });
    }

    protected void onPause() {
        super.onPause();
        setPaused(true);
    }

    protected void onDestroy() {
        super.onDestroy();
        mThread.interrupt();
    }

    private void showMenu(boolean show) {
        mMenu.setVisibility(show ? View.VISIBLE : View.GONE);
        mGameView.setAlpha(show ? 0.5f : 1.f);
    }

    private boolean setPaused(boolean paused) {
        if(!mThread.setPaused(paused))
            return false;
        showMenu(paused);
        if(paused) {
            mMessage.setText("Paused.");
            mBtnRestart.setVisibility(View.VISIBLE);
            mBtnContinue.setVisibility(View.VISIBLE);
            mBtnStart.setVisibility(View.GONE);
        }
        return true;
    }

    public void restartGame(View view) {
        if(setPaused(false)) {
            mData.reset();
        }
    }

    @Override
    public void onCollision() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showMenu(true);
                mMessage.setText(String.format("You lost after %.2fs!", mData.durationMs/1000));
                mBtnRestart.setVisibility(View.VISIBLE);
                mBtnContinue.setVisibility(View.GONE);
                mBtnStart.setVisibility(View.GONE);
            }
        });
    }
}
