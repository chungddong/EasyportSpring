package com.mnu.easyport;

import jakarta.persistence.*;

import java.sql.Blob;
import java.time.LocalDateTime;

@Entity
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private Blob image;

    private LocalDateTime uploadDate;

    // 생성자
    public Image() {
        this.uploadDate = LocalDateTime.now();
    }

    // Getter, Setter 메서드
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Blob getImage() {
        return image;
    }
    public void setImage(Blob image) {
        this.image = image;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
}