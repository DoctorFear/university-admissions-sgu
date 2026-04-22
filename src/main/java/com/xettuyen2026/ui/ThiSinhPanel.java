package com.xettuyen2026.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.xettuyen2026.entity.ThiSinh;
import com.xettuyen2026.service.ThiSinhService;
import com.xettuyen2026.ui.common.ConfirmDialog;
import com.xettuyen2026.ui.common.MessageHelper;
import com.xettuyen2026.ui.common.PaginatedTable;
import com.xettuyen2026.ui.common.RoundedButton;
import com.xettuyen2026.ui.common.SearchBar;
import com.xettuyen2026.ui.common.UIConstants;

/**
 * Quản lý Thí sinh - toolbar, search, table phân trang, CRUD + import Excel.
 */
public class ThiSinhPanel extends JPanel {

    private static final String[] COLUMNS = {
            "STT", "CCCD", "Số báo danh", "Họ và tên", "Ngày sinh",
            "Giới tính", "ĐTƯT", "KVƯT", "Dân tộc", "Nơi sinh"
    };

    private PaginatedTable styledTable;
    private SearchBar searchBar;
    private final ThiSinhService service;
    private boolean triedAutoLoadFromDataFile = false;

    public ThiSinhPanel() {
        service = new ThiSinhService();
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.BG_MAIN);

        add(createToolbar(), BorderLayout.NORTH);
        add(createTableCard(), BorderLayout.CENTER);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.setPreferredSize(new Dimension(0, 48));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        leftPanel.setOpaque(false);
        searchBar = new SearchBar("Tìm theo CCCD hoặc họ tên...", e -> doSearch());
        RoundedButton btnSearch = new RoundedButton(UIConstants.ICON_SEARCH + " Tìm kiếm", UIConstants.PRIMARY_LIGHT);
        btnSearch.addActionListener(e -> doSearch());
        leftPanel.add(searchBar);
        leftPanel.add(btnSearch);
        toolbar.add(leftPanel, BorderLayout.WEST);

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

        SwingUtilities.invokeLater(this::loadData);
        return card;
    }

    private void loadData() {
        try {
            // if (!triedAutoLoadFromDataFile) {
            //     triedAutoLoadFromDataFile = true;
            //     int imported = service.importDefaultDataFileIfPresent();
            //     if (imported > 0) {
            //         MessageHelper.showInfo(this, "Đã tự nạp " + imported + " thí sinh từ file Ds thi sinh.xlsx trong thư mục data.");
            //     }
            // }
            displayList(service.findAll());
        } catch (Exception e) {
            e.printStackTrace();
            MessageHelper.showError(this, "Không thể tải dữ liệu thí sinh:\n" + e.getMessage());
        }
    }

    private void doSearch() {
        String keyword = searchBar.getText().trim();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }
        try {
            displayList(service.search(keyword));
        } catch (Exception e) {
            MessageHelper.showError(this, "Lỗi tìm kiếm:\n" + e.getMessage());
        }
    }

    private void doImport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx, *.xls)", "xlsx", "xls"));
        chooser.setDialogTitle("Chọn file Excel import thí sinh");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                int count = service.importFromExcel(file);
                MessageHelper.showSuccess(this, "Import thành công " + count + " thí sinh!");
                loadData();
            } catch (Exception e) {
                MessageHelper.showError(this, "Lỗi import: " + e.getMessage());
            }
        }
    }

    private void doAdd() {
        ThiSinhDialog dlg = new ThiSinhDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            try {
                service.save(dlg.getEntity());
                MessageHelper.showSuccess(this, "Thêm thí sinh thành công!");
                loadData();
            } catch (Exception e) {
                MessageHelper.showError(this, "Lỗi: " + e.getMessage());
            }
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

        ThiSinh ts = service.findByCccd(cccd);
        if (ts != null) {
            ThiSinhDialog dlg = new ThiSinhDialog(SwingUtilities.getWindowAncestor(this), ts);
            dlg.setVisible(true);
            if (dlg.isSaved()) {
                try {
                    service.update(dlg.getEntity());
                    MessageHelper.showSuccess(this, "Cập nhật thí sinh thành công!");
                    loadData();
                } catch (Exception e) {
                    MessageHelper.showError(this, "Lỗi: " + e.getMessage());
                }
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
            int realIdx = styledTable.getRealIndex(row);
            List<Object[]> data = styledTable.getAllData();
            String cccd = (String) data.get(realIdx)[1];
            try {
                ThiSinh selected = service.findByCccd(cccd);
                if (selected != null) {
                    service.delete(selected);
                    MessageHelper.showSuccess(this, "Đã xóa thí sinh.");
                    loadData();
                }
            } catch (Exception e) {
                MessageHelper.showError(this, "Lỗi xóa: " + e.getMessage());
            }
        }
    }

    private void displayList(List<ThiSinh> list) {
        List<Object[]> rows = new ArrayList<>();
        int stt = 1;
        for (ThiSinh ts : list) {
            rows.add(new Object[]{
                    stt++,
                    ts.getCccd(),
                    ts.getSobaodanh(),
                    buildHoTen(ts),
                    ts.getNgaySinh(),
                    ts.getGioiTinh(),
                    ts.getDoiTuong(),
                    ts.getKhuVuc(),
                    ts.getDanToc(),
                    ts.getNoiSinh()
            });
        }
        styledTable.setData(rows);
    }

    private String buildHoTen(ThiSinh ts) {
        String ho = ts.getHo() == null ? "" : ts.getHo().trim();
        String ten = ts.getTen() == null ? "" : ts.getTen().trim();
        String fullName = (ho + " " + ten).trim();
        return fullName.isEmpty() ? ts.getCccd() : fullName;
    }
}
