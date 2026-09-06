package uk.co.roundaboutstrainer;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.view.animation.LinearInterpolator;
import android.widget.*;

public class TemplatePreviewActivity extends Activity {
    @Override public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setBackgroundColor(Color.rgb(20, 24, 28));

        FrameLayout scene = new FrameLayout(this);
        ImageView background = new ImageView(this);
        background.setImageResource(R.drawable.roundabout_template);
        background.setScaleType(ImageView.ScaleType.CENTER_CROP);
        scene.addView(background, new FrameLayout.LayoutParams(-1, -1));

        RouteOverlay overlay = new RouteOverlay(this);
        scene.addView(overlay, new FrameLayout.LayoutParams(-1, -1));
        root.addView(scene, new LinearLayout.LayoutParams(0, -1, 4));

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(28, 24, 28, 24);
        panel.setBackgroundColor(Color.rgb(28, 33, 38));
        root.addView(panel, new LinearLayout.LayoutParams(0, -1, 2));

        panel.addView(text("UK ROUNDABOUTS", 25, true));
        panel.addView(text("Simple lane trainer", 14, false));

        panel.addView(space(18));
        panel.addView(text("Roundabout", 13, true));
        Spinner type = spinner(new String[]{"2 Lane Roundabout", "Spiral Roundabout"});
        panel.addView(type);

        panel.addView(space(12));
        panel.addView(text("Select exit", 13, true));
        Spinner exit = spinner(new String[]{"1st Exit • Left", "2nd Exit • Ahead", "3rd Exit • Right", "4th Exit • U-turn"});
        exit.setSelection(1);
        panel.addView(exit);

        panel.addView(space(12));
        Switch route = new Switch(this);
        route.setText("Show Route");
        route.setTextColor(Color.WHITE);
        route.setChecked(true);
        panel.addView(route);

        Switch arrows = new Switch(this);
        arrows.setText("Show Lane Guide");
        arrows.setTextColor(Color.WHITE);
        arrows.setChecked(true);
        panel.addView(arrows);

        panel.addView(space(16));
        Button start = new Button(this);
        start.setText("START");
        start.setTextSize(18);
        panel.addView(start, new LinearLayout.LayoutParams(-1, 64));

        panel.addView(space(14));
        TextView info = text("2nd Exit • use the lane shown by the road markings", 14, true);
        panel.addView(info);
        TextView note = text("The road markings will be rebuilt as controlled app graphics. This background is the visual style base only.", 12, false);
        note.setPadding(0, 10, 0, 0);
        panel.addView(note);

        Runnable sync = () -> {
            overlay.spiral = type.getSelectedItemPosition() == 1;
            overlay.exit = exit.getSelectedItemPosition() + 1;
            overlay.showRoute = route.isChecked();
            overlay.showGuide = arrows.isChecked();
            overlay.reset();
            String road = overlay.spiral ? "Spiral" : "2 Lane";
            info.setText(road + " • " + exit.getSelectedItem().toString());
        };

        type.setOnItemSelectedListener(listener(sync));
        exit.setOnItemSelectedListener(listener(sync));
        route.setOnCheckedChangeListener((b, checked) -> sync.run());
        arrows.setOnCheckedChangeListener((b, checked) -> sync.run());
        start.setOnClickListener(v -> { sync.run(); overlay.start(); });

        setContentView(root);
    }

    private AdapterView.OnItemSelectedListener listener(Runnable r) {
        return new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { r.run(); }
            @Override public void onNothingSelected(AdapterView<?> p) { }
        };
    }

    private Spinner spinner(String[] values) {
        Spinner s = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, values);
        s.setAdapter(adapter);
        return s;
    }

    private TextView text(String value, int size, boolean bold) {
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(size);
        v.setTextColor(Color.WHITE);
        if (bold) v.setTypeface(null, Typeface.BOLD);
        return v;
    }

    private Space space(int h) {
        Space s = new Space(this);
        s.setLayoutParams(new LinearLayout.LayoutParams(1, h));
        return s;
    }

    static class RouteOverlay extends View {
        final Paint routePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint guidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint carPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint glassPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Path path = new Path();
        final Path guide = new Path();
        final PathMeasure measure = new PathMeasure();
        final float[] pos = new float[2];
        final float[] tan = new float[2];
        boolean spiral = false;
        boolean showRoute = true;
        boolean showGuide = true;
        int exit = 2;
        float progress = 0f;
        ValueAnimator animator;

        RouteOverlay(Context context) {
            super(context);
            routePaint.setColor(Color.argb(215, 55, 175, 255));
            routePaint.setStyle(Paint.Style.STROKE);
            routePaint.setStrokeCap(Paint.Cap.ROUND);
            routePaint.setStrokeJoin(Paint.Join.ROUND);
            guidePaint.setColor(Color.argb(190, 255, 255, 255));
            guidePaint.setStyle(Paint.Style.STROKE);
            guidePaint.setStrokeCap(Paint.Cap.ROUND);
            carPaint.setColor(Color.rgb(38, 101, 180));
            glassPaint.setColor(Color.rgb(185, 220, 238));
        }

        void reset() {
            if (animator != null) animator.cancel();
            progress = 0f;
            invalidate();
        }

        void start() {
            if (animator != null) animator.cancel();
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(6500);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(a -> {
                progress = (float) a.getAnimatedValue();
                invalidate();
            });
            animator.start();
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            if (w <= 0 || h <= 0) return;
            float s = Math.min(w, h);
            float l = (w - s) / 2f;
            float t = (h - s) / 2f;

            routePaint.setStrokeWidth(s * 0.018f);
            guidePaint.setStrokeWidth(s * 0.007f);
            buildPath(l, t, s);

            if (showGuide) canvas.drawPath(guide, guidePaint);
            if (showRoute) canvas.drawPath(path, routePaint);

            measure.setPath(path, false);
            float d = measure.getLength() * progress;
            if (measure.getPosTan(d, pos, tan)) drawCar(canvas, s);
        }

        void buildPath(float l, float t, float s) {
            path.reset();
            guide.reset();
            float startX = spiral ? .55f : (exit >= 3 ? .55f : .45f);
            path.moveTo(l + s * startX, t + s * .99f);
            path.cubicTo(l + s * startX, t + s * .84f, l + s * .50f, t + s * .72f, l + s * .43f, t + s * .66f);

            if (exit == 1) {
                path.cubicTo(l + s * .36f, t + s * .63f, l + s * .28f, t + s * .58f, l + s * .01f, t + s * .56f);
            } else if (exit == 2) {
                path.cubicTo(l + s * .31f, t + s * .58f, l + s * .30f, t + s * .43f, l + s * .39f, t + s * .32f);
                path.cubicTo(l + s * .44f, t + s * .26f, l + s * .45f, t + s * .12f, l + s * .45f, t + s * .01f);
            } else if (exit == 3) {
                path.cubicTo(l + s * .34f, t + s * .57f, l + s * .33f, t + s * .40f, l + s * .47f, t + s * .34f);
                path.cubicTo(l + s * .62f, t + s * .28f, l + s * .70f, t + s * .39f, l + s * .69f, t + s * .49f);
                path.cubicTo(l + s * .69f, t + s * .55f, l + s * .82f, t + s * .55f, l + s * .99f, t + s * .55f);
            } else {
                path.cubicTo(l + s * .34f, t + s * .57f, l + s * .33f, t + s * .39f, l + s * .50f, t + s * .32f);
                path.cubicTo(l + s * .69f, t + s * .25f, l + s * .76f, t + s * .44f, l + s * .66f, t + s * .60f);
                path.cubicTo(l + s * .58f, t + s * .72f, l + s * .53f, t + s * .82f, l + s * .53f, t + s * .99f);
            }

            if (spiral) {
                guide.moveTo(l + s * .55f, t + s * .94f);
                guide.cubicTo(l + s * .55f, t + s * .73f, l + s * .50f, t + s * .62f, l + s * .42f, t + s * .55f);
                guide.cubicTo(l + s * .36f, t + s * .48f, l + s * .40f, t + s * .36f, l + s * .51f, t + s * .34f);
                guide.cubicTo(l + s * .60f, t + s * .32f, l + s * .66f, t + s * .39f, l + s * .68f, t + s * .48f);
            }
        }

        void drawCar(Canvas c, float s) {
            float angle = (float) Math.toDegrees(Math.atan2(tan[1], tan[0])) + 90f;
            c.save();
            c.translate(pos[0], pos[1]);
            c.rotate(angle);
            float cw = s * .035f;
            float ch = cw * 1.8f;
            c.drawRoundRect(new RectF(-cw/2, -ch/2, cw/2, ch/2), cw*.22f, cw*.22f, carPaint);
            c.drawRoundRect(new RectF(-cw*.32f, -ch*.18f, cw*.32f, ch*.10f), cw*.08f, cw*.08f, glassPaint);
            c.restore();
        }
    }
}
