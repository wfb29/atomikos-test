# atomikos-test
分布式事务管理

一台机器准备两个mysql实例
http://mirrors.sohu.com/mysql/

mysqld -install
net start mysql
mysql -u root -p

2
复制一份mysql目录
修改my-default.ini的端口号等信息：
basedir = D:/Program Files/MySQL/MySQL Server
datadir = D:/Program Files/MySQL/MySQL Server/data
port = 3307

运行
mysqld --install mysql2 --defaults-file="D:\Program Files\MySQL\MySQL Server\bin\my-default.ini"
net start mysql2
