package uk.co.roundaboutstrainer;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.view.animation.LinearInterpolator;
import android.widget.*;
import android.content.Context;

public class MainActivity extends Activity {
    private RoundaboutView roadView;
    private TextView[] stepViews=new TextView[6];

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        boolean phone=getResources().getConfiguration().smallestScreenWidthDp<600;
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(phone?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        root.setBackgroundColor(Color.rgb(245,247,244));

        roadView=new RoundaboutView(this);
        root.addView(roadView,phone?new LinearLayout.LayoutParams(-1,0,6):new LinearLayout.LayoutParams(0,-1,3));

        ScrollView scroll=new ScrollView(this);
        LinearLayout panel=new LinearLayout(this); panel.setOrientation(LinearLayout.VERTICAL); panel.setPadding(20,12,18,20);
        scroll.addView(panel);
        root.addView(scroll,phone?new LinearLayout.LayoutParams(-1,0,5):new LinearLayout.LayoutParams(0,-1,2));

        panel.addView(text("UK ROUNDABOUTS TRAINER  v0.6.1",24,true));
        TextView sub=text("Realistic road scene rebuild • UK markings • MSPSL coaching",13,false); sub.setPadding(0,4,0,12); panel.addView(sub);

        panel.addView(text("Approach road",14,true)); Spinner approach=spinner(new String[]{"South","West","North","East"}); panel.addView(approach);
        panel.addView(text("Exit",14,true)); Spinner exit=spinner(new String[]{"1st exit / left","2nd exit / ahead","3rd exit / right"}); panel.addView(exit);
        panel.addView(text("Roundabout type",14,true)); Spinner type=spinner(new String[]{"Standard","Spiral training","Large 3-lane","Mini"}); panel.addView(type);
        panel.addView(text("Traffic",14,true)); Spinner scenario=spinner(new String[]{"Clear roundabout","Vehicle from the right","Busy traffic"}); panel.addView(scenario);
        panel.addView(text("Mode",14,true)); Spinner mode=spinner(new String[]{"Guided demo","Practice","Test"}); panel.addView(mode);

        CheckBox markings=check("Show lane markings and arrows",true); panel.addView(markings);
        CheckBox traffic=check("Show other traffic",true); panel.addView(traffic);
        CheckBox signs=check("Show road signs",true); panel.addView(signs);
        CheckBox route=check("Show training route",true); panel.addView(route);

        Button start=new Button(this); start.setText("▶  START DRIVING DEMO"); start.setTextSize(16); panel.addView(start);

        panel.addView(text("MSPSL Guide",17,true));
        String[] steps={"Mirrors — check all mirrors","Signal — communicate your intention","Position — follow signs and lane markings","Speed — slow down and be ready to give way","Look — assess traffic from the right","Leave — signal left and exit safely"};
        for(int i=0;i<6;i++){ stepViews[i]=text((i+1)+"   "+steps[i],14,false); stepViews[i].setPadding(8,7,8,7); panel.addView(stepViews[i]); }

        TextView info=text("ⓘ  Give way to traffic from the right unless signs, signals or road markings indicate otherwise.",12,true); info.setPadding(10,9,10,9); GradientDrawable ib=new GradientDrawable(); ib.setColor(Color.rgb(224,239,252)); ib.setCornerRadius(10); info.setBackground(ib); panel.addView(info);
        TextView stage=text("Ready • MIRRORS",17,true); stage.setPadding(0,12,0,5); panel.addView(stage);
        ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal); progress.setMax(100); panel.addView(progress,new LinearLayout.LayoutParams(-1,14));
        TextView result=text("Training result: —",14,true); result.setPadding(0,8,0,0); panel.addView(result);

        roadView.listener=(s,pct,step)->{ stage.setText(s); progress.setProgress(pct); highlight(step); if(pct>=100)result.setText("Training result: COMPLETE"); };

        Runnable apply=()->{
            roadView.approach=approach.getSelectedItemPosition(); roadView.exit=exit.getSelectedItemPosition()+1; roadView.type=type.getSelectedItemPosition();
            roadView.scenario=scenario.getSelectedItemPosition(); roadView.mode=mode.getSelectedItemPosition(); roadView.showMarkings=markings.isChecked(); roadView.showTraffic=traffic.isChecked(); roadView.showSigns=signs.isChecked(); roadView.showRoute=route.isChecked()&&roadView.mode<2;
            roadView.reset(); stage.setText("Ready • MIRRORS"); progress.setProgress(0); result.setText("Training result: —"); highlight(0);
        };
        start.setOnClickListener(v->{apply.run();roadView.start();});
        markings.setOnClickListener(v->apply.run()); traffic.setOnClickListener(v->apply.run()); signs.setOnClickListener(v->apply.run()); route.setOnClickListener(v->apply.run());
        approach.setOnItemSelectedListener(sel(apply)); exit.setOnItemSelectedListener(sel(apply)); type.setOnItemSelectedListener(sel(apply)); scenario.setOnItemSelectedListener(sel(apply)); mode.setOnItemSelectedListener(sel(apply));
        highlight(0); setContentView(root);
    }

    private TextView text(String s,int size,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.rgb(31,43,49));if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
    private Spinner spinner(String[] a){Spinner s=new Spinner(this);s.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,a));return s;}
    private CheckBox check(String s,boolean b){CheckBox c=new CheckBox(this);c.setText(s);c.setChecked(b);return c;}
    private AdapterView.OnItemSelectedListener sel(Runnable r){return new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?>p,View v,int x,long id){r.run();}public void onNothingSelected(AdapterView<?>p){}};}
    private void highlight(int active){for(int i=0;i<6;i++){GradientDrawable g=new GradientDrawable();g.setCornerRadius(9);g.setColor(i==active?Color.rgb(219,239,255):Color.TRANSPARENT);stepViews[i].setBackground(g);stepViews[i].setTypeface(null,i==active?Typeface.BOLD:Typeface.NORMAL);}}

    interface StageListener{void stage(String s,int pct,int step);}

    static class RoundaboutView extends View{
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); Paint routeP=new Paint(Paint.ANTI_ALIAS_FLAG); Paint routeGlow=new Paint(Paint.ANTI_ALIAS_FLAG);
        int approach=0,exit=1,type=0,scenario=0,mode=0; boolean showMarkings=true,showTraffic=true,showSigns=true,showRoute=true; float raw=0,carProgress=0; ValueAnimator anim; Path route=new Path(); StageListener listener;
        RoundaboutView(Context c){super(c);routeP.setColor(Color.rgb(44,235,84));routeP.setStyle(Paint.Style.STROKE);routeP.setStrokeWidth(9);routeP.setStrokeCap(Paint.Cap.ROUND);routeGlow.setColor(Color.argb(80,55,255,110));routeGlow.setStyle(Paint.Style.STROKE);routeGlow.setStrokeWidth(24);routeGlow.setStrokeCap(Paint.Cap.ROUND);}
        void reset(){if(anim!=null)anim.cancel();raw=carProgress=0;invalidate();}
        void start(){if(getWidth()==0)return;if(anim!=null)anim.cancel();anim=ValueAnimator.ofFloat(0,1);anim.setDuration(8500);anim.setInterpolator(new LinearInterpolator());anim.addUpdateListener(a->{raw=(float)a.getAnimatedValue();float wait=scenario==1?.42f:scenario==2?.38f:.31f;if(raw<.22f)carProgress=raw/.22f*.17f;else if(raw<wait)carProgress=.17f;else carProgress=.17f+(raw-wait)/(1-wait)*.83f;report();invalidate();});anim.start();}
        void report(){if(listener==null)return;int st;String s;if(raw<.11){st=0;s="1/6 • MIRRORS";}else if(raw<.19){st=1;s=exit==1?"2/6 • SIGNAL LEFT":exit==3?"2/6 • SIGNAL RIGHT":"2/6 • SIGNAL — usually none";}else if(raw<.27){st=2;s="3/6 • POSITION";}else if(raw<(scenario==1?.42f:scenario==2?.38f:.31f)){st=3;s=scenario==0?"4/6 • SPEED • GIVE WAY":"4/6 • GIVE WAY • WAIT FOR SAFE GAP";}else if(raw<.55){st=4;s="5/6 • LOOK RIGHT • SAFE GAP";}else if(raw<.82){st=4;s="ON ROUNDABOUT • LANE DISCIPLINE";}else{st=5;s="6/6 • SIGNAL LEFT • EXIT";}listener.stage(s,Math.round(raw*100),st);}

        @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight(),cx=w*.50f,cy=h*.49f,base=Math.min(w,h);drawTerrain(c,w,h);float outer=base*(type==2?.31f:type==3?.18f:.285f);float roadW=base*(type==2?.17f:type==3?.12f:.145f);drawRoundabout(c,cx,cy,outer,roadW,w,h);if(showSigns)drawSigns(c,cx,cy,outer,roadW,w,h);if(showTraffic)drawTraffic(c,cx,cy,outer-roadW*.46f,roadW);c.save();c.rotate(approach*90,cx,cy);route=buildRoute(cx,cy,outer,roadW,w,h,exit);if(showRoute){c.drawPath(route,routeGlow);c.drawPath(route,routeP);drawRouteArrows(c,route);}drawLearnerCar(c,route,carProgress,roadW);c.restore();drawCoachCard(c,w,h);}

        void drawTerrain(Canvas c,float w,float h){p.setShader(new LinearGradient(0,0,w,h,Color.rgb(80,145,69),Color.rgb(52,113,54),Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);p.setShader(null);for(int i=0;i<38;i++){float x=(i*97%997)/997f*w,y=(i*163%991)/991f*h;float r=4+(i%5)*2;p.setColor(Color.argb(90,31,92,40));c.drawCircle(x,y,r,p);}for(int i=0;i<16;i++){float x=(i*173%1009)/1009f*w,y=(i*281%1013)/1013f*h;drawTree(c,x,y,18+(i%4)*4);}}
        void drawTree(Canvas c,float x,float y,float r){p.setColor(Color.argb(75,0,0,0));c.drawCircle(x+5,y+7,r,p);p.setColor(Color.rgb(36,102,45));c.drawCircle(x,y,r,p);p.setColor(Color.rgb(54,126,58));c.drawCircle(x-r*.25f,y-r*.2f,r*.55f,p);p.setColor(Color.rgb(31,84,38));c.drawCircle(x+r*.28f,y+r*.15f,r*.45f,p);}

        void drawRoundabout(Canvas c,float cx,float cy,float outer,float rw,float w,float h){float inner=Math.max(outer-rw,outer*.44f);p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(50,51,53));c.drawCircle(cx,cy,outer,p);drawApproach(c,cx,cy,outer,rw,w,h,0);drawApproach(c,cx,cy,outer,rw,w,h,90);drawApproach(c,cx,cy,outer,rw,w,h,180);drawApproach(c,cx,cy,outer,rw,w,h,270);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(8);p.setColor(Color.rgb(194,194,184));c.drawCircle(cx,cy,outer-3,p);p.setStrokeWidth(5);p.setColor(Color.WHITE);c.drawCircle(cx,cy,outer-rw*.52f,p);p.setPathEffect(new DashPathEffect(new float[]{18,14},0));p.setStrokeWidth(3);if(type!=3)c.drawCircle(cx,cy,outer-rw*.76f,p);if(type==2)c.drawCircle(cx,cy,outer-rw*.28f,p);p.setPathEffect(null);p.setStyle(Paint.Style.FILL);if(type==3){p.setColor(Color.WHITE);c.drawCircle(cx,cy,inner*.42f,p);p.setColor(Color.rgb(60,120,58));c.drawCircle(cx,cy,inner*.26f,p);}else{p.setColor(Color.rgb(185,185,174));c.drawCircle(cx,cy,inner+6,p);p.setColor(Color.rgb(72,132,62));c.drawCircle(cx,cy,inner,p);for(int i=0;i<18;i++){double a=i*Math.PI*2/18;p.setColor(i%2==0?Color.rgb(44,108,45):Color.rgb(62,124,52));c.drawCircle(cx+(float)Math.cos(a)*inner*.57f,cy+(float)Math.sin(a)*inner*.57f,7,p);}}if(showMarkings)drawRoadMarkings(c,cx,cy,outer,rw,w,h);}

        void drawApproach(Canvas c,float cx,float cy,float outer,float rw,float w,float h,float angle){c.save();c.rotate(angle,cx,cy);float half=rw*.78f;Path road=new Path();road.moveTo(cx-half,h);road.lineTo(cx-half,cy+outer*.88f);road.quadTo(cx-half,cy+outer*.78f,cx-rw*.67f,cy+outer*.67f);road.lineTo(cx+rw*.67f,cy+outer*.67f);road.quadTo(cx+half,cy+outer*.78f,cx+half,cy+outer*.88f);road.lineTo(cx+half,h);road.close();p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(49,50,52));c.drawPath(road,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(7);p.setColor(Color.rgb(196,194,184));c.drawPath(road,p);drawSplitter(c,cx,cy+outer*.94f,rw);c.restore();}

        void drawSplitter(Canvas c,float x,float y,float rw){Path island=new Path();island.moveTo(x,y-rw*.42f);island.lineTo(x-rw*.13f,y+rw*.60f);island.quadTo(x,y+rw*.70f,x+rw*.13f,y+rw*.60f);island.close();p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(184,184,173));c.drawPath(island,p);p.setColor(Color.rgb(78,137,67));Path grass=new Path();grass.moveTo(x,y-rw*.29f);grass.lineTo(x-rw*.075f,y+rw*.50f);grass.lineTo(x+rw*.075f,y+rw*.50f);grass.close();c.drawPath(grass,p);}

        void drawRoadMarkings(Canvas c,float cx,float cy,float outer,float rw,float w,float h){p.setColor(Color.WHITE);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setPathEffect(new DashPathEffect(new float[]{20,15},0));for(int k=0;k<4;k++){c.save();c.rotate(k*90,cx,cy);c.drawLine(cx-rw*.25f,cy+outer*.83f,cx-rw*.25f,h,p);c.drawLine(cx+rw*.25f,cy+outer*.83f,cx+rw*.25f,h,p);c.restore();}p.setPathEffect(null);for(int k=0;k<4;k++){c.save();c.rotate(k*90,cx,cy);float y=cy+outer*.77f;p.setStrokeWidth(5);p.setPathEffect(new DashPathEffect(new float[]{14,8},0));c.drawLine(cx-rw*.62f,y,cx+rw*.62f,y,p);p.setPathEffect(null);drawGiveWayTriangleRoad(c,cx-rw*.34f,y+rw*.24f,18);drawGiveWayTriangleRoad(c,cx+rw*.34f,y+rw*.24f,18);drawLaneArrow(c,cx-rw*.38f,y+rw*.72f,-1);drawLaneArrow(c,cx+rw*.38f,y+rw*.72f,1);c.restore();}}
        void drawGiveWayTriangleRoad(Canvas c,float x,float y,float s){Path t=new Path();t.moveTo(x,y+s);t.lineTo(x-s,y-s);t.lineTo(x+s,y-s);t.close();p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(Color.WHITE);c.drawPath(t,p);}
        void drawLaneArrow(Canvas c,float x,float y,int turn){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);p.setColor(Color.WHITE);c.drawLine(x,y+22,x,y-20,p);c.drawLine(x,y-20,x-9,y-8,p);c.drawLine(x,y-20,x+9,y-8,p);if(turn!=0){float d=turn*22;c.drawLine(x,y,x+d,y,p);c.drawLine(x+d,y,x+d-turn*8,y-8,p);c.drawLine(x+d,y,x+d-turn*8,y+8,p);}}

        void drawSigns(Canvas c,float cx,float cy,float outer,float rw,float w,float h){for(int k=0;k<4;k++){c.save();c.rotate(k*90,cx,cy);drawGiveWaySign(c,cx-rw*.92f,cy+outer+rw*.18f,rw*.24f);drawDirectionSign(c,cx+rw*.90f,cy+outer+rw*.78f,k==0?"SOUTH ↑":k==1?"WEST ←":k==2?"NORTH ↑":"EAST →",rw*.55f,rw*.20f);c.restore();}}
        void drawGiveWaySign(Canvas c,float x,float y,float s){Path t=new Path();t.moveTo(x,y+s);t.lineTo(x-s,y-s);t.lineTo(x+s,y-s);t.close();p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);c.drawPath(t,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(4,s*.16f));p.setColor(Color.rgb(205,35,35));c.drawPath(t,p);p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(35,35,35));p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(Math.max(9,s*.36f));c.drawText("GIVE",x,y-s*.15f,p);c.drawText("WAY",x,y+s*.23f,p);p.setTextAlign(Paint.Align.LEFT);}
        void drawDirectionSign(Canvas c,float x,float y,String txt,float ww,float hh){p.setColor(Color.rgb(15,101,70));p.setStyle(Paint.Style.FILL);RectF r=new RectF(x-ww/2,y-hh/2,x+ww/2,y+hh/2);c.drawRoundRect(r,7,7,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(Color.WHITE);c.drawRoundRect(r,7,7,p);p.setStyle(Paint.Style.FILL);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(Math.max(10,hh*.42f));c.drawText(txt,x,y+hh*.15f,p);p.setTextAlign(Paint.Align.LEFT);}

        Path buildRoute(float cx,float cy,float outer,float rw,float w,float h,int e){Path q=new Path();float startX=e==3?cx+rw*.34f:cx-rw*.34f;float rr=e==3?outer-rw*.60f:outer-rw*.25f;q.moveTo(startX,h+20);q.lineTo(startX,cy+outer+rw*.45f);q.quadTo(startX,cy+outer*.80f,cx,cy+rr);q.arcTo(new RectF(cx-rr,cy-rr,cx+rr,cy+rr),90,e*90);float off=rw*.32f;if(e==1){q.quadTo(cx-rr,cy+off,cx-outer-rw*.35f,cy+off);q.lineTo(-20,cy+off);}else if(e==2){q.quadTo(cx-off,cy-rr,cx-off,cy-outer-rw*.35f);q.lineTo(cx-off,-20);}else{q.quadTo(cx+rr,cy-off,cx+outer+rw*.35f,cy-off);q.lineTo(w+20,cy-off);}return q;}
        void drawRouteArrows(Canvas c,Path q){PathMeasure pm=new PathMeasure(q,false);float len=pm.getLength();for(float d=len*.28f;d<len*.88f;d+=len*.18f){float[] pos=new float[2],tan=new float[2];if(pm.getPosTan(d,pos,tan)){float a=(float)Math.toDegrees(Math.atan2(tan[1],tan[0]));c.save();c.translate(pos[0],pos[1]);c.rotate(a);p.setColor(Color.WHITE);p.setStyle(Paint.Style.FILL);Path ar=new Path();ar.moveTo(11,0);ar.lineTo(-7,-7);ar.lineTo(-3,0);ar.lineTo(-7,7);ar.close();c.drawPath(ar,p);c.restore();}}}

        void drawTraffic(Canvas c,float cx,float cy,float rr,float rw){int n=scenario==0?2:scenario==1?3:6;for(int i=0;i<n;i++){float deg=(raw*150+i*(360f/n)+(scenario==1?20:0));double a=Math.toRadians(deg);float x=cx+(float)Math.cos(a)*rr,y=cy+(float)Math.sin(a)*rr;drawCar(c,x,y,deg+90,rw*.34f,i%3==0?Color.rgb(208,57,49):i%3==1?Color.rgb(49,92,179):Color.rgb(214,214,214),false);}}
        void drawLearnerCar(Canvas c,Path q,float f,float rw){PathMeasure pm=new PathMeasure(q,false);float[] pos=new float[2],tan=new float[2];if(!pm.getPosTan(pm.getLength()*f,pos,tan))return;float a=(float)Math.toDegrees(Math.atan2(tan[1],tan[0]));drawCar(c,pos[0],pos[1],a,rw*.40f,Color.WHITE,true);}
        void drawCar(Canvas c,float x,float y,float angle,float size,int color,boolean learner){c.save();c.translate(x,y);c.rotate(angle);float l=size*1.65f,w=size*.78f;p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(70,0,0,0));c.drawRoundRect(-l*.48f+4,-w*.48f+5,l*.48f+4,w*.48f+5,8,8,p);p.setColor(color);c.drawRoundRect(-l*.50f,-w*.50f,l*.50f,w*.50f,9,9,p);p.setColor(Color.rgb(137,183,209));c.drawRoundRect(-l*.10f,-w*.39f,l*.20f,w*.39f,5,5,p);p.setColor(Color.rgb(25,27,31));c.drawRoundRect(-l*.36f,-w*.58f,-l*.12f,-w*.44f,3,3,p);c.drawRoundRect(l*.12f,-w*.58f,l*.36f,-w*.44f,3,3,p);c.drawRoundRect(-l*.36f,w*.44f,-l*.12f,w*.58f,3,3,p);c.drawRoundRect(l*.12f,w*.44f,l*.36f,w*.58f,3,3,p);if(learner){p.setColor(Color.rgb(0,112,220));p.setTextAlign(Paint.Align.CENTER);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(size*.34f);c.drawText("L",0,size*.11f,p);p.setTextAlign(Paint.Align.LEFT);}boolean blink=((System.currentTimeMillis()/330)%2)==0;if(blink){int sig=exit==1?-1:exit==2?(raw>.72f?-1:0):(raw>.72f?-1:1);if(sig!=0){p.setColor(Color.rgb(255,175,15));c.drawCircle(l*.43f,sig<0?-w*.35f:w*.35f,size*.09f,p);}}c.restore();}

        void drawCoachCard(Canvas c,float w,float h){if(mode==2)return;float cw=Math.min(w*.34f,300),ch=Math.min(h*.14f,120),x=16,y=h-ch-18;GradientDrawable dummy=null;p.setColor(Color.argb(220,18,31,42));p.setStyle(Paint.Style.FILL);c.drawRoundRect(new RectF(x,y,x+cw,y+ch),12,12,p);p.setColor(Color.WHITE);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(Math.max(12,ch*.13f));c.drawText("STEP "+Math.min(6,(int)(raw*6)+1)+" OF 6",x+14,y+22,p);p.setTextSize(Math.max(14,ch*.17f));String line=raw<.20f?"Prepare on approach":raw<.40f?"Slow and assess the gap":raw<.80f?"Hold your lane":"Prepare to leave";c.drawText(line,x+14,y+49,p);p.setTypeface(Typeface.DEFAULT);p.setTextSize(Math.max(11,ch*.12f));c.drawText(raw<.40f?"Use signs, mirrors and road markings":"Keep observing and follow your lane",x+14,y+74,p);}
    }
}
