package com.example.tassadar.runnergame;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

public class SoundManager implements SoundPool.OnLoadCompleteListener {
    private SoundPool mPool;
    private boolean mPaused;
    private int mIdMusic;
    private int mIdJump;
    private int mIdDeath;
    private int mStreamMusic;

    @Override
    public synchronized void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        if(sampleId == mIdMusic) {
            mStreamMusic = mPool.play(mIdMusic, 1.f, 1.f, 0, -1, 1.f);
            if(mPaused) {
                mPool.pause(mStreamMusic);
            }
        }
    }

    public synchronized void initialize(Context ctx) {
        if(mPool != null) {
            return;
        }

        mPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        mPool.setOnLoadCompleteListener(this);

        mIdMusic = mPool.load(ctx, R.raw.music, 1);
        mIdJump = mPool.load(ctx, R.raw.jump, 2);
        mIdDeath = mPool.load(ctx, R.raw.death, 3);

        mPaused = true;
    }

    public synchronized void release() {
        if(mPool != null) {
            mPool.release();
            mPool = null;
            mStreamMusic = 0;
        }
    }

    public synchronized void setPaused(boolean paused) {
        mPaused = paused;
        if(mStreamMusic == 0)
            return;
        if(paused) {
            mPool.pause(mStreamMusic);
        } else {
            mPool.resume(mStreamMusic);
        }
    }

    public synchronized void playJump() {
        if(mPool != null) {
            mPool.play(mIdJump, 0.6f, 0.6f, 0, 0, 1.f);
        }
    }

    public synchronized void playDeath() {
        if(mPool != null) {
            mPool.play(mIdDeath, 1.f, 1.f, 0, 0, 1.f);
        }
    }

    public synchronized void setMusicRate(float rate) {
        if(mStreamMusic != 0) {
            mPool.setRate(mStreamMusic, rate);
        }
    }
}
