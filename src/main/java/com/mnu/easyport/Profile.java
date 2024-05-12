package com.mnu.easyport;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profiles")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

//새로운 프로필 정보를 저장하기 위한 엔티티 클래스
public class Profile {
    @Id
    @GeneratedValue
    private Long id;
    private String userid;
    private String introduce;
    private String phone;
    private String email;
    private String imgurl;
    private Boolean canview;
    private String viewkey;

}
