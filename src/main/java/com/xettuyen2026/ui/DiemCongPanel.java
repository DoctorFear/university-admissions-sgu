package com.xettuyen2026.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.xettuyen2026.dao.DiemCongDAO;
import com.xettuyen2026.entity.DiemCongXetTuyen;
import com.xettuyen2026.service.DiemCongService;
import com.xettuyen2026.ui.common.ConfirmDialog;
import com.xettuyen2026.ui.common.MessageHelper;
import com.xettuyen2026.ui.common.PaginatedTable;
import com.xettuyen2026.ui.common.RoundedButton;
import com.xettuyen2026.ui.common.SearchBar;
import com.xettuyen2026.ui.common.UIConstants;

public class DiemCongPanel extends JPanel{

    final DiemCongDAO service;
    final DiemCongService logic;
    private PaginatedTable styledTable;
    protected SearchBar searchBar;
    private JComboBox<String> filterCombo;
    private static final String[] FILTER_COLUMNS = {
        "Tất cả",
        "CCCD",
        "Mã ngành",
        "Mã tổ hợp",
        "Phương thức",
        "Ghi chú",
        "Điểm chứng chỉ",
        "Điểm ưu tiên",
        "Điểm tổng",
        "Điểm cộng key"
    };
    private List<DiemCongXetTuyen> diemcongList = new ArrayList<>();

    private static final String[] COLUMNS = {
        "CCCD", "Điểm tổng", "Mã ngành", "Mã tổ hợp", "Phương thức", "Ghi chú", "Điểm cc", "Điểm ưu tiên", "Điểm cộng key"
    };

    public DiemCongPanel() {
        service = new DiemCongDAO();
        logic = new DiemCongService();
        setLayout(new BorderLayout(0, 10));
        setBackground(UIConstants.BG_MAIN);

        add(createToolbar(), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);

        loadData();
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.setPreferredSize(new Dimension(0, 48));

        // Left: Search
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        leftPanel.setOpaque(false);

        searchBar = new SearchBar("Tìm kiếm điểm cộng...", e -> doSearch());
        filterCombo = new JComboBox<>(FILTER_COLUMNS);
        filterCombo.setPreferredSize(new Dimension(120, 36));

        RoundedButton btnSearch = new RoundedButton(UIConstants.ICON_SEARCH + " Tìm kiếm", UIConstants.PRIMARY_LIGHT);
        btnSearch.addActionListener(e -> doSearch());
        // RoundedButton btnReset = new RoundedButton(UIConstants.ICON_SEARCH + " Reset", UIConstants.PRIMARY_LIGHT);
        // btnReset.addActionListener(e -> doReset());

        leftPanel.add(searchBar);
        leftPanel.add(filterCombo);
        // leftPanel.add(btnReset);
        leftPanel.add(btnSearch);
        toolbar.add(leftPanel, BorderLayout.WEST);

        // Right: Action buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        rightPanel.setOpaque(false);

        RoundedButton btnImport = new RoundedButton(UIConstants.ICON_IMPORT + " Import Excel", new Color(0x00796B));
        btnImport.addActionListener(e -> doImport());

        RoundedButton btnAdd = new RoundedButton(UIConstants.ICON_ADD + " Thêm mới", UIConstants.SUCCESS);
        btnAdd.addActionListener(e -> doAdd());

        RoundedButton btnEdit = new RoundedButton(UIConstants.ICON_EDIT + " Sửa", UIConstants.WARNING);
        btnEdit.addActionListener(e -> doEdit());

        RoundedButton btnDelete = new RoundedButton(UIConstants.ICON_DELETE + " Xóa", UIConstants.DANGER);
        btnDelete.addActionListener(e -> doDelete());

        rightPanel.add(btnImport);
        rightPanel.add(btnAdd);
        rightPanel.add(btnEdit);
        rightPanel.add(btnDelete);
        toolbar.add(rightPanel, BorderLayout.EAST);

        return toolbar;
    }

    private JPanel createTable() {
        JPanel card = new JPanel(new BorderLayout()) {
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
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        styledTable = new PaginatedTable(COLUMNS);
        card.add(styledTable, BorderLayout.CENTER);

        // Auto-load on first show
        SwingUtilities.invokeLater(this::loadData);

        return card;
    }

    private void loadData() {
        try {
            diemcongList = service.findAll();
            List<Object[]> rows = new ArrayList<>();

            for (DiemCongXetTuyen d : diemcongList) {
                rows.add(new Object[]{
                    d.getTsCccd(),
                    d.getDiemTong(),
                    d.getManganh(),
                    d.getMatohop(),
                    d.getPhuongthuc(),
                    d.getGhichu(),
                    d.getDiemCC(),
                    d.getDiemUtxt(),
                    d.getDcKeys()
                });
            }
            styledTable.setData(rows);
        } catch (Exception e) {
            System.err.println(e);
            MessageHelper.showError(this, "Không thể tải dữ liệu điểm cộng: " + e.getMessage());
        }
    }

    private void doAdd() {
        DiemCongDialog dlg = new DiemCongDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);

        if (dlg.isSaved()) {
            DiemCongXetTuyen e = dlg.getEntity();
            logic.prepare(e);
            service.save(e);
            loadData();
        }

        // if (dlg.isSaved()) {
        //     try {

        //         DiemCongXetTuyen e = dlg.getEntity();

        //         logic.validate(e);
        //         logic.prepare(e);

        //         service.save(e);

        //         loadData();

        //         MessageHelper.showSuccess(this, "Thêm thành công");

        //     } catch (Exception ex) {

        //         MessageHelper.showError(this, ex.getMessage());
        //     }
        // }
    }

    private void doEdit() {
        int row = styledTable.getSelectedRow();
        if (row < 0) return;

        int real = styledTable.getRealIndex(row);
        DiemCongXetTuyen selected = diemcongList.get(real);

        DiemCongDialog dlg = new DiemCongDialog(SwingUtilities.getWindowAncestor(this), selected);
        dlg.setVisible(true);

        if (dlg.isSaved()) {
            DiemCongXetTuyen e = dlg.getEntity();
            logic.prepare(e);
            service.update(e);
            loadData();
        }
        
        // if (dlg.isSaved()) {

        //     try {

        //         DiemCongXetTuyen e = dlg.getEntity();

        //         logic.validate(e);
        //         logic.prepare(e);

        //         service.update(e);

        //         loadData();

        //         MessageHelper.showSuccess(this, "Cập nhật thành công");

        //     } catch (Exception ex) {

        //         MessageHelper.showError(this, ex.getMessage());
        //     }
        // }
    }

    private void doDelete() {
        int row = styledTable.getSelectedRow();
        if (row < 0) return;

        int real = styledTable.getRealIndex(row);
        DiemCongXetTuyen selected = diemcongList.get(real);

        if (ConfirmDialog.show(this, "Delete điểm cộng của " + selected.getTsCccd() + "?")) {
            service.delete(selected.getIddiemcong());
            loadData();
        }
    }

    private String buildSearchableText(DiemCongXetTuyen d) {
        return String.join(" ",
            safe(d.getTsCccd()),
            safe(d.getManganh()),
            safe(d.getMatohop()),
            safe(d.getPhuongthuc()),
            safe(d.getGhichu()),
            safe(d.getDiemCC()),
            safe(d.getDiemUtxt()),
            safe(d.getDiemTong())
        ).toLowerCase();
    }

    
    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
    
    private String safe(BigDecimal b) {
        return b == null ? "" : b.toPlainString();
    }

    private void doSearch() {

        String keyword = searchBar.getText();

        if (keyword == null || keyword.isBlank()) {
            loadData();
            return;
        }

        keyword = keyword.toLowerCase().trim();

        String selected = (String) filterCombo.getSelectedItem();

        List<Object[]> rows = new ArrayList<>();

        for (DiemCongXetTuyen d : diemcongList) {

            boolean matched = false;

            switch (selected) {

                case "CCCD":
                    matched = safe(d.getTsCccd()).toLowerCase().contains(keyword);
                    break;

                case "Mã ngành":
                    matched = safe(d.getManganh()).toLowerCase().contains(keyword);
                    break;

                case "Mã tổ hợp":
                    matched = safe(d.getMatohop()).toLowerCase().contains(keyword);
                    break;

                case "Phương thức":
                    matched = safe(d.getPhuongthuc()).toLowerCase().contains(keyword);
                    break;

                case "Ghi chú":
                    matched = safe(d.getGhichu()).toLowerCase().contains(keyword);
                    break;

                case "Điểm cc":
                    matched = safe(d.getDiemCC()).toLowerCase().contains(keyword);
                    break;

                case "Điểm ưu tiên":
                    matched = safe(d.getDiemUtxt()).toLowerCase().contains(keyword);
                    break;

                case "Điểm tổng":
                    matched = safe(d.getDiemTong()).toLowerCase().contains(keyword);
                    break;

                case "Điểm cộng key":
                    matched = safe(d.getDcKeys()).toLowerCase().contains(keyword);
                    break;

                default:
                    matched = buildSearchableText(d).contains(keyword);
                    break;
            }

            if (matched) {
                rows.add(new Object[]{
                    d.getTsCccd(),
                    d.getDiemTong(),
                    d.getManganh(),
                    d.getMatohop(),
                    d.getPhuongthuc(),
                    d.getGhichu(),
                    d.getDiemCC(),
                    d.getDiemUtxt(),
                    d.getDcKeys()
                });
            }
        }

        styledTable.setData(rows);
    }
    
    private void doImport() {
        try {
            javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx, *.xls)", "xlsx", "xls"));
            int result = chooser.showOpenDialog(this);

            if (result != javax.swing.JFileChooser.APPROVE_OPTION) return;

            java.io.File file = chooser.getSelectedFile();

            // String[] options = {"Chứng chỉ (Tiếng Anh)", "Thí sinh", "Ưu tiên"};
            String[] options = {"Chứng chỉ (Tiếng Anh)", "Ưu tiên"};
            int type = javax.swing.JOptionPane.showOptionDialog(
                this,
                "Chọn loại dữ liệu import",
                "Import",
                javax.swing.JOptionPane.DEFAULT_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
            );

            DiemCongService serv = new DiemCongService();

            switch (type) {
                case 0 -> serv.importTiengAnh(file.getAbsolutePath());
                // case 1 -> serv.importThiSinh(file.getAbsolutePath());
                case 2 -> serv.importUuTien(file.getAbsolutePath());
                default -> { return; }
            }

            MessageHelper.showSuccess(this, "Import thành công");
            loadData();

        } catch (Exception e) {
            MessageHelper.showError(this, "Lỗi import: " + e.getMessage());
        }
    }

    private void doReset() {
        searchBar.setText("");
        loadData();
    }
}
