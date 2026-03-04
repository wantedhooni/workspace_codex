package com.revy.mvpbanking.auth.infrastructure;

import com.revy.mvpbanking.admin.domain.AdminUserRepository;
import com.revy.mvpbanking.auth.domain.AuthenticatedPrincipal;
import com.revy.mvpbanking.auth.domain.AuthenticatedPrincipalLoader;
import com.revy.mvpbanking.auth.domain.PrincipalType;
import com.revy.mvpbanking.user.domain.EndUserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaAuthenticatedPrincipalLoader implements AuthenticatedPrincipalLoader {

    private final AdminUserRepository adminUserRepository;
    private final EndUserRepository endUserRepository;

    public JpaAuthenticatedPrincipalLoader(AdminUserRepository adminUserRepository, EndUserRepository endUserRepository) {
        this.adminUserRepository = adminUserRepository;
        this.endUserRepository = endUserRepository;
    }

    @Override
    public Optional<AuthenticatedPrincipal> load(PrincipalType principalType, UUID principalId) {
        return switch (principalType) {
            case ADMIN -> adminUserRepository.findById(principalId)
                    .map(admin -> new AuthenticatedPrincipal(
                            admin.getId(),
                            admin.getEmail(),
                            admin.getDisplayName(),
                            PrincipalType.ADMIN,
                            List.of("ROLE_" + admin.getRole().name()),
                            admin.isActive()
                    ));
            case USER -> endUserRepository.findById(principalId)
                    .map(user -> new AuthenticatedPrincipal(
                            user.getId(),
                            user.getEmail(),
                            user.getFullName(),
                            PrincipalType.USER,
                            List.of("ROLE_USER"),
                            user.isActive()
                    ));
        };
    }
}
