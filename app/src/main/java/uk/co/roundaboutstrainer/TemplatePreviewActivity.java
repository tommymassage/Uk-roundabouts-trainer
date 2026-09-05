package uk.co.roundaboutstrainer;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TemplatePreviewActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setBackgroundColor(Color.rgb(238, 243, 247));

        ImageView board = new ImageView(this);
        board.setImageResource(R.drawable.roundabout_template);
        board.setScaleType(ImageView.ScaleType.CENTER_CROP);
        board.setBackground(new ColorDrawable(Color.BLACK));
        root.addView(board, new LinearLayout.LayoutParams(0, -1, 3));

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(28, 28, 28, 28);
        panel.setGravity(Gravity.TOP);
        root.addView(panel, new LinearLayout.LayoutParams(0, -1, 2));

        TextView title = new TextView(this);
        title.setText("UK ROUNDABOUTS TRAINER  v0.7.1");
        title.setTextSize(25);
        title.setTextColor(Color.rgb(25, 35, 45));
        title.setTypeface(null, 1);
        panel.addView(title);

        TextView status = new TextView(this);
        status.setText("IMAGE TEMPLATE TEST\n\nThis screen is loading the saved roundabout image directly from the Android project resource folder.\n\nAsset: res/drawable/roundabout_template.webp\n\nIf the roundabout appears on the left, the image-template approach works and we can build cars, routes and training overlays on top of it.");
        status.setTextSize(17);
        status.setTextColor(Color.rgb(35, 45, 55));
        status.setPadding(0, 24, 0, 0);
        panel.addView(status);

        setContentView(root);
    }
}
