use sncfc_crawler;
-------------------------------------
---- 表名：cs_mp_user
---- 用户表
-------------------------------------
DROP TABLE IF EXISTS `cs_mp_user`;
CREATE TABLE `cs_mp_user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(190) NOT NULL,
  `password` varchar(200) NOT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `token` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-------------------------------------
---- 表名：cs_mp_tactics
---- 采集策略表
-------------------------------------
DROP TABLE IF EXISTS `cs_mp_tactics`;
CREATE TABLE `cs_mp_tactics` (
  `tactics_id` int(11) NOT NULL AUTO_INCREMENT,
  `tactics_name` varchar(190) NOT NULL,
  `status` int(11) NOT NULL DEFAULT 0,
  `jar_path` varchar(1024) DEFAULT NULL,
  `class_name` varchar(1024) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `describe` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`tactics_id`),
  UNIQUE KEY `tactics_name` (`tactics_name`),
  KEY `create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-------------------------------------
---- 表名：cs_mp_task_tag
---- 采集任务标签表
-------------------------------------
DROP TABLE IF EXISTS `cs_mp_task_tag`;
CREATE TABLE `cs_mp_task_tag` (
  `tag_id` int(11) NOT NULL AUTO_INCREMENT,
  `tag_name` varchar(190) NOT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`tag_id`),
  UNIQUE KEY `tag_name` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-------------------------------------
---- 表名：cs_mp_task
---- 采集任务表
-------------------------------------
DROP TABLE IF EXISTS `cs_mp_task`;
CREATE TABLE `cs_mp_task` (
  `task_id` int(11) NOT NULL AUTO_INCREMENT,
  `tag_id` int(11) NOT NULL,
  `task_name` varchar(190) NOT NULL,
  `task_type` int(11) NOT NULL DEFAULT 0,
  `circulation` int(11) DEFAULT 0,
  `status` int(11) DEFAULT 0,
  `describe` varchar(1024) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`task_id`),
  UNIQUE KEY `task_name` (`task_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-------------------------------------
---- 表名：cs_mp_task_detail
---- 采集任务详情表
-------------------------------------
DROP TABLE IF EXISTS `cs_mp_task_detail`;
CREATE TABLE `cs_mp_task_detail` (
  `detail_id` int(11) NOT NULL AUTO_INCREMENT,
  `task_id` int(11) NOT NULL,
  `tactics_id` int(11) NOT NULL,
  `charset` varchar(64),
  `url_host` varchar(64) NOT NULL,
  `url_path` varchar(1024) NOT NULL,
  `url_params` text,
  `request_header` text,
  `start_time` varchar(64) DEFAULT NULL,
  `end_time` varchar(64) DEFAULT NULL,
  `sleep_time` int(11) DEFAULT 0,
  `parse_type` int(11) DEFAULT 0,
  `parse_detail` text,
  `stop_floor` text,
  PRIMARY KEY (`detail_id`),
  CONSTRAINT `task_id_fk_1` FOREIGN KEY (`task_id`) REFERENCES `cs_mp_task` (`task_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-------------------------------------
---- 表名：cs_mp_baidu_ak
---- 百度ak表
-------------------------------------
DROP TABLE IF EXISTS `cs_mp_baidu_ak`;
CREATE TABLE `cs_mp_baidu_ak` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `baidu_ak` varchar(64) NOT NULL DEFAULT '',
  `status` int(11) NOT NULL DEFAULT 1,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-------------------------------------
---- 表名：cs_mp_error_info
---- 采集出错的信息表
-------------------------------------
DROP TABLE IF EXISTS `cs_mp_error_info`;
CREATE TABLE `cs_mp_error_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` int(11) DEFAULT NULL,
  `task_id` int(11) DEFAULT NULL,
  `result_type` int(11) DEFAULT NULL,
  `url_path` varchar(1024) NOT NULL,
  `table_name` varchar(64) NOT NULL,
  `describe` text,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

---------------------------------------------------------------------------------------
--------------------------------  以下为测试用表     --------------------------------------
---------------------------------------------------------------------------------------
-------------------------------------
---- 表名：cs_mp_test
---- 测试采集任务表 手机号归属地查询
-------------------------------------
DROP TABLE IF EXISTS `cs_mp_test`;
CREATE TABLE `cs_mp_test` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `phone_num` int(11) NOT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-------------------------------------
---- 表名：cs_mp_test_result
---- 测试采集任务表 手机号归属地查询的结果
-------------------------------------
DROP TABLE IF EXISTS `cs_mp_test_result`;
CREATE TABLE `cs_mp_test_result` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `task_id` varchar(64) NOT NULL DEFAULT '',
  `url_path` varchar(1024) NOT NULL DEFAULT '',
  `mobile` varchar(64) NOT NULL DEFAULT '',
  `to` varchar(64) NOT NULL DEFAULT '',
  `corp` varchar(64) NOT NULL DEFAULT '',
  `province` varchar(64) NOT NULL DEFAULT '',
  `city` varchar(64) NOT NULL DEFAULT '',
  `area_code` varchar(64) NOT NULL DEFAULT '',
  `post_code` varchar(64) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-------------------------------------
---- 表名：cs_mp_test_address
---- 测试采集会员地理编码的数据源表
-------------------------------------
DROP TABLE IF EXISTS `cs_mp_test_address`;
CREATE TABLE `cs_mp_test_address` (
  `ID` varchar(64) NOT NULL,
  `CERT_NO` varchar(64),
  `MEMBER_ID` varchar(64),
  `USERNO` varchar(64),
  `CUST_ID` varchar(64),
  `MOBILE` varchar(64),
  `ADDR_TYPE` varchar(64),
  `ADDR_PROV` varchar(64),
  `ADDR_CITY` varchar(64),
  `ADDR_INFO` varchar(64),
  `ETL_TIME` varchar(64),
  `STATIS_DATE` varchar(64),
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-------------------------------------
---- 表名：cs_mp_test_address_result
---- 测试采集会员地理编码的结果表
-------------------------------------
DROP TABLE IF EXISTS `cs_mp_test_address_result`;
CREATE TABLE `cs_mp_test_address_result` (
  `ID` varchar(64) NOT NULL,
  `CERT_NO` varchar(64),
  `MEMBER_ID` varchar(64),
  `USERNO` varchar(64),
  `CUST_ID` varchar(64),
  `MOBILE` varchar(64) NOT NULL DEFAULT '',
  `ADDR_TYPE` varchar(64),
  `ADDR_PROV` varchar(64),
  `ADDR_CITY` varchar(64),
  `ADDR_INFO` varchar(64),
  `STATIS_DATE` varchar(64),
  `LNG` varchar(64) NOT NULL DEFAULT '',
  `LAT` varchar(64) NOT NULL DEFAULT '',
  `CONFIDENCE` varchar(64) NOT NULL DEFAULT '',
  `STATUS` varchar(64) NOT NULL DEFAULT '',
  `DES` varchar(64) NOT NULL DEFAULT '',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-------------------------------------
---- 表名：cs_mp_test_store
---- 测试采集门店地理编码的数据源表
-------------------------------------
DROP TABLE IF EXISTS `cs_mp_test_store`;
CREATE TABLE `cs_mp_test_store` (
  `STR_ID` varchar(64) NOT NULL,
  `STR_CD` varchar(64),
  `PROVINCE` varchar(64),
  `CITY` varchar(64),
  `STR_ADDR` varchar(64),
  `IS_CLOSED` varchar(64),
  `UPDATOR` varchar(64),
  `UPDATE_TIME` varchar(64),
  `IS_VALID` varchar(64),
  PRIMARY KEY (`STR_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-------------------------------------
---- 表名：cs_mp_test_store_result
---- 测试采集门店地理编码的结果表
-------------------------------------
DROP TABLE IF EXISTS `cs_mp_test_store_result`;
CREATE TABLE `cs_mp_test_store_result` (
  `STR_ID` varchar(64) NOT NULL,
  `STR_CD` varchar(64),
  `STR_LEVEL` varchar(64),
  `PROVINCE` varchar(64),
  `CITY` varchar(64),
  `STR_ADDR` varchar(64),
  `IS_CLOSED` varchar(64),
  `UPDATE_TIME` varchar(64),
  `LNG` varchar(64) NOT NULL DEFAULT '',
  `LAT` varchar(64) NOT NULL DEFAULT '',
  `CONFIDENCE` varchar(64) NOT NULL DEFAULT '',
  `STATUS` varchar(64) NOT NULL DEFAULT '',
  `DES` varchar(64) NOT NULL DEFAULT '',
  PRIMARY KEY (`STR_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-------------------------------------
---- 表名：cs_mp_test_position
---- 测试采集逆地理编码的数据源表
-------------------------------------
DROP TABLE IF EXISTS `cs_mp_test_position`;
CREATE TABLE `cs_mp_test_position` (
  `cust_id` varchar(64) NOT NULL,
  `last_login_latitude` varchar(64),
  `last_login_longitude` varchar(64),
  `position` varchar(64),
  `status` varchar(64),
  PRIMARY KEY (`cust_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-------------------------------------
---- 表名：cs_mp_test_baidu_result
---- 测试采集百度执行人的结果表
-------------------------------------
DROP TABLE IF EXISTS `cs_mp_test_baidu_result`;
CREATE TABLE `cs_mp_test_baidu_result` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `STATIS_DATE` varchar(64) NOT NULL,
  `GRAP_DATE` varchar(64),
  `KEY_TYPE` varchar(64),
  `KEY_VALUE` varchar(64),
  `KEY_HIDE` varchar(64),
  `NAME` varchar(64),
  `REASON` varchar(64),
  `CASE_NO` varchar(64),
  `ISSUE_DATE` varchar(64) NOT NULL DEFAULT '',
  `ADD_TYPE` varchar(64) NOT NULL DEFAULT '',
  `CREATE_ID` varchar(64) NOT NULL DEFAULT '',
  `SOURCE` varchar(64) NOT NULL DEFAULT '',
  `REMARK` varchar(64) NOT NULL DEFAULT '',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;