package com.quant.mvp.access.infrastructure;

import com.quant.mvp.access.domain.AccessMenu;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessMenuRepository extends JpaRepository<AccessMenu, Long>, AccessMenuQueryRepository {

    Optional<AccessMenu> findByMenuKeyIgnoreCase(String menuKey);

    boolean existsByMenuKeyIgnoreCase(String menuKey);

    boolean existsByParentMenuId(Long parentMenuId);
}
