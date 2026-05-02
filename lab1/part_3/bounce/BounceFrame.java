package part_3.bounce;

import javax.swing.*;
import java.awt.*;

public class BounceFrame extends JFrame {

    private BallCanvas canvas;
    private JLabel warningLabel;
    private JSpinner experimentBlueCount;

    public BounceFrame() {
        setSize(Config.FRAME_WIDTH, Config.FRAME_HEIGHT);
        setTitle("Thread Priority: Red (high) vs Blue (low)");
        setLocationRelativeTo(null);

        canvas = new BallCanvas();
        add(canvas, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(10, 10, 24));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(80, 60, 180));
                g2.drawLine(0, 0, getWidth(), 0);
                g2.dispose();
            }
        };
        controlPanel.setOpaque(true);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        leftPanel.setOpaque(false);

        JButton addRed        = createButton("Add Red",                  new Color(160, 30, 50));
        JButton addBlue       = createButton("Add Blue",                 new Color(30, 60, 180));
        JButton add10Blue     = createButton("Add 10 Blue",              new Color(30, 60, 180));
        JButton experimentBtn = createButton("Experiment: 1 Red + N Blue", new Color(90, 80, 20));
        JButton exit          = createButton("Exit",                     new Color(90, 30, 30));

        addRed.addActionListener(e -> addBall(Ball.BallType.RED));
        addBlue.addActionListener(e -> addBall(Ball.BallType.BLUE));
        add10Blue.addActionListener(e -> { for (int i = 0; i < 10; i++) addBall(Ball.BallType.BLUE); });
        experimentBtn.addActionListener(e -> runExperiment());
        exit.addActionListener(e -> System.exit(0));

        experimentBlueCount = new JSpinner(new SpinnerNumberModel(15, 5, 500, 5));
        experimentBlueCount.setMaximumSize(new Dimension(70, 30));

        JLabel expLabel = new JLabel("N =");
        expLabel.setForeground(new Color(160, 140, 255));
        expLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        leftPanel.add(addRed);
        leftPanel.add(addBlue);
        leftPanel.add(add10Blue);
        leftPanel.add(expLabel);
        leftPanel.add(experimentBlueCount);
        leftPanel.add(experimentBtn);
        leftPanel.add(exit);

        warningLabel = new JLabel("");
        warningLabel.setForeground(new Color(255, 80, 80));
        warningLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 4));
        rightPanel.setOpaque(false);
        rightPanel.add(warningLabel);

        controlPanel.add(leftPanel, BorderLayout.WEST);
        controlPanel.add(rightPanel, BorderLayout.EAST);

        add(controlPanel, BorderLayout.SOUTH);
    }

    private JButton createButton(String text, Color bg) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(getBackground().brighter());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(bg);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));

        Color hoverBg = brighter(bg);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { button.setBackground(hoverBg); }
            public void mouseExited(java.awt.event.MouseEvent e)  { button.setBackground(bg); }
        });
        return button;
    }

    private static Color brighter(Color c) {
        return new Color(Math.min(255, c.getRed() + 40), Math.min(255, c.getGreen() + 40), Math.min(255, c.getBlue() + 40));
    }

    private void addBall(Ball.BallType type) {
        Ball ball = new Ball(canvas, type);
        canvas.add(ball);
        startBallThread(ball);
        updateWarning();
    }

    private void runExperiment() {
        int n = 20;
        try {
            Object v = experimentBlueCount.getValue();
            if (v instanceof Number) n = ((Number) v).intValue();
        } catch (Exception ignored) {}
        n = Math.max(1, Math.min(500, n));

        int w = canvas.getWidth();
        int h = canvas.getHeight();
        if (w <= 0) w = Config.FRAME_WIDTH;
        if (h <= 0) h = Config.FRAME_HEIGHT - 80;
        int size = Math.max(Config.BALL_MIN_SIZE, Math.min(w, h) / 30);
        int startX = 80;
        int startY = Math.max(0, h / 2 - size / 2);

        Ball redBall = new Ball(canvas, startX, startY, 2, 0, Ball.BallType.RED);
        canvas.add(redBall);

        java.util.List<Ball> blueBalls = new java.util.ArrayList<>();
        for (int i = 0; i < n; i++) {
            Ball blueBall = new Ball(canvas, startX, startY, 2, 0, Ball.BallType.BLUE);
            canvas.add(blueBall);
            blueBalls.add(blueBall);
        }

        startBallThread(redBall);
        for (Ball b : blueBalls) startBallThread(b);
        updateWarning();
    }

    private void startBallThread(Ball ball) {
        BallThread thread = new BallThread(ball, canvas, ball.getType().getThreadPriority());
        thread.start();
    }

    private void updateWarning() {
        warningLabel.setText(Thread.activeCount() > Config.WARNING_THREAD_COUNT ? "! High thread count!" : "");
    }
}
