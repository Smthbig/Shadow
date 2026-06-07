package com.smthbig.shadow.launcher.home;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

import com.smthbig.shadow.R;

public class AuraGradientView extends View {

    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ValueAnimator animator;
    private float phase = 0f;
    private int primaryColor = 0x1A58A6FF;
    private int secondaryColor = 0x0A3FB950;

    public AuraGradientView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        resolveColors();
        startAnimation();
    }

    private void resolveColors() {
        android.util.TypedValue tv = new android.util.TypedValue();
        if (getContext().getTheme().resolveAttribute(R.attr.fluxAuraPrimary, tv, true))
            primaryColor = tv.data;
        if (getContext().getTheme().resolveAttribute(R.attr.fluxAuraSecondary, tv, true))
            secondaryColor = tv.data;
    }

    private void startAnimation() {
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(6000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(a -> {
            phase = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) return;

        float cx = getWidth() * (0.3f + 0.4f * (float) Math.sin(phase * 2 * Math.PI));
        float cy = getHeight() * (0.2f + 0.3f * (float) Math.cos(phase * 1.3f * Math.PI));
        float radius = Math.max(getWidth(), getHeight()) * 0.9f;

        RadialGradient gradient = new RadialGradient(
                cx, cy, radius,
                new int[]{primaryColor, secondaryColor, Color.TRANSPARENT},
                new float[]{0f, 0.4f, 1f},
                Shader.TileMode.CLAMP
        );
        glowPaint.setShader(gradient);
        canvas.drawRect(0, 0, getWidth(), getHeight(), glowPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }
}
