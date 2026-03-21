package com.xettuyen2026.ui;

import com.xettuyen2026.entity.Nganh;
import com.xettuyen2026.service.*;
import com.xettuyen2026.ui.common.*;
 
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NganhPanel extends JPanel {

	private PaginatedTable styledTable;
    private SearchBar searchBar;
    private final NganhService service;
    private final TohopService tohopService;
    
    // Hien thi ds nganh 
    private List<Nganh> loadedEntities = new ArrayList<>();
    private List<Nganh> allEntities = new ArrayList<>();
    
    private static final String[] COLUMNS = {
            "STT", "Mã ngành", "Tên ngành", "Tổ hợp gốc",
            "Chỉ tiêu", "Điểm sàn", "Điểm T.Tuyển",
            "T.Thẳng", "ĐGNL", "THPT", "V-SAT"
        };
    
    public NganhPanel() {
        service = new NganhService();
        tohopService = new TohopService();
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
 
        add(buildHeader(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
    }
    
    // ------------------------------------
    // GIAO DIEN
    // ------------------------------------
    
    // Tieu de trang + toolbar tim kiem + button
    private JPanel buildHeader() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);
 
        // Page title
        JLabel lblTitle = new JLabel("Quản lý Ngành tuyển sinh");
        lblTitle.setFont(UIConstants.FONT_HEADER);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);
        wrapper.add(lblTitle, BorderLayout.NORTH);
 
        // Toolbar
        wrapper.add(buildToolbar(), BorderLayout.CENTER);
        return wrapper;
    }
 
    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.setPreferredSize(new Dimension(0, 48));
 
        // Left: Search
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        leftPanel.setOpaque(false);
 
        searchBar = new SearchBar("Tìm theo mã ngành hoặc tên...", e -> doSearch());
 
        RoundedButton btnSearch = new RoundedButton(
                UIConstants.ICON_SEARCH + " Tìm kiếm", UIConstants.PRIMARY_LIGHT);
        btnSearch.addActionListener(e -> doSearch());
 
        leftPanel.add(searchBar);
        leftPanel.add(btnSearch);
        toolbar.add(leftPanel, BorderLayout.WEST);
 
        // Right: Action buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        rightPanel.setOpaque(false);
 
        RoundedButton btnImport = new RoundedButton(
                UIConstants.ICON_IMPORT + " Import", new Color(0x00796B));
        btnImport.addActionListener(e -> doImport());
 
        RoundedButton btnAdd = new RoundedButton(
                UIConstants.ICON_ADD + " Thêm mới", UIConstants.SUCCESS);
        btnAdd.addActionListener(e -> doAdd());
 
        RoundedButton btnEdit = new RoundedButton(
                UIConstants.ICON_EDIT + " Sửa", UIConstants.WARNING);
        btnEdit.addActionListener(e -> doEdit());
 
        RoundedButton btnDelete = new RoundedButton(
                UIConstants.ICON_DELETE + " Xóa", UIConstants.DANGER);
        btnDelete.addActionListener(e -> doDelete());
 
        rightPanel.add(btnImport);
        rightPanel.add(btnAdd);
        rightPanel.add(btnEdit);
        rightPanel.add(btnDelete);
        toolbar.add(rightPanel, BorderLayout.EAST);
 
        return toolbar;
    }
    
    // Card chua bang du lieu
    private JPanel buildTableCard() {
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
 
        // Load dữ liệu sau khi UI xong
        SwingUtilities.invokeLater(this::loadData);
        return card;
    }
    
    // ------------------------
    // DATA
    // ------------------------
    
    private void loadData() {
        try {
            allEntities = service.findAll();
            loadedEntities = new ArrayList<>(allEntities);
            displayEntities(loadedEntities);
        } catch (Exception e) {
            e.printStackTrace();
            MessageHelper.showError(this, "Không thể tải dữ liệu ngành: " + e.getMessage());
        }
    }
 
    private void displayEntities(List<Nganh> list) {
        List<Object[]> rows = new ArrayList<>();
        int stt = 1;
        for (Nganh n : list) {
            rows.add(new Object[]{
                stt++,
                n.getManganh(),
                n.getTennganh(),
                n.getnTohopgoc() != null ? n.getnTohopgoc() : "",
                n.getnChitieu() != null  ? n.getnChitieu()  : "",
                n.getnDiemsan() != null  ? n.getnDiemsan()  : "",
                n.getnDiemtrungtuyen() != null ? n.getnDiemtrungtuyen() : "",
                slText("1".equals(n.getnTuyenthang()), toStr(n.getSlXtt())),
                slText("1".equals(n.getnDgnl()), toStr(n.getSlDgnl())),
                slText("1".equals(n.getnThpt()), n.getSlThpt()),
                slText("1".equals(n.getnVsat()), toStr(n.getSlVsat()))
            });
        }
        styledTable.setData(rows);
    }
    // Hien thi so luong chi tieu tung phuong thuc
    private String slText(boolean coXetTuyen, String sl) {
        if (!coXetTuyen) return "-";
        return (sl != null && !sl.isEmpty()) ? sl : "-";
    }
    
    // --------------------
    // ACTION
    // --------------------
    
    private void doSearch() {
        String keyword = searchBar.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadedEntities = new ArrayList<>(allEntities);
        } else {
            loadedEntities = new ArrayList<>();
            for (Nganh n : allEntities) {
                boolean match =
                    (n.getManganh() != null && n.getManganh().toLowerCase().contains(keyword)) ||
                    (n.getTennganh() != null && n.getTennganh().toLowerCase().contains(keyword));
                if (match) loadedEntities.add(n);
            }
        }
        displayEntities(loadedEntities);
    }
 
    private void doImport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Excel Files (*.xlsx, *.xls)", "xlsx", "xls"));
        chooser.setDialogTitle("Chọn file Excel import ngành tuyển sinh");
 
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                int count = service.importFromExcel(file);
                MessageHelper.showSuccess(this, "Import thành công " + count + " ngành!");
                loadData();
            } catch (Exception e) {
                e.printStackTrace();
                MessageHelper.showError(this, "Lỗi import: " + e.getMessage());
            }
        }
    }
 
    private void doAdd() {
    	NganhDialog dlg = new NganhDialog(
    	        SwingUtilities.getWindowAncestor(this), null, getValidTohop());
        dlg.setVisible(true);
 
        if (dlg.isSaved()) {
            try {
                service.save(dlg.getEntity());
                MessageHelper.showSuccess(this, "Thêm ngành thành công!");
                loadData();
            } catch (Exception e) {
                MessageHelper.showError(this, "Lỗi: " + e.getMessage());
            }
        }
    }
 
    private void doEdit() {
        int row = styledTable.getSelectedRow();
        if (row < 0) {
            MessageHelper.showWarning(this, "Vui lòng chọn một ngành để sửa.");
            return;
        }
 
        int realIdx = styledTable.getRealIndex(row);
        if (realIdx >= loadedEntities.size()) return;
 
        Nganh selected = loadedEntities.get(realIdx);
        NganhDialog dlg = new NganhDialog(
        	    SwingUtilities.getWindowAncestor(this), selected, getValidTohop());
        dlg.setVisible(true);
 
        if (dlg.isSaved()) {
            try {
                service.update(dlg.getEntity());
                MessageHelper.showSuccess(this, "Cập nhật ngành thành công!");
                loadData();
            } catch (Exception e) {
                MessageHelper.showError(this, "Lỗi: " + e.getMessage());
            }
        }
    }
 
    private void doDelete() {
        int row = styledTable.getSelectedRow();
        if (row < 0) {
            MessageHelper.showWarning(this, "Vui lòng chọn một ngành để xóa.");
            return;
        }
 
        int realIdx = styledTable.getRealIndex(row);
        if (realIdx >= loadedEntities.size()) return;
 
        Nganh selected = loadedEntities.get(realIdx);
        String info = selected.getManganh() + " - " + selected.getTennganh();
 
        if (ConfirmDialog.show(this, "Bạn có chắc muốn xóa ngành:\n" + info + "?")) {
            try {
                service.delete(selected);
                MessageHelper.showSuccess(this, "Đã xóa ngành " + selected.getManganh() + ".");
                loadData();
            } catch (Exception e) {
                MessageHelper.showError(this, "Lỗi: " + e.getMessage());
            }
        }
    }
    
    private List<String> getValidTohop() {
        try {
            return service.getValidTohopCheck();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    private Object nvl(Object val)  { return val != null ? val : ""; }
    private String toStr(Integer v) { return v != null ? String.valueOf(v) : ""; }
}
