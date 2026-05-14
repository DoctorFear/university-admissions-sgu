package com.xettuyen2026.ui;

import com.xettuyen2026.entity.Nganh;
import com.xettuyen2026.entity.NganhTohop;
import com.xettuyen2026.dao.NganhDAO;
import com.xettuyen2026.service.NganhTohopService;
import com.xettuyen2026.ui.common.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Quản lý Ngành - Tổ hợp: hiển thị danh sách ngành-tổ hợp,
 * lọc theo mã ngành, CRUD + import từ tohopmon.txt.
 */
public class NganhTohopPanel extends JPanel {

    private PaginatedTable styledTable;
    private SearchBar searchBar;
    private JComboBox<String> cboFilterNganh;
    private NganhTohopService service;
    private NganhDAO nganhDAO;
    private List<NganhTohop> loadedEntities = new ArrayList<>();
    private List<NganhTohop> displayedEntities = new ArrayList<>();
    private Map<String, String> nganhNameMap = new LinkedHashMap<>();

    private static final String[] COLUMNS = {
        "STT", "Mã ngành", "Tên ngành", "Mã tổ hợp", "Môn 1", "HS1",
        "Môn 2", "HS2", "Môn 3", "HS3", "tb_keys",
        "N1", "TO", "LI", "HO", "SI", "VA", "SU", "DI", "TI", "KHAC", "KTPL",
        "Độ lệch"
    };
    private static final int COL_TB_KEYS = 10;

    public NganhTohopPanel() {
        service = new NganhTohopService();
        nganhDAO = new NganhDAO();
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.BG_MAIN);

        add(createToolbar(), BorderLayout.NORTH);
        add(createTableCard(), BorderLayout.CENTER);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setOpaque(false);
        toolbar.setPreferredSize(new Dimension(0, 48));

        // Left: Filter by nganh + search
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        leftPanel.setOpaque(false);

        JLabel lblFilter = new JLabel("Ngành:");
        lblFilter.setFont(UIConstants.FONT_BOLD);
        leftPanel.add(lblFilter);

        cboFilterNganh = new JComboBox<>();
        cboFilterNganh.setFont(UIConstants.FONT_REGULAR);
        cboFilterNganh.setPreferredSize(new Dimension(200, 34));
        cboFilterNganh.addActionListener(e -> doFilter());
        leftPanel.add(cboFilterNganh);

        searchBar = new SearchBar("Tìm mã tổ hợp...", e -> doSearch());
        searchBar.setPreferredSize(new Dimension(180, 34));
        leftPanel.add(searchBar);

        RoundedButton btnSearch = new RoundedButton(UIConstants.ICON_SEARCH + " Tìm", UIConstants.PRIMARY_LIGHT);
        btnSearch.addActionListener(e -> doSearch());
        leftPanel.add(btnSearch);

        toolbar.add(leftPanel, BorderLayout.CENTER);

        // Right: Action buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        rightPanel.setOpaque(false);

        RoundedButton btnImport = new RoundedButton(UIConstants.ICON_IMPORT + " Import", new Color(0x00796B));
        btnImport.addActionListener(e -> doImport());

        RoundedButton btnAdd = new RoundedButton(UIConstants.ICON_ADD + " Thêm", UIConstants.SUCCESS);
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

    private JPanel createTableCard() {
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

        SwingUtilities.invokeLater(this::loadData);
        return card;
    }

    private void loadNganhMap() {
        try {
            nganhNameMap.clear();
            List<Nganh> nganhList = nganhDAO.findAll();
            for (Nganh n : nganhList) {
                nganhNameMap.put(n.getManganh(), n.getTennganh());
            }
            // Populate combobox
            cboFilterNganh.removeAllItems();
            cboFilterNganh.addItem("-- Tất cả ngành --");
            for (Nganh n : nganhList) {
                cboFilterNganh.addItem(n.getManganh() + " - " + n.getTennganh());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        try {
            loadNganhMap();
            loadedEntities = service.findAll();
            displayEntities(loadedEntities);
        } catch (Exception e) {
            e.printStackTrace();
            MessageHelper.showError(this, "Không thể tải dữ liệu ngành-tổ hợp: " + e.getMessage());
        }
    }

    private void displayEntities(List<NganhTohop> list) {
        displayedEntities = new ArrayList<>(list);
        List<Object[]> rows = new ArrayList<>();
        int stt = 1;
        for (NganhTohop nt : list) {
            String tenNganh = nganhNameMap.getOrDefault(nt.getManganh(), "");
            rows.add(new Object[]{
                stt++,
                nt.getManganh(),
                tenNganh,
                nt.getMatohop(),
                nt.getThMon1(),
                nt.getHsmon1() != null ? nt.getHsmon1() : "",
                nt.getThMon2(),
                nt.getHsmon2() != null ? nt.getHsmon2() : "",
                nt.getThMon3(),
                nt.getHsmon3() != null ? nt.getHsmon3() : "",
                nt.getTbKeys(),
                boolDisplay(nt.getN1()),
                boolDisplay(nt.getTo()),
                boolDisplay(nt.getLi()),
                boolDisplay(nt.getHo()),
                boolDisplay(nt.getSi()),
                boolDisplay(nt.getVa()),
                boolDisplay(nt.getSu()),
                boolDisplay(nt.getDi()),
                boolDisplay(nt.getTi()),
                boolDisplay(nt.getKhac()),
                boolDisplay(nt.getKtpl()),
                nt.getDolech() != null ? nt.getDolech().toPlainString() : "0.00"
            });
        }
        styledTable.setData(rows);
    }

    private String boolDisplay(Boolean b) {
        return Boolean.TRUE.equals(b) ? "1" : "0";
    }

    private void doFilter() {
        if (cboFilterNganh.getSelectedIndex() <= 0) {
            displayEntities(loadedEntities);
            return;
        }
        String selected = (String) cboFilterNganh.getSelectedItem();
        String maNganh = selected.split(" - ")[0].trim();
        List<NganhTohop> filtered = loadedEntities.stream()
                .filter(nt -> nt.getManganh().equals(maNganh))
                .collect(Collectors.toList());
        displayEntities(filtered);
    }

    private void doSearch() {
        String keyword = searchBar.getText().toLowerCase();
        if (keyword.isEmpty()) {
            doFilter();
            return;
        }
        List<NganhTohop> source = loadedEntities;
        // Also respect the nganh filter
        if (cboFilterNganh.getSelectedIndex() > 0) {
            String selected = (String) cboFilterNganh.getSelectedItem();
            String maNganh = selected.split(" - ")[0].trim();
            source = loadedEntities.stream()
                    .filter(nt -> nt.getManganh().equals(maNganh))
                    .collect(Collectors.toList());
        }
        List<NganhTohop> filtered = source.stream()
                .filter(nt -> (nt.getMatohop() != null && nt.getMatohop().toLowerCase().contains(keyword))
                        || (nt.getManganh() != null && nt.getManganh().toLowerCase().contains(keyword))
                        || (nt.getTbKeys() != null && nt.getTbKeys().toLowerCase().contains(keyword))
                        || (nt.getThMon1() != null && nt.getThMon1().toLowerCase().contains(keyword))
                        || (nt.getThMon2() != null && nt.getThMon2().toLowerCase().contains(keyword))
                        || (nt.getThMon3() != null && nt.getThMon3().toLowerCase().contains(keyword)))
                .collect(Collectors.toList());
        displayEntities(filtered);
    }

    private void doImport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Excel / Text Files", "xlsx", "xls", "txt"));
        chooser.setDialogTitle("Chọn file import ngành-tổ hợp");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                int count;
                String name = file.getName().toLowerCase();
                if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
                    count = service.importFromExcel(file);
                } else {
                    count = service.importFromTohopMonFile(file);
                }
                MessageHelper.showSuccess(this, "Import thành công " + count + " bản ghi ngành-tổ hợp mới.");
                loadData();
            } catch (Exception e) {
                e.printStackTrace();
                MessageHelper.showError(this, "Lỗi import: " + e.getMessage());
            }
        }
    }

    private void doAdd() {
        NganhTohopDialog dlg = new NganhTohopDialog(SwingUtilities.getWindowAncestor(this), null, nganhNameMap);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            try {
                service.save(dlg.getEntity());
                MessageHelper.showSuccess(this, "Thêm ngành-tổ hợp thành công!");
                loadData();
            } catch (IllegalArgumentException e) {
                MessageHelper.showWarning(this, e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                MessageHelper.showError(this, "Lỗi: " + e.getMessage());
            }
        }
    }

    private void doEdit() {
        int row = styledTable.getSelectedRow();
        if (row < 0) {
            MessageHelper.showWarning(this, "Vui lòng chọn một dòng để sửa.");
            return;
        }
        int realIdx = styledTable.getRealIndex(row);
        if (realIdx >= displayedEntities.size()) return;

        NganhTohop entity = displayedEntities.get(realIdx);
        if (entity == null) return;

        NganhTohopDialog dlg = new NganhTohopDialog(SwingUtilities.getWindowAncestor(this), entity, nganhNameMap);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            try {
                NganhTohop updated = dlg.getEntity();
                updated.setId(entity.getId());
                service.update(updated);
                MessageHelper.showSuccess(this, "Cập nhật thành công!");
                loadData();
            } catch (IllegalArgumentException e) {
                MessageHelper.showWarning(this, e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                MessageHelper.showError(this, "Lỗi: " + e.getMessage());
            }
        }
    }

    private void doDelete() {
        int row = styledTable.getSelectedRow();
        if (row < 0) {
            MessageHelper.showWarning(this, "Vui lòng chọn một dòng để xóa.");
            return;
        }
        int realIdx = styledTable.getRealIndex(row);
        if (realIdx >= displayedEntities.size()) return;

        NganhTohop entity = displayedEntities.get(realIdx);
        if (entity == null) return;

        String displayInfo = entity.getManganh() + " - " + entity.getMatohop();
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn xóa ngành-tổ hợp \"" + displayInfo + "\" không?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            service.delete(entity);
            MessageHelper.showSuccess(this, "Đã xóa ngành-tổ hợp \"" + displayInfo + "\".");
            loadData();
        } catch (IllegalArgumentException e) {
            MessageHelper.showError(this, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            MessageHelper.showError(this, "Lỗi: " + e.getMessage());
        }
    }
}
