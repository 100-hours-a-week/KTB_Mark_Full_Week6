package com.mark.community.entity;

import com.mark.community.entity.key.PostReportId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "post_report")
public class Report {
    @EmbeddedId
    private PostReportId id;


    public Report(Long userId, Long postId){
        this.id = new PostReportId(userId, postId);
    }
}
