# Phân tích Cơ sở dữ liệu và Kế hoạch Triển khai Dự án Tuyển sinh

## 1. Đánh giá file SQL (`xettuyen2026_empty.sql`)
Sau khi đối chiếu file SQL với yêu cầu trong `Dự án phần mềm tuyển sinh.txt`, rút ra kết luận: **File SQL CHƯA ĐẦY ĐỦ hoàn toàn.**

**Các điểm thiếu sót và khác biệt cần lưu ý:**
- **Thiếu bảng Quản lý Người dùng (Users/Admins):** Chức năng số 1 yêu cầu "Quản lý người dùng, có 2 loại quyền: user và admin" (xem danh sách, sửa, đổi password, đổi quyền...), nhưng trong SQL không có bảng nào lưu trữ thông tin tài khoản người quản trị hay hệ thống (như `users`, `accounts`, hay `admins`). Cần tạo thêm 1 bảng, ví dụ: `xt_users(id, username, password, role, is_active)`.
- **Sai khác nhỏ về tên bảng:** Trong yêu cầu `.txt` ghi là `xt_nguyenvongxetuyen`, nhưng trong SQL tạo ra là `xt_nguyenvongxettuyen` (dư 1 chữ `t`). Nhớ match Entity trong Code theo Database thực tế.
- **Thiếu cấu hình Khóa ngoại (Foreign Keys):** Các bảng hiện tại được thiết kế độc lập, không có foreign key constraints (VD: `cccd` trong bảng điểm thi hay nguyện vọng đáng lý tham chiếu đến `xt_thisinhxettuyen25`). Điều này giúp dễ import dữ liệu rời rạc, nhưng khi code bằng Hibernate, bạn có thể cần cẩn thận tự xử lý mapping quan hệ (OneToMany, ManyToOne) hoặc không sử dụng ràng buộc khóa ngoại cứng.

---

## 2. Chi tiết Chức năng "Quản lý Nguyện vọng và Xét tuyển"
Đây là Module phức tạp và quan trọng nhất của hệ thống. 

**Nhiệm vụ cần làm:**
1. **Hiển thị & Cập nhật nguyện vọng:** Giao diện cho phép xem danh sách nguyện vọng của tất cả thí sinh, lọc theo mã ngành, CCCD. Hỗ trợ import/sửa đổi/thu hồi nguyện vọng.
2. **Thuật toán Xét tuyển (Core Logic):**
   - Viết chức năng tính **Tổng điểm xét tuyển** cho từng nguyện vọng: Tổng điểm = Tổng điểm 3 môn thi (tra trong bảng điểm thi theo mã tổ hợp và hệ số môn từ bảng Ngành-Tổ hợp, kết hợp quy đổi nếu cần) + Điểm ưu tiên/điểm cộng.
   - Viết thuật toán Duyệt nguyện vọng: Lọc theo từng thí sinh, duyệt nguyện vọng dựa vào thứ tự `nv_tt` (từ 1 đến n). Nếu điểm xét tuyển (`diem_xettuyen`) thỏa mản điểm chuẩn (`n_diemtrungtuyen` của bảng Ngành) và ngành vẫn còn chỉ tiêu (`n_chitieu`), thì thí sinh Đậu nguyện vọng đó (`nv_ketqua` = "yes" hoặc "duolaar"). Các nguyện vọng dưới tự động bị hủy hoặc không xét nữa.

**Liên quan đến những file / bảng (Tables) nào?**
- Bảng chính: `xt_nguyenvongxettuyen` (Lưu thông tin nguyện vọng, điểm xét, kết quả trạng thái đỗ/trượt).
- Báo cáo/Tra cứu lấy từ các bảng: 
  - `xt_thisinhxettuyen25` (Lấy tên, đối tượng, khu vực của thí sinh).
  - `xt_nganh` (Danh sách ngành, điểm chuẩn, chỉ tiêu).
  - `xt_nganh_tohop` (Để biết ngành đó xét bằng tổ hợp gì, môn nào nhân hệ số mấy).
  - `xt_diemthixettuyen` & `xt_diemcongxetuyen` & `xt_bangquydoi` (Làm base data để tính toán `diem_xettuyen` vào bảng nguyện vọng).
- Về mặt Code: Cần tạo các lớp Service như `AdmissionService`, `CalculationService` để thực hiện Logic xét tuyển độc lập khỏi giao diện (Swing Forms).

---

## 3. Những công việc cần chuẩn bị & Lưu ý khi Code, Triển khai

### 3.1 Giai đoạn chuẩn bị (Setup)
- Tạo bảng `users` trong MySQL để xử lý chức năng đăng nhập/phân quyền (Chức năng 1).
- Khởi tạo project Java, thiết lập Maven/Gradle.
- Cấu hình file `hibernate.cfg.xml` kết nối đến database `xettuyen2026`.
- **Generate Entities:** Sử dụng công cụ (như Hibernate/JPA Tools) để map 9 bảng từ Database thành các Class Entity trong Java. (VD: Sinh class `Thisinh.class`, `Nganh.class`, `Nguyenvong.class`,...).

### 3.2 Quá trình Code dự án
- **Áp dụng mô hình MVC (hoặc 3-Layer):** 
  - **Models/Entities:** Chứa objects ánh xạ database.
  - **DAO/Repository:** Các Class thao tác trực tiếp với Database qua Session của Hibernate (VD: `ThisinhDAO.java` chứa các hàm insert, update, paginated findAll).
  - **Services:** Chứa logic nghiệp vụ phức tạp (như hàm xử lý file cấu trúc Excel tải lên, hàm chạy thuật toán lọc nguyện vọng).
  - **Views/Controllers (Swing form):** Chỉ gọi Service để lấy data và Binding lên bảng (JTable, JComboBox). Giúp code dễ bảo trì, dễ sửa.
- **Vấn đề Import File:** Vì hầu hết module đều có Import (Excel, CSV). Cần viết một thư viện đọc Excel chung (`Apache POI`) hỗ trợ parse hàng loạt row vào Entity list thay vì báo lỗi cho từng form.
- **Phân trang:** Entity `Thisinh` có chức năng xem danh sách *(phân trang 20 row/page)*. API Hibernate HQL nên dùng `.setFirstResult(offset)` và `.setMaxResults(20)` để query từng phần thay vì fetch tất cả vào RAM tránh tràn bộ nhớ lúc Data quá nhiều.

### 3.3 Triển khai & Kiểm thử
- **Đầu vào (Data Input):** Cần đảm bảo chuỗi Import chạy mượt được một dataset giả định vài ngàn row, để xem Swing UI có bị đơ không (Nên dùng `SwingWorker` hoặc `Threads` khi gọi hàm import Database).
- **Thuật toán Nguyện vọng:** Test kĩ trường hợp một người đăng ký 10 nguyện vọng, trong đó rớt NV1 nhưng điểm đủ đậu NV2, để xem status cập nhật `nv_ketqua` có chính xác không. Check kĩ hàm cộng điểm xem có bị null hay lỗi kiểu `decimal` không.
- **Đóng gói dự án:** Khi project hoàn thành, biên dịch ứng dụng ra file `*.jar` hoặc `*.exe` bằng Lunch4j và đính kèm hướng dẫn cấu hình connection string SQL để nộp đồ án.
