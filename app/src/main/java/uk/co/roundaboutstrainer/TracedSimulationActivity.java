package uk.co.roundaboutstrainer;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class TracedSimulationActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        FrameLayout root=new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        ImageView bg=new ImageView(this);
        bg.setImageResource(R.drawable.roundabout_master);
        bg.setScaleType(ImageView.ScaleType.FIT_CENTER);
        root.addView(bg,new FrameLayout.LayoutParams(-1,-1));
        root.addView(new TracedOverlay(this),new FrameLayout.LayoutParams(-1,-1));
        setContentView(root);
    }

    static final class TracedOverlay extends View {
        private static final float MASTER_ASPECT=1536f/1152f;
        private static final float SPLINE_TENSION=.78f;
        private static final float YIELD_PROGRESS=.285f;
        private static final float[] NPC_OFFSETS={.02f,.36f,.69f};
        private static final float[][] NPC_LOOP={
                {.343f,.633f},{.285f,.530f},{.245f,.405f},{.247f,.300f},
                {.325f,.215f},{.445f,.178f},{.565f,.178f},{.655f,.230f},
                {.690f,.325f},{.684f,.445f},{.646f,.555f},{.575f,.632f},
                {.470f,.670f},{.385f,.662f}
        };

        private final Paint routePaint=new Paint(Paint.ANTI_ALIAS_FLAG), shadowPaint=new Paint(Paint.ANTI_ALIAS_FLAG), companionRoutePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint carPaint=new Paint(Paint.ANTI_ALIAS_FLAG), carDarkPaint=new Paint(Paint.ANTI_ALIAS_FLAG), glassPaint=new Paint(Paint.ANTI_ALIAS_FLAG), tyrePaint=new Paint(Paint.ANTI_ALIAS_FLAG), lightPaint=new Paint(Paint.ANTI_ALIAS_FLAG), rearLightPaint=new Paint(Paint.ANTI_ALIAS_FLAG), indicatorPaint=new Paint(Paint.ANTI_ALIAS_FLAG), trimPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint companionPaint=new Paint(Paint.ANTI_ALIAS_FLAG), companionDarkPaint=new Paint(Paint.ANTI_ALIAS_FLAG), companionGlassPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint npcPaint=new Paint(Paint.ANTI_ALIAS_FLAG), npcGlassPaint=new Paint(Paint.ANTI_ALIAS_FLAG), npcTyrePaint=new Paint(Paint.ANTI_ALIAS_FLAG), brakePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint uiBlue=new Paint(Paint.ANTI_ALIAS_FLAG), uiWhite=new Paint(Paint.ANTI_ALIAS_FLAG), statusPaint=new Paint(Paint.ANTI_ALIAS_FLAG), statusTextPaint=new Paint(Paint.ANTI_ALIAS_FLAG), collisionPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path=new Path(), companionPath=new Path(), npcPath=new Path();
        private final PathMeasure pm=new PathMeasure(), companionPm=new PathMeasure(), npcPm=new PathMeasure();
        private final RectF imageRect=new RectF();
        private final float[] pos=new float[2],tan=new float[2],companionPos=new float[2],companionTan=new float[2],npcPos=new float[2],npcTan=new float[2],userCheck=new float[2];

        int exit=1;
        boolean showRoute=false, sideBySide=false, waitingAtGiveWay=false, passedGiveWay=false, collision=false;
        float progress=0f, trafficPhase=0f;
        ValueAnimator animator, trafficAnimator;

        TracedOverlay(Context c){super(c);setClickable(true);
            routePaint.setColor(Color.rgb(25,220,95));routePaint.setStyle(Paint.Style.STROKE);routePaint.setStrokeCap(Paint.Cap.ROUND);routePaint.setStrokeJoin(Paint.Join.ROUND);
            companionRoutePaint.setColor(Color.rgb(70,140,255));companionRoutePaint.setStyle(Paint.Style.STROKE);companionRoutePaint.setStrokeCap(Paint.Cap.ROUND);companionRoutePaint.setStrokeJoin(Paint.Join.ROUND);
            shadowPaint.setColor(Color.argb(110,0,0,0));shadowPaint.setStyle(Paint.Style.STROKE);shadowPaint.setStrokeCap(Paint.Cap.ROUND);
            carPaint.setColor(Color.rgb(210,45,45));carDarkPaint.setColor(Color.rgb(135,24,28));glassPaint.setColor(Color.rgb(105,155,180));tyrePaint.setColor(Color.rgb(22,22,22));lightPaint.setColor(Color.rgb(245,245,215));rearLightPaint.setColor(Color.rgb(220,28,35));indicatorPaint.setColor(Color.rgb(255,178,40));trimPaint.setColor(Color.rgb(45,45,45));
            companionPaint.setColor(Color.rgb(45,95,210));companionDarkPaint.setColor(Color.rgb(25,55,135));companionGlassPaint.setColor(Color.rgb(145,195,220));
            npcPaint.setColor(Color.rgb(90,105,120));npcGlassPaint.setColor(Color.rgb(165,205,225));npcTyrePaint.setColor(Color.rgb(25,25,25));brakePaint.setColor(Color.rgb(255,35,35));
            uiBlue.setColor(Color.rgb(20,120,230));uiWhite.setColor(Color.WHITE);uiWhite.setStyle(Paint.Style.STROKE);
            statusPaint.setColor(Color.argb(220,20,20,20));statusTextPaint.setColor(Color.WHITE);statusTextPaint.setTextAlign(Paint.Align.CENTER);statusTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
            collisionPaint.setColor(Color.rgb(230,40,40));collisionPaint.setStyle(Paint.Style.STROKE);
            startTraffic();
        }

        private void startTraffic(){
            trafficAnimator=ValueAnimator.ofFloat(0f,1f);
            trafficAnimator.setDuration(12000);
            trafficAnimator.setRepeatCount(ValueAnimator.INFINITE);
            trafficAnimator.setInterpolator(new LinearInterpolator());
            trafficAnimator.addUpdateListener(a->{trafficPhase=(float)a.getAnimatedValue();invalidate();});
            trafficAnimator.start();
        }

        void reset(){
            if(animator!=null)animator.cancel();
            progress=0f;waitingAtGiveWay=false;passedGiveWay=false;collision=false;invalidate();
        }

        void start(){
            if(!UserTracedRouteLibrary.hasUserTrace(exit))return;
            if(collision){reset();return;}
            if(waitingAtGiveWay){
                waitingAtGiveWay=false;passedGiveWay=true;
                animateFrom(progress,1f);
                return;
            }
            if(animator!=null&&animator.isRunning())return;
            passedGiveWay=false;collision=false;
            animateFrom(progress,1f);
        }

        private void animateFrom(float from,float to){
            if(animator!=null)animator.cancel();
            animator=ValueAnimator.ofFloat(from,to);
            animator.setDuration(Math.max(650L,(long)(durationForExit(exit)*(to-from))));
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(a->{
                float next=(float)a.getAnimatedValue();
                if(!passedGiveWay && next>=YIELD_PROGRESS){
                    progress=YIELD_PROGRESS;
                    waitingAtGiveWay=true;
                    animator.cancel();
                    invalidate();
                    return;
                }
                progress=next;
                if(passedGiveWay && progress>YIELD_PROGRESS+.015f && collidesWithTraffic(progress)){
                    collision=true;
                    animator.cancel();
                }
                invalidate();
            });
            animator.start();
        }

        private long durationForExit(int e){return e==1?4800:e==2?6200:e==3?7600:9000;}
        private void updateImageRect(){float vw=getWidth(),vh=getHeight();if(vw<=0||vh<=0){imageRect.setEmpty();return;}if(vw/vh>MASTER_ASPECT){float iw=vh*MASTER_ASPECT,left=(vw-iw)/2f;imageRect.set(left,0,left+iw,vh);}else{float ih=vw/MASTER_ASPECT,top=(vh-ih)/2f;imageRect.set(0,top,vw,top+ih);}}
        private float mapX(float n){return imageRect.left+n*imageRect.width();} private float mapY(float n){return imageRect.top+n*imageRect.height();}
        private float normX(float x){return(x-imageRect.left)/imageRect.width();} private float normY(float y){return(y-imageRect.top)/imageRect.height();}

        private void buildSpline(Path target,float[][] p){
            target.reset();
            if(p.length<2||imageRect.isEmpty())return;
            target.moveTo(mapX(p[0][0]),mapY(p[0][1]));
            for(int i=0;i<p.length-1;i++){
                float[] p0=p[Math.max(0,i-1)],p1=p[i],p2=p[i+1],p3=p[Math.min(p.length-1,i+2)];
                float x1=mapX(p1[0]),y1=mapY(p1[1]),x2=mapX(p2[0]),y2=mapY(p2[1]);
                float c1x=x1+(mapX(p2[0])-mapX(p0[0]))*(SPLINE_TENSION/6f);
                float c1y=y1+(mapY(p2[1])-mapY(p0[1]))*(SPLINE_TENSION/6f);
                float c2x=x2-(mapX(p3[0])-mapX(p1[0]))*(SPLINE_TENSION/6f);
                float c2y=y2-(mapY(p3[1])-mapY(p1[1]))*(SPLINE_TENSION/6f);
                target.cubicTo(c1x,c1y,c2x,c2y,x2,y2);
            }
        }

        private void buildPath(){updateImageRect();buildSpline(path,UserTracedRouteLibrary.pointsForExit(exit));}
        private int companionExit(){return exit<=2?3:1;}
        private void buildCompanionPath(){buildSpline(companionPath,UserTracedRouteLibrary.pointsForExit(companionExit()));}

        private void buildNpcPath(){
            npcPath.reset();if(imageRect.isEmpty())return;int n=NPC_LOOP.length;
            npcPath.moveTo(mapX(NPC_LOOP[0][0]),mapY(NPC_LOOP[0][1]));
            for(int i=0;i<n;i++){
                float[] p0=NPC_LOOP[(i-1+n)%n],p1=NPC_LOOP[i],p2=NPC_LOOP[(i+1)%n],p3=NPC_LOOP[(i+2)%n];
                float x1=mapX(p1[0]),y1=mapY(p1[1]),x2=mapX(p2[0]),y2=mapY(p2[1]);
                float c1x=x1+(mapX(p2[0])-mapX(p0[0]))*(SPLINE_TENSION/6f);
                float c1y=y1+(mapY(p2[1])-mapY(p0[1]))*(SPLINE_TENSION/6f);
                float c2x=x2-(mapX(p3[0])-mapX(p1[0]))*(SPLINE_TENSION/6f);
                float c2y=y2-(mapY(p3[1])-mapY(p1[1]))*(SPLINE_TENSION/6f);
                npcPath.cubicTo(c1x,c1y,c2x,c2y,x2,y2);
            }
            npcPath.close();
        }

        private float npcPhase(int i){float p=trafficPhase+NPC_OFFSETS[i];return p-(float)Math.floor(p);}

        private boolean trafficConflictAtEntry(){
            buildPath();buildNpcPath();pm.setPath(path,false);npcPm.setPath(npcPath,false);
            float ul=pm.getLength(),nl=npcPm.getLength();if(ul<=0||nl<=0)return false;
            pm.getPosTan(ul*YIELD_PROGRESS,userCheck,null);
            float s=Math.min(imageRect.width(),imageRect.height()),limit=s*.115f;
            for(int i=0;i<NPC_OFFSETS.length;i++){
                npcPm.getPosTan(nl*npcPhase(i),npcPos,null);
                if((float)Math.hypot(npcPos[0]-userCheck[0],npcPos[1]-userCheck[1])<limit)return true;
            }
            return false;
        }

        private boolean collidesWithTraffic(float userProgress){
            buildPath();buildNpcPath();pm.setPath(path,false);npcPm.setPath(npcPath,false);
            float ul=pm.getLength(),nl=npcPm.getLength();if(ul<=0||nl<=0)return false;
            pm.getPosTan(ul*userProgress,userCheck,null);
            float s=Math.min(imageRect.width(),imageRect.height()),limit=s*.043f;
            for(int i=0;i<NPC_OFFSETS.length;i++){
                npcPm.getPosTan(nl*npcPhase(i),npcPos,null);
                if((float)Math.hypot(npcPos[0]-userCheck[0],npcPos[1]-userCheck[1])<limit)return true;
            }
            return false;
        }

        @Override protected void onDraw(Canvas c){
            super.onDraw(c);if(getWidth()==0||getHeight()==0)return;
            buildPath();buildCompanionPath();buildNpcPath();float s=Math.min(imageRect.width(),imageRect.height());
            if(showRoute){
                shadowPaint.setStrokeWidth(s*.016f);routePaint.setStrokeWidth(s*.007f);c.drawPath(path,shadowPaint);c.drawPath(path,routePaint);
                if(sideBySide){companionRoutePaint.setStrokeWidth(s*.006f);c.drawPath(companionPath,companionRoutePaint);}
            }
            drawTraffic(c,s);drawSelectedExit(c,s);drawShowRouteState(c,s);drawSideBySideState(c,s);drawStatus(c,s);drawCar(c,s);if(sideBySide)drawCompanionCar(c,s);
        }

        private void drawTraffic(Canvas c,float s){
            npcPm.setPath(npcPath,false);float len=npcPm.getLength();if(len<=0)return;
            for(int i=0;i<NPC_OFFSETS.length;i++){
                npcPm.getPosTan(len*npcPhase(i),npcPos,npcTan);
                float angle=(float)Math.toDegrees(Math.atan2(npcTan[1],npcTan[0]))+90f;
                c.save();c.translate(npcPos[0],npcPos[1]);c.rotate(angle);
                float cw=s*.030f,ch=cw*1.72f;
                c.drawRoundRect(new RectF(-cw*.58f,-ch*.28f,-cw*.45f,ch*.30f),cw*.05f,cw*.05f,npcTyrePaint);
                c.drawRoundRect(new RectF(cw*.45f,-ch*.28f,cw*.58f,ch*.30f),cw*.05f,cw*.05f,npcTyrePaint);
                c.drawRoundRect(new RectF(-cw/2,-ch/2,cw/2,ch/2),cw*.18f,cw*.18f,npcPaint);
                c.drawRoundRect(new RectF(-cw*.32f,-ch*.18f,cw*.32f,ch*.08f),cw*.07f,cw*.07f,npcGlassPaint);
                c.drawCircle(-cw*.30f,ch*.42f,cw*.055f,brakePaint);c.drawCircle(cw*.30f,ch*.42f,cw*.055f,brakePaint);
                c.restore();
            }
        }

        private void drawStatus(Canvas c,float s){
            if(!waitingAtGiveWay&&!collision&& !sideBySide)return;
            String text;
            int col;
            if(collision){text="COLLISION - TAP START TO RESET";col=Color.argb(230,150,20,20);}
            else if(waitingAtGiveWay){boolean conflict=trafficConflictAtEntry();text=conflict?"GIVE WAY - TRAFFIC COMING":"CLEAR - TAP START TO ENTER";col=conflict?Color.argb(225,150,85,10):Color.argb(225,20,115,55);}
            else {text="2-CAR MODE: YOUR EXIT "+exit+" / BLUE CAR EXIT "+companionExit();col=Color.argb(225,20,85,150);}
            float cx=mapX(.50f),cy=mapY(.055f),w=s*(collision?.31f:.31f),h=s*.038f;
            statusPaint.setColor(col);c.drawRoundRect(new RectF(cx-w/2,cy-h/2,cx+w/2,cy+h/2),h*.45f,h*.45f,statusPaint);
            statusTextPaint.setTextSize(s*.017f);c.drawText(text,cx,cy+s*.006f,statusTextPaint);
        }

        private void drawSelectedExit(Canvas c,float s){float[] ys={.115f,.149f,.182f,.215f};c.drawCircle(mapX(.0247f),mapY(ys[Math.max(0,Math.min(3,exit-1))]),s*.006f,uiBlue);}
        private void drawShowRouteState(Canvas c,float s){c.drawCircle(mapX(showRoute?.888f:.874f),mapY(.0335f),s*.009f,showRoute?uiBlue:uiWhite);}
        private void drawSideBySideState(Canvas c,float s){c.drawCircle(mapX(sideBySide?.888f:.874f),mapY(.077f),s*.009f,sideBySide?uiBlue:uiWhite);}

        @Override public boolean onTouchEvent(MotionEvent e){
            if(e.getAction()!=MotionEvent.ACTION_DOWN)return true;
            updateImageRect();if(imageRect.isEmpty()||!imageRect.contains(e.getX(),e.getY()))return true;
            float x=normX(e.getX()),y=normY(e.getY());
            if(x>=.010f&&x<=.185f){
                if(y>=.098f&&y<.132f){selectExit(1);return true;}
                if(y<.166f&&y>=.132f){selectExit(2);return true;}
                if(y<.199f&&y>=.166f){selectExit(3);return true;}
                if(y<=.235f&&y>=.199f){selectExit(4);return true;}
            }
            if(x>=.748f&&x<=.915f&&y>=.012f&&y<=.055f){showRoute=!showRoute;invalidate();return true;}
            // Re-use the baked Show Lane Arrows control as the two-car training toggle.
            if(x>=.748f&&x<=.915f&&y>=.055f&&y<=.100f){sideBySide=!sideBySide;reset();invalidate();return true;}
            if(x>=.730f&&x<=.930f&&y>=.120f&&y<=.210f){start();return true;}
            return true;
        }

        private void selectExit(int e){exit=e;reset();}
        private boolean rightIndicatorOn(int e,float p){return (e==3||e==4)&&p<.60f;}
        private boolean leftIndicatorOn(int e,float p){if(e==1)return p<.62f;if(e==2)return p>.68f;if(e==3)return p>.67f;return p>.75f;}

        private void drawCompanionCar(Canvas c,float s){
            companionPm.setPath(companionPath,false);float len=companionPm.getLength();if(len<=0)return;
            float cp=Math.max(0f,Math.min(1f,progress));
            companionPm.getPosTan(len*cp,companionPos,companionTan);
            float angle=(float)Math.toDegrees(Math.atan2(companionTan[1],companionTan[0]))+90f;
            drawSimpleCar(c,s,companionPos[0],companionPos[1],angle,companionPaint,companionDarkPaint,companionGlassPaint,companionExit(),cp,false);
        }

        private void drawSimpleCar(Canvas c,float s,float x,float y,float angle,Paint bodyPaint,Paint darkPaint,Paint windowPaint,int carExit,float p,boolean user){
            c.save();c.translate(x,y);c.rotate(angle);
            float cw=s*(user?.0416f:.0385f),ch=cw*1.72f;
            c.drawRoundRect(new RectF(-cw*.61f,-ch*.30f,-cw*.45f,-ch*.03f),cw*.06f,cw*.06f,tyrePaint);
            c.drawRoundRect(new RectF(cw*.45f,-ch*.30f,cw*.61f,-ch*.03f),cw*.06f,cw*.06f,tyrePaint);
            c.drawRoundRect(new RectF(-cw*.61f,ch*.08f,-cw*.45f,ch*.34f),cw*.06f,cw*.06f,tyrePaint);
            c.drawRoundRect(new RectF(cw*.45f,ch*.08f,cw*.61f,ch*.34f),cw*.06f,cw*.06f,tyrePaint);
            Path body=new Path();body.moveTo(0,-ch*.52f);body.cubicTo(-cw*.36f,-ch*.50f,-cw*.52f,-ch*.38f,-cw*.54f,-ch*.20f);body.lineTo(-cw*.52f,ch*.27f);body.cubicTo(-cw*.47f,ch*.45f,-cw*.28f,ch*.50f,0,ch*.51f);body.cubicTo(cw*.28f,ch*.50f,cw*.47f,ch*.45f,cw*.52f,ch*.27f);body.lineTo(cw*.54f,-ch*.20f);body.cubicTo(cw*.52f,-ch*.38f,cw*.36f,-ch*.50f,0,-ch*.52f);body.close();c.drawPath(body,bodyPaint);
            c.drawRoundRect(new RectF(-cw*.42f,ch*.37f,cw*.42f,ch*.49f),cw*.08f,cw*.08f,darkPaint);
            c.drawRoundRect(new RectF(-cw*.34f,-ch*.24f,cw*.34f,ch*.22f),cw*.14f,cw*.14f,darkPaint);
            c.drawRoundRect(new RectF(-cw*.28f,-ch*.18f,cw*.28f,ch*.16f),cw*.08f,cw*.08f,windowPaint);
            c.drawRoundRect(new RectF(-cw*.37f,-ch*.455f,-cw*.08f,-ch*.415f),cw*.03f,cw*.03f,lightPaint);c.drawRoundRect(new RectF(cw*.08f,-ch*.455f,cw*.37f,-ch*.415f),cw*.03f,cw*.03f,lightPaint);
            boolean blink=((int)(p*34))%2==0;
            if(blink&&leftIndicatorOn(carExit,p))c.drawCircle(-cw*.43f,-ch*.39f,cw*.08f,indicatorPaint);
            if(blink&&rightIndicatorOn(carExit,p))c.drawCircle(cw*.43f,-ch*.39f,cw*.08f,indicatorPaint);
            if(user&&waitingAtGiveWay){c.drawCircle(-cw*.28f,ch*.43f,cw*.08f,brakePaint);c.drawCircle(cw*.28f,ch*.43f,cw*.08f,brakePaint);}
            if(user&&collision){collisionPaint.setStrokeWidth(s*.006f);c.drawCircle(0,0,cw*.85f,collisionPaint);}
            c.restore();
        }

        private void drawCar(Canvas c,float s){
            pm.setPath(path,false);float len=pm.getLength();if(len<=0)return;
            pm.getPosTan(len*Math.max(0f,Math.min(1f,progress)),pos,tan);
            float angle=(float)Math.toDegrees(Math.atan2(tan[1],tan[0]))+90f;
            drawSimpleCar(c,s,pos[0],pos[1],angle,carPaint,carDarkPaint,glassPaint,exit,progress,true);
        }
    }
}
