package com.smthbig.shadow.launcher.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Calendar;

public class DoomsdayView extends View {

    private Paint paintActive;
    private Paint paintInactive;
    private int dotCount = 7; // Seven day view
    private int activeDots = 0;

    public DoomsdayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paintActive = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintActive.setColor(0xCCFFFFFF);
        paintActive.setStyle(Paint.Style.FILL);

        paintInactive = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintInactive.setColor(0x33FFFFFF);
        paintInactive.setStyle(Paint.Style.FILL);

        updateTime();
    }

    public void updateTime() {
        Calendar calendar = Calendar.getInstance();
        activeDots = calendar.get(Calendar.DAY_OF_WEEK); // 1 = Sunday, 7 = Saturday
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float centerX = width / 2f;
        float centerY = height / 2f;

        float dotRadius = 8f;
        float spacing = 24f;

        float startX = centerX - ((dotCount - 1) * spacing) / 2f;

        for (int i = 0; i < dotCount; i++) {
            Paint p = (i < activeDots) ? paintActive : paintInactive;
            canvas.drawCircle(startX + (i * spacing), centerY, dotRadius, p);
        }
    }
}
