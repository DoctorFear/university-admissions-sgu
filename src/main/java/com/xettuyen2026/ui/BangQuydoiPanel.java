package com.xettuyen2026.ui;

import com.xettuyen2026.entity.BangQuydoi;
import com.xettuyen2026.service.BangQuydoiService;
import com.xettuyen2026.ui.common.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BangQuydoiPanel extends JPanel {

    private PaginatedTable styledTable;
    private SearchBar searchBar;
    private final BangQuydoiService service;

    private List<BangQuydoi> loadedEntities = new ArrayList<>();
    private List<BangQuydoi> allEntities = new ArrayList<>();

    private static final String[] COLUMNS = {
            "STT", "Phương thức", "Tổ hợp", "Môn thi",
            "Điểm a", "Điểm b", "Điểm c", "Điểm d",
            "Mã quy đổi", "Phân vị"
    };

    public BangQuydoiPanel() {
        service = new BangQuydoiService();
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.BG_MAIN);

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.setPreferredSize(new Dimension(0, 48));

        // Search
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        leftPanel.setOpaque(false);

        searchBar = new SearchBar("Tìm theo mã quy đổi, môn thi, tổ hợp...", e -> doSearch());

        RoundedButton btnSearch = new RoundedButton(
                UIConstants.ICON_SEARCH + " Tìm kiếm", UIConstants.PRIMARY_LIGHT);
        btnSearch.addActionListener(e -> doSearch());

        leftPanel.add(searchBar);
        leftPanel.add(btnSearch);
        toolbar.add(leftPanel, BorderLayout.WEST);

        // Action buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        rightPanel.setOpaque(false);

        RoundedButton btnImport = new RoundedButton(
                UIConstants.ICON_IMPORT + " Import Excel", new Color(0x00796B));
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

        SwingUtilities.invokeLater(this::loadData);
        return card;
    }

    // ====================== DATA ======================
    private void loadData() {
        try {
            allEntities = service.findAll();
            loadedEntities = new ArrayList<>(allEntities);
            displayEntities(loadedEntities);
        } catch (Exception e) {
            e.printStackTrace();
            MessageHelper.showError(this, "Không thể tải dữ liệu: " + e.getMessage());
        }
    }

    private void displayEntities(List<BangQuydoi> list) {
        List<Object[]> rows = new ArrayList<>();
        int stt = 1;
        for (BangQuydoi q : list) {
            rows.add(new Object[]{
                stt++,
                q.getdPhuongthuc(),
                q.getdTohop() != null ? q.getdTohop() : "-",
                q.getdMon() != null ? q.getdMon() : "-",
                q.getdDiema() != null ? q.getdDiema() : "",
                q.getdDiemb() != null ? q.getdDiemb() : "",
                q.getdDiemc() != null ? q.getdDiemc() : "",
                q.getdDiemd() != null ? q.getdDiemd() : "",
                q.getdMaquydoi(),
                q.getdPhanvi() != null ? q.getdPhanvi() : "-"
            });
        }
        styledTable.setData(rows);
    }

    // ====================== ACTIONS ======================
    private void doSearch() {
        String keyword = searchBar.getText().trim();
        if (keyword.isEmpty()) {
            loadedEntities = new ArrayList<>(allEntities);
        } else {
            loadedEntities = service.search(keyword);
        }
        displayEntities(loadedEntities);
    }

    private void doImport() {
        // Sử dụng ImportDialog bạn đã có
        ImportDialog importDlg = new ImportDialog(SwingUtilities.getWindowAncestor(this));
        importDlg.setVisible(true);

        if (importDlg.isConfirmed() && importDlg.getSelectedFile() != null) {
            try {
                int count = service.importFromExcel(importDlg.getSelectedFile());
                MessageHelper.showSuccess(this, "Import thành công " + count + " bản ghi quy đổi!");
                loadData();
            } catch (Exception e) {
                e.printStackTrace();
                MessageHelper.showError(this, "Lỗi khi import:\n" + e.getMessage());
            }
        }
    }

    private void doAdd() {
        BangQuydoiDialog dlg = new BangQuydoiDialog(
                SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);

        if (dlg.isSaved()) {
            try {
                service.save(dlg.getEntity());
                MessageHelper.showSuccess(this, "Thêm quy đổi thành công!");
                loadData();
            } catch (Exception e) {
                MessageHelper.showError(this, e.getMessage());
            }
        }
    }

    private void doEdit() {
        int row = styledTable.getSelectedRow();
        if (row < 0) {
            MessageHelper.showWarning(this, "Vui lòng chọn một bản ghi để sửa.");
            return;
        }

        int realIdx = styledTable.getRealIndex(row);
        BangQuydoi selected = loadedEntities.get(realIdx);

        if (ConfirmDialog.show(this,
                "Bạn đang sửa dữ liệu quy đổi chuẩn.\nThao tác này có thể ảnh hưởng đến việc tính điểm sau này.\n\nBạn vẫn muốn tiếp tục?")) {

            BangQuydoiDialog dlg = new BangQuydoiDialog(
                    SwingUtilities.getWindowAncestor(this), selected);
            dlg.setVisible(true);

            if (dlg.isSaved()) {
                try {
                    service.update(dlg.getEntity());
                    MessageHelper.showSuccess(this, "Cập nhật thành công!");
                    loadData();
                } catch (Exception e) {
                    MessageHelper.showError(this, e.getMessage());
                }
            }
        }
    }

    private void doDelete() {
        int row = styledTable.getSelectedRow();
        if (row < 0) {
            MessageHelper.showWarning(this, "Vui lòng chọn một bản ghi để xóa.");
            return;
        }

        int realIdx = styledTable.getRealIndex(row);
        BangQuydoi selected = loadedEntities.get(realIdx);

        if (ConfirmDialog.show(this,
                "Bạn có chắc chắn muốn xóa quy đổi này?\n\n" +
                "Mã: " + selected.getdMaquydoi() + "\n" +
                "Môn/Tổ hợp: " + (selected.getdMon() != null ? selected.getdMon() : selected.getdTohop()))) {

            try {
                service.delete(selected);
                MessageHelper.showSuccess(this, "Đã xóa thành công.");
                loadData();
            } catch (Exception e) {
                MessageHelper.showError(this, "Lỗi khi xóa: " + e.getMessage());
            }
        }
    }
}