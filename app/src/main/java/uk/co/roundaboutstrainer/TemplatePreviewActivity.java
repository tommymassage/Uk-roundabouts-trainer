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
        panel.setPadding(24, 20, 24, 28);
        panel.setGravity(Gravity.TOP);
        scroll.addView(panel);
        root.addView(scroll, new LinearLayout.LayoutParams(0, -1, 2));

        panel.addView(label("UK ROUNDABOUTS TRAINER  v0.7.6", 24, true));
        TextView sub = label("GUIDED • PRACTICE • TEST", 14, true);
        sub.setPadding(0, 6, 0, 14);
        panel.addView(sub);

        panel.addView(label("Mode", 13, true));
        Spinner mode = spinner(new String[]{"Guided demo", "Practice", "Test mode"});
        panel.addView(mode);

        panel.addView(label("Approach road", 13, true));
        Spinner approach = spinner(new String[]{"South", "West", "North", "East"});
        panel.addView(approach);

        panel.addView(label("Exit", 13, true));
        Spinner exit = spinner(new String[]{"1st exit / left", "2nd exit / ahead", "3rd exit / right"});
        exit.setSelection(1);
        panel.addView(exit);

        panel.addView(label("Traffic", 13, true));
        Spinner traffic = spinner(new String[]{"Clear roundabout", "Vehicle from the right", "Busy circulating traffic"});
        panel.addView(traffic);

        panel.addView(label("Your lane on approach", 13, true));
        Spinner lane = spinner(new String[]{"Left lane", "Right lane"});
        panel.addView(lane);

        panel.addView(label("Your signal on approach", 13, true));
        Spinner signal = spinner(new String[]{"No signal", "Signal left", "Signal right"});
        panel.addView(signal);

        CheckBox routeToggle = new CheckBox(this);
        routeToggle.setText("Show training route");
        routeToggle.setChecked(true);
        panel.addView(routeToggle);

        CheckBox laneGuideToggle = new CheckBox(this);
        laneGuideToggle.setText("Show lane guide");
        laneGuideToggle.setChecked(true);
        panel.addView(laneGuideToggle);

        Button start = new Button(this);
        start.setText("▶  START DRIVING DEMO");
        panel.addView(start, new LinearLayout.LayoutParams(-1, 58));

        TextView stage = label("Ready • MIRRORS", 17, true);
        stage.setPadding(0, 14, 0, 5);
        panel.addView(stage);
        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        panel.addView(progress, new LinearLayout.LayoutParams(-1, 16));

        TextView result = label("Training result: —", 14, true);
        result.setPadding(0, 10, 0, 5);
        panel.addView(result);
        TextView feedback = label("Choose a route and press START.", 13, false);
        panel.addView(feedback);

        TextView rule = label("UK training rule: give priority to traffic from the right unless signs, signals or road markings direct otherwise. Always follow the actual signs and markings.", 11, false);
        rule.setPadding(0, 14, 0, 0);
        panel.addView(rule);

        overlay.listener = (text, value, done, score, message) -> {
            stage.setText(text);
            progress.setProgress(value);
            if (done) {
                result.setText("Training result: " + score + "/100");
                feedback.setText(message);
            }
        };

        Runnable sync = () -> {
            overlay.mode = mode.getSelectedItemPosition();
            overlay.approach = approach.getSelectedItemPosition();
            overlay.exit = exit.getSelectedItemPosition() + 1;
            overlay.trafficMode = traffic.getSelectedItemPosition();
            overlay.chosenLane = lane.getSelectedItemPosition();
            overlay.chosenSignal = signal.getSelectedItemPosition();
            overlay.showRoute = routeToggle.isChecked() && overlay.mode != 2;
            overlay.showLaneGuide = laneGuideToggle.isChecked() && overlay.mode != 2;
            overlay.reset();
            stage.setText("Ready • MIRRORS");
            progress.setProgress(0);
            result.setText("Training result: —");
            feedback.setText(overlay.mode == 2 ? "Test mode: route and lane guide are hidden." : "Choose a route and press START.");
        };

        AdapterView.OnItemSelectedListener select = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int x, long id) { sync.run(); }
            @Override public void onNothingSelected(AdapterView<?> p) { }
        };
        mode.setOnItemSelectedListener(select);
        approach.setOnItemSelectedListener(select);
        exit.setOnItemSelectedListener(select);
        traffic.setOnItemSelectedListener(select);
        lane.setOnItemSelectedListener(select);
        signal.setOnItemSelectedListener(select);
        routeToggle.setOnCheckedChangeListener((b, checked) -> sync.run());
        laneGuideToggle.setOnCheckedChangeListener((b, checked) -> sync.run());
        start.setOnClickListener(v -> { sync.run(); overlay.start(); });

        setContentView(root);
    }

    private Spinner spinner(String[] values) {
        Spinner s = new Spinner(this);
        s.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, values));
        return s;
    }

    private TextView label(String text, int size, boolean bold) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(size);
        v.setTextColor(Color.rgb(28, 38, 47));
        if (bold) v.setTypeface(null, 1);
        return v;
    }

    interface StageListener { void onStage(String text, int progress, boolean done, int score, String message); }

    static class TrainingOverlay extends View {
        final Paint routeShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint routePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint laneGuide = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint carPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint glassPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint trafficPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint traffic2Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint amberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Path route = new Path();
        final Path trafficPath = new Path();
        final PathMeasure measure = new PathMeasure();
        final float[] pos = new float[2];
        final float[] tan = new float[2];

        boolean showRoute = true, showLaneGuide = true;
        int mode = 0, trafficMode = 0, approach = 0, exit = 2, chosenLane = 0, chosenSignal = 0;
        float raw = 0f, progress = 0f, trafficProgress = .05f;
        ValueAnimator animator;
        StageListener listener;

        TrainingOverlay(Context c) {
            super(c);
            setBackgroundColor(Color.TRANSPARENT);
            routeShadow.setColor(Color.argb(110, 0, 0, 0)); routeShadow.setStyle(Paint.Style.STROKE); routeShadow.setStrokeCap(Paint.Cap.ROUND); routeShadow.setStrokeJoin(Paint.Join.ROUND);
            routePaint.setColor(Color.rgb(25, 220, 75)); routePaint.setStyle(Paint.Style.STROKE); routePaint.setStrokeCap(Paint.Cap.ROUND); routePaint.setStrokeJoin(Paint.Join.ROUND);
            laneGuide.setColor(Color.argb(200, 70, 160, 255)); laneGuide.setStyle(Paint.Style.STROKE); laneGuide.setStrokeCap(Paint.Cap.ROUND);
            carPaint.setColor(Color.rgb(30, 103, 225)); glassPaint.setColor(Color.rgb(185, 228, 255));
            trafficPaint.setColor(Color.rgb(210, 52, 52)); traffic2Paint.setColor(Color.rgb(245, 145, 35)); amberPaint.setColor(Color.rgb(255, 175, 35));
        }

        void reset() {
            if (animator != null) animator.cancel();
            raw = progress = 0f; trafficProgress = .05f; invalidate();
        }

        void start() {
            if (animator != null) animator.cancel();
            raw = progress = 0f;
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(trafficMode == 0 ? 7200 : trafficMode == 1 ? 9000 : 10500);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(a -> {
                raw = (float)a.getAnimatedValue();
                trafficProgress = (raw * 1.18f + .04f) % 1f;
                float holdStart=.22f, holdEnd=trafficMode==1?.43f:.57f;
                if (trafficMode==0) progress=raw;
                else if (raw<holdStart) progress=raw/holdStart*.18f;
                else if (raw<holdEnd) progress=.18f;
                else progress=.18f+(raw-holdEnd)/(1f-holdEnd)*.82f;
                progress=Math.max(0f,Math.min(1f,progress));
                report(); invalidate();
            });
            animator.start();
        }

        int expectedSignal() { return exit==1 ? 1 : exit==3 ? 2 : 0; }
        int expectedLane() { return exit==3 ? 1 : 0; }
        int score() {
            int s=100;
            if (chosenSignal!=expectedSignal()) s-=25;
            if (chosenLane!=expectedLane()) s-=25;
            return s;
        }
        String feedback() {
            StringBuilder b=new StringBuilder();
            if (chosenSignal==expectedSignal()) b.append("Approach signal: good. "); else b.append("Check your approach signal for this exit. ");
            if (chosenLane==expectedLane()) b.append("Lane choice: good. "); else b.append("Review lane choice; always follow signs and road markings. ");
            if (trafficMode>0) b.append("You waited for circulating traffic from the right.");
            return b.toString();
        }

        void report() {
            if (listener==null) return;
            String s;
            if(raw<.12f)s="1/6 • MIRRORS";
            else if(raw<.22f)s=exit==1?"2/6 • SIGNAL LEFT":exit==3?"2/6 • SIGNAL RIGHT":"2/6 • SIGNAL normally none";
            else if(raw<.32f)s="3/6 • POSITION • " + (expectedLane()==0?"LEFT LANE":"RIGHT LANE");
            else if(trafficMode>0 && progress<=.181f)s="4/6 • SPEED • WAIT • GIVE WAY TO RIGHT";
            else if(raw<.58f)s="4/6 • SPEED • prepare to enter";
            else if(raw<.70f)s="5/6 • LOOK RIGHT • safe gap";
            else if(raw<.88f)s="ON ROUNDABOUT • lane discipline";
            else if(raw<.98f)s="6/6 • SIGNAL LEFT • EXIT";
            else s="COMPLETE • safe exit";
            boolean done=raw>=.995f;
            listener.onStage(s,Math.round(raw*100),done,score(),feedback());
        }

        float X(float l,float s,float n){return l+s*n;} float Y(float t,float s,float n){return t+s*n;}

        void buildSouthRoute(float l,float t,float s){
            route.reset();
            float startX = chosenLane==0 ? .435f : .565f;
            route.moveTo(X(l,s,startX),Y(t,s,.995f));
            if(chosenLane==0) route.cubicTo(X(l,s,.435f),Y(t,s,.86f),X(l,s,.425f),Y(t,s,.735f),X(l,s,.392f),Y(t,s,.665f));
            else route.cubicTo(X(l,s,.565f),Y(t,s,.86f),X(l,s,.555f),Y(t,s,.735f),X(l,s,.505f),Y(t,s,.650f));
            if(exit==1){
                route.cubicTo(X(l,s,.340f),Y(t,s,.650f),X(l,s,.300f),Y(t,s,.620f),X(l,s,.270f),Y(t,s,.565f));
                route.cubicTo(X(l,s,.205f),Y(t,s,.555f),X(l,s,.110f),Y(t,s,.555f),X(l,s,.005f),Y(t,s,.555f));
            } else if(exit==2){
                route.cubicTo(X(l,s,.315f),Y(t,s,.625f),X(l,s,.270f),Y(t,s,.555f),X(l,s,.270f),Y(t,s,.490f));
                route.cubicTo(X(l,s,.270f),Y(t,s,.390f),X(l,s,.335f),Y(t,s,.315f),X(l,s,.410f),Y(t,s,.285f));
                route.cubicTo(X(l,s,.435f),Y(t,s,.225f),X(l,s,.435f),Y(t,s,.120f),X(l,s,.435f),Y(t,s,.005f));
            } else {
                route.cubicTo(X(l,s,.435f),Y(t,s,.610f),X(l,s,.355f),Y(t,s,.555f),X(l,s,.345f),Y(t,s,.470f));
                route.cubicTo(X(l,s,.335f),Y(t,s,.350f),X(l,s,.410f),Y(t,s,.330f),X(l,s,.500f),Y(t,s,.330f));
                route.cubicTo(X(l,s,.610f),Y(t,s,.330f),X(l,s,.665f),Y(t,s,.390f),X(l,s,.675f),Y(t,s,.480f));
                route.cubicTo(X(l,s,.700f),Y(t,s,.535f),X(l,s,.835f),Y(t,s,.545f),X(l,s,.995f),Y(t,s,.545f));
            }
        }

        void buildTrafficPath(float l,float t,float s){ trafficPath.reset(); trafficPath.addArc(new RectF(X(l,s,.285f),Y(t,s,.285f),X(l,s,.715f),Y(t,s,.715f)),90f,-359f); }

        void drawCar(Canvas c,float s,Paint body,boolean learner){
            float angle=(float)Math.toDegrees(Math.atan2(tan[1],tan[0]))+90f;
            c.save(); c.translate(pos[0],pos[1]); c.rotate(angle);
            float cw=s*.030f,ch=cw*1.75f;
            c.drawRoundRect(new RectF(-cw/2,-ch/2,cw/2,ch/2),cw*.22f,cw*.22f,body);
            if(learner){
                c.drawRoundRect(new RectF(-cw*.33f,-ch*.18f,cw*.33f,ch*.08f),cw*.08f,cw*.08f,glassPaint);
                boolean blink=((int)(raw*40))%2==0;
                if(blink){
                    int sig = raw<.32f ? chosenSignal : raw>.84f ? 1 : 0;
                    if(sig==1)c.drawCircle(-cw*.45f,-ch*.32f,cw*.11f,amberPaint);
                    if(sig==2)c.drawCircle(cw*.45f,-ch*.32f,cw*.11f,amberPaint);
                }
            }
            c.restore();
        }

        @Override protected void onDraw(Canvas c){
            super.onDraw(c); float w=getWidth(),h=getHeight(); if(w<=0||h<=0)return;
            float s=Math.min(w,h),l=(w-s)/2f,t=(h-s)/2f,cx=l+s/2f,cy=t+s/2f;
            buildSouthRoute(l,t,s); buildTrafficPath(l,t,s);
            routeShadow.setStrokeWidth(s*.024f); routePaint.setStrokeWidth(s*.013f); laneGuide.setStrokeWidth(s*.007f);
            c.save(); c.rotate(approach*90f,cx,cy);
            if(showLaneGuide){
                float lx=chosenLane==0?.435f:.565f;
                c.drawLine(X(l,s,lx),Y(t,s,.98f),X(l,s,lx),Y(t,s,.72f),laneGuide);
            }
            if(showRoute){c.drawPath(route,routeShadow);c.drawPath(route,routePaint);}
            measure.setPath(route,false); measure.getPosTan(measure.getLength()*progress,pos,tan); drawCar(c,s,carPaint,true);
            if(trafficMode>0){
                measure.setPath(trafficPath,false); measure.getPosTan(measure.getLength()*trafficProgress,pos,tan); drawCar(c,s,trafficPaint,false);
                if(trafficMode==2){
                    float p2=(trafficProgress+.34f)%1f; measure.getPosTan(measure.getLength()*p2,pos,tan); drawCar(c,s,traffic2Paint,false);
                    float p3=(trafficProgress+.67f)%1f; measure.getPosTan(measure.getLength()*p3,pos,tan); drawCar(c,s,trafficPaint,false);
                }
            }
            c.restore();
        }
    }
}
