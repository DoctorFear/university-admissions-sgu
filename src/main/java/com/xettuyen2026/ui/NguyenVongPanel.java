package com.xettuyen2026.ui;

import com.xettuyen2026.dao.NguyenVongDAO;
import com.xettuyen2026.dao.NganhDAO;
import com.xettuyen2026.dao.ThiSinhDAO;
import com.xettuyen2026.entity.Nganh;
import com.xettuyen2026.entity.NguyenVongXetTuyen;
import com.xettuyen2026.entity.ThiSinh;
import com.xettuyen2026.service.AdmissionService;
import com.xettuyen2026.service.NguyenVongImportService;
import com.xettuyen2026.ui.common.*;
import com.xettuyen2026.ui.common.CancelReasonDialog;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
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
    private ThiSinhDAO thiSinhDAO;
    private AdmissionService admissionService;
    private List<NguyenVongXetTuyen> loadedEntities = new ArrayList<>();

    // YÊU CẦU 4: Tìm kiếm nâng cao (Unified Search)
    private JComboBox<String> cboSearchCriteria;

    private static final String[] COLUMNS = {
            "STT", "CCCD", "Tên", "Mã ngành", "Tên ngành", "NV Thứ", "PT", "THM",
            "Điểm THXT", "Điểm Cộng", "Điểm ƯT", "Điểm XT", "Kết quả"
    };

    public NguyenVongPanel() {
        dao = new NguyenVongDAO();
        nganhDAO = new NganhDAO();
        thiSinhDAO = new ThiSinhDAO();
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

        // ── Row 1: Unified Search ──
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        row1.setOpaque(false);
        
        cboSearchCriteria = new JComboBox<>(new String[]{"CCCD", "Mã ngành", "Phương thức", "Tổ hợp"});
        cboSearchCriteria.setFont(UIConstants.FONT_REGULAR);
        cboSearchCriteria.setPreferredSize(new Dimension(140, 36));
        cboSearchCriteria.addActionListener(e -> {
            String sel = (String) cboSearchCriteria.getSelectedItem();
            if ("CCCD".equals(sel)) searchBar.setPlaceholder("Nhập CCCD...");
            else if ("Mã ngành".equals(sel)) searchBar.setPlaceholder("Nhập Mã ngành...");
            else if ("Phương thức".equals(sel)) searchBar.setPlaceholder("Nhập PT (VD: PT2, THPT, DGNL)...");
            else if ("Tổ hợp".equals(sel)) searchBar.setPlaceholder("Nhập Tổ hợp (VD: A00)...");
            searchBar.clear();
            loadData();
        });

        searchBar = new SearchBar("Nhập CCCD...", e -> doUnifiedSearch());
        
        RoundedButton btnSearch = new RoundedButton(UIConstants.ICON_SEARCH + " Tìm", UIConstants.PRIMARY_LIGHT);
        btnSearch.addActionListener(e -> doUnifiedSearch());
        
        RoundedButton btnReset = new RoundedButton("🔄 Làm mới", new Color(0x607D8B));
        btnReset.addActionListener(e -> {
            searchBar.clear();
            cboSearchCriteria.setSelectedIndex(0);
            loadData();
        });
        
        RoundedButton btnLoad = new RoundedButton(UIConstants.ICON_DOWNLOAD + " Tải DS", UIConstants.PRIMARY);
        btnLoad.addActionListener(e -> doExport());
        
        row1.add(cboSearchCriteria);
        row1.add(searchBar);
        row1.add(btnSearch);
        row1.add(btnReset);
        row1.add(btnLoad);
        toolbar.add(row1);

        // ── Row 2: Action buttons ──
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        row2.setOpaque(false);

        RoundedButton btnImport = new RoundedButton(UIConstants.ICON_IMPORT + " Import Excel", new Color(0x00796B));
        btnImport.addActionListener(e -> doImport());

        RoundedButton btnAdd = new RoundedButton(UIConstants.ICON_ADD + " Bổ sung nguyện vọng", UIConstants.SUCCESS);
        btnAdd.addActionListener(e -> doAdd());

        RoundedButton btnDelete = new RoundedButton(UIConstants.ICON_DELETE + " Hủy NV", UIConstants.DANGER);
        btnDelete.addActionListener(e -> doCancel());

        // Separator
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(2, 24));
        sep.setForeground(UIConstants.BORDER_LIGHT);

        RoundedButton btnCalc = new RoundedButton(UIConstants.ICON_CALCULATE + " Xét tuyển cá nhân",
                new Color(0x6A1B9A));
        btnCalc.addActionListener(e -> calculateIndividualScore());

        RoundedButton btnRun = new RoundedButton(UIConstants.ICON_EXECUTE + " Xét tuyển toàn bộ",
                UIConstants.STAT_GREEN);
        btnRun.addActionListener(e -> runAdmission());

        row2.add(btnImport);
        row2.add(btnAdd);
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
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row,
                    int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (!sel)
                    c.setBackground(row % 2 == 0 ? UIConstants.ROW_ODD : UIConstants.ROW_EVEN);
                c.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

                // Color-code kết quả (col 12)
                if (col == 12 && value != null) {
                    String val = value.toString();
                    if ("yes".equals(val)) {
                        c.setForeground(UIConstants.SUCCESS);
                        c.setFont(UIConstants.FONT_BOLD);
                        c.setText("Trúng tuyển");
                    } else if ("duoisan".equals(val)) {
                        c.setForeground(UIConstants.DANGER);
                        c.setFont(UIConstants.FONT_BOLD);
                        c.setText("Rớt");
                    } else if ("Đã hủy".equals(val)) {
                        c.setForeground(new Color(0x9E9E9E));
                        c.setFont(UIConstants.FONT_BOLD);
                        c.setText("Đã hủy");
                    } else {
                        c.setForeground(UIConstants.TEXT_SECONDARY);
                    }
                } else if (col == 6 && value != null) {
                    // Badge màu Phương thức
                    String val = value.toString().toUpperCase();
                    if ("PT2".equals(val) || "THPT".equals(val) || "1".equals(val)) {
                        c.setForeground(new Color(0x1976D2)); // Xanh dương
                        c.setFont(UIConstants.FONT_BOLD);
                        c.setText("PT2 (THPT)");
                        c.setToolTipText("Xét Điểm Thi THPT");
                    } else if ("PT3".equals(val) || "VSAT".equals(val) || "5".equals(val)) {
                        c.setForeground(new Color(0x7B1FA2)); // Tím
                        c.setFont(UIConstants.FONT_BOLD);
                        c.setText("PT3 (V-SAT)");
                        c.setToolTipText("Kỳ thi V-SAT");
                    } else if ("PT4".equals(val) || "DGNL".equals(val) || "4".equals(val)) {
                        c.setForeground(new Color(0xF57C00)); // Cam
                        c.setFont(UIConstants.FONT_BOLD);
                        c.setText("PT4 (ĐGNL)");
                        c.setToolTipText("Đánh giá Năng lực ĐHQG");
                    } else {
                        c.setForeground(UIConstants.TEXT_PRIMARY);
                    }
                } else {
                    c.setForeground(UIConstants.TEXT_PRIMARY);
                    c.setToolTipText(null);
                }

                if (col == 0 || col == 5 || (col >= 8 && col <= 11))
                    setHorizontalAlignment(SwingConstants.CENTER);
                else
                    setHorizontalAlignment(SwingConstants.LEFT);

                return c;
            }
        });

        card.add(styledTable, BorderLayout.CENTER);
        SwingUtilities.invokeLater(this::loadData);
        return card;
    }

    // ── Data loading ──

    private Map<String, String> nganhNameMap;
    private Map<String, ThiSinh> thiSinhMap;

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

    private ThiSinh getThiSinh(String cccd) {
        if (thiSinhMap == null) {
            try {
                thiSinhMap = new HashMap<>();
                for (ThiSinh ts : thiSinhDAO.findAll()) {
                    thiSinhMap.put(ts.getCccd(), ts);
                }
            } catch (Exception e) {
                thiSinhMap = new HashMap<>();
            }
        }
        return thiSinhMap.get(cccd);
    }

    private void loadData() {
        try {
            nganhNameMap = null; // refresh
            thiSinhMap = null; // refresh
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

    public void refreshData() {
        loadData();
    }

    private Object[] mapToRow(NguyenVongXetTuyen nv, int stt) {
        ThiSinh ts = getThiSinh(nv.getNnCccd());
        String ten = ts != null ? ts.getTen() : "";
        String thm = nv.getTtThm() != null ? nv.getTtThm() : "";
        return new Object[] {
                stt,
                nv.getNnCccd(),
                ten,
                nv.getNvManganh(),
                getNganhName(nv.getNvManganh()),
                nv.getNvTt(),
                nv.getTtPhuongthuc(),
                thm,
                nv.getDiemThxt(),
                nv.getDiemCong(),
                nv.getDiemUtqd(),
                nv.getDiemXettuyen(),
                nv.getNvKetqua()
        };
    }

    private void doUnifiedSearch() {
        String kw = searchBar.getText().trim().toUpperCase();
        if (kw.isEmpty()) {
            loadData();
            return;
        }
        
        try {
            String sel = (String) cboSearchCriteria.getSelectedItem();
            List<NguyenVongXetTuyen> allNV = dao.findAllOrdered();
            List<NguyenVongXetTuyen> filtered = new ArrayList<>();
            
            for (NguyenVongXetTuyen nv : allNV) {
                if ("CCCD".equals(sel)) {
                    if (nv.getNnCccd() != null && nv.getNnCccd().toUpperCase().contains(kw)) {
                        filtered.add(nv);
                    }
                } else if ("Mã ngành".equals(sel)) {
                    if (nv.getNvManganh() != null && nv.getNvManganh().toUpperCase().contains(kw)) {
                        filtered.add(nv);
                    }
                } else if ("Phương thức".equals(sel)) {
                    String pt = nv.getTtPhuongthuc() != null ? nv.getTtPhuongthuc().toUpperCase() : "";
                    if (pt.contains(kw)) {
                        filtered.add(nv);
                    } else if (("THPT".contains(kw) || "1".equals(kw) || "2".equals(kw)) && pt.contains("PT2")) {
                        filtered.add(nv);
                    } else if (("VSAT".contains(kw) || "5".equals(kw)) && pt.contains("PT3")) {
                        filtered.add(nv);
                    } else if (("DGNL".contains(kw) || "4".equals(kw)) && pt.contains("PT4")) {
                        filtered.add(nv);
                    }
                } else if ("Tổ hợp".equals(sel)) {
                    if (nv.getTtThm() != null && nv.getTtThm().toUpperCase().contains(kw)) {
                        filtered.add(nv);
                    }
                }
            }
            
            loadedEntities = filtered;
            nganhNameMap = null;
            thiSinhMap = null;
            List<Object[]> rows = new ArrayList<>();
            int stt = 1;
            for (NguyenVongXetTuyen nv : loadedEntities)
                rows.add(mapToRow(nv, stt++));
            styledTable.setData(rows);
        } catch (Exception e) {
            MessageHelper.showError(this, "Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    // ── CRUD ──

    private void doAdd() {
        NguyenVongDialog dlg = new NguyenVongDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isSaved())
            loadData();
    }

    private void doCancel() {
        int row = styledTable.getSelectedRow();
        if (row < 0) {
            MessageHelper.showWarning(this, "Vui lòng chọn nguyện vọng.");
            return;
        }
        int realIdx = styledTable.getRealIndex(row);
        if (realIdx < 0 || realIdx >= loadedEntities.size())
            return;

        NguyenVongXetTuyen nv = loadedEntities.get(realIdx);
        if ("Đã hủy".equals(nv.getNvKetqua())) {
            MessageHelper.showWarning(this, "Nguyện vọng này đã được hủy trước đó.");
            return;
        }
        // YÊU CẦU 2: Không cho hủy nếu đã xét tuyển (yes hoặc duoisan)
        String ketQua = nv.getNvKetqua();
        if (ketQua != null && !ketQua.trim().isEmpty()) {
            MessageHelper.showWarning(this, "Không thể hủy nguyện vọng đã được xét tuyển.\nKết quả hiện tại: " + ketQua);
            return;
        }

        String ten = getThiSinh(nv.getNnCccd()) != null ? getThiSinh(nv.getNnCccd()).getTen() : "";
        boolean confirmed = CancelReasonDialog.showConfirm(this, ten, nv.getNnCccd(), nv.getNvManganh());
        if (confirmed) {
            try {
                nv.setNvKetqua("Đã hủy");
                dao.update(nv);
                System.out.println("[HỦY NV] CCCD=" + nv.getNnCccd()
                        + ", Ngành=" + nv.getNvManganh());
                MessageHelper.showSuccess(this, "Đã xóa nguyện vọng.");
                loadData();
            } catch (Exception e) {
                MessageHelper.showError(this, "Lỗi hủy: " + e.getMessage());
            }
        }
    }

    private void doExport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu file Excel");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().endsWith(".xlsx")) {
                file = new File(file.getAbsolutePath() + ".xlsx");
            }
            try {
                if (nganhNameMap == null) {
                    getNganhName(""); // trigger load map
                }
                com.xettuyen2026.util.ExportUtil.exportNguyenVongToExcel(loadedEntities, nganhNameMap, file);
                MessageHelper.showSuccess(this, "Xuất file Excel thành công!");
            } catch (Exception e) {
                MessageHelper.showError(this, "Lỗi xuất Excel: " + e.getMessage());
            }
        }
    }

    // ── Score calculation & Admission ──

    private void doImport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (.xlsx)", "xlsx"));
        chooser.setDialogTitle("Chọn file Excel nguyện vọng");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

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
                        msg += "\n\nChi tiết lỗi:\n"
                                + String.join("\n", r.errors.subList(0, Math.min(5, r.errors.size())));
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

    private void calculateIndividualScore() {
        int row = styledTable.getSelectedRow();
        if (row < 0) {
            MessageHelper.showWarning(this, "Vui lòng chọn một nguyện vọng của thí sinh để xét tuyển cá nhân.");
            return;
        }
        int realIdx = styledTable.getRealIndex(row);
        if (realIdx < 0 || realIdx >= loadedEntities.size())
            return;

        NguyenVongXetTuyen selectedNv = loadedEntities.get(realIdx);
        String cccd = selectedNv.getNnCccd();

        int result = JOptionPane.showConfirmDialog(this,
                "Tính điểm & xét duyệt trúng tuyển cho thí sinh có CCCD: " + cccd + "?",
                "Xác nhận xét tuyển cá nhân", JOptionPane.YES_NO_OPTION);
        if (result != JOptionPane.YES_OPTION)
            return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                com.xettuyen2026.dao.DiemCongDAO diemCongDAO = new com.xettuyen2026.dao.DiemCongDAO();
                com.xettuyen2026.dao.NganhDAO nganhDAO = new com.xettuyen2026.dao.NganhDAO();

                // Lọc và sắp xếp các NV của thí sinh theo thứ tự ưu tiên
                java.util.List<NguyenVongXetTuyen> candidateNvs = loadedEntities.stream()
                        .filter(nv -> cccd.equals(nv.getNnCccd()))
                        .sorted(java.util.Comparator
                                .comparingInt(n -> n.getNvTt() != null ? n.getNvTt() : Integer.MAX_VALUE))
                        .collect(java.util.stream.Collectors.toList());

                boolean alreadyPassed = false;

                for (NguyenVongXetTuyen nv : candidateNvs) {
                    if ("Đã hủy".equals(nv.getNvKetqua()))
                        continue; // Skip canceled

                    String pt = nv.getTtPhuongthuc();
                    if (pt == null)
                        pt = "PT2";

                    double dthxt = 0.0;
                    String bestThm = nv.getTtThm();
                    String ptUpper = pt.toUpperCase();
                    if (ptUpper.contains("PT3") || ptUpper.contains("VSAT") || pt.equals("5") || ptUpper.contains("PT5")) {
                        dthxt = admissionService.tinhDiemVSAT(nv.getNnCccd(), nv.getNvManganh(), null);
                        bestThm = admissionService.findBestTohopVSAT(nv.getNnCccd(), nv.getNvManganh());
                    } else if (ptUpper.contains("PT4") || ptUpper.contains("DGNL") || pt.equals("4")) {
                        dthxt = admissionService.tinhDiemDGNL(nv.getNnCccd(), nv.getNvManganh());
                        bestThm = admissionService.findBestTohopDGNL(nv.getNvManganh());
                    } else {
                        dthxt = admissionService.tinhDiemTHPT(nv.getNnCccd(), nv.getNvManganh(), null);
                        bestThm = admissionService.findBestTohopTHPT(nv.getNnCccd(), nv.getNvManganh());
                    }
                    // Cập nhật tổ hợp cho điểm cao nhất
                    if (bestThm != null && !bestThm.trim().isEmpty()) {
                        nv.setTtThm(bestThm);
                    }

                    boolean containsN1 = (nv.getTtThm() != null && nv.getTtThm().contains("N1"));
                    double dc = admissionService.getDiemCongDouble(nv.getNnCccd(), nv.getNvManganh(), nv.getTtThm(),
                            containsN1);
                    double mdut = admissionService.calculateMdutDouble(nv.getNnCccd());

                    double dut = admissionService.tinhDiemUuTien(dthxt, dc, mdut);
                    double dxt = admissionService.tinhDiemXT(dthxt, dc, dut);

                    nv.setDiemThxt(BigDecimal.valueOf(dthxt));
                    nv.setDiemCong(BigDecimal.valueOf(dc));
                    nv.setDiemUtqd(BigDecimal.valueOf(dut));
                    nv.setDiemXettuyen(BigDecimal.valueOf(dxt));

                    // Xét kết quả Trúng tuyển / Rớt
                    if (alreadyPassed) {
                        nv.setNvKetqua("duoisan");
                    } else {
                        com.xettuyen2026.entity.Nganh nganh = nganhDAO.findByMaNganh(nv.getNvManganh());
                        if (nganh == null) {
                            nv.setNvKetqua("duoisan");
                        } else {
                            BigDecimal diemsan = nganh.getnDiemsan() != null ? nganh.getnDiemsan() : BigDecimal.ZERO;
                            BigDecimal diemchuan = nganh.getnDiemtrungtuyen() != null ? nganh.getnDiemtrungtuyen()
                                    : diemsan;

                            if (BigDecimal.valueOf(dxt).compareTo(diemsan) < 0) {
                                nv.setNvKetqua("duoisan");
                            } else if (BigDecimal.valueOf(dxt).compareTo(diemchuan) >= 0) {
                                nv.setNvKetqua("yes");
                                alreadyPassed = true;
                            } else {
                                nv.setNvKetqua("duoisan");
                            }
                        }
                    }

                    dao.update(nv);

                    // Cập nhật diemTong, diemUtxt vào xt_diemcongxetuyen
                    String dcKey = nv.getNnCccd() + "_" + nv.getNvManganh() + "_" + pt;
                    com.xettuyen2026.entity.DiemCongXetTuyen dcEntity = diemCongDAO.findByKey(dcKey);
                    if (dcEntity == null) {
                        dcEntity = new com.xettuyen2026.entity.DiemCongXetTuyen();
                        dcEntity.setTsCccd(nv.getNnCccd());
                        dcEntity.setManganh(nv.getNvManganh());
                        dcEntity.setPhuongthuc(pt);
                        dcEntity.setDcKeys(dcKey);
                        dcEntity.setDiemTong(BigDecimal.valueOf(dc));
                        dcEntity.setDiemUtxt(BigDecimal.valueOf(dut));
                        diemCongDAO.save(dcEntity);
                    } else {
                        dcEntity.setDiemTong(BigDecimal.valueOf(dc));
                        dcEntity.setDiemUtxt(BigDecimal.valueOf(dut));
                        diemCongDAO.update(dcEntity);
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // check for exceptions
                    MessageHelper.showSuccess(NguyenVongPanel.this,
                            "Đã xét tuyển thành công cho thí sinh " + cccd + "!");
                } catch (Exception e) {
                    MessageHelper.showError(NguyenVongPanel.this, "Lỗi xét tuyển: " + e.getMessage());
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
        if (result != JOptionPane.YES_OPTION)
            return;

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
