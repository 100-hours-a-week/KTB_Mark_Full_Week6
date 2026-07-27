CREATE TABLE users
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    email    varchar(320) NOT NULL UNIQUE,
    password varchar(100) NOT NULL,
    nickname varchar(10)  NOT NULL,
    deleted  boolean      NOT NULL DEFAULT false,
    file_id  BIGINT NULL,
    role     varchar(20) NOT NULL DEFAULT 'ROLE_USER'
);

CREATE TABLE comments
(
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    post_id           BIGINT       NOT NULL,
    user_id           BIGINT       NOT NULL,
    comment           varchar(255) NOT NULL,
    parent_comment_id BIGINT NULL,
    deleted           boolean      NOT NULL DEFAULT false,
    comment_time      TIMESTAMP NULL
);

CREATE TABLE upload_file
(
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    file_name varchar(255) NOT NULL,
    file_path varchar(500) NOT NULL,
    file_type varchar(20)  NOT NULL CHECK (file_type IN ('IMAGE', 'UNKNOWN')),
    file_size BIGINT       NOT NULL
);

CREATE TABLE post_image
(
    post_id BIGINT NOT NULL,
    file_id BIGINT NOT NULL
);

CREATE TABLE post
(
    id        BIGINT      NOT NULL AUTO_INCREMENT,
    user_id   BIGINT      NOT NULL,
    title     varchar(26) NULL,
    body      text        NULL,
    blind     boolean     NOT NULL DEFAULT false,
    reports   int         NOT NULL DEFAULT 0,
    temp      boolean     NOT NULL DEFAULT true,
    post_time TIMESTAMP NULL,
    views     BIGINT      NOT NULL DEFAULT 0,
    deleted   boolean     NOT NULL DEFAULT false,
    edited    boolean     NOT NULL DEFAULT false
);

CREATE TABLE post_view
(
    user_id   BIGINT    NOT NULL,
    post_id   BIGINT    NOT NULL,
    view_time TIMESTAMP NOT NULL
);

CREATE TABLE post_like
(
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL
);

CREATE TABLE post_report
(
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL
);

ALTER TABLE users
    ADD CONSTRAINT PK_USER PRIMARY KEY (id);

ALTER TABLE comments
    ADD CONSTRAINT PK_COMMENT PRIMARY KEY (id);

ALTER TABLE upload_file
    ADD CONSTRAINT PK_UPLOADFILE PRIMARY KEY (id);

ALTER TABLE post_image
    ADD CONSTRAINT PK_POSTIMAGE PRIMARY KEY (post_id, file_id);

ALTER TABLE post
    ADD CONSTRAINT PK_POST PRIMARY KEY (id);

ALTER TABLE post_view
    ADD CONSTRAINT PK_VIEW PRIMARY KEY (user_id, post_id);

ALTER TABLE post_like
    ADD CONSTRAINT PK_LIKE PRIMARY KEY (user_id, post_id);

ALTER TABLE post_report
    ADD CONSTRAINT PK_REPORT PRIMARY KEY (user_id, post_id);

ALTER TABLE comments
    ADD CONSTRAINT FK_POST_TO_COMMENT_1 FOREIGN KEY (post_id) REFERENCES post (id);

ALTER TABLE comments
    ADD CONSTRAINT FK_USER_TO_COMMENT_1 FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE users
    ADD CONSTRAINT FK_UPLOADFILE_TO_USER_1 FOREIGN KEY (file_id) REFERENCES upload_file (id);

ALTER TABLE post_image
    ADD CONSTRAINT FK_POST_TO_POSTIMAGE_1 FOREIGN KEY (post_id) REFERENCES post (id);

ALTER TABLE post_image
    ADD CONSTRAINT FK_UPLOADFILE_TO_POSTIMAGE_1 FOREIGN KEY (file_id) REFERENCES upload_file (id);

ALTER TABLE post
    ADD CONSTRAINT FK_USER_TO_POST_1 FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE post_view
    ADD CONSTRAINT FK_USER_TO_VIEW_1 FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE post_view
    ADD CONSTRAINT FK_POST_TO_VIEW_1 FOREIGN KEY (post_id) REFERENCES post (id);

ALTER TABLE post_like
    ADD CONSTRAINT FK_USER_TO_LIKE_1 FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE post_like
    ADD CONSTRAINT FK_POST_TO_LIKE_1 FOREIGN KEY (post_id) REFERENCES post (id);

ALTER TABLE post_report
    ADD CONSTRAINT FK_USER_TO_REPORT_1 FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE post_report
    ADD CONSTRAINT FK_POST_TO_REPORT_1 FOREIGN KEY (post_id) REFERENCES post (id);

CREATE INDEX idx_post_deleted_temp_id ON post (deleted, temp, id DESC);
CREATE INDEX idx_comment_post_id ON comments (post_id);
CREATE INDEX idx_like_post_id ON post_like (post_id);
CREATE INDEX idx_post_image_post_id ON post_image (post_id);