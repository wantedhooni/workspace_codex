package com.example.securities.channel;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelApplicationRepository extends JpaRepository<ChannelApplication, String> {

    Optional<ChannelApplication> findByIdempotencyKey(String idempotencyKey);
}
