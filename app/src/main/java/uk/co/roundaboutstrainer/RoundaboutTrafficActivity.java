package uk.co.roundaboutstrainer;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

/**
 * Adds one extra, clearly visible vehicle on the circulating carriageway
 * without touching the master roundabout image, lane markings or traced routes.
 */
public class RoundaboutTrafficActivity extends TracedSimulationActivity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        addContentView(
                new CentreTrafficOverlay(this),
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
    }

    static final class CentreTrafficOverlay extends View {
        private static final float MASTER_ASPECT = 1536f / 1152f;
        private static final float CENTRE_X = .470f;
        private static final float CENTRE_Y = .424f;
        private static final float INNER_SCALE = .80f;

        // Same circulating shape as the simulator traffic, pulled inward so this
        // vehicle is unmistakably "inside" the roundabout on the inner lane.
        private static final float[][] LOOP = {
                {.343f,.633f},{.285f,.530f},{.245f,.405f},{.247f,.300f},
                {.325f,.215f},{.445f,.178f},{.565f,.178f},{.655f,.230f},
                {.690f,.325f},{.684f,.445f},{.646f,.555f},{.575f,.632f},
                {.470f,.670f},{.385f,.662f}
        };

        private final RectF imageRect = new RectF();
        private final Path innerLoop = new Path();
        private final PathMeasure pm = new PathMeasure();
        private final float[] pos = new float[2];
        private final float[] tan = new float[2];

        private final Paint body = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint glass = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint tyres = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint lights = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint rearLights = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint shadow = new Paint(Paint.ANTI_ALIAS_FLAG);

        private float phase = .12f;
        private final ValueAnimator animator;

        CentreTrafficOverlay(Context c) {
            super(c);
            setClickable(false);
            setFocusable(false);

            body.setColor(Color.rgb(238, 198, 52));
            glass.setColor(Color.rgb(125, 180, 205));
            tyres.setColor(Color.rgb(24, 24, 24));
            lights.setColor(Color.rgb(250, 246, 215));
            rearLights.setColor(Color.rgb(220, 36, 36));
            shadow.setColor(Color.argb(80, 0, 0, 0));

            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(18000L);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(a -> {
                phase = (float) a.getAnimatedValue();
                invalidate();
            });
            animator.start();
        }

        @Override protected void onDetachedFromWindow() {
            animator.cancel();
            super.onDetachedFromWindow();
        }

        private void updateImageRect() {
            float vw = getWidth(), vh = getHeight();
            if (vw <= 0 || vh <= 0) {
                imageRect.setEmpty();
                return;
            }
            if (vw / vh > MASTER_ASPECT) {
                float iw = vh * MASTER_ASPECT;
                float left = (vw - iw) / 2f;
                imageRect.set(left, 0, left + iw, vh);
            } else {
                float ih = vw / MASTER_ASPECT;
                float top = (vh - ih) / 2f;
                imageRect.set(0, top, vw, top + ih);
            }
        }

        private float mapX(float n) { return imageRect.left + n * imageRect.width(); }
        private float mapY(float n) { return imageRect.top + n * imageRect.height(); }

        private void buildInnerLoop() {
            innerLoop.reset();
            if (imageRect.isEmpty()) return;
            for (int i = 0; i < LOOP.length; i++) {
                float nx = CENTRE_X + (LOOP[i][0] - CENTRE_X) * INNER_SCALE;
                float ny = CENTRE_Y + (LOOP[i][1] - CENTRE_Y) * INNER_SCALE;
                float x = mapX(nx), y = mapY(ny);
                if (i == 0) innerLoop.moveTo(x, y);
                else innerLoop.lineTo(x, y);
            }
            innerLoop.close();
        }

        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            updateImageRect();
            buildInnerLoop();
            pm.setPath(innerLoop, false);
            float len = pm.getLength();
            if (len <= 0 || !pm.getPosTan(len * phase, pos, tan)) return;

            float angle = (float) Math.toDegrees(Math.atan2(tan[1], tan[0])) + 90f;
            float s = Math.min(imageRect.width(), imageRect.height());
            drawVehicle(c, pos[0], pos[1], angle, s);
        }

        private void drawVehicle(Canvas c, float x, float y, float angle, float s) {
            c.save();
            c.translate(x, y);
            c.rotate(angle);

            float w = s * .044f;
            float h = w * 1.72f;

            c.drawRoundRect(new RectF(-w * .47f + s*.004f, -h * .48f + s*.004f,
                    w * .47f + s*.004f, h * .48f + s*.004f), w*.17f, w*.17f, shadow);

            c.drawRoundRect(new RectF(-w*.62f,-h*.31f,-w*.46f,-h*.04f),w*.06f,w*.06f,tyres);
            c.drawRoundRect(new RectF(w*.46f,-h*.31f,w*.62f,-h*.04f),w*.06f,w*.06f,tyres);
            c.drawRoundRect(new RectF(-w*.62f,h*.04f,-w*.46f,h*.31f),w*.06f,w*.06f,tyres);
            c.drawRoundRect(new RectF(w*.46f,h*.04f,w*.62f,h*.31f),w*.06f,w*.06f,tyres);

            c.drawRoundRect(new RectF(-w/2,-h/2,w/2,h/2),w*.18f,w*.18f,body);
            c.drawRoundRect(new RectF(-w*.32f,-h*.20f,w*.32f,h*.10f),w*.07f,w*.07f,glass);

            c.drawCircle(-w*.28f,-h*.42f,w*.055f,lights);
            c.drawCircle(w*.28f,-h*.42f,w*.055f,lights);
            c.drawCircle(-w*.28f,h*.42f,w*.055f,rearLights);
            c.drawCircle(w*.28f,h*.42f,w*.055f,rearLights);
            c.restore();
        }
    }
}
