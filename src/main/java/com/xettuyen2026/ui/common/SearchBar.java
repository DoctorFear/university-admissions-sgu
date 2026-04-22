package com.xettuyen2026.ui.common;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * SearchBar với icon kính lúp, placeholder, border focus.
 */
public class SearchBar extends JPanel {

    private JTextField textField;
    private String placeholder;

    public SearchBar(String placeholder, ActionListener onSearch) {
        this.placeholder = placeholder;
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(300, 36));
        setMaximumSize(new Dimension(300, 36));

        textField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(UIConstants.TEXT_HINT);
                    g2.setFont(UIConstants.FONT_REGULAR);
                    g2.drawString(SearchBar.this.placeholder, 8, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        textField.setFont(UIConstants.FONT_REGULAR);
        textField.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        textField.setOpaque(false);

        // Enter key triggers search
        textField.addActionListener(onSearch);

        // Icon label
        JLabel iconLabel = new JLabel();
        javax.swing.Icon searchIcon = UIConstants.getIcon(UIConstants.ICON_SEARCH, 16, 16);
        if (searchIcon != null) {
            iconLabel.setIcon(searchIcon);
            iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 4));
        } else {
            iconLabel.setText(" 🔍 ");
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            iconLabel.setForeground(UIConstants.TEXT_SECONDARY);
        }

        JPanel container = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UIConstants.FIELD_RADIUS * 2, UIConstants.FIELD_RADIUS * 2);
                g2.setColor(textField.hasFocus() ? UIConstants.PRIMARY : UIConstants.BORDER_LIGHT);
                g2.setStroke(new BasicStroke(textField.hasFocus() ? 2f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, UIConstants.FIELD_RADIUS * 2, UIConstants.FIELD_RADIUS * 2);
                g2.dispose();
            }
        };
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        container.add(iconLabel, BorderLayout.WEST);
        container.add(textField, BorderLayout.CENTER);

        // Repaint on focus change for border effect
        textField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { container.repaint(); textField.repaint(); }
            @Override public void focusLost(FocusEvent e) { container.repaint(); textField.repaint(); }
        });

        add(container, BorderLayout.CENTER);
    }

    public String getText() { return textField.getText().trim(); }
    public void clear() { textField.setText(""); }
    public JTextField getTextField() { return textField; }

    public void setText(String string) {
        textField.setText(string); 
    }
}
