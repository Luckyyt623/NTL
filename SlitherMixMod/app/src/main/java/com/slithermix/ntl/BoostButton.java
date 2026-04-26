package com.slithermix.ntl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * BoostButton - Circular boost button for slither.io mobile controls
 * Glows and pulses when pressed
 */
public class BoostButton extends View {

    public interface BoostListener {
        void onBoost(boolean pressed);
    }

    private BoostListener boostListener;

    private Paint bgPaint;
    private Paint glowPaint;
    private Paint borderPaint;
    private Paint textPaint;

    private boolean isPressed = false;
    private float animPulse = 0f;

    public BoostButton(Context context) {
        super(context);
        init();
    }

    public BoostButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);
        glowPaint.setColor(Color.argb(80, 255, 60, 60));
        glowPaint.setMaskFilter(new android.graphics.BlurMaskFilter(20f, android.graphics.BlurMaskFilter.Blur.NORMAL));

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3f);
        borderPaint.setColor(Color.argb(200, 255, 150, 150));

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(28f);
        textPaint.setShadowLayer(4f, 0f, 2f, Color.argb(180, 0, 0, 0));

        setLayerType(LAYER_TYPE_SOFTWARE, null); // needed for blur
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float r = Math.min(cx, cy) - 4f;

        float scale = isPressed ? 0.9f : 1.0f;
        canvas.save();
        canvas.scale(scale, scale, cx, cy);

        // Glow effect when pressed
        if (isPressed) {
            glowPaint.setColor(Color.argb(120, 255, 80, 80));
            canvas.drawCircle(cx, cy, r + 12f, glowPaint);
        }

        // Background gradient
        RadialGradient grad = new RadialGradient(
            cx - r * 0.2f, cy - r * 0.2f,
            r * 1.3f,
            new int[]{
                isPressed ? Color.argb(255, 200, 40, 40) : Color.argb(255, 255, 80, 80),
                isPressed ? Color.argb(230, 120, 10, 10) : Color.argb(230, 180, 20, 20)
            },
            new float[]{0f, 1f},
            Shader.TileMode.CLAMP
        );
        bgPaint.setShader(grad);
        canvas.drawCircle(cx, cy, r, bgPaint);

        // Border
        canvas.drawCircle(cx, cy, r, borderPaint);

        // Highlight sheen
        Paint sheenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sheenPaint.setColor(Color.argb(60, 255, 255, 255));
        sheenPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx - r * 0.2f, cy - r * 0.25f, r * 0.35f, sheenPaint);

        // Lightning bolt emoji text
        float textY = cy - ((textPaint.descent() + textPaint.ascent()) / 2f);
        canvas.drawText("⚡", cx, textY, textPaint);

        // "BOOST" label below icon
        Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.argb(200, 255, 255, 255));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(11f);
        canvas.drawText("BOOST", cx, cy + r * 0.58f, labelPaint);

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isPressed = true;
                invalidate();
                if (boostListener != null) boostListener.onBoost(true);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isPressed = false;
                invalidate();
                if (boostListener != null) boostListener.onBoost(false);
                return true;
        }
        return false;
    }

    public void setBoostListener(BoostListener listener) {
        this.boostListener = listener;
    }
}
