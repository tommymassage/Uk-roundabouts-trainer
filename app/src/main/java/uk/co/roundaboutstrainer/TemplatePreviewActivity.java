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

        panel.addView(label("UK ROUNDABOUTS TRAINER  v0.7.5", 24, true));
        TextView sub = label("PRESERVED ROUNDABOUT + MOVING TRAFFIC", 14, true);
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

        panel.addView(label("Traffic", 14, true));
        Spinner traffic = new Spinner(this);
        traffic.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Clear roundabout", "Vehicle from the right", "Busy circulating traffic"}));
        panel.addView(traffic);

        CheckBox routeToggle = new CheckBox(this);
        routeToggle.setText("Show training route");
        routeToggle.setChecked(true);
        panel.addView(routeToggle);

        CheckBox carToggle = new CheckBox(this);
        carToggle.setText("Show learner car");
        carToggle.setChecked(true);
        panel.addView(carToggle);

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

        TextView rule = label("UK rule: give priority to traffic approaching from the right unless signs, signals or markings direct otherwise.", 12, true);
        rule.setPadding(0, 14, 0, 0);
        panel.addView(rule);

        overlay.listener = (text, value) -> {
            stage.setText(text);
            progress.setProgress(value);
        };

        approach.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                overlay.approach = position;
                resetUi(overlay, stage, progress);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        exit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                overlay.exit = position + 1;
                resetUi(overlay, stage, progress);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        traffic.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                overlay.trafficMode = position;
                resetUi(overlay, stage, progress);
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

    private void resetUi(TrainingOverlay overlay, TextView stage, ProgressBar progress) {
        overlay.reset();
        stage.setText("Ready • MIRRORS");
        progress.setProgress(0);
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
        private final Paint traffic2Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path route = new Path();
        private final Path trafficPath = new Path();
        private final PathMeasure measure = new PathMeasure();
        private final float[] pos = new float[2];
        private final float[] tan = new float[2];

        boolean showRoute = true;
        boolean showCar = true;
        int trafficMode = 0;
        int approach = 0;
        int exit = 2;
        float raw = 0f;
        float progress = 0f;
        float trafficProgress = .05f;
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
            traffic2Paint.setColor(Color.rgb(245, 145, 35));
        }

        void reset() {
            if (animator != null) animator.cancel();
            raw = 0f;
            progress = 0f;
            trafficProgress = .05f;
            invalidate();
        }

        void start() {
            if (animator != null) animator.cancel();
            raw = 0f;
            progress = 0f;
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(trafficMode == 0 ? 7000 : 9000);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(a -> {
                raw = (float) a.getAnimatedValue();
                trafficProgress = (raw * 1.20f + .05f) % 1f;

                if (trafficMode == 0) {
                    progress = raw;
                } else {
                    float holdStart = .22f;
                    float holdEnd = trafficMode == 1 ? .43f : .55f;
                    if (raw < holdStart) progress = raw / holdStart * .18f;
                    else if (raw < holdEnd) progress = .18f;
                    else progress = .18f + (raw - holdEnd) / (1f - holdEnd) * .82f;
                }
                progress = Math.max(0f, Math.min(1f, progress));
                report();
                invalidate();
            });
            animator.start();
        }

        private void report() {
            if (listener == null) return;
            String s;
            if (raw < .12f) s = "1/6 • MIRRORS";
            else if (raw < .22f) s = exit == 1 ? "2/6 • SIGNAL LEFT" : exit == 3 ? "2/6 • SIGNAL RIGHT" : "2/6 • SIGNAL normally none";
            else if (raw < .32f) s = "3/6 • POSITION";
            else if (trafficMode > 0 && progress <= .181f) s = "4/6 • SPEED • WAIT • GIVE WAY TO RIGHT";
            else if (raw < .58f) s = "4/6 • SPEED • prepare to enter";
            else if (raw < .70f) s = "5/6 • LOOK RIGHT • safe gap";
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

        private void buildTrafficPath(float left, float top, float s) {
            trafficPath.reset();
            RectF ring = new RectF(X(left,s,.285f), Y(top,s,.285f), X(left,s,.715f), Y(top,s,.715f));
            trafficPath.addArc(ring, 90f, -359f);
        }

        private void drawCar(Canvas c, float s, Paint bodyPaint, boolean glass) {
            float angle = (float) Math.toDegrees(Math.atan2(tan[1], tan[0])) + 90f;
            c.save();
            c.translate(pos[0], pos[1]);
            c.rotate(angle);
            float cw = s * .030f;
            float ch = cw * 1.75f;
            RectF body = new RectF(-cw / 2f, -ch / 2f, cw / 2f, ch / 2f);
            c.drawRoundRect(body, cw * .22f, cw * .22f, bodyPaint);
            if (glass) {
                RectF g = new RectF(-cw * .33f, -ch * .18f, cw * .33f, ch * .08f);
                c.drawRoundRect(g, cw * .08f, cw * .08f, carGlass);
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
            buildTrafficPath(left, top, s);
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
                measure.getPosTan(measure.getLength() * progress, pos, tan);
                drawCar(c, s, carPaint, true);
            }

            if (trafficMode > 0) {
                measure.setPath(trafficPath, false);
                measure.getPosTan(measure.getLength() * trafficProgress, pos, tan);
                drawCar(c, s, trafficPaint, false);

                if (trafficMode == 2) {
                    float p2 = (trafficProgress + .42f) % 1f;
                    measure.getPosTan(measure.getLength() * p2, pos, tan);
                    drawCar(c, s, traffic2Paint, false);
                }
            }
            c.restore();
        }
    }
}
