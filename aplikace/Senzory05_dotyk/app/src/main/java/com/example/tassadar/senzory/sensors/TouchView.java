package com.example.tassadar.senzory.sensors;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.tassadar.senzory.R;

import java.util.HashMap;
import java.util.Map;

public class TouchView extends View {
    private static final class Finger {
        Paint paint;
        int id;
        float x, y;
        float pressure;
    }

    private Paint mPaintCount;
    private Paint mPaintIndex;
    private Paint[] mPaintFingers;
    private Map<Integer, Finger> mFingers = new HashMap<>();
    private float mFingerRadius;

    public TouchView(Context context) {
        super(context);
        init();
    }

    public TouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TouchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaintCount = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintCount.setColor(Color.BLACK);
        mPaintCount.setTextAlign(Paint.Align.CENTER);
        mPaintCount.setLinearText(true);
        mPaintCount.setSubpixelText(true);
        mPaintCount.setTypeface(Typeface.MONOSPACE);

        mPaintIndex = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintIndex.setColor(Color.BLACK);
        mPaintIndex.setTextAlign(Paint.Align.CENTER);
        mPaintIndex.setLinearText(true);
        mPaintIndex.setSubpixelText(true);
        mPaintIndex.setTypeface(Typeface.MONOSPACE);

        TypedArray colors = getResources().obtainTypedArray(R.array.touch_fingers);
        mPaintFingers = new Paint[colors.length()];
        for(int i = 0; i < colors.length(); ++i) {
            mPaintFingers[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaintFingers[i].setColor(colors.getColor(i, 0));
            mPaintFingers[i].setAlpha(160);
            mPaintFingers[i].setStrokeWidth(3);
            mPaintFingers[i].setStyle(Paint.Style.FILL_AND_STROKE);
        }
        colors.recycle();
    }

    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        final int dim = Math.max(w, h);
        mPaintCount.setTextSize(dim / 10);
        mPaintIndex.setTextSize(dim / 20);

        mFingerRadius = Math.min(w, h)/10;
    }

    public boolean onTouchEvent(MotionEvent event) {
        int index = event.getActionIndex();
        int id = event.getPointerId(index);

        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                Finger finger = new Finger();
                finger.id = id;
                finger.paint = mPaintFingers[id % mPaintFingers.length];
                mFingers.put(id, finger);
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mFingers.remove(id);
                break;
        }

        for(Finger f  : mFingers.values()) {
            index = event.findPointerIndex(f.id);
            if(index != -1) {
                f.x = event.getX(index);
                f.y = event.getY(index);
                f.pressure = event.getPressure(index);
            }

        }

        invalidate();
        return true;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for(Finger f : mFingers.values()) {
            canvas.drawLine(0, f.y, getWidth(), f.y, f.paint);
            canvas.drawLine(f.x, 0, f.x, getHeight(), f.paint);

            float r = mFingerRadius * f.pressure;
            canvas.drawCircle(f.x, f.y, r, f.paint);

            canvas.drawText("" + (f.id + 1), f.x, f.y - r, mPaintIndex);
        }

        canvas.drawText("" + mFingers.size(), getWidth()/2, getHeight()*0.7f, mPaintCount);
    }
}
