package com.xettuyen2026.ui;

import com.xettuyen2026.dao.NguyenVongDAO;
import com.xettuyen2026.dao.NganhDAO;
import com.xettuyen2026.dao.ThiSinhDAO;
import com.xettuyen2026.entity.Nganh;
import com.xettuyen2026.entity.NguyenVongXetTuyen;
import com.xettuyen2026.entity.ThiSinh;
import com.xettuyen2026.ui.common.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel hiển thị kết quả xét tuyển:
 * - Tab 1: Danh sách trúng tuyển chi tiết theo ngành (CCCD, Họ, Tên, THM, điểm THXT, điểm cộng, điểm ƯT, điểm XT)
 * - Tab 2: Thống kê số lượng trúng tuyển từng phương thức theo ngành
 */
public class KetQuaXetTuyenPanel extends JPanel {

    private NguyenVongDAO nvDAO = new NguyenVongDAO();
    private NganhDAO nganhDAO = new NganhDAO();
    private ThiSinhDAO thiSinhDAO = new ThiSinhDAO();
    private JComboBox<String> cboNganh;
    private PaginatedTable styledTable;
    private JLabel lblDiemChuan, lblTrungTuyen, lblTongNV;
    private JTabbedPane tabbedPane;
    private PaginatedTable statsTable;

    // Cache
    private Map<String, ThiSinh> thiSinhMap;

    private static final String[] COLUMNS = {
        "STT", "CCCD", "Họ", "Tên", "NV Thứ", "Phương thức", "THM",
        "Điểm THXT", "Điểm Cộng", "Điểm ƯT", "Điểm XT", "Kết quả"
    };

    private static final String[] STATS_COLUMNS = {
        "STT", "Mã ngành", "Tên ngành", "Chỉ tiêu",
        "SL Trúng tuyển (THPT)", "SL Trúng tuyển (ĐGNL)", "SL Trúng tuyển (VSAT)",
        "Tổng trúng tuyển", "Điểm chuẩn"
    };

    public KetQuaXetTuyenPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.BG_MAIN);

        add(createHeaderPanel(), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIConstants.FONT_BOLD);
        tabbedPane.addTab("📋 Danh sách trúng tuyển chi tiết", createTableCard());
        tabbedPane.addTab("📊 Thống kê trúng tuyển theo phương thức", createStatsCard());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setOpaque(false);

        // Top row: ngành selector + button
        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        selectPanel.setOpaque(false);

        JLabel lbl = new JLabel("Chọn ngành:");
        lbl.setFont(UIConstants.FONT_BOLD);
        selectPanel.add(lbl);

        cboNganh = new JComboBox<>();
        cboNganh.setFont(UIConstants.FONT_REGULAR);
        cboNganh.setPreferredSize(new Dimension(400, 34));
        try {
            cboNganh.addItem("--- Tất cả ngành ---");
            List<Nganh> list = nganhDAO.findAll();
            for (Nganh n : list) {
                cboNganh.addItem(n.getManganh() + " - " + n.getTennganh());
            }
        } catch (Exception e) {
            cboNganh.addItem("(Lỗi tải ngành)");
        }
        selectPanel.add(cboNganh);

        RoundedButton btnView = new RoundedButton(UIConstants.ICON_DASHBOARD + " Xem kết quả", UIConstants.PRIMARY);
        btnView.addActionListener(e -> loadResults());
        selectPanel.add(btnView);

        RoundedButton btnStats = new RoundedButton(UIConstants.ICON_DASHBOARD + " Tải thống kê tất cả", new Color(0x00796B));
        btnStats.addActionListener(e -> loadAllStats());
        selectPanel.add(btnStats);

        header.add(selectPanel, BorderLayout.NORTH);

        // Stats cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(0, 70));

        lblDiemChuan = new JLabel("---");
        lblTrungTuyen = new JLabel("---");
        lblTongNV = new JLabel("---");

        statsPanel.add(createMiniCard("Điểm chuẩn", lblDiemChuan, UIConstants.STAT_BLUE));
        statsPanel.add(createMiniCard("Trúng tuyển", lblTrungTuyen, UIConstants.STAT_GREEN));
        statsPanel.add(createMiniCard("Tổng NV", lblTongNV, UIConstants.STAT_ORANGE));

        header.add(statsPanel, BorderLayout.CENTER);

        return header;
    }

    private JPanel createMiniCard(String label, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(color);
                g2.fillRoundRect(0, 0, 5, getHeight(), 5, 5);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(color);
        card.add(valueLabel, BorderLayout.WEST);

        JLabel lblText = new JLabel(label);
        lblText.setFont(UIConstants.FONT_REGULAR);
        lblText.setForeground(UIConstants.TEXT_SECONDARY);
        lblText.setHorizontalAlignment(SwingConstants.RIGHT);
        card.add(lblText, BorderLayout.EAST);

        return card;
    }

    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 2, getHeight() - 2, 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        styledTable = new PaginatedTable(COLUMNS);

        styledTable.getTable().setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? UIConstants.ROW_ODD : UIConstants.ROW_EVEN);
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                if (col == 11 && value != null) {
                    if ("yes".equals(value.toString())) {
                        c.setForeground(UIConstants.SUCCESS);
                        ((JLabel) c).setFont(UIConstants.FONT_BOLD);
                        ((JLabel) c).setText("✅ Trúng tuyển");
                    } else {
                        c.setForeground(UIConstants.DANGER);
                        ((JLabel) c).setFont(UIConstants.FONT_BOLD);
                        ((JLabel) c).setText("❌ Rớt");
                    }
                } else if (col == 5 && value != null) {
                    // Color-code phương thức
                    String val = value.toString().toUpperCase();
                    if ("PT2".equals(val) || "THPT".equals(val) || "1".equals(val)) {
                        c.setForeground(new Color(0x1976D2));
                        ((JLabel) c).setFont(UIConstants.FONT_BOLD);
                        ((JLabel) c).setText("THPT");
                    } else if ("PT3".equals(val) || "VSAT".equals(val) || "5".equals(val)) {
                        c.setForeground(new Color(0x7B1FA2));
                        ((JLabel) c).setFont(UIConstants.FONT_BOLD);
                        ((JLabel) c).setText("VSAT");
                    } else if ("PT4".equals(val) || "DGNL".equals(val) || "4".equals(val)) {
                        c.setForeground(new Color(0xF57C00));
                        ((JLabel) c).setFont(UIConstants.FONT_BOLD);
                        ((JLabel) c).setText("ĐGNL");
                    } else {
                        c.setForeground(UIConstants.TEXT_PRIMARY);
                    }
                } else {
                    c.setForeground(UIConstants.TEXT_PRIMARY);
                }
                if (col == 0 || col == 4 || (col >= 7 && col <= 10)) setHorizontalAlignment(SwingConstants.CENTER);
                else setHorizontalAlignment(SwingConstants.LEFT);
                return c;
            }
        });

        card.add(styledTable, BorderLayout.CENTER);
        return card;
    }

    private JPanel createStatsCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 2, getHeight() - 2, 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        statsTable = new PaginatedTable(STATS_COLUMNS);

        statsTable.getTable().setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? UIConstants.ROW_ODD : UIConstants.ROW_EVEN);
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                c.setForeground(UIConstants.TEXT_PRIMARY);
                if (col == 0 || col == 3 || (col >= 4 && col <= 8)) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                // Bold total column
                if (col == 7 && value != null) {
                    ((JLabel) c).setFont(UIConstants.FONT_BOLD);
                    c.setForeground(UIConstants.SUCCESS);
                }
                return c;
            }
        });

        card.add(statsTable, BorderLayout.CENTER);
        return card;
    }

    private ThiSinh getThiSinh(String cccd) {
        if (thiSinhMap == null) {
            try {
                thiSinhMap = new HashMap<>();
                for (ThiSinh ts : thiSinhDAO.findAll()) {
                    thiSinhMap.put(ts.getCccd(), ts);
                }
            } catch (Exception e) {
                thiSinhMap = new HashMap<>();
            }
        }
        return thiSinhMap.get(cccd);
    }

    private String normalizePT(String pt) {
        if (pt == null) return "THPT";
        String upper = pt.toUpperCase().trim();
        if ("PT2".equals(upper) || "THPT".equals(upper) || "1".equals(upper) || "2".equals(upper)) return "THPT";
        if ("PT4".equals(upper) || "DGNL".equals(upper) || "4".equals(upper)) return "DGNL";
        if ("PT3".equals(upper) || "PT5".equals(upper) || "VSAT".equals(upper) || "5".equals(upper)) return "VSAT";
        return "THPT";
    }

    private void loadResults() {
        String item = (String) cboNganh.getSelectedItem();
        if (item == null) return;

        try {
            thiSinhMap = null; // refresh cache
            List<NguyenVongXetTuyen> nvList;

            if (item.startsWith("---")) {
                // Tất cả ngành - chỉ hiển thị trúng tuyển
                nvList = nvDAO.findAll().stream()
                        .filter(n -> "yes".equals(n.getNvKetqua()))
                        .sorted(Comparator.comparing(NguyenVongXetTuyen::getNvManganh)
                                .thenComparing(n -> n.getDiemXettuyen() != null ? n.getDiemXettuyen() : BigDecimal.ZERO,
                                        Comparator.reverseOrder()))
                        .collect(Collectors.toList());

                lblTongNV.setText(String.valueOf(nvDAO.findAll().size()));
                lblTrungTuyen.setText(String.valueOf(nvList.size()));
                lblDiemChuan.setText("---");
            } else {
                String maNganh = item.split(" - ")[0].trim();
                Nganh nganh = nganhDAO.findByMaNganh(maNganh);
                nvList = nvDAO.findByMaNganh(maNganh);

                // Stats
                lblTongNV.setText(String.valueOf(nvList.size()));
                long trung = nvList.stream().filter(n -> "yes".equals(n.getNvKetqua())).count();
                lblTrungTuyen.setText(String.valueOf(trung));
                if (nganh != null && nganh.getnDiemtrungtuyen() != null) {
                    lblDiemChuan.setText(nganh.getnDiemtrungtuyen().toPlainString());
                } else {
                    lblDiemChuan.setText("Chưa xét");
                }
            }

            // Table
            List<Object[]> rows = new ArrayList<>();
            int stt = 1;
            for (NguyenVongXetTuyen nv : nvList) {
                ThiSinh ts = getThiSinh(nv.getNnCccd());
                String ho = ts != null ? ts.getHo() : "";
                String ten = ts != null ? ts.getTen() : "";
                String thm = nv.getTtThm() != null ? nv.getTtThm() : "";
                rows.add(new Object[]{
                    stt++, nv.getNnCccd(), ho, ten, nv.getNvTt(),
                    nv.getTtPhuongthuc(), thm,
                    nv.getDiemThxt(), nv.getDiemCong(), nv.getDiemUtqd(),
                    nv.getDiemXettuyen(), nv.getNvKetqua()
                });
            }
            styledTable.setData(rows);

            // Switch to detail tab
            tabbedPane.setSelectedIndex(0);
        } catch (Exception e) {
            MessageHelper.showError(this, "Lỗi tải kết quả: " + e.getMessage());
        }
    }

    /**
     * Tải thống kê số lượng trúng tuyển từng phương thức theo ngành.
     */
    private void loadAllStats() {
        try {
            List<Nganh> nganhList = nganhDAO.findAll();
            List<NguyenVongXetTuyen> allNV = nvDAO.findAll();

            // Group trúng tuyển theo mã ngành → phương thức → count
            Map<String, Map<String, Long>> statsByNganh = new LinkedHashMap<>();
            Map<String, Long> totalByNganh = new LinkedHashMap<>();

            for (NguyenVongXetTuyen nv : allNV) {
                if (!"yes".equals(nv.getNvKetqua())) continue;
                String maNganh = nv.getNvManganh();
                String pt = normalizePT(nv.getTtPhuongthuc());

                statsByNganh.computeIfAbsent(maNganh, k -> new LinkedHashMap<>());
                Map<String, Long> ptMap = statsByNganh.get(maNganh);
                ptMap.put(pt, ptMap.getOrDefault(pt, 0L) + 1);
                totalByNganh.put(maNganh, totalByNganh.getOrDefault(maNganh, 0L) + 1);
            }

            // Build table rows
            List<Object[]> rows = new ArrayList<>();
            int stt = 1;
            for (Nganh n : nganhList) {
                String ma = n.getManganh();
                Map<String, Long> ptMap = statsByNganh.getOrDefault(ma, Collections.emptyMap());
                long slTHPT = ptMap.getOrDefault("THPT", 0L);
                long slDGNL = ptMap.getOrDefault("DGNL", 0L);
                long slVSAT = ptMap.getOrDefault("VSAT", 0L);
                long total = totalByNganh.getOrDefault(ma, 0L);
                String diemChuan = n.getnDiemtrungtuyen() != null ? n.getnDiemtrungtuyen().toPlainString() : "---";

                rows.add(new Object[]{
                    stt++, ma, n.getTennganh(), n.getnChitieu(),
                    slTHPT, slDGNL, slVSAT, total, diemChuan
                });
            }

            statsTable.setData(rows);

            // Update summary stats
            long totalTrungTuyen = allNV.stream().filter(n -> "yes".equals(n.getNvKetqua())).count();
            lblTrungTuyen.setText(String.valueOf(totalTrungTuyen));
            lblTongNV.setText(String.valueOf(allNV.size()));
            lblDiemChuan.setText("Xem bảng");

            // Switch to stats tab
            tabbedPane.setSelectedIndex(1);
        } catch (Exception e) {
            MessageHelper.showError(this, "Lỗi tải thống kê: " + e.getMessage());
        }
    }
}
