package com.ksheerasagara.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExpensePieChartView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();
    private final int[] colors = {
            Color.rgb(23, 107, 77),
            Color.rgb(242, 183, 5),
            Color.rgb(192, 57, 43),
            Color.rgb(54, 96, 146),
            Color.rgb(127, 140, 141)
    };
    private Map<String, Double> values = new LinkedHashMap<>();

    public ExpensePieChartView(Context context) {
        super(context);
        paint.setTextSize(30f);
    }

    public void setValues(Map<String, Double> values) {
        this.values = values;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float size = Math.min(getWidth(), getHeight()) * 0.72f;
        float left = (getWidth() - size) / 2f;
        float top = 20f;
        oval.set(left, top, left + size, top + size);

        double total = 0;
        for (double value : values.values()) {
            total += value;
        }

        if (total <= 0) {
            paint.setColor(Color.rgb(90, 100, 96));
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("No expenses this month", getWidth() / 2f, getHeight() / 2f, paint);
            return;
        }

        float start = -90f;
        int index = 0;
        for (Map.Entry<String, Double> entry : values.entrySet()) {
            paint.setColor(colors[index % colors.length]);
            float sweep = (float) (entry.getValue() / total * 360f);
            canvas.drawArc(oval, start, sweep, true, paint);
            start += sweep;
            index++;
        }

        float legendY = top + size + 42f;
        index = 0;
        paint.setTextAlign(Paint.Align.LEFT);
        for (Map.Entry<String, Double> entry : values.entrySet()) {
            paint.setColor(colors[index % colors.length]);
            canvas.drawRect(24f, legendY - 22f, 50f, legendY + 4f, paint);
            paint.setColor(Color.rgb(42, 52, 48));
            canvas.drawText(entry.getKey() + " - Rs " + Math.round(entry.getValue()), 62f, legendY, paint);
            legendY += 38f;
            index++;
        }
    }
}
