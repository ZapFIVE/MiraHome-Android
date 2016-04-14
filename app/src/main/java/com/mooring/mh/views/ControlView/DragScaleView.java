package com.mooring.mh.views.ControlView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.mooring.mh.R;
import com.mooring.mh.utils.CommonUtils;

/**
 * 自定义可拖动改变图层控件
 * <p/>
 * Created by Will on 16/4/8.
 */
public class DragScaleView extends View implements View.OnTouchListener {
    private int lastY;
    private int oriTop;//拖动变量
    private boolean dragDirection;//是否符合拖动位置

    private Paint colorPaint;//上层颜色画笔
    private Bitmap drop;//拖动图标
    private Paint tickMarkPaint;//刻度线画笔
    private Paint scalePaint;//刻度圆圈
    private Paint maskPaint;//遮罩层

    private int dropW = 0;//拖动小球宽度
    private int dropH = 0;//拖动小球高度

    private int upperBound = 100;//温度上界
    private int lowerBound = 68;//温度下界
    private String bedTemperature = "83℉";//床的温度
    private String currTemperature = "86℉";//当前温度

    /*针对文本居中显示*/
    private Rect targetRect;
    private Paint commonPaint;
    private Paint.FontMetricsInt fontMetrics;

    private int viewW = 0;//当前View的宽度
    private int viewH = 0;//当前View的高度
    private boolean isDropAble = true;//是否可用且可拖动

    private OnDropListener listener;//拖动监听

    public DragScaleView(Context context) {
        this(context, null);
    }

    public DragScaleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragScaleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnTouchListener(this);
        initScreen();
    }


    private int lineTop = CommonUtils.dp2px(this.getContext(), 40);
    private int lineBottom = CommonUtils.dp2px(this.getContext(), 150);
    private int tempTop = CommonUtils.dp2px(this.getContext(), 20);
    private int tempBottom = CommonUtils.dp2px(this.getContext(), 130);
    private int tvHeight = CommonUtils.dp2px(this.getContext(), 40);
    private int tvWidth = CommonUtils.dp2px(this.getContext(), 50);
    private int scaleRadius = CommonUtils.dp2px(this.getContext(), 4);
    private int dropTvSize = CommonUtils.sp2px(this.getContext(), 50);
    private int tempTvSize = CommonUtils.sp2px(this.getContext(), 22);
    private int bedTvSize = CommonUtils.sp2px(this.getContext(), 14);


    /**
     * 初始化
     */
    private void initScreen() {

        colorPaint = new Paint();
        colorPaint.setColor(getResources().getColor(R.color.colorPurple));
        colorPaint.setStyle(Paint.Style.FILL);

        drop = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_adjust_temp);
        dropW = drop.getWidth();
        dropH = drop.getHeight();

        tickMarkPaint = new Paint();
        tickMarkPaint.setColor(Color.WHITE);
        tickMarkPaint.setStyle(Paint.Style.STROKE);
        tickMarkPaint.setStrokeWidth(5);

        scalePaint = new Paint();
        scalePaint.setColor(Color.WHITE);
        scalePaint.setStyle(Paint.Style.FILL);
        scalePaint.setAntiAlias(true);

        commonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        commonPaint.setStrokeWidth(3);

        maskPaint = new Paint();
        maskPaint.setStyle(Paint.Style.FILL);
        maskPaint.setColor(getResources().getColor(R.color.transparent_6));

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        oriTop = 10;

        viewW = w;
        viewH = h;

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //温度text
        drawText(canvas, currTemperature, dropTvSize, 0X7FFFFFFF, oriTop, viewW * 2 / 3 + dropW / 2);

        //颜色变动填充层
        canvas.drawRect(0, lineTop + dropH / 2 + oriTop, viewW, viewH, colorPaint);

        //拖动图标
        canvas.drawBitmap(drop, viewW * 2 / 3, lineTop + oriTop, null);

        //绘制遮罩层
        if (!isDropAble) {
            canvas.drawRect(0, 0, viewW, viewH, maskPaint);
        }

        //刻度直线
        canvas.drawLine(viewW / 4, lineTop + dropH / 2, viewW / 4, viewH - lineBottom, tickMarkPaint);

        int ss = (viewH - lineTop - lineBottom - dropH / 2) / 10;
        for (int i = 0; i <= 10; i++) {
            //刻度
            canvas.drawCircle(viewW / 4, lineTop + dropH / 2 + ss * i, scaleRadius, scalePaint);
        }

        //温度上界
        drawText(canvas, upperBound + "℉", tempTvSize, Color.WHITE, tempTop, viewW / 4);

        //温度下界
        drawText(canvas, lowerBound + "℉", tempTvSize, Color.WHITE, viewH - tempBottom, viewW / 4);

        //bed温度
        drawText(canvas, "Bed " + bedTemperature, bedTvSize, Color.WHITE, viewH - tempBottom, viewW * 3 / 4);

    }

    /**
     * 公用绘制文本方法
     *
     * @param canvas    画布
     * @param text      文本
     * @param textSize  文本字体大小
     * @param textColor 文本字体颜色
     * @param top       文本 top 的坐标
     * @param left      文本 left 的坐标
     */
    private void drawText(Canvas canvas, String text, int textSize, int textColor, int top, int left) {
        targetRect = new Rect(left - tvWidth, top, left + tvWidth, top + tvHeight);
        commonPaint.setTextSize(textSize);
        commonPaint.setColor(Color.TRANSPARENT);
        canvas.drawRect(targetRect, commonPaint);
        commonPaint.setColor(textColor);
        fontMetrics = commonPaint.getFontMetricsInt();
        int baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
        // 下面这行是实现水平居中，drawText对应改为传入targetRect.centerX()
        commonPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(text, targetRect.centerX(), baseline, commonPaint);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:

                lastY = (int) event.getRawY();
                dragDirection = getDirection((int) event.getX(),
                        (int) event.getY());

                break;
            case MotionEvent.ACTION_MOVE:
                //可执行拖动的范围

                int dy = (int) event.getRawY() - lastY;

                if (dragDirection && isDropAble) {

                    oriTop += dy;

                    if (oriTop >= 0 && oriTop <= viewH - lineTop - lineBottom - dropH / 2) {
                        currTemperature = computeTemp() + "℉";
                        invalidate();
                    }
                }

                lastY = (int) event.getRawY();

                break;
            case MotionEvent.ACTION_UP:
                dragDirection = false;
                if (oriTop < 0) {
                    oriTop = 0;

                    currTemperature = computeTemp() + "℉";
                    invalidate();
                }

                if (oriTop > viewH - lineTop - lineBottom - dropH / 2) {
                    oriTop = viewH - lineTop - lineBottom - dropH / 2;

                    currTemperature = computeTemp() + "℉";
                    invalidate();
                }

                if (listener != null) {
                    listener.onDrop(currTemperature);
                }
                break;
        }

        return false;
    }

    /**
     * 计算当前温度
     *
     * @return 当前温度
     */
    private int computeTemp() {
        return upperBound - (upperBound - lowerBound) * oriTop / (viewH - lineTop - lineBottom - dropH / 2);
    }

    /**
     * 判定是否拖动小球的位置
     *
     * @param x getX()
     * @param y getY()
     * @return 是否在小球得x, y位置
     */
    protected boolean getDirection(int x, int y) {

        if (y > lineTop + oriTop && y < lineTop + oriTop + dropH) {
            if (x > viewW * 2 / 3 && x < viewW * 2 / 3 + dropW) {
                return true;
            }
        }
        return false;
    }


    /**
     * 设置温度的上下界
     *
     * @param upperBound
     * @param lowerBound
     */
    public void setUpperAndLowerBound(int upperBound, int lowerBound) {
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;

        invalidate();
    }

    /**
     * 设置bed的温度
     *
     * @param bedTemperature
     */
    public void setBedTemperature(String bedTemperature) {
        this.bedTemperature = bedTemperature;
        invalidate();
    }

    /**
     * 设置当前的温度
     *
     * @param currTemperature 70℉
     */
    public void setCurrTemperature(String currTemperature) {
        this.currTemperature = currTemperature;
        int temp = Integer.parseInt(currTemperature.substring(0, currTemperature.length() - 1));
        oriTop = (upperBound - temp) * (viewH - lineTop - lineBottom - dropH / 2) / (upperBound - lowerBound);
        invalidate();
    }

    /**
     * 设置当前的温度
     *
     * @param currTemp 70
     */
    public void setCurrTemperature(int currTemp) {

        this.currTemperature = currTemp + "℉";
        oriTop = (upperBound - currTemp) * (viewH - lineTop - lineBottom - dropH / 2) / (upperBound - lowerBound);

        invalidate();
    }

    /**
     * 设置是否可拖动
     *
     * @param isDropAble
     */
    public void setIsDropAble(boolean isDropAble) {
        this.isDropAble = isDropAble;
        invalidate();
    }

    public interface OnDropListener {
        void onDrop(String currTemp);
    }

    /**
     * 设置拖动监听,松开手执行
     *
     * @param listener
     */
    public void setOnDropListener(OnDropListener listener) {
        this.listener = listener;
    }
}