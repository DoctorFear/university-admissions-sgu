package com.xettuyen2026.ui;

import com.xettuyen2026.entity.DiemThiXetTuyen;
import com.xettuyen2026.service.DiemThiService;
import com.xettuyen2026.ui.common.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiemThiPanel extends JPanel {

    private final DiemThiService service = new DiemThiService();

    // Luu toan bo data theo phuong thuc de tinh stats co dinh
    private final Map<String, List<DiemThiXetTuyen>> allDataMap = new HashMap<>();

    // Cot hien thi theo tung phuong thuc
    private static final String[] COL_THPT = {
        "STT", "CCCD", "SBD",
        "Toán", "Văn",
        "Lý", "Hóa", "Sinh",
        "Sử", "Địa", "GDCD",
        "NN(Thi)", "NN(CC)",
        "CN.CN", "CN.NN", "Tin", "KTPL",
        "Môn NK1", "Điểm NK1", "Môn NK2", "Điểm NK2"
    };
    private static final String[] COL_DGNL = {
        "STT", "CCCD", "SBD", "Điểm ĐGNL"
    };
    // TODO: cap nhat cot VSAT khi co thong tin tu thay
    private static final String[] COL_VSAT = {
        "STT", "CCCD", "SBD"
    };

    public DiemThiPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel lblTitle = new JLabel("Quản lý Điểm thi");
        lblTitle.setFont(UIConstants.FONT_HEADER);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        add(lblTitle, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(UIConstants.FONT_BOLD);
        tabs.setBackground(Color.WHITE);
        tabs.addTab("THPT",  createTabPanel("THPT",  COL_THPT));
        tabs.addTab("DGNL",  createTabPanel("DGNL",  COL_DGNL));
        tabs.addTab("V-SAT", createTabPanel("VSAT",  COL_VSAT));
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createTabPanel(String phuongThuc, String[] columns) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UIConstants.BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        PaginatedTable table = new PaginatedTable(columns);
        SearchBar searchBar  = new SearchBar("Tìm theo CCCD hoặc tên môn...", null);
        JLabel lblTB         = new JLabel("---");
        JLabel lblCao        = new JLabel("---");
        JLabel lblThap       = new JLabel("---");

        applyRenderer(table, phuongThuc);
        panel.add(buildToolbar(table, searchBar, phuongThuc, lblTB, lblCao, lblThap), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(12, 0));
        content.setOpaque(false);
        content.add(wrapCard(table), BorderLayout.CENTER);
        content.add(buildStatsPanel(lblTB, lblCao, lblThap), BorderLayout.EAST);
        panel.add(content, BorderLayout.CENTER);

        // Load du lieu lan dau
        SwingUtilities.invokeLater(() -> loadData(table, phuongThuc, lblTB, lblCao, lblThap));
        return panel;
    }

    // ─────────────────────────────────────────
    // TOOLBAR
    // ─────────────────────────────────────────

    private JPanel buildToolbar(PaginatedTable table, SearchBar searchBar,
                                String phuongThuc,
                                JLabel lblTB, JLabel lblCao, JLabel lblThap) {
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setOpaque(false);
        toolbar.setPreferredSize(new Dimension(0, 44));

        // Search: theo CCCD hoac ten mon, stats co dinh theo toan bo data
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        left.setOpaque(false);
        searchBar.getTextField().addActionListener(e -> {
            String kw = searchBar.getText().trim();
            try {
                if (kw.isEmpty()) {
                    // Khong co keyword -> hien thi toan bo + stats toan bo
                    List<DiemThiXetTuyen> all = allDataMap.getOrDefault(phuongThuc, new ArrayList<>());
                    fillTable(table, all, phuongThuc);
                    updateStats(all, phuongThuc, lblTB, lblCao, lblThap);
                } else {
                    // Co keyword -> loc bang + stats theo ket qua search
                    List<DiemThiXetTuyen> result = service.search(kw, phuongThuc);
                    fillTable(table, result, phuongThuc);
                    updateStats(result, phuongThuc, lblTB, lblCao, lblThap);
                }
            } catch (Exception ex) {
                MessageHelper.showError(this, "Lỗi tìm kiếm: " + ex.getMessage());
            }
        });
        left.add(searchBar);
        toolbar.add(left, BorderLayout.WEST);

        // Action buttons
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        right.setOpaque(false);

        RoundedButton btnImport = new RoundedButton(UIConstants.ICON_IMPORT + " Import Excel", new Color(0x00796B));
        btnImport.addActionListener(e -> doImport(table, phuongThuc, lblTB, lblCao, lblThap));

        RoundedButton btnAdd = new RoundedButton(UIConstants.ICON_ADD + " Thêm", UIConstants.SUCCESS);
        btnAdd.addActionListener(e -> doAdd(table, phuongThuc, lblTB, lblCao, lblThap));

        RoundedButton btnEdit = new RoundedButton(UIConstants.ICON_EDIT + " Sửa", UIConstants.WARNING);
        btnEdit.addActionListener(e -> doEdit(table, phuongThuc, lblTB, lblCao, lblThap));

        RoundedButton btnDelete = new RoundedButton(UIConstants.ICON_DELETE + " Xóa", UIConstants.DANGER);
        btnDelete.addActionListener(e -> doDelete(table, phuongThuc, lblTB, lblCao, lblThap));

        right.add(btnImport);
        right.add(btnAdd);
        right.add(btnEdit);
        right.add(btnDelete);
        toolbar.add(right, BorderLayout.EAST);
        return toolbar;
    }

    // ─────────────────────────────────────────
    // ACTIONS
    // ─────────────────────────────────────────

    private void doAdd(PaginatedTable table, String phuongThuc,
                       JLabel lblTB, JLabel lblCao, JLabel lblThap) {
        if ("VSAT".equals(phuongThuc)) {
            MessageHelper.showWarning(this, "Chức năng nhập điểm V-SAT chưa được hỗ trợ!");
            return;
        }
        DiemThiDialog dlg = new DiemThiDialog(SwingUtilities.getWindowAncestor(this), null, phuongThuc);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            try {
                service.save(dlg.getEntity());
                MessageHelper.showSuccess(this, "Thêm điểm thi thành công!");
                loadData(table, phuongThuc, lblTB, lblCao, lblThap);
            } catch (Exception e) {
                MessageHelper.showError(this, "Lỗi: " + e.getMessage());
            }
        }
    }

    private void doEdit(PaginatedTable table, String phuongThuc,
                        JLabel lblTB, JLabel lblCao, JLabel lblThap) {
        int row = table.getSelectedRow();
        if (row < 0) {
            MessageHelper.showWarning(this, "Vui lòng chọn một bản ghi để sửa.");
            return;
        }
        if ("VSAT".equals(phuongThuc)) {
            MessageHelper.showWarning(this, "Chức năng sửa điểm V-SAT chưa được hỗ trợ!");
            return;
        }
        String cccd = table.getTable().getValueAt(row, 1).toString();
        DiemThiXetTuyen selected = service.findByCccd(cccd);
        if (selected == null) return;

        DiemThiDialog dlg = new DiemThiDialog(SwingUtilities.getWindowAncestor(this), selected, phuongThuc);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            try {
                service.update(dlg.getEntity());
                MessageHelper.showSuccess(this, "Cập nhật điểm thi thành công!");
                loadData(table, phuongThuc, lblTB, lblCao, lblThap);
            } catch (Exception e) {
                MessageHelper.showError(this, "Lỗi: " + e.getMessage());
            }
        }
    }

    private void doDelete(PaginatedTable table, String phuongThuc,
                          JLabel lblTB, JLabel lblCao, JLabel lblThap) {
        int row = table.getSelectedRow();
        if (row < 0) {
            MessageHelper.showWarning(this, "Vui lòng chọn một bản ghi để xóa.");
            return;
        }
        String cccd = table.getTable().getValueAt(row, 1).toString();
        DiemThiXetTuyen selected = service.findByCccd(cccd);
        if (selected == null) return;

        if (ConfirmDialog.show(this, "Xóa điểm thi của CCCD: " + cccd + "?")) {
            try {
                service.delete(selected);
                MessageHelper.showSuccess(this, "Đã xóa thành công!");
                loadData(table, phuongThuc, lblTB, lblCao, lblThap);
            } catch (Exception e) {
                MessageHelper.showError(this, "Lỗi: " + e.getMessage());
            }
        }
    }

    private void doImport(PaginatedTable table, String phuongThuc,
                          JLabel lblTB, JLabel lblCao, JLabel lblThap) {
        if ("VSAT".equals(phuongThuc)) {
            MessageHelper.showWarning(this, "Chức năng import điểm V-SAT chưa được hỗ trợ!");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
        chooser.setDialogTitle("Chọn file Excel import điểm " + phuongThuc);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                int count = service.importFromExcel(file, phuongThuc);
                MessageHelper.showSuccess(this, "Import thành công " + count + " bản ghi!");
                loadData(table, phuongThuc, lblTB, lblCao, lblThap);
            } catch (Exception e) {
                MessageHelper.showError(this, "Lỗi import: " + e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────
    // DATA
    // ─────────────────────────────────────────

    // Load toan bo data, cap nhat allDataMap va stats
    private void loadData(PaginatedTable table, String phuongThuc,
                          JLabel lblTB, JLabel lblCao, JLabel lblThap) {
        try {
            List<DiemThiXetTuyen> list = service.findByPhuongThuc(phuongThuc);
            allDataMap.put(phuongThuc, list);
            fillTable(table, list, phuongThuc);
            updateStats(list, phuongThuc, lblTB, lblCao, lblThap);
        } catch (Exception e) {
            e.printStackTrace();
            MessageHelper.showError(this, "Không thể tải dữ liệu: " + e.getMessage());
        }
    }

    // Dien du lieu vao bang - thu tu phai khop voi COL tuong ung
    private void fillTable(PaginatedTable table,
                           List<DiemThiXetTuyen> list, String phuongThuc) {
        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        int stt = 1;
        for (DiemThiXetTuyen d : list) {
            if ("DGNL".equals(phuongThuc)) {
                rows.add(new Object[]{
                    stt++, d.getCccd(), nvlStr(d.getSobaodanh()),
                    fmt(d.getNl1())
                });
            } else if ("VSAT".equals(phuongThuc)) {
                // TODO: cap nhat khi co cau truc diem VSAT tu thay
                rows.add(new Object[]{ stt++, d.getCccd(), nvlStr(d.getSobaodanh()) });
            } else { // THPT
                rows.add(new Object[]{
                    stt++,                      // STT
                    d.getCccd(),                // CCCD
                    nvlStr(d.getSobaodanh()),   // SBD
                    fmt(d.getTo()),             // Toan
                    fmt(d.getVa()),             // Van
                    fmt(d.getLi()),             // Ly
                    fmt(d.getHo()),             // Hoa
                    fmt(d.getSi()),             // Sinh
                    fmt(d.getSu()),             // Su
                    fmt(d.getDi()),             // Dia
                    fmt(d.getGdcd()),           // GDCD
                    fmt(d.getN1Thi()),          // NN(Thi)
                    fmt(d.getN1Cc()),           // NN(CC)
                    fmt(d.getCncn()),           // CN.CN
                    fmt(d.getCnnn()),           // CN.NN
                    fmt(d.getTi()),             // Tin
                    fmt(d.getKtpl()),           // KTPL
                    nvlStr(d.getNkMon1()),      // Mon NK1
                    fmt(d.getNkDiem1()),        // Diem NK1
                    nvlStr(d.getNkMon2()),      // Mon NK2
                    fmt(d.getNkDiem2())         // Diem NK2
                });
            }
        }
        table.setData(rows);
    }

    // Stats luon tinh tren toan bo data (allDataMap), khong doi khi search
    private void updateStats(List<DiemThiXetTuyen> list, String phuongThuc,
                             JLabel lblTB, JLabel lblCao, JLabel lblThap) {
        DiemThiService.ThongKe tk = service.thongKe(list, phuongThuc);
        lblTB.setText(tk.diemTB     != null ? tk.diemTB.toPlainString()   : "---");
        lblCao.setText(tk.caoNhat   != null ? tk.caoNhat.toPlainString()  : "---");
        lblThap.setText(tk.thapNhat != null ? tk.thapNhat.toPlainString() : "---");
    }

    // ─────────────────────────────────────────
    // UI HELPERS
    // ─────────────────────────────────────────

    // Renderer: do neu diem = 0, xam neu "---", den neu co diem
    private void applyRenderer(PaginatedTable table, String phuongThuc) {
        table.getTable().setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? UIConstants.ROW_ODD : UIConstants.ROW_EVEN);
                setHorizontalAlignment(col >= 2 ? SwingConstants.CENTER : SwingConstants.LEFT);
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                if (col >= 3 && value != null) {
                    String val = value.toString().trim();
                    if ("0.00".equals(val) || "0".equals(val)) {
                        c.setForeground(UIConstants.DANGER);
                    } else if ("---".equals(val)) {
                        c.setForeground(UIConstants.TEXT_SECONDARY);
                    } else {
                        c.setForeground(UIConstants.TEXT_PRIMARY);
                    }
                }
                return c;
            }
        });
    }

    private JPanel wrapCard(JPanel content) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fillRoundRect(2, 2, getWidth()-2, getHeight()-2, 16, 16);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-2, getHeight()-2, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildStatsPanel(JLabel lblTB, JLabel lblCao, JLabel lblThap) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(180, 0));
        panel.setOpaque(false);
        panel.add(makeStat("Điểm TB",   lblTB,   UIConstants.STAT_BLUE));
        panel.add(Box.createVerticalStrut(8));
        panel.add(makeStat("Cao nhất",  lblCao,  UIConstants.STAT_GREEN));
        panel.add(Box.createVerticalStrut(8));
        panel.add(makeStat("Thấp nhất", lblThap, UIConstants.STAT_ORANGE));
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel makeStat(String label, JLabel valLbl, Color color) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(UIConstants.BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        card.setMaximumSize(new Dimension(180, 80));
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLbl.setForeground(color);
        valLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_SMALL);
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(valLbl);
        card.add(lbl);
        return card;
    }

    private String fmt(BigDecimal v) {
        return v != null && v.compareTo(BigDecimal.ZERO) != 0 ? v.toPlainString() : "---";
    }
    private String nvlStr(String s) { return s != null ? s : ""; }
}