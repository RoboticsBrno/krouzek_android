package com.example.tassadar.runnergame;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.text.DecimalFormat;
import java.text.FieldPosition;

public class GameView extends View {
    private Paint mPaintSky;
    private Paint mPaintGround;
    private Paint mPaintGroundBars;
    private Paint mPaintPlayer;
    private Paint mPaintBlock;

    private Rect mGround;
    private Rect[] mGroundBars;
    private Rect mPlayer;
    private float mGroundBarSpacing;
    private float mJumpHeight;
    private DecimalFormat mTimeFormat;
    private StringBuffer mTimeBuffer;
    private FieldPosition mTimeField;

    private GameData mData;

    private void init() {
        setClickable(true);

        mTimeFormat = new DecimalFormat("###0.00");
        mTimeBuffer = new StringBuffer(8);
        mTimeField = new FieldPosition(0);

        Resources res = getResources();

        mPaintSky = new Paint();
        mPaintSky.setColor(res.getColor(R.color.sky));

        mPaintGround = new Paint();
        mPaintGround.setColor(res.getColor(R.color.ground));

        mPaintGroundBars = new Paint();
        mPaintGroundBars.setColor(res.getColor(R.color.groundBar));

        mPaintPlayer = new Paint();
        mPaintPlayer.setColor(res.getColor(R.color.player));

        mPaintBlock = new Paint();
        mPaintBlock.setColor(res.getColor(R.color.block));
    }

    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setData(GameData data) {
        mData = data;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mData.jump();
        }
        return super.onTouchEvent(event);
    }

    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        final float split = 0.70f;
        mGround = new Rect(0, (int) (h*split), w, h);

        mPaintBlock.setTextSize(h/15);

        int playerW = (int) (w * GameData.PLAYER_WIDTH);
        mPlayer = new Rect(0, 0, playerW, playerW);
        mPlayer.offsetTo((int)(w*GameData.PLAYER_POSITION) - playerW/2,
                mGround.top - playerW);

        mGroundBars = new Rect[(int)GameData.GROUND_BARS];
        int barWidth = w / (mGroundBars.length*3);
        int barSpace = (w - barWidth*mGroundBars.length) / (mGroundBars.length - 1);
        int x = barSpace;
        mGroundBarSpacing = barWidth + barSpace;
        for(int i = 0; i < mGroundBars.length; ++i) {
            mGroundBars[i] = new Rect(x, mGround.top, x + barWidth, h);
            x += barWidth + barSpace;
        }

        mJumpHeight = mPlayer.height() * GameData.PLAYER_JUMP_MULT;
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int w = getWidth();

        canvas.drawColor(mPaintSky.getColor());
        canvas.drawRect(mGround, mPaintGround);

        float jumpOffset;
        double durationMs;
        synchronized (mData) {
            canvas.save();
            canvas.translate(-1 * mGroundBarSpacing * mData.groundBarsOffset, 0);
            for (Rect r : mGroundBars) {
                canvas.drawRect(r, mPaintGroundBars);
            }
            canvas.restore();

            for(GameData.Block b : mData.blocks) {
                if(b.pos <= -0.1f)
                    continue;
                float left = w * b.pos;
                float height = b.height * mPlayer.height();
                canvas.drawRect(left, mPlayer.bottom - height, left + mPlayer.width(), mPlayer.bottom, mPaintBlock);
            }

            durationMs = mData.durationMs;
            jumpOffset = -1 * mJumpHeight * mData.jumpOffset;
        }

        mTimeBuffer.setLength(0);
        mTimeFormat.format(durationMs / 1000, mTimeBuffer, mTimeField);
        canvas.drawText(mTimeBuffer.toString(),
                10, mPaintBlock.getTextSize()*2, mPaintBlock);

        canvas.save();
        canvas.translate(0, jumpOffset);
        canvas.drawRect(mPlayer, mPaintPlayer);
        canvas.restore();
    }
}
