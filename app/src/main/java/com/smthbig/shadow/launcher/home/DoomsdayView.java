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
    private DoomsdayStore store;
    
    private int totalDots = 0;
    private int activeDots = 0;

    public DoomsdayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        store = new DoomsdayStore(context);
        init();
    }

    private void init() {
        paintActive = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintActive.setStyle(Paint.Style.FILL);

        paintInactive = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintInactive.setStyle(Paint.Style.FILL);

        updateState();
    }

    public void updateState() {
        paintActive.setColor(store.getActiveColor());
        paintInactive.setColor(store.getInactiveColor());

        Calendar calendar = Calendar.getInstance();
        DoomsdayStore.Scale scale = store.getScale();

        switch (scale) {
            case WEEK:
                totalDots = 7;
                activeDots = calendar.get(Calendar.DAY_OF_WEEK);
                break;
            case MONTH:
                totalDots = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                activeDots = calendar.get(Calendar.DAY_OF_MONTH);
                break;
            case YEAR:
                totalDots = calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
                activeDots = calendar.get(Calendar.DAY_OF_YEAR);
                break;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        if (w == 0 || h == 0) return;

        // Grid calculation
        int cols = (store.getScale() == DoomsdayStore.Scale.YEAR) ? 20 : totalDots;
        int rows = (int) Math.ceil((float) totalDots / cols);

        float spacing = w / (cols + 1);
        float radius = Math.min(spacing / 3f, h / (rows * 2.5f));

        for (int i = 0; i < totalDots; i++) {
            int row = i / cols;
            int col = i % cols;

            float x = spacing + (col * spacing);
            float y = (radius * 2) + (row * radius * 3);

            Paint p = (i < activeDots) ? paintActive : paintInactive;
            canvas.drawCircle(x, y, radius, p);
        }
    }
}
