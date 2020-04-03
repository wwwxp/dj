# SQL-Front 5.1  (Build 4.16)

/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE */;
/*!40101 SET SQL_MODE='NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES */;
/*!40103 SET SQL_NOTES='ON' */;


# Host: localhost    Database: foods
# ------------------------------------------------------
# Server version 5.5.40

USE `foods`;

#
# Source for table admin
#

CREATE TABLE `admin` (
  `adminName` varchar(20) NOT NULL,
  `pwd` varchar(20) NOT NULL,
  `sex` int(1) DEFAULT '1',
  `telpone` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`adminName`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

#
# Dumping data for table admin
#

LOCK TABLES `admin` WRITE;
REPLACE INTO `admin` VALUES ('王贤朋','*23AE809DDACAF96AF0F',1,'13407127672');
UNLOCK TABLES;

#
# Source for table food
#

CREATE TABLE `food` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `foodName` varchar(50) DEFAULT NULL,
  `brith` date DEFAULT NULL,
  `bzq` varchar(20) DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `oldPrice` decimal(10,2) DEFAULT NULL,
  `margin` int(11) DEFAULT NULL,
  `saleNumber` int(11) DEFAULT NULL,
  `type` varchar(50) DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `goodResponse` int(10) DEFAULT NULL,
  `img` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`Id`)
) ENGINE=MyISAM AUTO_INCREMENT=28 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

#
# Dumping data for table food
#

LOCK TABLES `food` WRITE;
REPLACE INTO `food` VALUES (1,'番茄炒蛋','2018-06-01','6个月',12,5,98,2,'风味小炒','许多百姓家庭中一道普通的大众菜肴。烹饪方法简单易学，营养搭配合理',0,'fqcd.jpg');
REPLACE INTO `food` VALUES (2,'外婆菜','2017-12-23','6个月',10,3,19,1,'风味小炒','外婆菜又名万菜，是湖南湘西地区一道家常菜',0,'wpc.png');
REPLACE INTO `food` VALUES (3,'烤鱼','2017-04-07','12个月',30,18,30,0,'风味小炒','鱼类经过烤制之后然后进行烹饪的一种方法',0,'ky.bmp');
REPLACE INTO `food` VALUES (4,'麻婆豆腐','2018-04-07','12个月',10,2,0,0,'风味小炒','四川省传统名菜之一，属于川菜',0,'mpdf.jpg');
REPLACE INTO `food` VALUES (5,'肉末茄子','2017-11-26','12个月',14,6,90,0,'风味小炒','以茄子和猪肉为主要食材，大蒜、红椒、香葱、油、黄豆酱、草菇老抽等作为辅料制作而成的家常菜',0,'rmqz.jpg');
REPLACE INTO `food` VALUES (6,'海带汤','2018-03-03','12个月',8,2,5,0,'汤类','富含蛋白质、脂肪、碳水化合物，对人们的身体健康十分有益',0,'hdt.jpg');
REPLACE INTO `food` VALUES (7,'米饭','2020-06-05','36个月',1,0.3,13,0,'主食','中国人日常饮食中的主角之一',0,'mf.png');
REPLACE INTO `food` VALUES (8,'扬州炒饭','2018-06-01','12个月',8,1,64,0,'主食','腹胀、尿失禁患者忌食',0,'cf.png');
REPLACE INTO `food` VALUES (9,'千层饼','2017-11-02','12个月',2,1,0,0,'主食','饼里拌有海苔粉，外面洒上一层芝麻',0,'qcb.jpg');
REPLACE INTO `food` VALUES (10,'面条','2019-12-25','24个月',4.5,2.5,40,0,'主食','即可主食又可快餐的健康保健食品，早已为世界人民所接受与喜爱。',0,'mt.png');
REPLACE INTO `food` VALUES (11,'干锅茶树菇','2017-12-10','10天',12,5,86,0,'风味小炒','这是一道湖南家常菜，菜品口味浓郁鲜香，酸辣适口',0,'ggcsg.png');
REPLACE INTO `food` VALUES (12,'酸辣土豆丝','2017-12-02','10天',6,1,77,0,'风味小炒','一道人见人爱的川菜',0,'sltds.png');
REPLACE INTO `food` VALUES (13,'豆腐萝卜汤','2017-12-05','10天',5,1,34,0,'汤类','主料是南豆腐1块、白萝卜1/2根',0,'dflbt.jpg');
REPLACE INTO `food` VALUES (14,'羊肉萝卜汤','2017-12-06','10天',20,10,0,0,'汤类','常吃可提升气色、滋润肌肤',0,'yrlbt.jpg');
REPLACE INTO `food` VALUES (15,'银耳羹','2017-12-09','10天',7,3,89,0,'汤类','对女性具有很好的嫩肤美容功效',0,'yrt.png');
REPLACE INTO `food` VALUES (16,'冬瓜排骨汤','2017-12-25','10天',18,8,98,0,'汤类','本肴通利之功较强，体质瘦弱者不宜常服。孕妇慎用',0,'dgpgt.png');
REPLACE INTO `food` VALUES (17,'萝卜粉丝汤','2017-12-01','3天',13,2,60,0,'汤类','通过清炖的做法而成',0,'lbfst.png');
REPLACE INTO `food` VALUES (18,'水煮肉片','2018-09-12','3天',25,8,89,0,'风味小炒','因肉片未经划油，以水煮熟故名水煮肉片',0,'szrp.jpg');
REPLACE INTO `food` VALUES (19,'可乐鸡翅','2017-11-12','3天',20,8,100,0,'风味小炒','具有味道鲜美、色泽艳丽、鸡肉嫩滑、咸甜适中的特点。',0,'kljc.jpg');
REPLACE INTO `food` VALUES (20,'花生米','2016-07-15','36个月',2,0.5,7,0,'凉菜','对平衡膳食、改善中国居民的营养与健康状况具有重要作用。',0,'hsm.png');
REPLACE INTO `food` VALUES (21,'凉拌黄瓜','2018-04-15','36个月',2,0.5,55,0,'凉菜','具有美容养颜功效，营养价值丰富',0,'lbhg.jpg');
REPLACE INTO `food` VALUES (22,'皮蛋拌豆腐','2017-10-26','12个月',10,3,3,0,'凉菜','香菜：健胃、透疹、增强免疫力 豆腐：清热泻火、益气、解毒',0,'pdbdf.png');
REPLACE INTO `food` VALUES (23,'凉拌海带丝','2017-06-03','12个月',3,1,94,0,'凉菜','减少放射性疾病，御寒，抗癌防癌',0,'lbhds.png');
REPLACE INTO `food` VALUES (24,'鱼香肉丝','2014-04-15','12个月',13,4,13,0,'风味小炒','相传灵感来自泡椒肉丝，民国年间则是由四川籍厨师创制而成',0,'yxrs.jpg');
REPLACE INTO `food` VALUES (25,'干锅花菜','2017-12-23','12个月',10,3,100,0,'风味小炒','可作为菜品直接上桌食用，各种荤素搭配都可',0,'gghc.png');
REPLACE INTO `food` VALUES (26,'手撕包菜','2017-12-24','6个月',8,1,20,0,'风味小炒','有美白祛斑、预防感冒和胃溃疡等作用',0,'ssbc.png');
UNLOCK TABLES;

#
# Source for table user
#

CREATE TABLE `user` (
  `userName` varchar(50) NOT NULL,
  `pwd` varchar(100) NOT NULL,
  `name` varchar(50) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `telphone` varchar(20) DEFAULT NULL,
  `province` varchar(30) DEFAULT NULL,
  `city` varchar(30) DEFAULT NULL,
  `home` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`userName`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

#
# Dumping data for table user
#

LOCK TABLES `user` WRITE;
REPLACE INTO `user` VALUES ('wxp','*23AE809DDACAF96AF0FD78ED04B6A265E05AA257','王贤朋',22,'13407127672','湖北','武汉','软件园中路');
UNLOCK TABLES;

/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
