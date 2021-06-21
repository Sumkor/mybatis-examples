/*Table structure for table `student` */

DROP TABLE IF EXISTS `student`;

CREATE TABLE `student` (
                           `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '编号',
                           `name` varchar(20) DEFAULT NULL COMMENT '姓名',
                           `phone` varchar(20) DEFAULT NULL COMMENT '电话',
                           `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
                           `sex` tinyint(4) DEFAULT NULL COMMENT '性别',
                           `locked` tinyint(4) DEFAULT NULL COMMENT '状态(0:正常,1:锁定)',
                           `gmt_created` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                           `delete` int(11) DEFAULT NULL,
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COMMENT='学生表';