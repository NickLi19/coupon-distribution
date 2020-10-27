CREATE TABLE IF NOT EXISTS `imooc_coupon_data`.`coupon` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `template_id` int(11) NOT NULL DEFAULT '0',
    `user_id` bigint(20) NOT NULL DEFAULT '0',
    `coupon_code` varchar(64) NOT NULL DEFAULT '',
    `assign_time` datetime NOT NULL DEFAULT '0000-01-01 00:00:00',
    `status` int(11) NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_template_id` (`template_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;