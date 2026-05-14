package com.xettuyen2026.ui;

import com.xettuyen2026.entity.NganhTohop;
import com.xettuyen2026.entity.TohopMonthi;
import com.xettuyen2026.dao.TohopMonthiDAO;
import com.xettuyen2026.ui.common.RoundedButton;
import com.xettuyen2026.ui.common.UIConstants;
import com.xettuyen2026.util.NganhTohopValidator;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

/**
 * Dialog thêm/sửa ngành-tổ hợp.
 * - Autocomplete mã tổ hợp khi gõ
 * - Môn + Hệ số trên cùng dòng
 * - Dialog vừa vặn, không cần scroll
 */
public class NganhTohopDialog extends JDialog {

    private boolean saved = false;
    private boolean saving = false;
    private NganhTohop originalEntity; // Entity gốc từ DB (dùng để phân biệt add vs edit)
    private NganhTohop resultEntity;
    private Map<String, String> nganhMap;
    private Map<String, TohopMonthi> tohopCache = new LinkedHashMap<>();
    private List<String> tohopSuggestions = new ArrayList<>();

    private JComboBox<String> cboMaNganh;
    private JTextField txtMaTohop;
    private JTextField txtMon1, txtMon2, txtMon3;
    private JSpinner spnHs1, spnHs2, spnHs3;
    private JTextField txtDoLech;
    private RoundedButton btnSave;
    private JPanel suggestionPanel;
    private JList<String> suggestionList;
    private boolean suggestionVisible = false;
    private final int ROW_H = 34;

    public NganhTohopDialog(Window owner, NganhTohop existing, Map<String, String> nganhMap) {
        super(owner, existing == null ? "Thêm mới ngành-tổ hợp" : "Sửa ngành-tổ hợp",
                ModalityType.APPLICATION_MODAL);
        this.originalEntity = existing;
        this.nganhMap = nganhMap;
        // Khởi tạo resultEntity sớm để syncFromTohop có thể set các trường boolean
        this.resultEntity = new NganhTohop();
        setSize(560, 385);
        setLocationRelativeTo(owner);
        setResizable(false);
        initUI();
        loadTohopCache();
        if (existing != null) populateData();
    }

    private void loadTohopCache() {
        TohopMonthiDAO tohopDAO = new TohopMonthiDAO();
        List<TohopMonthi> all = tohopDAO.findAll();
        for (TohopMonthi th : all) {
            tohopCache.put(th.getMatohop(), th);
        }
        tohopSuggestions = new ArrayList<>(tohopCache.keySet());
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setBackground(Color.WHITE);
        main.setBorder(BorderFactory.createEmptyBorder(18, 22, 14, 22));

        // Title
        JLabel title = new JLabel(originalEntity == null ? "Thêm mới ngành - tổ hợp" : "Sửa ngành - tổ hợp");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY);
        main.add(title, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Dòng 1: Mã ngành
        addLabel(form, gbc, row, "Mã ngành *", 110);
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 2;
        cboMaNganh = new JComboBox<>();
        cboMaNganh.setFont(UIConstants.FONT_REGULAR);
        cboMaNganh.setPreferredSize(new Dimension(0, ROW_H));
        for (Map.Entry<String, String> e : nganhMap.entrySet()) {
            cboMaNganh.addItem(e.getKey() + " - " + e.getValue());
        }
        form.add(cboMaNganh, gbc);
        gbc.gridwidth = 1; row++;

        // Dòng 2: Mã tổ hợp + Panel gợi ý bên dưới
        addLabel(form, gbc, row, "Mã tổ hợp *", 110);
        gbc.gridx = 1; gbc.weightx = 1;
        txtMaTohop = new JTextField();
        txtMaTohop.setFont(UIConstants.FONT_REGULAR);
        txtMaTohop.setPreferredSize(new Dimension(0, ROW_H));
        txtMaTohop.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                SwingUtilities.invokeLater(() -> {
                    updateSuggestions(txtMaTohop.getText());
                    showSuggestions(!txtMaTohop.getText().isEmpty());
                });
            }
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) hideSuggestions();
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN && suggestionVisible) {
                    suggestionList.requestFocus();
                    suggestionList.setSelectedIndex(0);
                }
            }
        });
        txtMaTohop.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                javax.swing.Timer timer = new javax.swing.Timer(200, ev -> hideSuggestions());
                timer.setRepeats(false);
                timer.start();
            }
        });
        form.add(txtMaTohop, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        form.add(Box.createHorizontalStrut(1), gbc); // placeholder
        row++;

        // Dòng 3-5: Môn + Hệ số trên cùng dòng
        addMonRow(form, gbc, row++, "Môn 1 *", "HS1 *", txtMon1 = makeReadOnlyField(), spnHs1 = makeSpinner());
        addMonRow(form, gbc, row++, "Môn 2 *", "HS2 *", txtMon2 = makeReadOnlyField(), spnHs2 = makeSpinner());
        addMonRow(form, gbc, row++, "Môn 3 *", "HS3 *", txtMon3 = makeReadOnlyField(), spnHs3 = makeSpinner());

        // Dòng 6: Độ lệch
        addLabel(form, gbc, row, "Độ lệch", 110);
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 2;
        txtDoLech = new JTextField("0.00");
        txtDoLech.setFont(UIConstants.FONT_REGULAR);
        txtDoLech.setPreferredSize(new Dimension(0, ROW_H));
        form.add(txtDoLech, gbc);
        gbc.gridwidth = 1; row++;

        main.add(form, BorderLayout.NORTH);

        // Panel gợi ý tổ hợp — nằm ngay dưới txtMaTohop, trong main
        suggestionPanel = new JPanel(new BorderLayout());
        suggestionPanel.setOpaque(false);
        suggestionPanel.setVisible(false);

        suggestionList = new JList<>();
        suggestionList.setFont(UIConstants.FONT_REGULAR);
        suggestionList.setSelectionBackground(new Color(0xBBDEFB));
        suggestionList.setBorder(BorderFactory.createLineBorder(new Color(0xCCCCCC)));
        suggestionList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                String selected = suggestionList.getSelectedValue();
                if (selected != null) {
                    txtMaTohop.setText(selected);
                    syncFromTohop(selected);
                    hideSuggestions();
                }
            }
        });
        suggestionList.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    String selected = suggestionList.getSelectedValue();
                    if (selected != null) {
                        txtMaTohop.setText(selected);
                        syncFromTohop(selected);
                        hideSuggestions();
                        txtMaTohop.requestFocus();
                    }
                }
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    hideSuggestions();
                    txtMaTohop.requestFocus();
                }
            }
        });

        JScrollPane suggScroll = new JScrollPane(suggestionList);
        suggScroll.setBorder(BorderFactory.createLineBorder(new Color(0xBBDEFB), 1));
        suggScroll.setPreferredSize(new Dimension(0, 120));
        suggestionPanel.add(suggScroll, BorderLayout.NORTH);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        RoundedButton btnCancel = new RoundedButton("Hủy", new Color(0x757575));
        btnCancel.addActionListener(e -> dispose());

        btnSave = new RoundedButton("Lưu", UIConstants.PRIMARY);
        btnSave.addActionListener(e -> doSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        // Layout: title + form + suggestion + buttons
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);

        main.add(suggestionPanel, BorderLayout.CENTER);

        content.add(main);
        content.add(Box.createVerticalStrut(4));
        content.add(btnPanel);

        setContentPane(content);
    }

    private void addLabel(JPanel form, GridBagConstraints gbc, int row, String text, int width) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_BOLD);
        lbl.setPreferredSize(new Dimension(width, ROW_H));
        form.add(lbl, gbc);
    }

    private void addMonRow(JPanel form, GridBagConstraints gbc, int row,
                          String labelMon, String labelHs,
                          JTextField txtMon, JSpinner spn) {
        addLabel(form, gbc, row, labelMon, 110);

        // TextField môn
        gbc.gridx = 1; gbc.weightx = 0.6f;
        txtMon.setFont(UIConstants.FONT_REGULAR);
        txtMon.setPreferredSize(new Dimension(0, ROW_H));
        form.add(txtMon, gbc);

        // Panel: label HS + Spinner
        JPanel hsPanel = new JPanel(new BorderLayout(4, 0));
        hsPanel.setOpaque(false);
        JLabel lblHs = new JLabel(labelHs);
        lblHs.setFont(UIConstants.FONT_BOLD);
        lblHs.setHorizontalAlignment(SwingConstants.RIGHT);
        lblHs.setPreferredSize(new Dimension(42, ROW_H));
        spn.setFont(UIConstants.FONT_REGULAR);
        spn.setPreferredSize(new Dimension(0, ROW_H));
        hsPanel.add(lblHs, BorderLayout.WEST);
        hsPanel.add(spn, BorderLayout.CENTER);

        gbc.gridx = 2; gbc.weightx = 0.4f;
        form.add(hsPanel, gbc);
    }

    private JTextField makeReadOnlyField() {
        JTextField f = new JTextField();
        f.setEditable(false);
        f.setBackground(new Color(0xF0F0F0));
        f.setFont(UIConstants.FONT_REGULAR);
        return f;
    }

    private JSpinner makeSpinner() {
        return new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
    }

    private void updateSuggestions(String text) {
        List<String> filtered = tohopSuggestions.stream()
                .filter(s -> s.toLowerCase().contains(text.toLowerCase()))
                .toList();
        suggestionList.setListData(filtered.toArray(new String[0]));
    }

    private void showSuggestions(boolean show) {
        suggestionVisible = show;
        suggestionPanel.setVisible(show);
        if (show) {
            suggestionPanel.setBounds(
                txtMaTohop.getLocationOnScreen().x - getLocation().x,
                txtMaTohop.getLocationOnScreen().y - getLocation().y + ROW_H + 2,
                txtMaTohop.getWidth(),
                120
            );
        }
        revalidate();
        repaint();
    }

    private void hideSuggestions() {
        suggestionVisible = false;
        suggestionPanel.setVisible(false);
    }

    private void syncFromTohop(String maTohop) {
        TohopMonthi tohop = tohopCache.get(maTohop);
        if (tohop == null) return;

        // Điền 3 môn
        txtMon1.setText(tohop.getMon1());
        txtMon2.setText(tohop.getMon2());
        txtMon3.setText(tohop.getMon3());

        // Điền hệ số mặc định = 1 (TohopMonthi không lưu hệ số riêng)
        spnHs1.setValue(1);
        spnHs2.setValue(1);
        spnHs3.setValue(1);

        // Tự động điền tb_keys và các cột boolean dựa trên 3 môn trong tổ hợp
        if (resultEntity != null) {
            String m1 = tohop.getMon1();
            String m2 = tohop.getMon2();
            String m3 = tohop.getMon3();
            resultEntity.setTbKeys(m1 + m2 + m3);
            resultEntity.setN1(m1.equals("N1") || m2.equals("N1") || m3.equals("N1"));
            resultEntity.setTo(m1.equals("TO") || m2.equals("TO") || m3.equals("TO"));
            resultEntity.setLi(m1.equals("LI") || m2.equals("LI") || m3.equals("LI"));
            resultEntity.setHo(m1.equals("HO") || m2.equals("HO") || m3.equals("HO"));
            resultEntity.setSi(m1.equals("SI") || m2.equals("SI") || m3.equals("SI"));
            resultEntity.setVa(m1.equals("VA") || m2.equals("VA") || m3.equals("VA"));
            resultEntity.setSu(m1.equals("SU") || m2.equals("SU") || m3.equals("SU"));
            resultEntity.setDi(m1.equals("DI") || m2.equals("DI") || m3.equals("DI"));
            resultEntity.setTi(m1.equals("TI") || m2.equals("TI") || m3.equals("TI"));
            resultEntity.setKhac(!(resultEntity.getN1() || resultEntity.getTo() || resultEntity.getLi()
                    || resultEntity.getHo() || resultEntity.getSi() || resultEntity.getVa()
                    || resultEntity.getSu() || resultEntity.getDi() || resultEntity.getTi()));
            resultEntity.setKtpl(m1.equals("KTPL") || m2.equals("KTPL") || m3.equals("KTPL"));
        }
    }

    private void populateData() {
        if (originalEntity == null) return;

        // Select ngành
        for (int i = 0; i < cboMaNganh.getItemCount(); i++) {
            String item = cboMaNganh.getItemAt(i);
            if (item.startsWith(originalEntity.getManganh() + " ")) {
                cboMaNganh.setSelectedIndex(i);
                break;
            }
        }
        cboMaNganh.setEnabled(false);

        // Set tổ hợp (text field, không sửa khi edit)
        txtMaTohop.setText(originalEntity.getMatohop());
        txtMaTohop.setEditable(false);
        txtMaTohop.setBackground(new Color(0xF0F0F0));

        // Set môn
        txtMon1.setText(originalEntity.getThMon1());
        txtMon2.setText(originalEntity.getThMon2());
        txtMon3.setText(originalEntity.getThMon3());

        // Set hệ số
        if (originalEntity.getHsmon1() != null) spnHs1.setValue(originalEntity.getHsmon1().intValue());
        if (originalEntity.getHsmon2() != null) spnHs2.setValue(originalEntity.getHsmon2().intValue());
        if (originalEntity.getHsmon3() != null) spnHs3.setValue(originalEntity.getHsmon3().intValue());

        if (originalEntity.getDolech() != null) txtDoLech.setText(originalEntity.getDolech().toPlainString());

        // Điền các trường boolean vào resultEntity để giữ nguyên khi save
        resultEntity.setTbKeys(originalEntity.getTbKeys());
        resultEntity.setN1(originalEntity.getN1());
        resultEntity.setTo(originalEntity.getTo());
        resultEntity.setLi(originalEntity.getLi());
        resultEntity.setHo(originalEntity.getHo());
        resultEntity.setSi(originalEntity.getSi());
        resultEntity.setVa(originalEntity.getVa());
        resultEntity.setSu(originalEntity.getSu());
        resultEntity.setDi(originalEntity.getDi());
        resultEntity.setTi(originalEntity.getTi());
        resultEntity.setKhac(originalEntity.getKhac());
        resultEntity.setKtpl(originalEntity.getKtpl());
    }

    private void doSave() {
        if (saving) return;
        saving = true;
        btnSave.setEnabled(false);

        try {
            String maNganh = getNganhFromCombo();
            String maTohop = txtMaTohop.getText().trim().toUpperCase();
            String mon1 = txtMon1.getText().trim().toUpperCase();
            String mon2 = txtMon2.getText().trim().toUpperCase();
            String mon3 = txtMon3.getText().trim().toUpperCase();

            // Validate
            String err = NganhTohopValidator.validate(
                    maNganh, maTohop, mon1, mon2, mon3,
                    (Integer) spnHs1.getValue(),
                    (Integer) spnHs2.getValue(),
                    (Integer) spnHs3.getValue());
            if (err != null) {
                JOptionPane.showMessageDialog(this, err, "Lỗi dữ liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Thêm mới: kiểm tra mã tổ hợp có tồn tại trong cache không
            if (originalEntity == null && !tohopCache.containsKey(maTohop)) {
                JOptionPane.showMessageDialog(this,
                        "Mã tổ hợp \"" + maTohop + "\" chưa có trong danh sách.\nVui lòng vào mục \"Tổ hợp môn\" để thêm trước!",
                        "Tổ hợp không tồn tại", JOptionPane.WARNING_MESSAGE);
                return;
            }

            BigDecimal doLech;
            try {
                doLech = new BigDecimal(txtDoLech.getText().trim());
            } catch (NumberFormatException e) {
                doLech = BigDecimal.ZERO;
            }

            resultEntity.setManganh(maNganh);
            resultEntity.setMatohop(maTohop);
            resultEntity.setThMon1(mon1);
            resultEntity.setHsmon1((byte) ((int) spnHs1.getValue()));
            resultEntity.setThMon2(mon2);
            resultEntity.setHsmon2((byte) ((int) spnHs2.getValue()));
            resultEntity.setThMon3(mon3);
            resultEntity.setHsmon3((byte) ((int) spnHs3.getValue()));
            resultEntity.setDolech(doLech);

            saved = true;
            dispose();
        } finally {
            saving = false;
            btnSave.setEnabled(true);
        }
    }

    private String getNganhFromCombo() {
        Object selected = cboMaNganh.getSelectedItem();
        if (selected == null) return "";
        return selected.toString().split(" - ")[0].trim();
    }

    public boolean isSaved() { return saved; }
    public NganhTohop getEntity() { return resultEntity; }
}
