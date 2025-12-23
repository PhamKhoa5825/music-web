package com.example.music_web.controller;

import com.example.music_web.service.AlbumService;
import com.example.music_web.service.ArtistService;
import com.example.music_web.service.GenreService;
import com.example.music_web.service.SongService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminMusicController {

    @Autowired
    SongService songService;

    @Autowired
    ArtistService artistService;

    @Autowired
    AlbumService albumService;

    @Autowired
    GenreService genreService;

    @GetMapping("/manager")
    public String showManagerPage(
            @RequestParam(defaultValue = "songs") String tab,
            @RequestParam(required = false) String search,
            Model model
    ) {
        // Load tất cả data không phân trang
        model.addAttribute("songs", songService.getAllSongs(search, null, null, null,
                org.springframework.data.domain.PageRequest.of(0, 1000)).getContent());

        model.addAttribute("artists", artistService.getAllArtists(search,
                org.springframework.data.domain.PageRequest.of(0, 1000)).getContent());

        model.addAttribute("albums", albumService.getAllAlbums(search,
                org.springframework.data.domain.PageRequest.of(0, 1000)).getContent());

        model.addAttribute("genres", genreService.getAllGenres(search,
                org.springframework.data.domain.PageRequest.of(0, 1000)).getContent());

        model.addAttribute("currentTab", tab);
        model.addAttribute("search", search);
        model.addAttribute("activeTab", "manager");

        return "admin/manager";
    }
}