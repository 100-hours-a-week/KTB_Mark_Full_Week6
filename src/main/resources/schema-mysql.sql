CREATE TABLE IF NOT EXISTS upload_file
(
    id        BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    file_name varchar(255) NOT NULL,
    file_path varchar(500) NOT NULL,
    file_type varchar(20)  NOT NULL CHECK (file_type IN ('IMAGE', 'UNKNOWN')),
    file_size BIGINT       NOT NULL
);

CREATE TABLE IF NOT EXISTS users
(
    id       BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email    varchar(320) NOT NULL UNIQUE,
    password varchar(100) NOT NULL,
    nickname varchar(10)  NOT NULL,
    deleted  boolean      NOT NULL DEFAULT false,
    file_id  BIGINT NULL,
    role     varchar(20) NOT NULL DEFAULT 'ROLE_USER',
    CONSTRAINT FK_UPLOADFILE_TO_USER_1 FOREIGN KEY (file_id) REFERENCES upload_file (id)
);

CREATE TABLE IF NOT EXISTS post
(
    id        BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id   BIGINT      NOT NULL,
    title     varchar(26) NULL,
    body      text        NULL,
    blind     boolean     NOT NULL DEFAULT false,
    reports   int         NOT NULL DEFAULT 0,
    temp      boolean     NOT NULL DEFAULT true,
    post_time TIMESTAMP NULL,
    views     BIGINT      NOT NULL DEFAULT 0,
    deleted   boolean     NOT NULL DEFAULT false,
    edited    boolean     NOT NULL DEFAULT false,
    CONSTRAINT FK_USER_TO_POST_1 FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_post_deleted_temp_id (deleted, temp, id DESC)
);

CREATE TABLE IF NOT EXISTS comments
(
    id                BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    post_id           BIGINT       NOT NULL,
    user_id           BIGINT       NOT NULL,
    comment           varchar(255) NOT NULL,
    parent_comment_id BIGINT NULL,
    deleted           boolean      NOT NULL DEFAULT false,
    comment_time      TIMESTAMP NULL,
    CONSTRAINT FK_POST_TO_COMMENT_1 FOREIGN KEY (post_id) REFERENCES post (id),
    CONSTRAINT FK_USER_TO_COMMENT_1 FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_comment_post_id (post_id)
);

CREATE TABLE IF NOT EXISTS post_image
(
    post_id BIGINT NOT NULL,
    file_id BIGINT NOT NULL,
    PRIMARY KEY (post_id, file_id),
    CONSTRAINT FK_POST_TO_POSTIMAGE_1 FOREIGN KEY (post_id) REFERENCES post (id),
    CONSTRAINT FK_UPLOADFILE_TO_POSTIMAGE_1 FOREIGN KEY (file_id) REFERENCES upload_file (id),
    INDEX idx_post_image_post_id (post_id)
);

CREATE TABLE IF NOT EXISTS post_view
(
    user_id   BIGINT    NOT NULL,
    post_id   BIGINT    NOT NULL,
    view_time TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id, post_id),
    CONSTRAINT FK_USER_TO_VIEW_1 FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT FK_POST_TO_VIEW_1 FOREIGN KEY (post_id) REFERENCES post (id)
);

CREATE TABLE IF NOT EXISTS post_like
(
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, post_id),
    CONSTRAINT FK_USER_TO_LIKE_1 FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT FK_POST_TO_LIKE_1 FOREIGN KEY (post_id) REFERENCES post (id),
    INDEX idx_like_post_id (post_id)
);

CREATE TABLE IF NOT EXISTS post_report
(
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, post_id),
    CONSTRAINT FK_USER_TO_REPORT_1 FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT FK_POST_TO_REPORT_1 FOREIGN KEY (post_id) REFERENCES post (id)
);
