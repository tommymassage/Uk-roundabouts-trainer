package uk.co.roundaboutstrainer;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import android.content.Context;

public class MainActivity extends Activity {
    private RoundaboutView roadView;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setPadding(18, 18, 18, 18);
        root.setBackgroundColor(Color.rgb(238, 245, 238));

        roadView = new RoundaboutView(this);
        root.addView(roadView, new LinearLayout.LayoutParams(0, -1, 3));

        ScrollView scroll = new ScrollView(this);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(26, 10, 18, 12);
        scroll.addView(panel);
        root.addView(scroll, new LinearLayout.LayoutParams(0, -1, 2));

        TextView title = new TextView(this);
        title.setText("UK ROUNDABOUTS TRAINER  v0.2");
        title.setTextSize(24);
        title.setTypeface(null, Typeface.BOLD);
        panel.addView(title);

        TextView info = new TextView(this);
        info.setText("Choose an approach and exit, then watch the training car.\nMSPSL: Mirrors • Signal • Position • Speed • Look.");
        info.setTextSize(16);
        info.setPadding(0, 14, 0, 18);
        panel.addView(info);

        panel.addView(label("Approach road"));
        Spinner approach = spinner(new String[]{"South", "West", "North", "East"});
        panel.addView(approach);

        panel.addView(label("Exit"));
        Spinner exit = spinner(new String[]{"1st exit / left", "2nd exit / ahead", "3rd exit / right"});
        panel.addView(exit);

        CheckBox markings = new CheckBox(this);
        markings.setText("Show lane markings and arrows");
        markings.setChecked(true);
        markings.setTextSize(16);
        panel.addView(markings);

        Button simulate = new Button(this);
        simulate.setText("START DRIVING DEMO");
        simulate.setTextSize(17);
        panel.addView(simulate);

        TextView advice = new TextView(this);
        advice.setTextSize(16);
        advice.setPadding(0, 18, 0, 8);
        panel.addView(advice);

        TextView tip = new TextView(this);
        tip.setText("Training guide only. At real roundabouts always follow signs, traffic lights, road markings and local lane arrows. Give priority to traffic from the right unless directed otherwise.");
        tip.setTextSize(14);
        tip.setPadding(0, 14, 0, 0);
        panel.addView(tip);

        Runnable update = () -> {
            roadView.approach = approach.getSelectedItemPosition();
            roadView.exit = exit.getSelectedItemPosition() + 1;
            roadView.showMarkings = markings.isChecked();
            roadView.resetCar();
            advice.setText(buildAdvice(roadView.exit));
        };

        simulate.setOnClickListener(v -> {
            update.run();
            roadView.startCar();
        });
        markings.setOnClickListener(v -> update.run());
        approach.setOnItemSelectedListener(simpleSelection(update));
        exit.setOnItemSelectedListener(simpleSelection(update));
        advice.setText(buildAdvice(2));

        setContentView(root);
    }

    private AdapterView.OnItemSelectedListener simpleSelection(Runnable r) {
        return new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { r.run(); }
            public void onNothingSelected(AdapterView<?> parent) { }
        };
    }

    private String buildAdvice(int exit) {
        if (exit == 1) {
            return "1st exit / LEFT\n• Mirrors before changing speed or position\n• Signal LEFT on approach\n• Normally approach in the LEFT lane unless signs/markings say otherwise\n• Keep to the outer lane and leave at the first exit";
        }
        if (exit == 2) {
            return "2nd exit / AHEAD\n• Mirrors first\n• Usually NO signal on approach\n• Follow lane arrows; normally use the left lane when no markings indicate otherwise\n• Signal LEFT after passing the exit before yours";
        }
        return "3rd exit / RIGHT\n• Mirrors first\n• Signal RIGHT on approach\n• Normally approach in the RIGHT lane unless signs/markings say otherwise\n• Use the inner lane, then move outward when safe and signal LEFT before leaving";
    }

    private TextView label(String s) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(17);
        v.setTypeface(null, Typeface.BOLD);
        v.setPadding(0, 10, 0, 5);
        return v;
    }

    private Spinner spinner(String[] values) {
        Spinner s = new Spinner(this);
        s.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, values));
        return s;
    }

    static class RoundaboutView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint routePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int approach = 0;
        int exit = 2;
        boolean showMarkings = true;
        private float carProgress = 0f;
        private ValueAnimator animator;
        private Path route = new Path();

        RoundaboutView(Context c) {
            super(c);
            routePaint.setColor(Color.rgb(255, 210, 0));
            routePaint.setStyle(Paint.Style.STROKE);
            routePaint.setStrokeWidth(9);
            routePaint.setStrokeCap(Paint.Cap.ROUND);
        }

        void resetCar() {
            if (animator != null) animator.cancel();
            carProgress = 0f;
            invalidate();
        }

        void startCar() {
            if (getWidth() == 0 || getHeight() == 0) return;
            if (animator != null) animator.cancel();
            carProgress = 0f;
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(5200);
            animator.addUpdateListener(a -> {
                carProgress = (float) a.getAnimatedValue();
                invalidate();
            });
            animator.start();
        }

        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float w = getWidth(), h = getHeight();
            float cx = w / 2f, cy = h / 2f;
            float r = Math.min(w, h) * 0.215f;
            float roadWidth = r * 0.98f;
            float lane = roadWidth / 4f;

            c.drawColor(Color.rgb(82, 145, 78));

            p.setColor(Color.rgb(60, 60, 60));
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(roadWidth);
            c.drawCircle(cx, cy, r, p);

            p.setStyle(Paint.Style.FILL);
            c.drawRect(cx - roadWidth / 2, cy + r, cx + roadWidth / 2, h, p);
            c.drawRect(cx - roadWidth / 2, 0, cx + roadWidth / 2, cy - r, p);
            c.drawRect(0, cy - roadWidth / 2, cx - r, cy + roadWidth / 2, p);
            c.drawRect(cx + r, cy - roadWidth / 2, w, cy + roadWidth / 2, p);

            p.setColor(Color.rgb(67, 125, 62));
            p.setStyle(Paint.Style.FILL);
            c.drawCircle(cx, cy, r - roadWidth * 0.54f, p);

            if (showMarkings) drawMarkings(c, cx, cy, r, roadWidth, w, h);

            c.save();
            c.rotate(approach * 90f, cx, cy);
            route = buildRoute(cx, cy, r, roadWidth, w, h, exit);
            c.drawPath(route, routePaint);
            drawGiveWay(c, cx, cy + r + roadWidth * 0.31f, roadWidth);
            drawLaneArrows(c, cx, cy, r, roadWidth, h);
            drawCar(c, route, carProgress, lane);
            c.restore();

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.WHITE);
            p.setTextSize(22);
            p.setTypeface(Typeface.DEFAULT_BOLD);
            c.drawText("UK • CLOCKWISE", 16, 31, p);
        }

        private Path buildRoute(float cx, float cy, float r, float roadWidth, float w, float h, int chosenExit) {
            Path path = new Path();
            float entryX = cx - roadWidth * 0.23f; // left side of the south approach when travelling north
            float outerR = r + roadWidth * 0.20f;
            float innerR = r - roadWidth * 0.20f;
            float useR = chosenExit == 3 ? innerR : outerR;

            path.moveTo(entryX, h + 10);
            path.lineTo(entryX, cy + useR + roadWidth * 0.25f);
            path.quadTo(entryX, cy + useR, cx, cy + useR);

            RectF oval = new RectF(cx - useR, cy - useR, cx + useR, cy + useR);
            path.arcTo(oval, 90f, chosenExit * 90f);

            float outOffset = roadWidth * 0.23f;
            if (chosenExit == 1) {
                path.quadTo(cx - useR, cy + outOffset, cx - useR - roadWidth * 0.2f, cy + outOffset);
                path.lineTo(-10, cy + outOffset);
            } else if (chosenExit == 2) {
                path.quadTo(cx - outOffset, cy - useR, cx - outOffset, cy - useR - roadWidth * 0.2f);
                path.lineTo(cx - outOffset, -10);
            } else {
                path.quadTo(cx + useR, cy - outOffset, cx + useR + roadWidth * 0.2f, cy - outOffset);
                path.lineTo(w + 10, cy - outOffset);
            }
            return path;
        }

        private void drawMarkings(Canvas c, float cx, float cy, float r, float roadWidth, float w, float h) {
            p.setColor(Color.WHITE);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3.5f);
            p.setPathEffect(new DashPathEffect(new float[]{17, 15}, 0));
            c.drawCircle(cx, cy, r, p);
            c.drawLine(cx, cy + r + 5, cx, h, p);
            c.drawLine(cx, 0, cx, cy - r - 5, p);
            c.drawLine(0, cy, cx - r - 5, cy, p);
            c.drawLine(cx + r + 5, cy, w, cy, p);
            p.setPathEffect(null);
        }

        private void drawGiveWay(Canvas c, float cx, float y, float roadWidth) {
            p.setColor(Color.WHITE);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(5);
            p.setPathEffect(new DashPathEffect(new float[]{14, 9}, 0));
            c.drawLine(cx - roadWidth * 0.47f, y, cx + roadWidth * 0.47f, y, p);
            p.setPathEffect(null);
        }

        private void drawLaneArrows(Canvas c, float cx, float cy, float r, float roadWidth, float h) {
            p.setColor(Color.WHITE);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(5);
            p.setStrokeCap(Paint.Cap.SQUARE);
            float y = Math.min(h - 65, cy + r + roadWidth * 0.95f);
            float leftX = cx - roadWidth * 0.24f;
            float rightX = cx + roadWidth * 0.24f;
            drawArrow(c, leftX, y, -90, true);
            drawArrow(c, rightX, y, -90, false);
        }

        private void drawArrow(Canvas c, float x, float y, float angle, boolean leftChoice) {
            c.save();
            c.rotate(angle, x, y);
            c.drawLine(x - 22, y, x + 24, y, p);
            c.drawLine(x + 24, y, x + 10, y - 12, p);
            c.drawLine(x + 24, y, x + 10, y + 12, p);
            if (leftChoice) {
                c.drawLine(x, y, x, y - 25, p);
                c.drawLine(x, y - 25, x - 10, y - 14, p);
            } else {
                c.drawLine(x, y, x, y + 25, p);
                c.drawLine(x, y + 25, x - 10, y + 14, p);
            }
            c.restore();
        }

        private void drawCar(Canvas c, Path path, float progress, float lane) {
            PathMeasure pm = new PathMeasure(path, false);
            float distance = pm.getLength() * progress;
            float[] pos = new float[2];
            float[] tan = new float[2];
            if (!pm.getPosTan(distance, pos, tan)) return;
            float angle = (float)Math.toDegrees(Math.atan2(tan[1], tan[0]));

            c.save();
            c.translate(pos[0], pos[1]);
            c.rotate(angle);
            float carL = lane * 0.75f;
            float carW = lane * 0.42f;
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(40, 120, 220));
            c.drawRoundRect(-carL/2, -carW/2, carL/2, carW/2, 8, 8, p);
            p.setColor(Color.rgb(185, 225, 255));
            c.drawRect(-carL*0.12f, -carW*0.38f, carL*0.20f, carW*0.38f, p);
            p.setColor(Color.WHITE);
            Path nose = new Path();
            nose.moveTo(carL/2 + 7, 0);
            nose.lineTo(carL/2 - 5, -7);
            nose.lineTo(carL/2 - 5, 7);
            nose.close();
            c.drawPath(nose, p);
            c.restore();
        }
    }
}
