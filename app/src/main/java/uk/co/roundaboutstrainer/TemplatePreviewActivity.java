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
import android.widget.AdapterView;

public class TemplatePreviewActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setBackgroundColor(Color.rgb(238, 243, 247));

        FrameLayout boardStack = new FrameLayout(this);
        boardStack.setBackgroundColor(Color.BLACK);

        ImageView board = new ImageView(this);
        board.setImageResource(R.drawable.roundabout_template);
        board.setScaleType(ImageView.ScaleType.FIT_CENTER);
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
        title.setText("UK ROUNDABOUTS TRAINER  v0.7.3");
        title.setTextSize(25);
        title.setTextColor(Color.rgb(25, 35, 45));
        title.setTypeface(null, 1);
        panel.addView(title);

        TextView status = new TextView(this);
        status.setText("PRESERVED IMAGE TEMPLATE + CALIBRATED LIVE LAYERS\n\nThe roundabout artwork stays unchanged. The route and training car are separate interactive layers aligned to the square image area.");
        status.setTextSize(16);
        status.setTextColor(Color.rgb(35, 45, 55));
        status.setPadding(0, 18, 0, 16);
        panel.addView(status);

        TextView exitLabel = new TextView(this);
        exitLabel.setText("Exit from South approach");
        exitLabel.setTextSize(14);
        exitLabel.setTypeface(null, 1);
        panel.addView(exitLabel);

        Spinner exit = new Spinner(this);
        exit.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"1st exit / left", "2nd exit / ahead", "3rd exit / right"}));
        exit.setSelection(1);
        panel.addView(exit);

        CheckBox routeToggle = new CheckBox(this);
        routeToggle.setText("Show route overlay");
        routeToggle.setChecked(true);
        panel.addView(routeToggle);

        CheckBox carToggle = new CheckBox(this);
        carToggle.setText("Show training car");
        carToggle.setChecked(true);
        panel.addView(carToggle);

        Button start = new Button(this);
        start.setText("▶  START DRIVING DEMO");
        panel.addView(start, new LinearLayout.LayoutParams(-1, 60));

        TextView note = new TextView(this);
        note.setText("Template is now a permanent project resource. Next we can fine-tune lane positions and add other traffic without redrawing the roundabout.");
        note.setTextSize(14);
        note.setPadding(0, 18, 0, 0);
        panel.addView(note);

        exit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                overlay.exit = position + 1;
                overlay.reset();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

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
        int exit = 2;
        float progress = 0f;
        ValueAnimator animator;

        TrainingOverlay(Context c) {
            super(c);
            setBackgroundColor(Color.TRANSPARENT);

            routeShadow.setColor(Color.argb(125, 0, 0, 0));
            routeShadow.setStyle(Paint.Style.STROKE);
            routeShadow.setStrokeCap(Paint.Cap.ROUND);
            routeShadow.setStrokeJoin(Paint.Join.ROUND);

            routePaint.setColor(Color.rgb(30, 220, 78));
            routePaint.setStyle(Paint.Style.STROKE);
            routePaint.setStrokeCap(Paint.Cap.ROUND);
            routePaint.setStrokeJoin(Paint.Join.ROUND);

            carPaint.setColor(Color.rgb(30, 103, 225));
            carGlass.setColor(Color.rgb(185, 228, 255));
        }

        void reset() {
            if (animator != null) animator.cancel();
            progress = 0f;
            invalidate();
        }

        void start() {
            if (animator != null) animator.cancel();
            progress = 0f;
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(7000);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(a -> {
                progress = (float) a.getAnimatedValue();
                invalidate();
            });
            animator.start();
        }

        private float X(float left, float size, float n) { return left + size * n; }
        private float Y(float top, float size, float n) { return top + size * n; }

        private void buildRoute(float left, float top, float s) {
            route.reset();

            // South approach, inbound lane on the left side of the carriageway.
            route.moveTo(X(left, s, .435f), Y(top, s, .995f));
            route.cubicTo(X(left, s, .435f), Y(top, s, .86f),
                    X(left, s, .425f), Y(top, s, .735f),
                    X(left, s, .392f), Y(top, s, .665f));

            if (exit == 1) {
                // Clockwise entry, then first exit to the west.
                route.cubicTo(X(left, s, .340f), Y(top, s, .650f),
                        X(left, s, .300f), Y(top, s, .620f),
                        X(left, s, .270f), Y(top, s, .565f));
                route.cubicTo(X(left, s, .205f), Y(top, s, .555f),
                        X(left, s, .110f), Y(top, s, .555f),
                        X(left, s, .005f), Y(top, s, .555f));
            } else if (exit == 2) {
                // Ahead: around the west side, leaving northbound.
                route.cubicTo(X(left, s, .315f), Y(top, s, .625f),
                        X(left, s, .270f), Y(top, s, .555f),
                        X(left, s, .270f), Y(top, s, .490f));
                route.cubicTo(X(left, s, .270f), Y(top, s, .390f),
                        X(left, s, .335f), Y(top, s, .315f),
                        X(left, s, .410f), Y(top, s, .285f));
                route.cubicTo(X(left, s, .435f), Y(top, s, .225f),
                        X(left, s, .435f), Y(top, s, .120f),
                        X(left, s, .435f), Y(top, s, .005f));
            } else {
                // Right: continue clockwise and leave to the east.
                route.cubicTo(X(left, s, .315f), Y(top, s, .625f),
                        X(left, s, .270f), Y(top, s, .555f),
                        X(left, s, .270f), Y(top, s, .490f));
                route.cubicTo(X(left, s, .270f), Y(top, s, .365f),
                        X(left, s, .365f), Y(top, s, .275f),
                        X(left, s, .490f), Y(top, s, .275f));
                route.cubicTo(X(left, s, .615f), Y(top, s, .275f),
                        X(left, s, .700f), Y(top, s, .365f),
                        X(left, s, .720f), Y(top, s, .470f));
                route.cubicTo(X(left, s, .755f), Y(top, s, .535f),
                        X(left, s, .865f), Y(top, s, .545f),
                        X(left, s, .995f), Y(top, s, .545f));
            }
        }

        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;

            float s = Math.min(w, h);
            float left = (w - s) / 2f;
            float top = (h - s) / 2f;
            buildRoute(left, top, s);

            routeShadow.setStrokeWidth(s * .024f);
            routePaint.setStrokeWidth(s * .013f);

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

                float cw = s * .030f;
                float ch = cw * 1.75f;
                RectF body = new RectF(-cw / 2f, -ch / 2f, cw / 2f, ch / 2f);
                c.drawRoundRect(body, cw * .22f, cw * .22f, carPaint);
                RectF glass = new RectF(-cw * .33f, -ch * .18f, cw * .33f, ch * .08f);
                c.drawRoundRect(glass, cw * .08f, cw * .08f, carGlass);
                c.restore();
            }
        }
    }
}
