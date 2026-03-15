package com.xettuyen2026.ui.common;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Bảng dữ liệu theo style thiết kế, kèm phân trang.
 */
public class PaginatedTable extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JPanel paginationPanel;

    // Pagination state
    private java.util.List<Object[]> allData = new java.util.ArrayList<>();
    private int currentPage = 1;
    private int pageSize = UIConstants.PAGE_SIZE;
    private int totalPages = 1;

    private JLabel lblPageInfo;
    private JButton btnFirst, btnPrev, btnNext, btnLast;

    public PaginatedTable(String[] columns) {
        setLayout(new BorderLayout());
        setOpaque(false);

        // ── Table Model ──
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(UIConstants.FONT_REGULAR);
        table.setRowHeight(32);
        table.setShowGrid(true);
        table.setGridColor(UIConstants.TABLE_GRID);
        table.setSelectionBackground(new Color(0xBBDEFB));
        table.setSelectionForeground(UIConstants.TEXT_PRIMARY);
        table.setIntercellSpacing(new Dimension(0, 0));

        // ── Header ──
        JTableHeader header = table.getTableHeader();
        header.setBackground(UIConstants.TABLE_HEADER);
        header.setForeground(Color.WHITE);
        header.setFont(UIConstants.FONT_BOLD);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                lbl.setBackground(UIConstants.TABLE_HEADER);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(UIConstants.FONT_BOLD);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(255,255,255,50)),
                    BorderFactory.createEmptyBorder(0, 6, 0, 6)));
                return lbl;
            }
        });

        // ── Alternating row colors ──
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (!sel) {
                    c.setBackground(row % 2 == 0 ? UIConstants.ROW_ODD : UIConstants.ROW_EVEN);
                }
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // ── Pagination ──
        buildPaginationPanel();
        add(paginationPanel, BorderLayout.SOUTH);
    }

    private void buildPaginationPanel() {
        paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        paginationPanel.setOpaque(false);

        btnFirst = makePagBtn("<< Đầu");
        btnPrev  = makePagBtn("< Trước");
        lblPageInfo = new JLabel("Trang 1/1");
        lblPageInfo.setFont(UIConstants.FONT_REGULAR);
        btnNext  = makePagBtn("Tiếp >");
        btnLast  = makePagBtn("Cuối >>");

        btnFirst.addActionListener(e -> goToPage(1));
        btnPrev.addActionListener(e -> goToPage(currentPage - 1));
        btnNext.addActionListener(e -> goToPage(currentPage + 1));
        btnLast.addActionListener(e -> goToPage(totalPages));

        paginationPanel.add(btnFirst);
        paginationPanel.add(btnPrev);
        paginationPanel.add(lblPageInfo);
        paginationPanel.add(btnNext);
        paginationPanel.add(btnLast);
    }

    private JButton makePagBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UIConstants.FONT_SMALL);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 8, 28));
        return btn;
    }

    /** Load tất cả dữ liệu vào bộ nhớ, hiển thị trang 1. */
    public void setData(java.util.List<Object[]> data) {
        this.allData = data != null ? data : new java.util.ArrayList<>();
        this.totalPages = Math.max(1, (int) Math.ceil((double) allData.size() / pageSize));
        goToPage(1);
    }

    /** Thêm 1 dòng vào cuối data rồi refresh. */
    public void addRow(Object[] row) {
        allData.add(row);
        this.totalPages = Math.max(1, (int) Math.ceil((double) allData.size() / pageSize));
        goToPage(totalPages); // jump to last page
    }

    /** Xóa toàn bộ data. */
    public void clearData() {
        allData.clear();
        totalPages = 1;
        goToPage(1);
    }

    private void goToPage(int page) {
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        this.currentPage = page;

        tableModel.setRowCount(0);
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, allData.size());
        for (int i = start; i < end; i++) {
            tableModel.addRow(allData.get(i));
        }

        lblPageInfo.setText("Trang " + currentPage + "/" + totalPages + "  (" + allData.size() + " dòng)");
        btnFirst.setEnabled(currentPage > 1);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);
        btnLast.setEnabled(currentPage < totalPages);
    }

    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public int getSelectedRow() { return table.getSelectedRow(); }
    public int getCurrentPage() { return currentPage; }
    public int getPageSize() { return pageSize; }

    /** Trả về row index thực trong allData dựa trên visual row. */
    public int getRealIndex(int visualRow) {
        return (currentPage - 1) * pageSize + visualRow;
    }

    public java.util.List<Object[]> getAllData() { return allData; }
}
