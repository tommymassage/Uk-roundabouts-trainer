package uk.co.roundaboutstrainer;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.*;
import android.view.animation.LinearInterpolator;
import android.widget.*;
import android.content.Context;
import android.content.res.Configuration;

public class MainActivity extends Activity {
    private RoundaboutView roadView;
    private TextView[] stepViews = new TextView[6];

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        boolean phone = getResources().getConfiguration().smallestScreenWidthDp < 600;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(phone ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        root.setBackgroundColor(Color.rgb(242, 246, 248));

        roadView = new RoundaboutView(this);
        root.addView(roadView, phone
                ? new LinearLayout.LayoutParams(-1, 0, 5)
                : new LinearLayout.LayoutParams(0, -1, 3));

        ScrollView scroll = new ScrollView(this);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(18, 14, 18, 18);
        scroll.addView(panel);
        root.addView(scroll, phone
                ? new LinearLayout.LayoutParams(-1, 0, 4)
                : new LinearLayout.LayoutParams(0, -1, 2));

        panel.addView(text("UK ROUNDABOUTS TRAINER  v0.7.0", 24, true));
        TextView intro = text("Purpose-built UK roundabout • 2 circulating lanes • realistic kerbs", 13, false);
        intro.setPadding(0, 4, 0, 10);
        panel.addView(intro);

        panel.addView(text("Approach road", 14, true));
        Spinner approach = spinner(new String[]{"South", "West", "North", "East"});
        panel.addView(approach);

        panel.addView(text("Exit", 14, true));
        Spinner exit = spinner(new String[]{"1st exit / left", "2nd exit / ahead", "3rd exit / right"});
        panel.addView(exit);

        panel.addView(text("Traffic", 14, true));
        Spinner traffic = spinner(new String[]{"Clear roundabout", "Vehicle from the right", "Busy circulating traffic"});
        panel.addView(traffic);

        panel.addView(text("Mode", 14, true));
        Spinner mode = spinner(new String[]{"Guided demo", "Practice", "Test mode"});
        panel.addView(mode);

        CheckBox route = new CheckBox(this);
        route.setText("Show training route");
        route.setChecked(true);
        panel.addView(route);

        CheckBox cars = new CheckBox(this);
        cars.setText("Show other traffic");
        cars.setChecked(true);
        panel.addView(cars);

        CheckBox guide = new CheckBox(this);
        guide.setText("Show coaching card");
        guide.setChecked(true);
        panel.addView(guide);

        Button start = button("▶  START DRIVING DEMO");
        start.setTextSize(16);
        panel.addView(start, new LinearLayout.LayoutParams(-1, 54));

        panel.addView(text("MSPSL Guide", 17, true));
        String[] labels = {
                "Mirrors — check all mirrors",
                "Signal — communicate your intention",
                "Position — follow signs and road markings",
                "Speed — slow down and prepare to give way",
                "Look — assess traffic from the right",
                "Leave — signal left and exit safely"
        };
        for (int i = 0; i < 6; i++) {
            stepViews[i] = text((i + 1) + "   " + labels[i], 14, false);
            stepViews[i].setPadding(8, 8, 8, 8);
            panel.addView(stepViews[i]);
        }

        TextView note = text("ⓘ Follow signs and road markings. Give priority to traffic from the right unless signs or signals direct otherwise.", 12, true);
        note.setPadding(10, 10, 10, 10);
        note.setBackgroundColor(Color.rgb(220, 238, 253));
        panel.addView(note);

        TextView stage = text("Ready • MIRRORS", 18, true);
        stage.setPadding(0, 12, 0, 4);
        panel.addView(stage);

        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        panel.addView(progress, new LinearLayout.LayoutParams(-1, 18));

        TextView result = text("Training result: —", 14, true);
        result.setPadding(0, 8, 0, 0);
        panel.addView(result);

        roadView.listener = (s, p, step) -> {
            stage.setText(s);
            progress.setProgress(p);
            highlight(step);
            if (p >= 100) result.setText("Training result: COMPLETE");
        };

        Runnable update = () -> {
            roadView.approach = approach.getSelectedItemPosition();
            roadView.exit = exit.getSelectedItemPosition() + 1;
            roadView.traffic = traffic.getSelectedItemPosition();
            roadView.trainingMode = mode.getSelectedItemPosition();
            roadView.showRoute = route.isChecked() && roadView.trainingMode != 2;
            roadView.showTraffic = cars.isChecked();
            roadView.showGuide = guide.isChecked() && roadView.trainingMode != 2;
            roadView.reset();
            progress.setProgress(0);
            stage.setText("Ready • MIRRORS");
            result.setText("Training result: —");
            highlight(0);
        };

        approach.setOnItemSelectedListener(selection(update));
        exit.setOnItemSelectedListener(selection(update));
        traffic.setOnItemSelectedListener(selection(update));
        mode.setOnItemSelectedListener(selection(update));
        route.setOnClickListener(v -> update.run());
        cars.setOnClickListener(v -> update.run());
        guide.setOnClickListener(v -> update.run());
        start.setOnClickListener(v -> { update.run(); roadView.start(); });

        setContentView(root);
        highlight(0);
    }

    private void highlight(int active) {
        for (int i = 0; i < 6; i++) {
            stepViews[i].setBackgroundColor(i == active ? Color.rgb(218, 238, 255) : Color.TRANSPARENT);
            stepViews[i].setTypeface(null, i == active ? 1 : 0);
        }
    }

    private TextView text(String s, int size, boolean bold) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(size);
        v.setTextColor(Color.rgb(28, 38, 47));
        if (bold) v.setTypeface(null, 1);
        return v;
    }

    private Spinner spinner(String[] a) {
        Spinner s = new Spinner(this);
        s.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, a));
        return s;
    }

    private Button button(String s) {
        Button b = new Button(this);
        b.setText(s);
        return b;
    }

    private AdapterView.OnItemSelectedListener selection(Runnable r) {
        return new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int x, long id) { r.run(); }
            public void onNothingSelected(AdapterView<?> p) { }
        };
    }

    interface StageListener { void stage(String s, int progress, int step); }

    static class RoundaboutView extends View {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint routePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint routeShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint dashed = new Paint(Paint.ANTI_ALIAS_FLAG);

        boolean showRoute = true, showTraffic = true, showGuide = true;
        int approach = 0, exit = 1, traffic = 0, trainingMode = 0;
        float raw = 0, carProgress = 0;
        ValueAnimator animator;
        StageListener listener;

        RoundaboutView(Context c) {
            super(c);
            routePaint.setColor(Color.rgb(28, 226, 79));
            routePaint.setStyle(Paint.Style.STROKE);
            routePaint.setStrokeWidth(12);
            routePaint.setStrokeCap(Paint.Cap.ROUND);
            routePaint.setStrokeJoin(Paint.Join.ROUND);

            routeShadow.setColor(Color.argb(130, 0, 0, 0));
            routeShadow.setStyle(Paint.Style.STROKE);
            routeShadow.setStrokeWidth(22);
            routeShadow.setStrokeCap(Paint.Cap.ROUND);

            dashed.setStyle(Paint.Style.STROKE);
            dashed.setStrokeCap(Paint.Cap.BUTT);
        }

        void reset() {
            if (animator != null) animator.cancel();
            raw = 0;
            carProgress = 0;
            invalidate();
        }

        void start() {
            if (animator != null) animator.cancel();
            animator = ValueAnimator.ofFloat(0, 1);
            animator.setDuration(trainingMode == 2 ? 6500 : 8200);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(a -> {
                raw = (float) a.getAnimatedValue();
                float stop = traffic == 0 ? .26f : traffic == 1 ? .39f : .34f;
                if (raw < .20f) carProgress = raw / .20f * .14f;
                else if (raw < stop) carProgress = .14f;
                else carProgress = .14f + (raw - stop) / (1f - stop) * .86f;
                report();
                invalidate();
            });
            animator.start();
        }

        void report() {
            if (listener == null) return;
            String s;
            int st;
            if (raw < .12) { s = "1/6 • MIRRORS"; st = 0; }
            else if (raw < .22) { s = exit == 1 ? "2/6 • SIGNAL LEFT" : exit == 3 ? "2/6 • SIGNAL RIGHT" : "2/6 • SIGNAL normally none"; st = 1; }
            else if (raw < .32) { s = "3/6 • POSITION"; st = 2; }
            else if (raw < .46) { s = traffic == 0 ? "4/6 • SPEED • GIVE WAY" : "4/6 • WAIT • TRAFFIC FROM RIGHT"; st = 3; }
            else if (raw < .62) { s = "5/6 • LOOK RIGHT • safe gap"; st = 4; }
            else if (raw < .86) { s = "ON ROUNDABOUT • lane discipline"; st = 4; }
            else if (raw < .98) { s = "6/6 • SIGNAL LEFT • EXIT"; st = 5; }
            else { s = "COMPLETE • safe exit"; st = 5; }
            listener.stage(s, Math.round(raw * 100), st);
        }

        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float w = getWidth(), h = getHeight();
            float cx = w * .50f, cy = h * .50f;
            float outerR = Math.min(w, h) * .235f;
            float innerR = outerR * .52f;

            drawEnvironment(c, w, h);
            drawRoadArms(c, cx, cy, outerR, w, h);
            drawRoundabout(c, cx, cy, outerR, innerR);
            drawTrees(c, w, h, cx, cy, outerR);

            Path route = route(cx, cy, outerR, w, h, exit);
            c.save();
            c.rotate(approach * 90f, cx, cy);
            if (showRoute) {
                c.drawPath(route, routeShadow);
                c.drawPath(route, routePaint);
            }
            if (showTraffic) drawTraffic(c, cx, cy, outerR);
            drawCarOnPath(c, route, carProgress, Color.rgb(38, 104, 220));
            c.restore();

            if (showGuide) drawGuide(c, w, h);
        }

        void drawEnvironment(Canvas c, float w, float h) {
            c.drawColor(Color.rgb(89, 132, 77));
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(107, 151, 93));
            c.drawRect(0, 0, w, h, p);

            p.setColor(Color.rgb(203, 198, 184));
            float path = Math.min(w, h) * .035f;
            c.drawRect(w * .39f, 0, w * .61f, h, p);
            c.drawRect(0, h * .39f, w, h * .61f, p);

            p.setColor(Color.rgb(190, 186, 174));
            c.drawRect(w * .405f, 0, w * .595f, h, p);
            c.drawRect(0, h * .405f, w, h * .595f, p);
        }

        void drawRoadArms(Canvas c, float cx, float cy, float r, float w, float h) {
            float roadW = Math.min(w, h) * .19f;
            float kerb = Math.max(4, Math.min(w, h) * .008f);

            for (int i = 0; i < 4; i++) {
                c.save();
                c.rotate(i * 90f, cx, cy);

                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.rgb(49, 52, 54));
                c.drawRect(cx - roadW / 2, cy + r * .82f, cx + roadW / 2, h + 20, p);

                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(kerb);
                p.setColor(Color.rgb(214, 211, 202));
                c.drawLine(cx - roadW / 2, cy + r * .82f, cx - roadW / 2, h, p);
                c.drawLine(cx + roadW / 2, cy + r * .82f, cx + roadW / 2, h, p);

                dashed.setColor(Color.WHITE);
                dashed.setStrokeWidth(Math.max(3, kerb * .42f));
                dashed.setPathEffect(new DashPathEffect(new float[]{18, 16}, 0));
                c.drawLine(cx, cy + r * 1.42f, cx, h, dashed);
                dashed.setPathEffect(null);

                drawSplitterIsland(c, cx, cy, r, roadW);
                drawGiveWayLine(c, cx, cy, r, roadW);
                c.restore();
            }
        }

        void drawSplitterIsland(Canvas c, float cx, float cy, float r, float roadW) {
            float top = cy + r * .98f;
            float bottom = cy + r * 1.72f;
            float half = roadW * .11f;

            Path island = new Path();
            island.moveTo(cx, top);
            island.quadTo(cx - half, top + roadW * .10f, cx - half, top + roadW * .28f);
            island.lineTo(cx - half, bottom - roadW * .16f);
            island.quadTo(cx - half, bottom, cx, bottom + roadW * .06f);
            island.quadTo(cx + half, bottom, cx + half, bottom - roadW * .16f);
            island.lineTo(cx + half, top + roadW * .28f);
            island.quadTo(cx + half, top + roadW * .10f, cx, top);
            island.close();

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(205, 202, 192));
            c.drawPath(island, p);
            p.setColor(Color.rgb(114, 153, 92));
            Path inner = new Path();
            float inset = roadW * .035f;
            inner.addRoundRect(new RectF(cx - half + inset, top + roadW * .18f, cx + half - inset, bottom - roadW * .10f), half, half, Path.Direction.CW);
            c.drawPath(inner, p);

            p.setColor(Color.rgb(30, 101, 185));
            c.drawCircle(cx, top + roadW * .18f, roadW * .035f, p);
        }

        void drawGiveWayLine(Canvas c, float cx, float cy, float r, float roadW) {
            dashed.setColor(Color.WHITE);
            dashed.setStrokeWidth(Math.max(4, roadW * .035f));
            dashed.setPathEffect(new DashPathEffect(new float[]{12, 10}, 0));
            c.drawLine(cx - roadW * .44f, cy + r * 1.03f, cx - roadW * .08f, cy + r * 1.03f, dashed);
            c.drawLine(cx + roadW * .08f, cy + r * 1.03f, cx + roadW * .44f, cy + r * 1.03f, dashed);
            dashed.setPathEffect(null);
        }

        void drawRoundabout(Canvas c, float cx, float cy, float outerR, float innerR) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(211, 208, 198));
            c.drawCircle(cx, cy, outerR * 1.08f, p);

            p.setColor(Color.rgb(48, 51, 53));
            c.drawCircle(cx, cy, outerR, p);

            p.setColor(Color.rgb(207, 204, 194));
            c.drawCircle(cx, cy, innerR * 1.10f, p);
            p.setColor(Color.rgb(83, 129, 72));
            c.drawCircle(cx, cy, innerR, p);

            dashed.setColor(Color.WHITE);
            dashed.setStrokeWidth(Math.max(3, outerR * .018f));
            dashed.setPathEffect(new DashPathEffect(new float[]{16, 14}, 0));
            c.drawCircle(cx, cy, (outerR + innerR) * .51f, dashed);
            dashed.setPathEffect(null);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(3, outerR * .018f));
            p.setColor(Color.rgb(230, 228, 218));
            c.drawCircle(cx, cy, outerR * 1.01f, p);
            c.drawCircle(cx, cy, innerR * 1.08f, p);
            p.setStyle(Paint.Style.FILL);

            drawChevrons(c, cx, cy, innerR);
        }

        void drawChevrons(Canvas c, float cx, float cy, float innerR) {
            p.setStyle(Paint.Style.FILL);
            for (int i = 0; i < 8; i++) {
                double a = Math.toRadians(i * 45);
                float x = cx + (float) Math.cos(a) * innerR * .82f;
                float y = cy + (float) Math.sin(a) * innerR * .82f;
                c.save();
                c.translate(x, y);
                c.rotate(i * 45f + 90f);
                p.setColor(i % 2 == 0 ? Color.WHITE : Color.rgb(34, 34, 34));
                c.drawRect(-innerR * .09f, -innerR * .035f, innerR * .09f, innerR * .035f, p);
                c.restore();
            }
        }

        void drawTrees(Canvas c, float w, float h, float cx, float cy, float r) {
            float s = Math.min(w, h);
            float[][] pts = {
                    {.10f,.12f},{.18f,.22f},{.82f,.16f},{.90f,.28f},
                    {.13f,.80f},{.24f,.89f},{.79f,.84f},{.91f,.72f},
                    {.28f,.19f},{.72f,.22f},{.25f,.73f},{.75f,.70f}
            };
            for (int i = 0; i < pts.length; i++) {
                float x = w * pts[i][0], y = h * pts[i][1];
                p.setColor(Color.argb(65, 0, 0, 0));
                c.drawCircle(x + 5, y + 6, s * .028f, p);
                p.setColor(i % 2 == 0 ? Color.rgb(54, 108, 56) : Color.rgb(66, 119, 62));
                c.drawCircle(x, y, s * .028f, p);
                p.setColor(Color.rgb(83, 136, 70));
                c.drawCircle(x - s * .008f, y - s * .008f, s * .016f, p);
            }
        }

        Path route(float cx, float cy, float r, float w, float h, int e) {
            Path q = new Path();
            float lane = Math.min(w, h) * .035f;
            float sx = cx - lane * .75f;
            q.moveTo(sx, h + 10);
            q.lineTo(sx, cy + r * 1.22f);
            q.quadTo(sx, cy + r * 1.04f, cx - r * .06f, cy + r * 1.04f);
            float rr = e == 3 ? r * .74f : r * .88f;
            q.arcTo(new RectF(cx - rr, cy - rr, cx + rr, cy + rr), 92, e * 90);
            if (e == 1) {
                q.quadTo(cx - rr, cy + lane * .18f, cx - rr * 1.26f, cy + lane * .18f);
                q.lineTo(-10, cy + lane * .18f);
            } else if (e == 2) {
                q.quadTo(cx - lane * .18f, cy - rr, cx - lane * .18f, cy - rr * 1.26f);
                q.lineTo(cx - lane * .18f, -10);
            } else {
                q.quadTo(cx + rr, cy - lane * .18f, cx + rr * 1.26f, cy - lane * .18f);
                q.lineTo(w + 10, cy - lane * .18f);
            }
            return q;
        }

        void drawTraffic(Canvas c, float cx, float cy, float r) {
            int n = traffic == 0 ? 1 : traffic == 1 ? 2 : 5;
            for (int i = 0; i < n; i++) {
                float a = (float) Math.toRadians((raw * 180 + i * (360f / n) + 15) % 360);
                float rr = r * .82f;
                float x = cx + (float) Math.cos(a) * rr;
                float y = cy + (float) Math.sin(a) * rr;
                drawCar(c, x, y, (float) Math.toDegrees(a) + 90,
                        i % 2 == 0 ? Color.rgb(192, 48, 43) : Color.rgb(50, 62, 76));
            }
        }

        void drawCarOnPath(Canvas c, Path q, float f, int color) {
            PathMeasure pm = new PathMeasure(q, false);
            float[] pos = new float[2], tan = new float[2];
            if (pm.getPosTan(pm.getLength() * f, pos, tan)) {
                drawCar(c, pos[0], pos[1], (float) Math.toDegrees(Math.atan2(tan[1], tan[0])), color);
            }
        }

        void drawCar(Canvas c, float x, float y, float angle, int color) {
            c.save();
            c.translate(x, y);
            c.rotate(angle);
            float l = Math.max(34, Math.min(getWidth(), getHeight()) * .052f);
            float ww = l * .46f;
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(90, 0, 0, 0));
            c.drawRoundRect(-l * .48f + 3, -ww * .48f + 4, l * .48f + 3, ww * .48f + 4, 8, 8, p);
            p.setColor(color);
            c.drawRoundRect(-l * .50f, -ww * .50f, l * .50f, ww * .50f, 8, 8, p);
            p.setColor(Color.rgb(190, 220, 235));
            c.drawRoundRect(-l * .16f, -ww * .40f, l * .18f, ww * .40f, 4, 4, p);
            p.setColor(Color.rgb(25, 25, 25));
            c.drawRect(-l * .34f, -ww * .58f, -l * .14f, -ww * .45f, p);
            c.drawRect(l * .14f, -ww * .58f, l * .34f, -ww * .45f, p);
            c.drawRect(-l * .34f, ww * .45f, -l * .14f, ww * .58f, p);
            c.drawRect(l * .14f, ww * .45f, l * .34f, ww * .58f, p);
            c.restore();
        }

        void drawGuide(Canvas c, float w, float h) {
            float bw = w * .39f, bh = h * .17f, x = 14, y = h - bh - 14;
            p.setColor(Color.argb(225, 15, 31, 41));
            p.setStyle(Paint.Style.FILL);
            c.drawRoundRect(x, y, x + bw, y + bh, 12, 12, p);
            p.setColor(Color.WHITE);
            p.setTypeface(Typeface.DEFAULT_BOLD);
            p.setTextSize(Math.max(13, h * .018f));
            c.drawText("STEP " + currentStep() + " OF 6", x + 14, y + 24, p);
            p.setTextSize(Math.max(16, h * .024f));
            c.drawText(guideTitle(), x + 14, y + 51, p);
            p.setTypeface(Typeface.DEFAULT);
            p.setTextSize(Math.max(12, h * .017f));
            c.drawText(guideBody(), x + 14, y + 76, p);
        }

        int currentStep() {
            if (raw < .12) return 1;
            if (raw < .22) return 2;
            if (raw < .32) return 3;
            if (raw < .46) return 4;
            if (raw < .86) return 5;
            return 6;
        }

        String guideTitle() {
            int s = currentStep();
            if (s == 1) return "Check mirrors";
            if (s == 2) return "Signal your intention";
            if (s == 3) return "Choose the correct lane";
            if (s == 4) return "Slow down and prepare to give way";
            if (s == 5) return "Look right and judge the gap";
            return "Signal left and leave";
        }

        String guideBody() {
            return currentStep() == 4 && traffic > 0
                    ? "Traffic is approaching from your right — wait."
                    : "Follow signs and road markings.";
        }
    }
}
