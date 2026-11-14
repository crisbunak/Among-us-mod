package com.demo.menu;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FloatingMenuService extends Service {

    private WindowManager wm;
    private LinearLayout root;
    private LinearLayout panel;
    private WindowManager.LayoutParams params;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        build();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable bg(int color, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        return g;
    }

    private Button makeButton(String text) {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setText(text);
        b.setTextColor(Color.WHITE);
        b.setTextSize(13f);
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setPadding(dp(10), dp(6), dp(10), dp(6));
        return b;
    }

    private void refresh(Button b, String label, boolean on) {
        b.setText(label + ": " + (on ? "AÇIK" : "KAPALI"));
        b.setBackground(bg(on ? Color.parseColor("#2E9E5B") : Color.parseColor("#444B5E"), 8));
    }

    private void refreshTeam(Button b) {
        boolean imp = DemoState.impostor;
        b.setText("Takım: " + (imp ? "IMPOSTOR" : "CREWMATE"));
        b.setBackground(bg(imp ? Color.parseColor("#B3261E") : Color.parseColor("#1F8FA3"), 8));
    }

    private void build() {
        int type = Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = dp(16);
        params.y = dp(140);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(bg(Color.argb(235, 20, 24, 36), 14));
        root.setPadding(dp(8), dp(8), dp(8), dp(8));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView handle = new TextView(this);
        handle.setText("[ MENU ] sürükle");
        handle.setTextColor(Color.WHITE);
        handle.setTextSize(14f);
        handle.setPadding(dp(8), dp(8), dp(12), dp(8));
        header.addView(handle);

        final Button toggleBtn = makeButton("-");
        header.addView(toggleBtn);

        Button closeBtn = makeButton("X");
        closeBtn.setBackground(bg(Color.parseColor("#B3261E"), 8));
        LinearLayout.LayoutParams cl = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cl.leftMargin = dp(6);
        header.addView(closeBtn, cl);

        root.addView(header);

        panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);

        final Button bNoClip = makeButton("");
        refresh(bNoClip, "No Clip", DemoState.noClip);
        bNoClip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DemoState.noClip = !DemoState.noClip;
                refresh(bNoClip, "No Clip", DemoState.noClip);
            }
        });

        final Button bSpeed = makeButton("");
        refresh(bSpeed, "Speed Hack x2.5", DemoState.speed);
        bSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DemoState.speed = !DemoState.speed;
                refresh(bSpeed, "Speed Hack x2.5", DemoState.speed);
            }
        });

        final Button bTeam = makeButton("");
        refreshTeam(bTeam);
        bTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DemoState.impostor = !DemoState.impostor;
                refreshTeam(bTeam);
            }
        });

        final Button bGhost = makeButton("");
        refresh(bGhost, "Ghost Mode", DemoState.ghost);
        bGhost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DemoState.ghost = !DemoState.ghost;
                refresh(bGhost, "Ghost Mode", DemoState.ghost);
            }
        });

        Button bTp = makeButton("Teleport (sonraki nokta)");
        bTp.setBackground(bg(Color.parseColor("#7B4FD0"), 8));
        bTp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DemoState.teleportCount = DemoState.teleportCount + 1;
            }
        });

        Button[] all = {bNoClip, bSpeed, bTeam, bGhost, bTp};
        for (Button b : all) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = dp(6);
            panel.addView(b, lp);
        }
        root.addView(panel);

        toggleBtn.setBackground(bg(Color.parseColor("#444B5E"), 8));
        toggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean show = panel.getVisibility() == View.GONE;
                panel.setVisibility(show ? View.VISIBLE : View.GONE);
                toggleBtn.setText(show ? "-" : "+");
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();
            }
        });

        handle.setOnTouchListener(new View.OnTouchListener() {
            private int startX;
            private int startY;
            private float touchX;
            private float touchY;

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = params.x;
                        startY = params.y;
                        touchX = e.getRawX();
                        touchY = e.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = startX + (int) (e.getRawX() - touchX);
                        params.y = startY + (int) (e.getRawY() - touchY);
                        wm.updateViewLayout(root, params);
                        return true;
                }
                return false;
            }
        });

        wm.addView(root, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (root != null) {
            try {
                wm.removeView(root);
            } catch (Exception ignored) {
            }
            root = null;
        }
    }
}
