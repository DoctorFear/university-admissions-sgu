package com.xettuyen2026.ui;

import com.xettuyen2026.entity.TohopMonthi;
import com.xettuyen2026.service.TohopService;
import com.xettuyen2026.ui.common.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý Tổ hợp môn xét tuyển — CRUD + import + tìm kiếm.
 */
public class TohopPanel extends JPanel {

    private PaginatedTable styledTable;
    private SearchBar searchBar;
    private TohopService service;
    private List<TohopMonthi> loadedEntities = new ArrayList<>();
    private List<TohopMonthi> displayedEntities = new ArrayList<>();

    private static final String[] COLUMNS = {
        "STT", "Mã tổ hợp", "Môn 1", "Môn 2", "Môn 3", "Tên tổ hợp"
    };

    public TohopPanel() {
        service = new TohopService();
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

        searchBar = new SearchBar("Tìm theo mã tổ hợp hoặc tên...", e -> doSearch());

        RoundedButton btnSearch = new RoundedButton(UIConstants.ICON_SEARCH + " Tìm kiếm", UIConstants.PRIMARY_LIGHT);
        btnSearch.addActionListener(e -> doSearch());

        leftPanel.add(searchBar);
        leftPanel.add(btnSearch);
        toolbar.add(leftPanel, BorderLayout.WEST);

        // Right: Action buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        rightPanel.setOpaque(false);

        RoundedButton btnImport = new RoundedButton(UIConstants.ICON_IMPORT + " Import", new Color(0x00796B));
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

        SwingUtilities.invokeLater(this::loadData);
        return card;
    }

    private void loadData() {
        try {
            loadedEntities = service.findAll();
            displayEntities(loadedEntities);
        } catch (Exception e) {
            e.printStackTrace();
            MessageHelper.showError(this, "Không thể tải dữ liệu tổ hợp: " + e.getMessage());
        }
    }

    private void displayEntities(List<TohopMonthi> list) {
        displayedEntities = new ArrayList<>(list);
        List<Object[]> rows = new ArrayList<>();
        int stt = 1;
        for (TohopMonthi th : list) {
            rows.add(new Object[]{
                stt++, th.getMatohop(), th.getMon1(), th.getMon2(), th.getMon3(), th.getTentohop()
            });
        }
        styledTable.setData(rows);
    }

    private void doSearch() {
        String keyword = searchBar.getText().toLowerCase();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }
        List<TohopMonthi> filtered = new ArrayList<>();
        for (TohopMonthi th : loadedEntities) {
            boolean match = (th.getMatohop() != null && th.getMatohop().toLowerCase().contains(keyword))
                || (th.getTentohop() != null && th.getTentohop().toLowerCase().contains(keyword))
                || (th.getMon1() != null && th.getMon1().toLowerCase().contains(keyword))
                || (th.getMon2() != null && th.getMon2().toLowerCase().contains(keyword))
                || (th.getMon3() != null && th.getMon3().toLowerCase().contains(keyword));
            if (match) filtered.add(th);
        }
        displayEntities(filtered);
    }

    private void doImport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Excel / Text Files", "xlsx", "xls", "txt"));
        chooser.setDialogTitle("Chọn file import tổ hợp môn");
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
                MessageHelper.showSuccess(this, "Import thành công " + count + " tổ hợp mới.");
                loadData();
            } catch (Exception e) {
                e.printStackTrace();
                MessageHelper.showError(this, "Lỗi import: " + e.getMessage());
            }
        }
    }

    private void doAdd() {
        TohopDialog dlg = new TohopDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            try {
                service.save(dlg.getEntity());
                MessageHelper.showSuccess(this, "Thêm tổ hợp thành công!");
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
            MessageHelper.showWarning(this, "Vui lòng chọn một tổ hợp để sửa.");
            return;
        }
        int realIdx = styledTable.getRealIndex(row);
        if (realIdx >= displayedEntities.size()) return;

        TohopMonthi entity = displayedEntities.get(realIdx);
        if (entity == null) return;

        // Kiểm tra xem tổ hợp có được sử dụng không
        if (service.isUsedInNganhTohop(entity.getMatohop())) {
            int result = JOptionPane.showConfirmDialog(this,
                "Tổ hợp \"" + entity.getMatohop() + "\" đang được sử dụng bởi dữ liệu ngành-tổ hợp.\n\nBạn có chắc muốn tiếp tục sửa?",
                "Cảnh báo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }

        TohopDialog dlg = new TohopDialog(SwingUtilities.getWindowAncestor(this), entity);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            try {
                TohopMonthi updated = dlg.getEntity();
                updated.setIdtohop(entity.getIdtohop());
                service.update(updated);
                MessageHelper.showSuccess(this, "Cập nhật tổ hợp thành công!");
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
            MessageHelper.showWarning(this, "Vui lòng chọn một tổ hợp để xóa.");
            return;
        }
        int realIdx = styledTable.getRealIndex(row);
        if (realIdx >= displayedEntities.size()) return;

        TohopMonthi entity = displayedEntities.get(realIdx);
        if (entity == null) return;

        // Kiểm tra xem tổ hợp có được sử dụng không
        if (service.isUsedInNganhTohop(entity.getMatohop())) {
            MessageHelper.showError(this,
                "Không thể xóa vì tổ hợp \"" + entity.getMatohop() + "\" đang được sử dụng bởi dữ liệu ngành-tổ hợp.\n" +
                "Hãy xóa dữ liệu liên quan trước!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn xóa tổ hợp \"" + entity.getMatohop() + "\" không?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            service.delete(entity);
            MessageHelper.showSuccess(this, "Đã xóa tổ hợp \"" + entity.getMatohop() + "\".");
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            MessageHelper.showError(this, "Lỗi: " + e.getMessage());
        }
    }
}
