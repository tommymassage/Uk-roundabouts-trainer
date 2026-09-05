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
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;

public class MainActivity extends Activity {
    private PhotoRoundaboutView roadView;
    private TextView[] stepViews=new TextView[6];

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setBackgroundColor(Color.rgb(242,246,248));

        roadView=new PhotoRoundaboutView(this);
        root.addView(roadView,new LinearLayout.LayoutParams(0,-1,3));

        ScrollView scroll=new ScrollView(this);
        LinearLayout panel=new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(18,14,18,18);
        scroll.addView(panel);
        root.addView(scroll,new LinearLayout.LayoutParams(0,-1,2));

        panel.addView(text("UK ROUNDABOUTS TRAINER  v0.6.2",24,true));
        TextView intro=text("Real aerial background • UK coaching overlay • MSPSL",13,false); intro.setPadding(0,4,0,10); panel.addView(intro);

        LinearLayout modeRow=new LinearLayout(this); modeRow.setOrientation(LinearLayout.HORIZONTAL);
        Button realistic=button("REAL AERIAL"); Button map=button("TRAINING MAP");
        modeRow.addView(realistic,new LinearLayout.LayoutParams(0,48,1)); modeRow.addView(map,new LinearLayout.LayoutParams(0,48,1)); panel.addView(modeRow);

        panel.addView(text("Approach road",14,true)); Spinner approach=spinner(new String[]{"South","West","North","East"}); panel.addView(approach);
        panel.addView(text("Exit",14,true)); Spinner exit=spinner(new String[]{"1st exit / left","2nd exit / ahead","3rd exit / right"}); panel.addView(exit);
        panel.addView(text("Traffic",14,true)); Spinner traffic=spinner(new String[]{"Clear roundabout","Vehicle from the right","Busy circulating traffic"}); panel.addView(traffic);
        panel.addView(text("Mode",14,true)); Spinner mode=spinner(new String[]{"Guided demo","Practice","Test mode"}); panel.addView(mode);

        CheckBox route=new CheckBox(this); route.setText("Show training route"); route.setChecked(true); panel.addView(route);
        CheckBox cars=new CheckBox(this); cars.setText("Show other traffic"); cars.setChecked(true); panel.addView(cars);
        CheckBox guide=new CheckBox(this); guide.setText("Show coaching card"); guide.setChecked(true); panel.addView(guide);

        Button start=button("▶  START DRIVING DEMO"); start.setTextSize(16); panel.addView(start,new LinearLayout.LayoutParams(-1,54));

        panel.addView(text("MSPSL Guide",17,true));
        String[] labels={"Mirrors — check all mirrors","Signal — communicate your intention","Position — follow signs and lane markings","Speed — slow down and be ready to give way","Look — assess traffic from the right","Leave — signal left and exit safely"};
        for(int i=0;i<6;i++){ stepViews[i]=text((i+1)+"   "+labels[i],14,false); stepViews[i].setPadding(8,8,8,8); panel.addView(stepViews[i]); }

        TextView note=text("ⓘ Give way to traffic from the right unless signs, signals or road markings indicate otherwise.",12,true); note.setPadding(10,10,10,10); note.setBackgroundColor(Color.rgb(220,238,253)); panel.addView(note);
        TextView stage=text("Ready • MIRRORS",18,true); stage.setPadding(0,12,0,4); panel.addView(stage);
        ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal); progress.setMax(100); panel.addView(progress,new LinearLayout.LayoutParams(-1,18));
        TextView result=text("Training result: —",14,true); result.setPadding(0,8,0,0); panel.addView(result);
        TextView credit=text("Aerial background: Partney Roundabout, Chris / Geograph, CC BY-SA 2.0. Training overlay added by the app.",10,false); credit.setPadding(0,12,0,0); panel.addView(credit);

        roadView.listener=(s,p,step)->{ stage.setText(s); progress.setProgress(p); highlight(step); if(p>=100)result.setText("Training result: COMPLETE"); };

        Runnable update=()->{
            roadView.approach=approach.getSelectedItemPosition();
            roadView.exit=exit.getSelectedItemPosition()+1;
            roadView.traffic=traffic.getSelectedItemPosition();
            roadView.trainingMode=mode.getSelectedItemPosition();
            roadView.showRoute=route.isChecked() && roadView.trainingMode!=2;
            roadView.showTraffic=cars.isChecked();
            roadView.showGuide=guide.isChecked() && roadView.trainingMode!=2;
            roadView.reset(); progress.setProgress(0); stage.setText("Ready • MIRRORS"); result.setText("Training result: —"); highlight(0);
        };

        approach.setOnItemSelectedListener(selection(update)); exit.setOnItemSelectedListener(selection(update)); traffic.setOnItemSelectedListener(selection(update)); mode.setOnItemSelectedListener(selection(update));
        route.setOnClickListener(v->update.run()); cars.setOnClickListener(v->update.run()); guide.setOnClickListener(v->update.run());
        start.setOnClickListener(v->{update.run();roadView.start();});
        realistic.setOnClickListener(v->{roadView.photoMode=true;roadView.invalidate();});
        map.setOnClickListener(v->{roadView.photoMode=false;roadView.invalidate();});

        setContentView(root); highlight(0);
    }

    private void highlight(int active){ for(int i=0;i<6;i++){ stepViews[i].setBackgroundColor(i==active?Color.rgb(218,238,255):Color.TRANSPARENT); stepViews[i].setTypeface(null,i==active?1:0); } }
    private TextView text(String s,int size,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(Color.rgb(28,38,47));if(bold)v.setTypeface(null,1);return v;}
    private Spinner spinner(String[] a){Spinner s=new Spinner(this);s.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,a));return s;}
    private Button button(String s){Button b=new Button(this);b.setText(s);return b;}
    private AdapterView.OnItemSelectedListener selection(Runnable r){return new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?>p,View v,int x,long id){r.run();}public void onNothingSelected(AdapterView<?>p){}};}

    interface StageListener{void stage(String s,int progress,int step);}

    static class PhotoRoundaboutView extends View{
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG),routePaint=new Paint(Paint.ANTI_ALIAS_FLAG),shadow=new Paint(Paint.ANTI_ALIAS_FLAG);
        Bitmap aerial;
        boolean photoMode=true,showRoute=true,showTraffic=true,showGuide=true;
        int approach=0,exit=1,traffic=0,trainingMode=0;
        float raw=0,carProgress=0;
        ValueAnimator animator; StageListener listener;

        final String PHOTO="https://commons.wikimedia.org/wiki/Special:Redirect/file/Partney_Roundabout_on_the_A16_and_A158%2C_aerial_2026_-_geograph.org.uk_-_8309775.jpg";

        PhotoRoundaboutView(Context c){
            super(c);
            routePaint.setColor(Color.rgb(25,235,77)); routePaint.setStyle(Paint.Style.STROKE); routePaint.setStrokeWidth(12); routePaint.setStrokeCap(Paint.Cap.ROUND); routePaint.setStrokeJoin(Paint.Join.ROUND);
            shadow.setColor(Color.argb(120,0,0,0)); shadow.setStyle(Paint.Style.STROKE); shadow.setStrokeWidth(22); shadow.setStrokeCap(Paint.Cap.ROUND);
            loadPhoto();
        }

        void loadPhoto(){
            new Thread(()->{
                try{
                    HttpURLConnection con=(HttpURLConnection)new URL(PHOTO).openConnection();
                    con.setInstanceFollowRedirects(true); con.setConnectTimeout(12000); con.setReadTimeout(15000); con.setRequestProperty("User-Agent","UK-Roundabouts-Trainer/0.6.2");
                    InputStream in=con.getInputStream(); Bitmap b=BitmapFactory.decodeStream(in); in.close(); con.disconnect();
                    if(b!=null){aerial=b; post(this::invalidate);}
                }catch(Exception ignored){}
            }).start();
        }

        void reset(){if(animator!=null)animator.cancel();raw=0;carProgress=0;invalidate();}
        void start(){
            if(animator!=null)animator.cancel(); animator=ValueAnimator.ofFloat(0,1); animator.setDuration(trainingMode==2?6500:8200); animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(a->{raw=(float)a.getAnimatedValue(); float stop=traffic==0?.26f:traffic==1?.39f:.34f; if(raw<.20f)carProgress=raw/.20f*.14f; else if(raw<stop)carProgress=.14f; else carProgress=.14f+(raw-stop)/(1f-stop)*.86f; report(); invalidate();}); animator.start();
        }

        void report(){if(listener==null)return;String s;int st;if(raw<.12){s="1/6 • MIRRORS";st=0;}else if(raw<.22){s=exit==1?"2/6 • SIGNAL LEFT":exit==3?"2/6 • SIGNAL RIGHT":"2/6 • SIGNAL normally none";st=1;}else if(raw<.32){s="3/6 • POSITION";st=2;}else if(raw<.46){s=traffic==0?"4/6 • SPEED • GIVE WAY":"4/6 • WAIT • TRAFFIC FROM RIGHT";st=3;}else if(raw<.62){s="5/6 • LOOK RIGHT • safe gap";st=4;}else if(raw<.86){s="ON ROUNDABOUT • lane discipline";st=4;}else if(raw<.98){s="6/6 • SIGNAL LEFT • EXIT";st=5;}else{s="COMPLETE • safe exit";st=5;}listener.stage(s,Math.round(raw*100),st);}

        @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();
            if(photoMode && aerial!=null) drawPhoto(c,w,h); else drawFallback(c,w,h);
            float cx=w*.49f,cy=h*.49f,r=Math.min(w,h)*.255f;
            Path route=route(cx,cy,r,w,h,exit);
            if(showRoute){c.drawPath(route,shadow);c.drawPath(route,routePaint);drawRouteArrows(c,route);}
            if(showTraffic)drawTraffic(c,cx,cy,r);
            drawCarOnPath(c,route,carProgress,Color.rgb(35,105,215));
            if(showGuide)drawGuide(c,w,h);
        }

        void drawPhoto(Canvas c,float w,float h){
            float sw=aerial.getWidth(),sh=aerial.getHeight(),scale=Math.max(w/sw,h/sh); float dw=sw*scale,dh=sh*scale; RectF dst=new RectF((w-dw)/2,(h-dh)/2,(w+dw)/2,(h+dh)/2); c.drawBitmap(aerial,null,dst,p);
            p.setColor(Color.argb(25,0,0,0));c.drawRect(0,0,w,h,p);
        }

        void drawFallback(Canvas c,float w,float h){c.drawColor(Color.rgb(82,126,70));p.setColor(Color.rgb(52,54,56));float cx=w*.5f,cy=h*.5f,r=Math.min(w,h)*.25f;p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(r*.72f);c.drawCircle(cx,cy,r,p);p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(70,113,62));c.drawCircle(cx,cy,r*.62f,p);}

        Path route(float cx,float cy,float r,float w,float h,int e){
            Path q=new Path();float lane=Math.min(w,h)*.04f;float sx=cx-lane*.7f; q.moveTo(sx,h+10); q.lineTo(sx,cy+r*1.25f); q.quadTo(sx,cy+r*1.04f,cx-r*.05f,cy+r*1.04f); float rr=r*1.04f; q.arcTo(new RectF(cx-rr,cy-rr,cx+rr,cy+rr),92,e*90);
            if(e==1){q.quadTo(cx-rr,cy+lane*.15f,cx-rr*1.25f,cy+lane*.15f);q.lineTo(-10,cy+lane*.15f);}else if(e==2){q.quadTo(cx-lane*.15f,cy-rr,cx-lane*.15f,cy-rr*1.25f);q.lineTo(cx-lane*.15f,-10);}else{q.quadTo(cx+rr,cy-lane*.15f,cx+rr*1.25f,cy-lane*.15f);q.lineTo(w+10,cy-lane*.15f);} return q;
        }

        void drawRouteArrows(Canvas c,Path path){PathMeasure pm=new PathMeasure(path,false);p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);for(float f=.30f;f<.86f;f+=.16f){float[] pos=new float[2],tan=new float[2];pm.getPosTan(pm.getLength()*f,pos,tan);float a=(float)Math.toDegrees(Math.atan2(tan[1],tan[0]));c.save();c.translate(pos[0],pos[1]);c.rotate(a);Path t=new Path();t.moveTo(12,0);t.lineTo(-8,-7);t.lineTo(-8,7);t.close();c.drawPath(t,p);c.restore();}}

        void drawTraffic(Canvas c,float cx,float cy,float r){int n=traffic==0?1:traffic==1?2:5;for(int i=0;i<n;i++){float a=(float)Math.toRadians((raw*180+i*(360f/n)+20)%360);float x=cx+(float)Math.cos(a)*r*1.03f,y=cy+(float)Math.sin(a)*r*1.03f;drawCar(c,x,y,(float)Math.toDegrees(a)+90,i%2==0?Color.rgb(190,45,40):Color.rgb(40,55,75));}}
        void drawCarOnPath(Canvas c,Path q,float f,int color){PathMeasure pm=new PathMeasure(q,false);float[] pos=new float[2],tan=new float[2];if(pm.getPosTan(pm.getLength()*f,pos,tan)){drawCar(c,pos[0],pos[1],(float)Math.toDegrees(Math.atan2(tan[1],tan[0])),color);}}
        void drawCar(Canvas c,float x,float y,float angle,int color){c.save();c.translate(x,y);c.rotate(angle);float l=Math.max(34,Math.min(getWidth(),getHeight())*.055f),ww=l*.46f;p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(90,0,0,0));c.drawRoundRect(-l*.48f+3,-ww*.48f+4,l*.48f+3,ww*.48f+4,8,8,p);p.setColor(color);c.drawRoundRect(-l*.50f,-ww*.50f,l*.50f,ww*.50f,8,8,p);p.setColor(Color.rgb(190,220,235));c.drawRoundRect(-l*.16f,-ww*.40f,l*.18f,ww*.40f,4,4,p);p.setColor(Color.rgb(25,25,25));c.drawRect(-l*.34f,-ww*.58f,-l*.14f,-ww*.45f,p);c.drawRect(l*.14f,-ww*.58f,l*.34f,-ww*.45f,p);c.drawRect(-l*.34f,ww*.45f,-l*.14f,ww*.58f,p);c.drawRect(l*.14f,ww*.45f,l*.34f,ww*.58f,p);c.restore();}

        void drawGuide(Canvas c,float w,float h){float bw=w*.34f,bh=h*.17f,x=14,y=h-bh-14;p.setColor(Color.argb(220,14,34,42));p.setStyle(Paint.Style.FILL);c.drawRoundRect(x,y,x+bw,y+bh,12,12,p);p.setColor(Color.WHITE);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(Math.max(13,h*.018f));c.drawText("STEP "+currentStep()+" OF 6",x+14,y+24,p);p.setTextSize(Math.max(16,h*.024f));c.drawText(guideTitle(),x+14,y+51,p);p.setTypeface(Typeface.DEFAULT);p.setTextSize(Math.max(12,h*.017f));c.drawText(guideBody(),x+14,y+76,p);}
        int currentStep(){if(raw<.12)return 1;if(raw<.22)return 2;if(raw<.32)return 3;if(raw<.46)return 4;if(raw<.86)return 5;return 6;}
        String guideTitle(){int s=currentStep();if(s==1)return"Check mirrors";if(s==2)return"Signal your intention";if(s==3)return"Choose the correct lane";if(s==4)return"Slow down and give way";if(s==5)return"Look right and judge the gap";return"Signal left and leave";}
        String guideBody(){return currentStep()==4&&traffic>0?"Traffic is approaching from your right — wait.":"Follow real signs and road markings.";}
    }
}
