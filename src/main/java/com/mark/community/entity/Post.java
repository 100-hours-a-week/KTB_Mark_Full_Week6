package com.mark.community.entity;

import com.mark.community.enums.PostCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Getter
@NoArgsConstructor
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String body;
    private Date postTime;

    private long views;
    private int reports;

    private boolean deleted;
    private boolean blind;
    private boolean edited;
    private boolean temp = true;

    @Enumerated(EnumType.STRING)
    private PostCategory category;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Post(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public Post(String title, String body, User user) {
        this.title = title;
        this.body = body;
        this.user = user;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setCategory(PostCategory category) {
        this.category = category;
    }

    public void setPostTime(Date postTime) {
        this.postTime = postTime;
    }

    public void setTemp(boolean temp) {
        this.temp = temp;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isTemp() {
        return temp;
    }

    public boolean isBlind() {
        return blind;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public void setBlind(boolean blind) {
        this.blind = blind;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public void setReports(int reports) {
        this.reports = reports;
    }

    public void setId(Long id){
        this.id = id;
    }

    public void setUser(User user){
        this.user = user;
    }



}

