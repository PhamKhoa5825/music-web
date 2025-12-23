package com.example.music_web.repository;

import com.example.music_web.Entity.Report;
import com.example.music_web.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    // Lấy danh sách theo trạng thái (VD: chỉ lấy PENDING để admin xử lý)
    List<Report> findByStatusOrderByReportedAtDesc(ReportStatus status);

    // 1. Đếm theo trạng thái (Cho thẻ thống kê)
    long countByStatus(ReportStatus status);

    // 2. Đếm báo cáo trong khoảng thời gian (Cho thống kê Ngày/Tháng/Năm)
    long countByReportedAtBetween(LocalDateTime start, LocalDateTime end);

    // 3. Hàm lọc & Tìm kiếm tổng hợp (Native Query hoặc JPQL)
    // Nếu status là NULL thì lấy hết, sort thì xử lý ở Controller hoặc Query
    @Query("SELECT r FROM Report r WHERE (:status IS NULL OR r.status = :status) ORDER BY r.reportedAt DESC")
    List<Report> findAllByStatusNewest(@Param("status") ReportStatus status);

    @Query("SELECT r FROM Report r WHERE (:status IS NULL OR r.status = :status) ORDER BY r.reportedAt ASC")
    List<Report> findAllByStatusOldest(@Param("status") ReportStatus status);
}