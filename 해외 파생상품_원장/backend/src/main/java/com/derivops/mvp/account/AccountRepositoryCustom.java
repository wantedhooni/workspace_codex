package com.derivops.mvp.account;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountRepositoryCustom {
    Page<Account> search(AccountStatus status, String broker, String keyword, Pageable pageable);
}
