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

    private float cachedSpacing = 0;
    private float cachedRadius = 0;
    private int cachedCols = 0;

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
        int active = store.getActiveColor();
        int inactive = store.getInactiveColor();

        if (active == 0) active = getThemeColor(com.smthbig.shadow.R.attr.doomsdayActive);
        if (inactive == 0) inactive = getThemeColor(com.smthbig.shadow.R.attr.doomsdayInactive);

        paintActive.setColor(active);
        paintInactive.setColor(inactive);

        Calendar calendar = Calendar.getInstance();
        DoomsdayStore.Scale scale = store.getScale();

        int prevTotal = totalDots;

        switch (scale) {
            case WEEK:
                totalDots = 7;
                int dow = calendar.get(Calendar.DAY_OF_WEEK);
                activeDots = ((dow + 5) % 7) + 1;
                break;
            case MONTH:
                totalDots = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                activeDots = calendar.get(Calendar.DAY_OF_MONTH);
                break;
            case YEAR:
                totalDots = calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
                activeDots = calendar.get(Calendar.DAY_OF_YEAR);
                break;
            case CUSTOM:
                totalDots = Math.max(1, store.getCustomDays());
                int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
                int daysInYear = calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
                activeDots = Math.max(1, (int) ((float) dayOfYear / daysInYear * totalDots));
                break;
        }

        if (totalDots != prevTotal) {
            requestLayout();
        } else {
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (width == 0 || totalDots <= 0) {
            setMeasuredDimension(0, 0);
            return;
        }

        cachedCols = (totalDots > 14) ? 20 : totalDots;
        int rows = (int) Math.ceil((float) totalDots / cachedCols);

        cachedSpacing = (float) width / (cachedCols + 1);
        cachedRadius = cachedSpacing / 3.5f;

        int desiredHeight = (int) ((cachedRadius * 3) + (rows * cachedRadius * 3.5f));
        setMeasuredDimension(width, desiredHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (totalDots == 0 || cachedSpacing == 0) return;

        for (int i = 0; i < totalDots; i++) {
            int row = i / cachedCols;
            int col = i % cachedCols;

            float x = cachedSpacing + (col * cachedSpacing);
            float y = (cachedRadius * 3) + (row * cachedRadius * 3.5f);

            Paint p = (i < activeDots) ? paintActive : paintInactive;
            canvas.drawCircle(x, y, cachedRadius, p);
        }
    }

    private int getThemeColor(int attr) {
        android.util.TypedValue typedValue = new android.util.TypedValue();
        if (getContext().getTheme().resolveAttribute(attr, typedValue, true)) {
            return typedValue.data;
        }
        return android.graphics.Color.GRAY;
    }
}
