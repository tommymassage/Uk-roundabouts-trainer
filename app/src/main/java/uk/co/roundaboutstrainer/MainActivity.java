package uk.co.roundaboutstrainer;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.*;
import android.view.animation.LinearInterpolator;
import android.widget.*;
import android.content.Context;
import android.content.res.Configuration;

public class MainActivity extends Activity {
    private RoundaboutView roadView;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        boolean phone = getResources().getConfiguration().smallestScreenWidthDp < 600;
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(phone ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        root.setPadding(12,12,12,12);
        root.setBackgroundColor(Color.rgb(238,245,238));

        roadView = new RoundaboutView(this);
        root.addView(roadView, phone ? new LinearLayout.LayoutParams(-1,0,5) : new LinearLayout.LayoutParams(0,-1,3));

        ScrollView scroll = new ScrollView(this);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(20,8,16,16);
        scroll.addView(panel);
        root.addView(scroll, phone ? new LinearLayout.LayoutParams(-1,0,4) : new LinearLayout.LayoutParams(0,-1,2));

        panel.addView(text("UK ROUNDABOUTS TRAINER  v0.4",24,true));
        TextView intro=text("Responsive phone/tablet training • MSPSL • lane choice • traffic awareness",14,false); intro.setPadding(0,8,0,10); panel.addView(intro);

        panel.addView(text("Roundabout type",15,true));
        Spinner type=spinner(new String[]{"Standard 2-lane","Compact 1-lane","Large 3-lane training"}); panel.addView(type);
        panel.addView(text("Approach road",15,true));
        Spinner approach=spinner(new String[]{"South","West","North","East"}); panel.addView(approach);
        panel.addView(text("Exit",15,true));
        Spinner exit=spinner(new String[]{"1st exit / left","2nd exit / ahead","3rd exit / right"}); panel.addView(exit);
        panel.addView(text("Difficulty",15,true));
        Spinner difficulty=spinner(new String[]{"Beginner — guidance","Normal — some traffic","Test mode — more traffic"}); panel.addView(difficulty);

        CheckBox markings=new CheckBox(this); markings.setText("Show lane markings, arrows and route"); markings.setChecked(true); panel.addView(markings);
        CheckBox traffic=new CheckBox(this); traffic.setText("Show other traffic"); traffic.setChecked(true); panel.addView(traffic);

        Button simulate=new Button(this); simulate.setText("START TRAINING DRIVE"); simulate.setTextSize(16); panel.addView(simulate);

        TextView stage=text("Ready • MIRRORS",18,true); stage.setPadding(0,14,0,5); panel.addView(stage);
        ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal); progress.setMax(100); panel.addView(progress,new LinearLayout.LayoutParams(-1,18));
        TextView score=text("Training score: —",15,true); score.setPadding(0,10,0,2); panel.addView(score);
        TextView advice=text("",14,false); advice.setPadding(0,10,0,8); panel.addView(advice);
        TextView tip=text("Training guide only. Follow real signs, signals and road markings. Give priority to traffic from the right unless directed otherwise.",12,false); panel.addView(tip);

        roadView.listener=(s,v)->{stage.setText(s);progress.setProgress(v);if(v>=100)score.setText(roadView.resultText());};

        Runnable update=()->{
            roadView.type=type.getSelectedItemPosition();
            roadView.approach=approach.getSelectedItemPosition();
            roadView.exit=exit.getSelectedItemPosition()+1;
            roadView.difficulty=difficulty.getSelectedItemPosition();
            roadView.showMarkings=markings.isChecked();
            roadView.showTraffic=traffic.isChecked();
            roadView.resetCar();
            stage.setText("Ready • MIRRORS"); progress.setProgress(0); score.setText("Training score: —");
            advice.setText(advice(roadView.exit,roadView.difficulty));
        };

        simulate.setOnClickListener(v->{update.run();roadView.startCar();});
        markings.setOnClickListener(v->update.run()); traffic.setOnClickListener(v->update.run());
        type.setOnItemSelectedListener(selection(update)); approach.setOnItemSelectedListener(selection(update)); exit.setOnItemSelectedListener(selection(update)); difficulty.setOnItemSelectedListener(selection(update));
        advice.setText(advice(1,0));
        setContentView(root);
    }

    private TextView text(String s,int size,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);if(bold)v.setTypeface(null,Typeface.BOLD);return v;}
    private Spinner spinner(String[] a){Spinner s=new Spinner(this);s.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,a));return s;}
    private AdapterView.OnItemSelectedListener selection(Runnable r){return new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?>p,View v,int x,long id){r.run();}public void onNothingSelected(AdapterView<?>p){}};}
    private String advice(int e,int d){
        String mode=d==0?"Beginner: route and prompts shown.":d==1?"Normal: watch circulating traffic before entering.":"Test mode: more traffic and less time to assess gaps.";
        if(e==1)return mode+"\nLEFT: mirrors • signal left • normally left lane • slow • look right • first exit.";
        if(e==2)return mode+"\nAHEAD: mirrors • usually no approach signal • follow lane arrows • slow • look right • signal left before leaving.";
        return mode+"\nRIGHT: mirrors • signal right • normally right lane • slow • look right • move outward safely • signal left before leaving.";
    }

    interface StageListener{void stage(String s,int p);}

    static class RoundaboutView extends View{
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG),routePaint=new Paint(Paint.ANTI_ALIAS_FLAG),glow=new Paint(Paint.ANTI_ALIAS_FLAG);
        int approach=0,exit=1,type=0,difficulty=0; boolean showMarkings=true,showTraffic=true; float carProgress=0,raw=0; ValueAnimator animator; Path route=new Path(); StageListener listener;
        RoundaboutView(Context c){super(c);routePaint.setColor(Color.rgb(255,205,0));routePaint.setStyle(Paint.Style.STROKE);routePaint.setStrokeWidth(8);routePaint.setStrokeCap(Paint.Cap.ROUND);glow.setColor(Color.argb(60,75,175,255));glow.setStyle(Paint.Style.STROKE);glow.setStrokeWidth(36);glow.setStrokeCap(Paint.Cap.ROUND);}
        void resetCar(){if(animator!=null)animator.cancel();raw=carProgress=0;invalidate();}
        void startCar(){if(getWidth()==0)return;if(animator!=null)animator.cancel();animator=ValueAnimator.ofFloat(0,1);animator.setDuration(difficulty==2?6200:7600);animator.setInterpolator(new LinearInterpolator());animator.addUpdateListener(a->{raw=(float)a.getAnimatedValue();if(raw<.22f)carProgress=raw/.22f*.18f;else if(raw<.34f)carProgress=.18f;else carProgress=.18f+(raw-.34f)/.66f*.82f;report();invalidate();});animator.start();}
        String resultText(){int score=100-(difficulty*3);return "Training score: "+score+"/100 • route complete";}
        void report(){if(listener==null)return;String s;if(raw<.1)s="1/6 • MIRRORS";else if(raw<.18)s=exit==1?"2/6 • SIGNAL LEFT":exit==3?"2/6 • SIGNAL RIGHT":"2/6 • SIGNAL normally none";else if(raw<.26)s="3/6 • POSITION";else if(raw<.34)s="4/6 • SPEED • GIVE WAY";else if(raw<.48)s="5/6 • LOOK RIGHT • choose a safe gap";else if(raw<.78)s="ON ROUNDABOUT • lane + observation";else if(raw<.96)s="6/6 • SIGNAL LEFT • EXIT";else s="COMPLETE • safe exit";listener.stage(s,Math.round(raw*100));}

        @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight(),cx=w/2,cy=h/2;float base=Math.min(w,h);float r=base*(type==1?.19f:type==2?.225f:.21f);float rw=r*(type==1?.78f:type==2?1.22f:1.02f);float lane=rw/(type==2?6f:4f);c.drawColor(Color.rgb(78,143,75));road(c,cx,cy,r,rw,w,h);if(showMarkings)markings(c,cx,cy,r,rw,w,h);if(showTraffic)traffic(c,cx,cy,r,rw);c.save();c.rotate(approach*90,cx,cy);route=route(cx,cy,r,rw,w,h,exit);if(difficulty<2)c.drawPath(route,glow);if(showMarkings){c.drawPath(route,routePaint);giveWay(c,cx,cy+r+rw*.32f,rw);arrows(c,cx,cy,r,rw,h);}car(c,route,carProgress,lane);c.restore();p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);p.setTextSize(Math.max(16,base*.028f));p.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("UK • CLOCKWISE",12,26,p);}

        void road(Canvas c,float cx,float cy,float r,float rw,float w,float h){p.setColor(Color.rgb(56,56,56));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(rw);c.drawCircle(cx,cy,r,p);p.setStyle(Paint.Style.FILL);c.drawRect(cx-rw/2,cy+r,cx+rw/2,h,p);c.drawRect(cx-rw/2,0,cx+rw/2,cy-r,p);c.drawRect(0,cy-rw/2,cx-r,cy+rw/2,p);c.drawRect(cx+r,cy-rw/2,w,cy+rw/2,p);p.setColor(Color.rgb(62,120,58));c.drawCircle(cx,cy,r-rw*.54f,p);}
        Path route(float cx,float cy,float r,float rw,float w,float h,int e){Path q=new Path();float ex=e==3?cx+rw*.23f:cx-rw*.23f,rr=e==3?r-rw*.20f:r+rw*.20f;q.moveTo(ex,h+10);q.lineTo(ex,cy+rr+rw*.28f);q.quadTo(ex,cy+rr,cx,cy+rr);q.arcTo(new RectF(cx-rr,cy-rr,cx+rr,cy+rr),90,e*90);float o=rw*.23f;if(e==1){q.quadTo(cx-rr,cy+o,cx-rr-rw*.2f,cy+o);q.lineTo(-10,cy+o);}else if(e==2){q.quadTo(cx-o,cy-rr,cx-o,cy-rr-rw*.2f);q.lineTo(cx-o,-10);}else{q.quadTo(cx+rr,cy-o,cx+rr+rw*.2f,cy-o);q.lineTo(w+10,cy-o);}return q;}
        void markings(Canvas c,float cx,float cy,float r,float rw,float w,float h){p.setStyle(Paint.Style.STROKE);p.setColor(Color.WHITE);p.setStrokeWidth(3.2f);p.setPathEffect(new DashPathEffect(new float[]{16,13},0));c.drawCircle(cx,cy,r,p);c.drawLine(cx,cy+r+5,cx,h,p);c.drawLine(cx,0,cx,cy-r-5,p);c.drawLine(0,cy,cx-r-5,cy,p);c.drawLine(cx+r+5,cy,w,cy,p);if(type==2){c.drawCircle(cx,cy,r-rw*.22f,p);c.drawCircle(cx,cy,r+rw*.22f,p);}p.setPathEffect(null);}
        void giveWay(Canvas c,float cx,float y,float rw){p.setColor(Color.WHITE);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);p.setPathEffect(new DashPathEffect(new float[]{13,8},0));c.drawLine(cx-rw*.47f,y,cx+rw*.47f,y,p);p.setPathEffect(null);}
        void arrows(Canvas c,float cx,float cy,float r,float rw,float h){p.setColor(Color.WHITE);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);float y=Math.min(h-65,cy+r+rw*.92f);arrow(c,cx-rw*.24f,y,true);arrow(c,cx+rw*.24f,y,false);}
        void arrow(Canvas c,float x,float y,boolean left){c.drawLine(x,y+22,x,y-22,p);c.drawLine(x,y-22,x-9,y-10,p);c.drawLine(x,y-22,x+9,y-10,p);float d=left?-20:20;c.drawLine(x,y,x+d,y,p);c.drawLine(x+d,y,x+d-(left?-9:9),y-9,p);c.drawLine(x+d,y,x+d-(left?-9:9),y+9,p);}
        void traffic(Canvas c,float cx,float cy,float r,float rw){int n=difficulty==0?1:difficulty==1?3:5;float rr=r;for(int i=0;i<n;i++){float a=(float)(Math.toRadians((raw*220+i*(360f/n))%360));float x=cx+(float)Math.cos(a)*rr,y=cy+(float)Math.sin(a)*rr;drawOtherCar(c,x,y,(float)Math.toDegrees(a)+90,rw*.18f,i);}}
        void drawOtherCar(Canvas c,float x,float y,float angle,float s,int i){c.save();c.translate(x,y);c.rotate(angle);p.setStyle(Paint.Style.FILL);p.setColor(i%2==0?Color.rgb(220,70,70):Color.rgb(70,70,210));c.drawRoundRect(-s*.55f,-s*.28f,s*.55f,s*.28f,6,6,p);c.restore();}
        void car(Canvas c,Path q,float f,float lane){PathMeasure pm=new PathMeasure(q,false);float[] pos=new float[2],tan=new float[2];if(!pm.getPosTan(pm.getLength()*f,pos,tan))return;float a=(float)Math.toDegrees(Math.atan2(tan[1],tan[0]));c.save();c.translate(pos[0],pos[1]);c.rotate(a);float l=Math.max(40,lane*.9f),ww=Math.max(23,lane*.5f);p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(35,105,210));c.drawRoundRect(-l/2,-ww/2,l/2,ww/2,9,9,p);p.setColor(Color.rgb(185,225,255));c.drawRoundRect(-l*.10f,-ww*.38f,l*.20f,ww*.38f,4,4,p);boolean blink=((System.currentTimeMillis()/340)%2)==0;int sig=signal();if(blink&&sig!=0){p.setColor(Color.rgb(255,170,0));float sx=l/2-4,sy=ww/2-4;if(sig<0)c.drawCircle(sx,-sy,5,p);else c.drawCircle(sx,sy,5,p);}c.restore();}
        int signal(){if(exit==1)return-1;if(exit==2)return raw>.72f?-1:0;return raw<.72f?1:-1;}
    }
}
