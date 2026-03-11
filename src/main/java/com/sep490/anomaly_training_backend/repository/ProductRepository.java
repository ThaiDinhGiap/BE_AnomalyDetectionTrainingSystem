package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find product by code (case-sensitive, unique)
     */
    Optional<Product> findByCode(String code);

    /**
     * Check if product code exists
     */
    boolean existsByCode(String code);

    /**
     * Find all products with pagination, excluding deleted
     */
    Page<Product> findByDeleteFlagFalse(Pageable pageable);

    /**
     * Find all products, excluding deleted (no pagination)
     */
    List<Product> findByDeleteFlagFalse();

    /**
     * Search products by code or name, excluding deleted
     */
    @Query("""
        SELECT p FROM Product p
        WHERE p.deleteFlag = false
        AND (
            LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        ORDER BY p.code ASC
    """)
    List<Product> searchByCodeOrName(@Param("keyword") String keyword);

    /**
     * Find all products by process ID
     * Join through ProductProcess bridge table
     */
    @Query("""
        SELECT DISTINCT p FROM Product p
        INNER JOIN p.productProcesses pp
        WHERE pp.process.id = :processId
        AND p.deleteFlag = false
        ORDER BY p.code ASC
    """)
    List<Product> findByProcessId(@Param("processId") Long processId);

    /**
     * Find all products by process ID with pagination
     */
    @Query("""
        SELECT DISTINCT p FROM Product p
        INNER JOIN p.productProcesses pp
        WHERE pp.process.id = :processId
        AND p.deleteFlag = false
        ORDER BY p.code ASC
    """)
    Page<Product> findByProcessIdPaginated(@Param("processId") Long processId, Pageable pageable);

    /**
     * Find product by code, excluding deleted
     */
    @Query("""
        SELECT p FROM Product p
        WHERE p.code = :code
        AND p.deleteFlag = false
    """)
    Optional<Product> findByCodeAndNotDeleted(@Param("code") String code);
}
