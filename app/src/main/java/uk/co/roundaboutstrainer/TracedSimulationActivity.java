package uk.co.roundaboutstrainer;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class TracedSimulationActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        ImageView bg = new ImageView(this);
        bg.setImageResource(R.drawable.roundabout_master);
        bg.setScaleType(ImageView.ScaleType.FIT_CENTER);
        root.addView(bg, new FrameLayout.LayoutParams(-1,-1));

        // All interaction is now aligned directly to the controls baked into MASTER.
        // No extra Android panel is drawn over the approved image.
        TracedOverlay overlay = new TracedOverlay(this);
        root.addView(overlay, new FrameLayout.LayoutParams(-1,-1));

        setContentView(root);
    }

    static final class TracedOverlay extends View {
        private static final float MASTER_ASPECT = 1536f / 1152f;
        private final Paint routePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint carPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint glassPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint uiBlue = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint uiWhite = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final PathMeasure pm = new PathMeasure();
        private final RectF imageRect = new RectF();
        private final float[] pos = new float[2];
        private final float[] tan = new float[2];

        int exit = 1;
        boolean showRoute = false; // matches MASTER switch appearance
        float progress = 0f;
        ValueAnimator animator;

        TracedOverlay(Context c){
            super(c);
            setClickable(true);

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

            uiBlue.setColor(Color.rgb(20,120,230));
            uiWhite.setColor(Color.WHITE);
            uiWhite.setStyle(Paint.Style.STROKE);
        }

        void reset(){
            if(animator!=null) animator.cancel();
            progress=0f;
            invalidate();
        }

        void start(){
            if(!UserTracedRouteLibrary.hasUserTrace(exit)) return;
            if(animator!=null) animator.cancel();
            animator=ValueAnimator.ofFloat(0f,1f);
            animator.setDuration(durationForExit(exit));
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(a->{progress=(float)a.getAnimatedValue();invalidate();});
            animator.start();
        }

        private long durationForExit(int exit){
            if(exit==1)return 5200;
            if(exit==2)return 7000;
            if(exit==3)return 8600;
            return 10200;
        }

        private void updateImageRect(){
            float vw=getWidth(), vh=getHeight();
            if(vw<=0||vh<=0){imageRect.setEmpty();return;}
            if(vw/vh > MASTER_ASPECT){
                float iw=vh*MASTER_ASPECT;
                float left=(vw-iw)/2f;
                imageRect.set(left,0,left+iw,vh);
            }else{
                float ih=vw/MASTER_ASPECT;
                float top=(vh-ih)/2f;
                imageRect.set(0,top,vw,top+ih);
            }
        }

        private float mapX(float nx){return imageRect.left + nx*imageRect.width();}
        private float mapY(float ny){return imageRect.top + ny*imageRect.height();}
        private float normX(float x){return (x-imageRect.left)/imageRect.width();}
        private float normY(float y){return (y-imageRect.top)/imageRect.height();}

        private void buildPath(){
            updateImageRect();
            float[][] p = UserTracedRouteLibrary.pointsForExit(exit);
            path.reset();
            if(p.length<2 || imageRect.isEmpty()) return;
            path.moveTo(mapX(p[0][0]),mapY(p[0][1]));
            for(int i=1;i<p.length-1;i++){
                float x=mapX(p[i][0]), y=mapY(p[i][1]);
                float mx=mapX((p[i][0]+p[i+1][0])*.5f);
                float my=mapY((p[i][1]+p[i+1][1])*.5f);
                path.quadTo(x,y,mx,my);
            }
            path.lineTo(mapX(p[p.length-1][0]),mapY(p[p.length-1][1]));
        }

        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            if(getWidth()==0||getHeight()==0)return;
            buildPath();
            float s=Math.min(imageRect.width(),imageRect.height());

            if(showRoute){
                shadowPaint.setStrokeWidth(s*.018f);
                routePaint.setStrokeWidth(s*.009f);
                c.drawPath(path,shadowPaint);
                c.drawPath(path,routePaint);
            }

            drawSelectedExit(c,s);
            drawShowRouteState(c,s);
            drawCar(c,s);
        }

        private void drawSelectedExit(Canvas c,float s){
            final float[] ys={0.115f,0.149f,0.182f,0.215f};
            float cx=mapX(0.0247f);
            float cy=mapY(ys[Math.max(0,Math.min(3,exit-1))]);
            float r=s*.0060f;
            c.drawCircle(cx,cy,r,uiBlue);
        }

        private void drawShowRouteState(Canvas c,float s){
            // Small knob overlay so the baked switch visibly follows the actual route state.
            float cy=mapY(0.0335f);
            float cx=mapX(showRoute ? 0.888f : 0.874f);
            float r=s*.0090f;
            c.drawCircle(cx,cy,r,showRoute ? uiBlue : uiWhite);
        }

        @Override public boolean onTouchEvent(MotionEvent e){
            if(e.getAction()!=MotionEvent.ACTION_UP) return true;
            updateImageRect();
            if(imageRect.isEmpty() || !imageRect.contains(e.getX(),e.getY())) return true;

            float x=normX(e.getX());
            float y=normY(e.getY());

            // Exit rows on the left MASTER panel.
            if(x>=0.010f && x<=0.185f){
                if(y>=0.098f && y<0.132f){selectExit(1);return true;}
                if(y>=0.132f && y<0.166f){selectExit(2);return true;}
                if(y>=0.166f && y<0.199f){selectExit(3);return true;}
                if(y>=0.199f && y<=0.235f){selectExit(4);return true;}
            }

            // Show Route row on the right MASTER panel.
            if(x>=0.748f && x<=0.915f && y>=0.012f && y<=0.055f){
                showRoute=!showRoute;
                invalidate();
                return true;
            }

            // START button baked into MASTER.
            if(x>=0.760f && x<=0.905f && y>=0.139f && y<=0.190f){
                start();
                return true;
            }
            return true;
        }

        private void selectExit(int selected){
            exit=selected;
            reset();
        }

        private boolean rightIndicatorOn(){return exit>=3 && progress<0.63f;}
        private boolean leftIndicatorOn(){
            if(exit==1) return true;
            if(exit==2) return progress>0.70f;
            if(exit==3) return progress>0.70f;
            return progress>0.78f;
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
            boolean blink=((int)(progress*60))%2==0;
            if(blink && leftIndicatorOn()) c.drawCircle(-cw*.42f,-ch*.34f,cw*.10f,indicatorPaint);
            if(blink && rightIndicatorOn()) c.drawCircle(cw*.42f,-ch*.34f,cw*.10f,indicatorPaint);
            c.restore();
        }
    }
}
