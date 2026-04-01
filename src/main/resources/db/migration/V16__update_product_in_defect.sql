UPDATE defects
SET product_id = FLOOR(RAND() * 26) + 1
WHERE product_id IS NULL;