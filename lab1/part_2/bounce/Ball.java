package part_2.bounce;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Random;

public class Ball {

    private BallCanvas canvas;
    private int x, y, dx = 2, dy = 2;
    private int size;
    private final Color baseColor;

    public Ball(BallCanvas c) {
        this.canvas = c;
        updateSize();
        Random rand = new Random();
        int w = Math.max(1, canvas.getWidth() - size);
        int h = Math.max(1, canvas.getHeight() - size);
        switch (rand.nextInt(4)) {
            case 0 -> { x = rand.nextInt(w); y = 0; }
            case 1 -> { x = rand.nextInt(w); y = h; }
            case 2 -> { x = 0; y = rand.nextInt(h); }
            default -> { x = w; y = rand.nextInt(h); }
        }
        baseColor = Color.getHSBColor(rand.nextFloat(), 0.85f, 0.95f);
    }

    private void updateSize() {
        int min = Math.min(canvas.getWidth(), canvas.getHeight());
        size = Math.max(Config.BALL_MIN_SIZE, min / 30);
    }

    public void draw(Graphics2D g2) {
        updateSize();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float cx = x + size / 2f, cy = y + size / 2f, r = size / 2f;
        int br = baseColor.getRed(), bg = baseColor.getGreen(), bb = baseColor.getBlue();

        RadialGradientPaint glow = new RadialGradientPaint(cx, cy, r * 2.4f,
            new float[]{0f, 0.5f, 1f},
            new Color[]{new Color(br, bg, bb, 110), new Color(br, bg, bb, 30), new Color(0, 0, 0, 0)});
        g2.setPaint(glow);
        g2.fillOval((int)(cx - r * 2.4f), (int)(cy - r * 2.4f), (int)(r * 4.8f), (int)(r * 4.8f));

        RadialGradientPaint body = new RadialGradientPaint(
            cx - r * 0.28f, cy - r * 0.33f, r * 1.15f,
            new float[]{0f, 0.45f, 1f},
            new Color[]{baseColor.brighter().brighter(), baseColor, baseColor.darker().darker()},
            MultipleGradientPaint.CycleMethod.NO_CYCLE);
        g2.setPaint(body);
        g2.fill(new Ellipse2D.Float(x, y, size, size));

        RadialGradientPaint spec = new RadialGradientPaint(
            cx - r * 0.3f, cy - r * 0.35f, r * 0.45f,
            new float[]{0f, 1f},
            new Color[]{new Color(255, 255, 255, 180), new Color(255, 255, 255, 0)});
        g2.setPaint(spec);
        g2.fill(new Ellipse2D.Float(x, y, size, size));
    }

    public void move() {
        updateSize();
        x += dx; y += dy;
        if (x < 0) { x = 0; dx = -dx; }
        if (x + size >= canvas.getWidth()) { x = canvas.getWidth() - size; dx = -dx; }
        if (y < 0) { y = 0; dy = -dy; }
        if (y + size >= canvas.getHeight()) { y = canvas.getHeight() - size; dy = -dy; }
        canvas.repaint();
    }

    public int getBallX() { return x; }
    public int getBallY() { return y; }
    public int getBallSize() { return size; }
}
