package part_4.bounce;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BallCanvas extends JPanel {

    private final List<Ball> balls = new ArrayList<>();
    private final List<BallThread> ballThreads = new ArrayList<>();
    private int sleepTime = Config.DEFAULT_SLEEP_MS;

    public BallCanvas() {
        setDoubleBuffered(true);
    }

    public void add(Ball b) { synchronized (balls) { balls.add(b); } }
    public void registerBallThread(BallThread t) { ballThreads.add(t); }
    public void unregisterBallThread(BallThread t) { ballThreads.remove(t); }
    public int getSleepTime() { return sleepTime; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        drawBackground(g2);

        List<Ball> copy;
        synchronized (balls) { copy = new ArrayList<>(balls); }
        for (Ball b : copy) b.draw(g2);

        drawOverlay(g2);
    }

    private void drawBackground(Graphics2D g2) {
        int w = getWidth(), h = getHeight();
        g2.setColor(new Color(6, 6, 16));
        g2.fillRect(0, 0, w, h);

        RadialGradientPaint bgGlow = new RadialGradientPaint(w / 2f, h / 2f,
            Math.max(w, h) / 1.5f,
            new float[]{0f, 1f},
            new Color[]{new Color(25, 20, 60, 80), new Color(0, 0, 0, 0)});
        g2.setPaint(bgGlow);
        g2.fillRect(0, 0, w, h);

        g2.setColor(new Color(255, 255, 255, 18));
        for (int gx = 0; gx < w; gx += 50)
            for (int gy = 0; gy < h; gy += 50)
                g2.fillRect(gx, gy, 1, 1);

        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(80, 60, 180, 60));
        g2.drawRect(0, 0, w - 1, h - 1);
    }

    private void drawOverlay(Graphics2D g2) {
        int pw = 200, ph = 58, px = 15, py = 15;
        g2.setColor(new Color(8, 8, 22, 200));
        g2.fillRoundRect(px, py, pw, ph, 12, 12);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(80, 60, 180, 180));
        g2.drawRoundRect(px, py, pw, ph, 12, 12);

        int tx = px + 12, ty = py + 19;
        drawStat(g2, tx, ty, "Balls", String.valueOf(balls.size()), new Color(80, 200, 255)); ty += 17;
        drawStat(g2, tx, ty, "Threads", String.valueOf(ballThreads.size()), new Color(80, 200, 255));
    }

    private void drawStat(Graphics2D g2, int x, int y, String label, String value, Color valueColor) {
        g2.setFont(new Font("Consolas", Font.PLAIN, 12));
        g2.setColor(new Color(130, 120, 160));
        g2.drawString(label + ":", x, y);
        g2.setColor(valueColor);
        g2.drawString(value, x + 100, y);
    }
}
