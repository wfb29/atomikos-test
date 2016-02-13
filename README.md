# atomikos-test
分布式事务管理

一台机器准备两个mysql实例

1 下载安装第一个实例
http://mirrors.sohu.com/mysql/

mysqld -install
net start mysql
mysql -u root -p

2
安装第二个实例复制一份mysql目录
修改my-default.ini的端口号等信息：
basedir = D:/Program Files/MySQL/MySQL Server
datadir = D:/Program Files/MySQL/MySQL Server/data
port = 3307

运行
mysqld --install mysql2 --defaults-file="D:\Program Files\MySQL\MySQL Server\bin\my-default.ini"
net start mysql2

在两个库分别建立一个数据表：
-- ----------------------------
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account` (
  `UserId` int(11) NOT NULL,
  `Money` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of account
-- ----------------------------
INSERT INTO `account` VALUES ('123', '0');

代码逻辑是实现随机的从1库转账一定金额到2库，或者从2库转账到1库。

在转账过程中，会按照一定概率发生异常，回滚整个事务。
运行一段时间后，查看两个库的金额是否能够对账正确。

