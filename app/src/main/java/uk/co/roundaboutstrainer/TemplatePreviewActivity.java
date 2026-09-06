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
        root.setBackgroundColor(Color.rgb(20,24,28));

        FrameLayout scene = new FrameLayout(this);
        ImageView background = new ImageView(this);
        background.setImageResource(R.drawable.roundabout_template);
        background.setScaleType(ImageView.ScaleType.CENTER_CROP);
        scene.addView(background,new FrameLayout.LayoutParams(-1,-1));

        RouteOverlay overlay = new RouteOverlay(this);
        scene.addView(overlay,new FrameLayout.LayoutParams(-1,-1));
        root.addView(scene,new LinearLayout.LayoutParams(0,-1,4));

        ScrollView scroll = new ScrollView(this);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(24,20,24,24);
        panel.setBackgroundColor(Color.rgb(27,32,37));
        scroll.addView(panel);
        root.addView(scroll,new LinearLayout.LayoutParams(0,-1,2));

        panel.addView(text("UK ROUNDABOUTS TRAINER",24,true));
        panel.addView(text("v0.8.1 • simple realistic lane trainer",13,false));
        panel.addView(space(16));

        panel.addView(text("Roundabout type",13,true));
        Spinner type=spinner(new String[]{"2 Lane Roundabout","Spiral Roundabout"});
        panel.addView(type);

        panel.addView(space(10));
        panel.addView(text("Select exit",13,true));
        Spinner exit=spinner(new String[]{"1st Exit • Left","2nd Exit • Straight Ahead","3rd Exit • Right","4th Exit • U-turn"});
        exit.setSelection(1);
        panel.addView(exit);

        panel.addView(space(10));
        Switch route=new Switch(this); route.setText("Show Route"); route.setTextColor(Color.WHITE); route.setChecked(true); panel.addView(route);
        Switch guide=new Switch(this); guide.setText("Show Lane Guide"); guide.setTextColor(Color.WHITE); guide.setChecked(true); panel.addView(guide);
        Switch markings=new Switch(this); markings.setText("Show Controlled Markings"); markings.setTextColor(Color.WHITE); markings.setChecked(true); panel.addView(markings);

        panel.addView(space(14));
        Button start=new Button(this); start.setText("▶ START"); start.setTextSize(18); panel.addView(start,new LinearLayout.LayoutParams(-1,62));
        Button reset=new Button(this); reset.setText("RESET"); panel.addView(reset,new LinearLayout.LayoutParams(-1,50));

        panel.addView(space(14));
        TextView info=text("2 Lane • 2nd Exit",15,true); panel.addView(info);
        TextView laneInfo=text("Recommended approach: LEFT LANE",13,false); laneInfo.setPadding(0,8,0,0); panel.addView(laneInfo);
        TextView note=text("Road lines, arrows and route are app-controlled overlays. The photo is only the visual base.",11,false); note.setPadding(0,12,0,0); panel.addView(note);

        Runnable sync=()->{
            overlay.spiral=type.getSelectedItemPosition()==1;
            overlay.exit=exit.getSelectedItemPosition()+1;
            overlay.showRoute=route.isChecked();
            overlay.showGuide=guide.isChecked();
            overlay.showMarkings=markings.isChecked();
            overlay.reset();
            info.setText((overlay.spiral?"Spiral":"2 Lane")+" • "+exit.getSelectedItem().toString());
            laneInfo.setText("Recommended approach: "+overlay.laneText());
        };

        type.setOnItemSelectedListener(listener(sync));
        exit.setOnItemSelectedListener(listener(sync));
        route.setOnCheckedChangeListener((b,c)->sync.run());
        guide.setOnCheckedChangeListener((b,c)->sync.run());
        markings.setOnCheckedChangeListener((b,c)->sync.run());
        start.setOnClickListener(v->{sync.run();overlay.start();});
        reset.setOnClickListener(v->overlay.reset());
        setContentView(root);
    }

    private AdapterView.OnItemSelectedListener listener(Runnable r){return new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?>p,View v,int pos,long id){r.run();}public void onNothingSelected(AdapterView<?>p){}};}
    private Spinner spinner(String[] values){Spinner s=new Spinner(this);s.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,values));return s;}
    private TextView text(String value,int size,boolean bold){TextView v=new TextView(this);v.setText(value);v.setTextSize(size);v.setTextColor(Color.WHITE);if(bold)v.setTypeface(null,Typeface.BOLD);return v;}
    private View space(int h){View v=new View(this);v.setLayoutParams(new LinearLayout.LayoutParams(1,h));return v;}

    static class RouteOverlay extends View {
        final Paint route=new Paint(Paint.ANTI_ALIAS_FLAG),shadow=new Paint(Paint.ANTI_ALIAS_FLAG),guide=new Paint(Paint.ANTI_ALIAS_FLAG),white=new Paint(Paint.ANTI_ALIAS_FLAG),car=new Paint(Paint.ANTI_ALIAS_FLAG),glass=new Paint(Paint.ANTI_ALIAS_FLAG),amber=new Paint(Paint.ANTI_ALIAS_FLAG);
        final Path path=new Path(),guidePath=new Path();
        final PathMeasure measure=new PathMeasure();
        final float[] pos=new float[2],tan=new float[2];
        boolean spiral=false,showRoute=true,showGuide=true,showMarkings=true;
        int exit=2;
        float progress=0f;
        ValueAnimator animator;

        RouteOverlay(Context c){
            super(c);setBackgroundColor(Color.TRANSPARENT);
            route.setColor(Color.rgb(29,210,85));route.setStyle(Paint.Style.STROKE);route.setStrokeCap(Paint.Cap.ROUND);route.setStrokeJoin(Paint.Join.ROUND);
            shadow.setColor(Color.argb(120,0,0,0));shadow.setStyle(Paint.Style.STROKE);shadow.setStrokeCap(Paint.Cap.ROUND);
            guide.setColor(Color.argb(220,45,145,255));guide.setStyle(Paint.Style.STROKE);guide.setStrokeCap(Paint.Cap.ROUND);
            white.setColor(Color.argb(235,255,255,255));white.setStyle(Paint.Style.STROKE);white.setStrokeCap(Paint.Cap.SQUARE);
            car.setColor(Color.rgb(42,105,205));glass.setColor(Color.rgb(185,225,245));amber.setColor(Color.rgb(255,177,40));
        }

        String laneText(){
            if(spiral){if(exit==1)return "LEFT LANE";if(exit==2)return "LEFT LANE / follow markings";return "RIGHT LANE / follow spiral markings";}
            return exit<=2?"LEFT LANE":"RIGHT LANE";
        }

        void reset(){if(animator!=null)animator.cancel();progress=0f;invalidate();}
        void start(){if(animator!=null)animator.cancel();animator=ValueAnimator.ofFloat(0f,1f);animator.setDuration(6200);animator.setInterpolator(new LinearInterpolator());animator.addUpdateListener(a->{progress=(float)a.getAnimatedValue();invalidate();});animator.start();}

        float x(float l,float s,float n){return l+s*n;} float y(float t,float s,float n){return t+s*n;}

        void buildTwoLane(float l,float t,float s){
            path.reset();
            float startX=(exit<=2)?.445f:.555f;
            path.moveTo(x(l,s,startX),y(t,s,.99f));
            if(exit==1){
                path.cubicTo(x(l,s,.445f),y(t,s,.82f),x(l,s,.42f),y(t,s,.70f),x(l,s,.37f),y(t,s,.64f));
                path.cubicTo(x(l,s,.31f),y(t,s,.58f),x(l,s,.18f),y(t,s,.56f),x(l,s,.01f),y(t,s,.56f));
            } else if(exit==2){
                path.cubicTo(x(l,s,.445f),y(t,s,.82f),x(l,s,.42f),y(t,s,.70f),x(l,s,.36f),y(t,s,.64f));
                path.cubicTo(x(l,s,.29f),y(t,s,.57f),x(l,s,.29f),y(t,s,.44f),x(l,s,.34f),y(t,s,.36f));
                path.cubicTo(x(l,s,.39f),y(t,s,.29f),x(l,s,.445f),y(t,s,.21f),x(l,s,.445f),y(t,s,.01f));
            } else if(exit==3){
                path.cubicTo(x(l,s,.555f),y(t,s,.82f),x(l,s,.54f),y(t,s,.70f),x(l,s,.47f),y(t,s,.64f));
                path.cubicTo(x(l,s,.39f),y(t,s,.58f),x(l,s,.34f),y(t,s,.49f),x(l,s,.36f),y(t,s,.41f));
                path.cubicTo(x(l,s,.39f),y(t,s,.31f),x(l,s,.57f),y(t,s,.30f),x(l,s,.66f),y(t,s,.39f));
                path.cubicTo(x(l,s,.70f),y(t,s,.44f),x(l,s,.73f),y(t,s,.53f),x(l,s,.99f),y(t,s,.55f));
            } else {
                path.cubicTo(x(l,s,.555f),y(t,s,.82f),x(l,s,.54f),y(t,s,.70f),x(l,s,.47f),y(t,s,.64f));
                path.cubicTo(x(l,s,.34f),y(t,s,.55f),x(l,s,.33f),y(t,s,.35f),x(l,s,.45f),y(t,s,.30f));
                path.cubicTo(x(l,s,.58f),y(t,s,.24f),x(l,s,.72f),y(t,s,.34f),x(l,s,.70f),y(t,s,.49f));
                path.cubicTo(x(l,s,.68f),y(t,s,.62f),x(l,s,.56f),y(t,s,.66f),x(l,s,.555f),y(t,s,.99f));
            }
        }

        void buildSpiral(float l,float t,float s){
            path.reset();
            float startX=exit<=2?.445f:.555f;
            path.moveTo(x(l,s,startX),y(t,s,.99f));
            if(exit==1){buildTwoLane(l,t,s);return;}
            if(exit==2){
                path.cubicTo(x(l,s,.445f),y(t,s,.82f),x(l,s,.43f),y(t,s,.70f),x(l,s,.37f),y(t,s,.64f));
                path.cubicTo(x(l,s,.31f),y(t,s,.58f),x(l,s,.31f),y(t,s,.47f),x(l,s,.37f),y(t,s,.39f));
                path.cubicTo(x(l,s,.42f),y(t,s,.33f),x(l,s,.46f),y(t,s,.22f),x(l,s,.46f),y(t,s,.01f));
            } else if(exit==3){
                path.cubicTo(x(l,s,.555f),y(t,s,.82f),x(l,s,.55f),y(t,s,.70f),x(l,s,.49f),y(t,s,.64f));
                path.cubicTo(x(l,s,.40f),y(t,s,.57f),x(l,s,.37f),y(t,s,.48f),x(l,s,.40f),y(t,s,.40f));
                path.cubicTo(x(l,s,.44f),y(t,s,.33f),x(l,s,.57f),y(t,s,.33f),x(l,s,.64f),y(t,s,.40f));
                path.cubicTo(x(l,s,.68f),y(t,s,.45f),x(l,s,.69f),y(t,s,.50f),x(l,s,.99f),y(t,s,.55f));
            } else {
                path.cubicTo(x(l,s,.555f),y(t,s,.82f),x(l,s,.55f),y(t,s,.70f),x(l,s,.49f),y(t,s,.64f));
                path.cubicTo(x(l,s,.40f),y(t,s,.57f),x(l,s,.37f),y(t,s,.47f),x(l,s,.40f),y(t,s,.39f));
                path.cubicTo(x(l,s,.45f),y(t,s,.30f),x(l,s,.61f),y(t,s,.31f),x(l,s,.67f),y(t,s,.40f));
                path.cubicTo(x(l,s,.74f),y(t,s,.51f),x(l,s,.66f),y(t,s,.62f),x(l,s,.57f),y(t,s,.64f));
                path.cubicTo(x(l,s,.51f),y(t,s,.68f),x(l,s,.54f),y(t,s,.82f),x(l,s,.555f),y(t,s,.99f));
            }
        }

        void drawControlledMarkings(Canvas c,float l,float t,float s){
            white.setStrokeWidth(s*.006f);
            white.setPathEffect(new DashPathEffect(new float[]{s*.025f,s*.022f},0));
            RectF outer=new RectF(x(l,s,.285f),y(t,s,.285f),x(l,s,.715f),y(t,s,.715f));
            RectF inner=new RectF(x(l,s,.355f),y(t,s,.355f),x(l,s,.645f),y(t,s,.645f));
            c.drawArc(outer,0,360,false,white);c.drawArc(inner,0,360,false,white);
            white.setPathEffect(null);
            c.drawLine(x(l,s,.50f),y(t,s,.72f),x(l,s,.50f),y(t,s,.99f),white);
            for(int i=0;i<10;i++){
                float a=i/10f;
                c.drawLine(x(l,s,.385f+a*.23f),y(t,s,.685f),x(l,s,.397f+a*.205f),y(t,s,.674f),white);
            }
            if(spiral){
                white.setPathEffect(new DashPathEffect(new float[]{s*.018f,s*.017f},0));
                Path sp=new Path();sp.moveTo(x(l,s,.55f),y(t,s,.67f));sp.cubicTo(x(l,s,.48f),y(t,s,.61f),x(l,s,.43f),y(t,s,.52f),x(l,s,.44f),y(t,s,.43f));sp.cubicTo(x(l,s,.45f),y(t,s,.36f),x(l,s,.52f),y(t,s,.33f),x(l,s,.60f),y(t,s,.35f));c.drawPath(sp,white);white.setPathEffect(null);
            }
        }

        void drawArrow(Canvas c,float cx,float cy,float s,boolean right){
            Paint p=white;p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(s*.008f);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(cx,cy+s*.035f,cx,cy-s*.025f,p);Path a=new Path();a.moveTo(cx,cy-s*.025f);a.lineTo(cx-s*.018f,cy-s*.005f);a.moveTo(cx,cy-s*.025f);a.lineTo(cx+s*.018f,cy-s*.005f);c.drawPath(a,p);if(right){c.drawLine(cx,cy+s*.005f,cx+s*.026f,cy-s*.010f,p);}p.setStyle(Paint.Style.STROKE);
        }

        void drawCar(Canvas c,float s){
            measure.setPath(path,false);float d=measure.getLength()*Math.max(0f,Math.min(1f,progress));measure.getPosTan(d,pos,tan);float angle=(float)Math.toDegrees(Math.atan2(tan[1],tan[0]))+90f;c.save();c.translate(pos[0],pos[1]);c.rotate(angle);float w=s*.032f,h=w*1.75f;c.drawRoundRect(new RectF(-w/2,-h/2,w/2,h/2),w*.22f,w*.22f,car);c.drawRoundRect(new RectF(-w*.34f,-h*.18f,w*.34f,h*.10f),w*.08f,w*.08f,glass);boolean blink=((int)(progress*40))%2==0;if(blink&&(progress>.78f||exit==1)){c.drawCircle(-w*.43f,-h*.34f,w*.10f,amber);}c.restore();
        }

        @Override protected void onDraw(Canvas c){
            super.onDraw(c);float w=getWidth(),h=getHeight();if(w<1||h<1)return;float s=Math.min(w,h),l=(w-s)/2f,t=(h-s)/2f;
            if(spiral)buildSpiral(l,t,s);else buildTwoLane(l,t,s);
            if(showMarkings)drawControlledMarkings(c,l,t,s);
            if(showGuide){guide.setStrokeWidth(s*.018f);guidePath.reset();float gx=exit<=2?.445f:.555f;guidePath.moveTo(x(l,s,gx),y(t,s,.99f));guidePath.lineTo(x(l,s,gx),y(t,s,.73f));c.drawPath(guidePath,guide);drawArrow(c,x(l,s,gx),y(t,s,.84f),s,exit>=3);}
            if(showRoute){shadow.setStrokeWidth(s*.026f);route.setStrokeWidth(s*.015f);c.drawPath(path,shadow);c.drawPath(path,route);}
            drawCar(c,s);
        }
    }
}
