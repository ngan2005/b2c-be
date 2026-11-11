-- =============================================================================
-- FILE 3: 03_reset_sequences.sql
-- MỤC ĐÍCH: RESET LẠI TOÀN BỘ SEQUENCE CỦA CÁC BẢNG TRONG DATABASE
--
-- TẠI SAO CẦN CHẠY FILE NÀY?
-- Khi chúng ta INSERT dữ liệu thủ công có gán sẵn giá trị ID (ví dụ: id = 1, 2, ... 12),
-- PostgreSQL KHÔNG tự động tăng giá trị của internal sequence (bộ sinh số tự tăng).
-- Nếu không reset sequence, khi Spring Boot JPA (hoặc API đăng ký, tạo sản phẩm, đơn hàng...)
-- gọi repository.save() hoặc gọi nextval(), PostgreSQL sẽ trả về 1 hoặc 2 -> Gây lỗi:
-- "ERROR: duplicate key value violates unique constraint ..._pkey"
--
-- File này tự động đồng bộ sequence với: COALESCE(MAX(id), 0) + 1 (với is_called = false)
-- Đảm bảo lần gọi nextval() kế tiếp của hệ thống sẽ luôn nhận ID an toàn, không bao giờ trùng.
-- =============================================================================

DO $$
DECLARE
    rec RECORD;
    v_seq_name TEXT;
    v_max_id BIGINT;
    v_count INT := 0;
BEGIN
    RAISE NOTICE '-------------------------------------------------------------';
    RAISE NOTICE ' BẮT ĐẦU ĐỒNG BỘ TOÀN BỘ SEQUENCE IDENTITY TRONG DATABASE...';
    RAISE NOTICE '-------------------------------------------------------------';

    FOR rec IN 
        SELECT 
            c.table_schema,
            c.table_name, 
            c.column_name 
        FROM information_schema.columns c
        JOIN information_schema.tables t 
          ON t.table_schema = c.table_schema 
         AND t.table_name = c.table_name
        WHERE c.table_schema = 'public' 
          AND t.table_type = 'BASE TABLE'
          AND c.table_name != 'flyway_schema_history'
          AND c.column_name = 'id'
        ORDER BY c.table_name
    LOOP
        -- Lấy tên sequence gắn với cột id của bảng
        v_seq_name := pg_get_serial_sequence(format('%I.%I', rec.table_schema, rec.table_name), rec.column_name);

        IF v_seq_name IS NOT NULL THEN
            -- Lấy MAX(id) hiện tại của bảng
            EXECUTE format('SELECT COALESCE(MAX(%I), 0) FROM %I.%I', rec.column_name, rec.table_schema, rec.table_name) INTO v_max_id;
            
            -- Đặt sequence: nếu max = 0 thì nextval = 1; nếu max > 0 thì nextval = max + 1
            IF v_max_id = 0 THEN
                EXECUTE format('SELECT setval(%L, 1, false)', v_seq_name);
                RAISE NOTICE '  -> Bảng [%]: Rỗng (0 bản ghi). Sequence [%] đặt về 1', rec.table_name, v_seq_name;
            ELSE
                EXECUTE format('SELECT setval(%L, %s, false)', v_seq_name, v_max_id + 1);
                RAISE NOTICE '  -> Bảng [%]: MAX(id) = %. Sequence [%] đặt tiếp theo là %', rec.table_name, v_max_id, v_seq_name, v_max_id + 1;
            END IF;
            
            v_count := v_count + 1;
        END IF;
    END LOOP;

    RAISE NOTICE '-------------------------------------------------------------';
    RAISE NOTICE ' HOÀN TẤT ĐỒNG BỘ: Đã reset thành công % sequences!', v_count;
    RAISE NOTICE ' Hệ thống backend Spring Boot và API có thể tạo mới an toàn tuyệt đối';
    RAISE NOTICE ' mà không bao giờ bị lỗi duplicate key (..._pkey)!';
    RAISE NOTICE '-------------------------------------------------------------';
END $$;

