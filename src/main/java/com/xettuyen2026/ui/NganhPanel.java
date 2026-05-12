package com.xettuyen2026.ui;

import com.xettuyen2026.entity.Nganh;
import com.xettuyen2026.service.NganhService;
import com.xettuyen2026.service.TohopService;
import com.xettuyen2026.ui.common.ConfirmDialog;
import com.xettuyen2026.ui.common.MessageHelper;
import com.xettuyen2026.ui.common.PaginatedTable;
import com.xettuyen2026.ui.common.RoundedButton;
import com.xettuyen2026.ui.common.SearchBar;
import com.xettuyen2026.ui.common.UIConstants;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
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

public class NganhPanel extends JPanel {

    private PaginatedTable styledTable;
    private SearchBar searchBar;
    private final NganhService service;
    private final TohopService tohopService;
    private RoundedButton btnImport;

    // Hiển thị danh sách ngành đang có trên bảng
    private List<Nganh> loadedEntities = new ArrayList<>();
    private List<Nganh> allEntities = new ArrayList<>();

    private static final String[] COLUMNS = {
            "STT", "Mã ngành", "Tên ngành", "Tổ hợp gốc",
            "Chỉ tiêu", "Điểm sàn", "Điểm T.Tuyển"
    };

    private static final int[] COLUMN_WIDTHS = {
            60, 120, 360, 130, 110, 120, 140
    };

    public NganhPanel() {
        service = new NganhService();
        tohopService = new TohopService();
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.BG_MAIN);

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
    }

    // Tạo thanh công cụ tìm kiếm và nhóm nút thao tác
    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.setPreferredSize(new Dimension(0, 48));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        leftPanel.setOpaque(false);

        searchBar = new SearchBar("Tìm theo mã ngành hoặc tên...", e -> doSearch());

        RoundedButton btnSearch = new RoundedButton(
                UIConstants.ICON_SEARCH + " Tìm kiếm", UIConstants.PRIMARY_LIGHT);
        btnSearch.addActionListener(e -> doSearch());

        leftPanel.add(searchBar);
        leftPanel.add(btnSearch);
        toolbar.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        rightPanel.setOpaque(false);

        btnImport = new RoundedButton(
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

    // Tạo khung chứa bảng dữ liệu ngành
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
        javax.swing.table.TableColumnModel columnModel = styledTable.getTable().getColumnModel();
        for (int i = 0; i < Math.min(COLUMN_WIDTHS.length, columnModel.getColumnCount()); i++) {
            columnModel.getColumn(i).setPreferredWidth(COLUMN_WIDTHS[i]);
        }
        card.add(styledTable, BorderLayout.CENTER);

        SwingUtilities.invokeLater(this::loadData);
        return card;
    }

    // Tải lại toàn bộ dữ liệu ngành từ service
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

    // Đổ danh sách ngành ra bảng hiển thị
    private void displayEntities(List<Nganh> list) {
        List<Object[]> rows = new ArrayList<>();
        int stt = 1;
        for (Nganh n : list) {
            rows.add(new Object[]{
                    stt++,
                    n.getManganh(),
                    n.getTennganh(),
                    n.getnTohopgoc() != null ? n.getnTohopgoc() : "",
                    n.getnChitieu() != null ? n.getnChitieu() : "",
                    n.getnDiemsan() != null ? n.getnDiemsan() : "",
                    n.getnDiemtrungtuyen() != null ? n.getnDiemtrungtuyen() : ""
            });
        }
        styledTable.setData(rows);
    }

    // Hiển thị số lượng chỉ tiêu của từng phương thức xét tuyển
    private String slText(boolean coXetTuyen, String sl) {
        if (!coXetTuyen) return "-";
        return (sl != null && !sl.isEmpty()) ? sl : "-";
    }

    // Tìm kiếm ngành theo mã hoặc tên
    private void doSearch() {
        try {
            String keyword = searchBar.getText().trim();
            loadedEntities = keyword.isEmpty()
                    ? new ArrayList<>(allEntities)
                    : new ArrayList<>(service.search(keyword));
            displayEntities(loadedEntities);
        } catch (Exception e) {
            MessageHelper.showError(this, "Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    // Import một file Nganh.xlsx và xử lý đủ 3 sheet trong service
    private void doImport() {
        File file = chooseImportFile("Chọn file Excel Nganh.xlsx");
        if (file == null) return;

        try {
            NganhService.ImportNganhResult result = service.importWorkbookNganh(file);
            showImportResult("ngành", result);
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            MessageHelper.showError(this, "Lỗi import ngành: " + e.getMessage());
        }
    }

    // Mở hộp thoại để chọn file Excel import ngành
    private File chooseImportFile(String dialogTitle) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Excel Files (*.xlsx, *.xls)", "xlsx", "xls"));
        chooser.setDialogTitle(dialogTitle);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    // Hiển thị kết quả sau khi import dữ liệu ngành
    private void showImportResult(String loaiImport, NganhService.ImportNganhResult result) {
        StringBuilder message = new StringBuilder();
        message.append("Import ").append(loaiImport).append(" hoàn tất!\n")
                .append("Cập nhật: ").append(result.updateCount).append("\n")
                .append("Bỏ qua: ").append(result.skipCount).append("\n")
                .append("Lỗi: ").append(result.errorCount);

        if (!result.skippedMa.isEmpty()) {
            message.append("\n\nMã ngành bỏ qua:\n")
                    .append(String.join(", ", result.skippedMa.subList(0, Math.min(10, result.skippedMa.size()))));
        }

        if (!result.errors.isEmpty()) {
            message.append("\n\nChi tiết lỗi:\n")
                    .append(String.join("\n", result.errors.subList(0, Math.min(5, result.errors.size()))));
        }

        MessageHelper.showInfo(this, message.toString());
    }

    // Mở dialog thêm mới ngành
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

    // Mở dialog chỉnh sửa ngành đang chọn
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

    // Xóa ngành đang chọn sau khi người dùng xác nhận
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

    // Lấy danh sách tổ hợp hợp lệ để truyền vào dialog ngành
    private List<String> getValidTohop() {
        try {
            return service.getValidTohopCheck();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // Chuyển số nguyên sang chuỗi để hiển thị trên bảng
    private String toStr(Integer v) {
        return v != null ? String.valueOf(v) : "";
    }
}
