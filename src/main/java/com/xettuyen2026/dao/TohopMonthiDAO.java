package com.xettuyen2026.dao;

import com.xettuyen2026.dao.base.BaseDAO;
import com.xettuyen2026.entity.TohopMonthi;
import org.hibernate.Session;

/**
 * DAO cho bảng xt_tohop_monthi.
 * Hỗ trợ CRUD + kiểm tra xem mã tổ hợp có được sử dụng không.
 * 
 * @author Senior Developer
 */
public class TohopMonthiDAO extends BaseDAO<TohopMonthi> {

    public TohopMonthiDAO() {
        super(TohopMonthi.class);
    }

    /**
     * Tìm tổ hợp môn theo mã.
     * @param maTohop Mã tổ hợp
     * @return Đối tượng TohopMonthi hoặc null nếu không tìm thấy
     */
    public TohopMonthi findByMaTohop(String maTohop) {
        return queryOne("FROM TohopMonthi t WHERE t.matohop = :ma",
                q -> q.setParameter("ma", maTohop));
    }

    /**
     * Kiểm tra mã tổ hợp có tồn tại không.
     * @param maTohop Mã tổ hợp
     * @return true nếu tồn tại, false nếu không
     */
    public boolean existsByMaTohop(String maTohop) {
        if (maTohop == null || maTohop.trim().isEmpty()) {
            return false;
        }
        TohopMonthi result = findByMaTohop(maTohop);
        return result != null;
    }

    /**
     * Kiểm tra mã tổ hợp có được sử dụng trong bảng xt_nganh_tohop không.
     * Nếu có thì không được xóa/sửa đơn phương.
     * @param maTohop Mã tổ hợp
     * @return true nếu đang được sử dụng, false nếu không
     */
    public boolean isUsedInNganhTohop(String maTohop) {
        try (Session session = openSession()) {
            Long count = session.createQuery(
                    "SELECT COUNT(*) FROM NganhTohop nt WHERE nt.matohop = :maTohop",
                    Long.class)
                    .setParameter("maTohop", maTohop)
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi kiểm tra sử dụng tổ hợp: " + e.getMessage(), e);
        }
    }

    /**
     * Tìm kiếm tổ hợp môn theo từ khóa (mã hoặc tên).
     * @param keyword Từ khóa tìm kiếm
     * @return Danh sách tổ hợp môn khớp
     */
    public java.util.List<TohopMonthi> search(String keyword) {
        String kw = "%" + keyword.toLowerCase() + "%";
        return query(
                "FROM TohopMonthi t WHERE LOWER(t.matohop) LIKE :kw OR LOWER(t.tentohop) LIKE :kw",
                q -> q.setParameter("kw", kw));
    }
}
