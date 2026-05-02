package part_1.bounce;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.lang.Thread.State;
import java.lang.management.*;
import java.util.*;
import java.util.List;

public class BallCanvas extends JPanel {

    private List<Ball> balls = new ArrayList<>();
    private List<BallThread> ballThreads = new ArrayList<>();

    private int sleepTime = Config.DEFAULT_SLEEP_MS;

    private long lastTime = System.nanoTime();
    private int frames = 0;
    private int fps = 0;

    private ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private com.sun.management.OperatingSystemMXBean osBean =
        (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    private double processCpuLoad = 0;
    private int runnableThreads = 0;
    private int waitingThreads = 0;
    private long totalBallCpuTime = 0;

    private LinkedList<Integer> fpsHistory = new LinkedList<>();
    private LinkedList<Double> cpuHistory = new LinkedList<>();
    private final int GRAPH_POINTS = 200;

    public BallCanvas() {
        setDoubleBuffered(true);
        threadBean.setThreadCpuTimeEnabled(true);
    }

    public void add(Ball b) { balls.add(b); }

    public void registerBallThread(BallThread t) { ballThreads.add(t); }
    public void unregisterBallThread(BallThread t) { ballThreads.remove(t); }
    public int getSleepTime() { return sleepTime; }
    public void setSleepTime(int sleepTime) { this.sleepTime = sleepTime; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        drawBackground(g2);

        for (Ball b : balls) b.draw(g2);

        updateFPS();
        updateSystemStats();
        updateThreadStats();
        updateCpuTime();
        updateGraph();

        drawOverlay(g2);
        drawGraph(g2);
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

    private void updateFPS() {
        frames++;
        long current = System.nanoTime();
        if (current - lastTime >= 1_000_000_000) {
            fps = frames; frames = 0; lastTime = current;
        }
    }

    private void updateSystemStats() {
        processCpuLoad = osBean.getProcessCpuLoad() * 100.0;
    }

    private void updateThreadStats() {
        runnableThreads = 0; waitingThreads = 0;
        for (BallThread t : ballThreads) {
            switch (t.getState()) {
                case RUNNABLE -> runnableThreads++;
                case WAITING, TIMED_WAITING -> waitingThreads++;
                default -> {}
            }
        }
    }

    private void updateCpuTime() {
        totalBallCpuTime = 0;
        for (BallThread t : ballThreads)
            totalBallCpuTime += threadBean.getThreadCpuTime(t.threadId());
    }

    private void updateGraph() {
        fpsHistory.add(fps);
        cpuHistory.add(processCpuLoad);
        if (fpsHistory.size() > GRAPH_POINTS) fpsHistory.removeFirst();
        if (cpuHistory.size() > GRAPH_POINTS) cpuHistory.removeFirst();
    }

    private void drawOverlay(Graphics2D g2) {
        int pw = 222, ph = 172, px = 15, py = 15;
        g2.setColor(new Color(8, 8, 22, 200));
        g2.fillRoundRect(px, py, pw, ph, 12, 12);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(80, 60, 180, 180));
        g2.drawRoundRect(px, py, pw, ph, 12, 12);
        g2.setColor(new Color(80, 60, 180, 80));
        g2.drawLine(px + 8, py + 22, px + pw - 8, py + 22);

        int tx = px + 12, ty = py + 16;
        g2.setFont(new Font("Consolas", Font.BOLD, 11));
        g2.setColor(new Color(160, 140, 255));
        g2.drawString("STATS", tx, ty);
        ty += 18;

        drawStat(g2, tx, ty, "Balls", String.valueOf(balls.size()), new Color(80, 200, 255)); ty += 17;
        drawStat(g2, tx, ty, "Threads", String.valueOf(ballThreads.size()), new Color(80, 200, 255)); ty += 17;

        Color fpsColor = fps >= 50 ? new Color(80, 220, 100) : fps >= 25 ? new Color(255, 200, 60) : new Color(255, 80, 80);
        drawStat(g2, tx, ty, "FPS", String.valueOf(fps), fpsColor); ty += 17;

        double ft = fps > 0 ? 1000.0 / fps : 0;
        drawStat(g2, tx, ty, "Frame ms", String.format("%.2f", ft), new Color(200, 200, 200)); ty += 17;

        Color cpuColor = processCpuLoad < 40 ? new Color(80, 220, 100) : processCpuLoad < 70 ? new Color(255, 200, 60) : new Color(255, 80, 80);
        drawStat(g2, tx, ty, "CPU %", String.format("%.1f", processCpuLoad), cpuColor); ty += 17;

        drawStat(g2, tx, ty, "RUNNABLE", String.valueOf(runnableThreads), new Color(80, 220, 100)); ty += 17;
        drawStat(g2, tx, ty, "WAITING", String.valueOf(waitingThreads), new Color(255, 200, 60)); ty += 17;
        drawStat(g2, tx, ty, "CPU ms", String.valueOf(totalBallCpuTime / 1_000_000), new Color(200, 180, 255));
    }

    private void drawStat(Graphics2D g2, int x, int y, String label, String value, Color valueColor) {
        g2.setFont(new Font("Consolas", Font.PLAIN, 11));
        g2.setColor(new Color(130, 120, 160));
        g2.drawString(label + ":", x, y);
        g2.setColor(valueColor);
        g2.drawString(value, x + 112, y);
    }

    private void drawGraph(Graphics2D g2) {
        int gw = 300, gh = 120;
        int gx = getWidth() - gw - 15, gy = 15;

        g2.setColor(new Color(8, 8, 22, 200));
        g2.fillRoundRect(gx, gy, gw, gh, 12, 12);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(80, 60, 180, 180));
        g2.drawRoundRect(gx, gy, gw, gh, 12, 12);

        Shape oldClip = g2.getClip();
        g2.setClip(gx + 2, gy + 2, gw - 4, gh - 4);

        drawFilledGraph(g2, fpsHistory, gx, gy, gw, gh, 100,
            new Color(0, 200, 100, 60), new Color(0, 200, 100, 0), new Color(0, 220, 120));
        drawFilledGraph(g2, cpuHistory, gx, gy, gw, gh, 100,
            new Color(255, 80, 80, 60), new Color(255, 80, 80, 0), new Color(255, 100, 80));

        g2.setClip(oldClip);

        g2.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2.setColor(new Color(0, 220, 120));
        g2.drawString("FPS", gx + 8, gy + 13);
        g2.setColor(new Color(255, 100, 80));
        g2.drawString("CPU%", gx + 42, gy + 13);
    }

    private void drawFilledGraph(Graphics2D g2, List<? extends Number> data,
            int x, int y, int width, int height, int maxValue,
            Color fillTop, Color fillBot, Color lineColor) {
        if (data.size() < 2) return;
        int n = data.size();
        float step = (float) width / GRAPH_POINTS;

        Path2D.Float area = new Path2D.Float();
        float x0 = x + 0 * step;
        float y0 = y + height - (data.get(0).floatValue() * height / maxValue);
        area.moveTo(x0, y + height);
        area.lineTo(x0, y0);
        for (int i = 1; i < n; i++) {
            float px = x + i * step;
            float py = y + height - (data.get(i).floatValue() * height / maxValue);
            area.lineTo(px, py);
        }
        area.lineTo(x + (n - 1) * step, y + height);
        area.closePath();

        g2.setPaint(new GradientPaint(x, y, fillTop, x, y + height, fillBot));
        g2.fill(area);

        Path2D.Float line = new Path2D.Float();
        line.moveTo(x + 0 * step, y + height - (data.get(0).floatValue() * height / maxValue));
        for (int i = 1; i < n; i++) {
            float px = x + i * step;
            float py = y + height - (data.get(i).floatValue() * height / maxValue);
            line.lineTo(px, py);
        }
        g2.setColor(lineColor);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(line);
        g2.setStroke(new BasicStroke(1f));
    }
}
