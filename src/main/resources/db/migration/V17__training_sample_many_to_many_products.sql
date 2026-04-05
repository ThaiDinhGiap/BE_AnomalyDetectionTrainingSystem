-- ===========================================================================
-- V17: Refactor training_samples & training_sample_proposal_details
--      from ManyToOne (product_id) to ManyToMany (join tables)
-- ===========================================================================

-- ─── 1. CREATE JOIN TABLE: training_sample_products ───────────────────────
CREATE TABLE training_sample_products
(
    training_sample_id BIGINT NOT NULL,
    product_id         BIGINT NOT NULL,

    PRIMARY KEY (training_sample_id, product_id),
    FOREIGN KEY (training_sample_id) REFERENCES training_samples (id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,

    INDEX idx_tsp_sample (training_sample_id),
    INDEX idx_tsp_product (product_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─── 2. MIGRATE DATA: training_samples.product_id → join table ────────────
INSERT INTO training_sample_products (training_sample_id, product_id)
SELECT id, product_id
FROM training_samples
WHERE product_id IS NOT NULL;

-- ─── 3. DROP old product_id FK & column from training_samples ─────────────
ALTER TABLE training_samples DROP FOREIGN KEY training_samples_ibfk_4;
ALTER TABLE training_samples DROP INDEX idx_training_samples_product;
ALTER TABLE training_samples DROP COLUMN product_id;

-- ─── 4. CREATE JOIN TABLE: training_sample_proposal_detail_products ───────
CREATE TABLE training_sample_proposal_detail_products
(
    proposal_detail_id BIGINT NOT NULL,
    product_id         BIGINT NOT NULL,

    PRIMARY KEY (proposal_detail_id, product_id),
    FOREIGN KEY (proposal_detail_id)
        REFERENCES training_sample_proposal_details (id) ON DELETE CASCADE,
    FOREIGN KEY (product_id)
        REFERENCES products (id) ON DELETE CASCADE,

    INDEX idx_tspdp_detail (proposal_detail_id),
    INDEX idx_tspdp_product (product_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─── 5. MIGRATE DATA: proposal_details.product_id → join table ────────────
INSERT INTO training_sample_proposal_detail_products (proposal_detail_id, product_id)
SELECT id, product_id
FROM training_sample_proposal_details
WHERE product_id IS NOT NULL;

-- ─── 6. DROP old product_id FK & column from proposal_details ─────────────
-- Find and drop the FK (constraint name may vary — use the one from V1 DDL)
ALTER TABLE training_sample_proposal_details DROP FOREIGN KEY training_sample_proposal_details_ibfk_4;
ALTER TABLE training_sample_proposal_details DROP COLUMN product_id;

-- ─── 7. INSERT sample data into join tables ───────────────────────────────
-- V6 already inserted product_id values (1-15) for training_samples 1-30.
-- The data was migrated in step 2. Now add extra products to demonstrate
-- ManyToMany: some training samples apply to multiple products.

-- Training samples on Line Tiện (samples 1-6) → add product 2 (BOM-P200)
INSERT IGNORE INTO training_sample_products (training_sample_id, product_id) VALUES
(1, 2), (2, 2), (3, 2), (4, 2), (5, 2), (6, 2),
-- Also add product 16 (BOM-P150) to first 3 samples
(1, 16), (2, 16), (3, 16);

-- Training samples on Line Phay (samples 7-12) → add product 6 (BOM-H250)
INSERT IGNORE INTO training_sample_products (training_sample_id, product_id) VALUES
(7, 6), (8, 6), (9, 6), (10, 6), (11, 6), (12, 6);

-- Training samples on Line Hàn (samples 13-17) → add product 9 (BOM-W200)
INSERT IGNORE INTO training_sample_products (training_sample_id, product_id) VALUES
(13, 9), (14, 9), (15, 9), (16, 9), (17, 9);

-- Training samples on Line Động Cơ (samples 18-22) → add product 12 (MOT-E200)
INSERT IGNORE INTO training_sample_products (training_sample_id, product_id) VALUES
(18, 12), (19, 12), (20, 12), (21, 12), (22, 12);

-- Training samples on Line Lắp Ráp Bơm (samples 23-30) → add product 15 (BOM-B200)
INSERT IGNORE INTO training_sample_products (training_sample_id, product_id) VALUES
(23, 15), (24, 15), (25, 15), (26, 15), (27, 15), (28, 15), (29, 15), (30, 15);
