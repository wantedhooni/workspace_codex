package com.revy.scaffolding.user.domain.repository;

import com.revy.scaffolding.user.dto.UserSummaryResponse;
import java.util.List;

public interface UserQueryRepository {
    List<UserSummaryResponse> search(String keyword);
}

