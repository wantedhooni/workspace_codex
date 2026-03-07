package com.example.securities.external;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalMessageRepository extends JpaRepository<ExternalMessage, String> {
}
