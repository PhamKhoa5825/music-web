package com.example.music_web.controller;

import com.example.music_web.dto.request.CreateArtistRequest;
import com.example.music_web.dto.request.UpdateArtistRequest;
import com.example.music_web.dto.response.ArtistResponse;
import com.example.music_web.exception.AppException;
import com.example.music_web.service.ArtistService;
import com.example.music_web.service.SongService;
import jakarta.validation.Valid;
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

@Controller
@RequestMapping("/artists")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArtistController {

    @Autowired
    ArtistService artistService;
    @Autowired
    SongService songService;

    @GetMapping
    public String getAllArtists(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("name")));
        model.addAttribute("artists", artistService.getAllArtists(name, pageable));
        model.addAttribute("name", name);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "redirect:/admin/manager?tab=artists";
    }

    @GetMapping("/{artistId}")
    public String getArtistById(
            @PathVariable Long artistId,
            @RequestParam(defaultValue = "0") int page, // Thêm tham số phân trang
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        // 1. Lấy thông tin Artist (như cũ)
        model.addAttribute("artist", artistService.getArtistById(artistId));

        // 2. Lấy danh sách bài hát của Artist này (Cần thêm logic này)
        // Lưu ý: Bạn cần đảm bảo SongService có hàm findSongsByArtistId trả về Page<SongDTO>
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("uploadDate"))); // Hoặc sort theo tên
        model.addAttribute("songs", songService.getSongsByArtistId(artistId, pageable));

        // Gửi thêm thông tin phân trang để HTML dùng (nếu cần)
        model.addAttribute("pageSize", size);

        return "artists/detail";
    }

    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        model.addAttribute("artistRequest", new CreateArtistRequest());
        model.addAttribute("artist", new ArtistResponse());
        model.addAttribute("isEdit", false);
        return "artists/upload";
    }

    @PostMapping("/upload")
    public String uploadArtist(
            @Valid @ModelAttribute("artistRequest") CreateArtistRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            @RequestParam(required = false) String returnTab,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Field cannot empty!!!");
            model.addAttribute("isEdit", false);
            return "artists/upload";
        }

        try {
            artistService.createArtist(request);
            redirectAttributes.addFlashAttribute("successMessage", "Artist created successfully!");

            // Nếu có returnTab thì quay về admin manager với tab đó
            if (returnTab != null) {
                return "redirect:/admin/manager?tab=" + returnTab;
            }
            return "redirect:/artists";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to create artist: " + e.getMessage());
            model.addAttribute("isEdit", false);
            return "artists/upload";
        }
    }

    // ===== EDIT (SỬ DỤNG CÙNG TRANG UPLOAD) =====
    @GetMapping("/{artistId}/edit")
    public String showEditForm(
            @PathVariable Long artistId,
            @RequestParam(required = false) String returnTab,
            Model model
    ) {
        try {
            ArtistResponse artist = artistService.getArtistById(artistId);

            UpdateArtistRequest updateRequest = new UpdateArtistRequest();
            updateRequest.setName(artist.getName());
            updateRequest.setDescription(artist.getDescription());

            model.addAttribute("artistRequest", updateRequest);
            model.addAttribute("artist", artist);
            model.addAttribute("artistId", artistId);
            model.addAttribute("isEdit", true);
            model.addAttribute("returnTab", returnTab);

            return "artists/upload";
        } catch (Exception e) {
            return "redirect:/artists?error=" + e.getMessage();
        }
    }

    @PostMapping("/{artistId}/edit")
    public String updateArtist(
            @PathVariable Long artistId,
            @Valid @ModelAttribute("artistRequest") UpdateArtistRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            @RequestParam(required = false) String returnTab,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Field cannot empty!!!");
            ArtistResponse artist = artistService.getArtistById(artistId);
            model.addAttribute("artist", artist);
            model.addAttribute("artistId", artistId);
            model.addAttribute("isEdit", true);
            model.addAttribute("returnTab", returnTab);
            return "artists/upload";
        }

        try {
            artistService.updateArtist(artistId, request);
            redirectAttributes.addFlashAttribute("successMessage", "Artist updated successfully!");

            // Nếu có returnTab thì quay về admin manager với tab đó
            if (returnTab != null) {
                return "redirect:/admin/manager?tab=" + returnTab;
            }
            return "redirect:/artists/" + artistId;
        } catch (Exception e) {
            ArtistResponse artist = artistService.getArtistById(artistId);
            model.addAttribute("errorMessage", "Failed to update artist: " + e.getMessage());
            model.addAttribute("artist", artist);
            model.addAttribute("artistId", artistId);
            model.addAttribute("isEdit", true);
            model.addAttribute("returnTab", returnTab);
            return "artists/upload";
        }
    }

    @PostMapping("/{artistId}/delete")
    public String deleteArtist(
            @PathVariable Long artistId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            artistService.deleteArtist(artistId);
            redirectAttributes.addFlashAttribute("successMessage", "Artist deleted successfully!");
        } catch (AppException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/manager?tab=artists";
    }
}