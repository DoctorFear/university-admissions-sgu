package com.xettuyen2026.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.xettuyen2026.service.AuthService;
import com.xettuyen2026.ui.common.RoundedButton;
import com.xettuyen2026.ui.common.UIConstants;

/**
 * Màn hình Login - gradient background, centered white card.
 */
public class LoginForm extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private RoundedButton btnLogin;
    private JLabel lblError;

    public LoginForm() {
        setTitle("Đăng nhập - Phần mềm Tuyển sinh SGU 2026");
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        // Main panel with gradient background
        JPanel bgPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, UIConstants.SIDEBAR_BG,
                        getWidth(), getHeight(), UIConstants.PRIMARY);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        bgPanel.setLayout(new GridBagLayout());

        // Card container
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fill(new RoundRectangle2D.Float(4, 4, getWidth() - 4, getHeight() - 4, 32, 32));
                // Card
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 4, 32, 32));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(420, 520));
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 40, 6, 40);

        // ── Logo / Title ──
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 40, 0, 40);
        JLabel lblIcon = new JLabel("🎓", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        card.add(lblIcon, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(4, 40, 0, 40);
        JLabel lblTitle = new JLabel("TRƯỜNG ĐẠI HỌC SÀI GÒN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(UIConstants.PRIMARY_DARK);
        card.add(lblTitle, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 40, 16, 40);
        JLabel lblSub = new JLabel("Phần mềm Quản lý Tuyển sinh 2026", SwingConstants.CENTER);
        lblSub.setFont(UIConstants.FONT_REGULAR);
        lblSub.setForeground(UIConstants.TEXT_SECONDARY);
        card.add(lblSub, gbc);

        // ── Divider ──
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 40, 12, 40);
        JSeparator sep = new JSeparator();
        sep.setForeground(UIConstants.BORDER_LIGHT);
        card.add(sep, gbc);

        // ── Username ──
        gbc.gridy = 4;
        gbc.insets = new Insets(4, 40, 4, 40);
        JLabel lblUser = new JLabel("👤  Tên đăng nhập");
        lblUser.setFont(UIConstants.FONT_BOLD);
        lblUser.setForeground(UIConstants.TEXT_PRIMARY);
        card.add(lblUser, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 40, 12, 40);
        txtUsername = createStyledField("Nhập tên đăng nhập...");
        card.add(txtUsername, gbc);

        // ── Password ──
        gbc.gridy = 6;
        gbc.insets = new Insets(4, 40, 4, 40);
        JLabel lblPass = new JLabel("🔒  Mật khẩu");
        lblPass.setFont(UIConstants.FONT_BOLD);
        lblPass.setForeground(UIConstants.TEXT_PRIMARY);
        card.add(lblPass, gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(0, 40, 16, 40);
        txtPassword = new JPasswordField();
        txtPassword.setFont(UIConstants.FONT_REGULAR);
        txtPassword.setPreferredSize(new Dimension(0, 40));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT, 1, true),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        txtPassword.addActionListener(e -> doLogin());
        card.add(txtPassword, gbc);

        // ── Error label ──
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 40, 4, 40);
        lblError = new JLabel(" ");
        lblError.setFont(UIConstants.FONT_SMALL);
        lblError.setForeground(UIConstants.DANGER);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lblError, gbc);

        // ── Login Button ──
        gbc.gridy = 9;
        gbc.insets = new Insets(4, 40, 24, 40);
        btnLogin = new RoundedButton("Đăng nhập", UIConstants.PRIMARY);
        btnLogin.setPreferredSize(new Dimension(0, 44));
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.addActionListener(e -> doLogin());
        card.add(btnLogin, gbc);

        bgPanel.add(card);
        setContentPane(bgPanel);
    }

    private JTextField createStyledField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(UIConstants.TEXT_HINT);
                    g2.setFont(UIConstants.FONT_REGULAR);
                    g2.drawString(placeholder, 12, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        field.setFont(UIConstants.FONT_REGULAR);
        field.setPreferredSize(new Dimension(0, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT, 1, true),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        return field;
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        AuthService authService = new AuthService();
        var user = authService.login(username, password);

        if (user == null) {
            lblError.setText("Sai tài khoản hoặc mật khẩu!");
            return;
        }

        lblError.setText(" ");
        dispose();

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(user.getUsername());
            frame.setVisible(true);
        });
        // // TODO: Implement proper auth
        // // For now, allow any login
        // lblError.setText(" ");
        // dispose();
        // SwingUtilities.invokeLater(() -> {
        //     MainFrame frame = new MainFrame(username);
        //     frame.setVisible(true);
        // });
    }
}
