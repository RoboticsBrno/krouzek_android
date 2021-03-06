package com.example.tassadar.runnergame;

import android.view.View;

import java.util.concurrent.atomic.AtomicBoolean;

public class GameThread extends Thread {
    private static final int UPDATE_RATE = 60;
    private static final long OPTIMAL_PERIOD = 1_000_000_000 / UPDATE_RATE;

    private AtomicBoolean mPaused;
    private GameData mData;
    private View mGameView;

    public GameThread(GameData data, View gameView) {
        super();
        setDaemon(true);

        mData = data;
        mGameView = gameView;
        mPaused = new AtomicBoolean(true);
    }

    public void setPaused(boolean paused) {
        mPaused.set(paused);
    }

    public boolean isPaused() {
        return mPaused.get();
    }

    public void run() {
        long now, sleepMs;
        float diffMs;
        long previous = System.nanoTime();

        while(!this.isInterrupted()) {
            now = System.nanoTime();
            diffMs = (float)(now - previous) / 1_000_000;
            previous = now;

            if(!mPaused.get()) {
                if(mData.update(diffMs)) {
                    setPaused(true);
                }
                mGameView.postInvalidate();
            }

            sleepMs = Math.max(0, (previous - System.nanoTime() + OPTIMAL_PERIOD)) / 1_000_000;
            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
