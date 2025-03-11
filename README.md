# SimpleDB

-- 这是一个简单的数据库项目，实现一个简单的数据库。

其中数据库分为五个模块：

## TM: transaction management 

通过 XID 文件来维护事务的状态，并提供接口供其他模块来查询某个事务的状态

## DM: data management

直接管理db文件与日志文件。

对于数据的管理，主要有以下职责

1. 分页管理db文件，并提供对db文件的缓存查询从而加快速度
2. 管理日志文件，在出现错误时通过日志文件进行恢复
3. 将db文件进行抽象从而方便上层模块调用，并提供缓存

因此，这一模块就是上层模块和文件系统之间的一个抽象层，向下直接读写文件，向上提供数据的包装；另外就是日志功能。

## VM: version management

## IM: index management

## TBM: table management