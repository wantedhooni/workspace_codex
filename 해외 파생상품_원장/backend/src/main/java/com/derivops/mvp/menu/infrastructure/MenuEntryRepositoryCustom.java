package com.derivops.mvp.menu.infrastructure;
import com.derivops.mvp.menu.*;
import com.derivops.mvp.menu.api.*;
import com.derivops.mvp.menu.application.*;
import com.derivops.mvp.menu.dto.*;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MenuEntryRepositoryCustom {
    Page<MenuEntry> search(String keyword, String filter, Pageable pageable);
}
