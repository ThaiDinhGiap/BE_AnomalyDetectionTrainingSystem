-- Add 4 new columns to defects table

ALTER TABLE defects
ADD COLUMN customer VARCHAR(255) NULL AFTER defect_type,
ADD COLUMN quantity INT NULL AFTER customer,
ADD COLUMN conclusion LONGTEXT NULL AFTER quantity,
ADD COLUMN product_id BIGINT NULL AFTER conclusion,
ADD CONSTRAINT fk_defects_product FOREIGN KEY (product_id) REFERENCES products (id),
ADD INDEX idx_defects_product (product_id);

-- Add 4 new columns to defect_proposal_details table
ALTER TABLE defect_proposal_details
ADD COLUMN customer VARCHAR(255) NULL AFTER defect_type,
ADD COLUMN quantity INT NULL AFTER customer,
ADD COLUMN conclusion LONGTEXT NULL AFTER quantity,
ADD COLUMN product_id BIGINT NULL AFTER conclusion,
ADD CONSTRAINT fk_defect_proposal_details_product FOREIGN KEY (product_id) REFERENCES products (id),
ADD INDEX idx_defect_proposal_details_product (product_id);

