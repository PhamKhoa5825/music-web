package com.example.music_web.Controller;

import com.example.music_web.DTO.Request.CreateGenreRequest;
import com.example.music_web.DTO.Request.UpdateGenreRequest;
import com.example.music_web.DTO.Response.GenreResponse;
import com.example.music_web.ExceptionHandler.AppException;
import com.example.music_web.Service.GenreService;
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
@RequestMapping("/genres")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenreController {

    @Autowired
    GenreService genreService;
    @Autowired
    SongService songService;

    @GetMapping
    public String getAllGenres(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("name")));
        model.addAttribute("genres", genreService.getAllGenres(name, pageable));
        model.addAttribute("name", name);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "redirect:/admin/manager?tab=genres";
    }

    @GetMapping("/{genreId}")
    public String getGenreById(@PathVariable Long genreId,
                               @RequestParam(defaultValue = "0") int page, // Thêm tham số phân trang
                               @RequestParam(defaultValue = "10") int size,
                               Model model
    ) {

        model.addAttribute("genre", genreService.getGenreById(genreId));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("uploadDate")));
        model.addAttribute("songs", songService.getSongsByGenreId(genreId, pageable));


        model.addAttribute("pageSize", size);

        return "genres/detail";
    }

    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        model.addAttribute("genreRequest", new CreateGenreRequest());
        model.addAttribute("genre", new GenreResponse());
        model.addAttribute("isEdit", false);
        return "genres/upload";
    }

    @PostMapping("/upload")
    public String uploadGenre(
            @Valid @ModelAttribute("genreRequest") CreateGenreRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            @RequestParam(required = false) String returnTab,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Field cannot empty!!!");
            model.addAttribute("isEdit", false);
            return "genres/upload";
        }


        try {
            genreService.createGenre(request);
            redirectAttributes.addFlashAttribute("successMessage", "Genre created successfully!");

            if (returnTab != null) {
                return "redirect:/admin/manager?tab=" + returnTab;
            }
            return "redirect:/genres";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to create genre: " + e.getMessage());
            model.addAttribute("isEdit", false);
            return "genres/upload";
        }
    }

    // ===== EDIT =====
    @GetMapping("/{genreId}/edit")
    public String showEditForm(
            @PathVariable Long genreId,
            @RequestParam(required = false) String returnTab,
            Model model
    ) {
        try {
            GenreResponse genre = genreService.getGenreById(genreId);

            UpdateGenreRequest updateRequest = new UpdateGenreRequest();
            updateRequest.setName(genre.getName());
            model.addAttribute("genreRequest", updateRequest);
            model.addAttribute("genre", genre);
            model.addAttribute("genreId", genreId);
            model.addAttribute("isEdit", true);
            model.addAttribute("returnTab", returnTab);

            return "genres/upload";
        } catch (Exception e) {
            return "redirect:/genres?error=" + e.getMessage();
        }
    }

    @PostMapping("/{genreId}/edit")
    public String updateGenre(
            @PathVariable Long genreId,
            @Valid @ModelAttribute("genreRequest") UpdateGenreRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            @RequestParam(required = false) String returnTab,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Field cannot empty!!!");
            GenreResponse genre = genreService.getGenreById(genreId);
            model.addAttribute("genre", genre);
            model.addAttribute("genreId", genreId);
            model.addAttribute("isEdit", true);
            model.addAttribute("returnTab", returnTab);
            return "genres/upload";
        }

        try {
            genreService.updateGenre(genreId, request);
            redirectAttributes.addFlashAttribute("successMessage", "Genre updated successfully!");

            if (returnTab != null) {
                return "redirect:/admin/manager?tab=" + returnTab;
            }
            return "redirect:/genres/" + genreId;
        } catch (Exception e) {
            GenreResponse genre = genreService.getGenreById(genreId);
            model.addAttribute("errorMessage", "Failed to update genre: " + e.getMessage());
            model.addAttribute("genre", genre);
            model.addAttribute("genreId", genreId);
            model.addAttribute("isEdit", true);
            model.addAttribute("returnTab", returnTab);
            return "genres/upload";
        }
    }

    @PostMapping("/{genreId}/delete")
    public String deleteGenre(
            @PathVariable Long genreId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            genreService.deleteGenre(genreId);

            redirectAttributes.addFlashAttribute("successMessage", "Delete genre successfully!");

        } catch (AppException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/manager?tab=genres";
    }
}