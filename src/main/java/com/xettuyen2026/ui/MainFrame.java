package com.xettuyen2026.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.xettuyen2026.ui.common.UIConstants;

/**
 * MainFrame với sidebar navigation bên trái, header trên, content area trung tâm.
 */
public class MainFrame extends JFrame {

    private String currentUser;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JLabel lblModuleName;
    private JPanel sidebarPanel;
    private Map<String, JPanel> menuItems = new LinkedHashMap<>();
    private JPanel activeMenuItem = null;

    // Panel references
    private JPanel dashboardPanel;

    public MainFrame(String username) {
        this.currentUser = username;
        setTitle("Phần mềm Quản lý Tuyển sinh SGU 2026");
        setSize(1400, 850);
        setMinimumSize(new Dimension(1200, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(UIConstants.BG_MAIN);

        // ── SIDEBAR ──
        sidebarPanel = createSidebar();
        mainPanel.add(sidebarPanel, BorderLayout.WEST);

        // ── RIGHT AREA (Header + Content) ──
        JPanel rightPanel = new JPanel(new BorderLayout(0, 0));
        rightPanel.setBackground(UIConstants.BG_MAIN);

        // Header
        rightPanel.add(createHeader(), BorderLayout.NORTH);

        // Content Area
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIConstants.BG_MAIN);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // Add module panels
        addModulePanels();

        rightPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);

        // Select dashboard by default
        selectMenuItem("dashboard");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 0));
        sidebar.setBackground(UIConstants.SIDEBAR_BG);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // ── Brand ──
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(20, 16, 16, 16));
        brandPanel.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 80));

        JLabel lblBrand = new JLabel("SGU Tuyển sinh");
        javax.swing.Icon brandIcon = UIConstants.getWhiteIcon(UIConstants.ICON_STUDENT, 20, 20);
        if (brandIcon != null) {
            lblBrand.setIcon(brandIcon);
            lblBrand.setIconTextGap(8);
        }
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblBrand.setForeground(Color.WHITE);
        lblBrand.setAlignmentX(Component.CENTER_ALIGNMENT); // chỉ giữ dòng này
        brandPanel.add(lblBrand);

        JLabel lblYear = new JLabel("Năm 2026");
        lblYear.setFont(UIConstants.FONT_SMALL);
        lblYear.setForeground(new Color(255, 255, 255, 150));
        lblYear.setAlignmentX(Component.CENTER_ALIGNMENT); // căn giữa luôn
        brandPanel.add(lblYear);


        sidebar.add(brandPanel);

        // ── Divider ──
        sidebar.add(createDivider());
        sidebar.add(Box.createVerticalStrut(8));

        // ── Menu Items ──
        addMenuItem(sidebar, "dashboard",    UIConstants.ICON_DASHBOARD,   "Trang chủ");
        addMenuItem(sidebar, "user",         UIConstants.ICON_USER,        "Người dùng");
        addMenuItem(sidebar, "thisinh",      UIConstants.ICON_STUDENT,     "Thí sinh");
        addMenuItem(sidebar, "nganh",        UIConstants.ICON_MAJOR,       "Ngành tuyển sinh");
        addMenuItem(sidebar, "tohop",        UIConstants.ICON_COMBO,       "Tổ hợp môn");
        addMenuItem(sidebar, "nganh_tohop",  UIConstants.ICON_MAJOR_COMBO, "Ngành - Tổ hợp");
        addMenuItem(sidebar, "diemthi",      UIConstants.ICON_SCORE,       "Điểm thi");
        addMenuItem(sidebar, "diemcong",     UIConstants.ICON_BONUS,       "Điểm cộng");
        addMenuItem(sidebar, "nguyenvong",   UIConstants.ICON_WISH,        "Nguyện vọng & XT");
        addMenuItem(sidebar, "ketqua",       UIConstants.ICON_RESULT,      "Kết quả xét tuyển");
        addMenuItem(sidebar, "bangquydoi",   UIConstants.ICON_CONVERT,     "Bảng quy đổi");

        sidebar.add(Box.createVerticalGlue());

        // ── Logout ──
        sidebar.add(createDivider());
        sidebar.add(Box.createVerticalStrut(4));

        JPanel logoutItem = createMenuItemPanel(UIConstants.ICON_LOGOUT, "Đăng xuất");
        logoutItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { doLogout(); }
            @Override
            public void mouseEntered(MouseEvent e) { logoutItem.setBackground(UIConstants.SIDEBAR_HOVER); }
            @Override
            public void mouseExited(MouseEvent e) { logoutItem.setBackground(UIConstants.SIDEBAR_BG); }
        });
        sidebar.add(logoutItem);
        sidebar.add(Box.createVerticalStrut(12));

        return sidebar;
    }

    private void addMenuItem(JPanel sidebar, String key, String icon, String text) {
        JPanel item = createMenuItemPanel(icon, text);
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { selectMenuItem(key); }
            @Override
            public void mouseEntered(MouseEvent e) {
                if (item != activeMenuItem) item.setBackground(UIConstants.SIDEBAR_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (item != activeMenuItem) item.setBackground(UIConstants.SIDEBAR_BG);
            }
        });
        menuItems.put(key, item);
        sidebar.add(item);
    }

    private JPanel createMenuItemPanel(String icon, String text) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIConstants.SIDEBAR_BG);
        panel.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 44));
        panel.setPreferredSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 44));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 12));

        JLabel lbl = new JLabel(text);
        if (icon != null && !icon.isEmpty()) {
            lbl.setIcon(UIConstants.getWhiteIcon(icon, 20, 20));
            lbl.setIconTextGap(12);
        }
        lbl.setFont(UIConstants.FONT_MENU);
        lbl.setForeground(new Color(255, 255, 255, 210));
        panel.add(lbl, BorderLayout.CENTER);

        return panel;
    }

    private JSeparator createDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 40));
        sep.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 1));
        return sep;
    }

    private void selectMenuItem(String key) {
        // Reset previous active
        if (activeMenuItem != null) {
            activeMenuItem.setBackground(UIConstants.SIDEBAR_BG);
        }

        JPanel item = menuItems.get(key);
        if (item != null) {
            item.setBackground(UIConstants.SIDEBAR_ACTIVE);
            activeMenuItem = item;
        }

        // Show card
        cardLayout.show(contentPanel, key);
        if ("dashboard".equals(key) && dashboardPanel instanceof DashboardPanel) {
            ((DashboardPanel) dashboardPanel).refreshData();
        }

        // Update header
        String[] titles = {
            "dashboard", "Trang chủ",
            "user", "Quản lý Người dùng",
            "thisinh", "Quản lý Thí sinh",
            "nganh", "Quản lý Ngành tuyển sinh",
            "tohop", "Quản lý Tổ hợp môn",
            "nganh_tohop", "Quản lý Ngành - Tổ hợp",
            "diemthi", "Quản lý Điểm thi",
            "diemcong", "Quản lý Điểm cộng",
            "nguyenvong", "Quản lý Nguyện vọng & Xét tuyển",
            "ketqua", "Kết quả xét tuyển",
            "bangquydoi", "Bảng quy đổi"
        };
        for (int i = 0; i < titles.length; i += 2) {
            if (titles[i].equals(key)) {
                lblModuleName.setText(titles[i + 1]);
                break;
            }
        }
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(0, UIConstants.HEADER_HEIGHT));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(0, 20, 0, 20)));

        // Module name (left)
        lblModuleName = new JLabel("Trang chủ");
        lblModuleName.setFont(UIConstants.FONT_HEADER);
        lblModuleName.setForeground(UIConstants.TEXT_PRIMARY);
        header.add(lblModuleName, BorderLayout.WEST);

        // User info (right)
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        userPanel.setOpaque(false);

        JLabel lblUserIcon = new JLabel();
        javax.swing.Icon uIcon = UIConstants.getIcon(UIConstants.ICON_USER, 20, 20);
        if (uIcon != null) {
            lblUserIcon.setIcon(uIcon);
        } else {
            lblUserIcon.setText("👤");
            lblUserIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        }

        JLabel lblUsername = new JLabel(currentUser);
        lblUsername.setFont(UIConstants.FONT_BOLD);
        lblUsername.setForeground(UIConstants.TEXT_PRIMARY);

        userPanel.add(lblUserIcon);
        userPanel.add(lblUsername);

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem logoutItem = new JMenuItem("Đăng xuất");
        logoutItem.addActionListener(e -> doLogout());
        popupMenu.add(logoutItem);

        userPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                popupMenu.show(userPanel, 0, userPanel.getHeight());
            }
        });

        header.add(userPanel, BorderLayout.EAST);
        return header;
    }

    private void addModulePanels() {
        // Dashboard
        dashboardPanel = new DashboardPanel();
        contentPanel.add(dashboardPanel, "dashboard");

        // Module panels - lazy init with placeholder for unfinished ones
        contentPanel.add(new ThiSinhPanel(), "thisinh");
        contentPanel.add(new DiemThiPanel(), "diemthi");
        contentPanel.add(new NguyenVongPanel(), "nguyenvong");
        contentPanel.add(new NganhPanel(), "nganh");
        contentPanel.add(new UserPanel(), "user");
        contentPanel.add(new TohopPanel(), "tohop");
        contentPanel.add(new NganhTohopPanel(), "nganh_tohop");
        contentPanel.add(new DiemCongPanel(), "diemcong");
        contentPanel.add(new BangQuydoiPanel(), "bangquydoi");
        contentPanel.add(new KetQuaXetTuyenPanel(), "ketqua");
    }

    private JPanel createPlaceholderPanel(String title, String icon) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIConstants.BG_MAIN);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(40, 60, 40, 60)));

        JLabel iconLbl = new JLabel(icon, SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(iconLbl);

        card.add(Box.createVerticalStrut(12));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(UIConstants.FONT_TITLE);
        titleLbl.setForeground(UIConstants.TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLbl);

        card.add(Box.createVerticalStrut(8));

        JLabel subLbl = new JLabel("Module đang được phát triển...", SwingConstants.CENTER);
        subLbl.setFont(UIConstants.FONT_REGULAR);
        subLbl.setForeground(UIConstants.TEXT_SECONDARY);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subLbl);

        panel.add(card);
        return panel;
    }

    private void doLogout() {
        int result = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn đăng xuất?", "Đăng xuất",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginForm login = new LoginForm();
                login.setVisible(true);
            });
        }
    }
}
