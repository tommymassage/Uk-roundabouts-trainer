package uk.co.roundaboutstrainer;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import android.content.Context;

public class MainActivity extends Activity {
    private RoundaboutView roadView;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setPadding(24, 24, 24, 24);
        root.setBackgroundColor(Color.rgb(238,245,238));

        roadView = new RoundaboutView(this);
        root.addView(roadView, new LinearLayout.LayoutParams(0, -1, 3));

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(28, 12, 12, 12);
        root.addView(panel, new LinearLayout.LayoutParams(0, -1, 2));

        TextView title = new TextView(this);
        title.setText("UK ROUNDABOUTS TRAINER"); title.setTextSize(25); title.setTypeface(null,1);
        panel.addView(title);

        TextView info = new TextView(this);
        info.setText("Choose your approach and exit.\nPractise observation • signal • position • speed • look.");
        info.setTextSize(17); info.setPadding(0,16,0,24); panel.addView(info);

        panel.addView(label("Approach road"));
        Spinner approach = spinner(new String[]{"South","West","North","East"}); panel.addView(approach);
        panel.addView(label("Exit"));
        Spinner exit = spinner(new String[]{"1st exit","2nd exit","3rd exit"}); panel.addView(exit);

        Button simulate = new Button(this); simulate.setText("SHOW DRIVING PATH");
        simulate.setTextSize(17); panel.addView(simulate);
        TextView tip = new TextView(this); tip.setText("UK rule reminder: give priority to traffic from the right unless signs or road markings indicate otherwise.");
        tip.setTextSize(16); tip.setPadding(0,28,0,0); panel.addView(tip);

        simulate.setOnClickListener(v -> { roadView.approach=approach.getSelectedItemPosition(); roadView.exit=exit.getSelectedItemPosition()+1; roadView.invalidate(); });
        setContentView(root);
    }
    private TextView label(String s){ TextView v=new TextView(this);v.setText(s);v.setTextSize(18);v.setTypeface(null,1);v.setPadding(0,12,0,6);return v; }
    private Spinner spinner(String[] values){ Spinner s=new Spinner(this);s.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,values));return s; }

    static class RoundaboutView extends View {
        Paint p=new Paint(1); int approach=0, exit=2;
        RoundaboutView(Context c){super(c);}
        protected void onDraw(Canvas c){ super.onDraw(c); float w=getWidth(),h=getHeight(),cx=w/2,cy=h/2,r=Math.min(w,h)*.22f;
            c.drawColor(Color.rgb(87,145,82)); p.setColor(Color.DKGRAY);p.setStrokeWidth(r*.62f);p.setStyle(Paint.Style.STROKE);c.drawCircle(cx,cy,r,p);
            p.setStyle(Paint.Style.FILL); float rw=r*.62f; c.drawRect(cx-rw/2,cy+r,cx+rw/2,h,p);c.drawRect(cx-rw/2,0,cx+rw/2,cy-r,p);c.drawRect(0,cy-rw/2,cx-r,cy+rw/2,p);c.drawRect(cx+r,cy-rw/2,w,cy+rw/2,p);
            p.setColor(Color.WHITE);p.setStrokeWidth(5);p.setStyle(Paint.Style.STROKE);p.setPathEffect(new android.graphics.DashPathEffect(new float[]{18,16},0));c.drawCircle(cx,cy,r,p);p.setPathEffect(null);
            p.setColor(Color.rgb(255,210,0));p.setStrokeWidth(12);p.setStyle(Paint.Style.STROKE); Path path=new Path();
            float startX=cx,startY=h; path.moveTo(startX,startY); path.lineTo(cx,cy+r); RectF oval=new RectF(cx-r,cy-r,cx+r,cy+r); float sweep= exit==1?-90:exit==2?-180:-270; path.arcTo(oval,90,sweep); c.drawPath(path,p);
            p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);p.setTextSize(30);p.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("Give way",cx-rw/2,cy+r+42,p);
        }
    }
}
