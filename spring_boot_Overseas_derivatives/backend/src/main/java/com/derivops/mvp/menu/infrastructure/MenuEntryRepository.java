package com.derivops.mvp.menu.infrastructure;
import com.derivops.mvp.menu.*;
import com.derivops.mvp.menu.api.*;
import com.derivops.mvp.menu.application.*;
import com.derivops.mvp.menu.dto.*;


import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuEntryRepository extends JpaRepository<MenuEntry, Long>, MenuEntryRepositoryCustom {
    List<MenuEntry> findByEnabledTrueOrderBySortOrderAsc();
    Optional<MenuEntry> findByMenuKey(String menuKey);
}
