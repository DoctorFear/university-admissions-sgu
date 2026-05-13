package com.xettuyen2026.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Validator cho ngành-tổ hợp xét tuyển (NganhTohop).
 * 
 * Chức năng:
 * - Validate mã ngành + mã tổ hợp
 * - Validate 3 môn học tương ứng với tổ hợp
 * - Validate hệ số môn học (1-5)
 * - Kiểm tra không được trùng (manganh + matohop)
 * 
 * @author Senior Developer
 */
public class NganhTohopValidator {

    private static final String MSG_EMPTY_NGANH = "Mã ngành không được để trống";
    private static final String MSG_EMPTY_TOHOP = "Mã tổ hợp không được để trống";
    private static final String MSG_INVALID_TOHOP = "Mã tổ hợp không hợp lệ";
    private static final String MSG_INVALID_SUBJECT = "Môn học không hợp lệ";
    private static final String MSG_EMPTY_SUBJECT = "Các môn học không được để trống";
    private static final String MSG_DUPLICATE_SUBJECT = "3 môn trong tổ hợp không được trùng nhau";
    private static final String MSG_INVALID_HS = "Hệ số môn phải từ 1 đến 5";

    /**
     * Validate toàn bộ dữ liệu ngành-tổ hợp.
     * @param maNganh Mã ngành
     * @param maTohop Mã tổ hợp
     * @param mon1 Môn 1
     * @param mon2 Môn 2
     * @param mon3 Môn 3
     * @param hs1 Hệ số 1
     * @param hs2 Hệ số 2
     * @param hs3 Hệ số 3
     * @return Thông báo lỗi hoặc null nếu hợp lệ
     */
    public static String validate(String maNganh, String maTohop, 
                                  String mon1, String mon2, String mon3,
                                  Byte hs1, Byte hs2, Byte hs3) {
        Integer hs1Int = hs1 != null ? hs1.intValue() : null;
        Integer hs2Int = hs2 != null ? hs2.intValue() : null;
        Integer hs3Int = hs3 != null ? hs3.intValue() : null;
        return validate(maNganh, maTohop, mon1, mon2, mon3, hs1Int, hs2Int, hs3Int);
    }

    /**
     * Validate toàn bộ dữ liệu ngành-tổ hợp (overload với Integer).
     * @param maNganh Mã ngành
     * @param maTohop Mã tổ hợp
     * @param mon1 Môn 1
     * @param mon2 Môn 2
     * @param mon3 Môn 3
     * @param hs1 Hệ số 1
     * @param hs2 Hệ số 2
     * @param hs3 Hệ số 3
     * @return Thông báo lỗi hoặc null nếu hợp lệ
     */
    public static String validate(String maNganh, String maTohop, 
                                  String mon1, String mon2, String mon3,
                                  Integer hs1, Integer hs2, Integer hs3) {
        // Validate mã ngành
        String nganhErr = validateMaNganh(maNganh);
        if (nganhErr != null) {
            return nganhErr;
        }
        
        // Validate mã tổ hợp
        String tohopErr = validateMaTohop(maTohop);
        if (tohopErr != null) {
            return tohopErr;
        }
        
        // Validate 3 môn
        String subjectErr = validateSubjects(mon1, mon2, mon3);
        if (subjectErr != null) {
            return subjectErr;
        }
        
        // Validate hệ số
        String hsErr = validateHeSo(hs1, hs2, hs3);
        if (hsErr != null) {
            return hsErr;
        }
        
        return null;
    }

    /**
     * Validate mã ngành.
     * @param maNganh Mã ngành
     * @return Thông báo lỗi hoặc null nếu hợp lệ
     */
    public static String validateMaNganh(String maNganh) {
        if (maNganh == null || maNganh.trim().isEmpty()) {
            return MSG_EMPTY_NGANH;
        }
        return null;
    }

    /**
     * Validate mã tổ hợp.
     * @param maTohop Mã tổ hợp
     * @return Thông báo lỗi hoặc null nếu hợp lệ
     */
    public static String validateMaTohop(String maTohop) {
        if (maTohop == null || maTohop.trim().isEmpty()) {
            return MSG_EMPTY_TOHOP;
        }
        if (!TohopValidator.isValidMaTohop(maTohop)) {
            return MSG_INVALID_TOHOP;
        }
        return null;
    }

    /**
     * Validate 3 môn học.
     * @param mon1 Môn 1
     * @param mon2 Môn 2
     * @param mon3 Môn 3
     * @return Thông báo lỗi hoặc null nếu hợp lệ
     */
    public static String validateSubjects(String mon1, String mon2, String mon3) {
        // Check empty
        if (isEmptySubject(mon1) || isEmptySubject(mon2) || isEmptySubject(mon3)) {
            return MSG_EMPTY_SUBJECT;
        }
        
        // Check duplicate
        if (hasDuplicateSubjects(mon1, mon2, mon3)) {
            return MSG_DUPLICATE_SUBJECT;
        }
        
        // Check validity
        if (!TohopValidator.isValidSingleSubject(mon1)) {
            return MSG_INVALID_SUBJECT + " (Môn 1)";
        }
        if (!TohopValidator.isValidSingleSubject(mon2)) {
            return MSG_INVALID_SUBJECT + " (Môn 2)";
        }
        if (!TohopValidator.isValidSingleSubject(mon3)) {
            return MSG_INVALID_SUBJECT + " (Môn 3)";
        }
        
        return null;
    }

    /**
     * Validate hệ số môn (phải từ 1-5).
     * @param hs1 Hệ số 1
     * @param hs2 Hệ số 2
     * @param hs3 Hệ số 3
     * @return Thông báo lỗi hoặc null nếu hợp lệ
     */
    public static String validateHeSo(Integer hs1, Integer hs2, Integer hs3) {
        if (!isValidHeSo(hs1) || !isValidHeSo(hs2) || !isValidHeSo(hs3)) {
            return MSG_INVALID_HS;
        }
        return null;
    }

    /**
     * Kiểm tra hệ số có hợp lệ không (1-5).
     * @param hs Hệ số
     * @return true nếu hợp lệ
     */
    private static boolean isValidHeSo(Integer hs) {
        return hs != null && hs >= 1 && hs <= 5;
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
     * Kiểm tra 3 môn có trùng lặp không.
     * @param mon1 Môn 1
     * @param mon2 Môn 2
     * @param mon3 Môn 3
     * @return true nếu có trùng
     */
    public static boolean hasDuplicateSubjects(String mon1, String mon2, String mon3) {
        if (mon1 == null || mon2 == null || mon3 == null) {
            return false;
        }
        
        String m1 = mon1.trim().toUpperCase();
        String m2 = mon2.trim().toUpperCase();
        String m3 = mon3.trim().toUpperCase();
        
        return m1.equals(m2) || m2.equals(m3) || m1.equals(m3);
    }

    /**
     * Tự động generate cờ môn học dựa trên 3 môn.
     * Ví dụ: mon1=TO, mon2=VA, mon3=N1 => TO=1, VA=1, N1=1, còn lại=0
     * @param mon1 Môn 1
     * @param mon2 Môn 2
     * @param mon3 Môn 3
     * @return Array boolean [N1, TO, LI, HO, SI, VA, SU, DI, TI, KHAC, KTPL]
     * 
     * Order: N1, TO, LI, HO, SI, VA, SU, DI, TI, KHAC, KTPL
     */
    public static boolean[] generateSubjectFlags(String mon1, String mon2, String mon3) {
        Set<String> subjects = new HashSet<>();
        if (mon1 != null) subjects.add(mon1.trim().toUpperCase());
        if (mon2 != null) subjects.add(mon2.trim().toUpperCase());
        if (mon3 != null) subjects.add(mon3.trim().toUpperCase());
        
        boolean[] flags = new boolean[11];
        // Index: [0]=N1, [1]=TO, [2]=LI, [3]=HO, [4]=SI, [5]=VA, [6]=SU, [7]=DI, [8]=TI, [9]=KHAC, [10]=KTPL
        
        flags[0] = subjects.contains("N1");      // N1
        flags[1] = subjects.contains("TO");      // TO
        flags[2] = subjects.contains("LI");      // LI
        flags[3] = subjects.contains("HO");      // HO
        flags[4] = subjects.contains("SI");      // SI
        flags[5] = subjects.contains("VA");      // VA
        flags[6] = subjects.contains("SU");      // SU
        flags[7] = subjects.contains("DI");      // DI
        flags[8] = subjects.contains("TI");      // TI
        flags[9] = subjects.contains("KHAC");    // KHAC
        flags[10] = subjects.contains("KTPL");   // KTPL
        
        return flags;
    }

    /**
     * Validate mã tổ hợp tồn tại và hợp lệ (để dùng ở panel edit).
     * @param maTohop Mã tổ hợp
     * @return Thông báo lỗi hoặc null nếu hợp lệ
     */
    public static String validateTohopExists(String maTohop) {
        if (maTohop == null || maTohop.trim().isEmpty()) {
            return MSG_EMPTY_TOHOP;
        }
        if (!TohopValidator.isValidMaTohop(maTohop)) {
            return MSG_INVALID_TOHOP;
        }
        return null;
    }
}
