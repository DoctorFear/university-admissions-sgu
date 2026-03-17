package com.xettuyen2026.ui;

import com.xettuyen2026.entity.NganhTohop;
import com.xettuyen2026.ui.common.RoundedButton;
import com.xettuyen2026.ui.common.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Dialog thêm/sửa ngành-tổ hợp.
 */
public class NganhTohopDialog extends JDialog {

    private boolean saved = false;
    private NganhTohop entity;
    private Map<String, String> nganhMap;

    private JComboBox<String> cboMaNganh;
    private JTextField txtMaTohop, txtMon1, txtMon2, txtMon3;
    private JSpinner spnHs1, spnHs2, spnHs3;
    private JTextField txtDoLech;

    public NganhTohopDialog(Window owner, NganhTohop existing, Map<String, String> nganhMap) {
        super(owner, existing == null ? "Thêm mới ngành-tổ hợp" : "Sửa ngành-tổ hợp",
                ModalityType.APPLICATION_MODAL);
        this.entity = existing;
        this.nganhMap = nganhMap;
        setSize(560, 480);
        setLocationRelativeTo(owner);
        setResizable(false);
        initUI();
        if (existing != null) populateData();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setBackground(Color.WHITE);
        main.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));

        // Title
        JLabel title = new JLabel(entity == null ? "➕ Thêm mới ngành-tổ hợp" : "✏️ Sửa ngành-tổ hợp");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY);
        main.add(title, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Mã ngành (ComboBox)
        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0;
        JLabel lblNganh = new JLabel("Mã ngành *");
        lblNganh.setFont(UIConstants.FONT_BOLD);
        lblNganh.setPreferredSize(new Dimension(120, 30));
        form.add(lblNganh, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        cboMaNganh = new JComboBox<>();
        cboMaNganh.setFont(UIConstants.FONT_REGULAR);
        cboMaNganh.setPreferredSize(new Dimension(0, 34));
        for (Map.Entry<String, String> entry : nganhMap.entrySet()) {
            cboMaNganh.addItem(entry.getKey() + " - " + entry.getValue());
        }
        form.add(cboMaNganh, gbc);
        row++;

        // Mã tổ hợp
        txtMaTohop = addField(form, gbc, row++, "Mã tổ hợp *");

        // Môn 1 + Hệ số 1
        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0;
        JLabel lblMon1 = new JLabel("Môn 1 *");
        lblMon1.setFont(UIConstants.FONT_BOLD);
        lblMon1.setPreferredSize(new Dimension(120, 30));
        form.add(lblMon1, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        JPanel panMon1 = new JPanel(new BorderLayout(6, 0));
        panMon1.setOpaque(false);
        txtMon1 = new JTextField();
        txtMon1.setFont(UIConstants.FONT_REGULAR);
        txtMon1.setPreferredSize(new Dimension(0, 34));
        panMon1.add(txtMon1, BorderLayout.CENTER);
        JPanel hsPanel1 = createHsPanel("HS:");
        spnHs1 = (JSpinner) hsPanel1.getComponent(1);
        panMon1.add(hsPanel1, BorderLayout.EAST);
        form.add(panMon1, gbc);
        row++;

        // Môn 2 + Hệ số 2
        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0;
        JLabel lblMon2 = new JLabel("Môn 2 *");
        lblMon2.setFont(UIConstants.FONT_BOLD);
        lblMon2.setPreferredSize(new Dimension(120, 30));
        form.add(lblMon2, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        JPanel panMon2 = new JPanel(new BorderLayout(6, 0));
        panMon2.setOpaque(false);
        txtMon2 = new JTextField();
        txtMon2.setFont(UIConstants.FONT_REGULAR);
        txtMon2.setPreferredSize(new Dimension(0, 34));
        panMon2.add(txtMon2, BorderLayout.CENTER);
        JPanel hsPanel2 = createHsPanel("HS:");
        spnHs2 = (JSpinner) hsPanel2.getComponent(1);
        panMon2.add(hsPanel2, BorderLayout.EAST);
        form.add(panMon2, gbc);
        row++;

        // Môn 3 + Hệ số 3
        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0;
        JLabel lblMon3 = new JLabel("Môn 3 *");
        lblMon3.setFont(UIConstants.FONT_BOLD);
        lblMon3.setPreferredSize(new Dimension(120, 30));
        form.add(lblMon3, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        JPanel panMon3 = new JPanel(new BorderLayout(6, 0));
        panMon3.setOpaque(false);
        txtMon3 = new JTextField();
        txtMon3.setFont(UIConstants.FONT_REGULAR);
        txtMon3.setPreferredSize(new Dimension(0, 34));
        panMon3.add(txtMon3, BorderLayout.CENTER);
        JPanel hsPanel3 = createHsPanel("HS:");
        spnHs3 = (JSpinner) hsPanel3.getComponent(1);
        panMon3.add(hsPanel3, BorderLayout.EAST);
        form.add(panMon3, gbc);
        row++;

        // Độ lệch
        txtDoLech = addField(form, gbc, row++, "Độ lệch");
        txtDoLech.setText("0.00");

        main.add(new JScrollPane(form), BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        RoundedButton btnCancel = new RoundedButton("Hủy", new Color(0x757575));
        btnCancel.addActionListener(e -> dispose());

        RoundedButton btnSave = new RoundedButton("💾 Lưu", UIConstants.PRIMARY);
        btnSave.addActionListener(e -> doSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        main.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private JPanel createHsPanel(String label) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        panel.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BOLD);
        panel.add(lbl);
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
        spinner.setFont(UIConstants.FONT_REGULAR);
        spinner.setPreferredSize(new Dimension(50, 34));
        panel.add(spinner);
        return panel;
    }

    private JTextField addField(JPanel form, GridBagConstraints gbc, int row, String label) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BOLD);
        lbl.setPreferredSize(new Dimension(120, 30));
        form.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        JTextField field = new JTextField();
        field.setFont(UIConstants.FONT_REGULAR);
        field.setPreferredSize(new Dimension(0, 34));
        form.add(field, gbc);
        return field;
    }

    private void populateData() {
        if (entity == null) return;

        // Select the nganh in combo
        for (int i = 0; i < cboMaNganh.getItemCount(); i++) {
            String item = cboMaNganh.getItemAt(i);
            if (item.startsWith(entity.getManganh() + " ")) {
                cboMaNganh.setSelectedIndex(i);
                break;
            }
        }
        cboMaNganh.setEnabled(false);

        txtMaTohop.setText(entity.getMatohop());
        txtMaTohop.setEnabled(false);

        txtMon1.setText(entity.getThMon1());
        txtMon2.setText(entity.getThMon2());
        txtMon3.setText(entity.getThMon3());
        if (entity.getHsmon1() != null) spnHs1.setValue(entity.getHsmon1().intValue());
        if (entity.getHsmon2() != null) spnHs2.setValue(entity.getHsmon2().intValue());
        if (entity.getHsmon3() != null) spnHs3.setValue(entity.getHsmon3().intValue());
        if (entity.getDolech() != null) txtDoLech.setText(entity.getDolech().toPlainString());
    }

    private void doSave() {
        if (cboMaNganh.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn mã ngành!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maNganh = ((String) cboMaNganh.getSelectedItem()).split(" - ")[0].trim();
        String maTohop = txtMaTohop.getText().trim();
        String mon1 = txtMon1.getText().trim().toUpperCase();
        String mon2 = txtMon2.getText().trim().toUpperCase();
        String mon3 = txtMon3.getText().trim().toUpperCase();

        if (maTohop.isEmpty() || mon1.isEmpty() || mon2.isEmpty() || mon3.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập đầy đủ Mã tổ hợp, Môn 1, 2, 3!",
                "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal doLech;
        try {
            doLech = new BigDecimal(txtDoLech.getText().trim());
        } catch (NumberFormatException e) {
            doLech = BigDecimal.ZERO;
        }

        NganhTohop result = new NganhTohop();
        result.setManganh(maNganh);
        result.setMatohop(maTohop);
        result.setThMon1(mon1);
        result.setHsmon1((byte) ((int) spnHs1.getValue()));
        result.setThMon2(mon2);
        result.setHsmon2((byte) ((int) spnHs2.getValue()));
        result.setThMon3(mon3);
        result.setHsmon3((byte) ((int) spnHs3.getValue()));
        result.setDolech(doLech);

        this.entity = result;
        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public NganhTohop getEntity() { return entity; }
}
