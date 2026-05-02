package part_4.bounce;

import javax.swing.*;
import java.awt.*;

public class BounceFrame extends JFrame {

    private final BallCanvas canvas;
    private JLabel statusLabel;

    public BounceFrame() {
        setSize(Config.FRAME_WIDTH, Config.FRAME_HEIGHT);
        setTitle("Thread.join(): red → blue → green");
        setLocationRelativeTo(null);

        canvas = new BallCanvas();
        add(canvas, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8)) {
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

        JButton joinDemo = createButton("Red → Blue → Green (join)", new Color(40, 110, 55));
        joinDemo.addActionListener(e -> runJoinDemo());

        statusLabel = new JLabel(
            "Click the button: first the red ball moves, then the blue ball moves, then the green ball moves.");
        statusLabel.setForeground(new Color(160, 140, 255));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        controlPanel.add(joinDemo);
        controlPanel.add(statusLabel);

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

    private void runJoinDemo() {
        statusLabel.setText("The red ball moves...");
        Thread worker = new Thread(() -> {
            try {
                Ball red = new Ball(canvas, Ball.BallType.RED);
                canvas.add(red);
                BallThread redThread = new BallThread(red, canvas, Config.BALL_MOVES_JOIN_DEMO);
                redThread.start();
                redThread.join();
                SwingUtilities.invokeLater(() -> statusLabel.setText("The blue ball moves..."));

                Ball blue = new Ball(canvas, Ball.BallType.BLUE);
                canvas.add(blue);
                BallThread blueThread = new BallThread(blue, canvas, Config.BALL_MOVES_JOIN_DEMO);
                blueThread.start();
                blueThread.join();
                SwingUtilities.invokeLater(() -> statusLabel.setText("The green ball moves..."));

                Ball green = new Ball(canvas, Ball.BallType.GREEN);
                canvas.add(green);
                BallThread greenThread = new BallThread(green, canvas, Config.BALL_MOVES_JOIN_DEMO);
                greenThread.start();
                greenThread.join();

                SwingUtilities.invokeLater(() -> statusLabel.setText("All three threads have finished (join)."));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                SwingUtilities.invokeLater(() -> statusLabel.setText("Interrupted."));
            }
        });
        worker.start();
    }
}
