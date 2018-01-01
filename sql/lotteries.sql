/*
Navicat MySQL Data Transfer

Source Server         : localhost@root
Source Server Version : 50717
Source Host           : localhost:3306
Source Database       : maplestory

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2018-01-02 00:11:30
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `lotteries`
-- ----------------------------
DROP TABLE IF EXISTS `lotteries`;
CREATE TABLE `lotteries` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `date` int(11) NOT NULL COMMENT '开奖日期',
  `number` varchar(255) NOT NULL COMMENT '开奖号码',
  `money` int(11) NOT NULL COMMENT '这一期累计剩下的金额',
  `salary` int(11) NOT NULL COMMENT '每个汇率获得的奖金',
  PRIMARY KEY (`id`),
  UNIQUE KEY `date` (`date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=gbk;

-- ----------------------------
-- Records of lotteries
-- ----------------------------

-- ----------------------------
-- Table structure for `lottery`
-- ----------------------------
DROP TABLE IF EXISTS `lottery`;
CREATE TABLE `lottery` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `character_id` int(11) NOT NULL COMMENT '角色号',
  `number` varchar(255) NOT NULL COMMENT '买的号码',
  `rate` int(11) NOT NULL DEFAULT '1' COMMENT '倍率',
  `date` int(11) NOT NULL COMMENT '买票期号 格式ymd00-23',
  `money` int(11) NOT NULL DEFAULT '0' COMMENT '可领取的金额',
  PRIMARY KEY (`id`),
  KEY `IDX_date_number_money` (`date`,`number`,`money`) USING BTREE,
  KEY `IDX_characterId_money` (`character_id`,`money`)
) ENGINE=InnoDB DEFAULT CHARSET=gbk;

-- ----------------------------
-- Records of lottery
-- ----------------------------
