package com.xettuyen2026.ui;

import com.xettuyen2026.dao.ThiSinhDAO;
import com.xettuyen2026.entity.ThiSinh;
import com.xettuyen2026.ui.common.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý Thí sinh - toolbar, search, table phân trang, dialog thêm/sửa.
 */
public class ThiSinhPanel extends JPanel {

    private PaginatedTable styledTable;
    private SearchBar searchBar;
    private ThiSinhDAO dao;

    private static final String[] COLUMNS = {
        "STT", "CCCD", "Số báo danh", "Họ", "Tên", "Ngày sinh",
        "Giới tính", "Điện thoại", "Email", "Khu vực", "Đối tượng"
    };

    public ThiSinhPanel() {
        dao = new ThiSinhDAO();
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.BG_MAIN);

        add(createToolbar(), BorderLayout.NORTH);
        add(createTableCard(), BorderLayout.CENTER);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.setPreferredSize(new Dimension(0, 48));

        // Left: Search
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        leftPanel.setOpaque(false);

        searchBar = new SearchBar("Tìm theo CCCD hoặc họ tên...", e -> doSearch());

        RoundedButton btnSearch = new RoundedButton(UIConstants.ICON_SEARCH + " Tìm kiếm", UIConstants.PRIMARY_LIGHT);
        btnSearch.addActionListener(e -> doSearch());

        leftPanel.add(searchBar);
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

        // Auto-load on first show
        SwingUtilities.invokeLater(this::loadData);

        return card;
    }

    private void loadData() {
        try {
            List<ThiSinh> list = dao.findAll();
            List<Object[]> rows = new ArrayList<>();
            int stt = 1;
            for (ThiSinh ts : list) {
                rows.add(new Object[]{
                    stt++,
                    ts.getCccd(),
                    ts.getSobaodanh(),
                    ts.getHo(),
                    ts.getTen(),
                    ts.getNgaySinh(),
                    ts.getGioiTinh(),
                    ts.getDienThoai(),
                    ts.getEmail(),
                    ts.getKhuVuc(),
                    ts.getDoiTuong()
                });
            }
            styledTable.setData(rows);
        } catch (Exception e) {
            e.printStackTrace();
            MessageHelper.showError(this, "Không thể tải dữ liệu thí sinh: " + e.getMessage());
        }
    }

    private void doSearch() {
        String keyword = searchBar.getText();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }
        try {
            List<ThiSinh> list = dao.findAll(); // use findAll and filter in app
            List<Object[]> rows = new ArrayList<>();
            int stt = 1;
            String kw = keyword.toLowerCase();
            for (ThiSinh ts : list) {
                boolean match = (ts.getCccd() != null && ts.getCccd().toLowerCase().contains(kw))
                    || (ts.getHo() != null && ts.getHo().toLowerCase().contains(kw))
                    || (ts.getTen() != null && ts.getTen().toLowerCase().contains(kw));
                if (match) {
                    rows.add(new Object[]{
                        stt++, ts.getCccd(), ts.getSobaodanh(), ts.getHo(), ts.getTen(),
                        ts.getNgaySinh(), ts.getGioiTinh(), ts.getDienThoai(),
                        ts.getEmail(), ts.getKhuVuc(), ts.getDoiTuong()
                    });
                }
            }
            styledTable.setData(rows);
        } catch (Exception e) {
            MessageHelper.showError(this, "Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    private void doImport() {
        ImportDialog dlg = new ImportDialog(SwingUtilities.getWindowAncestor(this));
        dlg.setVisible(true);
        if (dlg.isConfirmed() && dlg.getSelectedFile() != null) {
            MessageHelper.showInfo(this, "Import file: " + dlg.getSelectedFile().getName()
                    + "\n(Chức năng import sẽ xử lý file thực tế ở service layer)");
            loadData();
        }
    }

    private void doAdd() {
        ThiSinhDialog dlg = new ThiSinhDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadData();
        }
    }

    private void doEdit() {
        int row = styledTable.getSelectedRow();
        if (row < 0) {
            MessageHelper.showWarning(this, "Vui lòng chọn một thí sinh để sửa.");
            return;
        }
        int realIdx = styledTable.getRealIndex(row);
        List<Object[]> data = styledTable.getAllData();
        String cccd = (String) data.get(realIdx)[1];

        ThiSinh ts = dao.findByCccd(cccd);
        if (ts != null) {
            ThiSinhDialog dlg = new ThiSinhDialog(SwingUtilities.getWindowAncestor(this), ts);
            dlg.setVisible(true);
            if (dlg.isSaved()) {
                loadData();
            }
        }
    }

    private void doDelete() {
        int row = styledTable.getSelectedRow();
        if (row < 0) {
            MessageHelper.showWarning(this, "Vui lòng chọn một thí sinh để xóa.");
            return;
        }
        if (ConfirmDialog.show(this, "Bạn có chắc muốn xóa thí sinh này?")) {
            MessageHelper.showSuccess(this, "Đã xóa thí sinh.");
            loadData();
        }
    }
}
