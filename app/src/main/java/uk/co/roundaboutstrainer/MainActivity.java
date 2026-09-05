package uk.co.roundaboutstrainer;

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
        root.setPadding(24, 24, 24, 24);
        root.setBackgroundColor(Color.rgb(238, 245, 238));

        roadView = new RoundaboutView(this);
        root.addView(roadView, new LinearLayout.LayoutParams(0, -1, 3));

        ScrollView scroll = new ScrollView(this);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(28, 12, 18, 12);
        scroll.addView(panel);
        root.addView(scroll, new LinearLayout.LayoutParams(0, -1, 2));

        TextView title = new TextView(this);
        title.setText("UK ROUNDABOUTS TRAINER");
        title.setTextSize(25);
        title.setTypeface(null, Typeface.BOLD);
        panel.addView(title);

        TextView info = new TextView(this);
        info.setText("Choose your approach and exit.\nPractise Mirrors • Signal • Position • Speed • Look.");
        info.setTextSize(17);
        info.setPadding(0, 16, 0, 20);
        panel.addView(info);

        panel.addView(label("Approach road"));
        Spinner approach = spinner(new String[]{"South", "West", "North", "East"});
        panel.addView(approach);

        panel.addView(label("Exit"));
        Spinner exit = spinner(new String[]{"1st exit / left", "2nd exit / ahead", "3rd exit / right"});
        panel.addView(exit);

        CheckBox markings = new CheckBox(this);
        markings.setText("Show lane markings");
        markings.setChecked(true);
        markings.setTextSize(16);
        panel.addView(markings);

        Button simulate = new Button(this);
        simulate.setText("SHOW DRIVING PATH");
        simulate.setTextSize(17);
        panel.addView(simulate);

        TextView advice = new TextView(this);
        advice.setTextSize(17);
        advice.setPadding(0, 22, 0, 12);
        panel.addView(advice);

        TextView tip = new TextView(this);
        tip.setText("UK reminder: give priority to traffic approaching from the right unless signs, signals or road markings tell you otherwise. Always follow local lane arrows and signs.");
        tip.setTextSize(15);
        tip.setPadding(0, 18, 0, 0);
        panel.addView(tip);

        View.OnClickListener update = v -> {
            roadView.approach = approach.getSelectedItemPosition();
            roadView.exit = exit.getSelectedItemPosition() + 1;
            roadView.showMarkings = markings.isChecked();
            roadView.invalidate();
            advice.setText(buildAdvice(roadView.exit));
        };

        simulate.setOnClickListener(update);
        markings.setOnClickListener(update);
        advice.setText(buildAdvice(2));

        setContentView(root);
    }

    private String buildAdvice(int exit) {
        if (exit == 1) {
            return "1st exit / left\n• Mirrors first\n• Signal left on approach\n• Normally use the left lane unless markings say otherwise\n• Keep left and leave at the first exit";
        }
        if (exit == 2) {
            return "2nd exit / ahead\n• Mirrors first\n• Usually no signal on approach\n• Use the lane shown by road markings; normally left lane if no markings indicate otherwise\n• Signal left after passing the exit before yours";
        }
        return "3rd exit / right\n• Mirrors first\n• Signal right on approach\n• Normally use the right lane unless markings say otherwise\n• Keep right around the roundabout, then signal left after passing the exit before yours";
    }

    private TextView label(String s) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(18);
        v.setTypeface(null, Typeface.BOLD);
        v.setPadding(0, 12, 0, 6);
        return v;
    }

    private Spinner spinner(String[] values) {
        Spinner s = new Spinner(this);
        s.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, values));
        return s;
    }

    static class RoundaboutView extends View {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        int approach = 0;
        int exit = 2;
        boolean showMarkings = true;

        RoundaboutView(Context c) {
            super(c);
        }

        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float w = getWidth(), h = getHeight();
            float cx = w / 2f, cy = h / 2f;
            float r = Math.min(w, h) * 0.22f;
            float roadWidth = r * 0.70f;

            c.drawColor(Color.rgb(87, 145, 82));

            p.setColor(Color.rgb(63, 63, 63));
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(roadWidth);
            c.drawCircle(cx, cy, r, p);

            p.setStyle(Paint.Style.FILL);
            c.drawRect(cx - roadWidth / 2, cy + r, cx + roadWidth / 2, h, p);
            c.drawRect(cx - roadWidth / 2, 0, cx + roadWidth / 2, cy - r, p);
            c.drawRect(0, cy - roadWidth / 2, cx - r, cy + roadWidth / 2, p);
            c.drawRect(cx + r, cy - roadWidth / 2, w, cy + roadWidth / 2, p);

            p.setColor(Color.rgb(70, 125, 65));
            p.setStyle(Paint.Style.FILL);
            c.drawCircle(cx, cy, r - roadWidth * 0.56f, p);

            if (showMarkings) {
                p.setColor(Color.WHITE);
                p.setStrokeWidth(4);
                p.setStyle(Paint.Style.STROKE);
                p.setPathEffect(new DashPathEffect(new float[]{18, 16}, 0));
                c.drawCircle(cx, cy, r, p);
                c.drawLine(cx, cy + r + 8, cx, h, p);
                c.drawLine(cx, 0, cx, cy - r - 8, p);
                c.drawLine(0, cy, cx - r - 8, cy, p);
                c.drawLine(cx + r + 8, cy, w, cy, p);
                p.setPathEffect(null);
            }

            c.save();
            c.rotate(approach * 90f, cx, cy);

            p.setColor(Color.rgb(255, 210, 0));
            p.setStrokeWidth(12);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStyle(Paint.Style.STROKE);

            Path path = new Path();
            path.moveTo(cx, h);
            path.lineTo(cx, cy + r);
            RectF oval = new RectF(cx - r, cy - r, cx + r, cy + r);
            float sweep = exit * 90f;
            path.arcTo(oval, 90f, sweep);

            if (exit == 1) path.lineTo(0, cy);
            else if (exit == 2) path.lineTo(cx, 0);
            else path.lineTo(w, cy);

            c.drawPath(path, p);
            c.restore();

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.WHITE);
            p.setTextSize(24);
            p.setTypeface(Typeface.DEFAULT_BOLD);
            c.drawText("UK: clockwise", 18, 34, p);
        }
    }
}
