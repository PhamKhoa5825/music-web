package com.example.music_web.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.music_web.dto.response.UploadResponse;
import com.example.music_web.exception.AppException;
import com.example.music_web.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    private static final long MAX_SONG_SIZE = 50 * 1024 * 1024; // 50MB
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB

    public UploadResponse uploadSong(MultipartFile file) {
        if (file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        if (file.getSize() > MAX_SONG_SIZE) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !isValidAudioType(contentType)) {
            throw new AppException(ErrorCode.INVALID_AUDIO_FORMAT);
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "video",
                            "folder", "music_app/songs",
                            "format", "mp3"
                    ));

            return buildUploadResponse(uploadResult, "video");

        } catch (IOException e) {
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }

    public UploadResponse uploadCover(MultipartFile file) {
        if (file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            throw new AppException(ErrorCode.INVALID_IMAGE_FORMAT);
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "folder", "music_app/covers"

                    ));

            return buildUploadResponse(uploadResult, "image");

        } catch (IOException e) {
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }

    private boolean isValidAudioType(String contentType) {
        return contentType.equals("audio/mpeg") ||      // mp3
                contentType.equals("audio/mp3") ||
                contentType.equals("audio/wav") ||
                contentType.equals("audio/x-wav") ||
                contentType.equals("audio/m4a") ||
                contentType.equals("audio/x-m4a") ||
                contentType.equals("audio/flac");
    }


    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/webp");
    }

    /**
     * Build UploadResponse từ Cloudinary result
     */
    private UploadResponse buildUploadResponse(Map uploadResult, String resourceType) {
        UploadResponse response = new UploadResponse();
        response.setUrl((String) uploadResult.get("secure_url"));
        response.setPublicId((String) uploadResult.get("public_id"));
        response.setResourceType(resourceType);

        // Cloudinary trả về bytes dạng Integer hoặc Long
        Object bytes = uploadResult.get("bytes");
        if (bytes instanceof Integer) {
            response.setFileSize(((Integer) bytes).longValue());
        } else if (bytes instanceof Long) {
            response.setFileSize((Long) bytes);
        }

        return response;
    }
}