package uk.co.roundaboutstrainer;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.*;

public class TracedSimulationActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);

        FrameLayout root = new FrameLayout(this);

        ImageView bg = new ImageView(this);
        bg.setImageResource(R.drawable.roundabout_template);
        bg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(bg, new FrameLayout.LayoutParams(-1,-1));

        TracedOverlay overlay = new TracedOverlay(this);
        root.addView(overlay, new FrameLayout.LayoutParams(-1,-1));

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(18,16,18,16);
        panel.setBackgroundColor(Color.argb(225,20,25,30));
        panel.addView(label("2 Lane Roundabout",20,true));
        panel.addView(label("User-traced simulation",12,false));
        panel.addView(space(12));

        RadioGroup exits = new RadioGroup(this);
        RadioButton e1 = radio("1st Exit (Left)");
        RadioButton e2 = radio("2nd Exit (Straight Ahead)");
        exits.addView(e1); exits.addView(e2);
        e1.setChecked(true);
        panel.addView(exits);

        panel.addView(space(12));
        Switch showRoute = toggle("Show Route", true);
        panel.addView(showRoute);

        Button start = new Button(this);
        start.setText("START");
        panel.addView(start,new LinearLayout.LayoutParams(-1,56));
        Button reset = new Button(this);
        reset.setText("RESET");
        panel.addView(reset,new LinearLayout.LayoutParams(-1,48));

        FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(dp(275),-2);
        pp.gravity = Gravity.TOP|Gravity.LEFT;
        pp.setMargins(dp(10),dp(10),0,0);
        root.addView(panel,pp);

        exits.setOnCheckedChangeListener((g,id)->{
            overlay.exit = (id==e2.getId()) ? 2 : 1;
            overlay.reset();
        });
        showRoute.setOnCheckedChangeListener((v,c)->{overlay.showRoute=c;overlay.invalidate();});
        start.setOnClickListener(v->overlay.start());
        reset.setOnClickListener(v->overlay.reset());

        setContentView(root);
    }

    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private View space(int h){View v=new View(this);v.setLayoutParams(new LinearLayout.LayoutParams(1,dp(h)));return v;}
    private TextView label(String s,int size,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(Color.WHITE);if(bold)v.setTypeface(null,Typeface.BOLD);return v;}
    private RadioButton radio(String s){RadioButton r=new RadioButton(this);r.setText(s);r.setTextColor(Color.WHITE);r.setId(View.generateViewId());return r;}
    private Switch toggle(String s,boolean checked){Switch sw=new Switch(this);sw.setText(s);sw.setTextColor(Color.WHITE);sw.setChecked(checked);return sw;}

    static final class TracedOverlay extends View {
        private final Paint routePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint carPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint glassPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final PathMeasure pm = new PathMeasure();
        private final float[] pos = new float[2];
        private final float[] tan = new float[2];
        int exit = 1;
        boolean showRoute = true;
        float progress = 0f;
        ValueAnimator animator;

        TracedOverlay(Context c){
            super(c);
            routePaint.setColor(Color.rgb(25,220,95));
            routePaint.setStyle(Paint.Style.STROKE);
            routePaint.setStrokeCap(Paint.Cap.ROUND);
            routePaint.setStrokeJoin(Paint.Join.ROUND);
            shadowPaint.setColor(Color.argb(110,0,0,0));
            shadowPaint.setStyle(Paint.Style.STROKE);
            shadowPaint.setStrokeCap(Paint.Cap.ROUND);
            carPaint.setColor(Color.rgb(40,92,155));
            glassPaint.setColor(Color.rgb(170,215,235));
            indicatorPaint.setColor(Color.rgb(255,178,40));
        }

        void reset(){if(animator!=null)animator.cancel();progress=0f;invalidate();}
        void start(){
            if(!UserTracedRouteLibrary.hasUserTrace(exit)) return;
            if(animator!=null)animator.cancel();
            animator=ValueAnimator.ofFloat(0f,1f);
            animator.setDuration(exit==1?5200:7000);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(a->{progress=(float)a.getAnimatedValue();invalidate();});
            animator.start();
        }

        private void buildPath(){
            float[][] p = UserTracedRouteLibrary.pointsForExit(exit);
            path.reset();
            if(p.length<2) return;
            float w=getWidth(), h=getHeight();
            path.moveTo(p[0][0]*w,p[0][1]*h);
            // Quadratic midpoint smoothing preserves the user's route while removing finger jitter.
            for(int i=1;i<p.length-1;i++){
                float x=p[i][0]*w, y=p[i][1]*h;
                float mx=(p[i][0]+p[i+1][0])*.5f*w;
                float my=(p[i][1]+p[i+1][1])*.5f*h;
                path.quadTo(x,y,mx,my);
            }
            path.lineTo(p[p.length-1][0]*w,p[p.length-1][1]*h);
        }

        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            if(getWidth()==0||getHeight()==0)return;
            buildPath();
            float s=Math.min(getWidth(),getHeight());
            if(showRoute){
                shadowPaint.setStrokeWidth(s*.018f);
                routePaint.setStrokeWidth(s*.009f);
                c.drawPath(path,shadowPaint);
                c.drawPath(path,routePaint);
            }
            drawCar(c,s);
        }

        private void drawCar(Canvas c,float s){
            pm.setPath(path,false);
            if(pm.getLength()<=0)return;
            pm.getPosTan(pm.getLength()*Math.max(0f,Math.min(1f,progress)),pos,tan);
            float angle=(float)Math.toDegrees(Math.atan2(tan[1],tan[0]))+90f;
            c.save();
            c.translate(pos[0],pos[1]);
            c.rotate(angle);
            float cw=s*.032f, ch=cw*1.75f;
            c.drawRoundRect(new RectF(-cw/2,-ch/2,cw/2,ch/2),cw*.2f,cw*.2f,carPaint);
            c.drawRoundRect(new RectF(-cw*.33f,-ch*.18f,cw*.33f,ch*.10f),cw*.08f,cw*.08f,glassPaint);
            boolean leftSignal = exit==1 || (exit==2 && progress>0.72f);
            if(leftSignal && ((int)(progress*50))%2==0) c.drawCircle(-cw*.42f,-ch*.34f,cw*.10f,indicatorPaint);
            c.restore();
        }
    }
}
