package com.xettuyen2026.ui;

import com.xettuyen2026.dto.DashboardData;
import com.xettuyen2026.service.DashboardService;
import com.xettuyen2026.ui.common.UIConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Dashboard tổng quan: 4 stat cards + top ngành + thống kê phương thức.
 */
public class DashboardPanel extends JPanel {

    private final DashboardService dashboardService = new DashboardService();

    public DashboardPanel() {
        refreshData();
    }

    // Tải lại dữ liệu trang chủ từ database và dựng lại giao diện
    public void refreshData() {
        DashboardData data;
        try {
            data = dashboardService.getDashboardData();
        } catch (Exception e) {
            e.printStackTrace();
            data = new DashboardData();
        }

        removeAll();
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConstants.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        // ── Top: 4 Stat Cards ──
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setPreferredSize(new Dimension(0, 120));

        statsRow.add(createStatCard("🎓", "Tổng thí sinh", String.valueOf(data.getTotalThiSinh()), UIConstants.STAT_BLUE));
        statsRow.add(createStatCard("📚", "Tổng ngành", String.valueOf(data.getTotalNganh()), UIConstants.STAT_PURPLE));
        statsRow.add(createStatCard("📌", "Tổng nguyện vọng", String.valueOf(data.getTotalNguyenVong()), UIConstants.STAT_ORANGE));
        statsRow.add(createStatCard("✅", "Trúng tuyển", data.getTotalTrungTuyen() != null ? String.valueOf(data.getTotalTrungTuyen()) : "---", UIConstants.STAT_GREEN));

        add(statsRow, BorderLayout.NORTH);

        // ── Bottom: 2 columns ──
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 16, 0));
        bottomRow.setOpaque(false);

        bottomRow.add(createTopNganhCard(data));
        bottomRow.add(createStatsCard(data));

        add(bottomRow, BorderLayout.CENTER);
        revalidate();
        repaint();
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

    private JPanel createTopNganhCard(DashboardData data) {
        JPanel card = createCardPanel("Top 10 Ngành nhiều nguyện vọng nhất");

        String[] cols = {"STT", "Mã ngành", "Tên ngành", "Số NV"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(UIConstants.FONT_REGULAR);
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(UIConstants.TABLE_GRID);
        table.getTableHeader().setBackground(UIConstants.TABLE_HEADER);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(UIConstants.FONT_BOLD);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(110);
        table.getColumnModel().getColumn(2).setPreferredWidth(260);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                if (!selected) {
                    c.setBackground(row % 2 == 0 ? UIConstants.ROW_ODD : UIConstants.ROW_EVEN);
                }
                setHorizontalAlignment(col == 2 ? SwingConstants.LEFT : SwingConstants.CENTER);
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                return c;
            }
        });

        int stt = 1;
        for (DashboardData.TopNganhItem item : data.getTopNganhItems()) {
            model.addRow(new Object[]{
                    stt++,
                    item.getMaNganh(),
                    item.getTenNganh(),
                    item.getSoNguyenVong()
            });
        }

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JPanel createStatsCard(DashboardData data) {
        JPanel card = createCardPanel("Thống kê theo phương thức xét tuyển");

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        Color[] colors = {UIConstants.STAT_BLUE, UIConstants.STAT_PURPLE, UIConstants.STAT_GREEN};
        for (int i = 0; i < data.getMethodStats().size(); i++) {
            DashboardData.MethodStatItem item = data.getMethodStats().get(i);
            if (i > 0) {
                content.add(Box.createVerticalStrut(16));
            }
            content.add(createProgressItem(item.getLabel(), item.getCount(), item.getPercent(), colors[Math.min(i, colors.length - 1)]));
        }

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createProgressItem(String label, long count, int percent, Color color) {
        JPanel item = new JPanel(new BorderLayout(0, 4));
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_REGULAR);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        labelPanel.add(lbl, BorderLayout.WEST);
        JLabel pct = new JLabel(count + " NV - " + percent + "%");
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
