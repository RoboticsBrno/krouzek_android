package com.example.tassadar.runnergame;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements GameData.GameListener {
    private GameData mData;
    private GameThread mThread;
    private GameView mGameView;
    private SoundManager mSoundMgr;
    private View mMenu;
    private TextView mMessage;
    private TextView mScoreText;
    private Button mBtnStart;
    private Button mBtnContinue;
    private Button mBtnRestart;
    private ImageButton mBtnSounds;
    private boolean mEnableSounds;

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

        mSoundMgr = new SoundManager();
        mData = new GameData(this, mSoundMgr);

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
        mScoreText = findViewById(R.id.score_text);
        mBtnStart = findViewById(R.id.btn_start);
        mBtnContinue = findViewById(R.id.btn_continue);
        mBtnRestart = findViewById(R.id.btn_restart);
        mBtnSounds = findViewById(R.id.btn_sound);

        SharedPreferences pref = getSharedPreferences("", MODE_PRIVATE);
        if(mData.load(pref)) {
            setPaused(true);
        } else {
            showMenu(true);
            mMessage.setText("Start new game?");
            mBtnRestart.setVisibility(View.GONE);
            mBtnContinue.setVisibility(View.GONE);
            mBtnStart.setVisibility(View.VISIBLE);
        }

        mBtnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPaused(false);
            }
        });

        mBtnSounds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEnableSounds = !mEnableSounds;
                if(mEnableSounds) {
                    mSoundMgr.initialize(MainActivity.this);
                    mSoundMgr.setPaused(false);
                    mBtnSounds.setImageResource(R.drawable.ic_volume_on);
                } else {
                    mSoundMgr.release();
                    mBtnSounds.setImageResource(R.drawable.ic_volume_off);
                }
            }
        });

        mEnableSounds = pref.getBoolean("enableSounds", false);
        if(mEnableSounds) {
            mBtnSounds.setImageResource(R.drawable.ic_volume_on);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    protected void onStart() {
        super.onStart();
        if(mEnableSounds) {
            mSoundMgr.initialize(this);
        }
    }

    protected void onStop() {
        super.onStop();
        if(mEnableSounds) {
            mSoundMgr.release();
        }
    }

    protected void onPause() {
        super.onPause();
        setPaused(true);

        SharedPreferences.Editor edit = getSharedPreferences("", MODE_PRIVATE).edit();
        edit.putBoolean("enableSounds", mEnableSounds);
        mData.save(edit);
        edit.apply();
    }

    protected void onDestroy() {
        super.onDestroy();
        mThread.interrupt();
    }

    private void showMenu(boolean show) {
        mMenu.setVisibility(show ? View.VISIBLE : View.GONE);
        mGameView.setAlpha(show ? 0.5f : 1.f);
        mSoundMgr.setPaused(show);
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
            mScoreText.setVisibility(View.GONE);
        }
        return true;
    }

    public void restartGame(View view) {
        synchronized (mData) {
            if (setPaused(false)) {
                mData.reset();
            }
        }
    }

    @Override
    public void onCollision(final boolean newHighScore) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showMenu(true);
                mMessage.setText(String.format("You lost after %.2fs!", mData.durationMs/1000));
                if(newHighScore) {
                    mScoreText.setText("New high score!");
                } else {
                    mScoreText.setText(String.format("Your high score is %.2fs.", mData.bestDurationMs/1000));
                }
                mBtnRestart.setVisibility(View.VISIBLE);
                mBtnContinue.setVisibility(View.GONE);
                mBtnStart.setVisibility(View.GONE);
                mScoreText.setVisibility(View.VISIBLE);
            }
        });
    }
}
