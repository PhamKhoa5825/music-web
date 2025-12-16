package com.example.music_web.Service;

import com.example.music_web.DTO.PlaylistRequest;
import com.example.music_web.Entity.*;
import com.example.music_web.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PlaylistService {

    @Autowired private PlaylistRepository playlistRepository;
    @Autowired private PlaylistSongRepository playlistSongRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SongRepository songRepository;

    public Playlist getPlaylistById(Long playlistId) {
        return playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found with id " + playlistId));
    }

    // 1. Tạo Playlist
    public Playlist createPlaylist(PlaylistRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Playlist playlist = new Playlist();
        playlist.setName(request.getName());
        playlist.setUser(user);
        playlist.setDescription("Danh sách phát của " + user.getUsername());

        // Set chế độ công khai/riêng tư (Mặc định là true nếu null)
        playlist.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : true);

        return playlistRepository.save(playlist);
    }

    // 2. Lấy danh sách Playlist của User
    public List<Playlist> getUserPlaylists(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return playlistRepository.findByUser(user);
    }

    // 3. Cập nhật thông tin Playlist (Tên, Mô tả) - Dùng cho tính năng Edit In-Place
    public Playlist updatePlaylistInfo(Long playlistId, String name, String description, Boolean isPublic) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        if(name != null && !name.isEmpty()) playlist.setName(name);
        if(description != null) playlist.setDescription(description);
        if(isPublic != null) playlist.setIsPublic(isPublic); // Cập nhật trạng thái

        return playlistRepository.save(playlist);
    }

    // 4. Thêm bài hát (Có Check trùng)
    @Transactional
    public void addSongToPlaylist(Long playlistId, Long songId) {
        // A. Check trùng trước khi query sâu
        if (playlistSongRepository.existsByPlaylist_PlaylistIdAndSong_SongId(playlistId, songId)) {
            throw new RuntimeException("Bài hát này đã có trong Playlist rồi!");
        }

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        PlaylistSongId id = new PlaylistSongId(playlistId, songId);

        PlaylistSong playlistSong = new PlaylistSong();
        playlistSong.setId(id);
        playlistSong.setPlaylist(playlist);
        playlistSong.setSong(song);
        // Tự động xếp cuối danh sách
        playlistSong.setTrackOrder(playlistSongRepository.countByPlaylist(playlist) + 1);

        playlistSongRepository.save(playlistSong);
    }

    // 5. Xóa bài hát
    @Transactional
    public void removeSongFromPlaylist(Long playlistId, Long songId) {
        PlaylistSong ps = playlistSongRepository.findByPlaylist_PlaylistIdAndSong_SongId(playlistId, songId)
                .orElseThrow(() -> new RuntimeException("Song not in playlist"));
        playlistSongRepository.delete(ps);
        // Note: Thực tế cần logic đánh lại số thứ tự (re-index) nhưng tạm thời bỏ qua để đơn giản.
    }

    // Trong PlaylistService.java
    @Transactional // Nhớ thêm Transactional
    public void updatePlaylistImages(Long playlistId, String coverUrl, String bgUrl) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        if (coverUrl != null) playlist.setImageUrl(coverUrl);
        if (bgUrl != null) playlist.setBackgroundImage(bgUrl);

        playlistRepository.save(playlist);
    }
}