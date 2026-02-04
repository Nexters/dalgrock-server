-- User 더미 데이터 (users 테이블 가정)
INSERT INTO users (id, nickname, profile_image, provider, provider_id, role, created_at, updated_at)
VALUES (1, '테스트유저', 'https://example.com/profile.jpg', 'KAKAO', '123456789', 'USER', NOW(), NOW());

-- Weekly 더미 데이터
INSERT INTO weekly (id, title, created_at, updated_at)
VALUES (1, '2024년 2월 첫째 주', NOW(), NOW());

-- Music 데이터
INSERT INTO musics (id, title, artist, genre, thumbnail, created_at, updated_at)
VALUES (1, 'Dynamite', 'BTS', 'Pop', 'https://example.com/dynamite.jpg', NOW(), NOW()),
       (2, '밤편지', '아이유', 'Ballad', 'https://example.com/night-letter.jpg', NOW(), NOW()),
       (3, 'Shape of You', 'Ed Sheeran', 'Pop', 'https://example.com/shape.jpg', NOW(), NOW());

-- Record 데이터
INSERT INTO records (id, thumbnail, location, content, user_id, weekly_id, created_at, updated_at)
VALUES (1,
        'https://example.com/record-thumb.jpg',
        '강남역 카페',
        '오늘은 친구들과 카페에서 좋은 시간을 보냈다. 오랜만에 만나서 이야기하며 힐링되는 시간이었다.',
        1,
        1,
        NOW(),
        NOW());

-- RecordMusic 연결 데이터 (Record 1번과 Music들 연결)
INSERT INTO record_music (id, record_id, music_id, created_at, updated_at)
VALUES (1, 1, 1, NOW(), NOW()),
       (2, 1, 2, NOW(), NOW());

-- Emotion 데이터 (Record 1번에 속함)
INSERT INTO emotions (record_id, category, value)
VALUES (1, 'positive', '행복해요'),
       (1, 'positive', '설레요'),
       (1, 'calm', '평온해요');

-- Situation 데이터 (Record 1번에 속함)
INSERT INTO situations (record_id, category, value)
VALUES (1, 'social', '친구와 함께'),
       (1, 'place', '카페'),
       (1, 'activity', '대화');
