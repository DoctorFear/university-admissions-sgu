package com.xettuyen2026.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Enum chứa tất cả các mã môn học hợp lệ.
 * Tự động tạo tên môn học (name) từ mã (code).
 * Sử dụng để validate và generate tên tổ hợp.
 * 
 * @author Senior Developer
 */
public enum SubjectCode {
    // Các môn chính
    TO("Toán"),
    LI("Vật lý"),
    HO("Hóa học"),
    SI("Sinh học"),
    VA("Ngữ văn"),
    SU("Lịch sử"),
    DI("Địa lí"),
    N1("Tiếng Anh"),
    TI("Tiếng Trung"),
    
    // Các môn khác
    KTPL("Kiến thức pháp luật"),
    CNCN("Công nghệ cao"),
    CNNN("Công nghệ nông nghiệp"),
    GD("Giáo dục công dân"),
    NK1("Nhạc khóa 1"),
    NK2("Nhạc khóa 2"),
    NK3("Nhạc khóa 3"),
    NK4("Nhạc khóa 4"),
    NK5("Nhạc khóa 5"),
    NK6("Nhạc khóa 6");

    private final String name;

    SubjectCode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return this.name();
    }

    /**
     * Kiểm tra mã môn học có hợp lệ không.
     * @param code Mã môn học
     * @return true nếu mã hợp lệ, false nếu không
     */
    public static boolean isValid(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        try {
            valueOf(code.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Lấy tên môn học từ mã.
     * @param code Mã môn học
     * @return Tên môn học hoặc code nếu không tìm thấy
     */
    public static String getNameByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return "";
        }
        try {
            return valueOf(code.toUpperCase()).getName();
        } catch (IllegalArgumentException e) {
            return code;
        }
    }

    /**
     * Lấy tất cả các mã môn học hợp lệ.
     * @return Set các mã môn học
     */
    public static Set<String> getAllCodes() {
        Map<String, String> codes = new HashMap<>();
        for (SubjectCode subject : SubjectCode.values()) {
            codes.put(subject.getCode(), subject.getName());
        }
        return codes.keySet();
    }

    /**
     * Lấy map của tất cả các môn (code -> name).
     * @return Map mã môn -> tên môn
     */
    public static Map<String, String> getSubjectMap() {
        Map<String, String> map = new HashMap<>();
        for (SubjectCode subject : SubjectCode.values()) {
            map.put(subject.getCode(), subject.getName());
        }
        return map;
    }
}
