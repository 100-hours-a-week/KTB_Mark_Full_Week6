INSERT INTO upload_file (id, file_name, file_path, file_type, file_size)
VALUES (1, 'F11.png', 'uploads/003f0af6-7393-4662-b2cf-b7d74935f819.png', 'IMAGE', 23009);

INSERT INTO users (id, email, password, nickname, deleted, file_id, role)
VALUES (1, 'test@gmail.com', '$2a$10$8halKtcDzh8BiFonhOWXMOGs4VTdMrFBj672ZO.y8xUv/JVQ3Wg6i', 'fa', FALSE, 1, 'ROLE_USER');

INSERT INTO post (id, user_id, title, body, blind, reports, temp, post_time, views, deleted, edited)
VALUES (1, 1, '공지 게시글 입니다.', '공지 게시글입니다..', FALSE, 0, FALSE, CURRENT_TIMESTAMP, 0, FALSE, FALSE);



ALTER TABLE upload_file ALTER COLUMN id RESTART WITH 2;
ALTER TABLE users ALTER COLUMN id RESTART WITH 2;
ALTER TABLE post ALTER COLUMN id RESTART WITH 2;