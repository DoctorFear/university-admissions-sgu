import java.sql.*;

public class TestQuery {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/xettuyen2026?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh";
        try (Connection conn = DriverManager.getConnection(url, "root", "")) {
            System.out.println("Connected.");
            try (Statement st = conn.createStatement()) {
                ResultSet rs = st.executeQuery(
                        "SELECT nn_cccd, nv_manganh, nv_ketqua, status FROM xt_nguyenvongxettuyen WHERE nn_cccd='TS_0003'");
                while (rs.next()) {
                    System.out.println(rs.getString("nn_cccd") + " " + rs.getString("nv_manganh") + " "
                            + rs.getString("nv_ketqua") + " " + rs.getString("status"));
                }
            } catch (SQLException e) {
                System.out.println("Error selecting with status: " + e.getMessage());
                try (Statement st2 = conn.createStatement()) {
                    ResultSet rs2 = st2.executeQuery(
                            "SELECT nn_cccd, nv_manganh, nv_ketqua FROM xt_nguyenvongxettuyen WHERE nn_cccd='TS_0003'");
                    while (rs2.next()) {
                        System.out.println(rs2.getString("nn_cccd") + " " + rs2.getString("nv_manganh") + " "
                                + rs2.getString("nv_ketqua"));
                    }
                }
            }

            System.out.println("All candidates:");
            try (Statement st = conn.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM xt_nguyenvongxettuyen");
                if (rs.next())
                    System.out.println("Total NguyenVong: " + rs.getInt(1));
            }

            try (Statement st = conn.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT cccd FROM thisinh LIMIT 5");
                while (rs.next())
                    System.out.println("Candidate: " + rs.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
