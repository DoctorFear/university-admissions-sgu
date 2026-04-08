package com.xettuyen2026.ui.common;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

public class PaginatedTable extends JPanel {

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JScrollPane scrollPane;

    private final JLabel lblPageInfo;
    private final JButton btnPrev;
    private final JButton btnNext;
    private final JComboBox<Integer> cboPageSize;
    private final JComboBox<Integer> cboPage;

    private List<Object[]> allData = new ArrayList<>();
    private int currentPage = 1;
    private int pageSize = UIConstants.PAGE_SIZE;
    private int totalPages = 1;
    private boolean updatingControls = false;

    public PaginatedTable(String[] columns) {
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(UIConstants.FONT_REGULAR);
        table.setRowHeight(32);
        table.setShowGrid(true);
        table.setGridColor(UIConstants.TABLE_GRID);
        table.setSelectionBackground(new Color(0xBBDEFB));
        table.setSelectionForeground(UIConstants.TEXT_PRIMARY);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(UIConstants.TABLE_HEADER);
        header.setForeground(Color.WHITE);
        header.setFont(UIConstants.FONT_BOLD);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                JTable t,
                Object value,
                boolean selected,
                boolean focus,
                int row,
                int column
            ) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, value, selected, focus, row, column);
                lbl.setBackground(UIConstants.TABLE_HEADER);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(UIConstants.FONT_BOLD);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(255, 255, 255, 50)),
                    BorderFactory.createEmptyBorder(0, 6, 0, 6)
                ));
                return lbl;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                JTable t,
                Object value,
                boolean selected,
                boolean focus,
                int row,
                int column
            ) {
                java.awt.Component c = super.getTableCellRendererComponent(t, value, selected, focus, row, column);
                if (!selected) {
                    c.setBackground(row % 2 == 0 ? UIConstants.ROW_ODD : UIConstants.ROW_EVEN);
                }
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        JPanel paginationPanel = new JPanel(new BorderLayout());
        paginationPanel.setOpaque(false);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leftPanel.setOpaque(false);
        JLabel lblPageSize = new JLabel("Dòng/trang");
        lblPageSize.setFont(UIConstants.FONT_REGULAR);
        cboPageSize = new JComboBox<>(UIConstants.PAGE_SIZE_OPTIONS);
        cboPageSize.setFont(UIConstants.FONT_REGULAR);
        cboPageSize.setSelectedItem(pageSize);
        cboPageSize.addActionListener(e -> {
            if (updatingControls) {
                return;
            }
            Integer selected = (Integer) cboPageSize.getSelectedItem();
            if (selected != null) {
                setPageSize(selected);
            }
        });
        leftPanel.add(lblPageSize);
        leftPanel.add(cboPageSize);
        paginationPanel.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightPanel.setOpaque(false);

        btnPrev = makePagBtn("< Trước");
        lblPageInfo = new JLabel("Trang 1/1");
        lblPageInfo.setFont(UIConstants.FONT_REGULAR);
        JLabel lblJump = new JLabel("Tới trang");
        lblJump.setFont(UIConstants.FONT_REGULAR);
        cboPage = new JComboBox<>();
        cboPage.setFont(UIConstants.FONT_REGULAR);
        cboPage.setPreferredSize(new Dimension(90, 28));
        cboPage.addActionListener(e -> {
            if (updatingControls) {
                return;
            }
            Integer selected = (Integer) cboPage.getSelectedItem();
            if (selected != null) {
                goToPage(selected);
            }
        });
        btnNext = makePagBtn("Tiếp >");

        btnPrev.addActionListener(e -> goToPage(currentPage - 1));
        btnNext.addActionListener(e -> goToPage(currentPage + 1));

        rightPanel.add(btnPrev);
        rightPanel.add(lblPageInfo);
        rightPanel.add(lblJump);
        rightPanel.add(cboPage);
        rightPanel.add(btnNext);

        paginationPanel.add(rightPanel, BorderLayout.EAST);
        add(paginationPanel, BorderLayout.SOUTH);

        refreshPagination();
    }

    private JButton makePagBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UIConstants.FONT_SMALL);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 8, 28));
        return btn;
    }

    public void setData(List<Object[]> data) {
        this.allData = data != null ? new ArrayList<>(data) : new ArrayList<>();
        refreshPageCount();
        goToPage(1);
    }

    public void addRow(Object[] row) {
        allData.add(row);
        refreshPageCount();
        goToPage(totalPages);
    }

    public void clearData() {
        allData.clear();
        refreshPageCount();
        goToPage(1);
    }

    public void setPageSize(int newPageSize) {
        if (newPageSize <= 0) {
            return;
        }

        int firstRowIndex = (currentPage - 1) * pageSize;
        this.pageSize = newPageSize;
        refreshPageCount();
        int nextPage = allData.isEmpty() ? 1 : (firstRowIndex / pageSize) + 1;
        goToPage(nextPage);
    }

    /**
     * Cau hinh bang nhieu cot voi scroll ngang.
     * Chi can truyen danh sach do rong tung cot theo dung thu tu hien thi.
     */
    public void enableHorizontalScroll(int... widths) {
        setPreferredColumnWidths(widths);
    }

    public void setPreferredColumnWidths(int... widths) {
        if (widths == null) {
            return;
        }
        
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        TableColumnModel columnModel = table.getColumnModel();
        int totalWidth = 0;
        for (int i = 0; i < Math.min(widths.length, columnModel.getColumnCount()); i++) {
            int width = Math.max(60, widths[i]);
            columnModel.getColumn(i).setPreferredWidth(width);
            totalWidth += width;
        }

        int remaining = columnModel.getColumnCount() - widths.length;
        for (int i = widths.length; i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setPreferredWidth(90);
            totalWidth += 90;
        }

        if (remaining < 0) {
            totalWidth = table.getColumnCount() * 90;
        }

        table.setPreferredScrollableViewportSize(new Dimension(totalWidth, Math.max(320, table.getRowHeight() * pageSize)));
        revalidate();
    }

    private void refreshPageCount() {
        totalPages = Math.max(1, (int) Math.ceil((double) allData.size() / pageSize));
    }

    private void goToPage(int page) {
        if (page < 1) {
            page = 1;
        }
        if (page > totalPages) {
            page = totalPages;
        }

        currentPage = page;
        table.clearSelection();
        tableModel.setRowCount(0);

        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, allData.size());
        for (int i = start; i < end; i++) {
            tableModel.addRow(allData.get(i));
        }

        refreshPagination();
    }

    private void refreshPagination() {
        updatingControls = true;

        lblPageInfo.setText("Trang " + currentPage + "/" + totalPages + " (" + allData.size() + " dòng)");
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);

        cboPage.removeAllItems();
        for (int i = 1; i <= totalPages; i++) {
            cboPage.addItem(i);
        }
        cboPage.setSelectedItem(currentPage);
        cboPageSize.setSelectedItem(pageSize);

        updatingControls = false;
    }

    public JTable getTable() {
        return table;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public int getSelectedRow() {
        return table.getSelectedRow();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getRealIndex(int visualRow) {
        return (currentPage - 1) * pageSize + visualRow;
    }

    public List<Object[]> getAllData() {
        return allData;
    }
}
