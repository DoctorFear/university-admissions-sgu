package com.xettuyen2026.ui;

import com.xettuyen2026.entity.DiemThiXetTuyen;
import com.xettuyen2026.service.DiemThiService;
import com.xettuyen2026.ui.common.RoundedButton;
import com.xettuyen2026.ui.common.UIConstants;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;

public class DiemThiDialog extends JDialog {

    private boolean saved = false;
    private DiemThiXetTuyen entity;
    private final String phuongThuc;

    private JTextField txtCccd;
    private JTextField txtSobaodanh;

    private JTextField txtTo;
    private JTextField txtVa;
    private JTextField txtLi;
    private JTextField txtHo;
    private JTextField txtSi;
    private JTextField txtSu;
    private JTextField txtDi;
    private JTextField txtGdcd;
    private JTextField txtN1Thi;
    private JTextField txtN1Cc;
    private JTextField txtCncn;
    private JTextField txtCnnn;
    private JTextField txtTi;
    private JTextField txtKtpl;
    private JTextField txtNk1;
    private JTextField txtNk2;
    private JTextField txtNk3;
    private JTextField txtNk4;
    private JTextField txtNk5;
    private JTextField txtNk6;

    private JTextField txtNl1;

    public DiemThiDialog(Window owner, DiemThiXetTuyen existing, String phuongThuc) {
        super(
            owner,
            existing == null
                ? "Thêm điểm thi " + DiemThiService.getPhuongThucLabel(phuongThuc)
                : "Sửa điểm thi " + DiemThiService.getPhuongThucLabel(phuongThuc),
            ModalityType.APPLICATION_MODAL
        );
        this.entity = existing;
        this.phuongThuc = DiemThiService.getPhuongThucLabel(phuongThuc);

        setSize(560, dialogHeight());
        setLocationRelativeTo(owner);
        setResizable(false);

        initUI();
        if (existing != null) {
            populateData();
        }
    }

    private int dialogHeight() {
        if (isDgnl()) {
            return 280;
        }
        if (isVsat()) {
            return 520;
        }
        return 760;
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setBackground(Color.WHITE);
        main.setBorder(BorderFactory.createEmptyBorder(50, 24, 16, 24));

        JLabel title = new JLabel(
            entity == null
                ? "Thêm điểm " + phuongThuc
                : "Sửa điểm " + phuongThuc
        );
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY);
        main.add(title, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(createForm());
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        main.add(scrollPane, BorderLayout.CENTER);
        main.add(createButtonPanel(), BorderLayout.SOUTH);

        setContentPane(main);
    }

    private JPanel createForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        txtCccd = addField(form, gbc, row++, "CCCD *");
        if (entity != null) {
            txtCccd.setEnabled(false);
        }
        txtSobaodanh = addField(form, gbc, row++, "Số báo danh");

        if (isDgnl()) {
            addSection(form, gbc, row++, "Điểm đánh giá năng lực");
            txtNl1 = addField(form, gbc, row++, "Điểm ĐGNL");
            return form;
        }

        addSection(form, gbc, row++, "Môn bắt buộc");
        txtTo = addField(form, gbc, row++, "Toán");
        txtVa = addField(form, gbc, row++, "Ngữ văn");

        addSection(form, gbc, row++, "Các môn khoa học");
        txtLi = addField(form, gbc, row++, "Vật lý");
        txtHo = addField(form, gbc, row++, "Hóa học");
        txtSi = addField(form, gbc, row++, "Sinh học");
        txtSu = addField(form, gbc, row++, "Lịch sử");
        txtDi = addField(form, gbc, row++, "Địa lý");
        txtN1Thi = addField(form, gbc, row++, "Tiếng Anh");

        if (!isVsat()) {
            addSection(form, gbc, row++, "Môn bổ sung THPT");
            txtGdcd = addField(form, gbc, row++, "GDCD");
            txtN1Cc = addField(form, gbc, row++, "Ngoại ngữ (chứng chỉ)");
            txtCncn = addField(form, gbc, row++, "Công nghệ công nghiệp");
            txtCnnn = addField(form, gbc, row++, "Công nghệ ngôn ngữ");
            txtTi = addField(form, gbc, row++, "Tin học");
            txtKtpl = addField(form, gbc, row++, "KTPL");

            addSection(form, gbc, row++, "Điểm năng khiếu");
            txtNk1 = addField(form, gbc, row++, "NK1");
            txtNk2 = addField(form, gbc, row++, "NK2");
            txtNk3 = addField(form, gbc, row++, "NK3");
            txtNk4 = addField(form, gbc, row++, "NK4");
            txtNk5 = addField(form, gbc, row++, "NK5");
            txtNk6 = addField(form, gbc, row++, "NK6");
        }

        return form;
    }

    private JTextField addField(JPanel form, GridBagConstraints gbc, int row, String label) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.gridwidth = 1;

        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BOLD);
        lbl.setPreferredSize(new Dimension(180, 30));
        form.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        JTextField field = new JTextField();
        field.setFont(UIConstants.FONT_REGULAR);
        field.setPreferredSize(new Dimension(0, 32));
        form.add(field, gbc);
        return field;
    }

    private void addSection(JPanel form, GridBagConstraints gbc, int row, String text) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;

        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_BOLD);
        lbl.setForeground(UIConstants.PRIMARY);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_LIGHT));
        form.add(lbl, gbc);

        gbc.gridwidth = 1;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setOpaque(false);

        RoundedButton btnCancel = new RoundedButton("Hủy", new Color(0x757575));
        btnCancel.addActionListener(e -> dispose());

        RoundedButton btnSave = new RoundedButton("Lưu", UIConstants.PRIMARY);
        btnSave.addActionListener(e -> doSave());

        panel.add(btnCancel);
        panel.add(btnSave);
        return panel;
    }

    private void populateData() {
        txtCccd.setText(entity.getCccd());
        txtSobaodanh.setText(nvlStr(entity.getSobaodanh()));

        if (isDgnl()) {
            setText(txtNl1, entity.getNl1());
            return;
        }

        setText(txtTo, entity.getTo());
        setText(txtVa, entity.getVa());
        setText(txtLi, entity.getLi());
        setText(txtHo, entity.getHo());
        setText(txtSi, entity.getSi());
        setText(txtSu, entity.getSu());
        setText(txtDi, entity.getDi());
        setText(txtN1Thi, entity.getN1Thi());

        if (!isVsat()) {
            setText(txtGdcd, entity.getGdcd());
            setText(txtN1Cc, entity.getN1Cc());
            setText(txtCncn, entity.getCncn());
            setText(txtCnnn, entity.getCnnn());
            setText(txtTi, entity.getTi());
            setText(txtKtpl, entity.getKtpl());
            setText(txtNk1, entity.getNk1());
            setText(txtNk2, entity.getNk2());
            setText(txtNk3, entity.getNk3());
            setText(txtNk4, entity.getNk4());
            setText(txtNk5, entity.getNk5());
            setText(txtNk6, entity.getNk6());
        }
    }

    private void doSave() {
        String cccd = txtCccd.getText().trim();
        if (cccd.isEmpty()) {
            warn("CCCD không được để trống.");
            return;
        }

        DiemThiXetTuyen result = entity != null ? entity : new DiemThiXetTuyen();
        result.setCccd(cccd);
        result.setSobaodanh(emptyToNull(txtSobaodanh.getText()));
        result.setdPhuongthuc(phuongThuc);

        try {
            if (isDgnl()) {
                result.setNl1(parseDecimal(txtNl1, "Điểm ĐGNL", false));
            } else {
                result.setTo(parseDecimal(txtTo, "Toán", false));
                result.setVa(parseDecimal(txtVa, "Ngữ văn", false));
                result.setLi(parseDecimal(txtLi, "Vật lý", false));
                result.setHo(parseDecimal(txtHo, "Hóa học", false));
                result.setSi(parseDecimal(txtSi, "Sinh học", false));
                result.setSu(parseDecimal(txtSu, "Lịch sử", false));
                result.setDi(parseDecimal(txtDi, "Địa lý", false));
                result.setN1Thi(parseDecimal(txtN1Thi, "Tiếng Anh", false));

                if (isVsat()) {
                    result.setGdcd(null);
                    result.setN1Cc(null);
                    result.setCncn(null);
                    result.setCnnn(null);
                    result.setTi(null);
                    result.setKtpl(null);
                    result.setNk1(null);
                    result.setNk2(null);
                    result.setNk3(null);
                    result.setNk4(null);
                    result.setNk5(null);
                    result.setNk6(null);
                } else {
                    result.setGdcd(parseDecimal(txtGdcd, "GDCD", false));
                    result.setN1Cc(parseDecimal(txtN1Cc, "Ngoại ngữ (chứng chỉ)", false));
                    result.setCncn(parseDecimal(txtCncn, "Công nghệ công nghiệp", false));
                    result.setCnnn(parseDecimal(txtCnnn, "Công nghệ ngôn ngữ", false));
                    result.setTi(parseDecimal(txtTi, "Tin học", false));
                    result.setKtpl(parseDecimal(txtKtpl, "KTPL", false));
                    result.setNk1(parseDecimal(txtNk1, "NK1", false));
                    result.setNk2(parseDecimal(txtNk2, "NK2", false));
                    result.setNk3(parseDecimal(txtNk3, "NK3", false));
                    result.setNk4(parseDecimal(txtNk4, "NK4", false));
                    result.setNk5(parseDecimal(txtNk5, "NK5", false));
                    result.setNk6(parseDecimal(txtNk6, "NK6", false));
                }
            }
        } catch (IllegalArgumentException ex) {
            warn(ex.getMessage());
            return;
        }

        this.entity = result;
        this.saved = true;
        dispose();
    }

    private BigDecimal parseDecimal(JTextField field, String label, boolean required) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            if (required) {
                throw new IllegalArgumentException(label + " không được để trống.");
            }
            return null;
        }

        try {
            BigDecimal number = new BigDecimal(value);
            if (number.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Điểm " + label + " không được âm.");
            }

            BigDecimal max = isDgnl() ? new BigDecimal("1200") : new BigDecimal("10");
            if (number.compareTo(max) > 0) {
                throw new IllegalArgumentException("Điểm " + label + " không được vượt quá " + max + ".");
            }
            return number;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Điểm " + label + " không hợp lệ.");
        }
    }

    private void setText(JTextField field, BigDecimal value) {
        if (field != null) {
            field.setText(value != null ? value.stripTrailingZeros().toPlainString() : "");
        }
    }

    private String emptyToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String nvlStr(String value) {
        return value != null ? value : "";
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
    }

    private boolean isDgnl() {
        return DiemThiService.PHUONG_THUC_DGNL.equals(phuongThuc);
    }

    private boolean isVsat() {
        return DiemThiService.PHUONG_THUC_VSAT.equals(phuongThuc);
    }

    public boolean isSaved() {
        return saved;
    }

    public DiemThiXetTuyen getEntity() {
        return entity;
    }
}
