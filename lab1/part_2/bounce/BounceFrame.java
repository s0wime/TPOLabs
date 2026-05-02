package part_2.bounce;

import javax.swing.*;
import java.awt.*;

public class BounceFrame extends JFrame {

    private BallCanvas canvas;
    private JLabel warningLabel;
    private JLabel pocketedLabel;

    public BounceFrame() {
        setSize(Config.FRAME_WIDTH, Config.FRAME_HEIGHT);
        setTitle("Multithreaded Billiard Simulation With Pockets");
        setLocationRelativeTo(null);

        canvas = new BallCanvas();
        add(canvas, BorderLayout.CENTER);

        pocketedLabel = new JLabel("В лузі: 0");
        pocketedLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pocketedLabel.setForeground(new Color(80, 220, 255));
        canvas.setPocketedLabel(pocketedLabel);

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

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        leftPanel.setOpaque(false);

        JButton addOne = createButton("Add Ball", new Color(40, 100, 220));
        JButton addTen = createButton("Add 10",   new Color(40, 100, 220));
        JButton exit   = createButton("Exit",     new Color(90, 30, 30));

        addOne.addActionListener(e -> addBall());
        addTen.addActionListener(e -> { for (int i = 0; i < 10; i++) addBall(); });
        exit.addActionListener(e -> System.exit(0));

        leftPanel.add(addOne);
        leftPanel.add(addTen);
        leftPanel.add(exit);

        warningLabel = new JLabel("");
        warningLabel.setForeground(new Color(255, 80, 80));
        warningLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 4));
        rightPanel.setOpaque(false);
        rightPanel.add(pocketedLabel);
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
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(bg);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));

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

    private void addBall() {
        Ball ball = new Ball(canvas);
        canvas.add(ball);
        BallThread thread = new BallThread(ball, canvas);
        thread.start();
        warningLabel.setText(Thread.activeCount() > Config.WARNING_THREAD_COUNT ? "! High thread count!" : "");
    }
}
