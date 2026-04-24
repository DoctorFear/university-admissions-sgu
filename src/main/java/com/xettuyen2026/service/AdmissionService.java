package com.xettuyen2026.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import com.xettuyen2026.dao.BangQuydoiDAO;
import com.xettuyen2026.dao.DiemCongDAO;
import com.xettuyen2026.dao.DiemThiDAO;
import com.xettuyen2026.dao.NganhDAO;
import com.xettuyen2026.dao.NganhTohopDAO;
import com.xettuyen2026.dao.NguyenVongDAO;
import com.xettuyen2026.dao.ThiSinhDAO;
import com.xettuyen2026.entity.BangQuydoi;
import com.xettuyen2026.entity.DiemCongXetTuyen;
import com.xettuyen2026.entity.DiemThiXetTuyen;
import com.xettuyen2026.entity.Nganh;
import com.xettuyen2026.entity.NganhTohop;
import com.xettuyen2026.entity.NguyenVongXetTuyen;
import com.xettuyen2026.entity.ThiSinh;

/**
 * Service tính điểm + chạy thuật toán xét tuyển.
 *
 * Các bước:
 * 1. calculateAllScores()  — Tính ĐTHGXT, ĐC, ĐƯT, ĐXT cho mỗi nguyện vọng
 * 2. executeAdmissionProcess() — Chạy Gale-Shapley deferred acceptance
 *
 * Phương thức được hỗ trợ (qua d_phuongthuc trong xt_diemthixettuyen):
 *   "1"  = THPT (điểm thi THPT quốc gia)
 *   "4"  = ĐGNL (Đánh giá năng lực ĐHQG-HCM, thang 1200; dùng NL1)
 *   "5"  = VSAT (V-SAT, điểm từng môn đã lưu ở TO/LI/HO/...; quy đổi qua bangquydoi)
 *
 * Trong nguyện vọng, tt_phuongthuc dùng giá trị "PT2"/"DGNL"/"VSAT" v.v.
 * Mapping: PT2 → d_phuongthuc "1"; DGNL → "4"; VSAT → "5".
 */
public class AdmissionService {

    private NguyenVongDAO nguyenVongDAO = new NguyenVongDAO();
    private NganhDAO nganhDAO = new NganhDAO();
    private NganhTohopDAO nganhTohopDAO = new NganhTohopDAO();
    private DiemThiDAO diemThiDAO = new DiemThiDAO();
    private ThiSinhDAO thiSinhDAO = new ThiSinhDAO();
    private DiemCongDAO diemCongDAO = new DiemCongDAO();
    private BangQuydoiDAO bangQuydoiDAO = new BangQuydoiDAO();

    // ════════════════════════════════════════════════════════════
    //  BƯỚC 1: TÍNH ĐIỂM XÉT TUYỂN CHO MỖI NGUYỆN VỌNG
    // ════════════════════════════════════════════════════════════

    /**
     * Tính toàn bộ điểm cho tất cả nguyện vọng rồi lưu DB.
     */
    public void calculateAllScores() {
        List<NguyenVongXetTuyen> allNv = nguyenVongDAO.findAll();
        for (NguyenVongXetTuyen nv : allNv) {
            calculateScoreForAspiration(nv);
        }
        nguyenVongDAO.batchUpdate(allNv);
    }

    private static class TohopResult {
        BigDecimal rawScore;
        BigDecimal adjustedScore;
        NganhTohop bestTohop;
    }

    /**
     * Tính điểm cho 1 nguyện vọng:
     *   ĐTHXT & ĐTHGXT → ĐC → ĐƯT → ĐXT (cap tại 30)
     */
    public void calculateScoreForAspiration(NguyenVongXetTuyen nv) {
        ThiSinh thiSinh = thiSinhDAO.findByCccd(nv.getNnCccd());
        if (thiSinh == null) return;

        // Chuyển tt_phuongthuc (e.g. "PT2","DGNL","VSAT") sang mã d_phuongthuc trong DB
        String phuongthucCode = mapPhuongThucToCode(nv.getTtPhuongthuc());

        DiemThiXetTuyen diemThi = diemThiDAO.findByCccdAndPhuongThuc(nv.getNnCccd(), phuongthucCode);
        if (diemThi == null) {
            // Fallback: lấy bản ghi điểm đầu tiên của thí sinh nếu không match chính xác
            diemThi = diemThiDAO.findByCccd(nv.getNnCccd());
        }
        if (diemThi == null) return;

        String maNganh = nv.getNvManganh();
        String phuongthuc = nv.getTtPhuongthuc();

        // ── Bước 1: Tính ĐTHGXT (để xét ưu tiên) và ĐTHXT (để làm tổ hợp gốc) ──
        TohopResult tohopRes = calculateDTHGXT(diemThi, maNganh, phuongthuc);
        BigDecimal dthxt = tohopRes.rawScore != null ? tohopRes.rawScore : BigDecimal.ZERO;
        BigDecimal dthgxt = tohopRes.adjustedScore != null ? tohopRes.adjustedScore : BigDecimal.ZERO;
        
        nv.setDiemThxt(dthxt); // Lưu bảng thực tế không có độ lệch vào DB

        // ── Bước 2: Lấy Điểm cộng (ĐC), tối đa 3.0, phân biệt có Ngoại ngữ hay không ──
        boolean containsN1 = tohopRes.bestTohop != null && (
               "N1".equalsIgnoreCase(tohopRes.bestTohop.getThMon1()) ||
               "N1".equalsIgnoreCase(tohopRes.bestTohop.getThMon2()) ||
               "N1".equalsIgnoreCase(tohopRes.bestTohop.getThMon3())
        );
        BigDecimal dCong = getDiemCong(nv.getNnCccd(), maNganh, nv.getTtThm(), containsN1);
        nv.setDiemCong(dCong);

        // ── Bước 3: Tính Mức điểm ưu tiên (MĐƯT) + Điểm ưu tiên (ĐƯT) ──
        BigDecimal mdut = calculateMdut(thiSinh.getKhuVuc(), thiSinh.getDoiTuong());
        BigDecimal sumScore = dthgxt.add(dCong);

        BigDecimal dUutien;
        if (sumScore.compareTo(new BigDecimal("22.5")) < 0) {
            // Dưới 22.5 → ĐƯT = MĐƯT (nguyên)
            dUutien = mdut;
        } else {
            // Trên 22.5 → ĐƯT = [(30 - ĐTHGXT - ĐC) / 7.5] × MĐƯT
            BigDecimal diff = new BigDecimal("30").subtract(dthgxt).subtract(dCong);
            if (diff.compareTo(BigDecimal.ZERO) < 0) diff = BigDecimal.ZERO;
            BigDecimal factor = diff.divide(new BigDecimal("7.5"), 5, RoundingMode.HALF_UP);
            dUutien = factor.multiply(mdut).setScale(5, RoundingMode.HALF_UP);
        }
        nv.setDiemUtqd(dUutien);

        // ── Bước 4: ĐXT = ĐTHXT + ĐC + ĐƯT, CAP tại 30 ──
        BigDecimal dxt = dthxt.add(dCong).add(dUutien).setScale(5, RoundingMode.HALF_UP);
        // Đảm bảo điểm không vượt quá 30
        if (dxt.compareTo(new BigDecimal("30")) > 0) {
            dxt = new BigDecimal("30.00000");
        }
        nv.setDiemXettuyen(dxt);

        // Tạo nv_keys nếu chưa có
        if (nv.getNvKeys() == null || nv.getNvKeys().isEmpty()) {
            nv.setNvKeys(nv.getNnCccd() + "_" + maNganh + "_" + (phuongthuc != null ? phuongthuc : ""));
        }
    }

    // ════════════════════════════════════════════════════════════
    //  MAPPING PHƯƠNG THỨC
    // ════════════════════════════════════════════════════════════

    /**
     * Chuyển mã phương thức lưu trong nguyện vọng (tt_phuongthuc)
     * sang mã d_phuongthuc trong bảng xt_diemthixettuyen.
     *
     *   PT2, THPT, 1 → "1"
     *   DGNL, PT4, 4  → "4"
     *   VSAT, PT5, 5  → "5"
     *   Mặc định      → "1"
     */
    private String mapPhuongThucToCode(String ttPhuongthuc) {
        if (ttPhuongthuc == null) return "1";
        switch (ttPhuongthuc.toUpperCase().trim()) {
            case "4": case "PT4": case "DGNL":        return "4";
            case "5": case "PT5": case "VSAT":         return "5";
            case "1": case "PT1": case "PT2": case "THPT": default: return "1";
        }
    }

    // ════════════════════════════════════════════════════════════
    //  TÍNH ĐTHGXT (Điểm tổ hợp gốc xét tuyển)
    // ════════════════════════════════════════════════════════════

    /**
     * Tính ĐTHGXT: với mỗi tổ hợp của ngành,
     *   score = DiemToHop(tổ hợp dự thi) - dolech
     * Lấy tổ hợp cho điểm cao nhất sau khi trừ dolech.
     *
     * Với phương thức ĐGNL: quy đổi NL1 → thang 30 qua bảng xt_bangquydoi.
     * Với phương thức VSAT: quy đổi từng môn V-SAT → THPT qua bảng xt_bangquydoi,
     *   rồi tính tổ hợp bình thường.
     * Với phương thức THPT: tính trực tiếp từ điểm thi × hệ số môn.
     */
    private TohopResult calculateDTHGXT(DiemThiXetTuyen diemThi, String maNganh, String phuongthuc) {
        List<NganhTohop> tohopList = nganhTohopDAO.findByMaNganh(maNganh);
        TohopResult result = new TohopResult();
        result.rawScore = BigDecimal.ZERO;
        result.adjustedScore = BigDecimal.ZERO;

        if (tohopList == null || tohopList.isEmpty()) {
            return result;
        }

        BigDecimal bestAdjusted = new BigDecimal("-1");

        for (NganhTohop nt : tohopList) {
            BigDecimal rawScore;

            if ("DGNL".equalsIgnoreCase(phuongthuc) || "PT4".equalsIgnoreCase(phuongthuc) || "4".equals(phuongthuc)) {
                // ĐGNL: quy đổi NL1 → thang 30 bằng bảng quy đổi theo tổ hợp
                rawScore = convertDGNLScore(diemThi.getNl1(), nt.getMatohop());
            } else if ("VSAT".equalsIgnoreCase(phuongthuc) || "PT5".equalsIgnoreCase(phuongthuc) || "5".equals(phuongthuc)) {
                // V-SAT — quy đổi từng môn sang thang THPT, rồi tính tổ hợp
                rawScore = calculateVSATScore(diemThi, nt);
            } else {
                // THPT (mặc định): tính từ điểm thi × hệ số môn
                rawScore = calculateTHPTScore(diemThi, nt);
            }

            // Trừ độ lệch giữa tổ hợp dự thi và tổ hợp gốc của ngành
            BigDecimal dolech = nt.getDolech() != null ? nt.getDolech() : BigDecimal.ZERO;
            BigDecimal adjustedScore = rawScore.subtract(dolech);
            if (adjustedScore.compareTo(BigDecimal.ZERO) < 0) adjustedScore = BigDecimal.ZERO;

            if (adjustedScore.compareTo(bestAdjusted) > 0) {
                bestAdjusted = adjustedScore;
                result.bestTohop = nt;
                result.rawScore = rawScore;
                result.adjustedScore = adjustedScore;
            }
        }

        return result;
    }

    // ════════════════════════════════════════════════════════════
    //  TÍNH ĐIỂM THEO TỪNG PHƯƠNG THỨC
    // ════════════════════════════════════════════════════════════

    /**
     * Tính điểm THPT cho 1 tổ hợp:
     *   rawScore = (mon1 * hs1 + mon2 * hs2 + mon3 * hs3) * 3 / (hs1 + hs2 + hs3)
     * → Quy về thang 30.
     */
    private BigDecimal calculateTHPTScore(DiemThiXetTuyen diemThi, NganhTohop nt) {
        BigDecimal mon1Score = getSubjectScore(diemThi, nt.getThMon1());
        BigDecimal mon2Score = getSubjectScore(diemThi, nt.getThMon2());
        BigDecimal mon3Score = getSubjectScore(diemThi, nt.getThMon3());

        int hs1 = nt.getHsmon1() != null ? nt.getHsmon1() : 1;
        int hs2 = nt.getHsmon2() != null ? nt.getHsmon2() : 1;
        int hs3 = nt.getHsmon3() != null ? nt.getHsmon3() : 1;
        int totalHs = hs1 + hs2 + hs3;

        BigDecimal rawSum = mon1Score.multiply(BigDecimal.valueOf(hs1))
                .add(mon2Score.multiply(BigDecimal.valueOf(hs2)))
                .add(mon3Score.multiply(BigDecimal.valueOf(hs3)));

        if (totalHs == 3) {
            // Hệ số đều (1,1,1) → tổng 3 môn đã đúng thang 30
            return rawSum.setScale(5, RoundingMode.HALF_UP);
        } else {
            // Có hệ số: chuẩn hóa về thang 30
            return rawSum.multiply(BigDecimal.valueOf(3))
                    .divide(BigDecimal.valueOf(totalHs), 5, RoundingMode.HALF_UP);
        }
    }

    /**
     * Tính điểm V-SAT cho 1 tổ hợp:
     * - Quy đổi từng môn từ thang V-SAT về thang THPT (0-10) qua bảng xt_bangquydoi.
     * - Sau đó tính tổ hợp giống THPT.
     *
     * Bảng quy đổi V-SAT sử dụng d_phuongthuc = "VSAT", d_tohop = null (hoặc "VSAT"),
     * d_mon = mã môn (TO, LI, HO...).
     * Nội suy tuyến tính: y = d_diemc + (x - d_diema) / (d_diemb - d_diema) * (d_diemd - d_diemc)
     */
    private BigDecimal calculateVSATScore(DiemThiXetTuyen diemThi, NganhTohop nt) {
        BigDecimal mon1Raw = getRawSubjectScore(diemThi, nt.getThMon1());
        BigDecimal mon2Raw = getRawSubjectScore(diemThi, nt.getThMon2());
        BigDecimal mon3Raw = getRawSubjectScore(diemThi, nt.getThMon3());

        BigDecimal mon1Converted = convertVSATSubjectScore(mon1Raw, nt.getThMon1());
        BigDecimal mon2Converted = convertVSATSubjectScore(mon2Raw, nt.getThMon2());
        BigDecimal mon3Converted = convertVSATSubjectScore(mon3Raw, nt.getThMon3());

        int hs1 = nt.getHsmon1() != null ? nt.getHsmon1() : 1;
        int hs2 = nt.getHsmon2() != null ? nt.getHsmon2() : 1;
        int hs3 = nt.getHsmon3() != null ? nt.getHsmon3() : 1;
        int totalHs = hs1 + hs2 + hs3;

        BigDecimal rawSum = mon1Converted.multiply(BigDecimal.valueOf(hs1))
                .add(mon2Converted.multiply(BigDecimal.valueOf(hs2)))
                .add(mon3Converted.multiply(BigDecimal.valueOf(hs3)));

        if (totalHs == 3) {
            return rawSum.setScale(5, RoundingMode.HALF_UP);
        } else {
            return rawSum.multiply(BigDecimal.valueOf(3))
                    .divide(BigDecimal.valueOf(totalHs), 5, RoundingMode.HALF_UP);
        }
    }

    /**
     * Quy đổi điểm 1 môn V-SAT về thang THPT (0-10)
     * bằng nội suy tuyến tính từ bảng xt_bangquydoi (d_phuongthuc = "VSAT", d_mon = monKey).
     * Nếu không có bảng quy đổi → trả về điểm gốc (fallback, không mất dữ liệu).
     */
    private BigDecimal convertVSATSubjectScore(BigDecimal diemVSAT, String monKey) {
        if (diemVSAT == null || diemVSAT.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        if (monKey == null) return BigDecimal.ZERO;

        // Lấy bảng quy đổi cho môn cụ thể (VSAT, tổ hợp null, theo môn)
        List<BangQuydoi> quydoiList = bangQuydoiDAO.findByPhuongthucAndTohop("VSAT", monKey);
        if (quydoiList == null || quydoiList.isEmpty()) {
            // Thử tìm với tohop = "VSAT" (fallback nếu d_tohop lưu là "VSAT")
            quydoiList = bangQuydoiDAO.findByPhuongthucAndTohop("VSAT", "VSAT");
        }
        if (quydoiList == null || quydoiList.isEmpty()) {
            // Không có bảng quy đổi → trả về điểm gốc / 100 * 10 (ước tính thô)
            return diemVSAT.divide(BigDecimal.TEN, 5, RoundingMode.HALF_UP);
        }

        return interpolateScore(diemVSAT, quydoiList);
    }

    /**
     * Quy đổi điểm ĐGNL (NL1, thang 1200) sang thang 30 bằng bảng xt_bangquydoi.
     * Tìm dòng quy đổi trong đó d_diema <= diemNL1 <= d_diemb,
     * rồi nội suy tuyến tính giữa d_diemc và d_diemd.
     */
    private BigDecimal convertDGNLScore(BigDecimal diemNL1, String tohop) {
        if (diemNL1 == null) return BigDecimal.ZERO;

        List<BangQuydoi> quydoiList = bangQuydoiDAO.findByPhuongthucAndTohop("DGNL", tohop);
        if (quydoiList == null || quydoiList.isEmpty()) {
            quydoiList = bangQuydoiDAO.findByPhuongthucAndTohop("DGNL", "A01");
        }
        if (quydoiList == null || quydoiList.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return interpolateScore(diemNL1, quydoiList);
    }

    /**
     * Nội suy tuyến tính chung:
     *   result = d_diemc + (x - d_diema) / (d_diemb - d_diema) * (d_diemd - d_diemc)
     * Quét qua danh sách khoảng, trả về giá trị khoảng khớp đầu tiên.
     */
    private BigDecimal interpolateScore(BigDecimal x, List<BangQuydoi> quydoiList) {
        if (quydoiList == null || quydoiList.isEmpty()) return BigDecimal.ZERO;
        
        quydoiList.sort(Comparator.comparing(BangQuydoi::getdDiema));

        // Nằm ngoài khoảng dưới
        if (x.compareTo(quydoiList.get(0).getdDiema()) <= 0) {
            return quydoiList.get(0).getdDiemc() != null ? quydoiList.get(0).getdDiemc() : BigDecimal.ZERO;
        }

        // Nằm ngoài khoảng trên
        BangQuydoi lastQd = quydoiList.get(quydoiList.size() - 1);
        if (x.compareTo(lastQd.getdDiemb()) >= 0) {
            return lastQd.getdDiemd() != null ? lastQd.getdDiemd() : (lastQd.getdDiemc() != null ? lastQd.getdDiemc() : BigDecimal.ZERO);
        }

        for (BangQuydoi qd : quydoiList) {
            if (qd.getdDiema() == null || qd.getdDiemb() == null) continue;
            // Nằm lọt trong phổ nội suy (mở tại cận trên để không gộp lặp)
            if (x.compareTo(qd.getdDiema()) >= 0 && x.compareTo(qd.getdDiemb()) < 0) {
                BigDecimal diemc = qd.getdDiemc() != null ? qd.getdDiemc() : BigDecimal.ZERO;
                BigDecimal diemd = qd.getdDiemd() != null ? qd.getdDiemd() : diemc;
                BigDecimal rangeInput = qd.getdDiemb().subtract(qd.getdDiema());
                BigDecimal rangeOutput = diemd.subtract(diemc);

                if (rangeInput.compareTo(BigDecimal.ZERO) == 0) return diemc;

                BigDecimal ratio = x.subtract(qd.getdDiema()).divide(rangeInput, 5, RoundingMode.HALF_UP);
                return diemc.add(ratio.multiply(rangeOutput)).setScale(5, RoundingMode.HALF_UP);
            }
        }
        return BigDecimal.ZERO;
    }

    // ════════════════════════════════════════════════════════════
    //  LẤY ĐIỂM MÔN
    // ════════════════════════════════════════════════════════════

    /**
     * Lấy điểm môn đã xử lý (đặc biệt N1 = MAX(N1_THI, N1_CC)).
     * Dùng khi tính tổ hợp THPT.
     */
    private BigDecimal getSubjectScore(DiemThiXetTuyen d, String monKey) {
        if (monKey == null) return BigDecimal.ZERO;
        switch (monKey.toUpperCase()) {
            case "TO":   return d.getTo()   != null ? d.getTo()   : BigDecimal.ZERO;
            case "LI":   return d.getLi()   != null ? d.getLi()   : BigDecimal.ZERO;
            case "HO":   return d.getHo()   != null ? d.getHo()   : BigDecimal.ZERO;
            case "SI":   return d.getSi()   != null ? d.getSi()   : BigDecimal.ZERO;
            case "SU":   return d.getSu()   != null ? d.getSu()   : BigDecimal.ZERO;
            case "DI":   return d.getDi()   != null ? d.getDi()   : BigDecimal.ZERO;
            case "VA":   return d.getVa()   != null ? d.getVa()   : BigDecimal.ZERO;
            // FIX #2: N1 = MAX(điểm thi gốc, quy đổi từ chứng chỉ)
            case "N1":   return getN1Score(d);
            case "TI":   return d.getTi()   != null ? d.getTi()   : BigDecimal.ZERO;
            case "CNCN": return d.getCncn() != null ? d.getCncn() : BigDecimal.ZERO;
            case "CNNN": return d.getCnnn() != null ? d.getCnnn() : BigDecimal.ZERO;
            case "KTPL": return d.getKtpl() != null ? d.getKtpl() : BigDecimal.ZERO;
            case "NK1":  return d.getNk1()  != null ? d.getNk1()  : BigDecimal.ZERO;
            case "NK2":  return d.getNk2()  != null ? d.getNk2()  : BigDecimal.ZERO;
            case "NK3":  return d.getNk3()  != null ? d.getNk3()  : BigDecimal.ZERO;
            case "NK4":  return d.getNk4()  != null ? d.getNk4()  : BigDecimal.ZERO;
            case "NK5":  return d.getNk5()  != null ? d.getNk5()  : BigDecimal.ZERO;
            case "NK6":  return d.getNk6()  != null ? d.getNk6()  : BigDecimal.ZERO;
            default:     return BigDecimal.ZERO;
        }
    }

    /**
     * Lấy điểm môn thô (chưa xử lý N1 MAX) — dùng trước khi quy đổi V-SAT.
     */
    private BigDecimal getRawSubjectScore(DiemThiXetTuyen d, String monKey) {
        if (monKey == null) return BigDecimal.ZERO;
        switch (monKey.toUpperCase()) {
            case "TO":   return d.getTo()   != null ? d.getTo()   : BigDecimal.ZERO;
            case "LI":   return d.getLi()   != null ? d.getLi()   : BigDecimal.ZERO;
            case "HO":   return d.getHo()   != null ? d.getHo()   : BigDecimal.ZERO;
            case "SI":   return d.getSi()   != null ? d.getSi()   : BigDecimal.ZERO;
            case "SU":   return d.getSu()   != null ? d.getSu()   : BigDecimal.ZERO;
            case "DI":   return d.getDi()   != null ? d.getDi()   : BigDecimal.ZERO;
            case "VA":   return d.getVa()   != null ? d.getVa()   : BigDecimal.ZERO;
            case "N1":   return d.getN1Thi() != null ? d.getN1Thi() : BigDecimal.ZERO;
            case "TI":   return d.getTi()   != null ? d.getTi()   : BigDecimal.ZERO;
            case "CNCN": return d.getCncn() != null ? d.getCncn() : BigDecimal.ZERO;
            case "CNNN": return d.getCnnn() != null ? d.getCnnn() : BigDecimal.ZERO;
            case "KTPL": return d.getKtpl() != null ? d.getKtpl() : BigDecimal.ZERO;
            default:     return BigDecimal.ZERO;
        }
    }

    /**
     * FIX #2: Điểm N1 (Tiếng Anh) = MAX(N1_THI, N1_CC).
     * N1_THI = điểm thi THPT gốc.
     * N1_CC  = điểm quy đổi từ chứng chỉ tiếng Anh (IELTS, TOEFL...).
     * Lấy giá trị lớn hơn để có lợi cho thí sinh.
     */
    private BigDecimal getN1Score(DiemThiXetTuyen d) {
        BigDecimal n1Thi = d.getN1Thi() != null ? d.getN1Thi() : BigDecimal.ZERO;
        BigDecimal n1Cc  = d.getN1Cc()  != null ? d.getN1Cc()  : BigDecimal.ZERO;
        return n1Thi.compareTo(n1Cc) >= 0 ? n1Thi : n1Cc;
    }

    // ════════════════════════════════════════════════════════════
    //  BƯỚC 2: THUẬT TOÁN XÉT TUYỂN (Gale-Shapley deferred acceptance)
    // ════════════════════════════════════════════════════════════

    /**
     * Chạy thuật toán xét tuyển Gale-Shapley.
     * 1. Mỗi thí sinh đề xuất vào nguyện vọng ưu tiên cao nhất chưa bị từ chối.
     * 2. Mỗi ngành so sánh và giữ lại top N (theo chỉ tiêu), loại thí sinh yếu nhất.
     * 3. Thí sinh bị loại chuyển sang nguyện vọng tiếp theo.
     * 4. Lặp lại cho đến khi không còn thí sinh nào cần xử lý.
     */
    public void executeAdmissionProcess() {
        System.out.println("═══ Bắt đầu quy trình xét tuyển ═══");

        // 1. Fetch data
        List<NguyenVongXetTuyen> allAspirations = nguyenVongDAO.findAllOrdered();
        List<Nganh> allNganh = nganhDAO.findAll();

        Map<String, Nganh> nganhMap = new HashMap<>();
        for (Nganh n : allNganh) {
            nganhMap.put(n.getManganh(), n);
        }

        // 2. Group nguyện vọng theo CCCD
        Map<String, List<NguyenVongXetTuyen>> candidateAspirations = new LinkedHashMap<>();
        for (NguyenVongXetTuyen nv : allAspirations) {
            candidateAspirations.computeIfAbsent(nv.getNnCccd(), k -> new ArrayList<>()).add(nv);
        }

        // 3. Sắp xếp nguyện vọng theo thứ tự ưu tiên (nvTt), null-safe
        for (List<NguyenVongXetTuyen> list : candidateAspirations.values()) {
            list.sort(Comparator.comparingInt(nv -> nv.getNvTt() != null ? nv.getNvTt() : Integer.MAX_VALUE));
        }

        // acceptedMap[maNganh] = PriorityQueue (min-heap theo điểm XT)
        Map<String, PriorityQueue<NguyenVongXetTuyen>> acceptedMap = new HashMap<>();
        for (String maNganh : nganhMap.keySet()) {
            acceptedMap.put(maNganh, new PriorityQueue<>((nv1, nv2) -> {
                BigDecimal s1 = nv1.getDiemXettuyen() != null ? nv1.getDiemXettuyen() : BigDecimal.ZERO;
                BigDecimal s2 = nv2.getDiemXettuyen() != null ? nv2.getDiemXettuyen() : BigDecimal.ZERO;
                int cmp = s1.compareTo(s2);
                if (cmp == 0) {
                    BigDecimal raw1 = nv1.getDiemThxt() != null ? nv1.getDiemThxt() : BigDecimal.ZERO;
                    BigDecimal raw2 = nv2.getDiemThxt() != null ? nv2.getDiemThxt() : BigDecimal.ZERO;
                    return raw1.compareTo(raw2);
                }
                return cmp;
            }));
        }

        Queue<String> candidatesToProcess = new LinkedList<>(candidateAspirations.keySet());
        Map<String, Integer> currentIndex = new HashMap<>();
        for (String cccd : candidateAspirations.keySet()) {
            currentIndex.put(cccd, 0);
        }

        // 5. Gale-Shapley loop
        int iterations = 0;
        while (!candidatesToProcess.isEmpty()) {
            String cccd = candidatesToProcess.poll();
            int idx = currentIndex.getOrDefault(cccd, 0);
            List<NguyenVongXetTuyen> aps = candidateAspirations.get(cccd);

            if (idx >= aps.size()) continue; // Hết nguyện vọng

            NguyenVongXetTuyen currentNv = aps.get(idx);
            String maNganh = currentNv.getNvManganh();
            Nganh nganh = nganhMap.get(maNganh);

            // Skip nếu ngành không tồn tại hoặc không có điểm XT
            if (nganh == null || currentNv.getDiemXettuyen() == null) {
                currentIndex.put(cccd, idx + 1);
                candidatesToProcess.add(cccd);
                continue;
            }

            BigDecimal score = currentNv.getDiemXettuyen();
            BigDecimal diemsan = nganh.getnDiemsan() != null ? nganh.getnDiemsan() : BigDecimal.ZERO;

            // Dưới điểm sàn → chuyển NV tiếp theo
            if (score.compareTo(diemsan) < 0) {
                currentIndex.put(cccd, idx + 1);
                candidatesToProcess.add(cccd);
                continue;
            }

            PriorityQueue<NguyenVongXetTuyen> acceptedList = acceptedMap.get(maNganh);
            if (acceptedList == null) {
                currentIndex.put(cccd, idx + 1);
                candidatesToProcess.add(cccd);
                continue;
            }

            int quota = nganh.getnChitieu() != null ? nganh.getnChitieu() : 0;

            if (quota <= 0) {
                currentIndex.put(cccd, idx + 1);
                candidatesToProcess.add(cccd);
                continue;
            }

            if (acceptedList.size() < quota) {
                // Còn chỗ → accept
                acceptedList.add(currentNv);
            } else {
                // Đầy → so sánh với người yếu nhất
                NguyenVongXetTuyen weakest = acceptedList.peek();
                if (weakest != null) {
                    BigDecimal weakestScore = weakest.getDiemXettuyen() != null ? weakest.getDiemXettuyen() : BigDecimal.ZERO;
                    BigDecimal currentScore = currentNv.getDiemXettuyen() != null ? currentNv.getDiemXettuyen() : BigDecimal.ZERO;
                    
                    int cmpScore = currentScore.compareTo(weakestScore);
                    int cmpRaw = 0;
                    if (cmpScore == 0) {
                        BigDecimal weakestRaw = weakest.getDiemThxt() != null ? weakest.getDiemThxt() : BigDecimal.ZERO;
                        BigDecimal currentRaw = currentNv.getDiemThxt() != null ? currentNv.getDiemThxt() : BigDecimal.ZERO;
                        cmpRaw = currentRaw.compareTo(weakestRaw);
                    }

                    if (cmpScore > 0 || (cmpScore == 0 && cmpRaw > 0)) {
                        // current is strictly better than weakest
                        acceptedList.poll();
                        String weakestCccd = weakest.getNnCccd();
                        currentIndex.put(weakestCccd, currentIndex.getOrDefault(weakestCccd, 0) + 1);
                        candidatesToProcess.add(weakestCccd);
                        acceptedList.add(currentNv);
                    } else if (cmpScore == 0 && cmpRaw == 0) {
                        // equal tie, both accepted, bypassing quota
                        acceptedList.add(currentNv);
                    } else {
                        // Bị từ chối → chuyển NV tiếp
                        currentIndex.put(cccd, idx + 1);
                        candidatesToProcess.add(cccd);
                    }
                }
            }

            iterations++;
            if (iterations > allAspirations.size() * 10) {
                System.out.println("⚠️ Dừng xét tuyển: quá nhiều vòng lặp!");
                break;
            }
        }

        // 6. Cập nhật kết quả
        System.out.println("Cập nhật kết quả vào database...");

        // Reset tất cả kết quả
        for (NguyenVongXetTuyen nv : allAspirations) {
            nv.setNvKetqua("duoisan"); // Mặc định rớt
        }

        // Đánh dấu trúng tuyển + tính điểm trúng tuyển mỗi ngành
        for (Map.Entry<String, PriorityQueue<NguyenVongXetTuyen>> entry : acceptedMap.entrySet()) {
            PriorityQueue<NguyenVongXetTuyen> accepted = entry.getValue();
            Nganh nganh = nganhMap.get(entry.getKey());

            if (!accepted.isEmpty()) {
                // Điểm trúng tuyển = điểm thấp nhất trong accepted
                BigDecimal diemChuan = accepted.peek().getDiemXettuyen();
                if (nganh != null) {
                    nganh.setnDiemtrungtuyen(diemChuan);
                }
            }

            for (NguyenVongXetTuyen nv : accepted) {
                nv.setNvKetqua("yes");
            }
        }

        // 7. Batch update nguyện vọng
        nguyenVongDAO.batchUpdate(allAspirations);

        // 8. Lưu điểm trúng tuyển vào bảng ngành
        for (Nganh nganh : allNganh) {
            if (nganh.getnDiemtrungtuyen() != null) {
                nganhDAO.update(nganh);
            }
        }

        System.out.println("═══ Xét tuyển hoàn tất! Đã xử lý " + allAspirations.size()
                + " nguyện vọng trong " + iterations + " vòng lặp ═══");
    }

    // ════════════════════════════════════════════════════════════
    //  HELPER: TÍNH ĐIỂM ƯU TIÊN
    // ════════════════════════════════════════════════════════════

    /**
     * Tính Mức điểm ưu tiên (MĐƯT) = điểm khu vực + điểm đối tượng.
     * Hỗ trợ cả "KV2NT" và "KV2-NT".
     */
    private BigDecimal calculateMdut(String khuVuc, String doiTuong) {
        BigDecimal uutien = BigDecimal.ZERO;

        if (khuVuc != null) {
            switch (khuVuc.toUpperCase().replace("-", "")) {
                case "KV1":   uutien = uutien.add(new BigDecimal("0.75")); break;
                case "KV2NT": uutien = uutien.add(new BigDecimal("0.5"));  break;
                case "KV2":   uutien = uutien.add(new BigDecimal("0.25")); break;
                // KV3: không có điểm ưu tiên khu vực
            }
        }

        if (doiTuong != null) {
            switch (doiTuong) {
                case "01": uutien = uutien.add(new BigDecimal("2.0")); break;
                case "02": uutien = uutien.add(new BigDecimal("1.5")); break;
                case "03": uutien = uutien.add(new BigDecimal("1.0")); break;
                case "04": break; // 0.0
                case "05":
                case "06":
                case "07": uutien = uutien.add(new BigDecimal("1.0")); break; 
            }
        }

        return uutien;
    }

    /**
     * Lấy tổng điểm cộng cho thí sinh ở ngành + tổ hợp cụ thể. Giới hạn max 3.0.
     * Chống cộng trùng: Nếu tổ hợp đã chứa Ngoại ngữ, trừ đi phần điểm cấu thành CC.
     */
    private BigDecimal getDiemCong(String cccd, String maNganh, String tohop, boolean containsN1) {
        List<DiemCongXetTuyen> diems = diemCongDAO.findByCccd(cccd);
        BigDecimal sum = BigDecimal.ZERO;
        for (DiemCongXetTuyen dc : diems) {
            if (dc.getManganh() != null && !dc.getManganh().equals(maNganh)) continue;
            if (tohop != null && dc.getMatohop() != null && !dc.getMatohop().equals(tohop)) continue;
            
            if (dc.getDiemTong() != null) {
                BigDecimal diemThem = dc.getDiemTong();
                // Rào ngoại ngữ để tránh tính vào tổng điểm cộng 2 lần
                if (containsN1 && dc.getDiemCC() != null) {
                    diemThem = diemThem.subtract(dc.getDiemCC());
                }
                
                if (diemThem.compareTo(BigDecimal.ZERO) > 0) {
                    sum = sum.add(diemThem);
                }
            }
        }
        // Giới hạn tối đa 3.0 điểm cộng
        if (sum.compareTo(new BigDecimal("3.0")) > 0) {
            return new BigDecimal("3.0");
        }
        return sum;
    }
}
