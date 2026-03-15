package com.xettuyen2026.ui;

import com.xettuyen2026.dao.NguyenVongDAO;
import com.xettuyen2026.dao.NganhDAO;
import com.xettuyen2026.entity.Nganh;
import com.xettuyen2026.entity.NguyenVongXetTuyen;
import com.xettuyen2026.service.AdmissionService;
import com.xettuyen2026.service.NguyenVongImportService;
import com.xettuyen2026.ui.common.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Quản lý Nguyện vọng & Xét tuyển — full CRUD + tính điểm + chạy xét tuyển.
 */
public class NguyenVongPanel extends JPanel {

    private PaginatedTable styledTable;
    private SearchBar searchBar;
    private NguyenVongDAO dao;
    private NganhDAO nganhDAO;
    private AdmissionService admissionService;
    private List<NguyenVongXetTuyen> loadedEntities = new ArrayList<>();

    private static final String[] COLUMNS = {
        "STT", "CCCD", "Mã ngành", "Tên ngành", "NV Thứ", "PT",
        "Điểm THXT", "Điểm Cộng", "Điểm ƯT", "Điểm XT", "Kết quả"
    };

    public NguyenVongPanel() {
        dao = new NguyenVongDAO();
        nganhDAO = new NganhDAO();
        admissionService = new AdmissionService();
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.BG_MAIN);

        add(createToolbar(), BorderLayout.NORTH);
        add(createTableCard(), BorderLayout.CENTER);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
        toolbar.setOpaque(false);

        // ── Row 1: Search + Load ──
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        row1.setOpaque(false);
        searchBar = new SearchBar("Tìm theo CCCD...", e -> doSearch());
        RoundedButton btnSearch = new RoundedButton(UIConstants.ICON_SEARCH + " Tìm", UIConstants.PRIMARY);
        btnSearch.addActionListener(e -> doSearch());
        RoundedButton btnLoad = new RoundedButton("📋 Tải DS", UIConstants.PRIMARY);
        btnLoad.addActionListener(e -> loadData());
        row1.add(searchBar);
        row1.add(btnSearch);
        row1.add(btnLoad);
        toolbar.add(row1);

        // ── Row 2: Action buttons ──
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        row2.setOpaque(false);

        RoundedButton btnImport = new RoundedButton(UIConstants.ICON_IMPORT + " Import Excel", new Color(0x00796B));
        btnImport.addActionListener(e -> doImport());

        RoundedButton btnAdd = new RoundedButton(UIConstants.ICON_ADD + " Thêm NV", UIConstants.SUCCESS);
        btnAdd.addActionListener(e -> doAdd());

        RoundedButton btnEdit = new RoundedButton(UIConstants.ICON_EDIT + " Sửa", UIConstants.WARNING);
        btnEdit.addActionListener(e -> doEdit());

        RoundedButton btnDelete = new RoundedButton(UIConstants.ICON_DELETE + " Xóa", UIConstants.DANGER);
        btnDelete.addActionListener(e -> doDelete());

        // Separator
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(2, 24));
        sep.setForeground(UIConstants.BORDER_LIGHT);

        RoundedButton btnCalc = new RoundedButton("🧮 Tính điểm ĐXT", new Color(0x6A1B9A));
        btnCalc.addActionListener(e -> calculateScores());

        RoundedButton btnRun = new RoundedButton("🚀 Chạy xét tuyển", UIConstants.STAT_GREEN);
        btnRun.addActionListener(e -> runAdmission());

        row2.add(btnImport);
        row2.add(btnAdd);
        row2.add(btnEdit);
        row2.add(btnDelete);
        row2.add(Box.createHorizontalStrut(6));
        row2.add(sep);
        row2.add(Box.createHorizontalStrut(6));
        row2.add(btnCalc);
        row2.add(btnRun);
        toolbar.add(row2);

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

        styledTable.getTable().setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? UIConstants.ROW_ODD : UIConstants.ROW_EVEN);
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

                // Color-code kết quả (col 10)
                if (col == 10 && value != null) {
                    String val = value.toString();
                    if ("yes".equals(val)) {
                        c.setForeground(UIConstants.SUCCESS);
                        ((JLabel) c).setFont(UIConstants.FONT_BOLD);
                        ((JLabel) c).setText("✅ Trúng tuyển");
                    } else if ("duolaar".equals(val)) {
                        c.setForeground(UIConstants.DANGER);
                        ((JLabel) c).setFont(UIConstants.FONT_BOLD);
                        ((JLabel) c).setText("❌ Rớt");
                    } else {
                        c.setForeground(UIConstants.TEXT_SECONDARY);
                    }
                } else {
                    c.setForeground(UIConstants.TEXT_PRIMARY);
                }

                if (col >= 6 && col <= 9) setHorizontalAlignment(SwingConstants.CENTER);
                else setHorizontalAlignment(SwingConstants.LEFT);

                return c;
            }
        });

        card.add(styledTable, BorderLayout.CENTER);
        SwingUtilities.invokeLater(this::loadData);
        return card;
    }

    // ── Data loading ──

    private Map<String, String> nganhNameMap;

    private String getNganhName(String maNganh) {
        if (nganhNameMap == null) {
            try {
                nganhNameMap = nganhDAO.findAll().stream()
                    .collect(Collectors.toMap(Nganh::getManganh, Nganh::getTennganh, (a, b) -> a));
            } catch (Exception e) {
                nganhNameMap = java.util.Collections.emptyMap();
            }
        }
        return nganhNameMap.getOrDefault(maNganh, maNganh);
    }

    private void loadData() {
        try {
            nganhNameMap = null; // refresh
            loadedEntities = dao.findAllOrdered();
            List<Object[]> rows = new ArrayList<>();
            int stt = 1;
            for (NguyenVongXetTuyen nv : loadedEntities) {
                rows.add(mapToRow(nv, stt++));
            }
            styledTable.setData(rows);
        } catch (Exception e) {
            e.printStackTrace();
            MessageHelper.showError(this, "Không thể tải danh sách nguyện vọng:\n" + e.getMessage());
        }
    }

    private Object[] mapToRow(NguyenVongXetTuyen nv, int stt) {
        return new Object[]{
            stt,
            nv.getNnCccd(),
            nv.getNvManganh(),
            getNganhName(nv.getNvManganh()),
            nv.getNvTt(),
            nv.getTtPhuongthuc(),
            nv.getDiemThxt(),
            nv.getDiemCong(),
            nv.getDiemUtqd(),
            nv.getDiemXettuyen(),
            nv.getNvKetqua()
        };
    }

    private void doSearch() {
        String kw = searchBar.getText();
        if (kw.isEmpty()) { loadData(); return; }
        try {
            loadedEntities = dao.findByCccd(kw);
            List<Object[]> rows = new ArrayList<>();
            int stt = 1;
            for (NguyenVongXetTuyen nv : loadedEntities) rows.add(mapToRow(nv, stt++));
            styledTable.setData(rows);
        } catch (Exception e) {
            MessageHelper.showError(this, "Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    // ── CRUD ──

    private void doAdd() {
        NguyenVongDialog dlg = new NguyenVongDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isSaved()) loadData();
    }

    private void doEdit() {
        int row = styledTable.getSelectedRow();
        if (row < 0) { MessageHelper.showWarning(this, "Vui lòng chọn nguyện vọng."); return; }
        int realIdx = styledTable.getRealIndex(row);
        if (realIdx >= 0 && realIdx < loadedEntities.size()) {
            NguyenVongXetTuyen nv = loadedEntities.get(realIdx);
            NguyenVongDialog dlg = new NguyenVongDialog(SwingUtilities.getWindowAncestor(this), nv);
            dlg.setVisible(true);
            if (dlg.isSaved()) loadData();
        }
    }

    private void doImport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (.xlsx)", "xlsx"));
        chooser.setDialogTitle("Chọn file Excel nguyện vọng");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        SwingWorker<NguyenVongImportService.ImportResult, Void> worker = new SwingWorker<>() {
            @Override
            protected NguyenVongImportService.ImportResult doInBackground() throws Exception {
                NguyenVongImportService importService = new NguyenVongImportService();
                return importService.importFromExcel(file);
            }
            @Override
            protected void done() {
                try {
                    NguyenVongImportService.ImportResult r = get();
                    String msg = "Import hoàn tất!\n"
                            + "✅ Thành công: " + r.successCount + "\n"
                            + "⏭️ Bỏ qua: " + r.skipCount + "\n"
                            + "❌ Lỗi: " + r.errorCount;
                    if (!r.errors.isEmpty()) {
                        msg += "\n\nChi tiết lỗi:\n" + String.join("\n", r.errors.subList(0, Math.min(5, r.errors.size())));
                    }
                    MessageHelper.showInfo(NguyenVongPanel.this, msg);
                    loadData();
                } catch (Exception e) {
                    MessageHelper.showError(NguyenVongPanel.this, "Lỗi import: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void doDelete() {
        int row = styledTable.getSelectedRow();
        if (row < 0) { MessageHelper.showWarning(this, "Vui lòng chọn nguyện vọng."); return; }
        if (ConfirmDialog.show(this, "Bạn có chắc muốn xóa nguyện vọng này?")) {
            int realIdx = styledTable.getRealIndex(row);
            if (realIdx >= 0 && realIdx < loadedEntities.size()) {
                NguyenVongXetTuyen nv = loadedEntities.get(realIdx);
                try {
                    dao.delete(nv);
                    MessageHelper.showSuccess(this, "Đã xóa nguyện vọng.");
                    loadData();
                } catch (Exception e) {
                    MessageHelper.showError(this, "Lỗi xóa: " + e.getMessage());
                }
            }
        }
    }

    // ── Score calculation & Admission ──

    private void calculateScores() {
        int result = JOptionPane.showConfirmDialog(this,
                "Tính lại ĐTHGXT + ĐC + ĐƯT + ĐXT cho tất cả nguyện vọng?",
                "Xác nhận tính điểm", JOptionPane.YES_NO_OPTION);
        if (result != JOptionPane.YES_OPTION) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                admissionService.calculateAllScores();
                return null;
            }
            @Override
            protected void done() {
                try {
                    get(); // check for exceptions
                    MessageHelper.showSuccess(NguyenVongPanel.this, "Đã tính xong điểm xét tuyển cho tất cả nguyện vọng!");
                } catch (Exception e) {
                    MessageHelper.showError(NguyenVongPanel.this, "Lỗi tính điểm: " + e.getMessage());
                }
                loadData();
            }
        };
        worker.execute();
    }

    private void runAdmission() {
        int result = JOptionPane.showConfirmDialog(this,
                "Chạy thuật toán xét tuyển Gale-Shapley?\nKết quả hiện tại sẽ bị ghi đè.",
                "Xác nhận chạy xét tuyển", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result != JOptionPane.YES_OPTION) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                admissionService.executeAdmissionProcess();
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    MessageHelper.showSuccess(NguyenVongPanel.this, "Xét tuyển hoàn tất! Kết quả đã được cập nhật.");
                } catch (Exception e) {
                    MessageHelper.showError(NguyenVongPanel.this, "Lỗi xét tuyển: " + e.getMessage());
                }
                loadData();
            }
        };
        worker.execute();
    }
}
