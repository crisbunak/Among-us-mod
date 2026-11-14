package com.demo.menu;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int pad = (int) (getResources().getDisplayMetrics().density * 12);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#11141C"));
        root.setPadding(pad, pad * 2, pad, pad);

        TextView info = new TextView(this);
        info.setText("Demo arenası: ekrana basılı tutup sürükleyerek karakteri yürüt. "
                + "Kayan menüyü başlatıp özellikleri dene.");
        info.setTextColor(Color.WHITE);
        info.setTextSize(14f);
        root.addView(info);

        Button start = new Button(this);
        start.setAllCaps(false);
        start.setText("Kayan menüyü başlat");
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Settings.canDrawOverlays(MainActivity.this)) {
                    Toast.makeText(MainActivity.this,
                            "Önce 'diğer uygulamaların üzerinde göster' iznini aç",
                            Toast.LENGTH_LONG).show();
                    Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivity(i);
                    return;
                }
                startService(new Intent(MainActivity.this, FloatingMenuService.class));
            }
        });
        root.addView(start);

        Button stop = new Button(this);
        stop.setAllCaps(false);
        stop.setText("Kayan menüyü kapat");
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, FloatingMenuService.class));
            }
        });
        root.addView(stop);

        ArenaView arena = new ArenaView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        lp.topMargin = pad;
        root.addView(arena, lp);

        setContentView(root);
    }
                     }
