package com.curd.template.infra.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface SpringDataItemRepository extends JpaRepository<ItemJpaEntity, String>, QuerydslPredicateExecutor<ItemJpaEntity> {
}
