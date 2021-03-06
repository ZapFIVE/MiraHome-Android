package com.mooring.mh.views.LoadingDots;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

/**
 *
 *
 * Created by Will on 16/6/23.
 */
public class JumpingSpan extends ReplacementSpan {

    private float translationX = 0;
    private float translationY = 0;

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return (int) paint.measureText(text, start, end);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top,
                     int y, int bottom, Paint paint) {
        canvas.drawText(text, start, end, x + translationX, y + translationY, paint);
    }

    public void setTranslationX(float translationX) {
        this.translationX = translationX;
    }

    public void setTranslationY(float translationY) {
        this.translationY = translationY;
    }
}
