package com.example.tassadar.runnergame;


public class GameData {
    public interface GameListener {
        void onCollision();
    };

    public static final class Block {
        float pos;
        float height;
    };

    public static final float PLAYER_POSITION = 0.33f;
    public static final float PLAYER_WIDTH = 1.f / 20.f;
    public static final float PLAYER_JUMP_MULT = 3.f;
    public static final float GROUND_BARS = 8;

    private static final float COLLISION_MIN = PLAYER_POSITION - PLAYER_WIDTH*1.45f;
    private static final float COLLISION_MAX = PLAYER_POSITION + PLAYER_WIDTH*0.45f;

    private static final float TIME_JUMP = 400.f;
    private static final float TIME_ONE_LENGTH = 1500.f;
    private static final float SPEEDUP_COEF = 0.05f / 5000.f;

    private static final float SPEED_JUMP = 1.f / (TIME_JUMP / 2.f);

    public float groundBarsOffset;
    public float jumpOffset;
    public Block[] blocks = new Block[(int)(TIME_ONE_LENGTH / (TIME_JUMP*2)) + 1];
    public double durationMs;
    public boolean collision;
    public float cloudsOffset;

    private float mJumpProgress;
    private int mJumpState;
    private float mBlockTimer;
    private int mBlockIdx;
    private float mTimeCoef;
    private GameListener mListener;
    private SoundManager mSoundMgr;
    private float mMusicRate;
    private int mMusicRateTimer;

    public synchronized void reset() {
        durationMs = 0;
        groundBarsOffset = 0.f;
        cloudsOffset = 0.f;
        jumpOffset = 0.f;
        collision = false;

        for (int i = 0; i < blocks.length; ++i) {
            blocks[i].pos = -1.f;
        }

        mBlockTimer = 2500;
        mJumpProgress = 0.f;
        mJumpState = 0;
        mBlockIdx = 0;
        mTimeCoef = 1.f;
        mMusicRate = 0.9f;
        mMusicRateTimer = 0;
    }

    public GameData(GameListener listener, SoundManager soundMgr) {
        mListener = listener;
        mSoundMgr = soundMgr;
        for(int i = 0; i < blocks.length; ++i) {
            blocks[i] = new Block();
        }
        reset();
    }

    public synchronized void resetIfColision() {
        if(collision) {
            reset();
        }
    }

    public float getSpeedPlayer() {
        return 1.f / (TIME_ONE_LENGTH * mTimeCoef);
    }

    private float getNextBlockDelay() {
        return TIME_JUMP * 2 +
                ((float) Math.random()) * (TIME_ONE_LENGTH*2*mTimeCoef);
    }

    private float getBlockHeight() {
        float min = 0.3f;
        float moving = PLAYER_JUMP_MULT*0.66f - min;
        return (float) (min + moving * (1.0f - (mTimeCoef*mTimeCoef)) * Math.random());
    }

    public synchronized void jump() {
        if(!collision && mJumpState == 0) {
            mSoundMgr.playJump();
            mJumpState = 1;
            mJumpProgress = 0.f;
        }
    }

    private void handleJump(float diffMs) {
        switch(mJumpState) {
            case 1:
                mJumpProgress += SPEED_JUMP * diffMs;
                jumpOffset = (1.f - (1.f - mJumpProgress) * (1.f - mJumpProgress));
                break;
            case 2:
                mJumpProgress += SPEED_JUMP * diffMs;
                jumpOffset = 1.f - mJumpProgress * mJumpProgress;
                break;
        }

        if(mJumpProgress >= 1.0f) {
            mJumpProgress = 0.f;
            ++mJumpState;
            if(mJumpState == 3) {
                mJumpState = 0;
                jumpOffset = 0;
            }
        }
    }

    public synchronized boolean update(float diffMs) {
        durationMs += diffMs;
        groundBarsOffset = (groundBarsOffset + getSpeedPlayer() * diffMs * (GROUND_BARS - 0.5f)) % 1.0f;
        cloudsOffset += getSpeedPlayer() * diffMs * 200;

        handleJump(diffMs);

        for (Block b : blocks) {
            if (b.pos > -0.1f) {
                b.pos -= getSpeedPlayer() * diffMs;
                if (b.pos > COLLISION_MIN && b.pos < COLLISION_MAX && jumpOffset*PLAYER_JUMP_MULT < b.height*0.95f) {
                    collision = true;
                }
            }
        }

        if(collision) {
            mSoundMgr.playDeath();
            mListener.onCollision();
            return true;
        }

        if (mBlockTimer <= diffMs) {
            blocks[mBlockIdx].pos = 1.f;
            blocks[mBlockIdx].height = getBlockHeight();
            mBlockIdx = (mBlockIdx + 1) % blocks.length;
            mBlockTimer = getNextBlockDelay();
        } else {
            mBlockTimer -= diffMs;
        }

        if (mMusicRateTimer <= diffMs) {
            mMusicRate *= 1.005f;
            if(mMusicRate > 2.f) {
                mMusicRate = 2.f;
                mMusicRateTimer = Integer.MAX_VALUE;
            } else {
                mMusicRateTimer = 1000;
            }
            mSoundMgr.setMusicRate(mMusicRate);
        } else {
            mMusicRateTimer -= diffMs;
        }

        mTimeCoef *= 1.f - SPEEDUP_COEF * diffMs;
        return collision;
    }
}