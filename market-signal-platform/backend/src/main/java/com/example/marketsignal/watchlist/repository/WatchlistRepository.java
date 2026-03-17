package com.example.marketsignal.watchlist;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

/**
 * 관심 종목 저장소 접근을 담당한다.
 */
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    List<Watchlist> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Watchlist> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndTickerIgnoreCase(Long userId, String ticker);

    @Query("""
            select w
            from Watchlist w
            where w.user.id = :userId
              and (
                  lower(w.ticker) like lower(concat('%', :query, '%'))
                  or lower(w.companyName) like lower(concat('%', :query, '%'))
              )
            order by w.createdAt desc
            """)
    List<Watchlist> searchByUserIdAndKeywordOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("query") String query);
}
