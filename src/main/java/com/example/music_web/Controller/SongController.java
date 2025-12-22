package com.example.music_web.Controller;

import com.example.music_web.DTO.Request.CreateSongRequest;
import com.example.music_web.DTO.Request.UpdateSongRequest;
import com.example.music_web.DTO.Response.SongResponse;
import com.example.music_web.Repository.AlbumRepo;
import com.example.music_web.Repository.ArtistRepo;
import com.example.music_web.Repository.GenreRepo;
import com.example.music_web.Service.SongService;
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

@Controller
@RequestMapping("/songs")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SongController {

    @Autowired
    SongService songService;
    @Autowired
    ArtistRepo artistRepo;
    @Autowired
    AlbumRepo albumRepo;
    @Autowired
    GenreRepo genreRepo;

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
                                                                Sort.Order.asc("songId")));
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
        return "songs/list";
    }

    @GetMapping("/{songId}")
    public String getSongById(@PathVariable Long songId, Model model) {
        model.addAttribute("song", songService.getSongById(songId));

        return "songs/detail";
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