package com.portal.admin.service;

import com.portal.admin.domain.LoginPolicy;
import static com.portal.admin.dto.LoginPolicyDtos.*;
import com.portal.admin.repo.LoginPolicyRepository;
import com.portal.admin.repo.ServiceAuditLogRepository;
import com.portal.admin.service.base.BaseCrudService;
import org.springframework.stereotype.Service;

@Service
public class LoginPolicyCrudService extends BaseCrudService<LoginPolicy, Long, CreateLoginPolicyRequest, UpdateLoginPolicyRequest, LoginPolicyResponse> {

    public LoginPolicyCrudService(LoginPolicyRepository repository, ServiceAuditLogRepository auditLogs) {
        super(repository, auditLogs);
    }

    @Override
    protected LoginPolicy createEntity(CreateLoginPolicyRequest request) {
        return new LoginPolicy(
                request.name(),
                request.maxFailCount(),
                request.lockMinutes(),
                request.allowedIpCidr(),
                request.enabled() == null ? Boolean.TRUE : request.enabled()
        );
    }

    @Override
    protected void applyUpdate(LoginPolicy entity, UpdateLoginPolicyRequest request) {
        if (request.name() != null) entity.setName(request.name());
        if (request.maxFailCount() != null) entity.setMaxFailCount(request.maxFailCount());
        if (request.lockMinutes() != null) entity.setLockMinutes(request.lockMinutes());
        if (request.allowedIpCidr() != null) entity.setAllowedIpCidr(request.allowedIpCidr());
        if (request.enabled() != null) entity.setEnabled(request.enabled());
    }

    @Override
    protected LoginPolicyResponse toResponse(LoginPolicy entity) {
        return new LoginPolicyResponse(
                entity.getId(),
                entity.getName(),
                entity.getMaxFailCount(),
                entity.getLockMinutes(),
                entity.getAllowedIpCidr(),
                entity.getEnabled()
        );
    }
}
