-- 기록 날짜는 createdAt에 반영하므로 record_date 컬럼 제거 (이미 add-record-date 적용한 경우에만 실행)
-- PostgreSQL:
ALTER TABLE records DROP COLUMN IF EXISTS record_date;
