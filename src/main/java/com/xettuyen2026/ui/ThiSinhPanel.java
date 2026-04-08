package com.xettuyen2026.ui;

import com.xettuyen2026.dao.ThiSinhDAO;
import com.xettuyen2026.entity.ThiSinh;
import com.xettuyen2026.service.ThiSinhImportService;
import com.xettuyen2026.ui.common.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý Thí sinh — toolbar, search, table phân trang, CRUD + import Excel.
 */
public class ThiSinhPanel extends JPanel {

    private PaginatedTable styledTable;
    private SearchBar searchBar;
    private ThiSinhDAO dao;
    private List<ThiSinh> loadedEntities = new ArrayList<>();

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

    // ── Toolbar ────────────────────────────────────────────────────────────────

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

    // ── Table card ─────────────────────────────────────────────────────────────

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

    // ── Data ───────────────────────────────────────────────────────────────────

    private void loadData() {
        try {
            loadedEntities = dao.findAll();
            styledTable.setData(toRows(loadedEntities));
        } catch (Exception e) {
            e.printStackTrace();
            MessageHelper.showError(this, "Không thể tải dữ liệu thí sinh:\n" + e.getMessage());
        }
    }

    private List<Object[]> toRows(List<ThiSinh> list) {
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
        return rows;
    }

    private void doSearch() {
        String keyword = searchBar.getText().trim();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }
        try {
            loadedEntities = dao.search(keyword);
            styledTable.setData(toRows(loadedEntities));
        } catch (Exception e) {
            MessageHelper.showError(this, "Lỗi tìm kiếm:\n" + e.getMessage());
        }
    }

    // ── Import Excel ───────────────────────────────────────────────────────────

    private void doImport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (.xlsx)", "xlsx"));
        chooser.setDialogTitle("Chọn file Excel danh sách thí sinh");
        // Mở thẳng thư mục data của dự án nếu có
        File dataDir = new File("src/main/data");
        if (dataDir.exists()) chooser.setCurrentDirectory(dataDir);

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        SwingWorker<ThiSinhImportService.ImportResult, Void> worker = new SwingWorker<>() {
            @Override
            protected ThiSinhImportService.ImportResult doInBackground() throws Exception {
                return new ThiSinhImportService().importFromExcel(file);
            }

            @Override
            protected void done() {
                try {
                    ThiSinhImportService.ImportResult r = get();

                    StringBuilder msg = new StringBuilder();
                    msg.append("Import hoàn tất!\n");
                    msg.append("✅ Thêm mới : ").append(r.insertCount).append(" bản ghi\n");
                    msg.append("🔄 Cập nhật : ").append(r.updateCount).append(" bản ghi\n");
                    msg.append("⏭️ Bỏ qua  : ").append(r.skipCount).append(" dòng\n");
                    msg.append("❌ Lỗi     : ").append(r.errorCount).append(" dòng");

                    if (!r.errors.isEmpty()) {
                        msg.append("\n\nChi tiết lỗi:\n");
                        int show = Math.min(8, r.errors.size());
                        for (int i = 0; i < show; i++) msg.append("• ").append(r.errors.get(i)).append("\n");
                        if (r.errors.size() > show)
                            msg.append("... và ").append(r.errors.size() - show).append(" lỗi khác.");
                    }

                    if (r.errorCount == 0) {
                        MessageHelper.showSuccess(ThiSinhPanel.this, msg.toString());
                    } else {
                        MessageHelper.showInfo(ThiSinhPanel.this, msg.toString());
                    }

                } catch (Exception e) {
                    MessageHelper.showError(ThiSinhPanel.this, "Lỗi import:\n" + e.getMessage());
                } finally {
                    loadData();
                }
            }
        };
        worker.execute();
    }

    // ── CRUD ───────────────────────────────────────────────────────────────────

    private void doAdd() {
        ThiSinhDialog dlg = new ThiSinhDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isSaved()) loadData();
    }

    private void doEdit() {
        int row = styledTable.getSelectedRow();
        if (row < 0) {
            MessageHelper.showWarning(this, "Vui lòng chọn một thí sinh để sửa.");
            return;
        }
        int realIdx = styledTable.getRealIndex(row);
        if (realIdx >= 0 && realIdx < loadedEntities.size()) {
            ThiSinh ts = loadedEntities.get(realIdx);
            ThiSinhDialog dlg = new ThiSinhDialog(SwingUtilities.getWindowAncestor(this), ts);
            dlg.setVisible(true);
            if (dlg.isSaved()) loadData();
        }
    }

    private void doDelete() {
        int row = styledTable.getSelectedRow();
        if (row < 0) {
            MessageHelper.showWarning(this, "Vui lòng chọn một thí sinh để xóa.");
            return;
        }
        int realIdx = styledTable.getRealIndex(row);
        if (realIdx < 0 || realIdx >= loadedEntities.size()) return;

        ThiSinh ts = loadedEntities.get(realIdx);
        String label = (ts.getHo() != null ? ts.getHo() + " " : "") + (ts.getTen() != null ? ts.getTen() : ts.getCccd());

        if (ConfirmDialog.show(this, "Bạn có chắc muốn xóa thí sinh:\n" + label + "?")) {
            try {
                dao.delete(ts);
                MessageHelper.showSuccess(this, "Đã xóa thí sinh " + label + ".");
                loadData();
            } catch (Exception e) {
                MessageHelper.showError(this, "Lỗi xóa thí sinh:\n" + e.getMessage());
            }
        }
    }
}
