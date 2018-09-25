CREATE TABLE `user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `email` varchar(32) NOT NULL COMMENT '用户邮箱',
  `username` varchar(32) NOT NULL COMMENT '用户昵称',
	`password` varchar(32) NOT NULL COMMENT '用户密码',
  `role` varchar(32) NOT NULL COMMENT '用户身份',
  `status` TINYINT(2) NOT NULL COMMENT '用户状态',
  `regTime` datetime NOT NULL COMMENT '注册时间',
  `regIp` varchar(240) NOT NULL COMMENT '注册IP',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;


INSERT INTO `user`(`email`, `username`, `password`, `role`, `status`, `regTime`, `regIp`) VALUES ('7361435714@qq.com', 'jamie', '123456', 'root', 1, '2018-09-21 15:15:51', '127.0.0.1');
