package com.example.tassadar.runnergame;

public class GameData {
    public static final float PLAYER_POSITION = 0.33f;
    public static final float PLAYER_WIDTH = 1.f / 20.f;
    public static final float PLAYER_JUMP_MULT = 3.f;

    private static final float COLLISION_MIN = PLAYER_POSITION - PLAYER_WIDTH*0.9f;
    private static final float COLLISION_MAX = PLAYER_POSITION + PLAYER_WIDTH*0.9f;

    private static final float TIME_JUMP = 300.f;
    private static final float TIME_ONE_LENGTH = 1000.f;
    private static final float TIME_MIN_BLOCK_DELAY = TIME_JUMP * 2;
    private static final float TIME_MAX_BLOCK_DELAY = 2000.f;

    private static final float SPEED_PLAYER = 1.f / TIME_ONE_LENGTH;
    private static final float SPEED_JUMP = 1.f / (TIME_JUMP / 2.f);

    public float groundBarsOffset;
    public float jumpOffset;
    public float[] blocks = new float[(int)(TIME_ONE_LENGTH / TIME_MIN_BLOCK_DELAY) + 1];
    public double durationMs;
    public boolean collision;

    private float mJumpProgress;
    private int mJumpState;
    private float mBlockTimer;
    private int mBlockIdx;

    public GameData() {
        reset();
    }

    public synchronized void reset() {
        durationMs = 0;
        groundBarsOffset = 0.f;
        jumpOffset = 0.f;
        collision = false;

        for (int i = 0; i < blocks.length; ++i) {
            blocks[i] = 0;
        }

        mBlockTimer = 5000;
        mJumpProgress = 0.f;
        mJumpState = 0;
        mBlockIdx = 0;
    }

    private void handleJump(float diffMs) {
        switch(mJumpState) {
            case 1:
                mJumpProgress += SPEED_JUMP * diffMs;
                jumpOffset = (float) ((Math.cos((mJumpProgress + 1) * Math.PI) / 2.f) + 0.5);
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
        groundBarsOffset = (groundBarsOffset + SPEED_PLAYER * diffMs * 7.5f) % 1.0f;

        handleJump(diffMs);

        for(int i = 0; i < blocks.length; ++i) {
            if(blocks.length > 0.f) {
                blocks[i] -= SPEED_PLAYER * diffMs;
                if(blocks[i] > COLLISION_MIN && blocks[i] < COLLISION_MAX && jumpOffset < 1.f/PLAYER_JUMP_MULT) {
                    collision = true;
                }
            }
        }

        if(mBlockTimer <= diffMs) {
            blocks[mBlockIdx] = 1.f;
            mBlockIdx = (mBlockIdx + 1) % blocks.length;
            mBlockTimer = TIME_MIN_BLOCK_DELAY + ((float)Math.random())*TIME_MAX_BLOCK_DELAY;
        } else {
            mBlockTimer -= diffMs;
        }
        return collision;
    }

    public synchronized  void jump() {
        if(collision) {
            reset();
        } else if(mJumpState == 0) {
            mJumpState = 1;
            mJumpProgress = 0.f;
        }
    }
}
