package com.xettuyen2026.ui;

import com.xettuyen2026.dao.NguyenVongDAO;
import com.xettuyen2026.dao.NganhDAO;
import com.xettuyen2026.entity.Nganh;
import com.xettuyen2026.entity.NguyenVongXetTuyen;
import com.xettuyen2026.ui.common.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

/**
 * Panel hiển thị kết quả xét tuyển theo ngành: điểm chuẩn, danh sách trúng tuyển/rớt.
 */
public class KetQuaXetTuyenPanel extends JPanel {

    private NguyenVongDAO nvDAO = new NguyenVongDAO();
    private NganhDAO nganhDAO = new NganhDAO();
    private JComboBox<String> cboNganh;
    private PaginatedTable styledTable;
    private JLabel lblDiemChuan, lblTrungTuyen, lblTongNV;

    private static final String[] COLUMNS = {
        "STT", "CCCD", "NV Thứ", "Phương thức", "Điểm THXT", "Điểm Cộng",
        "Điểm ƯT", "Điểm XT", "Kết quả"
    };

    public KetQuaXetTuyenPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.BG_MAIN);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createTableCard(), BorderLayout.CENTER);
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
            List<Nganh> list = nganhDAO.findAll();
            for (Nganh n : list) {
                cboNganh.addItem(n.getManganh() + " - " + n.getTennganh());
            }
        } catch (Exception e) {
            cboNganh.addItem("(Lỗi tải ngành)");
        }
        selectPanel.add(cboNganh);

        RoundedButton btnView = new RoundedButton("📊 Xem kết quả", UIConstants.PRIMARY);
        btnView.addActionListener(e -> loadResults());
        selectPanel.add(btnView);

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
                if (col == 8 && value != null) {
                    if ("yes".equals(value.toString())) {
                        c.setForeground(UIConstants.SUCCESS);
                        ((JLabel) c).setFont(UIConstants.FONT_BOLD);
                        ((JLabel) c).setText("✅ Trúng tuyển");
                    } else {
                        c.setForeground(UIConstants.DANGER);
                        ((JLabel) c).setFont(UIConstants.FONT_BOLD);
                        ((JLabel) c).setText("❌ Rớt");
                    }
                } else {
                    c.setForeground(UIConstants.TEXT_PRIMARY);
                }
                if (col >= 4 && col <= 7) setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        card.add(styledTable, BorderLayout.CENTER);
        return card;
    }

    private void loadResults() {
        String item = (String) cboNganh.getSelectedItem();
        if (item == null) return;
        String maNganh = item.split(" - ")[0].trim();

        try {
            Nganh nganh = nganhDAO.findByMaNganh(maNganh);
            List<NguyenVongXetTuyen> nvList = nvDAO.findByMaNganh(maNganh);

            // Stats
            lblTongNV.setText(String.valueOf(nvList.size()));
            long trung = nvList.stream().filter(n -> "yes".equals(n.getNvKetqua())).count();
            lblTrungTuyen.setText(String.valueOf(trung));
            if (nganh != null && nganh.getnDiemtrungtuyen() != null) {
                lblDiemChuan.setText(nganh.getnDiemtrungtuyen().toPlainString());
            } else {
                lblDiemChuan.setText("Chưa xét");
            }

            // Table
            List<Object[]> rows = new ArrayList<>();
            int stt = 1;
            for (NguyenVongXetTuyen nv : nvList) {
                rows.add(new Object[]{
                    stt++, nv.getNnCccd(), nv.getNvTt(), nv.getTtPhuongthuc(),
                    nv.getDiemThxt(), nv.getDiemCong(), nv.getDiemUtqd(),
                    nv.getDiemXettuyen(), nv.getNvKetqua()
                });
            }
            styledTable.setData(rows);
        } catch (Exception e) {
            MessageHelper.showError(this, "Lỗi tải kết quả: " + e.getMessage());
        }
    }
}
