# u8server
U8Server——U8SDK服务器端（统一渠道SDK接入用户中心和支付中心）
主要包括两部分。
1.管理后台(游戏、渠道配置、用户查询、订单查询下载、统计分析)
2.SDK接口(登陆支付)

# 技术选型
1.jdk:jdk1.7.0_79
2.构建工具:Gradle3.5 
3.架构:Struts2+Spring+Hibernate4+jsp+Jquery
4.数据库:Mysql

# 部署注意事项
1.数据库需要开启时间，存储过程由事件触发

# 后记
工作期间，对这个项目进行了一些优化，包括：
1.c3p0连接池更换成druid连接池 
2018-05-22 11:47:18,080 WARN [com.mchange.v2.async.ThreadPoolAsynchronousRunner] - <com.mchange.v2.async.ThreadPoolAsynchronousRunner$DeadlockDetector@32018e3a -- APPARENT DEADLOCK!!! Complete Status:
    Managed Threads: 3
    Active Threads: 3
    Active Tasks:

com.mchange.v2.resourcepool.TimeoutException: A client timed out while waiting to acquire a resource from com.mchange.v2.resourcepool.BasicResourcePool@57c5e777 -- timeout at awaitAvailable()
查了一些资料 大概意思是说c3p0在处理高并发请求时会造成线程死锁，导致资源耗尽，服务启动失败。趁此机会赶紧换成高大上的druid，并配置了监听器WebStatFilter，可以查看数据库的并发，sql执行情况等等。注意druid的版本(推荐使用1.0.11，1.0.11以上好像无法使用,论坛也没有回复)。

2.后台新增订单下载
之前使用XSSFWorkbook，通过读取模板样式下载excel，筛选7000+记录导出用了5min左右，40000的记录等了一个小时也没完成。之后查阅资料进行优化，放弃读取模板，使用默认样式，同时使用SXSSFWorkbook，100000+的记录1min导出。

3.后台新增文件上传
后台上传游戏强更包和版本号，通过版本号判断是否需要强更。如果需要强更，直接链接到强更包下载地址。

4.同步更新缓存
后台读取的是数据库数据，sdk读取的是缓存的数据。当后台更改了配置信息，需要重启服务，加载缓存生效。后续后台修改数据同步到缓存。

5.新增用户角色信息
之前后台只有用户信息，没有用户角色信息。sdk这边刚好有用户创建角色和等级提升的接口，存在每天的日志表中。创建了用户角色表，从日志表中提取角色数据和更新数据。

6.修复部分渠道无法登陆
每个渠道都有一个单独的类来处理登陆认证。这个类对象是通过反射生成，并放入Map缓存中(只有一个对象)。渠道参数都是配置在数据库中。之前同事在接入渠道的时候，将这些参数写在了类的全局变量中，多线程下调用导致参数错乱，登陆验证失败。

7.支付回调接口没有同步
判断订单状态是否完成，如果未完成，执行业务逻辑，并修改订单状态。如果已完成，不处理。这种读写没有分离在多线程访问下很容易出现问题。(正常情况下不会出现，业内人士可以抓到请求通过Jmeter并发很多请求)

