-- =========================================================================
-- V7: TẠO BẢNG LƯU TRỮ ẢNH (ATTACHMENTS) VÀ OUTBOX XÓA ẢNH (DELETE OUTBOX)
-- =========================================================================

-- 1. Bảng attachments: Chứa thông tin (metadata) của tất cả ảnh trong nhà máy
CREATE TABLE attachments (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Liên kết đa hình (Polymorphic) tới các bảng khác (không dùng Foreign Key cứng)
                             entity_type VARCHAR(50)  NULL,    -- VD: 'DEFECT', 'TRAINING_SAMPLE', 'PRODUCT'...
                             entity_id BIGINT  NULL,           -- ID của bản ghi tương ứng trong các bảng trên

    -- Thông tin file trên MinIO
                             bucket VARCHAR(100)  NULL,
                             object_key VARCHAR(512)  NULL,    -- Đường dẫn file (VD: defects/2026/03/13/10/abc.jpg)

    -- Metadata gốc của file
                             original_filename VARCHAR(255)  NULL,
                             content_type VARCHAR(100)  NULL,  -- VD: image/jpeg, image/png
                             size_bytes BIGINT  NULL,

    -- Trạng thái & Phân loại hiển thị
                             status VARCHAR(20)  NULL DEFAULT 'PENDING', -- PENDING, ACTIVE, DELETING, FAILED
                             is_primary TINYINT(1)  NULL DEFAULT 0,      -- 1 = Ảnh đại diện/chính, 0 = Ảnh phụ
                             sort_order INT  NULL DEFAULT 0,             -- Thứ tự sắp xếp hiển thị trên UI
                             note VARCHAR(255) NULL,                        -- Ghi chú thêm cho ảnh (nếu cần)

    -- Base
                             delete_flag    BOOLEAN      NOT NULL DEFAULT FALSE,
                             created_at     TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
                             created_by     VARCHAR(255),
                             updated_at     TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             updated_by     VARCHAR(255),


    -- Các Ràng buộc (Constraints) & Chỉ mục (Indexes) để tăng tốc độ truy vấn
                             CONSTRAINT uk_attachments_object UNIQUE (bucket, object_key),
                             INDEX idx_attachments_entity (entity_type, entity_id),
                             INDEX idx_attachments_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 2. Bảng attachment_delete_outbox: Hàng đợi chứa các file cần xóa ngầm trên MinIO
CREATE TABLE attachment_delete_outbox (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                          attachment_id BIGINT  NULL,
                                          bucket VARCHAR(100)  NULL,
                                          object_key VARCHAR(512)  NULL,

    -- Thông tin retry nếu xóa lỗi
                                          attempts INT  NULL DEFAULT 0,
                                          status VARCHAR(20)  NULL DEFAULT 'PENDING', -- PENDING, DONE, FAILED
                                          next_run_at DATETIME  NULL DEFAULT CURRENT_TIMESTAMP,
                                          last_error TEXT NULL,

                                          delete_flag    BOOLEAN      NOT NULL DEFAULT FALSE,
                                          created_at     TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
                                          created_by     VARCHAR(255),
                                          updated_at     TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                          updated_by     VARCHAR(255),

    -- Chỉ mục phục vụ Job quét các file cần xóa
                                          INDEX idx_outbox_status_next (status, next_run_at),
                                          INDEX idx_outbox_attachment_id (attachment_id),
                                              CONSTRAINT fk_outbox_to_attachment
        FOREIGN KEY (attachment_id)
        REFERENCES attachments(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;