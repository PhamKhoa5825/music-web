package com.example.music_web.DTO;

public class PlaylistRequest {
    private String name;
    private Long userId; // Tạm thời truyền userId, sau này lấy từ Token
    private Boolean isPublic;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
}
