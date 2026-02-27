package com.quant.mvp.access.infrastructure;

import com.quant.mvp.access.domain.AccessUser;
import com.quant.mvp.pipeline.domain.UserStatus;
import java.util.List;

public interface AccessUserQueryRepository {

    List<AccessUser> searchUsers(Long userId, String email, String name, UserStatus status, String roleCode);
}
