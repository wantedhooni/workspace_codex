package com.example.marketsignal.news;

import com.example.marketsignal.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 뉴스 분석 결과를 저장한다.
 */
@Getter
@Entity
@Table(name = "news_analysis")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewsAnalysis extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String headline;

    @Lob
    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NewsSentiment sentiment;

    @Column(nullable = false, length = 500)
    private String summary;

    @Column(nullable = false, length = 50)
    private String impact;

    @Column(nullable = false, length = 600)
    private String interpretation;

    @Builder
    public NewsAnalysis(
            String headline,
            String content,
            NewsSentiment sentiment,
            String summary,
            String impact,
            String interpretation
    ) {
        this.headline = headline;
        this.content = content;
        this.sentiment = sentiment;
        this.summary = summary;
        this.impact = impact;
        this.interpretation = interpretation;
    }
}
