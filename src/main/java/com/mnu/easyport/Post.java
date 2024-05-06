package com.mnu.easyport;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "posts")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

//새로운 글 정보를 저장하기 위한 엔티티 클래스
public class Post {
    @Id
    @GeneratedValue
    private Long id;
    private String title;
    private String userid;
    @Lob
    private String content;
}
