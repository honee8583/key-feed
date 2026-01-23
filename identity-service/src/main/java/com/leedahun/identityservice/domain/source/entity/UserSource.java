package com.leedahun.identityservice.domain.source.entity;

import com.leedahun.identityservice.common.entity.BaseTimeEntity;
import com.leedahun.identityservice.domain.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class UserSource extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_source_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    @Column(name = "user_defined_name", nullable = false, length = 100)
    private String userDefinedName;

    @Column(name = "receive_feed", nullable = false)
    @Builder.Default
    private Boolean receiveFeed = true;

    public void toggleReceiveFeed() {
        this.receiveFeed = !this.receiveFeed;
    }

}
