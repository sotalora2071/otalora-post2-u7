package com.example.hexagonal.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoJpaRepository extends JpaRepository<ProductoJpaEntity, Long> {
}
