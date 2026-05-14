package com.xettuyen2026.ui;

import com.xettuyen2026.entity.TohopMonthi;
import com.xettuyen2026.ui.common.RoundedButton;
import com.xettuyen2026.ui.common.UIConstants;
import com.xettuyen2026.util.TohopValidator;
import com.xettuyen2026.util.SubjectCode;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Dialog thêm/sửa tổ hợp môn xét tuyển.
 * 
 * Chức năng:
 * - Validate mã tổ hợp (1 chữ + 2 số)
 * - Validate 3 môn học
 * - Auto-generate tên tổ hợp
 * - Chống double-click save
 * - Normalize dữ liệu
 * 
 * @author Senior Developer
 */
public class TohopDialog extends JDialog {

    private boolean saved = false;
    private boolean saving = false; // Chống double-click
    private TohopMonthi entity;

    private JTextField txtMaTohop, txtTenTohop;
    private JComboBox<String> cboMon1, cboMon2, cboMon3;
    private RoundedButton btnSave;

    public TohopDialog(Window owner, TohopMonthi existing) {
        super(owner, existing == null ? "Thêm mới tổ hợp" : "Sửa tổ hợp",
                ModalityType.APPLICATION_MODAL);
        this.entity = existing;
        setSize(500, 400);
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
        JLabel title = new JLabel(entity == null ? "➕ Thêm mới tổ hợp" : "✏️ Sửa tổ hợp");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY);
        main.add(title, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;
        
        // Mã tổ hợp
        txtMaTohop = addField(form, gbc, row++, "Mã tổ hợp * (VD: A00, D01)");

        // Môn 1 - ComboBox auto-complete
        addComboLabel(form, gbc, row, "Môn 1 *");
        cboMon1 = addSubjectCombo(form, gbc, row++, 1);

        // Môn 2
        addComboLabel(form, gbc, row, "Môn 2 *");
        cboMon2 = addSubjectCombo(form, gbc, row++, 2);

        // Môn 3
        addComboLabel(form, gbc, row, "Môn 3 *");
        cboMon3 = addSubjectCombo(form, gbc, row++, 3);

        // Tên tổ hợp (auto-generate)
        JPanel namePanel = new JPanel(new BorderLayout(6, 0));
        namePanel.setOpaque(false);
        
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel lblName = new JLabel("Tên tổ hợp");
        lblName.setFont(UIConstants.FONT_BOLD);
        lblName.setPreferredSize(new Dimension(140, 30));
        form.add(lblName, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        txtTenTohop = new JTextField();
        txtTenTohop.setFont(UIConstants.FONT_REGULAR);
        txtTenTohop.setPreferredSize(new Dimension(0, 34));
        txtTenTohop.setEditable(false);
        txtTenTohop.setBackground(new Color(0xF0F0F0));
        namePanel.add(txtTenTohop, BorderLayout.CENTER);
        form.add(namePanel, gbc);
        row++;

        main.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        RoundedButton btnCancel = new RoundedButton("Hủy", new Color(0x757575));
        btnCancel.addActionListener(e -> dispose());

        btnSave = new RoundedButton("Lưu", UIConstants.PRIMARY);
        btnSave.addActionListener(e -> doSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        main.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(main);

        // Auto-generate tên tổ hợp khi chọn môn
        cboMon1.addActionListener(e -> updateTentohopAuto());
        cboMon2.addActionListener(e -> updateTentohopAuto());
        cboMon3.addActionListener(e -> updateTentohopAuto());
    }

    private JComboBox<String> addSubjectCombo(JPanel form, GridBagConstraints gbc, int row, int monNumber) {
        gbc.gridy = row;
        gbc.gridx = 1;
        gbc.weightx = 1;
        JComboBox<String> combo = new JComboBox<>();
        combo.setFont(UIConstants.FONT_REGULAR);
        combo.setPreferredSize(new Dimension(0, 34));
        combo.addItem("-- Chọn môn --");
        
        // Populate subject codes
        Map<String, String> subjects = SubjectCode.getSubjectMap();
        for (String code : subjects.keySet()) {
            combo.addItem(code + " - " + subjects.get(code));
        }
        
        form.add(combo, gbc);
        return combo;
    }

    private void addComboLabel(JPanel form, GridBagConstraints gbc, int row, String label) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BOLD);
        lbl.setPreferredSize(new Dimension(140, 30));
        form.add(lbl, gbc);
    }

    private JTextField addField(JPanel form, GridBagConstraints gbc, int row, String label) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BOLD);
        lbl.setPreferredSize(new Dimension(140, 30));
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
        txtMaTohop.setText(entity.getMatohop());
        txtMaTohop.setEnabled(false); // unique key - không sửa

        // Set combobox values
        setComboValue(cboMon1, entity.getMon1());
        setComboValue(cboMon2, entity.getMon2());
        setComboValue(cboMon3, entity.getMon3());

        txtTenTohop.setText(entity.getTentohop() != null ? entity.getTentohop() : "");
    }

    private void setComboValue(JComboBox<String> combo, String code) {
        if (code == null || code.isEmpty()) return;
        for (int i = 0; i < combo.getItemCount(); i++) {
            String item = combo.getItemAt(i);
            if (item.startsWith(code + " - ")) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void updateTentohopAuto() {
        String mon1 = getSelectedMonCode(cboMon1);
        String mon2 = getSelectedMonCode(cboMon2);
        String mon3 = getSelectedMonCode(cboMon3);

        if (!mon1.isEmpty() && !mon2.isEmpty() && !mon3.isEmpty()) {
            String tenTohop = TohopValidator.generateTohopName(mon1, mon2, mon3);
            txtTenTohop.setText(tenTohop);
        }
    }

    private String getSelectedMonCode(JComboBox<String> combo) {
        Object selected = combo.getSelectedItem();
        if (selected == null || selected.equals("-- Chọn môn --")) {
            return "";
        }
        String item = selected.toString();
        return item.split(" - ")[0].trim();
    }

    private void doSave() {
        // Chống double-click
        if (saving) {
            return;
        }
        saving = true;
        btnSave.setEnabled(false);

        try {
            String maTohop = txtMaTohop.getText().trim();
            String mon1 = getSelectedMonCode(cboMon1);
            String mon2 = getSelectedMonCode(cboMon2);
            String mon3 = getSelectedMonCode(cboMon3);
            String tenTohop = txtTenTohop.getText().trim();

            // Validate mã tổ hợp
            String maTohopErr = TohopValidator.getMaTohopError(maTohop);
            if (maTohopErr != null) {
                JOptionPane.showMessageDialog(this, maTohopErr, "Lỗi mã tổ hợp", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validate 3 môn
            String subjectErr = TohopValidator.getSubjectsError(mon1, mon2, mon3);
            if (subjectErr != null) {
                JOptionPane.showMessageDialog(this, subjectErr, "Lỗi môn học", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Normalize dữ liệu
            maTohop = TohopValidator.normalizeMaTohop(maTohop);
            mon1 = TohopValidator.normalizeSubject(mon1);
            mon2 = TohopValidator.normalizeSubject(mon2);
            mon3 = TohopValidator.normalizeSubject(mon3);

            TohopMonthi result = new TohopMonthi();
            result.setMatohop(maTohop);
            result.setMon1(mon1);
            result.setMon2(mon2);
            result.setMon3(mon3);
            result.setTentohop(tenTohop.isEmpty() ? TohopValidator.generateTohopName(mon1, mon2, mon3) : tenTohop);

            this.entity = result;
            saved = true;
            dispose();
        } finally {
            saving = false;
            btnSave.setEnabled(true);
        }
    }

    public boolean isSaved() { return saved; }
    public TohopMonthi getEntity() { return entity; }
}
