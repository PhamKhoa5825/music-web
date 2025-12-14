
## 1. 🛡️ Backend & Security (Quan trọng)
Đã chuyển đổi cơ chế xác thực từ API (JWT) sang **Session-based (Stateful)** để phù hợp với giao diện Web (Thymeleaf).

* **Cấu hình Spring Security (`SecurityConfig.java`):**
    * Thiết lập cơ chế `Form Login`: Tự động xử lý đăng nhập, logout.
    * **Phân quyền (Authorization):**
        * `Guest`: Truy cập tự do Trang chủ (`/`), Đăng ký, Đăng nhập.
        * `User`: Truy cập Profile, Danh sách yêu thích.
        * `Admin`: Truy cập Dashboard quản trị (`/admin/**`).
    * **Fix lỗi phân quyền:** Cập nhật tiền tố `ROLE_` trong `User.java` để tương thích với hàm `hasRole()`.
* **Tự động khởi tạo dữ liệu (`DataSeeder.java`):**
    * Tự động tạo tài khoản **Admin** mặc định (`admin`/`123456`) khi chạy ứng dụng lần đầu.
* **Xử lý Đăng nhập (`LoginSuccessHandler.java`):**
    * Bắt sự kiện đăng nhập thành công để ghi Log và điều hướng người dùng.

## 2. 🗄️ Database & Entity
Cập nhật các Entity cốt lõi để phục vụ logic nghiệp vụ:

* **User Entity:**
    * Implement `UserDetails` của Spring Security.
    * Chuyển `Role` sang sử dụng **Enum** (`ROLE_USER`, `ROLE_ADMIN`) thay vì String thuần.
* **System Log:**
    * Xây dựng `SystemLogService` để tự động ghi lại các hành động quan trọng: `LOGIN`, `REGISTER`, `UPDATE_PROFILE`.
    * Tạo Repository truy vấn Log mới nhất cho Admin xem.

## 3. 🎨 Frontend & UI (Giao diện Thymeleaf)
Xây dựng giao diện theo phong cách **Cyberpunk / Neon Dark Mode** (giống NCT/Spotify).

* **Trang chủ (`index.html` & `HomeController`):**
    * **Guest Mode:** Cho phép nghe nhạc, xem bảng xếp hạng mà **không cần đăng nhập**.
    * **Sidebar Navigation:** Menu điều hướng cố định bên trái.
    * **Logic hiển thị:** Nút Đăng nhập/Đăng ký tự động chuyển thành Avatar/Tên người dùng khi đã Login.
* **Trang Đăng nhập (`login.html`):**
    * Thiết kế dạng **Modal Popup** (Cửa sổ nổi) trên nền mờ.
    * Tích hợp thông báo lỗi khi sai tài khoản/mật khẩu.
* **Trang Profile (`profile.html`):**
    * Hiển thị thông tin người dùng và form cập nhật sở thích (User Preference).
    * **Dynamic Button:** Nút truy cập **Admin Dashboard** chỉ hiện ra nếu tài khoản là Admin.
* **Trang Quản trị (`admin.html` & `AdminController`):**
    * Hiển thị danh sách Người dùng (User Management).
    * Hiển thị Nhật ký hệ thống (System Logs) theo thời gian thực.

## 4. ⚙️ Các Controller đã triển khai
* `AuthController`: Xử lý đăng ký thành viên mới, validate trùng username.
* `UserController`: Quản lý thông tin cá nhân, cập nhật sở thích cho AI.
* `AdminController`: Cung cấp số liệu cho trang Dashboard.
* `HomeController`: Điều phối giao diện trang chủ công khai.

---
**Tình trạng hiện tại:** Module User/Security đã hoàn thiện, giao diện đã tích hợp xong. Hệ thống đã sẵn sàng để tích hợp với các module Bài hát/Playlist của các thành viên khác.