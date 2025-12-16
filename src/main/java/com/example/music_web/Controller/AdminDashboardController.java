package com.example.music_web.Controller;

import com.example.music_web.Entity.*;
import com.example.music_web.Repository.*;
import com.example.music_web.Service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.music_web.Repository.GenreRepository;
import com.example.music_web.Entity.ReportStatus;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired private ListeningHistoryRepository historyRepo;
    @Autowired private SongRepository songRepo;
    @Autowired private ReportRepository reportRepo;
    @Autowired private CommentRepository commentRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private GenreRepository genreRepo;
    @Autowired private SongService songService;
    @Autowired private ArtistRepository artistRepository;
    @Autowired private AlbumRepository albumRepository;
    @Autowired private GenreRepository genreRepository;

    // --- CÁC TRANG VIEW HTML ---
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Thống kê cơ bản
        long totalSongs = songRepo.count();
        long totalPlays = historyRepo.count();
        long pendingReports = reportRepo.countByStatus(ReportStatus.PENDING); // Cần thêm method này trong Repo

        model.addAttribute("totalSongs", totalSongs);
        model.addAttribute("totalPlays", totalPlays);
        model.addAttribute("pendingReports", pendingReports);
        model.addAttribute("songs", songRepo.findAll());

        return "admin-dashboard";
    }

    @GetMapping("/reports")
    public String viewReports(
            Model model,
            @RequestParam(required = false) String status, // "PENDING", "RESOLVED", "DISMISSED", "ALL"
            @RequestParam(defaultValue = "newest") String sort // "newest", "oldest"
    ) {
        // 1. XỬ LÝ LỌC DANH SÁCH
        ReportStatus statusEnum = null;
        if (status != null && !status.equals("ALL")) {
            try { statusEnum = ReportStatus.valueOf(status); } catch (Exception e) {}
        }

        List<Report> reports;
        if ("oldest".equals(sort)) {
            reports = reportRepo.findAllByStatusOldest(statusEnum);
        } else {
            reports = reportRepo.findAllByStatusNewest(statusEnum);
        }

        model.addAttribute("reports", reports);
        model.addAttribute("currentStatus", status != null ? status : "ALL");
        model.addAttribute("currentSort", sort);

        // 2. TÍNH TOÁN THỐNG KÊ (Cho các thẻ bài trên cùng)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();
        LocalDateTime startOfYear = now.with(TemporalAdjusters.firstDayOfYear()).toLocalDate().atStartOfDay();

        model.addAttribute("statPending", reportRepo.countByStatus(ReportStatus.PENDING));
        model.addAttribute("statToday", reportRepo.countByReportedAtBetween(startOfDay, now));
        model.addAttribute("statMonth", reportRepo.countByReportedAtBetween(startOfMonth, now));
        model.addAttribute("statYear", reportRepo.countByReportedAtBetween(startOfYear, now));

        return "admin-reports";
    }

    @GetMapping("/songs")
    public String viewSongs(
            Model model,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LocalDate date
    ) {
        // Gọi hàm tìm kiếm vừa viết ở Repo
        List<Song> songs = songRepo.searchSongs(keyword, date);

        model.addAttribute("songs", songs);
        model.addAttribute("keyword", keyword);
        model.addAttribute("date", date);

        return "admin-songs";
    }

    // API ẨN / HIỆN BÀI HÁT (Đã Refactor)
    @PostMapping("/songs/{id}/toggle-hide")
    @ResponseBody
    public ResponseEntity<String> toggleHideSong(@PathVariable Long id) {
        try {
            songService.toggleSongVisibility(id);
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // --- [MỚI] 1. Hiển thị trang Edit Full Page ---
    @GetMapping("/songs/edit/{id}")
    public String showEditPage(@PathVariable Long id, Model model) {
        // Lấy bài hát hiện tại
        Song song = songService.getSongById(id);
        model.addAttribute("song", song);

        // Lấy danh sách dữ liệu cho Dropdown/Checkbox
        model.addAttribute("listArtists", artistRepository.findAll());
        model.addAttribute("listAlbums", albumRepository.findAll());
        model.addAttribute("listGenres", genreRepository.findAll());

        return "admin-song-edit"; // Trả về file HTML mới tạo ở Bước 1
    }

    // --- [MỚI] 2. Xử lý Form Update (POST) ---
    @PostMapping("/songs/update")
    public String updateSongFromForm(@ModelAttribute("song") Song songRequest) {
        // Gọi hàm update full (bạn cần đảm bảo đã thêm hàm updateSongFull bên Service như hướng dẫn trước)
        songService.updateSongFull(songRequest);

        return "redirect:/admin/songs"; // Lưu xong quay về danh sách
    }

    // --- API XÓA ---
    @DeleteMapping("/songs/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteSong(@PathVariable Long id) {
        songService.deleteSong(id);
        return ResponseEntity.ok("Deleted successfully");
    }



    // --- API DỮ LIỆU BIỂU ĐỒ (Chart.js gọi vào đây) ---
    // --- 1. API Lấy danh sách Thể loại (Cho Dropdown) ---
    @GetMapping("/api/genres")
    @ResponseBody
    public ResponseEntity<List<Genre>> getAllGenres() {
        return ResponseEntity.ok(genreRepo.findAll());
    }

    // --- 2. API Thống kê nâng cao ---
    @GetMapping("/api/stats/advanced")
    @ResponseBody
    public ResponseEntity<?> getAdvancedStats(
            @RequestParam String mode,         // "HOUR", "DAY", "MONTH"
            @RequestParam(required = false) Long genreId,
            @RequestParam int year,
            @RequestParam(required = false, defaultValue = "1") int month
    ) {
        List<Object[]> data;

        switch (mode) {
            case "HOUR":
                // Thống kê theo giờ trong năm nay (hoặc logic tùy bạn chọn phạm vi)
                data = historyRepo.getStatsByHourOfDay(genreId, year);
                break;
            case "DAY":
                // Thống kê từng ngày trong tháng được chọn
                data = historyRepo.getStatsByDayOfMonth(genreId, year, month);
                break;
            case "MONTH":
                // Thống kê từng tháng trong năm được chọn
                data = historyRepo.getStatsByMonthOfYear(genreId, year);
                break;
            default:
                return ResponseEntity.badRequest().body("Invalid Mode");
        }
        return ResponseEntity.ok(data);
    }

    // --- API CHI TIẾT (Mới) ---
    @GetMapping("/api/stats/detail")
    @ResponseBody
    public ResponseEntity<?> getDetailStats(
            @RequestParam String type,  // "DATE", "MONTH", "YEAR"
            @RequestParam String value  // "2023-12-25" hoặc "2023-12" hoặc "2023"
    ) {
        long totalViews = 0;
        List<Object[]> topSongs = null;

        try {
            if ("DATE".equals(type)) {
                LocalDate date = LocalDate.parse(value);
                totalViews = historyRepo.countByDate(date);
                topSongs = historyRepo.findTopSongsByDate(date);
            }
            else if ("MONTH".equals(type)) {
                // value dạng "2023-12"
                String[] parts = value.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                totalViews = historyRepo.countByMonth(year, month);
                topSongs = historyRepo.findTopSongsByMonth(year, month);
            }
            else if ("YEAR".equals(type)) {
                int year = Integer.parseInt(value);
                totalViews = historyRepo.countByYear(year);
                topSongs = historyRepo.findTopSongsByYear(year);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi định dạng dữ liệu");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalViews", totalViews);
        response.put("songs", topSongs);

        return ResponseEntity.ok(response);
    }


    // --- API XỬ LÝ BÁO CÁO ---
    @PostMapping("/reports/{id}/resolve")
    @ResponseBody
    public ResponseEntity<String> resolveReport(@PathVariable Long id, @RequestParam String action) {
        Report report = reportRepo.findById(id).orElseThrow();

        if (action.equals("DELETE_CONTENT")) {
            // Xóa nội dung bị báo cáo
            if (report.getSong() != null) {
                // Nếu là bài hát -> Có thể xóa hoặc ẩn (Ở đây demo xóa)
                // songRepo.delete(report.getSong());
                // Tốt hơn là set status = HIDDEN (cần thêm field status cho song)
            }
            // Cần bổ sung field Comment vào Report entity nếu muốn report comment
            // if (report.getComment() != null) commentRepo.delete(report.getComment());
        }

        if (action.equals("WARN_USER")) {
            // Gửi thông báo (Mock logic)
            System.out.println("Gửi email cảnh báo tới user: " + report.getSong().getArtist().getName());
        }

        report.setStatus(ReportStatus.RESOLVED); // Đánh dấu đã xử lý
        reportRepo.save(report);
        return ResponseEntity.ok("Đã xử lý: " + action);
    }
}