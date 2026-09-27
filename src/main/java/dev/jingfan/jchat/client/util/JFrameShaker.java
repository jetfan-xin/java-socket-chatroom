package dev.jingfan.jchat.client.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Applies a short sinusoidal movement to a Swing window. */
public class JFrameShaker {

    public static final int SHAKE_DISTANCE = 4;

    public static final double SHAKE_CYCLE = 10;

    public static final int SHAKE_DURATION = 600;

    private final JFrame frame;
    private Point oldLocation;
    private long startTime;
    private Timer shakeTimer;

    public JFrameShaker(JFrame frame) {
        this.frame = frame;
    }

    public void startShake() {
        oldLocation = frame.getLocation();

        startTime = System.currentTimeMillis();
        shakeTimer = new Timer((int) SHAKE_CYCLE / 5, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;

                double waveOffset = (elapsed % SHAKE_CYCLE) / SHAKE_CYCLE;
                double angle = waveOffset * Math.PI;
                double angley = waveOffset * Math.PI;
                int shakeX = (int) ((Math.sin(angle) * SHAKE_DISTANCE) + oldLocation.x);
                int shakeY = (int) ((Math.sin(angley) * SHAKE_DISTANCE) + oldLocation.y);
                frame.setLocation(shakeX, shakeY);
                if (elapsed >= SHAKE_DURATION) {
                    stopShake();
                }
            }
        });
        shakeTimer.start();
    }

    public void stopShake() {
        shakeTimer.stop();
        frame.setLocation(oldLocation);
    }

    public static void main(String asrg[]) {
        final JFrame frame = new JFrame();
        JButton button = new JButton("Nudge window");
        frame.setBounds(300, 200, 500, 500);
        frame.getContentPane().add(button);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                JFrameShaker dialog1 = new JFrameShaker(frame);
                dialog1.startShake();
            }
        });
    }
}
