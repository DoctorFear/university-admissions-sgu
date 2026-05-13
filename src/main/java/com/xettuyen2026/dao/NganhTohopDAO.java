package com.xettuyen2026.dao;

import com.xettuyen2026.dao.base.BaseDAO;
import com.xettuyen2026.entity.NganhTohop;
import org.hibernate.Session;

import java.util.List;

/**
 * DAO cho bảng xt_nganh_tohop.
 * Hỗ trợ CRUD + kiểm tra các ràng buộc liên quan đến xét tuyển.
 * 
 * @author Senior Developer
 */
public class NganhTohopDAO extends BaseDAO<NganhTohop> {

    public NganhTohopDAO() {
        super(NganhTohop.class);
    }

    /**
     * Tìm danh sách ngành-tổ hợp theo mã ngành.
     * @param maNganh Mã ngành
     * @return Danh sách ngành-tổ hợp
     */
    public List<NganhTohop> findByMaNganh(String maNganh) {
        return query("FROM NganhTohop n WHERE n.manganh = :maNganh",
                q -> q.setParameter("maNganh", maNganh));
    }

    /**
     * Tìm danh sách ngành-tổ hợp theo mã tổ hợp.
     * @param maTohop Mã tổ hợp
     * @return Danh sách ngành-tổ hợp
     */
    public List<NganhTohop> findByMaTohop(String maTohop) {
        return query("FROM NganhTohop n WHERE n.matohop = :maTohop",
                q -> q.setParameter("maTohop", maTohop));
    }

    /**
     * Tìm ngành-tổ hợp theo tb_keys (manganh_matohop).
     * @param tbKeys Key duy nhất
     * @return Đối tượng NganhTohop hoặc null nếu không tìm thấy
     */
    public NganhTohop findByTbKeys(String tbKeys) {
        return queryOne("FROM NganhTohop n WHERE n.tbKeys = :tbKeys",
                q -> q.setParameter("tbKeys", tbKeys));
    }

    /**
     * Kiểm tra sự tồn tại của ngành-tổ hợp theo tb_keys.
     * @param tbKeys Key duy nhất (manganh_matohop)
     * @return true nếu tồn tại, false nếu không
     */
    public boolean existsByTbKeys(String tbKeys) {
        if (tbKeys == null || tbKeys.trim().isEmpty()) {
            return false;
        }
        NganhTohop result = findByTbKeys(tbKeys);
        return result != null;
    }

    /**
     * Kiểm tra xem ngành-tổ hợp có dữ liệu liên quan không.
     * Kiểm tra trong: xt_nguyenvongxettuyen, xt_diemcongxetuyen, xt_bangquydoi
     * @param nganhTohop Ngành-tổ hợp cần kiểm tra
     * @return Danh sách các bảng có liên quan (để warning cho admin)
     */
    public List<String> checkRelatedData(NganhTohop nganhTohop) {
        List<String> relatedTables = new java.util.ArrayList<>();
        
        try (Session session = openSession()) {
            // Check xt_nguyenvongxettuyen
            Long countNguyen = session.createQuery(
                    "SELECT COUNT(*) FROM NguyenVongXetTuyen nv WHERE nv.manganh = :manganh",
                    Long.class)
                    .setParameter("manganh", nganhTohop.getManganh())
                    .uniqueResult();
            if (countNguyen != null && countNguyen > 0) {
                relatedTables.add("xt_nguyenvongxettuyen");
            }

            // Check xt_diemcongxetuyen
            Long countDiem = session.createQuery(
                    "SELECT COUNT(*) FROM DiemCongXetTuyen dc WHERE dc.manganh = :manganh AND dc.matohop = :matohop",
                    Long.class)
                    .setParameter("manganh", nganhTohop.getManganh())
                    .setParameter("matohop", nganhTohop.getMatohop())
                    .uniqueResult();
            if (countDiem != null && countDiem > 0) {
                relatedTables.add("xt_diemcongxetuyen");
            }

            // Check xt_bangquydoi
            Long countBang = session.createQuery(
                    "SELECT COUNT(*) FROM BangQuydoi bq WHERE bq.dTohop = :matohop",
                    Long.class)
                    .setParameter("matohop", nganhTohop.getMatohop())
                    .uniqueResult();
            if (countBang != null && countBang > 0) {
                relatedTables.add("xt_bangquydoi");
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi kiểm tra dữ liệu liên quan: " + e.getMessage(), e);
        }

        return relatedTables;
    }

    /**
     * Tìm kiếm ngành-tổ hợp theo từ khóa.
     * @param keyword Từ khóa (mã ngành, mã tổ hợp, hoặc tb_keys)
     * @return Danh sách ngành-tổ hợp khớp
     */
    public List<NganhTohop> search(String keyword) {
        String kw = "%" + keyword.toUpperCase() + "%";
        return query(
                "FROM NganhTohop n WHERE UPPER(n.manganh) LIKE :kw OR UPPER(n.matohop) LIKE :kw OR UPPER(n.tbKeys) LIKE :kw",
                q -> q.setParameter("kw", kw));
    }
}
