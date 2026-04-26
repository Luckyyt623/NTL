package com.slithermix.ntl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * JoystickView - Mobile arrow/joystick control
 * Replicates the directional arrow system from the mobile slither SWF
 */
public class JoystickView extends View {

    public interface JoystickListener {
        void onJoystickMoved(float normX, float normY, boolean active);
    }

    private JoystickListener listener;

    // Drawing state
    private Paint basePaint;
    private Paint baseBorderPaint;
    private Paint knobPaint;
    private Paint knobHighlightPaint;
    private Paint arrowPaint;
    private Paint dirLinePaint;

    // Joystick state
    private float knobX, knobY;       // current knob position relative to center
    private boolean isActive = false;
    private int activeTouchId = -1;
    private float centerX, centerY;
    private float baseRadius;
    private float knobRadius;
    private float MAX_KNOB_DIST;

    public JoystickView(Context context) {
        super(context);
        init();
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Base circle (outer ring)
        basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        basePaint.setColor(Color.argb(30, 255, 255, 255));
        basePaint.setStyle(Paint.Style.FILL);

        baseBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        baseBorderPaint.setColor(Color.argb(120, 255, 255, 255));
        baseBorderPaint.setStyle(Paint.Style.STROKE);
        baseBorderPaint.setStrokeWidth(3f);

        // Knob
        knobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        knobPaint.setStyle(Paint.Style.FILL);

        knobHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        knobHighlightPaint.setColor(Color.argb(100, 255, 255, 255));
        knobHighlightPaint.setStyle(Paint.Style.FILL);

        // Arrows
        arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowPaint.setColor(Color.argb(180, 255, 255, 255));
        arrowPaint.setStyle(Paint.Style.FILL);

        // Direction line
        dirLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dirLinePaint.setColor(Color.argb(200, 100, 220, 255));
        dirLinePaint.setStyle(Paint.Style.STROKE);
        dirLinePaint.setStrokeWidth(3f);
        dirLinePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;
        baseRadius = Math.min(w, h) / 2f - 4f;
        knobRadius = baseRadius * 0.32f;
        MAX_KNOB_DIST = baseRadius * 0.58f;
        knobX = centerX;
        knobY = centerY;

        // Build knob gradient
        updateKnobGradient();
    }

    private void updateKnobGradient() {
        RadialGradient gradient = new RadialGradient(
            knobX - knobRadius * 0.3f,
            knobY - knobRadius * 0.3f,
            knobRadius * 1.4f,
            new int[]{Color.argb(255, 120, 220, 255), Color.argb(220, 30, 100, 200)},
            new float[]{0f, 1f},
            Shader.TileMode.CLAMP
        );
        knobPaint.setShader(gradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw base
        canvas.drawCircle(centerX, centerY, baseRadius, basePaint);
        canvas.drawCircle(centerX, centerY, baseRadius, baseBorderPaint);

        // Draw 4 directional arrows (mobile SWF style)
        float arrowOffset = baseRadius * 0.72f;
        float arrowSize = baseRadius * 0.18f;

        // UP arrow
        drawArrow(canvas, centerX, centerY - arrowOffset, 0, arrowSize);
        // DOWN arrow
        drawArrow(canvas, centerX, centerY + arrowOffset, 180, arrowSize);
        // LEFT arrow
        drawArrow(canvas, centerX - arrowOffset, centerY, 270, arrowSize);
        // RIGHT arrow
        drawArrow(canvas, centerX + arrowOffset, centerY, 90, arrowSize);

        // Draw direction line when active
        if (isActive) {
            float dx = knobX - centerX;
            float dy = knobY - centerY;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist > 5f) {
                // Gradient alpha along the line
                canvas.drawLine(centerX, centerY, knobX, knobY, dirLinePaint);
            }
        }

        // Draw knob with gradient
        float kx = isActive ? knobX : centerX;
        float ky = isActive ? knobY : centerY;

        // Update gradient to follow knob
        RadialGradient gradient = new RadialGradient(
            kx - knobRadius * 0.3f,
            ky - knobRadius * 0.3f,
            knobRadius * 1.4f,
            new int[]{Color.argb(255, 130, 230, 255), Color.argb(230, 30, 110, 210)},
            new float[]{0f, 1f},
            Shader.TileMode.CLAMP
        );
        knobPaint.setShader(gradient);
        canvas.drawCircle(kx, ky, knobRadius, knobPaint);

        // Knob highlight (top-left sheen)
        canvas.drawCircle(kx - knobRadius * 0.28f, ky - knobRadius * 0.28f, knobRadius * 0.35f, knobHighlightPaint);

        // Knob border
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.argb(180, 255, 255, 255));
        borderPaint.setStrokeWidth(2f);
        canvas.drawCircle(kx, ky, knobRadius, borderPaint);
    }

    /** Draw a triangular arrow at (x, y), rotated by degrees (0=up, 90=right, etc.) */
    private void drawArrow(Canvas canvas, float x, float y, float degrees, float size) {
        Path path = new Path();
        // Arrow pointing up before rotation
        path.moveTo(0, -size);
        path.lineTo(size * 0.7f, size * 0.5f);
        path.lineTo(-size * 0.7f, size * 0.5f);
        path.close();

        canvas.save();
        canvas.translate(x, y);
        canvas.rotate(degrees);
        canvas.drawPath(path, arrowPaint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(event.getActionIndex());

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (activeTouchId == -1) {
                    activeTouchId = pointerId;
                    isActive = true;
                    updateKnob(event, event.getActionIndex());
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (activeTouchId != -1) {
                    int idx = event.findPointerIndex(activeTouchId);
                    if (idx >= 0) {
                        updateKnob(event, idx);
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                if (pointerId == activeTouchId) {
                    activeTouchId = -1;
                    isActive = false;
                    knobX = centerX;
                    knobY = centerY;
                    invalidate();
                    if (listener != null) {
                        listener.onJoystickMoved(0f, 0f, false);
                    }
                }
                return true;
        }
        return false;
    }

    private void updateKnob(MotionEvent event, int pointerIndex) {
        float rawX = event.getX(pointerIndex);
        float rawY = event.getY(pointerIndex);

        float dx = rawX - centerX;
        float dy = rawY - centerY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        float clampedX, clampedY;
        if (dist > MAX_KNOB_DIST) {
            float scale = MAX_KNOB_DIST / dist;
            clampedX = dx * scale;
            clampedY = dy * scale;
        } else {
            clampedX = dx;
            clampedY = dy;
        }

        knobX = centerX + clampedX;
        knobY = centerY + clampedY;
        invalidate();

        // Normalize to -1..1
        float normX = clampedX / MAX_KNOB_DIST;
        float normY = clampedY / MAX_KNOB_DIST;
        if (listener != null) {
            listener.onJoystickMoved(normX, normY, true);
        }
    }

    public void setListener(JoystickListener listener) {
        this.listener = listener;
    }
}
