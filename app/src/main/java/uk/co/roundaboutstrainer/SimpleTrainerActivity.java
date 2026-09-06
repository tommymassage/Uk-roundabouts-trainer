package uk.co.roundaboutstrainer;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.view.animation.LinearInterpolator;
import android.widget.*;

public class SimpleTrainerActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setBackgroundColor(Color.rgb(18,22,26));

        FrameLayout scene = new FrameLayout(this);
        ImageView bg = new ImageView(this);
        bg.setImageResource(R.drawable.roundabout_template);
        bg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        scene.addView(bg, new FrameLayout.LayoutParams(-1,-1));

        RoadOverlay overlay = new RoadOverlay(this);
        scene.addView(overlay, new FrameLayout.LayoutParams(-1,-1));
        root.addView(scene, new LinearLayout.LayoutParams(0,-1,4));

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(24,20,24,20);
        panel.setBackgroundColor(Color.rgb(26,31,36));
        root.addView(panel, new LinearLayout.LayoutParams(0,-1,2));

        panel.addView(label("UK ROUNDABOUTS TRAINER",24,true));
        panel.addView(label("v0.8.3 • refined UK approach geometry",13,false));
        panel.addView(gap(14));

        panel.addView(label("Roundabout type",13,true));
        Spinner type = spinner(new String[]{"2 Lane Roundabout","Spiral Roundabout"});
        panel.addView(type);

        panel.addView(gap(10));
        panel.addView(label("Select exit",13,true));
        Spinner exit = spinner(new String[]{"1st Exit • Left","2nd Exit • Straight Ahead","3rd Exit • Right","4th Exit • U-turn"});
        exit.setSelection(1);
        panel.addView(exit);

        panel.addView(gap(10));
        Switch showRoute = toggle("Show Route",true); panel.addView(showRoute);
        Switch showGuide = toggle("Show Lane Guide",true); panel.addView(showGuide);
        Switch showMarkings = toggle("Show UK Markings",true); panel.addView(showMarkings);

        panel.addView(gap(14));
        Button start = new Button(this); start.setText("▶ START"); start.setTextSize(18); panel.addView(start,new LinearLayout.LayoutParams(-1,62));
        Button reset = new Button(this); reset.setText("RESET"); panel.addView(reset,new LinearLayout.LayoutParams(-1,48));

        panel.addView(gap(12));
        TextView info = label("2 Lane • 2nd Exit",15,true); panel.addView(info);
        TextView lane = label("Recommended approach: LEFT LANE",13,false); panel.addView(lane);
        TextView note = label("Give-way markings, splitter island, approach divider and lane arrows are app-controlled overlays.",11,false); note.setPadding(0,10,0,0); panel.addView(note);

        Runnable sync = () -> {
            overlay.spiral = type.getSelectedItemPosition()==1;
            overlay.exit = exit.getSelectedItemPosition()+1;
            overlay.showRoute = showRoute.isChecked();
            overlay.showGuide = showGuide.isChecked();
            overlay.showMarkings = showMarkings.isChecked();
            overlay.reset();
            info.setText((overlay.spiral?"Spiral":"2 Lane")+" • "+exit.getSelectedItem());
            lane.setText("Recommended approach: "+overlay.laneText());
        };

        type.setOnItemSelectedListener(listener(sync));
        exit.setOnItemSelectedListener(listener(sync));
        showRoute.setOnCheckedChangeListener((v,c)->sync.run());
        showGuide.setOnCheckedChangeListener((v,c)->sync.run());
        showMarkings.setOnCheckedChangeListener((v,c)->sync.run());
        start.setOnClickListener(v->{sync.run();overlay.start();});
        reset.setOnClickListener(v->overlay.reset());

        setContentView(root);
    }

    private TextView label(String s,int size,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(Color.WHITE);if(bold)v.setTypeface(null,Typeface.BOLD);return v;}
    private View gap(int h){View v=new View(this);v.setLayoutParams(new LinearLayout.LayoutParams(1,h));return v;}
    private Spinner spinner(String[] v){Spinner s=new Spinner(this);s.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,v));return s;}
    private Switch toggle(String text,boolean checked){Switch s=new Switch(this);s.setText(text);s.setTextColor(Color.WHITE);s.setChecked(checked);return s;}
    private AdapterView.OnItemSelectedListener listener(Runnable r){return new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?>p,View v,int i,long id){r.run();}public void onNothingSelected(AdapterView<?>p){}};}

    static class RoadOverlay extends View {
        final Paint white=new Paint(Paint.ANTI_ALIAS_FLAG), route=new Paint(Paint.ANTI_ALIAS_FLAG), guide=new Paint(Paint.ANTI_ALIAS_FLAG), shadow=new Paint(Paint.ANTI_ALIAS_FLAG), car=new Paint(Paint.ANTI_ALIAS_FLAG), glass=new Paint(Paint.ANTI_ALIAS_FLAG), amber=new Paint(Paint.ANTI_ALIAS_FLAG), islandFill=new Paint(Paint.ANTI_ALIAS_FLAG), kerb=new Paint(Paint.ANTI_ALIAS_FLAG), bollard=new Paint(Paint.ANTI_ALIAS_FLAG), blue=new Paint(Paint.ANTI_ALIAS_FLAG);
        final Path routePath=new Path();
        final PathMeasure measure=new PathMeasure();
        final float[] pos=new float[2], tan=new float[2];
        boolean spiral=false,showRoute=true,showGuide=true,showMarkings=true;
        int exit=2;
        float progress=0f;
        ValueAnimator animator;

        RoadOverlay(Context c){
            super(c);setBackgroundColor(Color.TRANSPARENT);
            white.setColor(Color.argb(245,255,255,255));white.setStyle(Paint.Style.STROKE);white.setStrokeCap(Paint.Cap.BUTT);white.setStrokeJoin(Paint.Join.ROUND);
            route.setColor(Color.rgb(35,220,90));route.setStyle(Paint.Style.STROKE);route.setStrokeCap(Paint.Cap.ROUND);route.setStrokeJoin(Paint.Join.ROUND);
            guide.setColor(Color.argb(220,60,160,255));guide.setStyle(Paint.Style.STROKE);guide.setStrokeCap(Paint.Cap.ROUND);
            shadow.setColor(Color.argb(100,0,0,0));shadow.setStyle(Paint.Style.STROKE);shadow.setStrokeCap(Paint.Cap.ROUND);
            car.setColor(Color.rgb(42,105,205));glass.setColor(Color.rgb(185,225,245));amber.setColor(Color.rgb(255,177,40));
            islandFill.setColor(Color.rgb(190,185,170));islandFill.setStyle(Paint.Style.FILL);
            kerb.setColor(Color.rgb(235,235,230));kerb.setStyle(Paint.Style.STROKE);kerb.setStrokeJoin(Paint.Join.ROUND);
            bollard.setColor(Color.rgb(245,220,40));bollard.setStyle(Paint.Style.FILL);
            blue.setColor(Color.rgb(35,95,180));blue.setStyle(Paint.Style.FILL);
        }

        String laneText(){if(spiral){if(exit==1)return "LEFT LANE";if(exit==2)return "LEFT LANE / follow road markings";return "RIGHT LANE / follow spiral markings";}return exit<=2?"LEFT LANE":"RIGHT LANE";}
        void reset(){if(animator!=null)animator.cancel();progress=0f;invalidate();}
        void start(){if(animator!=null)animator.cancel();animator=ValueAnimator.ofFloat(0f,1f);animator.setDuration(6200);animator.setInterpolator(new LinearInterpolator());animator.addUpdateListener(a->{progress=(float)a.getAnimatedValue();invalidate();});animator.start();}
        float X(float l,float s,float n){return l+s*n;} float Y(float t,float s,float n){return t+s*n;}

        void buildRoute(float l,float t,float s){
            routePath.reset();
            float sx=exit<=2?.445f:.555f;
            routePath.moveTo(X(l,s,sx),Y(t,s,.995f));
            if(exit==1){
                routePath.cubicTo(X(l,s,.445f),Y(t,s,.84f),X(l,s,.43f),Y(t,s,.72f),X(l,s,.39f),Y(t,s,.665f));
                routePath.cubicTo(X(l,s,.34f),Y(t,s,.60f),X(l,s,.20f),Y(t,s,.565f),X(l,s,.01f),Y(t,s,.565f));
            } else if(exit==2){
                routePath.cubicTo(X(l,s,.445f),Y(t,s,.84f),X(l,s,.43f),Y(t,s,.72f),X(l,s,.39f),Y(t,s,.66f));
                routePath.cubicTo(X(l,s,.31f),Y(t,s,.59f),X(l,s,.30f),Y(t,s,.46f),X(l,s,.35f),Y(t,s,.38f));
                routePath.cubicTo(X(l,s,.40f),Y(t,s,.30f),X(l,s,.445f),Y(t,s,.22f),X(l,s,.445f),Y(t,s,.01f));
            } else if(exit==3){
                routePath.cubicTo(X(l,s,.555f),Y(t,s,.84f),X(l,s,.545f),Y(t,s,.72f),X(l,s,.50f),Y(t,s,.66f));
                routePath.cubicTo(X(l,s,.42f),Y(t,s,.58f),X(l,s,.37f),Y(t,s,.48f),X(l,s,.39f),Y(t,s,.40f));
                routePath.cubicTo(X(l,s,.43f),Y(t,s,.31f),X(l,s,.58f),Y(t,s,.31f),X(l,s,.66f),Y(t,s,.39f));
                routePath.cubicTo(X(l,s,.71f),Y(t,s,.45f),X(l,s,.73f),Y(t,s,.53f),X(l,s,.99f),Y(t,s,.55f));
            } else {
                routePath.cubicTo(X(l,s,.555f),Y(t,s,.84f),X(l,s,.545f),Y(t,s,.72f),X(l,s,.50f),Y(t,s,.66f));
                routePath.cubicTo(X(l,s,.40f),Y(t,s,.58f),X(l,s,.36f),Y(t,s,.44f),X(l,s,.41f),Y(t,s,.35f));
                routePath.cubicTo(X(l,s,.47f),Y(t,s,.25f),X(l,s,.63f),Y(t,s,.28f),X(l,s,.68f),Y(t,s,.39f));
                routePath.cubicTo(X(l,s,.75f),Y(t,s,.52f),X(l,s,.65f),Y(t,s,.63f),X(l,s,.57f),Y(t,s,.66f));
                routePath.cubicTo(X(l,s,.52f),Y(t,s,.70f),X(l,s,.54f),Y(t,s,.84f),X(l,s,.555f),Y(t,s,.995f));
            }
        }

        void drawSplitterIsland(Canvas c,float l,float t,float s){
            Path island=new Path();
            island.moveTo(X(l,s,.485f),Y(t,s,.995f));
            island.lineTo(X(l,s,.515f),Y(t,s,.995f));
            island.cubicTo(X(l,s,.520f),Y(t,s,.90f),X(l,s,.518f),Y(t,s,.81f),X(l,s,.508f),Y(t,s,.735f));
            island.quadTo(X(l,s,.500f),Y(t,s,.705f),X(l,s,.492f),Y(t,s,.735f));
            island.cubicTo(X(l,s,.482f),Y(t,s,.81f),X(l,s,.480f),Y(t,s,.90f),X(l,s,.485f),Y(t,s,.995f));
            c.drawPath(island,islandFill);
            kerb.setStrokeWidth(s*.007f);c.drawPath(island,kerb);
            float bx=X(l,s,.500f), by=Y(t,s,.755f);float bw=s*.018f,bh=s*.038f;
            c.drawRoundRect(new RectF(bx-bw/2,by-bh/2,bx+bw/2,by+bh/2),bw*.25f,bw*.25f,bollard);
            c.drawCircle(bx,by-bh*.12f,bw*.32f,blue);
        }

        void drawMarkings(Canvas c,float l,float t,float s){
            white.setStrokeWidth(s*.0055f);
            white.setPathEffect(new DashPathEffect(new float[]{s*.026f,s*.020f},0));
            c.drawArc(new RectF(X(l,s,.285f),Y(t,s,.285f),X(l,s,.715f),Y(t,s,.715f)),0,360,false,white);
            c.drawArc(new RectF(X(l,s,.355f),Y(t,s,.355f),X(l,s,.645f),Y(t,s,.645f)),0,360,false,white);
            white.setPathEffect(new DashPathEffect(new float[]{s*.030f,s*.020f},0));
            c.drawLine(X(l,s,.50f),Y(t,s,.735f),X(l,s,.50f),Y(t,s,.995f),white);

            white.setPathEffect(null);white.setStrokeWidth(s*.006f);
            for(int row=0;row<2;row++){
                float yy=.676f+row*.013f;
                for(int i=0;i<8;i++){
                    float x1=.392f+i*.028f; float x2=x1+.018f;
                    c.drawLine(X(l,s,x1),Y(t,s,yy),X(l,s,x2),Y(t,s,yy),white);
                }
            }

            white.setStrokeWidth(s*.0045f);white.setPathEffect(new DashPathEffect(new float[]{s*.018f,s*.017f},0));
            Path left=new Path();left.moveTo(X(l,s,.445f),Y(t,s,.73f));left.cubicTo(X(l,s,.44f),Y(t,s,.70f),X(l,s,.42f),Y(t,s,.675f),X(l,s,.39f),Y(t,s,.655f));c.drawPath(left,white);
            Path right=new Path();right.moveTo(X(l,s,.555f),Y(t,s,.73f));right.cubicTo(X(l,s,.55f),Y(t,s,.70f),X(l,s,.53f),Y(t,s,.675f),X(l,s,.50f),Y(t,s,.655f));c.drawPath(right,white);

            if(spiral){
                Path sp=new Path();sp.moveTo(X(l,s,.55f),Y(t,s,.66f));sp.cubicTo(X(l,s,.50f),Y(t,s,.61f),X(l,s,.46f),Y(t,s,.54f),X(l,s,.45f),Y(t,s,.46f));sp.cubicTo(X(l,s,.45f),Y(t,s,.38f),X(l,s,.52f),Y(t,s,.34f),X(l,s,.61f),Y(t,s,.36f));c.drawPath(sp,white);
            }
            white.setPathEffect(null);
        }

        void drawStraightArrow(Canvas c,float cx,float cy,float s){
            white.setStrokeWidth(s*.0075f);white.setStyle(Paint.Style.STROKE);white.setStrokeCap(Paint.Cap.ROUND);
            c.drawLine(cx,cy+s*.035f,cx,cy-s*.025f,white);
            Path a=new Path();a.moveTo(cx,cy-s*.025f);a.lineTo(cx-s*.017f,cy-s*.006f);a.moveTo(cx,cy-s*.025f);a.lineTo(cx+s*.017f,cy-s*.006f);c.drawPath(a,white);
        }

        void drawRightArrow(Canvas c,float cx,float cy,float s){
            white.setStrokeWidth(s*.0075f);white.setStyle(Paint.Style.STROKE);white.setStrokeCap(Paint.Cap.ROUND);
            c.drawLine(cx,cy+s*.035f,cx,cy-s*.005f,white);
            c.drawLine(cx,cy-s*.005f,cx+s*.026f,cy-s*.020f,white);
            Path a=new Path();a.moveTo(cx+s*.026f,cy-s*.020f);a.lineTo(cx+s*.009f,cy-s*.022f);a.moveTo(cx+s*.026f,cy-s*.020f);a.lineTo(cx+s*.019f,cy-s*.004f);c.drawPath(a,white);
        }

        void drawLeftArrow(Canvas c,float cx,float cy,float s){
            white.setStrokeWidth(s*.0075f);white.setStyle(Paint.Style.STROKE);white.setStrokeCap(Paint.Cap.ROUND);
            c.drawLine(cx,cy+s*.035f,cx,cy-s*.005f,white);
            c.drawLine(cx,cy-s*.005f,cx-s*.026f,cy-s*.020f,white);
            Path a=new Path();a.moveTo(cx-s*.026f,cy-s*.020f);a.lineTo(cx-s*.009f,cy-s*.022f);a.moveTo(cx-s*.026f,cy-s*.020f);a.lineTo(cx-s*.019f,cy-s*.004f);c.drawPath(a,white);
        }

        void drawCar(Canvas c,float s){
            measure.setPath(routePath,false);float d=measure.getLength()*Math.max(0f,Math.min(1f,progress));measure.getPosTan(d,pos,tan);
            float angle=(float)Math.toDegrees(Math.atan2(tan[1],tan[0]))+90f;c.save();c.translate(pos[0],pos[1]);c.rotate(angle);
            float w=s*.032f,h=w*1.75f;c.drawRoundRect(new RectF(-w/2,-h/2,w/2,h/2),w*.22f,w*.22f,car);c.drawRoundRect(new RectF(-w*.34f,-h*.18f,w*.34f,h*.10f),w*.08f,w*.08f,glass);
            if(((int)(progress*40))%2==0 && (exit==1 || progress>.78f))c.drawCircle(-w*.43f,-h*.34f,w*.10f,amber);
            c.restore();
        }

        @Override protected void onDraw(Canvas c){
            super.onDraw(c);float w=getWidth(),h=getHeight();if(w<1||h<1)return;float s=Math.min(w,h),l=(w-s)/2f,t=(h-s)/2f;
            buildRoute(l,t,s);
            if(showMarkings){drawSplitterIsland(c,l,t,s);drawMarkings(c,l,t,s);}
            if(showGuide){guide.setStrokeWidth(s*.010f);guide.setPathEffect(new DashPathEffect(new float[]{s*.03f,s*.025f},0));c.drawPath(routePath,guide);guide.setPathEffect(null);}
            if(showRoute){shadow.setStrokeWidth(s*.020f);route.setStrokeWidth(s*.012f);c.drawPath(routePath,shadow);c.drawPath(routePath,route);}
            if(showMarkings){
                if(exit==1){drawLeftArrow(c,X(l,s,.445f),Y(t,s,.84f),s);drawStraightArrow(c,X(l,s,.555f),Y(t,s,.84f),s);} 
                else if(exit==2){drawStraightArrow(c,X(l,s,.445f),Y(t,s,.84f),s);drawRightArrow(c,X(l,s,.555f),Y(t,s,.84f),s);} 
                else {drawStraightArrow(c,X(l,s,.445f),Y(t,s,.84f),s);drawRightArrow(c,X(l,s,.555f),Y(t,s,.84f),s);} 
            }
            drawCar(c,s);
        }
    }
}
