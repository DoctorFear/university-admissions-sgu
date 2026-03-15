package com.xettuyen2026.ui;

import com.xettuyen2026.ui.common.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Dashboard tổng quan: 4 stat cards + top ngành + thống kê phương thức.
 */
public class DashboardPanel extends JPanel {

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConstants.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        // ── Top: 4 Stat Cards ──
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setPreferredSize(new Dimension(0, 120));

        statsRow.add(createStatCard("🎓", "Tổng thí sinh", "---", UIConstants.STAT_BLUE));
        statsRow.add(createStatCard("📚", "Tổng ngành", "---", UIConstants.STAT_PURPLE));
        statsRow.add(createStatCard("📌", "Tổng nguyện vọng", "---", UIConstants.STAT_ORANGE));
        statsRow.add(createStatCard("✅", "Trúng tuyển", "---", UIConstants.STAT_GREEN));

        add(statsRow, BorderLayout.NORTH);

        // ── Bottom: 2 columns ──
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 16, 0));
        bottomRow.setOpaque(false);

        bottomRow.add(createTopNganhCard());
        bottomRow.add(createStatsCard());

        add(bottomRow, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String icon, String label, String value, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 12));
                g2.fill(new RoundRectangle2D.Float(2, 2, getWidth() - 2, getHeight() - 2, 24, 24));
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 2, getHeight() - 2, 24, 24));
                // Left accent
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, 6, getHeight() - 2, 6, 6);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 16));

        // Icon
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        iconLbl.setPreferredSize(new Dimension(50, 50));
        card.add(iconLbl, BorderLayout.WEST);

        // Text
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(UIConstants.FONT_STAT_NUM);
        valLbl.setForeground(accentColor);
        valLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(valLbl);

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(UIConstants.FONT_REGULAR);
        lblLabel.setForeground(UIConstants.TEXT_SECONDARY);
        lblLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(lblLabel);

        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTopNganhCard() {
        JPanel card = createCardPanel("📊 Top 10 Ngành nhiều nguyện vọng nhất");

        String[] cols = {"#", "Mã ngành", "Tên ngành", "Số NV"};
        JTable table = new JTable(new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        });
        table.setFont(UIConstants.FONT_REGULAR);
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(UIConstants.TABLE_GRID);
        table.getTableHeader().setBackground(UIConstants.TABLE_HEADER);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(UIConstants.FONT_BOLD);

        // Sample data placeholder
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) table.getModel();
        model.addRow(new Object[]{"1", "7480201", "Công nghệ thông tin", "---"});
        model.addRow(new Object[]{"2", "7340101", "Quản trị kinh doanh", "---"});
        model.addRow(new Object[]{"3", "7140202", "Giáo dục Tiểu học", "---"});

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JPanel createStatsCard() {
        JPanel card = createCardPanel("📈 Thống kê theo phương thức xét tuyển");

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        content.add(createProgressItem("PT2 - Xét điểm THPT", 65, UIConstants.STAT_BLUE));
        content.add(Box.createVerticalStrut(16));
        content.add(createProgressItem("PT4 - Xét học bạ", 20, UIConstants.STAT_ORANGE));
        content.add(Box.createVerticalStrut(16));
        content.add(createProgressItem("ĐGNL - Đánh giá năng lực", 10, UIConstants.STAT_PURPLE));
        content.add(Box.createVerticalStrut(16));
        content.add(createProgressItem("VSAT - V-SAT", 5, UIConstants.STAT_GREEN));

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createProgressItem(String label, int percent, Color color) {
        JPanel item = new JPanel(new BorderLayout(0, 4));
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_REGULAR);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        labelPanel.add(lbl, BorderLayout.WEST);
        JLabel pct = new JLabel(percent + "%");
        pct.setFont(UIConstants.FONT_BOLD);
        pct.setForeground(color);
        labelPanel.add(pct, BorderLayout.EAST);
        item.add(labelPanel, BorderLayout.NORTH);

        // Progress bar
        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Background
                g2.setColor(new Color(0xE8E8E8));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Fill
                int w = (int) (getWidth() * percent / 100.0);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, w, getHeight(), 10, 10);
                g2.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(0, 10));
        bar.setOpaque(false);
        item.add(bar, BorderLayout.CENTER);

        return item;
    }

    private JPanel createCardPanel(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 12));
                g2.fill(new RoundRectangle2D.Float(2, 2, getWidth() - 2, getHeight() - 2, 16, 16));
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 2, getHeight() - 2, 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UIConstants.FONT_SUBTITLE);
        titleLbl.setForeground(UIConstants.TEXT_PRIMARY);
        card.add(titleLbl, BorderLayout.NORTH);

        return card;
    }
}
