package com.xettuyen2026.util;

import java.util.regex.Pattern;

/**
 * Validator cho tổ hợp môn xét tuyển (TohopMonthi).
 * 
 * Chức năng:
 * - Validate mã tổ hợp (matohop): ^[A-Z]{1}[0-9]{2}$
 * - Validate môn học: phải là mã hợp lệ từ SubjectCode enum
 * - Kiểm tra trùng lặp môn
 * 
 * @author Senior Developer
 */
public class TohopValidator {

    // Regex: 1 chữ in hoa + 2 số (VD: A00, D01, X99)
    private static final Pattern MA_TOHOP_PATTERN = Pattern.compile("^[A-Z]{1}[0-9]{2}$");
    
    private static final String MSG_INVALID_MA_TOHOP = 
        "Mã tổ hợp phải có định dạng 1 chữ in hoa và 2 số. Ví dụ: A00, D01";
    
    private static final String MSG_INVALID_SUBJECT = 
        "Môn học không hợp lệ";
    
    private static final String MSG_EMPTY_SUBJECT = 
        "Các môn học không được để trống";
    
    private static final String MSG_DUPLICATE_SUBJECT = 
        "3 môn trong tổ hợp không được trùng nhau";

    /**
     * Validate mã tổ hợp.
     * @param maTohop Mã tổ hợp cần validate
     * @return true nếu hợp lệ, false nếu không
     */
    public static boolean isValidMaTohop(String maTohop) {
        if (maTohop == null || maTohop.trim().isEmpty()) {
            return false;
        }
        return MA_TOHOP_PATTERN.matcher(maTohop.trim()).matches();
    }

    /**
     * Lấy thông báo lỗi nếu mã tổ hợp không hợp lệ.
     * @param maTohop Mã tổ hợp
     * @return Thông báo lỗi hoặc null nếu hợp lệ
     */
    public static String getMaTohopError(String maTohop) {
        if (maTohop == null || maTohop.trim().isEmpty()) {
            return "Mã tổ hợp không được để trống";
        }
        if (!isValidMaTohop(maTohop)) {
            return MSG_INVALID_MA_TOHOP;
        }
        return null;
    }

    /**
     * Validate 3 môn học của tổ hợp.
     * @param mon1 Môn học 1
     * @param mon2 Môn học 2
     * @param mon3 Môn học 3
     * @return true nếu hợp lệ, false nếu không
     */
    public static boolean isValidSubjects(String mon1, String mon2, String mon3) {
        return isValidSingleSubject(mon1) && 
               isValidSingleSubject(mon2) && 
               isValidSingleSubject(mon3) &&
               !hasDuplicateSubjects(mon1, mon2, mon3);
    }

    /**
     * Validate một môn học.
     * @param subject Mã môn học
     * @return true nếu môn hợp lệ, false nếu không
     */
    public static boolean isValidSingleSubject(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            return false;
        }
        return SubjectCode.isValid(subject.toUpperCase());
    }

    /**
     * Kiểm tra 3 môn có trùng lặp không.
     * @param mon1 Môn 1
     * @param mon2 Môn 2
     * @param mon3 Môn 3
     * @return true nếu có trùng, false nếu không
     */
    public static boolean hasDuplicateSubjects(String mon1, String mon2, String mon3) {
        if (mon1 == null || mon2 == null || mon3 == null) {
            return false; // Let other validators handle null check
        }
        
        String m1 = mon1.trim().toUpperCase();
        String m2 = mon2.trim().toUpperCase();
        String m3 = mon3.trim().toUpperCase();
        
        return m1.equals(m2) || m2.equals(m3) || m1.equals(m3);
    }

    /**
     * Lấy thông báo lỗi nếu 3 môn không hợp lệ.
     * @param mon1 Môn 1
     * @param mon2 Môn 2
     * @param mon3 Môn 3
     * @return Thông báo lỗi hoặc null nếu hợp lệ
     */
    public static String getSubjectsError(String mon1, String mon2, String mon3) {
        // Check empty
        if (isEmptySubject(mon1) || isEmptySubject(mon2) || isEmptySubject(mon3)) {
            return MSG_EMPTY_SUBJECT;
        }
        
        // Check duplicate
        if (hasDuplicateSubjects(mon1, mon2, mon3)) {
            return MSG_DUPLICATE_SUBJECT;
        }
        
        // Check validity
        if (!isValidSingleSubject(mon1)) {
            return MSG_INVALID_SUBJECT + " (Môn 1)";
        }
        if (!isValidSingleSubject(mon2)) {
            return MSG_INVALID_SUBJECT + " (Môn 2)";
        }
        if (!isValidSingleSubject(mon3)) {
            return MSG_INVALID_SUBJECT + " (Môn 3)";
        }
        
        return null;
    }

    /**
     * Kiểm tra môn học có rỗng không.
     * @param subject Mã môn học
     * @return true nếu rỗng hoặc null
     */
    private static boolean isEmptySubject(String subject) {
        return subject == null || subject.trim().isEmpty();
    }

    /**
     * Normalize môn học: trim + toUpperCase.
     * @param subject Mã môn học
     * @return Mã môn học đã normalize
     */
    public static String normalizeSubject(String subject) {
        if (subject == null) {
            return "";
        }
        return subject.trim().toUpperCase();
    }

    /**
     * Normalize mã tổ hợp: trim + toUpperCase.
     * @param maTohop Mã tổ hợp
     * @return Mã tổ hợp đã normalize
     */
    public static String normalizeMaTohop(String maTohop) {
        if (maTohop == null) {
            return "";
        }
        return maTohop.trim().toUpperCase();
    }

    /**
     * Tạo tên tổ hợp từ 3 môn học.
     * Ví dụ: TO + VA + N1 => "Toán, Ngữ văn, Tiếng Anh"
     * @param mon1 Môn 1
     * @param mon2 Môn 2
     * @param mon3 Môn 3
     * @return Tên tổ hợp
     */
    public static String generateTohopName(String mon1, String mon2, String mon3) {
        String name1 = SubjectCode.getNameByCode(normalizeSubject(mon1));
        String name2 = SubjectCode.getNameByCode(normalizeSubject(mon2));
        String name3 = SubjectCode.getNameByCode(normalizeSubject(mon3));
        
        return String.format("%s, %s, %s", name1, name2, name3);
    }
}
