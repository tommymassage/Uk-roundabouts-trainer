package uk.co.roundaboutstrainer;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.*;
import android.view.animation.LinearInterpolator;
import android.widget.*;
import android.content.Context;

public class MainActivity extends Activity {
    private RoundaboutView roadView;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.HORIZONTAL); root.setPadding(14,14,14,14); root.setBackgroundColor(Color.rgb(238,245,238));
        roadView=new RoundaboutView(this); root.addView(roadView,new LinearLayout.LayoutParams(0,-1,3));
        ScrollView scroll=new ScrollView(this); LinearLayout panel=new LinearLayout(this); panel.setOrientation(LinearLayout.VERTICAL); panel.setPadding(24,8,18,14); scroll.addView(panel); root.addView(scroll,new LinearLayout.LayoutParams(0,-1,2));
        TextView title=text("UK ROUNDABOUTS TRAINER  v0.3",24,true); panel.addView(title);
        TextView info=text("Full training sequence: Mirrors • Signal • Position • Speed • Look",15,false); info.setPadding(0,10,0,14); panel.addView(info);
        panel.addView(text("Approach road",16,true)); Spinner approach=spinner(new String[]{"South","West","North","East"}); panel.addView(approach);
        panel.addView(text("Exit",16,true)); Spinner exit=spinner(new String[]{"1st exit / left","2nd exit / ahead","3rd exit / right"}); panel.addView(exit);
        CheckBox markings=new CheckBox(this); markings.setText("Show markings, arrows and training route"); markings.setChecked(true); panel.addView(markings);
        Button simulate=new Button(this); simulate.setText("START FULL DRIVING DEMO"); panel.addView(simulate);
        TextView stage=text("Ready • MIRRORS",18,true); stage.setPadding(0,16,0,6); panel.addView(stage);
        ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal); progress.setMax(100); panel.addView(progress,new LinearLayout.LayoutParams(-1,18));
        TextView advice=text("",15,false); advice.setPadding(0,14,0,8); panel.addView(advice);
        TextView features=text("Demo: slows and pauses at GIVE WAY • flashes the correct indicator • changes to LEFT before exit • highlights suggested route/lane.",14,false); features.setPadding(0,10,0,8); panel.addView(features);
        TextView tip=text("Training guide only. Always follow signs, traffic lights, road markings and local lane arrows. Give priority to traffic from the right unless directed otherwise.",13,false); panel.addView(tip);
        roadView.listener=(s,v)->{stage.setText(s);progress.setProgress(v);};
        Runnable update=()->{roadView.approach=approach.getSelectedItemPosition();roadView.exit=exit.getSelectedItemPosition()+1;roadView.showMarkings=markings.isChecked();roadView.resetCar();advice.setText(advice(roadView.exit));stage.setText("Ready • MIRRORS");progress.setProgress(0);};
        simulate.setOnClickListener(v->{update.run();roadView.startCar();}); markings.setOnClickListener(v->update.run()); approach.setOnItemSelectedListener(selection(update)); exit.setOnItemSelectedListener(selection(update)); advice.setText(advice(1)); setContentView(root);
    }
    private TextView text(String s,int size,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);if(bold)v.setTypeface(null,Typeface.BOLD);return v;}
    private Spinner spinner(String[] a){Spinner s=new Spinner(this);s.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,a));return s;}
    private AdapterView.OnItemSelectedListener selection(Runnable r){return new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?>p,View v,int x,long id){r.run();}public void onNothingSelected(AdapterView<?>p){}};}
    private String advice(int e){if(e==1)return "1st exit / LEFT\n1 MIRRORS before slowing\n2 SIGNAL left on approach\n3 POSITION normally left lane unless markings say otherwise\n4 SPEED down and prepare to stop\n5 LOOK right; enter when safe and leave at first exit";if(e==2)return "2nd exit / AHEAD\n1 MIRRORS first\n2 SIGNAL usually none on approach\n3 POSITION follow lane arrows; normally left if unmarked\n4 SPEED down and assess traffic\n5 LOOK right; signal LEFT after passing the exit before yours";return "3rd exit / RIGHT\n1 MIRRORS first\n2 SIGNAL right on approach\n3 POSITION normally right lane unless markings say otherwise\n4 SPEED down and prepare to stop\n5 LOOK right; use inner lane, then move outward safely and signal LEFT before leaving";}
    interface StageListener{void stage(String s,int p);}

    static class RoundaboutView extends View{
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG), routePaint=new Paint(Paint.ANTI_ALIAS_FLAG), glow=new Paint(Paint.ANTI_ALIAS_FLAG); int approach=0,exit=1; boolean showMarkings=true; float carProgress=0,raw=0; ValueAnimator animator; Path route=new Path(); StageListener listener;
        RoundaboutView(Context c){super(c);routePaint.setColor(Color.rgb(255,205,0));routePaint.setStyle(Paint.Style.STROKE);routePaint.setStrokeWidth(8);routePaint.setStrokeCap(Paint.Cap.ROUND);glow.setColor(Color.argb(65,70,170,255));glow.setStyle(Paint.Style.STROKE);glow.setStrokeWidth(38);glow.setStrokeCap(Paint.Cap.ROUND);}
        void resetCar(){if(animator!=null)animator.cancel();carProgress=raw=0;invalidate();}
        void startCar(){if(getWidth()==0)return;if(animator!=null)animator.cancel();animator=ValueAnimator.ofFloat(0,1);animator.setDuration(7600);animator.setInterpolator(new LinearInterpolator());animator.addUpdateListener(a->{raw=(float)a.getAnimatedValue();if(raw<.24f)carProgress=raw/.24f*.19f;else if(raw<.37f)carProgress=.19f;else carProgress=.19f+(raw-.37f)/.63f*.81f;report();invalidate();});animator.start();}
        void report(){if(listener==null)return;String s;if(raw<.1)s="1/5 • MIRRORS — check before slowing";else if(raw<.2)s=exit==1?"2/5 • SIGNAL — LEFT":exit==3?"2/5 • SIGNAL — RIGHT":"2/5 • SIGNAL — normally none";else if(raw<.28)s="3/5 • POSITION — correct lane";else if(raw<.37)s="4/5 • SPEED — pause at GIVE WAY";else if(raw<.48)s="5/5 • LOOK RIGHT — enter when safe";else if(raw<.78)s="ON ROUNDABOUT • observe and hold lane";else if(raw<.96)s="EXIT • SIGNAL LEFT + mirrors";else s="COMPLETE • safe exit";listener.stage(s,Math.round(raw*100));}
        @Override protected void onDraw(Canvas c){float w=getWidth(),h=getHeight(),cx=w/2,cy=h/2,r=Math.min(w,h)*.215f,rw=r*1.02f,lane=rw/4;c.drawColor(Color.rgb(77,142,74));road(c,cx,cy,r,rw,w,h);if(showMarkings)markings(c,cx,cy,r,rw,w,h);c.save();c.rotate(approach*90,cx,cy);route=route(cx,cy,r,rw,w,h,exit);c.drawPath(route,glow);if(showMarkings){c.drawPath(route,routePaint);giveWay(c,cx,cy+r+rw*.32f,rw);arrows(c,cx,cy,r,rw,h);}car(c,route,carProgress,lane);c.restore();p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);p.setTextSize(21);p.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("UK • CLOCKWISE",14,29,p);}
        void road(Canvas c,float cx,float cy,float r,float rw,float w,float h){p.setColor(Color.rgb(57,57,57));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(rw);c.drawCircle(cx,cy,r,p);p.setStyle(Paint.Style.FILL);c.drawRect(cx-rw/2,cy+r,cx+rw/2,h,p);c.drawRect(cx-rw/2,0,cx+rw/2,cy-r,p);c.drawRect(0,cy-rw/2,cx-r,cy+rw/2,p);c.drawRect(cx+r,cy-rw/2,w,cy+rw/2,p);p.setColor(Color.rgb(63,120,59));c.drawCircle(cx,cy,r-rw*.54f,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(6);p.setColor(Color.rgb(92,155,84));c.drawCircle(cx,cy,r-rw*.54f,p);}
        Path route(float cx,float cy,float r,float rw,float w,float h,int e){Path q=new Path();float ex=e==3?cx+rw*.23f:cx-rw*.23f,rr=e==3?r-rw*.20f:r+rw*.20f;q.moveTo(ex,h+10);q.lineTo(ex,cy+rr+rw*.28f);q.quadTo(ex,cy+rr,cx,cy+rr);q.arcTo(new RectF(cx-rr,cy-rr,cx+rr,cy+rr),90,e*90);float o=rw*.23f;if(e==1){q.quadTo(cx-rr,cy+o,cx-rr-rw*.2f,cy+o);q.lineTo(-10,cy+o);}else if(e==2){q.quadTo(cx-o,cy-rr,cx-o,cy-rr-rw*.2f);q.lineTo(cx-o,-10);}else{q.quadTo(cx+rr,cy-o,cx+rr+rw*.2f,cy-o);q.lineTo(w+10,cy-o);}return q;}
        void markings(Canvas c,float cx,float cy,float r,float rw,float w,float h){p.setStyle(Paint.Style.STROKE);p.setColor(Color.WHITE);p.setStrokeWidth(3.5f);p.setPathEffect(new DashPathEffect(new float[]{17,14},0));c.drawCircle(cx,cy,r,p);c.drawLine(cx,cy+r+5,cx,h,p);c.drawLine(cx,0,cx,cy-r-5,p);c.drawLine(0,cy,cx-r-5,cy,p);c.drawLine(cx+r+5,cy,w,cy,p);p.setPathEffect(null);}
        void giveWay(Canvas c,float cx,float y,float rw){p.setColor(Color.WHITE);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);p.setPathEffect(new DashPathEffect(new float[]{13,8},0));c.drawLine(cx-rw*.47f,y,cx+rw*.47f,y,p);p.setPathEffect(null);p.setStyle(Paint.Style.FILL);for(int i=-1;i<=1;i+=2){float x=cx+i*rw*.23f;Path t=new Path();t.moveTo(x,y+20);t.lineTo(x-11,y+40);t.lineTo(x+11,y+40);t.close();c.drawPath(t,p);}}
        void arrows(Canvas c,float cx,float cy,float r,float rw,float h){p.setColor(Color.WHITE);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);float y=Math.min(h-70,cy+r+rw*.95f);arrow(c,cx-rw*.24f,y,true);arrow(c,cx+rw*.24f,y,false);}
        void arrow(Canvas c,float x,float y,boolean left){c.drawLine(x,y+24,x,y-24,p);c.drawLine(x,y-24,x-10,y-10,p);c.drawLine(x,y-24,x+10,y-10,p);float d=left?-22:22;c.drawLine(x,y,x+d,y,p);c.drawLine(x+d,y,x+d-(left?-10:10),y-10,p);c.drawLine(x+d,y,x+d-(left?-10:10),y+10,p);}
        void car(Canvas c,Path q,float f,float lane){PathMeasure pm=new PathMeasure(q,false);float[] pos=new float[2],tan=new float[2];if(!pm.getPosTan(pm.getLength()*f,pos,tan))return;float a=(float)Math.toDegrees(Math.atan2(tan[1],tan[0]));c.save();c.translate(pos[0],pos[1]);c.rotate(a);float l=Math.max(42,lane*.82f),ww=Math.max(24,lane*.46f);p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(35,105,210));c.drawRoundRect(-l/2,-ww/2,l/2,ww/2,9,9,p);p.setColor(Color.rgb(180,225,255));c.drawRoundRect(-l*.1f,-ww*.38f,l*.2f,ww*.38f,4,4,p);p.setColor(Color.rgb(25,25,25));c.drawRect(-l*.3f,-ww*.57f,-l*.08f,-ww*.48f,p);c.drawRect(-l*.3f,ww*.48f,-l*.08f,ww*.57f,p);c.drawRect(l*.12f,-ww*.57f,l*.34f,-ww*.48f,p);c.drawRect(l*.12f,ww*.48f,l*.34f,ww*.57f,p);boolean blink=((System.currentTimeMillis()/350)%2)==0;int sig=signal();if(blink&&sig!=0){p.setColor(Color.rgb(255,170,0));float sx=l/2-4,sy=ww/2-4;if(sig<0)c.drawCircle(sx,-sy,5,p);else c.drawCircle(sx,sy,5,p);}p.setColor(Color.WHITE);c.drawCircle(l/2-4,-ww*.22f,3.5f,p);c.drawCircle(l/2-4,ww*.22f,3.5f,p);c.restore();}
        int signal(){if(exit==1)return-1;if(exit==2)return raw>.72f?-1:0;return raw<.72f?1:-1;}
    }
}
