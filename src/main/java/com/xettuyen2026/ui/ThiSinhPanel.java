package com.xettuyen2026.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
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
    private JLabel lblTotalThiSinh;
    private JComboBox<String> cboDoiTuong;
    private JComboBox<String> cboKhuVuc;
    private JComboBox<String> cboDanToc;
    private JComboBox<String> cboNoiSinh;
    private final ThiSinhService service;
    private boolean triedAutoLoadFromDataFile = false;
    private List<ThiSinh> allEntities = new ArrayList<>();
    private List<ThiSinh> filteredEntities = new ArrayList<>();

    public ThiSinhPanel() {
        service = new ThiSinhService();
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.BG_MAIN);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createTableCard(), BorderLayout.CENTER);
    }

    // Tạo khu vực tổng quan, tìm kiếm và bộ lọc thí sinh
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(createSummaryPanel());
        header.add(Box.createVerticalStrut(8));
        header.add(createToolbar());
        header.add(Box.createVerticalStrut(8));
        header.add(createFilterPanel());
        return header;
    }

    // Tạo ô tổng thí sinh thay đổi theo kết quả lọc hiện tại
    private JPanel createSummaryPanel() {
        JPanel summary = new JPanel(new GridLayout(1, 1, 0, 0));
        summary.setOpaque(false);
        summary.setPreferredSize(new Dimension(0, 80));
        lblTotalThiSinh = new JLabel("0");
        summary.add(createStatCard("Tổng thí sinh", lblTotalThiSinh, UIConstants.STAT_BLUE));
        return summary;
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

        RoundedButton btnDetail = new RoundedButton("Chi tiết", UIConstants.PRIMARY);
        btnDetail.addActionListener(e -> doDetail());

        RoundedButton btnDelete = new RoundedButton(UIConstants.ICON_DELETE + " Xóa", UIConstants.DANGER);
        btnDelete.addActionListener(e -> doDelete());

        rightPanel.add(btnImport);
        rightPanel.add(btnAdd);
        rightPanel.add(btnEdit);
        rightPanel.add(btnDetail);
        rightPanel.add(btnDelete);
        toolbar.add(rightPanel, BorderLayout.EAST);

        return toolbar;
    }

    // Tạo bộ lọc theo đối tượng, khu vực, dân tộc và nơi sinh
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterPanel.setOpaque(false);

        cboDoiTuong = createFilterCombo();
        cboKhuVuc = createFilterCombo();
        cboDanToc = createFilterCombo();
        cboNoiSinh = createFilterCombo();

        filterPanel.add(createFilterLabel("ĐTUT"));
        filterPanel.add(cboDoiTuong);
        filterPanel.add(createFilterLabel("KVUT"));
        filterPanel.add(cboKhuVuc);
        filterPanel.add(createFilterLabel("Dân tộc"));
        filterPanel.add(cboDanToc);
        filterPanel.add(createFilterLabel("Nơi sinh"));
        filterPanel.add(cboNoiSinh);

        RoundedButton btnReset = new RoundedButton("Bỏ lọc", new Color(0x757575));
        btnReset.addActionListener(e -> clearFilters());
        filterPanel.add(btnReset);
        return filterPanel;
    }

    // Tạo nhãn cho từng bộ lọc
    private JLabel createFilterLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_BOLD);
        label.setForeground(UIConstants.TEXT_PRIMARY);
        return label;
    }

    // Tạo combobox lọc có sự kiện cập nhật bảng
    private JComboBox<String> createFilterCombo() {
        JComboBox<String> combo = new JComboBox<>();
        combo.setFont(UIConstants.FONT_REGULAR);
        combo.setPreferredSize(new Dimension(130, 30));
        combo.addActionListener(e -> applyFilters());
        return combo;
    }

    // Tạo ô thống kê theo màu giao diện hiện có
    private JPanel createStatCard(String label, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, 6, getHeight() - 1, 6, 6);
                g2.setColor(UIConstants.BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 16));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        valueLabel.setFont(UIConstants.FONT_STAT_NUM);
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel labelText = new JLabel(label);
        labelText.setFont(UIConstants.FONT_REGULAR);
        labelText.setForeground(UIConstants.TEXT_SECONDARY);
        labelText.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(valueLabel);
        textPanel.add(labelText);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
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
            allEntities = service.findAll();
            updateFilterOptions(allEntities);
            applyFilters();
        } catch (Exception e) {
            e.printStackTrace();
            MessageHelper.showError(this, "Không thể tải dữ liệu thí sinh:\n" + e.getMessage());
        }
    }

    private void doSearch() {
        applyFilters();
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
        ThiSinh ts = getSelectedThiSinh("Vui lòng chọn một thí sinh để sửa.");
        if (ts == null) return;

        ThiSinh selected = service.findByCccd(ts.getCccd());
        if (selected != null) {
            ThiSinhDialog dlg = new ThiSinhDialog(SwingUtilities.getWindowAncestor(this), selected);
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

    // Mở hộp thoại xem chi tiết thí sinh và điểm thi
    private void doDetail() {
        ThiSinh ts = getSelectedThiSinh("Vui lòng chọn một thí sinh để xem chi tiết.");
        if (ts == null) return;

        ThiSinh selected = service.findByCccd(ts.getCccd());
        if (selected != null) {
            ThiSinhDetailDialog dlg = new ThiSinhDetailDialog(SwingUtilities.getWindowAncestor(this), selected);
            dlg.setVisible(true);
        }
    }

    private void doDelete() {
        ThiSinh selectedRow = getSelectedThiSinh("Vui lòng chọn một thí sinh để xóa.");
        if (selectedRow == null) return;

        if (ConfirmDialog.show(this, "Sẽ xóa luôn điểm thi, điểm cộng, nguyện vọng của thí sinh đó.<br>Bấm Xóa lần nữa để xác nhận.")) {
            try {
                ThiSinh selected = service.findByCccd(selectedRow.getCccd());
                if (selected != null) {
                    service.delete(selected);
                    MessageHelper.showSuccess(this, "Đã xóa thí sinh và dữ liệu liên quan.");
                    loadData();
                    refreshCandidateRelatedPanels();
                }
            } catch (Exception e) {
                MessageHelper.showError(this, "Lỗi xóa: " + e.getMessage());
            }
        }
    }

    private void refreshCandidateRelatedPanels() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (owner instanceof MainFrame) {
            ((MainFrame) owner).refreshCandidateRelatedPanels();
        }
    }

    // Áp dụng tìm kiếm và bộ lọc vào danh sách đang hiển thị
    private void applyFilters() {
        if (styledTable == null) return;

        String keyword = normalizeForFilter(searchBar != null ? searchBar.getText() : "");
        String doiTuong = getSelectedFilter(cboDoiTuong);
        String khuVuc = getSelectedFilter(cboKhuVuc);
        String danToc = getSelectedFilter(cboDanToc);
        String noiSinh = getSelectedFilter(cboNoiSinh);

        List<ThiSinh> result = new ArrayList<>();
        for (ThiSinh ts : allEntities) {
            if (!matchesKeyword(ts, keyword)) continue;
            if (!matchesFilter(ts.getDoiTuong(), doiTuong)) continue;
            if (!matchesFilter(ts.getKhuVuc(), khuVuc)) continue;
            if (!matchesFilter(ts.getDanToc(), danToc)) continue;
            if (!matchesFilter(ts.getNoiSinh(), noiSinh)) continue;
            result.add(ts);
        }

        filteredEntities = result;
        displayList(filteredEntities);
        updateTotal(filteredEntities.size());
    }

    // Cập nhật danh sách lựa chọn lọc từ dữ liệu thí sinh hiện có
    private void updateFilterOptions(List<ThiSinh> list) {
        updateCombo(cboDoiTuong, collectValues(list, "doiTuong"));
        updateCombo(cboKhuVuc, collectValues(list, "khuVuc"));
        updateCombo(cboDanToc, collectValues(list, "danToc"));
        updateCombo(cboNoiSinh, collectValues(list, "noiSinh"));
    }

    // Cập nhật dữ liệu cho một combobox lọc
    private void updateCombo(JComboBox<String> combo, Set<String> values) {
        if (combo == null) return;

        Object selected = combo.getSelectedItem();
        combo.removeAllItems();
        combo.addItem("Tất cả");
        for (String value : values) {
            combo.addItem(value);
        }
        if (selected != null) {
            combo.setSelectedItem(selected);
        }
        if (combo.getSelectedItem() == null && combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
    }

    // Lấy danh sách giá trị lọc theo tên thuộc tính
    private Set<String> collectValues(List<ThiSinh> list, String field) {
        Set<String> values = new LinkedHashSet<>();
        for (ThiSinh ts : list) {
            String value = "";
            if ("doiTuong".equals(field)) {
                value = ts.getDoiTuong();
            } else if ("khuVuc".equals(field)) {
                value = ts.getKhuVuc();
            } else if ("danToc".equals(field)) {
                value = ts.getDanToc();
            } else if ("noiSinh".equals(field)) {
                value = ts.getNoiSinh();
            }
            value = clean(value);
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return values;
    }

    // Xóa toàn bộ bộ lọc và áp dụng lại danh sách
    private void clearFilters() {
        if (searchBar != null) {
            searchBar.setText("");
        }
        resetCombo(cboDoiTuong);
        resetCombo(cboKhuVuc);
        resetCombo(cboDanToc);
        resetCombo(cboNoiSinh);
        applyFilters();
    }

    // Đưa combobox lọc về lựa chọn tất cả
    private void resetCombo(JComboBox<String> combo) {
        if (combo != null && combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
    }

    // Lấy thí sinh đang chọn trên bảng theo đúng danh sách đã lọc
    private ThiSinh getSelectedThiSinh(String warningMessage) {
        int row = styledTable.getSelectedRow();
        if (row < 0) {
            MessageHelper.showWarning(this, warningMessage);
            return null;
        }

        int realIdx = styledTable.getRealIndex(row);
        if (realIdx < 0 || realIdx >= filteredEntities.size()) {
            return null;
        }
        return filteredEntities.get(realIdx);
    }

    // Kiểm tra thí sinh có khớp từ khóa tìm kiếm hay không
    private boolean matchesKeyword(ThiSinh ts, String keyword) {
        if (keyword.isEmpty()) return true;
        return normalizeForFilter(ts.getCccd()).contains(keyword)
                || normalizeForFilter(ts.getSobaodanh()).contains(keyword)
                || normalizeForFilter(buildHoTen(ts)).contains(keyword);
    }

    // Kiểm tra giá trị thí sinh có khớp bộ lọc hay không
    private boolean matchesFilter(String actual, String expected) {
        if (expected == null || expected.isEmpty()) return true;
        return clean(actual).equals(expected);
    }

    // Lấy giá trị đang chọn của bộ lọc
    private String getSelectedFilter(JComboBox<String> combo) {
        if (combo == null || combo.getSelectedItem() == null) return "";
        String value = combo.getSelectedItem().toString();
        return "Tất cả".equals(value) ? "" : value;
    }

    // Cập nhật tổng thí sinh theo kết quả lọc
    private void updateTotal(int total) {
        if (lblTotalThiSinh != null) {
            lblTotalThiSinh.setText(String.valueOf(total));
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

    // Chuẩn hóa chuỗi để so khớp tìm kiếm không phân biệt hoa thường
    private String normalizeForFilter(String value) {
        return clean(value).toLowerCase();
    }

    // Chuyển null thành chuỗi rỗng và loại khoảng trắng thừa
    private String clean(String value) {
        return value != null ? value.trim() : "";
    }

    private String buildHoTen(ThiSinh ts) {
        String ho = ts.getHo() == null ? "" : ts.getHo().trim();
        String ten = ts.getTen() == null ? "" : ts.getTen().trim();
        String fullName = (ho + " " + ten).trim();
        return fullName.isEmpty() ? ts.getCccd() : fullName;
    }
}
