DROP TABLE IF EXISTS `article`;
CREATE TABLE `article` (
  `id`               INTEGER(20)  NOT NULL,
  `creator`          VARCHAR(128) NOT NULL,
  `title`            VARCHAR(256) NOT NULL,
  `context`          TEXT         NULL,
  `category_id`      INTEGER(20)  NULL,
  `create_date`      INTEGER(13)  NOT NULL,
  `last_update_date` INTEGER(13)           DEFAULT 1,
  `status`           INTEGER(4)   NULL,
  `is_del`           INTEGER      NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id`   INTEGER(20)  NOT NULL,
  `name` VARCHAR(128) NOT NULL,
  PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
  `id`   INTEGER(20)  NOT NULL,
  `name` VARCHAR(128) NOT NULL,
  PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `article_tag`;
CREATE TABLE `article_tag` (
  `id`         INTEGER(20) NOT NULL,
  `article_id` INTEGER(20) NOT NULL,
  `tag_id`     INTEGER(20) NOT NULL,
  PRIMARY KEY (`id`)
);