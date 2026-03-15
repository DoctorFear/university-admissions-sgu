package com.xettuyen2026.service;

import com.xettuyen2026.dao.*;
import com.xettuyen2026.entity.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Service tính điểm + chạy thuật toán xét tuyển.
 *
 * Các bước:
 * 1. calculateAllScores()  — Tính ĐTHGXT, ĐC, ĐƯT, ĐXT cho mỗi nguyện vọng
 * 2. executeAdmissionProcess() — Chạy Gale-Shapley deferred acceptance
 */
public class AdmissionService {

    private NguyenVongDAO nguyenVongDAO = new NguyenVongDAO();
    private NganhDAO nganhDAO = new NganhDAO();
    private NganhTohopDAO nganhTohopDAO = new NganhTohopDAO();
    private DiemThiDAO diemThiDAO = new DiemThiDAO();
    private ThiSinhDAO thiSinhDAO = new ThiSinhDAO();
    private DiemCongDAO diemCongDAO = new DiemCongDAO();
    private BangQuydoiDAO bangQuydoiDAO = new BangQuydoiDAO();

    // ══════════════════════════════════════════════════════════════
    //  BƯỚC 1: TÍNH ĐIỂM XÉT TUYỂN CHO MỖI NGUYỆN VỌNG
    // ══════════════════════════════════════════════════════════════

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

    /**
     * Tính điểm cho 1 nguyện vọng:
     *   ĐTHGXT → ĐC → ĐƯT → ĐXT
     */
    public void calculateScoreForAspiration(NguyenVongXetTuyen nv) {
        ThiSinh thiSinh = thiSinhDAO.findByCccd(nv.getNnCccd());
        if (thiSinh == null) return;

        DiemThiXetTuyen diemThi = diemThiDAO.findByCccd(nv.getNnCccd());
        if (diemThi == null) return;

        String maNganh = nv.getNvManganh();
        String phuongthuc = nv.getTtPhuongthuc();

        // ── Bước 1: Tính ĐTHGXT (Điểm thi hàng gốc xét tuyển) ──
        BigDecimal dthgxt = calculateDTHGXT(diemThi, maNganh, phuongthuc);
        nv.setDiemThxt(dthgxt);

        // ── Bước 2: Lấy Điểm cộng (ĐC) ──
        BigDecimal dCong = getDiemCong(nv.getNnCccd(), maNganh, nv.getTtThm());
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

        // ── Bước 4: ĐXT = ĐTHGXT + ĐC + ĐƯT ──
        BigDecimal dxt = dthgxt.add(dCong).add(dUutien).setScale(5, RoundingMode.HALF_UP);
        nv.setDiemXettuyen(dxt);

        // Tạo nv_keys nếu chưa có
        if (nv.getNvKeys() == null || nv.getNvKeys().isEmpty()) {
            nv.setNvKeys(nv.getNnCccd() + "_" + maNganh + "_" + (phuongthuc != null ? phuongthuc : ""));
        }
    }

    /**
     * Tính ĐTHGXT: chọn tổ hợp tốt nhất cho ngành, lấy điểm các môn × hệ số.
     *
     * Với phương thức ĐGNL: quy đổi từ điểm NL1 sang thang 30 bằng bảng xt_bangquydoi.
     * Với phương thức THPT/PT2: tính từ điểm thi THPT × hệ số môn chính.
     */
    private BigDecimal calculateDTHGXT(DiemThiXetTuyen diemThi, String maNganh, String phuongthuc) {
        // Lấy tất cả tổ hợp có thể xét cho ngành này
        List<NganhTohop> tohopList = nganhTohopDAO.findByMaNganh(maNganh);
        if (tohopList == null || tohopList.isEmpty()) {
            // Không có tổ hợp → trả điểm 0
            return BigDecimal.ZERO;
        }

        BigDecimal bestScore = BigDecimal.ZERO;

        for (NganhTohop nt : tohopList) {
            BigDecimal score;

            if ("DGNL".equals(phuongthuc)) {
                // Quy đổi điểm ĐGNL
                score = convertDGNLScore(diemThi.getNl1(), nt.getMatohop());
            } else {
                // Tính từ điểm thi THPT (PT2, PT4, etc.)
                score = calculateTHPTScore(diemThi, nt);
            }

            if (score.compareTo(bestScore) > 0) {
                bestScore = score;
            }
        }

        return bestScore;
    }

    /**
     * Tính điểm THPT cho 1 tổ hợp:
     * Score = mon1_score * hsmon1 + mon2_score * hsmon2 + mon3_score * hsmon3
     * Rồi chia cho tổng hệ số, nhân 3 để ra thang 30.
     *
     * Ví dụ: hsmon1=3, hsmon2=3, hsmon3=1 → tổng hs = 7
     * Score = (mon1*3 + mon2*3 + mon3*1) * 3 / 7  → tương đương thang 30
     *
     * Thực tế đơn giản hơn: score = điểm_mon1 * hs1 + điểm_mon2 * hs2 + điểm_mon3 * hs3
     * Chuẩn hóa: score * 30 / (10 * tổng_hs)
     * Nhưng theo data mẫu, cách tính thực tế là:
     *   score = (mon1*hs1 + mon2*hs2 + mon3*hs3) * 3 / totalHs
     */
    private BigDecimal calculateTHPTScore(DiemThiXetTuyen diemThi, NganhTohop nt) {
        BigDecimal mon1Score = getSubjectScore(diemThi, nt.getThMon1());
        BigDecimal mon2Score = getSubjectScore(diemThi, nt.getThMon2());
        BigDecimal mon3Score = getSubjectScore(diemThi, nt.getThMon3());

        int hs1 = nt.getHsmon1() != null ? nt.getHsmon1() : 1;
        int hs2 = nt.getHsmon2() != null ? nt.getHsmon2() : 1;
        int hs3 = nt.getHsmon3() != null ? nt.getHsmon3() : 1;
        int totalHs = hs1 + hs2 + hs3;

        // Tính tổng = mon1*hs1 + mon2*hs2 + mon3*hs3
        BigDecimal rawSum = mon1Score.multiply(BigDecimal.valueOf(hs1))
                .add(mon2Score.multiply(BigDecimal.valueOf(hs2)))
                .add(mon3Score.multiply(BigDecimal.valueOf(hs3)));

        // Chuẩn hóa về thang 30: rawSum * 3 / totalHs
        // (vì thang gốc 10 * 3 môn = 30, nhưng hệ số làm thay đổi)
        if (totalHs == 3) {
            // Hệ số bằng nhau (1,1,1) → tổng đã đúng thang 30
            return rawSum.setScale(5, RoundingMode.HALF_UP);
        } else {
            // Có hệ số: score = rawSum * 3 / totalHs
            return rawSum.multiply(BigDecimal.valueOf(3))
                    .divide(BigDecimal.valueOf(totalHs), 5, RoundingMode.HALF_UP);
        }
    }

    /**
     * Lấy điểm của 1 môn cụ thể từ bảng điểm thi.
     */
    private BigDecimal getSubjectScore(DiemThiXetTuyen d, String monKey) {
        if (monKey == null) return BigDecimal.ZERO;
        switch (monKey.toUpperCase()) {
            case "TO": return d.getTo() != null ? d.getTo() : BigDecimal.ZERO;
            case "LI": return d.getLi() != null ? d.getLi() : BigDecimal.ZERO;
            case "HO": return d.getHo() != null ? d.getHo() : BigDecimal.ZERO;
            case "SI": return d.getSi() != null ? d.getSi() : BigDecimal.ZERO;
            case "SU": return d.getSu() != null ? d.getSu() : BigDecimal.ZERO;
            case "DI": return d.getDi() != null ? d.getDi() : BigDecimal.ZERO;
            case "VA": return d.getVa() != null ? d.getVa() : BigDecimal.ZERO;
            case "N1": return d.getN1Cc() != null ? d.getN1Cc()
                           : (d.getN1Thi() != null ? d.getN1Thi() : BigDecimal.ZERO);
            case "TI": return d.getTi() != null ? d.getTi() : BigDecimal.ZERO;
            case "CNCN": return d.getCncn() != null ? d.getCncn() : BigDecimal.ZERO;
            case "CNNN": return d.getCnnn() != null ? d.getCnnn() : BigDecimal.ZERO;
            case "KTPL": return d.getKtpl() != null ? d.getKtpl() : BigDecimal.ZERO;
            case "NK1": return d.getNk1() != null ? d.getNk1() : BigDecimal.ZERO;
            case "NK2": return d.getNk2() != null ? d.getNk2() : BigDecimal.ZERO;
            default: return BigDecimal.ZERO;
        }
    }

    /**
     * Quy đổi điểm ĐGNL (NL1) sang thang 30 bằng bảng xt_bangquydoi.
     * Tìm dòng quy đổi trong đó d_diema <= diemNL1 <= d_diemb,
     * rồi nội suy tuyến tính giữa d_diemc và d_diemd.
     */
    private BigDecimal convertDGNLScore(BigDecimal diemNL1, String tohop) {
        if (diemNL1 == null) return BigDecimal.ZERO;

        List<BangQuydoi> quydoiList = bangQuydoiDAO.findByPhuongthucAndTohop("DGNL", tohop);
        if (quydoiList == null || quydoiList.isEmpty()) {
            // Không có bảng quy đổi → không tính được
            return BigDecimal.ZERO;
        }

        for (BangQuydoi qd : quydoiList) {
            if (qd.getdDiema() == null || qd.getdDiemb() == null) continue;
            // Nếu diemNL1 nằm trong khoảng [d_diema, d_diemb]
            if (diemNL1.compareTo(qd.getdDiema()) >= 0 && diemNL1.compareTo(qd.getdDiemb()) <= 0) {
                // Nội suy tuyến tính: result = d_diemc + (diemNL1 - d_diema) * (d_diemd - d_diemc) / (d_diemb - d_diema)
                BigDecimal diemc = qd.getdDiemc() != null ? qd.getdDiemc() : BigDecimal.ZERO;
                BigDecimal diemd = qd.getdDiemd() != null ? qd.getdDiemd() : diemc;
                BigDecimal rangeInput = qd.getdDiemb().subtract(qd.getdDiema());
                BigDecimal rangeOutput = diemd.subtract(diemc);

                if (rangeInput.compareTo(BigDecimal.ZERO) == 0) return diemc;

                BigDecimal ratio = diemNL1.subtract(qd.getdDiema()).divide(rangeInput, 5, RoundingMode.HALF_UP);
                return diemc.add(ratio.multiply(rangeOutput)).setScale(5, RoundingMode.HALF_UP);
            }
        }

        // Không tìm thấy khoảng phù hợp
        return BigDecimal.ZERO;
    }

    // ══════════════════════════════════════════════════════════════
    //  BƯỚC 2: THUẬT TOÁN XÉT TUYỂN (Gale-Shapley deferred acceptance)
    // ══════════════════════════════════════════════════════════════

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

        // 4. Khởi tạo state
        // acceptedMap[maNganh] = PriorityQueue (min-heap theo điểm XT)
        Map<String, PriorityQueue<NguyenVongXetTuyen>> acceptedMap = new HashMap<>();
        for (String maNganh : nganhMap.keySet()) {
            acceptedMap.put(maNganh, new PriorityQueue<>(
                Comparator.comparing(nv -> nv.getDiemXettuyen() != null ? nv.getDiemXettuyen() : BigDecimal.ZERO)));
        }

        // Thí sinh đã được "accepted" ở đâu đó → không cần xử lý thêm
        Set<String> settledCandidates = new HashSet<>();
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
                // Ngành không có trong acceptedMap (mã lạ)
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
                if (weakest != null && score.compareTo(weakest.getDiemXettuyen()) > 0) {
                    // Thay thế yếu nhất
                    acceptedList.poll();
                    String weakestCccd = weakest.getNnCccd();
                    currentIndex.put(weakestCccd, currentIndex.getOrDefault(weakestCccd, 0) + 1);
                    candidatesToProcess.add(weakestCccd);
                    acceptedList.add(currentNv);
                } else {
                    // Bị từ chối → chuyển NV tiếp
                    currentIndex.put(cccd, idx + 1);
                    candidatesToProcess.add(cccd);
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
            nv.setNvKetqua("duolaar"); // Mặc định rớt
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

    // ══════════════════════════════════════════════════════════════
    //  HELPER: TÍNH ĐIỂM ƯU TIÊN
    // ══════════════════════════════════════════════════════════════

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
            List<String> ut1 = Arrays.asList("01", "02", "03", "04");
            List<String> ut2 = Arrays.asList("05", "06", "07");
            if (ut1.contains(doiTuong)) uutien = uutien.add(new BigDecimal("2.0"));
            else if (ut2.contains(doiTuong)) uutien = uutien.add(new BigDecimal("1.0"));
        }

        return uutien;
    }

    /**
     * Lấy tổng điểm cộng cho thí sinh ở ngành + tổ hợp cụ thể. Giới hạn max 3.0.
     */
    private BigDecimal getDiemCong(String cccd, String maNganh, String tohop) {
        List<DiemCongXetTuyen> diems = diemCongDAO.findByCccd(cccd);
        BigDecimal sum = BigDecimal.ZERO;
        for (DiemCongXetTuyen dc : diems) {
            if (dc.getManganh() != null && !dc.getManganh().equals(maNganh)) continue;
            if (tohop != null && dc.getMatohop() != null && !dc.getMatohop().equals(tohop)) continue;
            if (dc.getDiemTong() != null) {
                sum = sum.add(dc.getDiemTong());
            }
        }
        // Giới hạn tối đa 3.0 điểm cộng
        if (sum.compareTo(new BigDecimal("3.0")) > 0) {
            return new BigDecimal("3.0");
        }
        return sum;
    }
}
