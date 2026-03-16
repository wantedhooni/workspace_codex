package com.example.marketsignal.news;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 뉴스 분석 저장소 접근을 담당한다.
 */
public interface NewsAnalysisRepository extends JpaRepository<NewsAnalysis, Long> {

    List<NewsAnalysis> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
            select n
            from NewsAnalysis n
            where (:query is null or trim(:query) = '' or lower(n.headline) like lower(concat('%', :query, '%')))
              and (:sentiment is null or n.sentiment = :sentiment)
            order by n.createdAt desc
            """)
    Page<NewsAnalysis> searchRecent(@Param("query") String query, @Param("sentiment") NewsSentiment sentiment, Pageable pageable);
}
