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
    private JRadioButton radAll, radPassed, radFailed;
    private PaginatedTable styledTable;
    private JLabel lblDiemChuan, lblTrungTuyen, lblTongNV;
    private JTabbedPane tabbedPane;
    private PaginatedTable statsTable;
    private JComboBox<String> cboStatsSearch;
    private List<Object[]> allStatsRows = new ArrayList<>();
    private Map<String, String> nganhNameMap;

    // Cache
    private Map<String, ThiSinh> thiSinhMap;

    private static final String[] COLUMNS = {
        "STT", "CCCD", "Tên", "Tên ngành", "NV Thứ", "Phương thức", "THM",
        "Điểm THXT", "Điểm Cộng", "Điểm ƯT", "Điểm XT", "Kết quả"
    };

    private static final String[] STATS_COLUMNS = {
        "STT", "Mã ngành", "Tên ngành", "Chỉ tiêu",
        "SL Trúng tuyển (Tuyển thẳng)", "SL Trúng tuyển (THPT)", "SL Trúng tuyển (ĐGNL)", "SL Trúng tuyển (VSAT)",
        "Tổng trúng tuyển", "Điểm chuẩn"
    };

    public KetQuaXetTuyenPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.BG_MAIN);

        add(createHeaderPanel(), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIConstants.FONT_BOLD);
        tabbedPane.addTab("Danh sách trúng tuyển chi tiết", createTableCard());
        tabbedPane.addTab("Thống kê trúng tuyển theo phương thức", createStatsCard());
        add(tabbedPane, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> {
            loadAllStats();
            loadResults();
        });
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setOpaque(false);

        // Stats cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(0, 70));

        lblDiemChuan = new JLabel("---");
        lblTrungTuyen = new JLabel("---");
        lblTongNV = new JLabel("---");

        statsPanel.add(createMiniCard("Không trúng tuyển", lblDiemChuan, UIConstants.STAT_BLUE));
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

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);

        // Row 1: Chọn ngành + Buttons
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.setOpaque(false);
        
        JLabel lbl = new JLabel("Chọn ngành:");
        lbl.setFont(UIConstants.FONT_BOLD);
        row1.add(lbl);

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
        row1.add(cboNganh);

        RoundedButton btnView = new RoundedButton(UIConstants.ICON_SEARCH + " Xem kết quả", UIConstants.PRIMARY);
        btnView.addActionListener(e -> loadResults());
        row1.add(btnView);

        // Row 2: Radio buttons
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2.setOpaque(false);
        
        radAll = new JRadioButton("Tất cả", true);
        radPassed = new JRadioButton("Đậu");
        radFailed = new JRadioButton("Rớt");
        
        radAll.setOpaque(false);
        radPassed.setOpaque(false);
        radFailed.setOpaque(false);
        
        ButtonGroup bg = new ButtonGroup();
        bg.add(radAll);
        bg.add(radPassed);
        bg.add(radFailed);
        
        java.awt.event.ActionListener filterAction = e -> loadResults();
        radAll.addActionListener(filterAction);
        radPassed.addActionListener(filterAction);
        radFailed.addActionListener(filterAction);
        
        row2.add(new JLabel("Lọc kết quả: "));
        row2.add(radAll);
        row2.add(radPassed);
        row2.add(radFailed);
        
        topContainer.add(row1, BorderLayout.NORTH);
        topContainer.add(row2, BorderLayout.SOUTH);
        
        card.add(topContainer, BorderLayout.NORTH);

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
                        ((JLabel) c).setText("Trúng tuyển");
                    } else {
                        c.setForeground(UIConstants.DANGER);
                        ((JLabel) c).setFont(UIConstants.FONT_BOLD);
                        ((JLabel) c).setText("Rớt");
                    }
                } else if (col == 5 && value != null) {
                    // Color-code phương thức
                    String val = value.toString().toUpperCase();
                    if (val.startsWith("PT2") || val.contains("THPT")) {
                        c.setForeground(new Color(0x1976D2));
                        ((JLabel) c).setFont(UIConstants.FONT_BOLD);
                        ((JLabel) c).setText("THPT");
                    } else if (val.startsWith("PT3") || val.contains("VSAT")) {
                        c.setForeground(new Color(0x7B1FA2));
                        ((JLabel) c).setFont(UIConstants.FONT_BOLD);
                        ((JLabel) c).setText("VSAT");
                    } else if (val.startsWith("PT4") || val.contains("DGNL")) {
                        c.setForeground(new Color(0xF57C00));
                        ((JLabel) c).setFont(UIConstants.FONT_BOLD);
                        ((JLabel) c).setText("ĐGNL");
                    } else if (val.startsWith("PT1") || val.contains("TUYỂN")) {
                        c.setForeground(new Color(0xD32F2F));
                        ((JLabel) c).setFont(UIConstants.FONT_BOLD);
                        ((JLabel) c).setText("Tuyển thẳng");
                    } else {
                        c.setForeground(UIConstants.TEXT_PRIMARY);
                    }
                } else {
                    c.setForeground(UIConstants.TEXT_PRIMARY);
                }
                if (col == 0 || col == 4 || col == 6 || (col >= 7 && col <= 10)) setHorizontalAlignment(SwingConstants.CENTER);
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

        cboStatsSearch = new JComboBox<>();
        cboStatsSearch.setFont(UIConstants.FONT_REGULAR);
        cboStatsSearch.setPreferredSize(new Dimension(300, 34));
        try {
            cboStatsSearch.addItem("--- Tất cả ngành ---");
            List<Nganh> list = nganhDAO.findAll();
            for (Nganh n : list) {
                cboStatsSearch.addItem(n.getManganh() + " - " + n.getTennganh());
            }
        } catch (Exception e) {
            cboStatsSearch.addItem("(Lỗi tải ngành)");
        }
        cboStatsSearch.addActionListener(e -> doSearchStats());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);
        topPanel.add(new JLabel("Tìm kiếm ngành:"));
        topPanel.add(cboStatsSearch);

        // YÊU CẦU 5: Nút Tải thống kê tất cả chuyển sang tab này
        RoundedButton btnStatsExport = new RoundedButton(UIConstants.ICON_STATISTIC + " Tải thống kê tất cả", new Color(0x00796B));
        btnStatsExport.addActionListener(e -> loadAllStats());
        topPanel.add(btnStatsExport);

        card.add(topPanel, BorderLayout.NORTH);

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
                if (col == 8 && value != null) {
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

    private String getNganhName(String maNganh) {
        if (nganhNameMap == null) {
            try {
                nganhNameMap = nganhDAO.findAll().stream()
                        .collect(Collectors.toMap(Nganh::getManganh, Nganh::getTennganh, (a, b) -> a));
            } catch (Exception e) {
                nganhNameMap = Collections.emptyMap();
            }
        }
        return nganhNameMap.getOrDefault(maNganh, maNganh);
    }

    private void doSearchStats() {
        if (cboStatsSearch == null) return;
        String item = (String) cboStatsSearch.getSelectedItem();
        if (item == null || item.startsWith("---") || item.startsWith("(")) {
            statsTable.setData(allStatsRows);
            return;
        }
        String maNganh = item.split(" - ")[0].trim();
        List<Object[]> filtered = allStatsRows.stream()
                .filter(row -> String.valueOf(row[1]).equals(maNganh))
                .collect(Collectors.toList());
        statsTable.setData(filtered);
    }

    private String normalizePT(String pt) {
        if (pt == null) return "THPT";
        String upper = pt.toUpperCase().trim();
        if (upper.startsWith("PT1") || upper.contains("TUYỂN") || "TT".equals(upper)) return "TT";
        if (upper.startsWith("PT2") || upper.contains("THPT") || "1".equals(upper) || "2".equals(upper)) return "THPT";
        if (upper.startsWith("PT4") || upper.contains("DGNL") || "4".equals(upper)) return "DGNL";
        if (upper.startsWith("PT3") || upper.startsWith("PT5") || upper.contains("VSAT") || "5".equals(upper)) return "VSAT";
        return "THPT";
    }

    private void loadResults() {
        String item = (String) cboNganh.getSelectedItem();
        if (item == null) return;
        
        String ketQuaFilter = "Tất cả";
        if (radPassed != null && radPassed.isSelected()) ketQuaFilter = "Trúng tuyển";
        else if (radFailed != null && radFailed.isSelected()) ketQuaFilter = "Rớt";

        try {
            thiSinhMap = null; // refresh cache
            nganhNameMap = null; // refresh cache
            List<NguyenVongXetTuyen> allNvForStats;

            if (item.startsWith("---")) {
                allNvForStats = nvDAO.findAll();
            } else {
                String maNganh = item.split(" - ")[0].trim();
                allNvForStats = nvDAO.findByMaNganh(maNganh);
            }

            final String finalKetQuaFilter = ketQuaFilter;
            List<NguyenVongXetTuyen> nvList = allNvForStats.stream().filter(n -> {
                if ("Trúng tuyển".equals(finalKetQuaFilter)) return "yes".equals(n.getNvKetqua());
                if ("Rớt".equals(finalKetQuaFilter)) return "duoisan".equals(n.getNvKetqua());
                return true;
            }).sorted(Comparator.comparing(NguyenVongXetTuyen::getNvManganh)
                    .thenComparing(n -> n.getDiemXettuyen() != null ? n.getDiemXettuyen() : BigDecimal.ZERO, Comparator.reverseOrder()))
            .collect(Collectors.toList());

            // Stats
            long trung = allNvForStats.stream().filter(n -> "yes".equals(n.getNvKetqua())).count();
            lblTongNV.setText(String.valueOf(allNvForStats.size()));
            lblTrungTuyen.setText(String.valueOf(trung));
            lblDiemChuan.setText(String.valueOf(allNvForStats.size() - trung));

            // Table
            List<Object[]> rows = new ArrayList<>();
            int stt = 1;
            for (NguyenVongXetTuyen nv : nvList) {
                ThiSinh ts = getThiSinh(nv.getNnCccd());
                String ten = ts != null ? ts.getTen() : "";
                String thm = nv.getTtThm() != null ? nv.getTtThm() : "";
                // Kiểm tra nguyện vọng tuyển thẳng để hiển thị dấu gạch ngang ở các cột điểm
                boolean tuyenThang = "TT".equals(normalizePT(nv.getTtPhuongthuc()));
                rows.add(new Object[]{
                    stt++, nv.getNnCccd(), ten, getNganhName(nv.getNvManganh()), nv.getNvTt(),
                    nv.getTtPhuongthuc(), tuyenThang ? "-" : thm,
                    tuyenThang ? "-" : nv.getDiemThxt(),
                    tuyenThang ? "-" : nv.getDiemCong(),
                    tuyenThang ? "-" : nv.getDiemUtqd(),
                    tuyenThang ? "-" : nv.getDiemXettuyen(),
                    nv.getNvKetqua()
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
                long slTT = ptMap.getOrDefault("TT", 0L);
                long slTHPT = ptMap.getOrDefault("THPT", 0L);
                long slDGNL = ptMap.getOrDefault("DGNL", 0L);
                long slVSAT = ptMap.getOrDefault("VSAT", 0L);
                long total = totalByNganh.getOrDefault(ma, 0L);
                String diemChuan = n.getnDiemtrungtuyen() != null ? n.getnDiemtrungtuyen().toPlainString() : "---";

                rows.add(new Object[]{
                    stt++, ma, n.getTennganh(), n.getnChitieu(),
                    slTT, slTHPT, slDGNL, slVSAT, total, diemChuan
                });
            }

            allStatsRows = rows;
            statsTable.setData(rows);

            // Update summary stats
            long totalTrungTuyen = allNV.stream().filter(n -> "yes".equals(n.getNvKetqua())).count();
            lblTrungTuyen.setText(String.valueOf(totalTrungTuyen));
            lblTongNV.setText(String.valueOf(allNV.size()));
            lblDiemChuan.setText(String.valueOf(allNV.size() - totalTrungTuyen));

            // Switch to stats tab
            tabbedPane.setSelectedIndex(1);
        } catch (Exception e) {
            MessageHelper.showError(this, "Lỗi tải thống kê: " + e.getMessage());
        }
    }
}
