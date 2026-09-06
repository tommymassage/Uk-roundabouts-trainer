package uk.co.roundaboutstrainer;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
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
        private final Paint routePaint=new Paint(Paint.ANTI_ALIAS_FLAG), shadowPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint carPaint=new Paint(Paint.ANTI_ALIAS_FLAG), glassPaint=new Paint(Paint.ANTI_ALIAS_FLAG), tyrePaint=new Paint(Paint.ANTI_ALIAS_FLAG), lightPaint=new Paint(Paint.ANTI_ALIAS_FLAG), indicatorPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint uiBlue=new Paint(Paint.ANTI_ALIAS_FLAG), uiWhite=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path=new Path(); private final PathMeasure pm=new PathMeasure(); private final RectF imageRect=new RectF();
        private final float[] pos=new float[2],tan=new float[2];
        int exit=1; boolean showRoute=false; float progress=0f; ValueAnimator animator;

        TracedOverlay(Context c){super(c);setClickable(true);
            routePaint.setColor(Color.rgb(25,220,95));routePaint.setStyle(Paint.Style.STROKE);routePaint.setStrokeCap(Paint.Cap.ROUND);routePaint.setStrokeJoin(Paint.Join.ROUND);
            shadowPaint.setColor(Color.argb(110,0,0,0));shadowPaint.setStyle(Paint.Style.STROKE);shadowPaint.setStrokeCap(Paint.Cap.ROUND);
            carPaint.setColor(Color.rgb(40,92,155));glassPaint.setColor(Color.rgb(170,215,235));tyrePaint.setColor(Color.rgb(25,25,25));lightPaint.setColor(Color.rgb(245,245,210));indicatorPaint.setColor(Color.rgb(255,178,40));
            uiBlue.setColor(Color.rgb(20,120,230));uiWhite.setColor(Color.WHITE);uiWhite.setStyle(Paint.Style.STROKE);
        }
        void reset(){if(animator!=null)animator.cancel();progress=0f;invalidate();}
        void start(){if(!UserTracedRouteLibrary.hasUserTrace(exit))return;if(animator!=null)animator.cancel();animator=ValueAnimator.ofFloat(0f,1f);animator.setDuration(durationForExit(exit));animator.setInterpolator(new AccelerateDecelerateInterpolator());animator.addUpdateListener(a->{progress=(float)a.getAnimatedValue();invalidate();});animator.start();}
        private long durationForExit(int e){return e==1?4800:e==2?6200:e==3?7600:9000;}
        private void updateImageRect(){float vw=getWidth(),vh=getHeight();if(vw<=0||vh<=0){imageRect.setEmpty();return;}if(vw/vh>MASTER_ASPECT){float iw=vh*MASTER_ASPECT,left=(vw-iw)/2f;imageRect.set(left,0,left+iw,vh);}else{float ih=vw/MASTER_ASPECT,top=(vh-ih)/2f;imageRect.set(0,top,vw,top+ih);}}
        private float mapX(float n){return imageRect.left+n*imageRect.width();} private float mapY(float n){return imageRect.top+n*imageRect.height();}
        private float normX(float x){return(x-imageRect.left)/imageRect.width();} private float normY(float y){return(y-imageRect.top)/imageRect.height();}

        private void buildPath(){
            updateImageRect();
            float[][] p=UserTracedRouteLibrary.pointsForExit(exit);
            path.reset();
            if(p.length<2||imageRect.isEmpty())return;

            path.moveTo(mapX(p[0][0]),mapY(p[0][1]));
            for(int i=0;i<p.length-1;i++){
                float[] p0=p[Math.max(0,i-1)];
                float[] p1=p[i];
                float[] p2=p[i+1];
                float[] p3=p[Math.min(p.length-1,i+2)];

                float x1=mapX(p1[0]), y1=mapY(p1[1]);
                float x2=mapX(p2[0]), y2=mapY(p2[1]);
                float c1x=x1+(mapX(p2[0])-mapX(p0[0]))*(SPLINE_TENSION/6f);
                float c1y=y1+(mapY(p2[1])-mapY(p0[1]))*(SPLINE_TENSION/6f);
                float c2x=x2-(mapX(p3[0])-mapX(p1[0]))*(SPLINE_TENSION/6f);
                float c2y=y2-(mapY(p3[1])-mapY(p1[1]))*(SPLINE_TENSION/6f);
                path.cubicTo(c1x,c1y,c2x,c2y,x2,y2);
            }
        }

        @Override protected void onDraw(Canvas c){super.onDraw(c);if(getWidth()==0||getHeight()==0)return;buildPath();float s=Math.min(imageRect.width(),imageRect.height());if(showRoute){shadowPaint.setStrokeWidth(s*.016f);routePaint.setStrokeWidth(s*.007f);c.drawPath(path,shadowPaint);c.drawPath(path,routePaint);}drawSelectedExit(c,s);drawShowRouteState(c,s);drawCar(c,s);}
        private void drawSelectedExit(Canvas c,float s){float[] ys={.115f,.149f,.182f,.215f};c.drawCircle(mapX(.0247f),mapY(ys[Math.max(0,Math.min(3,exit-1))]),s*.006f,uiBlue);}
        private void drawShowRouteState(Canvas c,float s){c.drawCircle(mapX(showRoute?.888f:.874f),mapY(.0335f),s*.009f,showRoute?uiBlue:uiWhite);}
        @Override public boolean onTouchEvent(MotionEvent e){
            if(e.getAction()!=MotionEvent.ACTION_DOWN)return true;
            updateImageRect();
            if(imageRect.isEmpty()||!imageRect.contains(e.getX(),e.getY()))return true;
            float x=normX(e.getX()),y=normY(e.getY());
            if(x>=.010f&&x<=.185f){
                if(y>=.098f&&y<.132f){selectExit(1);return true;}
                if(y<.166f&&y>=.132f){selectExit(2);return true;}
                if(y<.199f&&y>=.166f){selectExit(3);return true;}
                if(y<=.235f&&y>=.199f){selectExit(4);return true;}
            }
            if(x>=.748f&&x<=.915f&&y>=.012f&&y<=.055f){showRoute=!showRoute;invalidate();return true;}
            // Larger touch target and immediate ACTION_DOWN response for the baked START button.
            if(x>=.730f&&x<=.930f&&y>=.120f&&y<=.210f){start();return true;}
            return true;
        }
        private void selectExit(int e){exit=e;reset();}
        private boolean rightIndicatorOn(){return (exit==3||exit==4)&&progress<.60f;}
        private boolean leftIndicatorOn(){if(exit==1)return progress<.62f;if(exit==2)return progress>.68f;if(exit==3)return progress>.67f;return progress>.75f;}
        private void drawCar(Canvas c,float s){pm.setPath(path,false);float len=pm.getLength();if(len<=0)return;pm.getPosTan(len*Math.max(0f,Math.min(1f,progress)),pos,tan);float angle=(float)Math.toDegrees(Math.atan2(tan[1],tan[0]))+90f;c.save();c.translate(pos[0],pos[1]);c.rotate(angle);float cw=s*.032f,ch=cw*1.82f;
            c.drawRoundRect(new RectF(-cw*.58f,-ch*.31f,-cw*.43f,ch*.30f),cw*.07f,cw*.07f,tyrePaint);c.drawRoundRect(new RectF(cw*.43f,-ch*.31f,cw*.58f,ch*.30f),cw*.07f,cw*.07f,tyrePaint);
            c.drawRoundRect(new RectF(-cw/2,-ch/2,cw/2,ch/2),cw*.20f,cw*.20f,carPaint);c.drawRoundRect(new RectF(-cw*.34f,-ch*.18f,cw*.34f,ch*.08f),cw*.08f,cw*.08f,glassPaint);
            c.drawCircle(-cw*.28f,-ch*.43f,cw*.07f,lightPaint);c.drawCircle(cw*.28f,-ch*.43f,cw*.07f,lightPaint);
            boolean blink=((int)(progress*34))%2==0;if(blink&&leftIndicatorOn())c.drawCircle(-cw*.43f,-ch*.39f,cw*.095f,indicatorPaint);if(blink&&rightIndicatorOn())c.drawCircle(cw*.43f,-ch*.39f,cw*.095f,indicatorPaint);c.restore();}
    }
}
