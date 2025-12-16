package com.example.music_web.Service;

import com.example.music_web.Entity.Song;
import com.example.music_web.Repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SongService {

    @Autowired
    private SongRepository songRepository;

    // 1. LẤY CHI TIẾT (Dùng cho chức năng Sửa)
    public Song getSongById(Long id) {
        return songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Song not found"));
    }

    // 2. ẨN / HIỆN BÀI HÁT (Toggle)
    @Transactional
    public void toggleSongVisibility(Long songId) {
        Song song = getSongById(songId);
        boolean currentStatus = (song.getIsHidden() == null) ? false : song.getIsHidden();
        song.setIsHidden(!currentStatus); // Đảo ngược trạng thái
        songRepository.save(song);
    }

    // 3. CẬP NHẬT BÀI HÁT (Edit)
    @Transactional
    public void updateSongFull(Song request) {
        Song existingSong = getSongById(request.getSongId());

        // Cập nhật các trường thông tin
        existingSong.setTitle(request.getTitle());
        existingSong.setCoverImage(request.getCoverImage());
        existingSong.setFilePath(request.getFilePath());
        existingSong.setIsHidden(request.getIsHidden());

        // Cập nhật quan hệ (JPA tự động xử lý ID)
        existingSong.setArtist(request.getArtist());
        existingSong.setAlbum(request.getAlbum());

        // Cập nhật Genres (Many-to-Many)
        // Lưu ý: request.getGenres() sẽ chứa các Genre có ID được bind từ checkbox
        existingSong.setGenres(request.getGenres());

        songRepository.save(existingSong);
    }

    // 4. XÓA BÀI HÁT (Delete Vĩnh Viễn)
    @Transactional
    public void deleteSong(Long id) {
        if (!songRepository.existsById(id)) {
            throw new RuntimeException("Song not found");
        }
        songRepository.deleteById(id);
    }
}