package com.example.marketsignal.watchlist;

import com.example.marketsignal.common.BaseTimeEntity;
import com.example.marketsignal.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자의 관심 종목 항목을 저장하는 엔티티다.
 */
@Getter
@Entity
@Table(name = "watchlists")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Watchlist extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 20)
    private String ticker;

    @Column(nullable = false, length = 120)
    private String companyName;

    @Builder
    public Watchlist(User user, String ticker, String companyName) {
        this.user = user;
        this.ticker = ticker;
        this.companyName = companyName;
    }
}
