package com.xettuyen2026.ui.common;

import java.awt.*;

/**
 * Định nghĩa tất cả hằng số UI: màu sắc, font, kích thước.
 */
public final class UIConstants {

    private UIConstants() {}

    // ── Colors ────────────────────────────────────────────────
    public static final Color PRIMARY        = new Color(0x1565C0);
    public static final Color PRIMARY_DARK   = new Color(0x0D47A1);
    public static final Color PRIMARY_LIGHT  = new Color(0x42A5F5);
    public static final Color SIDEBAR_BG     = new Color(0x1A237E);
    public static final Color SIDEBAR_HOVER  = new Color(0x283593);
    public static final Color SIDEBAR_ACTIVE = new Color(0x1565C0);

    public static final Color BG_MAIN        = new Color(0xF0F2F5);
    public static final Color BG_CARD        = Color.WHITE;
    public static final Color BG_HEADER      = new Color(0xFAFAFA);

    public static final Color TEXT_PRIMARY   = new Color(0x212121);
    public static final Color TEXT_SECONDARY = new Color(0x757575);
    public static final Color TEXT_WHITE     = Color.WHITE;
    public static final Color TEXT_HINT      = new Color(0xBDBDBD);

    public static final Color ROW_EVEN       = new Color(0xF5F9FF);
    public static final Color ROW_ODD        = Color.WHITE;
    public static final Color TABLE_GRID     = new Color(0xE0E0E0);
    public static final Color TABLE_HEADER   = PRIMARY;

    public static final Color SUCCESS        = new Color(0x2E7D32);
    public static final Color WARNING        = new Color(0xF57F17);
    public static final Color DANGER         = new Color(0xC62828);
    public static final Color INFO           = new Color(0x1565C0);

    public static final Color STAT_BLUE      = new Color(0x1565C0);
    public static final Color STAT_PURPLE    = new Color(0x6A1B9A);
    public static final Color STAT_ORANGE    = new Color(0xE65100);
    public static final Color STAT_GREEN     = new Color(0x2E7D32);

    public static final Color BORDER_LIGHT   = new Color(0xE0E0E0);
    public static final Color SHADOW_COLOR   = new Color(0, 0, 0, 20);

    // ── Fonts ─────────────────────────────────────────────────
    public static final Font FONT_REGULAR    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD       = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TITLE      = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE   = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SMALL      = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_HEADER     = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_STAT_NUM   = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_MENU       = new Font("Segoe UI", Font.PLAIN, 14);

    // ── Sizes ─────────────────────────────────────────────────
    public static final int SIDEBAR_WIDTH    = 240;
    public static final int HEADER_HEIGHT    = 60;
    public static final int CARD_RADIUS      = 8;
    public static final int CARD_RADIUS_LG   = 16;
    public static final int BUTTON_RADIUS    = 8;
    public static final int FIELD_RADIUS     = 8;
    public static final int PAGE_SIZE        = 18;
    public static final Integer[] PAGE_SIZE_OPTIONS = {10, 18, 25, 50, 100};

    // ── Helper to load icon ───────────────────────────────────
    public static javax.swing.Icon getIcon(String fileName, int width, int height) {
        if (fileName == null || fileName.trim().isEmpty()) return null;
        try {
            java.io.File file = new java.io.File("src/main/data/icon/" + fileName);
            if (file.exists()) {
                javax.swing.ImageIcon icon = new javax.swing.ImageIcon(file.getAbsolutePath());
                java.awt.Image img = icon.getImage().getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
                return new javax.swing.ImageIcon(img);
            }
        } catch (Exception e) {}
        return null;
    }

    public static javax.swing.Icon getWhiteIcon(String fileName, int width, int height) {
        javax.swing.Icon baseIcon = getIcon(fileName, width, height);
        if (baseIcon == null) return null;
        
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2 = img.createGraphics();
        baseIcon.paintIcon(null, g2, 0, 0);
        g2.setComposite(java.awt.AlphaComposite.SrcAtop);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);
        g2.dispose();
        
        return new javax.swing.ImageIcon(img);
    }

    // ── Emoji Icons for Menu  ─────────────────────────────────
    // ── Icons for Menu  ─────────────────────────────────
    public static final String ICON_DASHBOARD  = "house.png";
    public static final String ICON_USER       = "users.png";
    public static final String ICON_STUDENT    = "graduation-cap.png";
    public static final String ICON_MAJOR      = "book-open-check.png";
    public static final String ICON_COMBO      = "layers.png";
    public static final String ICON_MAJOR_COMBO= "cable.png";
    public static final String ICON_SCORE      = "clipboard-paste.png";
    public static final String ICON_BONUS      = "badge-plus.png";
    public static final String ICON_WISH       = "file-heart.png";
    public static final String ICON_CONVERT    = "arrow-left-right.png";
    public static final String ICON_LOGOUT     = "exit.png"; // No PNG available
    public static final String ICON_SEARCH     = "search.png";
    public static final String ICON_IMPORT     = "cloud-upload.png";
    public static final String ICON_ADD        = "plus.png";
    public static final String ICON_EDIT       = "pencil-line.png";
    public static final String ICON_DELETE     = "eraser.png";
    public static final String ICON_DOWNLOAD   = "cloud-download.png";
    public static final String ICON_CALCULATE  = "calculator.png";
    public static final String ICON_EXECUTE    = "play.png";
    public static final String ICON_RESULT     = "certificate.png";
    public static final String ICON_STATISTIC  = "statistic.png";
}
