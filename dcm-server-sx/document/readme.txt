







1、当前采集程序为陕西采集程序
2、测试用采集链路ID:131  GROUPID:101  分发链路:100070




常用SQL:
SELECT * FROM DC_COLL_LINK WHERE DEV_ID = 131;
SELECT * FROM DC_DIST_LINK WHERE DEV_ID = '100070';
SELECT * FROM DC_DEV_CFG WHERE DEV_ID IN ('131', '100070') ORDER BY DEV_ID
SELECT * FROM DC_DIST_TASK WHERE COLL_DEV_ID = 131;
SELECT * FROM DC_DIST_LOG WHERE LINK_ID = 100070;
SELECT * FROM DC_COLL_LOG WHERE LINK_ID = 131

DELETE FROM DC_COLL_LOG WHERE LINK_ID = '131';
DELETE FROM DC_DIST_TASK WHERE COLL_DEV_ID = '131';
DELETE FROM DC_DIST_LOG WHERE LINK_ID = '100070';




20190910需求（康国华提出）：
链路参数datasource新增一个数据源，在现有的基础上增加第三个数据源

1、替换DcmServer.jar采集服务，修改config.properties，将jdbc.dataSourceCount值修改为3，并且新增数据源datasource_3
2、前台替换datadict文件，重启tomcat服务




1、陕西采集支持存储方式有：本地存储(local)，集团文件系统存储(dfs)，该字段对应DC_COLL_LINK.FILE_STORE_TYPE
2、采集成功后续操作添加DC_DIST_TASK规则
   当采集链路DC_COLL_LINK.FMT_FLAG=1表示文件直接分发，添加分发任务表DC_DIST_TASK；
   当采集链路DC_COLL_LINK.FMT_FLAG=2根据关联的分发链路获取链路PARENT_FLAG字段，进行采集格式化
   1.分发链路PARENT_FLAG=0直接添加DC_DIST_TASK
   2.分发链路PARENT_FLAG=1调用SQL_jiekou,格式化后的子文件只会往PARENT_FLAG=1发送


3、分发任务类型有：消息分发(1)，FTP分发(2)，该字段对应DC_DIST_LINK.TSK_TYPE
4、分发文件存储方式有：本地存储(local)，集团文件系统存储(dfs)，该字段对应DC_DIST_LINK.FILE_STORE_TYPE


5、本地直接启停命令，切换到bin目录下
./start.sh "../cfg/" start

6、***注意lib目录下文件引用顺序，dca_client_2.5.0.jar文件引用顺序要放到后面，不然日志会出现使用log4j2.xml打印

7、手动采集、分发， [条件配置]是用来对"列表"按钮作用的，用来对获取的文件列表进行过滤；[使用规则]复选框是对"传送"按钮作用的，用来对传输的文件是否启动重命名、删除源文件等


8、分发任务表异常回收
create or replace procedure RECOVERY_DIST_TASK is
  CURSOR CURSOR_DIST_TASK IS --声明显式游标
    SELECT
		A.SOURCE_ID,
		A.DIST_DEV_ID,
		A.ORI_PATH,
		A.ORI_FILE_NAME,
		A.ORI_FILE_LENGTH,
		A.ORI_FILE_TIME,
		A.LATN_ID,
		A.COLL_DEV_ID,
		A.CREATE_TIME,
		A.STATUS,
		A.RECID,
		A.BATCH_ID
	FROM
		DC_DIST_TASK_ABN A;
     --where CREATE_TIME + 1 / 24 < SYSDATE;--查询1小时前的记录
  ROW_DIST_TASK CURSOR_DIST_TASK%ROWTYPE; --定义游标变量,该变量的类型为基于游标CURSOR_DIST_TASK的记录
BEGIN
  --For 循环
  FOR ROW_DIST_TASK IN CURSOR_DIST_TASK LOOP
    --分发任务回收
    INSERT INTO DC_DIST_TASK
      (SOURCE_ID,
       DIST_DEV_ID,
       ORI_PATH,
       ORI_FILE_NAME,
       ORI_FILE_LENGTH,
       ORI_FILE_TIME,
       LATN_ID,
       COLL_DEV_ID,
       CREATE_TIME,
     STATUS,
       RECID,
     BATCH_ID)
    values
      ( ROW_DIST_TASK.SOURCE_ID,
    ROW_DIST_TASK.DIST_DEV_ID,
    ROW_DIST_TASK.ORI_PATH,
    ROW_DIST_TASK.ORI_FILE_NAME,
    ROW_DIST_TASK.ORI_FILE_LENGTH,
    ROW_DIST_TASK.ORI_FILE_TIME,
    ROW_DIST_TASK.LATN_ID,
    ROW_DIST_TASK.COLL_DEV_ID,
    ROW_DIST_TASK.CREATE_TIME,
    0,
    ROW_DIST_TASK.RECID,
    ROW_DIST_TASK.BATCH_ID);

    --分发任务异常删除
    delete from DC_DIST_TASK_ABN where RECID = ROW_DIST_TASK.RECID;

    --提交事务
    commit;
  END LOOP;

end RECOVERY_DIST_TASK;