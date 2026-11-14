package com.demo.menu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

public class ArenaView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rc = new RectF();

    private static final float[][] WALLS = {
        {0.00f, 0.30f, 0.55f, 0.34f},
        {0.45f, 0.55f, 1.00f, 0.59f},
        {0.30f, 0.10f, 0.34f, 0.30f},
        {0.70f, 0.34f, 0.74f, 0.55f},
        {0.00f, 0.75f, 0.40f, 0.79f}
    };

    private static final float[][] TP = {
        {0.15f, 0.12f},
        {0.85f, 0.12f},
        {0.15f, 0.50f},
        {0.85f, 0.80f},
        {0.55f, 0.90f},
        {0.50f, 0.45f}
    };

    private float px, py, safeX, safeY, pr;
    private float jx, jy, ox, oy, joyR;
    private boolean joyActive = false;
    private long last = 0;
    private int tpIndex = 0;
    private int seenTp = 0;
    private boolean ready = false;

    public ArenaView(Context context) {
        super(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        pr = w * 0.045f;
        joyR = w * 0.15f;
        px = TP[0][0] * w;
        py = TP[0][1] * h;
        safeX = px;
        safeY = py;
        seenTp = DemoState.teleportCount;
        ready = true;
    }

    private boolean hits(float x, float y) {
        int w = getWidth();
        int h = getHeight();
        for (float[] k : WALLS) {
            float l = k[0] * w;
            float t = k[1] * h;
            float rr = k[2] * w;
            float b = k[3] * h;
            float cx = Math.max(l, Math.min(x, rr));
            float cy = Math.max(t, Math.min(y, b));
            float dx = x - cx;
            float dy = y - cy;
            if (dx * dx + dy * dy < pr * pr) return true;
        }
        return false;
    }

    private void step(float dt) {
        int w = getWidth();
        int h = getHeight();
        boolean pass = DemoState.noClip || DemoState.ghost;

        if (seenTp != DemoState.teleportCount) {
            seenTp = DemoState.teleportCount;
            tpIndex = (tpIndex + 1) % TP.length;
            px = TP[tpIndex][0] * w;
            py = TP[tpIndex][1] * h;
            safeX = px;
            safeY = py;
        }

        if (!pass) {
            if (hits(px, py)) {
                px = safeX;
                py = safeY;
            } else {
                safeX = px;
                safeY = py;
            }
        }

        float sp = w * 0.30f * (DemoState.speed ? 2.5f : 1f) * dt;
        float nx = px + jx * sp;
        float ny = py + jy * sp;

        if (pass) {
            px = nx;
            py = ny;
        } else {
            if (!hits(nx, py)) px = nx;
            if (!hits(px, ny)) py = ny;
        }

        px = Math.max(pr, Math.min(w - pr, px));
        py = Math.max(pr, Math.min(h - pr, py));
    }

    private String o(boolean b) {
        return b ? "AÇIK" : "KAPALI";
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        if (!ready) return;

        long now = System.nanoTime();
        float dt = last == 0 ? 0f : (now - last) / 1e9f;
        last = now;
        if (dt > 0.05f) dt = 0.05f;
        step(dt);

        int w = getWidth();
        int h = getHeight();

        c.drawColor(Color.parseColor("#1B1F2A"));

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(w * 0.006f);
        p.setColor(Color.parseColor("#2E7D6B"));
        for (float[] t : TP) {
            c.drawCircle(t[0] * w, t[1] * h, pr * 1.1f, p);
        }

        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.parseColor("#47526B"));
        for (float[] k : WALLS) {
            rc.set(k[0] * w, k[1] * h, k[2] * w, k[3] * h);
            c.drawRoundRect(rc, 8f, 8f, p);
        }

        drawPlayer(c);

        if (joyActive) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(w * 0.005f);
            p.setColor(Color.WHITE);
            p.setAlpha(80);
            c.drawCircle(ox, oy, joyR, p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.WHITE);
            p.setAlpha(120);
            c.drawCircle(ox + jx * joyR, oy + jy * joyR, joyR * 0.35f, p);
        }

        p.setStyle(Paint.Style.FILL);
        p.setTextAlign(Paint.Align.LEFT);
        p.setTextSize(w * 0.032f);
        p.setColor(Color.parseColor("#C8D0E0"));
        p.setAlpha(255);
        c.drawText("No Clip: " + o(DemoState.noClip) + "   Hız x2.5: " + o(DemoState.speed),
                w * 0.03f, w * 0.05f, p);
        c.drawText("Hayalet: " + o(DemoState.ghost) + "   Takım: "
                + (DemoState.impostor ? "IMPOSTOR" : "CREWMATE"), w * 0.03f, w * 0.09f, p);

        postInvalidateOnAnimation();
    }

    private void drawPlayer(Canvas c) {
        int body = DemoState.impostor ? Color.parseColor("#C51111") : Color.parseColor("#00B7C9");
        int alpha = DemoState.ghost ? 110 : 255;
        float bh = pr * 2.3f;

        p.setStyle(Paint.Style.FILL);

        p.setColor(body);
        p.setAlpha(alpha);
        rc.set(px - pr * 1.25f, py - pr * 0.3f, px - pr * 0.7f, py + pr * 0.8f);
        c.drawRoundRect(rc, pr * 0.2f, pr * 0.2f, p);

        p.setColor(body);
        p.setAlpha(alpha);
        rc.set(px - pr, py - bh / 2f, px + pr, py + bh / 2f);
        c.drawRoundRect(rc, pr * 0.8f, pr * 0.8f, p);

        p.setColor(Color.parseColor("#A8E6F5"));
        p.setAlpha(alpha);
        rc.set(px - pr * 0.1f, py - bh * 0.32f, px + pr * 0.95f, py - bh * 0.02f);
        c.drawRoundRect(rc, pr * 0.4f, pr * 0.4f, p);

        p.setColor(Color.WHITE);
        p.setAlpha(255);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(pr * 0.7f);
        c.drawText(DemoState.impostor ? "IMPOSTOR" : "CREWMATE", px, py - bh / 2f - pr * 0.3f, p);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                joyActive = true;
                ox = e.getX();
                oy = e.getY();
                jx = 0f;
                jy = 0f;
                return true;
            case MotionEvent.ACTION_MOVE: {
                float dx = (e.getX() - ox) / joyR;
                float dy = (e.getY() - oy) / joyR;
                float len = (float) Math.sqrt(dx * dx + dy * dy);
                if (len > 1f) {
                    dx /= len;
                    dy /= len;
                }
                jx = dx;
                jy = dy;
                return true;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                joyActive = false;
                jx = 0f;
                jy = 0f;
                return true;
        }
        return true;
    }
  }
