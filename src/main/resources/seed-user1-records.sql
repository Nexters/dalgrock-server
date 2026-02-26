-- userId 1번 테스트 유저: 이번주·저번주·저저번주 기록 시딩 (월요일 기준 주차)
-- PostgreSQL. 실행: psql 또는 DB 클라이언트에서 실행

-- 1) Weekly 3개 (이번주, 저번주, 저저번주)
--    주차: year, month, week (월요일 시작 기준)
INSERT INTO weekly (id, title, year, month, week, created_at, updated_at)
VALUES
  (100, '2026년 2월 4주차', 2026, 2, 4, NOW(), NOW()),
  (101, '2026년 2월 3주차', 2026, 2, 3, NOW(), NOW()),
  (102, '2026년 2월 2주차', 2026, 2, 2, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- 2) Records: 이번주 1개, 저번주 5개, 저저번주 6개 (user_id = 1)
INSERT INTO records (id, user_id, weekly_id, thumbnail, location, content, created_at, updated_at)
VALUES
  (200, 1, 100, 'https://example.com/thumb1.png', '서울', '이번주 기록 내용', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
  (201, 1, 101, 'https://example.com/thumb2.png', '카페', '저번주 기록 1', NOW() - INTERVAL '9 days', NOW() - INTERVAL '9 days'),
  (202, 1, 101, 'https://example.com/thumb3.png', '카페', '저번주 기록 2', NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days'),
  (203, 1, 101, 'https://example.com/thumb4.png', '퇴근길', '저번주 기록 3', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
  (204, 1, 101, 'https://example.com/thumb5.png', '카페', '저번주 기록 4', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
  (205, 1, 101, 'https://example.com/thumb6.png', '부산', '저번주 기록 5', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
  (206, 1, 102, 'https://example.com/thumb7.png', '버스', '저저번주 기록 1', NOW() - INTERVAL '16 days', NOW() - INTERVAL '16 days'),
  (207, 1, 102, 'https://example.com/thumb8.png', '회사', '저저번주 기록 2', NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days'),
  (208, 1, 102, 'https://example.com/thumb9.png', '버스', '저저번주 기록 3', NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days'),
  (209, 1, 102, 'https://example.com/thumb10.png', '부산', '저저번주 기록 4', NOW() - INTERVAL '13 days', NOW() - INTERVAL '13 days'),
  (210, 1, 102, 'https://example.com/thumb11.png', '카페', '저저번주 기록 5', NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days'),
  (211, 1, 102, 'https://example.com/thumb12.png', '버스', '저저번주 기록 6', NOW() - INTERVAL '11 days', NOW() - INTERVAL '11 days')
ON CONFLICT DO NOTHING;

-- 3) Emotions (record_id 200~211)
-- category 컬럼 없음. value는 EmotionValue 한글 표시값 또는 enum 이름 사용
INSERT INTO emotions (record_id, value)
VALUES
  (200, '기쁨'),
  (200, '설렘'),
  (201, '감사'),
  (201, '행복'),
  (202, '신남'),
  (202, '설렘'),
  (203, '행복'),
  (203, '뿌듯함'),
  (204, '설렘'),
  (204, '기쁨'),
  (205, '감사'),
  (205, '행복'),
  (206, '신남'),
  (206, '설렘'),
  (207, '행복'),
  (207, '뿌듯함'),
  (208, '설렘'),
  (208, '기쁨'),
  (209, '감사'),
  (209, '행복'),
  (210, '신남'),
  (210, '설렘'),
  (211, '행복'),
  (211, '뿌듯함')
ON CONFLICT DO NOTHING;

-- 4) Situations (record_id 200~211)
-- category 컬럼 없음. value는 SituationValue 한글 표시값 또는 enum 이름 사용
INSERT INTO situations (record_id, value)
VALUES
  (200, '퇴근길'),
  (200, '낮잠'),
  (201, '운동'),
  (201, '독서'),
  (202, '드라이브'),
  (202, '산책'),
  (203, '퇴근길'),
  (203, '낮잠'),
  (204, '운동'),
  (204, '독서'),
  (205, '드라이브'),
  (205, '산책'),
  (206, '퇴근길'),
  (206, '낮잠'),
  (207, '운동'),
  (207, '독서'),
  (208, '드라이브'),
  (208, '산책'),
  (209, '퇴근길'),
  (209, '낮잠'),
  (210, '운동'),
  (210, '독서'),
  (211, '드라이브'),
  (211, '산책')
ON CONFLICT DO NOTHING;

-- 5) 시퀀스 갱신 (다음 INSERT 시 id 충돌 방지, 이미 사용 중인 max id보다 크게)
SELECT setval(pg_get_serial_sequence('weekly', 'id'), (SELECT COALESCE(MAX(id), 1) FROM weekly));
SELECT setval(pg_get_serial_sequence('records', 'id'), (SELECT COALESCE(MAX(id), 1) FROM records));
