CREATE TABLE IF NOT EXISTS `imooc_coupon_data`.`coupon_template` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `available` boolean NOT NULL DEFAULT false,
    `expired` boolean NOT NULL DEFAULT false,
    `name` varchar(64) NOT NULL DEFAULT '',
    `logo` varchar(256) NOT NULL DEFAULT '',
    `intro` varchar(256) NOT NULL DEFAULT '',
    `category` varchar(64) NOT NULL DEFAULT '',
    `product_line` int(11) NOT NULL DEFAULT '0',
    `coupon_count` int(11) NOT NULL DEFAULT '0',
    `create_time` datetime NOT NULL DEFAULT '0000-01-01 00:00:00',
    `user_id` bigint(20) NOT NULL DEFAULT '0',
    `template_key` varchar(128) NOT NULL DEFAULT '',
    `target` int(11) NOT NULL DEFAULT '0',
    `rule` varchar(1024) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_user_id` (`user_id`),
    UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8

# truncate coupon_template;