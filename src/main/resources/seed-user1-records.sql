-- userId 1번 테스트 유저: 이번주·저번주·저저번주 기록 시딩 (월요일 기준 주차)
-- PostgreSQL. 실행: psql 또는 DB 클라이언트에서 실행

-- 1) Weekly 3개 (이번주, 저번주, 저저번주)
--    주차: year, month, week (월요일 시작 기준)
INSERT INTO weekly (id, title, year, month, week, created_at, updated_at)
VALUES
  (100, '2026년 2월 2주차', 2026, 2, 2, NOW(), NOW()),
  (101, '2026년 2월 1주차', 2026, 2, 1, NOW(), NOW()),
  (102, '2026년 1월 4주차', 2026, 1, 4, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- 2) Records: 이번주 1개, 저번주 5개, 저저번주 6개 (user_id = 1)
INSERT INTO records (id, user_id, weekly_id, thumbnail, location, content, created_at, updated_at)
VALUES
  (200, 1, 100, 'https://example.com/thumb1.png', '서울', '이번주 기록 내용', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
  (201, 1, 101, 'https://example.com/thumb2.png', '카페', '저번주 기록 1', NOW() - INTERVAL '9 days', NOW() - INTERVAL '9 days'),
  (202, 1, 101, 'https://example.com/thumb2.png', '카페', '저번주 기록 2', NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days'),
  (203, 1, 101, 'https://example.com/thumb2.png', '퇴근길', '저번주 기록 3', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
  (204, 1, 101, 'https://example.com/thumb2.png', '카페', '저번주 기록 4', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
  (205, 1, 101, 'https://example.com/thumb2.png', '부산', '저번주 기록 5', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
  (206, 1, 102, 'https://example.com/thumb3.png', '버스', '저저번주 기록 1', NOW() - INTERVAL '16 days', NOW() - INTERVAL '16 days'),
  (207, 1, 102, 'https://example.com/thumb3.png', '회사', '저저번주 기록 2', NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days'),
  (208, 1, 102, 'https://example.com/thumb3.png', '버스', '저저번주 기록 3', NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days'),
  (209, 1, 102, 'https://example.com/thumb3.png', '부산', '저저번주 기록 4', NOW() - INTERVAL '13 days', NOW() - INTERVAL '13 days'),
  (210, 1, 102, 'https://example.com/thumb3.png', '카페', '저저번주 기록 5', NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days'),
  (211, 1, 102, 'https://example.com/thumb3.png', '버스', '저저번주 기록 6', NOW() - INTERVAL '11 days', NOW() - INTERVAL '11 days')
ON CONFLICT DO NOTHING;

-- 3) Emotions (record_id 200~211)
INSERT INTO emotions (record_id, category, value)
VALUES
  (200, '', '기쁨'),
  (200, '', '설렘'),
  (201, '', '평온'),
  (201, '', '감사'),
  (202, '', '신남'),
  (202, '', '기대'),
  (203, '', '행복'),
  (203, '', '편안'),
  (204, '', '설렘'),
  (204, '', '기쁨'),
  (205, '', '감사'),
  (205, '', '평온'),
  (206, '', '신남'),
  (206, '', '기대'),
  (207, '', '행복'),
  (207, '', '편안'),
  (208, '', '설렘'),
  (208, '', '기쁨'),
  (209, '', '감사'),
  (209, '', '평온'),
  (210, '', '신남'),
  (210, '', '기대'),
  (211, '', '행복'),
  (211, '', '편안');

-- 4) Situations (record_id 200~211)
INSERT INTO situations (record_id, category, value)
VALUES
  (200, '', '출퇴근'),
  (200, '', '휴식'),
  (201, '', '운동'),
  (201, '', '독서'),
  (202, '', '여행'),
  (202, '', '맛집'),
  (203, '', '출퇴근'),
  (203, '', '휴식'),
  (204, '', '운동'),
  (204, '', '독서'),
  (205, '', '여행'),
  (205, '', '맛집'),
  (206, '', '출퇴근'),
  (206, '', '휴식'),
  (207, '', '운동'),
  (207, '', '독서'),
  (208, '', '여행'),
  (208, '', '맛집'),
  (209, '', '출퇴근'),
  (209, '', '휴식'),
  (210, '', '운동'),
  (210, '', '독서'),
  (211, '', '여행'),
  (211, '', '맛집');

-- 5) 시퀀스 갱신 (다음 INSERT 시 id 충돌 방지, 이미 사용 중인 max id보다 크게)
SELECT setval(pg_get_serial_sequence('weekly', 'id'), (SELECT COALESCE(MAX(id), 1) FROM weekly));
SELECT setval(pg_get_serial_sequence('records', 'id'), (SELECT COALESCE(MAX(id), 1) FROM records));
