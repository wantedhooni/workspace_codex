package com.quant.mvp.access.infrastructure;

import com.quant.mvp.access.domain.AccessMenu;
import java.util.List;

public interface AccessMenuQueryRepository {

    List<AccessMenu> searchMenus(Long menuId, Long parentMenuId, String menuKey, String menuLabel, Boolean enabled);
}
