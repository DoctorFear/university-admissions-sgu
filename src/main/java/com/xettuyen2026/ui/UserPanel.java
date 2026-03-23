package com.xettuyen2026.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.xettuyen2026.dao.UserDAO;
import com.xettuyen2026.entity.User;
import com.xettuyen2026.ui.common.ConfirmDialog;
import com.xettuyen2026.ui.common.MessageHelper;
import com.xettuyen2026.ui.common.PaginatedTable;
import com.xettuyen2026.ui.common.RoundedButton;
import com.xettuyen2026.ui.common.SearchBar;
import com.xettuyen2026.ui.common.UIConstants;

public class UserPanel extends JPanel {

    final UserDAO service;
    private PaginatedTable styledTable;
    private SearchBar searchBar;
    private List<User> users = new ArrayList<>();

    private static final String[] COLUMNS = {
        "STT", "Username", "Email", "Vai trò", "Kích hoạt", "Ngày tạo"
    };

    public UserPanel() {
        service = new UserDAO();
        setLayout(new BorderLayout(0, 10));
        setBackground(UIConstants.BG_MAIN);

        add(createToolbar(), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);

        loadData();
    }

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

    private JPanel createTable() {
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

        // Auto-load on first show
        SwingUtilities.invokeLater(this::loadData);

        return card;
    }

    private void loadData() {
        try {
            users = service.findAll();
            List<Object[]> rows = new ArrayList<>();

            for (User u : users) {
                rows.add(new Object[]{
                    u.getId(),
                    u.getUsername(),
                    u.getEmail(),
                    u.getRole(),
                    u.isEnabled() ? "Yes" : "No"
                });
            }
            styledTable.setData(rows);
        } catch (Exception e) {
            System.err.println(e);
            MessageHelper.showError(this, "Không thể tải dữ liệu thí sinh: " + e.getMessage());
        }
    }

    private void doAdd() {
        UserDialog dlg = new UserDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);

        if (dlg.isSaved()) {
            service.save(dlg.getEntity());
            loadData();
        }
    }

    private void doEdit() {
        int row = styledTable.getSelectedRow();
        if (row < 0) return;

        int real = styledTable.getRealIndex(row);
        User selected = users.get(real);

        UserDialog dlg = new UserDialog(SwingUtilities.getWindowAncestor(this), selected);
        dlg.setVisible(true);

        if (dlg.isSaved()) {
            service.update(dlg.getEntity());
            loadData();
        }
    }

    private void doDelete() {
        int row = styledTable.getSelectedRow();
        if (row < 0) return;

        int real = styledTable.getRealIndex(row);
        User selected = users.get(real);

        if (ConfirmDialog.show(this, "Delete user " + selected.getUsername() + "?")) {
            service.delete(selected.getId());
            loadData();
        }
    }

    private void doSearch() {
    
    }
    
    private void doImport() {
    
    }
}