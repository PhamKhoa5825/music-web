package com.example.music_web.controller;

import com.example.music_web.Entity.Comment;
import com.example.music_web.Entity.Playlist;
import com.example.music_web.Entity.Song;
import com.example.music_web.dto.request.CreateSongRequest;
import com.example.music_web.dto.request.UpdateSongRequest;
import com.example.music_web.dto.response.SongResponse;
import com.example.music_web.repository.AlbumRepository;
import com.example.music_web.repository.ArtistRepository;
import com.example.music_web.repository.GenreRepository;
import com.example.music_web.repository.SongRepository;
import com.example.music_web.service.CommentService;
import com.example.music_web.service.InteractionService;
import com.example.music_web.service.PlaylistService;
import com.example.music_web.service.SongService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/songs")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SongController {

    @Autowired
    SongService songService;
    @Autowired
    ArtistRepository artistRepo;
    @Autowired
    AlbumRepository albumRepo;
    @Autowired
    GenreRepository genreRepo;

    @Autowired private CommentService commentService;
    @Autowired private InteractionService interactionService;
    @Autowired private PlaylistService playlistService;
    @Autowired SongRepository songRepo;

    @GetMapping
    public String getAllSongs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long artistId,
            @RequestParam(required = false) Long albumId,
            @RequestParam(required = false) Long genreId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("uploadDate"),
                                                                Sort.Order.desc("songId")));
        // 1. Lấy danh sách bài hát đã lọc
        model.addAttribute("songs", songService.getAllSongs(title, artistId, albumId, genreId, pageable));

        // 2. --- BỔ SUNG: Gửi danh sách Artist, Album, Genre sang View để làm Dropdown ---
        model.addAttribute("artists", artistRepo.findAll());
        model.addAttribute("albums", albumRepo.findAll());
        model.addAttribute("genres", genreRepo.findAll());

        // 3. Giữ lại giá trị đã chọn để hiển thị lại trên giao diện (giữ trạng thái selected)
        model.addAttribute("title", title);
        model.addAttribute("selectedArtistId", artistId); // Đổi tên biến chút cho rõ nghĩa ở View
        model.addAttribute("selectedAlbumId", albumId);
        model.addAttribute("selectedGenreId", genreId);

        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("activeTab", "songs");
        return "songs/list";
    }

    @GetMapping("/{songId}")
    public String getSongById(@PathVariable Long songId, Model model) {
        Long userId = 1L; // User giả định

        // 1. Thông tin bài hát chính (Service đã trả về DTO SongResponse - Tốt!)
        SongResponse song = songService.getSongById(songId);
        model.addAttribute("song", song);

        // 2. Trạng thái Like
        boolean isLiked = interactionService.isLiked(userId, songId);
        model.addAttribute("isLiked", isLiked);

        // 3. Danh sách Bình luận
        List<Comment> comments = commentService.getComments(songId, "newest");
        model.addAttribute("comments", comments);

        // 4. Danh sách Playlist của User
        List<Playlist> myPlaylists = playlistService.getUserPlaylists(userId);
        model.addAttribute("myPlaylists", myPlaylists);

        // 5. Gợi ý bài hát (QUAN TRỌNG: Phải chuyển Entity sang DTO để tránh lỗi JSON/Hibernate)
        List<SongResponse> relatedSongs = songRepo.findAll().stream()
                .filter(s -> !s.getSongId().equals(songId)) // Trừ bài đang nghe
                .limit(5)
                .map(this::convertToDTO) // <-- Gọi hàm chuyển đổi ở dưới
                .collect(Collectors.toList());

        model.addAttribute("relatedSongs", relatedSongs);
        model.addAttribute("activeTab", "songs");

        return "songs/detail";
    }

    // Hàm phụ trợ để chuyển đổi Entity sang DTO (Viết ngay trong Controller hoặc chuyển vào Service)
    private SongResponse convertToDTO(Song entity) {
        SongResponse dto = new SongResponse();
        dto.setSongId(entity.getSongId());
        dto.setTitle(entity.getTitle());
        dto.setMusicUrl(entity.getFilePath());
        dto.setImageUrl(entity.getCoverImage());
        dto.setViews(entity.getViews());

        // Xử lý Artist (Tránh lỗi null)
        if (entity.getArtist() != null) {
            dto.setArtistName(entity.getArtist().getName());
            // Nếu DTO có object ArtistResponse thì map thêm vào đây
        }

        return dto;
    }

    @PostMapping("/{songId}/delete")
    public String deleteSong(
            @PathVariable Long songId,
            RedirectAttributes redirectAttributes
    ) {
        songService.deleteSong(songId);
        redirectAttributes.addFlashAttribute("successMessage", "Song deleted successfully!");
        return "redirect:/admin/manager?tab=songs";
    }

    @PostMapping("/{songId}/increment-view")
    @ResponseBody
    public void incrementView(@PathVariable Long songId) {
        songService.incrementView(songId);
    }


    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        model.addAttribute("songRequest", new CreateSongRequest());
        model.addAttribute("song", new SongResponse());
        model.addAttribute("artists", artistRepo.findAll());
        model.addAttribute("albums", albumRepo.findAll());
        model.addAttribute("genres", genreRepo.findAll());
        model.addAttribute("isEdit", false); // Đánh dấu là trang upload mới
        return "songs/upload";
    }

    @PostMapping("/upload")
    public String uploadSong(
            @ModelAttribute("songRequest") CreateSongRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (request.getFilePath() == null || request.getFilePath().isEmpty()) {
            bindingResult.rejectValue("filePath", "error.filePath", "File audio is required!");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("artists", artistRepo.findAll());
            model.addAttribute("albums", albumRepo.findAll());
            model.addAttribute("genres", genreRepo.findAll());
            model.addAttribute("isEdit", false);
            model.addAttribute("song", new SongResponse());
            return "songs/upload";
        }

        try {
            SongResponse createdSong = songService.createNewSong(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Song uploaded successfully! '" + createdSong.getTitle() + "'");
            return "redirect:/songs/" + createdSong.getSongId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to upload song: " + e.getMessage());
            model.addAttribute("artists", artistRepo.findAll());
            model.addAttribute("albums", albumRepo.findAll());
            model.addAttribute("genres", genreRepo.findAll());
            model.addAttribute("isEdit", false);
            return "songs/upload";
        }
    }

    @GetMapping("/{songId}/edit")
    public String showEditForm(@PathVariable Long songId, Model model) {
        try {
            SongResponse song = songService.getSongById(songId);

            // Chuyển đổi SongResponse sang UpdateSongRequest
            UpdateSongRequest updateRequest = new UpdateSongRequest();
            updateRequest.setTitle(song.getTitle());
            updateRequest.setArtistId(song.getArtistId());
            updateRequest.setAlbumId(song.getAlbumId());
            updateRequest.setLyric(song.getLyric());

            // Lấy danh sách genreId từ database
            List<Long> genreIds = songService.getGenreIdsBySongId(songId);
            updateRequest.setGenreId(genreIds);

            model.addAttribute("songRequest", updateRequest);
            model.addAttribute("song", song); // Để hiển thị thông tin cũ
            model.addAttribute("songId", songId);
            model.addAttribute("artists", artistRepo.findAll());
            model.addAttribute("albums", albumRepo.findAll());
            model.addAttribute("genres", genreRepo.findAll());
            model.addAttribute("isEdit", true); // Đánh dấu là trang edit

            return "songs/upload"; // Sử dụng cùng template

        } catch (Exception e) {
            return "redirect:/songs?error=" + e.getMessage();
        }
    }

    @PostMapping("/{songId}/edit")
    public String updateSong(
            @PathVariable Long songId,
            @ModelAttribute("songRequest") UpdateSongRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (request.getFilePath() == null || request.getFilePath().isEmpty()) {
            bindingResult.rejectValue("filePath", "error.filePath", "File audio is required!");
        }

        if (bindingResult.hasErrors()) {
            SongResponse song = songService.getSongById(songId);
            model.addAttribute("song", song);
            model.addAttribute("songId", songId);
            model.addAttribute("artists", artistRepo.findAll());
            model.addAttribute("albums", albumRepo.findAll());
            model.addAttribute("genres", genreRepo.findAll());
            model.addAttribute("isEdit", true);
            return "songs/upload";
        }

        try {
            SongResponse updatedSong = songService.updateSong(request, songId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Song updated successfully! '" + updatedSong.getTitle() + "'");
            return "redirect:/songs/" + songId;
        } catch (Exception e) {
            SongResponse song = songService.getSongById(songId);
            model.addAttribute("errorMessage", "Failed to update song: " + e.getMessage());
            model.addAttribute("song", song);
            model.addAttribute("songId", songId);
            model.addAttribute("artists", artistRepo.findAll());
            model.addAttribute("albums", albumRepo.findAll());
            model.addAttribute("genres", genreRepo.findAll());
            model.addAttribute("isEdit", true);
            return "songs/upload";
        }
    }
}