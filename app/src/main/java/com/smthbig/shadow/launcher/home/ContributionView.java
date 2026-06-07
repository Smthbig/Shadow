package com.smthbig.shadow.launcher.home;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import com.smthbig.shadow.R;
import com.smthbig.shadow.data.FocusStore;

import java.util.Calendar;

public class ContributionView extends View {

    private static final int WEEKS = 13;
    private static final int ROWS = 7;
    private static final int CELLS = WEEKS * ROWS;
    private static final int DEFAULT_LEVEL_COLOR = 0xFF161B22;
    private static final int DEFAULT_TODAY_COLOR = 0xFF58A6FF;

    private final Paint cellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint todayRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF cellRect = new RectF();

    private FocusStore focusStore;
    private int[] levels = new int[CELLS];

    private float cellSize;
    private float cellGap;
    private float labelWidth;
    private float topLabelHeight;
    private float cornerRadius;

    private int todayIndex = -1;
    private float ringProgress = 0f;
    private ValueAnimator pulseAnimator;
    private long lastRenderTime = 0;

    private final String[] dayLabels = {"", "Mon", "", "Wed", "", "Fri", ""};
    private final String[] monthNames = {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    public ContributionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        focusStore = new FocusStore(context);

        cellPaint.setStyle(Paint.Style.FILL);

        labelPaint.setColor(0xFF8B949E);
        labelPaint.setTextSize(dpToPx(9));
        labelPaint.setAntiAlias(true);

        todayRingPaint.setStyle(Paint.Style.STROKE);
        todayRingPaint.setStrokeWidth(dpToPx(2));
        todayRingPaint.setAntiAlias(true);

        startPulseAnimation();
        updateData();
    }

    private int getThemeAttrColor(int attr) {
        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(attr, tv, true)) {
            return tv.data;
        }
        return DEFAULT_LEVEL_COLOR;
    }

    public void updateData() {
        levels = focusStore.getLastWeeksData(WEEKS);
        computeTodayIndex();
        invalidate();
    }

    private void computeTodayIndex() {
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int rowIndex = (dayOfWeek + 5) % 7;
        todayIndex = CELLS - ROWS + rowIndex;
        if (todayIndex < 0 || todayIndex >= CELLS) todayIndex = CELLS - 1;
    }

    private void startPulseAnimation() {
        pulseAnimator = ValueAnimator.ofFloat(0f, 1f);
        pulseAnimator.setDuration(1500);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setInterpolator(new DecelerateInterpolator());
        pulseAnimator.addUpdateListener(a -> {
            ringProgress = (float) a.getAnimatedValue();
            long now = System.currentTimeMillis();
            if (now - lastRenderTime > 30) {
                lastRenderTime = now;
                invalidate();
            }
        });
        pulseAnimator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (width <= 0) {
            setMeasuredDimension(0, 0);
            return;
        }

        labelWidth = dpToPx(28);
        topLabelHeight = dpToPx(18);

        float availableWidth = width - labelWidth - getPaddingLeft() - getPaddingRight();
        cellGap = dpToPx(3);
        cellSize = (availableWidth - cellGap * (WEEKS - 1)) / WEEKS;
        cornerRadius = cellSize * 0.25f;

        int height = (int) (topLabelHeight + getPaddingTop() + getPaddingBottom()
                + ROWS * cellSize + (ROWS - 1) * cellGap);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (cellSize <= 0 || levels.length == 0) return;

        float startX = labelWidth + getPaddingLeft();
        float startY = topLabelHeight + getPaddingTop();

        drawMonthLabels(canvas, startX, startY);
        drawDayLabels(canvas, startY);
        drawCells(canvas, startX, startY);
        drawTodayHighlight(canvas, startX, startY);
    }

    private int[] getLevelColors() {
        return new int[]{
            getThemeAttrColor(R.attr.fluxLevel0),
            getThemeAttrColor(R.attr.fluxLevel1),
            getThemeAttrColor(R.attr.fluxLevel2),
            getThemeAttrColor(R.attr.fluxLevel3),
            getThemeAttrColor(R.attr.fluxLevel4)
        };
    }

    private void drawCells(Canvas canvas, float startX, float startY) {
        int[] levelColors = getLevelColors();
        for (int i = 0; i < levels.length; i++) {
            int col = i / ROWS;
            int row = i % ROWS;

            float x = startX + col * (cellSize + cellGap);
            float y = startY + row * (cellSize + cellGap);

            int level = Math.max(0, Math.min(4, levels[i]));
            if (level < 0 || level >= levelColors.length) {
                cellPaint.setColor(DEFAULT_LEVEL_COLOR);
            } else {
                cellPaint.setColor(levelColors[level]);
            }

            cellRect.set(x, y, x + cellSize, y + cellSize);
            canvas.drawRoundRect(cellRect, cornerRadius, cornerRadius, cellPaint);
        }
    }

    private void drawMonthLabels(Canvas canvas, float startX, float startY) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -(WEEKS * 7 - 1));

        int lastMonth = -1;

        for (int week = 0; week < WEEKS; week++) {
            int month = cal.get(Calendar.MONTH);
            if (month != lastMonth) {
                float x = startX + week * (cellSize + cellGap);
                canvas.drawText(monthNames[month], x, startY - dpToPx(4), labelPaint);
                lastMonth = month;
            }
            cal.add(Calendar.DAY_OF_YEAR, 7);
        }
    }

    private void drawDayLabels(Canvas canvas, float startY) {
        float x = dpToPx(4);
        for (int row = 0; row < ROWS; row++) {
            if (dayLabels[row].isEmpty()) continue;
            float y = startY + row * (cellSize + cellGap) + cellSize / 2 + labelPaint.getTextSize() / 3;
            canvas.drawText(dayLabels[row], x, y, labelPaint);
        }
    }

    private void drawTodayHighlight(Canvas canvas, float startX, float startY) {
        if (todayIndex < 0 || todayIndex >= levels.length) return;

        int col = todayIndex / ROWS;
        int row = todayIndex % ROWS;

        float cx = startX + col * (cellSize + cellGap) + cellSize / 2;
        float cy = startY + row * (cellSize + cellGap) + cellSize / 2;

        todayRingPaint.setColor(getThemeAttrColor(R.attr.fluxToday));

        float radius = cellSize / 2 + dpToPx(2) + ringProgress * dpToPx(3);
        int alpha = (int) (100 + (1 - ringProgress) * 155);
        todayRingPaint.setAlpha(alpha);
        canvas.drawCircle(cx, cy, radius, todayRingPaint);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
            pulseAnimator = null;
        }
    }
}
