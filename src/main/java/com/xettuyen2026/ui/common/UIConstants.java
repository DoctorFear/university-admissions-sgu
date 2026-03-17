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

    // ── Emoji Icons for Menu  ─────────────────────────────────
    public static final String ICON_DASHBOARD  = "📊";
    public static final String ICON_USER       = "👤";
    public static final String ICON_STUDENT    = "🎓";
    public static final String ICON_MAJOR      = "📚";
    public static final String ICON_COMBO      = "🔢";
    public static final String ICON_MAJOR_COMBO= "📋";
    public static final String ICON_SCORE      = "📝";
    public static final String ICON_BONUS      = "➕";
    public static final String ICON_WISH       = "📌";
    public static final String ICON_CONVERT    = "🔄";
    public static final String ICON_LOGOUT     = "🚪";
    public static final String ICON_SEARCH     = "🔍";
    public static final String ICON_IMPORT     = "📥";
    public static final String ICON_ADD        = "➕";
    public static final String ICON_EDIT       = "✏️";
    public static final String ICON_DELETE     = "🗑️";
}
