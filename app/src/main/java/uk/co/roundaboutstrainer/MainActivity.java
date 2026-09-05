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
        root.setBackgroundColor(Color.rgb(244,247,244));

        roadView=new RoundaboutView(this);
        root.addView(roadView,phone?new LinearLayout.LayoutParams(-1,0,5):new LinearLayout.LayoutParams(0,-1,62));

        ScrollView scroll=new ScrollView(this);
        LinearLayout panel=new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(22,16,22,20);
        panel.setBackgroundColor(Color.rgb(249,251,249));
        scroll.addView(panel);
        root.addView(scroll,phone?new LinearLayout.LayoutParams(-1,0,5):new LinearLayout.LayoutParams(0,-1,38));

        panel.addView(text("UK ROUNDABOUTS TRAINER  v0.6",23,true));
        TextView sub=text("Learn. Practise. Drive with confidence.",14,false); sub.setTextColor(Color.DKGRAY); sub.setPadding(0,2,0,14); panel.addView(sub);

        panel.addView(label("Approach road"));
        Spinner approach=spinner(new String[]{"South","West","North","East"}); panel.addView(approach);
        panel.addView(label("Exit"));
        Spinner exit=spinner(new String[]{"1st exit / left","2nd exit / ahead","3rd exit / right"}); panel.addView(exit);
        panel.addView(label("Roundabout type"));
        Spinner type=spinner(new String[]{"Standard","Spiral training","Large (3 lanes)","Mini"}); panel.addView(type);
        panel.addView(label("Traffic"));
        Spinner scenario=spinner(new String[]{"Clear roundabout","Vehicle from the right","Busy circulating traffic"}); panel.addView(scenario);
        panel.addView(label("Mode"));
        Spinner difficulty=spinner(new String[]{"Guided demo","Practice","Test mode"}); panel.addView(difficulty);

        CheckBox markings=check("Show lane markings and arrows",true); panel.addView(markings);
        CheckBox traffic=check("Show other traffic",true); panel.addView(traffic);
        CheckBox signs=check("Show road signs",true); panel.addView(signs);
        CheckBox route=check("Show training route",true); panel.addView(route);

        Button start=new Button(this); start.setText("▶  START DRIVING DEMO"); start.setTextSize(16); start.setTextColor(Color.WHITE); start.setAllCaps(false); start.setBackground(rounded(Color.rgb(22,150,73),12));
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,58); bp.setMargins(0,10,0,14); panel.addView(start,bp);

        TextView guideTitle=text("MSPSL Guide",17,true); guideTitle.setPadding(0,4,0,6); panel.addView(guideTitle);
        String[] steps={"Mirrors — check all mirrors","Signal — communicate your intention","Position — use signs and lane markings","Speed — slow down and be ready to give way","Look — assess traffic from the right","Leave — signal left and exit safely"};
        for(int i=0;i<6;i++){stepViews[i]=text((i+1)+"   "+steps[i],14,false);stepViews[i].setPadding(10,8,10,8);panel.addView(stepViews[i]);}

        TextView info=text("ⓘ  Give way to traffic from the right, unless signs, signals or road markings indicate otherwise.",13,true); info.setPadding(14,12,14,12); info.setBackground(rounded(Color.rgb(221,238,252),12)); panel.addView(info);

        TextView stage=text("Ready • MIRRORS",17,true); stage.setPadding(0,14,0,6); panel.addView(stage);
        ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal); progress.setMax(100); panel.addView(progress,new LinearLayout.LayoutParams(-1,18));
        TextView result=text("Training result: —",14,true); result.setPadding(0,9,0,2); panel.addView(result);

        roadView.listener=(s,v,step)->{stage.setText(s);progress.setProgress(v);highlight(step);if(v>=100)result.setText(roadView.resultText());};

        Runnable update=()->{
            roadView.approach=approach.getSelectedItemPosition();
            roadView.exit=exit.getSelectedItemPosition()+1;
            roadView.type=type.getSelectedItemPosition();
            roadView.scenario=scenario.getSelectedItemPosition();
            roadView.difficulty=difficulty.getSelectedItemPosition();
            roadView.showMarkings=markings.isChecked(); roadView.showTraffic=traffic.isChecked(); roadView.showSigns=signs.isChecked(); roadView.showRoute=route.isChecked()&&roadView.difficulty<2;
            roadView.resetCar(); stage.setText("Ready • MIRRORS"); progress.setProgress(0); result.setText("Training result: —"); highlight(-1);
        };

        start.setOnClickListener(v->{update.run();roadView.startCar();});
        markings.setOnClickListener(v->update.run()); traffic.setOnClickListener(v->update.run()); signs.setOnClickListener(v->update.run()); route.setOnClickListener(v->update.run());
        approach.setOnItemSelectedListener(selection(update));exit.setOnItemSelectedListener(selection(update));type.setOnItemSelectedListener(selection(update));scenario.setOnItemSelectedListener(selection(update));difficulty.setOnItemSelectedListener(selection(update));
        setContentView(root);
    }

    private TextView text(String s,int size,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(Color.rgb(30,42,50));if(bold)v.setTypeface(null,Typeface.BOLD);return v;}
    private TextView label(String s){TextView v=text(s,13,true);v.setPadding(0,8,0,2);v.setTextColor(Color.DKGRAY);return v;}
    private Spinner spinner(String[] a){Spinner s=new Spinner(this);s.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,a));return s;}
    private CheckBox check(String s,boolean on){CheckBox c=new CheckBox(this);c.setText(s);c.setChecked(on);return c;}
    private GradientDrawable rounded(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(radius);return g;}
    private AdapterView.OnItemSelectedListener selection(Runnable r){return new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?>p,View v,int x,long id){r.run();}public void onNothingSelected(AdapterView<?>p){}};}
    private void highlight(int active){for(int i=0;i<6;i++){stepViews[i].setTypeface(null,i==active?Typeface.BOLD:Typeface.NORMAL);stepViews[i].setBackgroundColor(i==active?Color.rgb(225,241,255):Color.TRANSPARENT);}}

    interface StageListener{void stage(String s,int progress,int step);}

    static class RoundaboutView extends View{
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG),routePaint=new Paint(Paint.ANTI_ALIAS_FLAG),routeGlow=new Paint(Paint.ANTI_ALIAS_FLAG);
        int approach=0,exit=1,type=0,difficulty=0,scenario=0; boolean showMarkings=true,showTraffic=true,showSigns=true,showRoute=true;
        float raw=0,carProgress=0; ValueAnimator animator; StageListener listener; Path route=new Path();

        RoundaboutView(Context c){super(c);routePaint.setColor(Color.rgb(45,225,72));routePaint.setStyle(Paint.Style.STROKE);routePaint.setStrokeWidth(10);routePaint.setStrokeCap(Paint.Cap.ROUND);routeGlow.setColor(Color.argb(80,45,225,72));routeGlow.setStyle(Paint.Style.STROKE);routeGlow.setStrokeWidth(28);routeGlow.setStrokeCap(Paint.Cap.ROUND);}
        void resetCar(){if(animator!=null)animator.cancel();raw=carProgress=0;invalidate();}
        void startCar(){if(getWidth()==0)return;if(animator!=null)animator.cancel();animator=ValueAnimator.ofFloat(0,1);animator.setDuration(difficulty==2?7000:8500);animator.setInterpolator(new LinearInterpolator());animator.addUpdateListener(a->{raw=(float)a.getAnimatedValue();float stopStart=.21f,stopEnd=scenario==1?.43f:scenario==2?.40f:.32f;if(raw<stopStart)carProgress=raw/stopStart*.18f;else if(raw<stopEnd)carProgress=.18f;else carProgress=.18f+(raw-stopEnd)/(1-stopEnd)*.82f;report();invalidate();});animator.start();}
        String resultText(){return difficulty==2?"Training result: TEST COMPLETE":"Training result: COMPLETE • MSPSL sequence finished";}
        void report(){if(listener==null)return;String s;int st;float stopEnd=scenario==1?.43f:scenario==2?.40f:.32f;if(raw<.10){s="1/6 • MIRRORS";st=0;}else if(raw<.18){s=exit==1?"2/6 • SIGNAL LEFT":exit==3?"2/6 • SIGNAL RIGHT":"2/6 • SIGNAL normally none";st=1;}else if(raw<.25){s="3/6 • POSITION";st=2;}else if(raw<stopEnd){s=scenario==1?"4/6 • GIVE WAY — vehicle from right: WAIT":scenario==2?"4/6 • SPEED — find a safe gap":"4/6 • SPEED — assess entry";st=3;}else if(raw<.55){s="5/6 • LOOK RIGHT — safe gap";st=4;}else if(raw<.80){s="ON ROUNDABOUT • hold lane • observe";st=4;}else if(raw<.97){s="6/6 • SIGNAL LEFT • LEAVE";st=5;}else{s="COMPLETE • safe exit";st=5;}listener.stage(s,Math.round(raw*100),st);}

        @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight(),cx=w*.50f,cy=h*.50f,base=Math.min(w,h);float r=base*(type==3?.135f:type==2?.225f:.205f);float rw=type==3?r*.9f:type==2?r*1.30f:r*1.08f;drawGround(c,w,h);drawRoad(c,cx,cy,r,rw,w,h);drawIsland(c,cx,cy,r,rw);if(showMarkings)drawMarkings(c,cx,cy,r,rw,w,h);if(showSigns)drawSigns(c,cx,cy,r,rw,w,h);if(showTraffic)drawTraffic(c,cx,cy,r,rw);c.save();c.rotate(approach*90,cx,cy);route=makeRoute(cx,cy,r,rw,w,h,exit);if(showRoute){c.drawPath(route,routeGlow);c.drawPath(route,routePaint);drawRouteArrows(c,route);}drawGiveWay(c,cx,cy+r+rw*.36f,rw);drawLearnerCar(c,route,carProgress,Math.max(32,rw*.22f));c.restore();drawCoachCard(c,w,h);}

        void drawGround(Canvas c,float w,float h){c.drawColor(Color.rgb(69,127,58));p.setStyle(Paint.Style.FILL);for(int i=0;i<90;i++){float x=(i*83)%Math.max(1,w),y=(i*137)%Math.max(1,h);p.setColor(i%3==0?Color.rgb(58,112,51):Color.rgb(77,139,65));c.drawCircle(x,y,3+(i%4),p);}}

        void drawRoad(Canvas c,float cx,float cy,float r,float rw,float w,float h){p.setStyle(Paint.Style.STROKE);p.setColor(Color.rgb(185,185,180));p.setStrokeWidth(rw+18);c.drawCircle(cx,cy,r,p);p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(185,185,180));c.drawRect(cx-rw/2-9,cy+r,cx+rw/2+9,h,p);c.drawRect(cx-rw/2-9,0,cx+rw/2+9,cy-r,p);c.drawRect(0,cy-rw/2-9,cx-r,cy+rw/2+9,p);c.drawRect(cx+r,cy-rw/2-9,w,cy+rw/2+9,p);p.setColor(Color.rgb(54,56,58));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(rw);c.drawCircle(cx,cy,r,p);p.setStyle(Paint.Style.FILL);c.drawRect(cx-rw/2,cy+r,cx+rw/2,h,p);c.drawRect(cx-rw/2,0,cx+rw/2,cy-r,p);c.drawRect(0,cy-rw/2,cx-r,cy+rw/2,p);c.drawRect(cx+r,cy-rw/2,w,cy+rw/2,p);}

        void drawIsland(Canvas c,float cx,float cy,float r,float rw){float ir=Math.max(18,r-rw*.55f);p.setStyle(Paint.Style.FILL);if(type==3){p.setColor(Color.WHITE);c.drawCircle(cx,cy,ir,p);p.setColor(Color.rgb(45,95,150));c.drawCircle(cx,cy,ir*.45f,p);p.setColor(Color.WHITE);p.setStrokeWidth(5);p.setStyle(Paint.Style.STROKE);c.drawArc(new RectF(cx-ir*.25f,cy-ir*.25f,cx+ir*.25f,cy+ir*.25f),190,160,false,p);}else{p.setColor(Color.rgb(145,145,138));c.drawCircle(cx,cy,ir+7,p);p.setColor(Color.rgb(53,112,49));c.drawCircle(cx,cy,ir,p);for(int i=0;i<14;i++){double a=i*Math.PI*2/14;p.setColor(i%2==0?Color.rgb(44,92,42):Color.rgb(72,132,60));c.drawCircle(cx+(float)Math.cos(a)*ir*.62f,cy+(float)Math.sin(a)*ir*.62f,ir*.10f,p);}}}

        void drawMarkings(Canvas c,float cx,float cy,float r,float rw,float w,float h){p.setColor(Color.WHITE);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setPathEffect(new DashPathEffect(new float[]{16,13},0));if(type!=3){c.drawCircle(cx,cy,r,p);if(type==2){c.drawCircle(cx,cy,r-rw*.22f,p);c.drawCircle(cx,cy,r+rw*.22f,p);}}c.drawLine(cx,cy+r,cx,h,p);c.drawLine(cx,0,cx,cy-r,p);c.drawLine(0,cy,cx-r,cy,p);c.drawLine(cx+r,cy,w,cy,p);p.setPathEffect(null);drawLaneArrow(c,cx-rw*.23f,h*.84f,-1);drawLaneArrow(c,cx+rw*.23f,h*.84f,1);}

        void drawLaneArrow(Canvas c,float x,float y,int dir){p.setColor(Color.WHITE);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);c.drawLine(x,y+24,x,y-25,p);c.drawLine(x,y-25,x-9,y-12,p);c.drawLine(x,y-25,x+9,y-12,p);if(type!=3){float dx=dir*20;c.drawLine(x,y,x+dx,y,p);c.drawLine(x+dx,y,x+dx-dir*8,y-8,p);c.drawLine(x+dx,y,x+dx-dir*8,y+8,p);}}

        void drawGiveWay(Canvas c,float cx,float y,float rw){p.setColor(Color.WHITE);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);p.setPathEffect(new DashPathEffect(new float[]{14,8},0));c.drawLine(cx-rw*.47f,y,cx+rw*.47f,y,p);p.setPathEffect(null);}

        void drawSigns(Canvas c,float cx,float cy,float r,float rw,float w,float h){drawGiveWaySign(c,cx-rw*.72f,cy+r+rw*.52f);drawGiveWaySign(c,cx+rw*.72f,cy-r-rw*.52f);drawDirectionSign(c,cx-rw*.05f,h*.93f,"↑ SOUTH");drawDirectionSign(c,w*.88f,cy-rw*.28f,"EAST →");drawDirectionSign(c,w*.08f,cy+rw*.35f,"← WEST");drawDirectionSign(c,cx-rw*.05f,h*.07f,"↑ NORTH");}

        void drawGiveWaySign(Canvas c,float x,float y){Path t=new Path();t.moveTo(x-18,y-14);t.lineTo(x+18,y-14);t.lineTo(x,y+18);t.close();p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);c.drawPath(t,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);p.setColor(Color.rgb(195,35,35));c.drawPath(t,p);p.setStyle(Paint.Style.FILL);p.setColor(Color.BLACK);p.setTextSize(8);p.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("GIVE",x-10,y-3,p);c.drawText("WAY",x-9,y+7,p);}

        void drawDirectionSign(Canvas c,float x,float y,String s){p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(22,91,62));RectF box=new RectF(x-46,y-19,x+46,y+19);c.drawRoundRect(box,5,5,p);p.setStyle(Paint.Style.STROKE);p.setColor(Color.WHITE);p.setStrokeWidth(2);c.drawRoundRect(box,5,5,p);p.setStyle(Paint.Style.FILL);p.setTextSize(12);p.setColor(Color.WHITE);p.setTypeface(Typeface.DEFAULT_BOLD);c.drawText(s,x-37,y+5,p);}

        Path makeRoute(float cx,float cy,float r,float rw,float w,float h,int e){Path q=new Path();float ex=e==3?cx+rw*.23f:cx-rw*.23f,rr=e==3?r-rw*.19f:r+rw*.19f;if(type==3){ex=cx-rw*.08f;rr=r;}q.moveTo(ex,h+10);q.lineTo(ex,cy+rr+rw*.28f);q.quadTo(ex,cy+rr,cx,cy+rr);q.arcTo(new RectF(cx-rr,cy-rr,cx+rr,cy+rr),90,e*90);float o=rw*.23f;if(e==1){q.quadTo(cx-rr,cy+o,cx-rr-rw*.22f,cy+o);q.lineTo(-10,cy+o);}else if(e==2){q.quadTo(cx-o,cy-rr,cx-o,cy-rr-rw*.22f);q.lineTo(cx-o,-10);}else{q.quadTo(cx+rr,cy-o,cx+rr+rw*.22f,cy-o);q.lineTo(w+10,cy-o);}return q;}

        void drawRouteArrows(Canvas c,Path q){PathMeasure pm=new PathMeasure(q,false);for(float f=.30f;f<.90f;f+=.22f){float[] pos=new float[2],tan=new float[2];if(pm.getPosTan(pm.getLength()*f,pos,tan)){float a=(float)Math.toDegrees(Math.atan2(tan[1],tan[0]));c.save();c.translate(pos[0],pos[1]);c.rotate(a);p.setColor(Color.WHITE);p.setStyle(Paint.Style.FILL);Path ar=new Path();ar.moveTo(12,0);ar.lineTo(-4,-7);ar.lineTo(-4,7);ar.close();c.drawPath(ar,p);c.restore();}}}

        void drawTraffic(Canvas c,float cx,float cy,float r,float rw){int n=scenario==0?1:scenario==1?2:5;for(int i=0;i<n;i++){float deg=scenario==1&&i==0?25+raw*170:(raw*220+i*(360f/n));double a=Math.toRadians(deg);float rr=r+(i%2==0?rw*.16f:-rw*.16f);float x=cx+(float)Math.cos(a)*rr,y=cy+(float)Math.sin(a)*rr;drawCar(c,x,y,deg+90,rw*.19f,i%2==0?Color.rgb(72,105,160):Color.rgb(185,55,48),false);}}

        void drawLearnerCar(Canvas c,Path q,float f,float size){PathMeasure pm=new PathMeasure(q,false);float[] pos=new float[2],tan=new float[2];if(!pm.getPosTan(pm.getLength()*f,pos,tan))return;float a=(float)Math.toDegrees(Math.atan2(tan[1],tan[0]));drawCar(c,pos[0],pos[1],a,size,Color.WHITE,true);}

        void drawCar(Canvas c,float x,float y,float angle,float s,int color,boolean learner){c.save();c.translate(x,y);c.rotate(angle);float l=Math.max(34,s),ww=l*.48f;p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(60,0,0,0));c.drawRoundRect(-l*.48f+4,-ww*.48f+5,l*.48f+4,ww*.48f+5,6,6,p);p.setColor(color);c.drawRoundRect(-l*.5f,-ww*.5f,l*.5f,ww*.5f,7,7,p);p.setColor(Color.rgb(130,185,210));c.drawRoundRect(-l*.10f,-ww*.40f,l*.20f,ww*.40f,3,3,p);p.setColor(Color.BLACK);c.drawRect(-l*.34f,-ww*.59f,-l*.10f,-ww*.48f,p);c.drawRect(l*.10f,-ww*.59f,l*.34f,-ww*.48f,p);c.drawRect(-l*.34f,ww*.48f,-l*.10f,ww*.59f,p);c.drawRect(l*.10f,ww*.48f,l*.34f,ww*.59f,p);if(learner){boolean blink=((System.currentTimeMillis()/350)%2)==0;int sig=signal();if(blink&&sig!=0){p.setColor(Color.rgb(255,165,0));c.drawCircle(l*.40f,sig<0?-ww*.42f:ww*.42f,4,p);}}c.restore();}
        int signal(){if(exit==1)return-1;if(exit==2)return raw>.72f?-1:0;return raw<.72f?1:-1;}

        void drawCoachCard(Canvas c,float w,float h){if(difficulty==2)return;float cw=Math.min(w*.34f,310),ch=105,x=18,y=h-ch-18;p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(215,18,30,42));c.drawRoundRect(new RectF(x,y,x+cw,y+ch),12,12,p);p.setColor(Color.WHITE);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(12);c.drawText("STEP "+Math.min(6,Math.max(1,(int)(raw*6)+1))+" OF 6",x+14,y+22,p);p.setTextSize(16);String msg=raw<.18?"Prepare on approach":raw<.42?"Slow down and give way":raw<.78?"Enter and hold your lane":"Signal left and exit";c.drawText(msg,x+14,y+51,p);p.setTypeface(Typeface.DEFAULT);p.setTextSize(12);String sub=raw<.42?"Keep left and follow signs / road markings":"Keep observing traffic and lane position";c.drawText(sub,x+14,y+75,p);}
    }
}
