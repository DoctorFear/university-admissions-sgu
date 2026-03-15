package com.xettuyen2026.ui.common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * JButton tùy chỉnh bo góc, hover effect, cursor hand.
 */
public class RoundedButton extends JButton {

    private Color bgColor;
    private Color hoverColor;
    private Color pressColor;
    private boolean hovering = false;
    private boolean pressing = false;
    private int radius = UIConstants.BUTTON_RADIUS;

    public RoundedButton(String text) {
        this(text, UIConstants.PRIMARY);
    }

    public RoundedButton(String text, Color bgColor) {
        super(text);
        this.bgColor = bgColor;
        this.hoverColor = brighten(bgColor, 0.15f);
        this.pressColor = darken(bgColor, 0.1f);

        setFont(UIConstants.FONT_BOLD);
        setForeground(Color.WHITE);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(getPreferredSize().width + 24, 36));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
            @Override
            public void mouseExited(MouseEvent e) { hovering = false; pressing = false; repaint(); }
            @Override
            public void mousePressed(MouseEvent e) { pressing = true; repaint(); }
            @Override
            public void mouseReleased(MouseEvent e) { pressing = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg = pressing ? pressColor : (hovering ? hoverColor : bgColor);
        if (!isEnabled()) {
            bg = new Color(0xBDBDBD);
        }

        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
        g2.dispose();

        super.paintComponent(g);
    }

    public void setRadius(int r) { this.radius = r; repaint(); }

    public void setBackgroundColor(Color c) {
        this.bgColor = c;
        this.hoverColor = brighten(c, 0.15f);
        this.pressColor = darken(c, 0.1f);
        repaint();
    }

    private static Color brighten(Color c, float factor) {
        int r = Math.min(255, (int)(c.getRed() + (255 - c.getRed()) * factor));
        int g = Math.min(255, (int)(c.getGreen() + (255 - c.getGreen()) * factor));
        int b = Math.min(255, (int)(c.getBlue() + (255 - c.getBlue()) * factor));
        return new Color(r, g, b);
    }

    private static Color darken(Color c, float factor) {
        int r = Math.max(0, (int)(c.getRed() * (1 - factor)));
        int g = Math.max(0, (int)(c.getGreen() * (1 - factor)));
        int b = Math.max(0, (int)(c.getBlue() * (1 - factor)));
        return new Color(r, g, b);
    }
}
