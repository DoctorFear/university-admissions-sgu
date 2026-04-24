package com.xettuyen2026.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xettuyen2026.dao.BangQuydoiDAO;
import com.xettuyen2026.dao.NganhDAO;
import com.xettuyen2026.dao.NganhTohopDAO;
import com.xettuyen2026.dao.NguyenVongDAO;
import com.xettuyen2026.dao.ThiSinhDAO;
import com.xettuyen2026.dto.ThiSinhRequest;
import com.xettuyen2026.dto.TinhDiemRequest;
import com.xettuyen2026.dto.TinhDiemResponse;
import com.xettuyen2026.dto.TraCuuRequest;
import com.xettuyen2026.dto.TraCuuResponse;
import com.xettuyen2026.entity.BangQuydoi;
import com.xettuyen2026.entity.Nganh;
import com.xettuyen2026.entity.NganhTohop;
import com.xettuyen2026.entity.NguyenVongXetTuyen;
import com.xettuyen2026.entity.ThiSinh;
import com.xettuyen2026.service.ThiSinhService;

@RestController
public class ThiSinhController {
    private final ThiSinhService tsService;
    private final ThiSinhDAO thiSinhDAO = new ThiSinhDAO();
    private final NguyenVongDAO nguyenVongDAO = new NguyenVongDAO();
    private final NganhDAO nganhDAO = new NganhDAO();
    private final NganhTohopDAO nganhTohopDAO = new NganhTohopDAO();
    private final BangQuydoiDAO bangQuydoiDAO = new BangQuydoiDAO();

    public ThiSinhController() {
        this.tsService = new ThiSinhService();
    }

    @GetMapping("/api/nganh")
    @CrossOrigin(origins = "*")
    public List<Nganh> getListNganh() {
        return nganhDAO.findAll();
    }
    
    @GetMapping("/")
    public String Testing() {
        return "Hello world!";
    }

    @PostMapping("/thisinh/auth")
    public ResponseEntity<String> postMethodName(@RequestBody ThiSinhRequest request) {
        boolean valid = tsService.authenticate(request.getCccd(), request.getPassword());
        
        return valid ? ResponseEntity.ok("OK") : ResponseEntity.status(401).body("INVALID");
    }

    @GetMapping("/api/debug/bangquydoi")
    @CrossOrigin(origins = "*")
    public List<BangQuydoi> debugBangQuydoi() {
        return bangQuydoiDAO.findAll();
    }
    
    @PostMapping("/api/tracuu")
    @CrossOrigin(origins = "*")
    public TraCuuResponse tracuu(@RequestBody TraCuuRequest request) {
        TraCuuResponse response = new TraCuuResponse();
        try {
            ThiSinh ts = thiSinhDAO.findByCccd(request.getCccd());
            if (ts == null) {
                response.setSuccess(false);
                response.setMessage("Không tìm thấy dữ liệu thí sinh khớp với CCCD vừa nhập.");
                return response;
            }
            
            // Generate password from dob: "25/07/2007" -> "25072007"
            String dob = ts.getNgaySinh();
            String dobPassword = (dob != null) ? dob.replace("/", "").replace("-", "") : "";
            
            if (!dobPassword.equals(request.getPassword())) {
                response.setSuccess(false);
                response.setMessage("Mật khẩu (Ngày sinh) không chính xác.");
                return response;
            }

            List<NguyenVongXetTuyen> nvs = nguyenVongDAO.findByCccd(ts.getCccd());
            response.setSuccess(true);
            
            List<TraCuuResponse.NguyenVongDTO> dtoList = new ArrayList<>();
            boolean isPending = false;
            boolean isAdmitted = false;
            TraCuuResponse.NguyenVongDTO admittedNV = null;

            for (NguyenVongXetTuyen nv : nvs) {
                TraCuuResponse.NguyenVongDTO dto = new TraCuuResponse.NguyenVongDTO();
                dto.setMaNganh(nv.getNvManganh());
                
                Nganh nganh = nganhDAO.findByMaNganh(nv.getNvManganh());
                dto.setTenNganh(nganh != null ? nganh.getTennganh() : "Chưa xác định");
                dto.setDiemSan(nganh != null && nganh.getnDiemsan() != null ? nganh.getnDiemsan().doubleValue() : null);
                dto.setDiemChuan(nganh != null && nganh.getnDiemtrungtuyen() != null ? nganh.getnDiemtrungtuyen().doubleValue() : null);
                
                dto.setThuTu(nv.getNvTt());
                dto.setToHop(nv.getTtThm());
                dto.setPhuongThuc(nv.getTtPhuongthuc());
                if (nv.getDiemXettuyen() != null) {
                    dto.setDiemXetTuyen(nv.getDiemXettuyen().doubleValue());
                }
                dto.setKetQua(nv.getNvKetqua());
                dtoList.add(dto);

                String kq = nv.getNvKetqua();
                if (kq == null || kq.trim().isEmpty()) {
                    isPending = true;
                } else if ("yes".equalsIgnoreCase(kq) || "Đậu".equalsIgnoreCase(kq) || "Trúng tuyển".equalsIgnoreCase(kq)) {
                    isAdmitted = true;
                    if (admittedNV == null || (dto.getThuTu() != null && admittedNV.getThuTu() != null && dto.getThuTu() < admittedNV.getThuTu())) {
                        admittedNV = dto;
                    }
                }
            }

            response.setNguyenVongs(dtoList);
            response.setPending(isPending);
            response.setAdmitted(isAdmitted);
            response.setAdmittedNV(admittedNV);

        } catch (Exception e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setMessage("Lỗi hệ thống khi tra cứu.");
        }
        return response;
    }

    @PostMapping("/api/tinhdiem/dgnl")
    @CrossOrigin(origins = "*")
    public TinhDiemResponse tinhDiemDGNL(@RequestBody TinhDiemRequest request) {
        TinhDiemResponse response = new TinhDiemResponse();
        try {
            double diemThi = request.getDiemThi() != null ? request.getDiemThi() : 0;
            double diemCong = request.getDiemCong() != null ? request.getDiemCong() : 0;
            double khuVuc = request.getKhuVuc() != null ? request.getKhuVuc() : 0;
            double doiTuong = request.getDoiTuong() != null ? request.getDoiTuong() : 0;
            
            // Priority score
            double diemUuTien = khuVuc + doiTuong;
            
            Nganh nganh = nganhDAO.findByMaNganh(request.getMaNganh());
            if (nganh == null) {
                throw new Exception("Ngành không tồn tại");
            }
            double diemSan = nganh.getnDiemsan() != null ? nganh.getnDiemsan().doubleValue() : 24.0;
            
            // Get tohop for DGNL if any
            List<NganhTohop> tohopList = nganhTohopDAO.findByMaNganh(request.getMaNganh());
            if (tohopList == null || tohopList.isEmpty()) {
                NganhTohop defaultNt = new NganhTohop();
                defaultNt.setMatohop("A01");
                tohopList = new java.util.ArrayList<>();
                tohopList.add(defaultNt);
            }

            double bestDiemQuyDoi = -1;
            BangQuydoi bestMatched = null;
            String bestTohopGoc = "A01";

            for (NganhTohop nt : tohopList) {
                String tohop = nt.getMatohop();
                List<BangQuydoi> rows = bangQuydoiDAO.findByPhuongthucAndTohop("DGNL", tohop);
                if (rows == null || rows.isEmpty()) {
                    // Fallback to A01 if the combination has no DGNL mapping in the database
                    rows = bangQuydoiDAO.findByPhuongthucAndTohop("DGNL", "A01");
                    tohop = "A01 (Quy đổi mặc định)";
                }

                BangQuydoi matched = null;
                double diemQuyDoi = 0;
                if (rows != null && !rows.isEmpty()) {
                    rows.sort(java.util.Comparator.comparing(BangQuydoi::getdDiema));
                    if (diemThi <= rows.get(0).getdDiema().doubleValue()) {
                        matched = rows.get(0);
                        diemQuyDoi = matched.getdDiemc() != null ? matched.getdDiemc().doubleValue() : 0;
                    } else if (diemThi >= rows.get(rows.size()-1).getdDiemb().doubleValue()) {
                        matched = rows.get(rows.size()-1);
                        diemQuyDoi = matched.getdDiemd() != null ? matched.getdDiemd().doubleValue() : (matched.getdDiemc() != null ? matched.getdDiemc().doubleValue() : 0);
                    } else {
                        for (BangQuydoi qd : rows) {
                            if (qd.getdDiema() == null || qd.getdDiemb() == null) continue;
                            double a = qd.getdDiema().doubleValue();
                            double b = qd.getdDiemb().doubleValue();
                            if (diemThi >= a && diemThi < b) {
                                matched = qd;
                                double c = qd.getdDiemc() != null ? qd.getdDiemc().doubleValue() : 0;
                                double d = qd.getdDiemd() != null ? qd.getdDiemd().doubleValue() : c;
                                if (b == a) {
                                    diemQuyDoi = c;
                                } else {
                                    diemQuyDoi = c + (diemThi - a) / (b - a) * (d - c);
                                }
                                break;
                            }
                        }
                    }
                }
                
                if (diemQuyDoi > bestDiemQuyDoi) {
                    bestDiemQuyDoi = diemQuyDoi;
                    bestMatched = matched;
                    bestTohopGoc = tohop;
                }
            }
            
            double finalDiemQuyDoi = bestDiemQuyDoi > 0 ? bestDiemQuyDoi : 0;
            double diemXetTuyen = finalDiemQuyDoi + diemCong + diemUuTien;
            
            if (bestMatched != null) {
                response.setDiema(bestMatched.getdDiema() != null ? bestMatched.getdDiema().doubleValue() : null);
                response.setDiemb(bestMatched.getdDiemb() != null ? bestMatched.getdDiemb().doubleValue() : null);
                response.setDiemc(bestMatched.getdDiemc() != null ? bestMatched.getdDiemc().doubleValue() : null);
                response.setDiemd(bestMatched.getdDiemd() != null ? bestMatched.getdDiemd().doubleValue() : null);
            }
            
            response.setSuccess(true);
            response.setMaNganh(nganh.getManganh());
            response.setTenNganh(nganh.getTennganh());
            response.setToHop(bestTohopGoc);
            response.setDiemThi(diemThi);
            response.setDiemQuyDoi(finalDiemQuyDoi);
            response.setDiemCong(diemCong);
            response.setDiemUuTien(diemUuTien);
            response.setDiemXetTuyen(diemXetTuyen);
            response.setDiemNguong(diemSan);
            response.setIsDat(diemXetTuyen >= diemSan);
            
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    @PostMapping("/api/tinhdiem/vsat")
    @CrossOrigin(origins = "*")
    public TinhDiemResponse tinhDiemVSAT(@RequestBody TinhDiemRequest request) {
        TinhDiemResponse response = new TinhDiemResponse();
        try {
            double diemCong = request.getDiemCong() != null ? request.getDiemCong() : 0;
            double khuVuc = request.getKhuVuc() != null ? request.getKhuVuc() : 0;
            double doiTuong = request.getDoiTuong() != null ? request.getDoiTuong() : 0;
            double diemUuTien = khuVuc + doiTuong;

            // Map subject code -> input value
            Map<String, Double> subjectInputs = new java.util.HashMap<>();
            subjectInputs.put("TO", request.getToan() != null ? request.getToan() : 0.0);
            subjectInputs.put("VA", request.getVan() != null ? request.getVan() : 0.0);
            subjectInputs.put("SI", request.getSinh() != null ? request.getSinh() : 0.0);
            subjectInputs.put("LI", request.getLy() != null ? request.getLy() : 0.0);
            subjectInputs.put("HO", request.getHoa() != null ? request.getHoa() : 0.0);
            subjectInputs.put("SU", request.getSu() != null ? request.getSu() : 0.0);
            subjectInputs.put("DI", request.getDia() != null ? request.getDia() : 0.0);
            subjectInputs.put("AN", request.getAnh() != null ? request.getAnh() : 0.0);

            Nganh nganh = nganhDAO.findByMaNganh(request.getMaNganh());
            if (nganh == null) {
                throw new Exception("Ngành không tồn tại");
            }
            double diemSan = nganh.getnDiemsan() != null ? nganh.getnDiemsan().doubleValue() : 20.0;

            List<NganhTohop> tohopList = nganhTohopDAO.findByMaNganh(request.getMaNganh());

            List<Map<String, Object>> combinations = new java.util.ArrayList<>();
            String bestTohop = "";
            double bestScore = -Double.MAX_VALUE;

            // Map monCode to Vietnamese name for V-SAT
            Map<String, String> monMap = new java.util.HashMap<>();
            monMap.put("TO", "Toán");
            monMap.put("VA", "Ngữ văn");
            monMap.put("SI", "Sinh học");
            monMap.put("LI", "Vật lý");
            monMap.put("HO", "Hóa học");
            monMap.put("SU", "Lịch sử");
            monMap.put("DI", "Địa lí");
            monMap.put("AN", "Tiếng Anh");

            List<BangQuydoi> allQuydoi = bangQuydoiDAO.findAll();
            List<BangQuydoi> vsatRules = new java.util.ArrayList<>();
            for(BangQuydoi b : allQuydoi) {
                if("V-SAT".equalsIgnoreCase(b.getdPhuongthuc())) {
                    vsatRules.add(b);
                }
            }

            // Helper: look up BangQuydoi and compute converted score for one subject
            // Returns a map with: conv, diema, diemb, diemc, diemd, error
            java.util.function.Function<String[], Map<String, Object>> convertSubject = (args) -> {
                String monCode = args[0];
                String tohopCode = args[1];
                double inputVal = subjectInputs.getOrDefault(monCode, 0.0);
                Map<String, Object> res = new java.util.LinkedHashMap<>();
                res.put("inputVal", inputVal);
                if (inputVal <= 0) {
                    res.put("conv", 0.0);
                    res.put("error", false);
                    return res;
                }
                String monName = monMap.get(monCode);
                List<BangQuydoi> rows = new java.util.ArrayList<>();
                for(BangQuydoi b : vsatRules) {
                    if(monName != null && monName.equalsIgnoreCase(b.getdMon())) {
                        rows.add(b);
                    }
                }
                
                BangQuydoi matched = null;
                for (BangQuydoi bq : rows) {
                    double a = bq.getdDiema() != null ? bq.getdDiema().doubleValue() : 0;
                    double b = bq.getdDiemb() != null ? bq.getdDiemb().doubleValue() : 0;
                    if (inputVal >= a && inputVal <= b) {
                        matched = bq;
                        break;
                    }
                }
                if (matched == null) {
                    res.put("conv", 0.0);
                    res.put("error", true);
                    return res;
                }
                double a = matched.getdDiema().doubleValue();
                double b = matched.getdDiemb().doubleValue();
                double c = matched.getdDiemc().doubleValue();
                double d = matched.getdDiemd().doubleValue();
                double conv = (b == a) ? c : c + (inputVal - a) / (b - a) * (d - c);
                res.put("conv", conv);
                res.put("diema", a);
                res.put("diemb", b);
                res.put("diemc", c);
                res.put("diemd", d);
                res.put("error", false);
                return res;
            };

            if (tohopList != null && !tohopList.isEmpty()) {
                for (NganhTohop th : tohopList) {
                    String mon1 = th.getThMon1();
                    String mon2 = th.getThMon2();
                    String mon3 = th.getThMon3();
                    int hs1 = th.getHsmon1() != null ? th.getHsmon1().intValue() : 1;
                    int hs2 = th.getHsmon2() != null ? th.getHsmon2().intValue() : 1;
                    int hs3 = th.getHsmon3() != null ? th.getHsmon3().intValue() : 1;
                    int hsSum = hs1 + hs2 + hs3;
                    double dolech = th.getDolech() != null ? th.getDolech().doubleValue() : 0.0;

                    Map<String, Object> r1 = convertSubject.apply(new String[]{mon1, th.getMatohop()});
                    Map<String, Object> r2 = convertSubject.apply(new String[]{mon2, th.getMatohop()});
                    Map<String, Object> r3 = convertSubject.apply(new String[]{mon3, th.getMatohop()});

                    double m1Conv = (Double) r1.get("conv");
                    double m2Conv = (Double) r2.get("conv");
                    double m3Conv = (Double) r3.get("conv");
                    double m1Val = (Double) r1.get("inputVal");
                    double m2Val = (Double) r2.get("inputVal");
                    double m3Val = (Double) r3.get("inputVal");

                    // DTHXT (before dolech subtraction, this is the raw weighted average)
                    double dthxtRaw = (m1Conv * hs1 + m2Conv * hs2 + m3Conv * hs3) / (double) hsSum * 3.0;
                    // Net DTHXT sent to formula display
                    double dthxt = dthxtRaw;
                    // Xet nguong = sum of converted + uu tien
                    double xetNguong = m1Conv + m2Conv + m3Conv + diemUuTien;
                    // DXT = DTHXT + DC + DUT - dolech (capped <=30)
                    double dxtCombo = Math.min(dthxt + diemCong + diemUuTien - dolech, 30.0);

                    Map<String, Object> thDetail = new java.util.LinkedHashMap<>();
                    thDetail.put("maTohop", th.getMatohop());
                    thDetail.put("mon1", mon1);
                    thDetail.put("mon2", mon2);
                    thDetail.put("mon3", mon3);
                    thDetail.put("hs1", hs1);
                    thDetail.put("hs2", hs2);
                    thDetail.put("hs3", hs3);
                    thDetail.put("dolech", dolech);
                    thDetail.put("m1Val", m1Val);
                    thDetail.put("m2Val", m2Val);
                    thDetail.put("m3Val", m3Val);
                    thDetail.put("m1Conv", m1Conv);
                    thDetail.put("m2Conv", m2Conv);
                    thDetail.put("m3Conv", m3Conv);
                    // Pass phân vị values for formula display
                    thDetail.put("m1Error", r1.get("error"));
                    thDetail.put("m2Error", r2.get("error"));
                    thDetail.put("m3Error", r3.get("error"));
                    thDetail.put("m1Diema", r1.getOrDefault("diema", null));
                    thDetail.put("m1Diemb", r1.getOrDefault("diemb", null));
                    thDetail.put("m1Diemc", r1.getOrDefault("diemc", null));
                    thDetail.put("m1Diemd", r1.getOrDefault("diemd", null));
                    thDetail.put("m2Diema", r2.getOrDefault("diema", null));
                    thDetail.put("m2Diemb", r2.getOrDefault("diemb", null));
                    thDetail.put("m2Diemc", r2.getOrDefault("diemc", null));
                    thDetail.put("m2Diemd", r2.getOrDefault("diemd", null));
                    thDetail.put("m3Diema", r3.getOrDefault("diema", null));
                    thDetail.put("m3Diemb", r3.getOrDefault("diemb", null));
                    thDetail.put("m3Diemc", r3.getOrDefault("diemc", null));
                    thDetail.put("m3Diemd", r3.getOrDefault("diemd", null));
                    thDetail.put("dthxt", dthxt);
                    thDetail.put("xetNguong", xetNguong);
                    thDetail.put("dut", diemUuTien);
                    thDetail.put("dc", diemCong);
                    thDetail.put("diemXetTuyen", dxtCombo);

                    if (dxtCombo > bestScore) {
                        bestScore = dxtCombo;
                        bestTohop = th.getMatohop();
                    }
                    combinations.add(thDetail);
                }
            }

            if (bestScore == -Double.MAX_VALUE) bestScore = 0;
            if (bestTohop.isEmpty() && !combinations.isEmpty()) {
                bestTohop = (String) combinations.get(0).get("maTohop");
            }

            response.setSuccess(true);
            response.setMaNganh(nganh.getManganh());
            response.setTenNganh(nganh.getTennganh());
            response.setToHop(bestTohop);
            response.setDiemCong(diemCong);
            response.setDiemUuTien(diemUuTien);
            response.setDiemXetTuyen(bestScore);
            response.setDiemNguong(diemSan);
            response.setIsDat(bestScore >= diemSan);
            response.setToHopDetails(combinations);

        } catch (Exception e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }
        return response;
    }
}
