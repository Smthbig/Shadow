package com.smthbig.shadow.launcher.home;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

public class CircularProgressRingView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();

    private float progress = 0f;
    private float animatedProgress = 0f;
    private int ringColor = 0xFF58A6FF;
    private int trackColor = 0xFF21262D;
    private int textColor = 0xFFF0F6FC;
    private int mutedColor = 0xFF8B949E;
    private String valueText = "0";
    private String labelText = "";
    private float strokeWidth;
    private ValueAnimator animator;

    public CircularProgressRingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        float density = getResources().getDisplayMetrics().density;
        strokeWidth = density * 4;

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokeWidth);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setColor(trackColor);

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(ringColor);

        valuePaint.setAntiAlias(true);
        valuePaint.setColor(textColor);
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setFakeBoldText(true);

        labelPaint.setAntiAlias(true);
        labelPaint.setColor(mutedColor);
        labelPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setProgress(float p) {
        progress = Math.max(0f, Math.min(1f, p));
        animateToProgress(progress);
    }

    public void setRingColor(int color) {
        ringColor = color;
        progressPaint.setColor(color);
        invalidate();
    }

    public void setTrackColor(int color) {
        trackColor = color;
        trackPaint.setColor(color);
        invalidate();
    }

    public void setValueText(String text) {
        valueText = text;
        invalidate();
    }

    public void setLabelText(String text) {
        labelText = text;
        invalidate();
    }

    public void setTextColor(int color) {
        textColor = color;
        valuePaint.setColor(color);
        invalidate();
    }

    public void setMutedColor(int color) {
        mutedColor = color;
        labelPaint.setColor(color);
        invalidate();
    }

    private void animateToProgress(float target) {
        if (animator != null) animator.cancel();
        float start = animatedProgress;
        animator = ValueAnimator.ofFloat(start, target);
        animator.setDuration(800);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> {
            animatedProgress = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    public void setValues(String value, String label, float progress, int color) {
        valueText = value;
        labelText = label;
        ringColor = color;
        progressPaint.setColor(color);
        this.progress = Math.max(0f, Math.min(1f, progress));
        animateToProgress(this.progress);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = Math.min(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float padding = strokeWidth / 2f + getResources().getDisplayMetrics().density * 4;
        arcRect.set(padding, padding, w - padding, h - padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        canvas.drawArc(arcRect, -90f, 360f, false, trackPaint);
        if (animatedProgress > 0) {
            canvas.drawArc(arcRect, -90f, 360f * animatedProgress, false, progressPaint);
        }

        float density = getResources().getDisplayMetrics().density;
        valuePaint.setTextSize(density * 18);
        labelPaint.setTextSize(density * 8);

        float baseline = cy - (valuePaint.descent() + valuePaint.ascent()) / 2f;
        canvas.drawText(valueText, cx, baseline, valuePaint);

        float labelBaseline = cy + density * 10;
        canvas.drawText(labelText, cx, labelBaseline, labelPaint);
    }
}
