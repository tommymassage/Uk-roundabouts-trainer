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

        ScrollView scroll = new ScrollView(this);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(26, 24, 26, 28);
        panel.setGravity(Gravity.TOP);
        scroll.addView(panel);
        root.addView(scroll, new LinearLayout.LayoutParams(0, -1, 2));

        TextView title = label("UK ROUNDABOUTS TRAINER  v0.7.4", 24, true);
        panel.addView(title);
        TextView sub = label("PRESERVED ROUNDABOUT + LIVE TRAINING LAYERS", 14, true);
        sub.setPadding(0, 8, 0, 16);
        panel.addView(sub);

        panel.addView(label("Approach road", 14, true));
        Spinner approach = new Spinner(this);
        approach.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"South", "West", "North", "East"}));
        panel.addView(approach);

        panel.addView(label("Exit", 14, true));
        Spinner exit = new Spinner(this);
        exit.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"1st exit / left", "2nd exit / ahead", "3rd exit / right"}));
        exit.setSelection(1);
        panel.addView(exit);

        CheckBox routeToggle = new CheckBox(this);
        routeToggle.setText("Show training route");
        routeToggle.setChecked(true);
        panel.addView(routeToggle);

        CheckBox carToggle = new CheckBox(this);
        carToggle.setText("Show learner car");
        carToggle.setChecked(true);
        panel.addView(carToggle);

        CheckBox trafficToggle = new CheckBox(this);
        trafficToggle.setText("Show vehicle from the right");
        trafficToggle.setChecked(false);
        panel.addView(trafficToggle);

        Button start = new Button(this);
        start.setText("▶  START DRIVING DEMO");
        panel.addView(start, new LinearLayout.LayoutParams(-1, 60));

        TextView stage = label("Ready • MIRRORS", 17, true);
        stage.setPadding(0, 18, 0, 6);
        panel.addView(stage);

        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        panel.addView(progress, new LinearLayout.LayoutParams(-1, 18));

        TextView guide = label("MSPSL: Mirrors → Signal → Position → Speed → Look → Leave", 13, false);
        guide.setPadding(0, 12, 0, 0);
        panel.addView(guide);

        overlay.listener = (text, value) -> {
            stage.setText(text);
            progress.setProgress(value);
        };

        approach.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                overlay.approach = position;
                overlay.reset();
                stage.setText("Ready • MIRRORS");
                progress.setProgress(0);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        exit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                overlay.exit = position + 1;
                overlay.reset();
                stage.setText("Ready • MIRRORS");
                progress.setProgress(0);
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
        trafficToggle.setOnCheckedChangeListener((button, checked) -> {
            overlay.showTraffic = checked;
            overlay.invalidate();
        });
        start.setOnClickListener(v -> overlay.start());

        setContentView(root);
    }

    private TextView label(String text, int size, boolean bold) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(size);
        v.setTextColor(Color.rgb(28, 38, 47));
        if (bold) v.setTypeface(null, 1);
        return v;
    }

    interface StageListener { void onStage(String text, int progress); }

    static class TrainingOverlay extends View {
        private final Paint routeShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint routePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint carPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint carGlass = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint trafficPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path route = new Path();
        private final PathMeasure measure = new PathMeasure();
        private final float[] pos = new float[2];
        private final float[] tan = new float[2];

        boolean showRoute = true;
        boolean showCar = true;
        boolean showTraffic = false;
        int approach = 0;
        int exit = 2;
        float progress = 0f;
        ValueAnimator animator;
        StageListener listener;

        TrainingOverlay(Context c) {
            super(c);
            setBackgroundColor(Color.TRANSPARENT);
            routeShadow.setColor(Color.argb(120, 0, 0, 0));
            routeShadow.setStyle(Paint.Style.STROKE);
            routeShadow.setStrokeCap(Paint.Cap.ROUND);
            routeShadow.setStrokeJoin(Paint.Join.ROUND);
            routePaint.setColor(Color.rgb(30, 220, 78));
            routePaint.setStyle(Paint.Style.STROKE);
            routePaint.setStrokeCap(Paint.Cap.ROUND);
            routePaint.setStrokeJoin(Paint.Join.ROUND);
            carPaint.setColor(Color.rgb(30, 103, 225));
            carGlass.setColor(Color.rgb(185, 228, 255));
            trafficPaint.setColor(Color.rgb(210, 52, 52));
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
            animator.setDuration(showTraffic ? 8200 : 7000);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(a -> {
                float raw = (float) a.getAnimatedValue();
                if (showTraffic && raw > .22f && raw < .36f) progress = .18f;
                else if (showTraffic && raw >= .36f) progress = .18f + (raw - .36f) / .64f * .82f;
                else progress = raw;
                report(raw);
                invalidate();
            });
            animator.start();
        }

        private void report(float raw) {
            if (listener == null) return;
            String s;
            if (raw < .14f) s = "1/6 • MIRRORS";
            else if (raw < .26f) s = exit == 1 ? "2/6 • SIGNAL LEFT" : exit == 3 ? "2/6 • SIGNAL RIGHT" : "2/6 • SIGNAL normally none";
            else if (raw < .38f) s = "3/6 • POSITION";
            else if (raw < .52f) s = showTraffic ? "4/6 • SPEED • GIVE WAY" : "4/6 • SPEED";
            else if (raw < .68f) s = "5/6 • LOOK RIGHT • safe gap";
            else if (raw < .88f) s = "ON ROUNDABOUT • lane discipline";
            else if (raw < .98f) s = "6/6 • SIGNAL LEFT • EXIT";
            else s = "COMPLETE • safe exit";
            listener.onStage(s, Math.round(raw * 100));
        }

        private float X(float left, float size, float n) { return left + size * n; }
        private float Y(float top, float size, float n) { return top + size * n; }

        private void buildSouthRoute(float left, float top, float s) {
            route.reset();
            route.moveTo(X(left, s, .435f), Y(top, s, .995f));
            route.cubicTo(X(left, s, .435f), Y(top, s, .86f), X(left, s, .425f), Y(top, s, .735f), X(left, s, .392f), Y(top, s, .665f));
            if (exit == 1) {
                route.cubicTo(X(left, s, .340f), Y(top, s, .650f), X(left, s, .300f), Y(top, s, .620f), X(left, s, .270f), Y(top, s, .565f));
                route.cubicTo(X(left, s, .205f), Y(top, s, .555f), X(left, s, .110f), Y(top, s, .555f), X(left, s, .005f), Y(top, s, .555f));
            } else if (exit == 2) {
                route.cubicTo(X(left, s, .315f), Y(top, s, .625f), X(left, s, .270f), Y(top, s, .555f), X(left, s, .270f), Y(top, s, .490f));
                route.cubicTo(X(left, s, .270f), Y(top, s, .390f), X(left, s, .335f), Y(top, s, .315f), X(left, s, .410f), Y(top, s, .285f));
                route.cubicTo(X(left, s, .435f), Y(top, s, .225f), X(left, s, .435f), Y(top, s, .120f), X(left, s, .435f), Y(top, s, .005f));
            } else {
                route.cubicTo(X(left, s, .315f), Y(top, s, .625f), X(left, s, .270f), Y(top, s, .555f), X(left, s, .270f), Y(top, s, .490f));
                route.cubicTo(X(left, s, .270f), Y(top, s, .365f), X(left, s, .365f), Y(top, s, .275f), X(left, s, .490f), Y(top, s, .275f));
                route.cubicTo(X(left, s, .615f), Y(top, s, .275f), X(left, s, .700f), Y(top, s, .365f), X(left, s, .720f), Y(top, s, .470f));
                route.cubicTo(X(left, s, .755f), Y(top, s, .535f), X(left, s, .865f), Y(top, s, .545f), X(left, s, .995f), Y(top, s, .545f));
            }
        }

        private void drawCar(Canvas c, float s, Paint bodyPaint) {
            float angle = (float) Math.toDegrees(Math.atan2(tan[1], tan[0])) + 90f;
            c.save();
            c.translate(pos[0], pos[1]);
            c.rotate(angle);
            float cw = s * .030f;
            float ch = cw * 1.75f;
            RectF body = new RectF(-cw / 2f, -ch / 2f, cw / 2f, ch / 2f);
            c.drawRoundRect(body, cw * .22f, cw * .22f, bodyPaint);
            if (bodyPaint == carPaint) {
                RectF glass = new RectF(-cw * .33f, -ch * .18f, cw * .33f, ch * .08f);
                c.drawRoundRect(glass, cw * .08f, cw * .08f, carGlass);
            }
            c.restore();
        }

        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;
            float s = Math.min(w, h);
            float left = (w - s) / 2f;
            float top = (h - s) / 2f;
            float cx = left + s / 2f;
            float cy = top + s / 2f;

            buildSouthRoute(left, top, s);
            routeShadow.setStrokeWidth(s * .024f);
            routePaint.setStrokeWidth(s * .013f);

            c.save();
            c.rotate(approach * 90f, cx, cy);
            if (showRoute) {
                c.drawPath(route, routeShadow);
                c.drawPath(route, routePaint);
            }
            if (showCar) {
                measure.setPath(route, false);
                measure.getPosTan(measure.getLength() * Math.max(0f, Math.min(1f, progress)), pos, tan);
                drawCar(c, s, carPaint);
            }
            if (showTraffic) {
                Path trafficPath = new Path();
                trafficPath.addArc(new RectF(X(left,s,.285f), Y(top,s,.285f), X(left,s,.715f), Y(top,s,.715f)), 350f, -150f);
                measure.setPath(trafficPath, false);
                measure.getPosTan(measure.getLength() * .42f, pos, tan);
                drawCar(c, s, trafficPaint);
            }
            c.restore();
        }
    }
}
