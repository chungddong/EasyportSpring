package com.mnu.easyport;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class SiteUser {

    @Id @Column(length = 50)
    private String email;
    private String name;
    private String passwd;
    private String id;

    public String getEmail( ) { return email; }

    public void setEmail(String e) { email = e;}

    public String getName() { return name; }

    public void setName(String n) { name = n; }

    public String getPasswd() { return passwd; }

    public void setPasswd(String p) { passwd = p; }

    public String getId() { return id; }

    public void setId(String p) { id = p; } 


}
