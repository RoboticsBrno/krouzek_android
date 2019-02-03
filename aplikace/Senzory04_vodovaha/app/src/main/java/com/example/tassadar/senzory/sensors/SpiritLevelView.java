package com.example.tassadar.senzory.sensors;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.example.tassadar.senzory.R;

public class SpiritLevelView extends View {
    private static class Line {
        float[] coords;
        Line(float sx, float sy, float ex, float ey) {
            coords = new float[] { sx, sy, ex, ey };
        }
    }

    private static class Angle {
        float x, y;
        float degrees;
    }

    private Paint mPaintLine;
    private Paint mPaintWhite;
    private Paint mPaintPrimary;
    private Paint mPaintLeveled;
    private Paint mPaintText;
    private Line mLineHorizontal;
    private Line mLineVertical;
    private float mBubbleRadius;
    private float mLineRollDeg;
    private float mBubbleX, mBubbleY;
    private boolean mLeveledHor, mLeveledVer;

    private static final float LEVEL_TOLERANCE_RAD = 0.0174533f;

    private Angle[] mAngles = new Angle[3];
    private static final int YAW = 0;
    private static final int PITCH = 1;
    private static final int ROLL = 2;

    public SpiritLevelView(Context context) {
        super(context);
        init();
    }

    public SpiritLevelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SpiritLevelView(Context context, AttributeSet attrs, int def) {
        super(context, attrs, def);
        init();
    }

    private void init() {
        Resources res = getResources();

        mPaintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintLine.setColor(res.getColor(R.color.colorPrimaryDark));
        mPaintLine.setStrokeWidth(res.getDimensionPixelSize(R.dimen.spiritlevel_linewidth));
        mPaintLine.setStyle(Paint.Style.STROKE);

        mPaintWhite = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintWhite.setColor(Color.WHITE);

        mPaintPrimary = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintPrimary.setColor(res.getColor(R.color.colorPrimary));

        mPaintLeveled = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintLeveled.setColor(res.getColor(R.color.spiritlevel_leveled));
        mPaintLeveled.setStrokeWidth(res.getDimensionPixelSize(R.dimen.spiritlevel_linewidth));

        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintText.setTextAlign(Paint.Align.CENTER);
        mPaintText.setLinearText(true);
        mPaintText.setSubpixelText(true);
        mPaintText.setColor(Color.BLACK);
        mPaintText.setTextSize(res.getDimensionPixelSize(R.dimen.spiritlevel_textsize));
        mPaintText.setTypeface(Typeface.MONOSPACE);

        mBubbleRadius = res.getDimensionPixelSize(R.dimen.spiritlevel_bubbleradius);

        for(int i = 0; i < mAngles.length; ++i) {
            mAngles[i] = new Angle();
        }
    }

    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        int maxdim = Math.max(w, h);
        mLineHorizontal = new Line(-maxdim, 0, maxdim, 0);
        mLineVertical = new Line(0, -maxdim, 0, maxdim);

        float textX = w * 0.25f;
        float spacing = ((float)h) / 2 / (mAngles.length + 1);
        float textY = h/2 + spacing;
        for(Angle a : mAngles) {
            a.x = textX;
            a.y = textY;
            textY += spacing;
        }
    }

    private void drawAngle(Canvas canvas, Angle angle) {
        canvas.save();
        canvas.translate(angle.x, angle.y);
        canvas.rotate(mLineRollDeg);
        canvas.drawText(String.format("%3.1f°", angle.degrees), 0, 0, mPaintText);
        canvas.restore();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(getWidth()/2, getHeight()/2);
        canvas.rotate(mLineRollDeg);

        canvas.drawLines(mLineHorizontal.coords, mLeveledVer ? mPaintLeveled : mPaintLine);
        canvas.drawLines(mLineVertical.coords, mLeveledHor ? mPaintLeveled : mPaintLine);

        canvas.drawCircle(0, 0, mBubbleRadius, mPaintWhite);

        canvas.drawCircle(mBubbleX, mBubbleY, mBubbleRadius*0.90f,
                mLeveledHor && mLeveledVer ? mPaintLeveled : mPaintPrimary);

        canvas.drawCircle(0, 0, mBubbleRadius, mPaintLine);
        canvas.restore();

        for(Angle a: mAngles) {
            drawAngle(canvas, a);
        }
    }

    public void setOrientation(float yawRad, float pitchRad, float rollRad) {
        mAngles[YAW].degrees = yawRad * (float)(180.0 / Math.PI);
        mAngles[PITCH].degrees = pitchRad * (float)(180.0 / Math.PI);
        mAngles[ROLL].degrees = rollRad * (float)(180.0 / Math.PI);

        if(Math.abs(pitchRad) < 0.5f) { // vertical
            mLineRollDeg = mAngles[ROLL].degrees;

            mBubbleX = 0;
            mBubbleY = -1 * (pitchRad/((float)Math.PI/2)) * getHeight()/2;

            mLeveledHor = Math.abs(rollRad) < LEVEL_TOLERANCE_RAD ||
                    (Math.PI/2) - Math.abs(rollRad) < LEVEL_TOLERANCE_RAD;
            mLeveledVer = Math.abs(pitchRad) < LEVEL_TOLERANCE_RAD;
        } else { // horizontal
            mLineRollDeg = 0;
            mBubbleX = (rollRad/((float)Math.PI/2)) * getWidth()/2;
            mBubbleY = -1 * (yawRad/((float)Math.PI/2)) * getHeight()/2;

            mLeveledHor = Math.abs(rollRad) < LEVEL_TOLERANCE_RAD;
            mLeveledVer = Math.abs(yawRad) < LEVEL_TOLERANCE_RAD;
        }

        invalidate();
    }
}
