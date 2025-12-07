# 📌 Tổng Quan Vai Trò & Hướng Phát Triển Các Entity Trong Hệ Thống Web Nghe Nhạc

Tài liệu này giúp lập trình viên hiểu rõ **vai trò – mục đích – dữ liệu chính – quan hệ – hướng phát triển** của toàn bộ entity trong hệ thống. Đây là phiên bản mở rộng, chuyên nghiệp hơn, phù hợp dự án lớn hoặc chia nhóm nhiều backend dev.

---

# I. 👥 Entity Cốt Lõi & Quản Lý Người Dùng

## **1. User**

### 🎯 Vai t

* Tài khoản hệ thống
* Đăng nhập / phân quyền / bảo mật
* Gắn với tất cả dữ liệu cá nhân

### 🔑 Dữ liệu chính

* username, password (mã hoá)
* role (USER/ADMIN)
* createdAt

### 🔗 Quan hệ

* One-to-Many với Playlist, ListeningHistory, Favorite, SongRating, SystemLog, Recommendation
* One-to-One với UserPreference

### 🚀 Hướng phát triển

* Tài khoản Premium
* Hồ sơ người dùng (avatar, giới thiệu)
* Cài đặt bảo mật 2FA

---

## **2. UserPreference**

### 🎯 Vai trò

Lưu trữ **sở thích rõ ràng và hành vi học được** để làm dữ liệu cho AI.

### 🔑 Dữ liệu chính

* favoriteGenres (JSON hoặc bảng M-M)
* favoriteArtists
* listeningPattern (hành vi theo thời gian)

### 🔗 Quan hệ

* One-to-One với User

### 🚀 Hướng phát triển

* Gợi ý thông minh "Dành cho bạn"
* Nhận diện xu hướng nghe nhạc theo thời điểm (AI clustering)

---

## **3. SystemLog**

### 🎯 Vai trò

Audit — Lưu lại mọi thay đổi quan trọng trong hệ thống.

### 🔑 Dữ liệu

* action
* description
* time

### 🔗 Quan hệ

* Many-to-One với User

### 🚀 Hướng phát triển

* Trang Admin Log View
* Lọc theo ngày, user, mức độ quan trọng

---

# II. 🎵 Entity Cấu Trúc Nội Dung Nhạc

## **4. Artist**

### 🎯 Vai trò

Ca sĩ/Nhạc sĩ — thành phần quan trọng trong bài hát & album.

### 🔑 Dữ liệu

* name
* description

### 🔗 Quan hệ

* One-to-Many với Song, Album

### 🚀 Hướng phát triển

* Trang profile ca sĩ
* Top ca sĩ nổi bật (qua lượt nghe)

---

## **5. Genre**

### 🎯 Vai trò

Thể loại nhạc

### 🔑 Dữ liệu

* name

### 🔗 Quan hệ

* Many-to-Many với Song

### 🚀 Hướng phát triển

* Explore theo thể loại
* BXH thể loại theo tuần

---

## **6. Album**

### 🎯 Vai trò

Gom nhóm bài hát, tạo bố cục chuyên nghiệp

### 🔑 Dữ liệu

* title
* coverUrl
* releaseYear

### 🔗 Quan hệ

* Many-to-One với Artist
* One-to-Many với Song

### 🚀 Hướng phát triển

* Trang album
* Album trending theo lượt nghe

---

## **7. Song**

### 🎯 Vai trò

**Trung tâm hệ thống** — đại diện nội dung âm nhạc

### 🔑 Dữ liệu

* title
* filePath, coverImage
* views
* audioFeatures (tempo, energy, danceability — dùng AI)
* averageRating

### 🔗 Quan hệ

* Many-to-One với Artist, Album
* Many-to-Many với Genre
* One-to-Many với Favorite, SongRating, ListeningHistory, Recommendation

### 🚀 Hướng phát triển

* Top bài hát ngày/tuần/tháng
* Lọc bài hát theo tempo, mood (AI)

---

# III. 💡 Entity Tương Tác Người Dùng & Phản Hồi

## **8. Favorite**

### 🎯 Vai trò

Like bài hát — phản hồi tường minh

### 🔑 Dữ liệu

* createdAt

### 🔗 Quan hệ

* Many-to-One với User & Song

### 🚀 Hướng phát triển

* Danh sách bài hát yêu thích
* Gợi ý dựa trên like

---

## **9. SongRating**

### 🎯 Vai trò

Đánh giá (1–5 sao) và review bài hát

### 🔑 Dữ liệu

* rating
* review

### 🔗 Quan hệ

* Many-to-One với User & Song

### 🚀 Hướng phát triển

* Xếp hạng bài hát chi tiết
* Bình luận bài hát

---

## **10. ListeningHistory**

### 🎯 Vai trò

Tracking mỗi lần người dùng nghe bài hát (feedback ngầm)

### 🔑 Dữ liệu

* listenedAt

### 🔗 Quan hệ

* Many-to-One với User & Song

### 🚀 Hướng phát triển

* AI phân tích thói quen nghe nhạc
* Trending bài hát 24h
* Heatmap thời gian nghe nhạc

---

# IV. 📜 Playlist & Bảng Xếp Hạng

## **11. Playlist**

### 🎯 Vai trò

Danh sách nhạc do người dùng tạo

### 🔑 Dữ liệu

* name
* createdAt

### 🔗 Quan hệ

* Many-to-One với User
* One-to-Many với PlaylistSong

### 🚀 Hướng phát triển

* Playlist public / private
* Playlist thông minh (AI generate)

---

## **12. PlaylistSong**

### 🎯 Vai trò

Bảng trung gian quản lý thứ tự bài hát

### 🔑 Dữ liệu

* trackOrder
* addedAt

### 🔗 Quan hệ

* Many-to-One với Playlist & Song

### 🚀 Hướng phát triển

* Kéo thả thay đổi thứ tự bài hát

---

## **13. SongRanking**

### 🎯 Vai trò

Lưu BXH theo ngày để tăng tốc độ tải

### 🔑 Dữ liệu

* rankingDate
* rank
* totalViews

### 🔗 Quan hệ

* Many-to-One với Song

### 🚀 Hướng phát triển

* BXH theo thể loại
* BXH theo quốc gia

---

# V. 🤖 Entity Trí Tuệ Nhân Tạo

## **14. Recommendation**

### 🎯 Vai trò

Lưu kết quả gợi ý và đo lường phản hồi

### 🔑 Dữ liệu

* confidenceScore
* reason
* clicked
* liked

### 🔗 Quan hệ

* Many-to-One với User & Song

### 🚀 Hướng phát triển

* Hệ thống gợi ý cá nhân hoá 100%
* Theo dõi chất lượng gợi ý

---

# 💾 Hướng Dẫn Lưu Trữ File (Tiêu Chuẩn Công Nghiệp)

**Không lưu file nhạc hoặc hình ảnh vào database.**

## 📍 Lưu trong DB:

* Chỉ lưu **đường dẫn (filePath, coverImage)**
* Không lưu file binary

## 📁 File được lưu ở đâu?

### **Phương án A — Lưu tại server (phổ biến cho dự án nhỏ)**

```
/uploads/songs/abc.mp3
/uploads/covers/song1.jpg
```

### **Phương án B — Cloud Storage (dự án chuyên nghiệp)**

* AWS S3
* Firebase Storage
* Cloudinary

### **Phương án C — Lưu BLOB trong DB (không khuyến nghị)**

* Nặng, chậm, tốn tài nguyên

---

# 📌 Kết Luận

Bộ entity này được thiết kế để:

* Mở rộng dễ dàng
* Phù hợp cho nhiều backend dev cùng làm
* Hỗ trợ cả tính năng cơ bản lẫn AI nâng cao

Nếu bạn cần thêm:
✔ ERD Diagram visual
✔ API list cho từng entity
✔ Gợi ý chia backend cho nhiều người
→ Hãy nói **"Tạo ERD"** hoặc **"Tạo API"**!




# ⚠️ Hạn Chế & Ghi Chú Quan Trọng

Mặc dù hệ thống entity mở rộng mang tính chuyên nghiệp và hỗ trợ AI/BXH, vẫn tồn tại một số hạn chế cần lưu ý khi phát triển dự án:

❌ 1. Phức tạp hơn đáng kể so với phiên bản cơ bản

Số lượng entity tăng lên khiến project đòi hỏi:

Kiến thức tốt về mô hình quan hệ

Nhiều thời gian để code và test

Nhiều tài nguyên để vận hành

Điều này có thể gây quá tải nếu nhóm nhỏ hoặc kinh nghiệm chưa cao.

❌ 2. Một số entity không cần thiết trong giai đoạn đầu

Ví dụ:

Recommendation

UserPreference

SongRanking

AudioFeatures

SongRating

Những tính năng này có thể để phase 2 hoặc sau khi core hệ thống đã ổn định.

❌ 3. Nguy cơ trùng lắp hoặc dư thừa dữ liệu

Các entity như Favorite, Rating, ListeningHistory đều liên quan phản hồi người dùng → cần thiết kế index và unique constraints cẩn thận.

❌ 4. Chi phí xử lý tăng mạnh

Các bảng lớn như ListeningHistory có thể có hàng triệu bản ghi → cần tối ưu truy vấn và sử dụng index.

❌ 5. Đòi hỏi rõ ràng về workflow phát triển

Không có roadmap rõ ràng → team dễ làm chồng chéo hoặc sai thứ tự ưu tiên.
