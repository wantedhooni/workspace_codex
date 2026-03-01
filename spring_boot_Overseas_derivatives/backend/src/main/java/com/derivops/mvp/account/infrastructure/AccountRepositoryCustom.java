package com.derivops.mvp.account.infrastructure;
import com.derivops.mvp.account.*;
import com.derivops.mvp.account.api.*;
import com.derivops.mvp.account.application.*;
import com.derivops.mvp.account.dto.*;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountRepositoryCustom {
    Page<Account> search(AccountStatus status, String broker, String keyword, String filter, Pageable pageable);
}
