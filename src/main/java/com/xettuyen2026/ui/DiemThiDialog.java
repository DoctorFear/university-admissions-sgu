package com.xettuyen2026.ui;

import com.xettuyen2026.entity.DiemThiXetTuyen;
import com.xettuyen2026.ui.common.*;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class DiemThiDialog extends JDialog {

    private boolean saved = false;
    private DiemThiXetTuyen entity;
    private final String phuongThuc;

    // Truong chung
    private JTextField txtCccd, txtSobaodanh;

    // THPT
    private JTextField txtTo, txtLi, txtHo, txtSi, txtSu, txtDi, txtVa, txtGdcd;
    private JTextField txtN1Thi, txtN1Cc, txtCncn, txtCnnn, txtTi, txtKtpl;
    private JTextField txtNkMon1, txtNkDiem1, txtNkMon2, txtNkDiem2;

    // DGNL
    private JTextField txtNl1;

    public DiemThiDialog(Window owner, DiemThiXetTuyen existing, String phuongThuc) {
        super(owner,
              existing == null ? "Thêm điểm thi " + phuongThuc
                               : "Sửa điểm thi " + phuongThuc,
              ModalityType.APPLICATION_MODAL);
        this.entity     = existing;
        this.phuongThuc = phuongThuc;
        setSize(500, dialogHeight());
        setLocationRelativeTo(owner);
        setResizable(false);
        initUI();
        if (existing != null) populateData();
    }

    private int dialogHeight() {
        if ("DGNL".equals(phuongThuc)) return 260;
        if ("VSAT".equals(phuongThuc)) return 220;
        return 620; // THPT nhieu mon
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setBackground(Color.WHITE);
        main.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));

        JLabel title = new JLabel(entity == null
                ? "➕ Thêm điểm " + phuongThuc
                : "✏️ Sửa điểm " + phuongThuc);
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY);
        main.add(title, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(createForm());
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        main.add(scroll, BorderLayout.CENTER);
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

        // Truong chung
        txtCccd      = addField(form, gbc, row++, "CCCD *");
        if (entity != null) txtCccd.setEnabled(false);
        txtSobaodanh = addField(form, gbc, row++, "Số báo danh");

     // Trong createForm() - phan THPT
        if ("THPT".equals(phuongThuc)) {
            addSection(form, gbc, row++, "Môn bắt buộc");
            txtTo    = addField(form, gbc, row++, "Toán");
            txtVa    = addField(form, gbc, row++, "Văn");

            addSection(form, gbc, row++, "Khoa học tự nhiên");
            txtLi    = addField(form, gbc, row++, "Lý");
            txtHo    = addField(form, gbc, row++, "Hóa");
            txtSi    = addField(form, gbc, row++, "Sinh");

            addSection(form, gbc, row++, "Khoa học xã hội");
            txtSu    = addField(form, gbc, row++, "Sử");
            txtDi    = addField(form, gbc, row++, "Địa");
            txtGdcd  = addField(form, gbc, row++, "GDCD");

            addSection(form, gbc, row++, "Ngoại ngữ");
            txtN1Thi = addField(form, gbc, row++, "Ngoại ngữ (Thi)");
            txtN1Cc  = addField(form, gbc, row++, "Ngoại ngữ (Chứng chỉ)");

            addSection(form, gbc, row++, "Môn khác");
            txtCncn  = addField(form, gbc, row++, "CN Công nghệ");
            txtCnnn  = addField(form, gbc, row++, "CN Ngôn ngữ");
            txtTi    = addField(form, gbc, row++, "Tin học");
            txtKtpl  = addField(form, gbc, row++, "KTPL");

            addSection(form, gbc, row++, "Năng khiếu");
            txtNkMon1  = addField(form, gbc, row++, "Môn NK1");
            txtNkDiem1 = addField(form, gbc, row++, "Điểm NK1");
            txtNkMon2  = addField(form, gbc, row++, "Môn NK2");
            txtNkDiem2 = addField(form, gbc, row++, "Điểm NK2");
        } else if ("DGNL".equals(phuongThuc)) {
            txtNl1 = addField(form, gbc, row++, "Điểm ĐGNL");

        } else if ("VSAT".equals(phuongThuc)) {
            // TODO: chua co cau truc diem VSAT - hien thi thong bao tam thoi
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
            JLabel note = new JLabel("⚠️ Cấu trúc điểm V-SAT chưa được cập nhật.");
            note.setFont(UIConstants.FONT_REGULAR);
            note.setForeground(UIConstants.WARNING);
            form.add(note, gbc);
            gbc.gridwidth = 1;
        }

        return form;
    }

    private JTextField addField(JPanel form, GridBagConstraints gbc, int row, String label) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BOLD);
        lbl.setPreferredSize(new Dimension(160, 30));
        form.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        JTextField field = new JTextField();
        field.setFont(UIConstants.FONT_REGULAR);
        field.setPreferredSize(new Dimension(0, 32));
        form.add(field, gbc);
        return field;
    }

    private void addSection(JPanel form, GridBagConstraints gbc, int row, String text) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 1;
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

        if ("THPT".equals(phuongThuc)) {
            setText(txtTo,    entity.getTo());
            setText(txtVa,    entity.getVa());
            setText(txtLi,    entity.getLi());
            setText(txtHo,    entity.getHo());
            setText(txtSi,    entity.getSi());
            setText(txtSu,    entity.getSu());
            setText(txtDi,    entity.getDi());
            setText(txtN1Thi, entity.getN1Thi());
            setText(txtN1Cc,  entity.getN1Cc());
            setText(txtCncn,  entity.getCncn());
            setText(txtCnnn,  entity.getCnnn());
            setText(txtTi,    entity.getTi());
            setText(txtKtpl,  entity.getKtpl());
            setText(txtGdcd,    entity.getGdcd());
            setText(txtNkDiem1, entity.getNkDiem1());
            setText(txtNkDiem2, entity.getNkDiem2());
            if (txtNkMon1 != null) txtNkMon1.setText(nvlStr(entity.getNkMon1()));
            if (txtNkMon2 != null) txtNkMon2.setText(nvlStr(entity.getNkMon2()));
        } else if ("DGNL".equals(phuongThuc)) {
            setText(txtNl1, entity.getNl1());
        }
        // VSAT: TODO
    }

    private void doSave() {
        // VSAT chua ho tro
        if ("VSAT".equals(phuongThuc)) {
            warn("Chức năng nhập điểm V-SAT chưa được hỗ trợ!");
            return;
        }

        String cccd = txtCccd.getText().trim();
        if (cccd.isEmpty()) { warn("CCCD không được để trống!"); return; }

        DiemThiXetTuyen result = entity != null ? entity : new DiemThiXetTuyen();
        result.setCccd(cccd);
        result.setSobaodanh(txtSobaodanh.getText().trim());
        result.setdPhuongthuc(phuongThuc);

        try {
            if ("THPT".equals(phuongThuc)) {
                result.setTo(parseDecimal(txtTo,    "Toán",         false));
                result.setVa(parseDecimal(txtVa,    "Văn",          false));
                result.setLi(parseDecimal(txtLi,    "Lý",           false));
                result.setHo(parseDecimal(txtHo,    "Hóa",          false));
                result.setSi(parseDecimal(txtSi,    "Sinh",         false));
                result.setSu(parseDecimal(txtSu,    "Sử",           false));
                result.setDi(parseDecimal(txtDi,    "Địa",          false));
                result.setN1Thi(parseDecimal(txtN1Thi, "NN1 (Thi)", false));
                result.setN1Cc(parseDecimal(txtN1Cc,  "NN1 (CC)",   false));
                result.setCncn(parseDecimal(txtCncn,  "CN Công nghệ", false));
                result.setCnnn(parseDecimal(txtCnnn,  "CN Ngôn ngữ",  false));
                result.setTi(parseDecimal(txtTi,    "Tin học",      false));
                result.setKtpl(parseDecimal(txtKtpl, "KTPL",        false));
                result.setGdcd(parseDecimal(txtGdcd,   "GDCD",   false));
                result.setNkMon1(txtNkMon1.getText().trim());
                result.setNkDiem1(parseDecimal(txtNkDiem1, "Điểm NK1", false));
                result.setNkMon2(txtNkMon2.getText().trim());
                result.setNkDiem2(parseDecimal(txtNkDiem2, "Điểm NK2", false));
            } else if ("DGNL".equals(phuongThuc)) {
                result.setNl1(parseDecimal(txtNl1, "Điểm ĐGNL", false));
            }
        } catch (IllegalArgumentException e) {
            warn(e.getMessage());
            return;
        }

        this.entity = result;
        this.saved  = true;
        dispose();
    }

    // Parse diem: thang 10 cho THPT, thang 1200 cho DGNL
    private BigDecimal parseDecimal(JTextField field, String tenMon, boolean required) {
        String val = field.getText().trim();
        if (val.isEmpty()) {
            if (required) throw new IllegalArgumentException(tenMon + " không được để trống!");
            return null;
        }
        try {
            BigDecimal bd = new BigDecimal(val);
            if (bd.compareTo(BigDecimal.ZERO) < 0)
                throw new IllegalArgumentException("Điểm " + tenMon + " không được âm!");
            // DGNL thang 1200, con lai thang 10
            BigDecimal max = "DGNL".equals(phuongThuc)
                    ? new BigDecimal("1200") : new BigDecimal("10");
            if (bd.compareTo(max) > 0)
                throw new IllegalArgumentException(
                        "Điểm " + tenMon + " không được vượt quá " + max + "!");
            return bd;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Điểm " + tenMon + " không hợp lệ!");
        }
    }

    private void setText(JTextField f, BigDecimal v) {
        if (f != null) f.setText(v != null ? v.toPlainString() : "");
    }
    private String nvlStr(String s) { return s != null ? s : ""; }
    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
    }

    public boolean isSaved() { return saved; }
    public DiemThiXetTuyen getEntity() { return entity; }
}