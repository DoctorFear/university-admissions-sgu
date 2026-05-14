package com.xettuyen2026.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.xettuyen2026.entity.DiemThiXetTuyen;
import com.xettuyen2026.entity.ThiSinh;
import com.xettuyen2026.service.DiemThiService;
import com.xettuyen2026.ui.common.RoundedButton;
import com.xettuyen2026.ui.common.UIConstants;

public class ThiSinhDetailDialog extends JDialog {

    private final ThiSinh thiSinh;
    private final DiemThiService diemThiService;

    public ThiSinhDetailDialog(Window owner, ThiSinh thiSinh) {
        super(owner, "Chi tiết thí sinh", ModalityType.APPLICATION_MODAL);
        this.thiSinh = thiSinh;
        this.diemThiService = new DiemThiService();
        setSize(620, 500);
        setLocationRelativeTo(owner);
        setResizable(false);
        initUI();
    }

    // Khởi tạo giao diện xem chi tiết thí sinh và điểm thi
    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(0, 14));
        main.setBackground(Color.WHITE);
        main.setBorder(BorderFactory.createEmptyBorder(18, 20, 16, 20));

        main.add(createProfileHeader(), BorderLayout.NORTH);
        main.add(createContentPanel(), BorderLayout.CENTER);
        main.add(createButtonPanel(), BorderLayout.SOUTH);
        setContentPane(main);
    }

    // Tạo phần đầu dialog gồm tên thí sinh và khu vực ưu tiên
    private JPanel createProfileHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(buildHoTen());
        name.setFont(UIConstants.FONT_TITLE);
        name.setForeground(UIConstants.TEXT_PRIMARY);

        JLabel meta = new JLabel(joinMeta());
        meta.setFont(UIConstants.FONT_REGULAR);
        meta.setForeground(UIConstants.TEXT_SECONDARY);

        textPanel.add(name);
        textPanel.add(meta);
        header.add(textPanel, BorderLayout.CENTER);

        JLabel khuVuc = new JLabel(nvl(thiSinh.getKhuVuc()));
        khuVuc.setOpaque(true);
        khuVuc.setBackground(new Color(0xE3F2FD));
        khuVuc.setForeground(UIConstants.PRIMARY);
        khuVuc.setFont(UIConstants.FONT_BOLD);
        khuVuc.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        khuVuc.setPreferredSize(new Dimension(54, 32));
        header.add(khuVuc, BorderLayout.EAST);
        return header;
    }

    // Tạo phần nội dung gồm thông tin cơ bản và tab điểm
    private JPanel createContentPanel() {
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        content.add(createInfoPanel(), BorderLayout.NORTH);
        content.add(createScoreTabs(), BorderLayout.CENTER);
        return content;
    }

    // Tạo khối thông tin cá nhân có trong dữ liệu thí sinh
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 0, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT));

        panel.add(createInfoCell("CCCD", thiSinh.getCccd()));
        panel.add(createInfoCell("Số báo danh", thiSinh.getSobaodanh()));
        panel.add(createInfoCell("Dân tộc", thiSinh.getDanToc()));
        panel.add(createInfoCell("ĐTƯT", thiSinh.getDoiTuong()));
        panel.add(createInfoCell("KVƯT", thiSinh.getKhuVuc()));
        panel.add(createInfoCell("Nơi sinh", thiSinh.getNoiSinh()));
        return panel;
    }

    // Tạo một ô thông tin cá nhân
    private JPanel createInfoCell(String label, String value) {
        JPanel cell = new JPanel();
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setBackground(Color.WHITE);
        cell.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 1, UIConstants.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)
        ));

        JLabel labelText = new JLabel(label);
        labelText.setFont(UIConstants.FONT_SMALL);
        labelText.setForeground(UIConstants.TEXT_SECONDARY);

        JLabel valueText = new JLabel(nvl(value));
        valueText.setFont(UIConstants.FONT_BOLD);
        valueText.setForeground(UIConstants.TEXT_PRIMARY);

        cell.add(labelText);
        cell.add(valueText);
        return cell;
    }

    // Tạo các tab điểm thi theo từng phương thức
    private JTabbedPane createScoreTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIConstants.FONT_BOLD);
        tabs.addTab("THPT", createSubjectScorePanel(getScore(DiemThiService.PHUONG_THUC_THPT), false));
        tabs.addTab("V-SAT", createSubjectScorePanel(getScore(DiemThiService.PHUONG_THUC_VSAT), true));
        tabs.addTab("ĐGNL", createDgnlPanel(getScore(DiemThiService.PHUONG_THUC_DGNL)));
        return tabs;
    }

    // Lấy điểm thi của thí sinh theo phương thức qua tầng service điểm thi
    private DiemThiXetTuyen getScore(String phuongThuc) {
        return diemThiService.findByCccdAndPhuongThuc(thiSinh.getCccd(), phuongThuc);
    }

    // Tạo panel điểm theo môn và chỉ hiển thị môn có điểm
    private JPanel createSubjectScorePanel(DiemThiXetTuyen diem, boolean vsat) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        List<ScoreItem> scores = buildSubjectScores(diem, vsat);
        if (scores.isEmpty()) {
            panel.add(createEmptyPanel("Không có thông tin điểm."), BorderLayout.NORTH);
            return panel;
        }

        JPanel listPanel = new JPanel(new GridBagLayout());
        listPanel.setOpaque(false);
        listPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        for (int i = 0; i < scores.size(); i++) {
            ScoreItem item = scores.get(i);
            gbc.gridy = i / 4;
            gbc.gridx = i % 4;
            listPanel.add(createScoreCard(item), gbc);
        }

        panel.add(listPanel, BorderLayout.NORTH);
        return panel;
    }

    // Tạo panel điểm ĐGNL, nếu không có hoặc bằng 0 thì hiển thị thông báo không có điểm
    private JPanel createDgnlPanel(DiemThiXetTuyen diem) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        BigDecimal value = diem != null ? diem.getNl1() : null;
        if (!isPositive(value)) {
            panel.add(createEmptyPanel("Không có thông tin điểm."), BorderLayout.NORTH);
            return panel;
        }

        ScoreItem score = new ScoreItem("Điểm ĐGNL", fmtOrDash(value), value, UIConstants.STAT_BLUE);
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        wrapper.add(createScoreCard(score));
        panel.add(wrapper, BorderLayout.NORTH);
        return panel;
    }

    // Tạo danh sách môn có điểm theo đúng phương thức thi
    private List<ScoreItem> buildSubjectScores(DiemThiXetTuyen diem, boolean vsat) {
        List<ScoreItem> list = new ArrayList<>();
        if (diem == null) {
            return list;
        }

        addScore(list, "Toán", diem.getTo());
        addScore(list, "Ngữ văn", diem.getVa());
        addScore(list, "Vật lý", diem.getLi());
        addScore(list, "Hóa học", diem.getHo());
        addScore(list, "Sinh học", diem.getSi());
        addScore(list, "Lịch sử", diem.getSu());
        addScore(list, "Địa lý", diem.getDi());
        addScore(list, "Tiếng Anh", diem.getN1Thi());

        if (!vsat) {
            addScore(list, "GDCD", diem.getGdcd());
            addScore(list, "Ngoại ngữ CC", diem.getN1Cc());
            addScore(list, "CNCN", diem.getCncn());
            addScore(list, "CNNN", diem.getCnnn());
            addScore(list, "Tin học", diem.getTi());
            addScore(list, "KTPL", diem.getKtpl());
            addScore(list, "NK1", diem.getNk1());
            addScore(list, "NK2", diem.getNk2());
            addScore(list, "NK3", diem.getNk3());
            addScore(list, "NK4", diem.getNk4());
            addScore(list, "NK5", diem.getNk5());
            addScore(list, "NK6", diem.getNk6());
        }
        return list;
    }

    // Thêm môn vào danh sách nếu điểm lớn hơn 0
    private void addScore(List<ScoreItem> list, String label, BigDecimal value) {
        if (isPositive(value)) {
            list.add(new ScoreItem(label, fmtOrDash(value), value, pickScoreColor(list.size())));
        }
    }

    // Chọn màu nhấn cho từng ô điểm theo thứ tự hiển thị
    private Color pickScoreColor(int index) {
        Color[] colors = {
                UIConstants.DANGER,
                UIConstants.WARNING,
                UIConstants.SUCCESS,
                UIConstants.PRIMARY
        };
        return colors[index % colors.length];
    }

    // Tạo ô điểm theo môn
    private JPanel createScoreCard(ScoreItem item) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xF8FAFC));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(item.color);
                g2.fillRoundRect(0, 0, getWidth(), 4, 8, 8);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        card.setPreferredSize(new Dimension(128, 76));

        JLabel nameLabel = new JLabel(item.label);
        nameLabel.setFont(UIConstants.FONT_SMALL);
        nameLabel.setForeground(UIConstants.TEXT_PRIMARY);

        JLabel valueLabel = new JLabel(item.text);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(item.color);

        card.add(nameLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // Tạo panel rỗng khi không có dữ liệu điểm
    private JPanel createEmptyPanel(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_REGULAR);
        label.setForeground(UIConstants.TEXT_SECONDARY);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    // Tạo khu vực nút đóng hộp thoại
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setOpaque(false);

        RoundedButton btnClose = new RoundedButton("Đóng", UIConstants.PRIMARY);
        btnClose.addActionListener(e -> dispose());
        panel.add(btnClose);
        return panel;
    }

    // Ghép thông tin phụ của thí sinh dưới tên
    private String joinMeta() {
        List<String> parts = new ArrayList<>();
        addMeta(parts, thiSinh.getGioiTinh());
        addMeta(parts, "Sinh " + nvl(thiSinh.getNgaySinh()));
        addMeta(parts, thiSinh.getNoiSinh());
        return String.join(" · ", parts);
    }

    // Thêm thông tin phụ nếu có dữ liệu
    private void addMeta(List<String> parts, String value) {
        String cleaned = nvl(value);
        if (!"-".equals(cleaned)) {
            parts.add(cleaned);
        }
    }

    // Ghép họ và tên để hiển thị thông tin thí sinh
    private String buildHoTen() {
        String ho = thiSinh.getHo() == null ? "" : thiSinh.getHo().trim();
        String ten = thiSinh.getTen() == null ? "" : thiSinh.getTen().trim();
        String fullName = (ho + " " + ten).trim();
        return fullName.isEmpty() ? thiSinh.getCccd() : fullName;
    }

    // Định dạng điểm, trả về dấu gạch nếu không có hoặc bằng 0
    private String fmtOrDash(BigDecimal value) {
        return isPositive(value) ? value.stripTrailingZeros().toPlainString() : "-";
    }

    // Kiểm tra điểm có giá trị lớn hơn 0 hay không
    private boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    // Chuyển chuỗi null hoặc rỗng thành dấu gạch
    private String nvl(String value) {
        return value != null && !value.trim().isEmpty() && !"NaN".equalsIgnoreCase(value.trim()) ? value.trim() : "-";
    }

    // Lưu một cặp tên môn và điểm để hiển thị
    private static class ScoreItem {
        private final String label;
        private final String text;
        private final BigDecimal value;
        private final Color color;

        private ScoreItem(String label, String text, BigDecimal value, Color color) {
            this.label = label;
            this.text = text;
            this.value = value != null ? value : BigDecimal.ZERO;
            this.color = color;
        }
    }
}
