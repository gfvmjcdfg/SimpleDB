# SimpleDB

这是一个简单的数据库项目，实现一个简单的数据库。

其中数据库分为五个模块：
TM: 
transaction management 
通过 XID 文件来维护事务的状态，并提供接口供其他模块来查询某个事务的状态


DM: data management
VM: version management
IM: index management
TBM: table management