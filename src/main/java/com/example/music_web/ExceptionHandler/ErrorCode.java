package com.example.music_web.ExceptionHandler;


public enum ErrorCode {
    SONG_NOT_EXISTED(1001, "Song not existed !!!"),
    ARTIST_NOT_EXISTED(1002, "Artist not existed !!!"),
    ALBUM_NOT_EXISTED(1003, "Album not existed !!!"),
    GENRE_NOT_EXISTED(1004, "Genre not extsted !!!"),
    ARTIST_HAS_SONGS(1009, "Cannot delete artist because they have songs !!!"),
    ARTIST_HAS_ALBUMS(1010, "Cannot delete artist because they have albums !!!"),
    ALBUM_HAS_SONGS(1012, "Cannot delete album because it has songs"),
    GENRE_HAS_SONGS(1014, "Cannot delete genre because it's used in songs"),
    FILE_EMPTY(1015, "File is empty"),
    FILE_TOO_LARGE(1016, "File size exceeds limit"),
    INVALID_AUDIO_FORMAT(1017, "Invalid audio format. Accepted: mp3, wav, m4a, flac"),
    INVALID_IMAGE_FORMAT(1018, "Invalid image format. Accepted: jpg, jpeg, png, webp"),
    UPLOAD_FAILED(1019, "Failed to upload file to cloud storage");




    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
