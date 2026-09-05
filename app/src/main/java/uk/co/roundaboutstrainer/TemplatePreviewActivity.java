package uk.co.roundaboutstrainer;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.View;
import android.view.Gravity;
import android.view.animation.LinearInterpolator;
import android.widget.*;
import android.content.Context;

public class TemplatePreviewActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setBackgroundColor(Color.rgb(238, 243, 247));

        FrameLayout boardStack = new FrameLayout(this);
        ImageView board = new ImageView(this);
        board.setImageResource(R.drawable.roundabout_template);
        board.setScaleType(ImageView.ScaleType.CENTER_CROP);
        boardStack.addView(board, new FrameLayout.LayoutParams(-1, -1));

        TrainingOverlay overlay = new TrainingOverlay(this);
        boardStack.addView(overlay, new FrameLayout.LayoutParams(-1, -1));
        root.addView(boardStack, new LinearLayout.LayoutParams(0, -1, 3));

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(28, 28, 28, 28);
        panel.setGravity(Gravity.TOP);
        root.addView(panel, new LinearLayout.LayoutParams(0, -1, 2));

        TextView title = new TextView(this);
        title.setText("UK ROUNDABOUTS TRAINER  v0.7.2");
        title.setTextSize(25);
        title.setTextColor(Color.rgb(25, 35, 45));
        title.setTypeface(null, 1);
        panel.addView(title);

        TextView status = new TextView(this);
        status.setText("IMAGE TEMPLATE + LIVE OVERLAY TEST\n\nThe roundabout is the background layer. The green route and blue training car are separate live layers drawn on top.\n\nThis proves we can preserve the artwork while keeping cars, routes and training logic interactive.");
        status.setTextSize(16);
        status.setTextColor(Color.rgb(35, 45, 55));
        status.setPadding(0, 20, 0, 18);
        panel.addView(status);

        CheckBox routeToggle = new CheckBox(this);
        routeToggle.setText("Show route overlay");
        routeToggle.setChecked(true);
        panel.addView(routeToggle);

        CheckBox carToggle = new CheckBox(this);
        carToggle.setText("Show training car");
        carToggle.setChecked(true);
        panel.addView(carToggle);

        Button start = new Button(this);
        start.setText("▶  START OVERLAY DEMO");
        panel.addView(start, new LinearLayout.LayoutParams(-1, 60));

        TextView note = new TextView(this);
        note.setText("Next: replace the temporary resource with the full artwork, then calibrate the path to the exact lane geometry.");
        note.setTextSize(14);
        note.setPadding(0, 18, 0, 0);
        panel.addView(note);

        routeToggle.setOnCheckedChangeListener((button, checked) -> {
            overlay.showRoute = checked;
            overlay.invalidate();
        });
        carToggle.setOnCheckedChangeListener((button, checked) -> {
            overlay.showCar = checked;
            overlay.invalidate();
        });
        start.setOnClickListener(v -> overlay.start());

        setContentView(root);
    }

    static class TrainingOverlay extends View {
        private final Paint routeShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint routePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint carPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint carGlass = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path route = new Path();
        private final PathMeasure measure = new PathMeasure();
        private final float[] pos = new float[2];
        private final float[] tan = new float[2];
        boolean showRoute = true;
        boolean showCar = true;
        float progress = 0f;
        ValueAnimator animator;

        TrainingOverlay(Context c) {
            super(c);
            setBackgroundColor(Color.TRANSPARENT);
            routeShadow.setColor(Color.argb(120, 0, 0, 0));
            routeShadow.setStyle(Paint.Style.STROKE);
            routeShadow.setStrokeWidth(22f);
            routeShadow.setStrokeCap(Paint.Cap.ROUND);
            routeShadow.setStrokeJoin(Paint.Join.ROUND);

            routePaint.setColor(Color.rgb(32, 220, 82));
            routePaint.setStyle(Paint.Style.STROKE);
            routePaint.setStrokeWidth(12f);
            routePaint.setStrokeCap(Paint.Cap.ROUND);
            routePaint.setStrokeJoin(Paint.Join.ROUND);

            carPaint.setColor(Color.rgb(28, 102, 220));
            carGlass.setColor(Color.rgb(180, 225, 255));
        }

        void start() {
            if (animator != null) animator.cancel();
            progress = 0f;
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(6500);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(a -> {
                progress = (float) a.getAnimatedValue();
                invalidate();
            });
            animator.start();
        }

        private void buildRoute(float w, float h) {
            route.reset();
            float cx = w * .50f;
            float cy = h * .50f;
            float r = Math.min(w, h) * .235f;
            route.moveTo(cx - w * .030f, h * .96f);
            route.cubicTo(cx - w * .030f, h * .76f, cx - r * .62f, cy + r * 1.10f, cx - r * .72f, cy + r * .68f);
            route.cubicTo(cx - r * 1.02f, cy + r * .35f, cx - r * 1.02f, cy - r * .35f, cx - r * .68f, cy - r * .70f);
            route.cubicTo(cx - r * .35f, cy - r * 1.02f, cx + r * .35f, cy - r * 1.02f, cx + r * .68f, cy - r * .70f);
            route.cubicTo(cx + r * .90f, cy - r * .45f, cx + r * .96f, cy - r * .10f, cx + r * .92f, cy + r * .10f);
            route.cubicTo(cx + r * .88f, cy + r * .40f, cx + r * .72f, cy + r * .58f, cx + r * .48f, cy + r * .72f);
            route.cubicTo(cx + r * .20f, cy + r * .92f, cx + w * .030f, h * .76f, cx + w * .030f, h * .96f);
        }

        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;
            buildRoute(w, h);

            if (showRoute) {
                c.drawPath(route, routeShadow);
                c.drawPath(route, routePaint);
            }

            if (showCar) {
                measure.setPath(route, false);
                measure.getPosTan(measure.getLength() * progress, pos, tan);
                float angle = (float) Math.toDegrees(Math.atan2(tan[1], tan[0])) + 90f;
                c.save();
                c.translate(pos[0], pos[1]);
                c.rotate(angle);
                float cw = Math.min(w, h) * .035f;
                float ch = cw * 1.65f;
                RectF body = new RectF(-cw / 2, -ch / 2, cw / 2, ch / 2);
                c.drawRoundRect(body, cw * .22f, cw * .22f, carPaint);
                RectF glass = new RectF(-cw * .33f, -ch * .18f, cw * .33f, ch * .08f);
                c.drawRoundRect(glass, cw * .08f, cw * .08f, carGlass);
                c.restore();
            }
        }
    }
}
