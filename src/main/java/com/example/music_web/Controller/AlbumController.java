package com.example.music_web.Controller;

import com.example.music_web.DTO.Request.CreateAlbumRequest;
import com.example.music_web.DTO.Request.UpdateAlbumRequest;
import com.example.music_web.DTO.Response.AlbumResponse;
import com.example.music_web.ExceptionHandler.AppException;
import com.example.music_web.Repository.ArtistRepo;
import com.example.music_web.Service.AlbumService;
import com.example.music_web.Service.SongService;
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
@RequestMapping("/albums")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AlbumController {

    @Autowired
    AlbumService albumService;
    @Autowired
    SongService songService;
    @Autowired
    ArtistRepo artistRepo;

    @GetMapping
    public String getAllAlbums(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        model.addAttribute("albums", albumService.getAllAlbums(title, pageable));
        model.addAttribute("title", title);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "redirect:/admin/manager?tab=albums";
    }

    @GetMapping("/{albumId}")
    public String getAlbumById(@PathVariable Long albumId,
                               @RequestParam(defaultValue = "0") int page, // Thêm tham số phân trang
                               @RequestParam(defaultValue = "10") int size,
                               Model model
    ) {

        model.addAttribute("album", albumService.getAlbumById(albumId));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("uploadDate")));
        model.addAttribute("songs", songService.getSongsByAlbumId(albumId, pageable));


        model.addAttribute("pageSize", size);

        return "albums/detail";
    }



    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        model.addAttribute("albumRequest", new CreateAlbumRequest());
        model.addAttribute("album", new AlbumResponse());
        model.addAttribute("artists", artistRepo.findAll());
        model.addAttribute("isEdit", false);
        return "albums/upload";
    }

    @PostMapping("/upload")
    public String uploadAlbum(
            @Valid @ModelAttribute("albumRequest") CreateAlbumRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            @RequestParam(required = false) String returnTab,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Field cannot empty!!!");

            model.addAttribute("artists", artistRepo.findAll());
            model.addAttribute("isEdit", false);
            return "albums/upload";
        }

        try {
            albumService.createAlbum(request);
            redirectAttributes.addFlashAttribute("successMessage", "Album created successfully!");

            if (returnTab != null) {
                return "redirect:/admin/manager?tab=" + returnTab;
            }
            return "redirect:/albums";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to create album: " + e.getMessage());
            model.addAttribute("artists", artistRepo.findAll());
            model.addAttribute("isEdit", false);
            return "albums/upload";
        }
    }

    // ===== EDIT =====
    @GetMapping("/{albumId}/edit")
    public String showEditForm(
            @PathVariable Long albumId,
            @RequestParam(required = false) String returnTab,
            Model model
    ) {
        try {
            AlbumResponse album = albumService.getAlbumById(albumId);

            UpdateAlbumRequest updateRequest = new UpdateAlbumRequest();
            updateRequest.setTitle(album.getTitle());
            updateRequest.setDescription(album.getDescription());
            updateRequest.setReleaseYear(album.getReleaseYear());
            updateRequest.setArtistId(album.getArtistId());

            model.addAttribute("albumRequest", updateRequest);
            model.addAttribute("album", album);
            model.addAttribute("albumId", albumId);
            model.addAttribute("artists", artistRepo.findAll());
            model.addAttribute("isEdit", true);
            model.addAttribute("returnTab", returnTab);

            return "albums/upload";
        } catch (Exception e) {
            return "redirect:/albums?error=" + e.getMessage();
        }
    }

    @PostMapping("/{albumId}/edit")
    public String updateAlbum(
            @PathVariable Long albumId,
            @Valid @ModelAttribute("albumRequest") UpdateAlbumRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            @RequestParam(required = false) String returnTab,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Field cannot empty!!!");

            AlbumResponse album = albumService.getAlbumById(albumId);
            model.addAttribute("album", album);
            model.addAttribute("albumId", albumId);
            model.addAttribute("artists", artistRepo.findAll());
            model.addAttribute("isEdit", true);
            model.addAttribute("returnTab", returnTab);
            return "albums/upload";
        }

        try {
            albumService.updateAlbum(albumId, request);
            redirectAttributes.addFlashAttribute("successMessage", "Album updated successfully!");

            if (returnTab != null) {
                return "redirect:/admin/manager?tab=" + returnTab;
            }
            return "redirect:/albums/" + albumId;
        } catch (Exception e) {
            AlbumResponse album = albumService.getAlbumById(albumId);
            model.addAttribute("errorMessage", "Failed to update album: " + e.getMessage());
            model.addAttribute("album", album);
            model.addAttribute("albumId", albumId);
            model.addAttribute("artists", artistRepo.findAll());
            model.addAttribute("isEdit", true);
            model.addAttribute("returnTab", returnTab);
            return "albums/upload";
        }
    }

    @PostMapping("/{albumId}/delete")
    public String deleteAlbum(
            @PathVariable Long albumId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            albumService.deleteAlbum(albumId);
            redirectAttributes.addFlashAttribute("successMessage", "Album deleted successfully!");
        } catch (AppException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/manager?tab=albums";
    }
}