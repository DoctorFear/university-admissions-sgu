package com.xettuyen2026.ui;

import com.xettuyen2026.dao.DiemThiDAO;
import com.xettuyen2026.entity.DiemThiXetTuyen;
import com.xettuyen2026.ui.common.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý Điểm thi - 3 tabs: THPT, ĐGNL, VSAT, mỗi tab có toolbar + table + stats.
 */
public class DiemThiPanel extends JPanel {

    private DiemThiDAO dao;

    public DiemThiPanel() {
        dao = new DiemThiDAO();
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_MAIN);

        // Custom styled TabbedPane
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(UIConstants.FONT_BOLD);
        tabs.setBackground(Color.WHITE);

        tabs.addTab("📝 THPT", createTabPanel("THPT"));
        tabs.addTab("📊 ĐGNL", createTabPanel("DGNL"));
        tabs.addTab("📋 VSAT", createTabPanel("VSAT"));

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createTabPanel(String phuongThuc) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UIConstants.BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setOpaque(false);
        toolbar.setPreferredSize(new Dimension(0, 44));

        JPanel leftBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        leftBar.setOpaque(false);
        SearchBar searchBar = new SearchBar("Tìm theo CCCD...", e -> {});
        leftBar.add(searchBar);
        toolbar.add(leftBar, BorderLayout.WEST);

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        rightBar.setOpaque(false);
        rightBar.add(new RoundedButton(UIConstants.ICON_IMPORT + " Import Excel", new Color(0x00796B)));
        rightBar.add(new RoundedButton(UIConstants.ICON_ADD + " Thêm", UIConstants.SUCCESS));
        rightBar.add(new RoundedButton(UIConstants.ICON_EDIT + " Sửa", UIConstants.WARNING));
        rightBar.add(new RoundedButton(UIConstants.ICON_DELETE + " Xóa", UIConstants.DANGER));
        toolbar.add(rightBar, BorderLayout.EAST);

        panel.add(toolbar, BorderLayout.NORTH);

        // Main content: table + stats sidebar
        JPanel contentPanel = new JPanel(new BorderLayout(12, 0));
        contentPanel.setOpaque(false);

        // Table Card
        String[] columns;
        if ("DGNL".equals(phuongThuc)) {
            columns = new String[]{"STT", "CCCD", "Số báo danh", "Điểm NL1"};
        } else if ("VSAT".equals(phuongThuc)) {
            columns = new String[]{"STT", "CCCD", "Số báo danh", "Điểm VSAT"};
        } else {
            columns = new String[]{"STT", "CCCD", "Toán", "Lý", "Hóa", "Sinh", "Sử", "Địa", "Văn", "Anh"};
        }

        PaginatedTable styledTable = new PaginatedTable(columns);

        // Custom cell renderer to highlight zero scores in red
        styledTable.getTable().setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (!sel) {
                    c.setBackground(row % 2 == 0 ? UIConstants.ROW_ODD : UIConstants.ROW_EVEN);
                }
                setHorizontalAlignment(col >= 2 ? SwingConstants.CENTER : SwingConstants.LEFT);
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

                // Highlight zero scores in red
                if (col >= 2 && value != null) {
                    String val = value.toString().trim();
                    if ("0.00".equals(val) || "0".equals(val)) {
                        c.setForeground(UIConstants.DANGER);
                    } else {
                        c.setForeground(sel ? UIConstants.TEXT_PRIMARY : UIConstants.TEXT_PRIMARY);
                    }
                }
                return c;
            }
        });

        JPanel tableCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 16, 16);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 16, 16);
                g2.dispose();
            }
        };
        tableCard.setOpaque(false);
        tableCard.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        tableCard.add(styledTable, BorderLayout.CENTER);
        contentPanel.add(tableCard, BorderLayout.CENTER);

        // Stats sidebar
        JPanel statsPanel = createStatsPanel();
        contentPanel.add(statsPanel, BorderLayout.EAST);

        panel.add(contentPanel, BorderLayout.CENTER);

        // Load data
        SwingUtilities.invokeLater(() -> loadDiemThi(styledTable, phuongThuc));

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(180, 0));
        panel.setOpaque(false);

        panel.add(createMiniStatCard("Điểm TB", "---", UIConstants.STAT_BLUE));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createMiniStatCard("Cao nhất", "---", UIConstants.STAT_GREEN));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createMiniStatCard("Thấp nhất", "---", UIConstants.STAT_ORANGE));
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createMiniStatCard(String label, String value, Color color) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(UIConstants.BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        card.setMaximumSize(new Dimension(180, 80));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLbl.setForeground(color);
        valLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(valLbl);

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(UIConstants.FONT_SMALL);
        lblLabel.setForeground(UIConstants.TEXT_SECONDARY);
        lblLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblLabel);

        return card;
    }

    private void loadDiemThi(PaginatedTable table, String phuongThuc) {
        try {
            List<DiemThiXetTuyen> allDiem = dao.findAll();
            List<Object[]> rows = new ArrayList<>();
            int stt = 1;
            for (DiemThiXetTuyen d : allDiem) {
                if ("DGNL".equals(phuongThuc)) {
                    if (d.getNl1() != null) {
                        rows.add(new Object[]{stt++, d.getCccd(), d.getSobaodanh(), d.getNl1()});
                    }
                } else if ("VSAT".equals(phuongThuc)) {
                    // VSAT placeholder
                } else {
                    // THPT
                    rows.add(new Object[]{
                        stt++, d.getCccd(),
                        fmt(d.getTo()), fmt(d.getLi()), fmt(d.getHo()), fmt(d.getSi()),
                        fmt(d.getSu()), fmt(d.getDi()), fmt(d.getVa()),
                        fmt(d.getN1Cc())
                    });
                }
            }
            table.setData(rows);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String fmt(BigDecimal v) {
        return v != null ? v.toPlainString() : "---";
    }
}
