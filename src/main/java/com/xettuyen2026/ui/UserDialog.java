package com.xettuyen2026.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.xettuyen2026.entity.User;
import com.xettuyen2026.ui.common.RoundedButton;
import com.xettuyen2026.ui.common.UIConstants;

public class UserDialog extends JDialog {

    private boolean saved = false;
    private User entity;

    private JTextField txtUsername, txtEmail;
    private JPasswordField txtPassword;
    private JComboBox<String> cboRole;
    private JCheckBox chkEnabled;

    public UserDialog(Window owner, User existing) {
        super(owner, existing == null ? "Thêm User" : "Sửa User", ModalityType.APPLICATION_MODAL);
        this.entity = existing;

        setSize(420, 340);
        setLocationRelativeTo(owner);
        setResizable(false);

        initUI();
        if (existing != null) populateData();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));
        main.setBackground(Color.WHITE);

        JLabel title = new JLabel(entity == null ? "➕ Thêm User" : "✏️ Sửa User");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY);
        main.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        txtUsername = addField(form, gbc, row++, "Username *");
        txtEmail = addField(form, gbc, row++, "Email");

        txtPassword = new JPasswordField();
        addComponent(form, gbc, row++, "Password" + (entity == null ? " *" : ""), txtPassword);

        cboRole = new JComboBox<>(new String[]{"ADMIN", "STAFF", "USER"});
        addComponent(form, gbc, row++, "Role", cboRole);

        chkEnabled = new JCheckBox("Enabled", true);
        chkEnabled.setOpaque(false);
        gbc.gridy = row;
        gbc.gridx = 1;
        form.add(chkEnabled, gbc);

        main.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);

        RoundedButton btnCancel = new RoundedButton("Hủy", Color.GRAY);
        btnCancel.addActionListener(e -> dispose());

        RoundedButton btnSave = new RoundedButton("Lưu", UIConstants.PRIMARY);
        btnSave.addActionListener(e -> doSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        main.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(main);
    }

    private JTextField addField(JPanel form, GridBagConstraints gbc, int row, String label) {
        JTextField txt = new JTextField();
        addComponent(form, gbc, row, label, txt);
        return txt;
    }

    private void addComponent(JPanel form, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridy = row;
        gbc.gridx = 0;
        form.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        comp.setPreferredSize(new Dimension(0, 30));
        form.add(comp, gbc);
    }

    private void populateData() {
        txtUsername.setText(entity.getUsername());
        txtUsername.setEnabled(false);

        txtEmail.setText(entity.getEmail());
        cboRole.setSelectedItem(entity.getRole());
        chkEnabled.setSelected(entity.isEnabled());
    }

    private void doSave() {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username required");
            return;
        }

        if (entity == null && password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password required");
            return;
        }

    User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setRole((String) cboRole.getSelectedItem());
        u.setEnabled(chkEnabled.isSelected());

        if (!password.isEmpty()) {
            u.setPassword(password); // raw, will hash in service
        }

        if (entity != null) u.setId(entity.getId());

        this.entity = u;
        this.saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public User getEntity() { return entity; }
}