#!/bin/sh

if [ $# -lt 3 ]
then
  cmd=`basename $0`
  echo "Error!"
  echo "usage:$cmd create|drop proc_id timeout"
  exit 1
fi

Action=$1
proc_id=$2
time_out=$3
param=$4
restart=$5
user=`whoami`
result=1
source ~/.bash_profile
#检查
check_refresh_process(){
  pid=`ps -fu $user | grep "acWholeRefresh -p $proc_id" | grep -v grep | awk '{print $2}'`

  if [ x"$pid" != x ]
  then
    return 0
  else
    return 1
  fi

}

start_refresh_process(){
  cd $M2DB_HOME/bin
  nohup ./acWholeRefresh -p $proc_id > ../log/acWholeRefresh_$proc_id.log 2>&1 &
  sleep 3
  check_refresh_process
  return $?
}

stop_refresh_process(){
  pids=`ps -fu $user | grep "acWholeRefresh -p $proc_id" | grep -v grep | awk '{print $2}'` 
  for pid in $pids
  do
    kill -9 $pid
  done
}
#创建实例
if [ $Action == "create" ]
then
  cd $M2DB_HOME/bin&& ./check_inst.exp $proc_id 5
  if [ $? -eq 0 ]
  then
    echo "Instance $proc_id already exist!"
    result=0
  else
    cd $M2DB_HOME/bin&& ./create_inst.exp $proc_id $time_out
    cd $M2DB_HOME/bin&& ./check_inst.exp $proc_id 5
    result=$?
  fi
#删除实例
elif [ $Action == "drop" ] 
then
  cd $M2DB_HOME/bin&& ./check_inst.exp $proc_id 5
  if [ $? -ne 0 ]
  then
    echo "Instance $proc_id does not exist!"
    result=0
  else
     userh=`whoami`
     pids=`ps -fu $userh | grep -E "acWholeRefresh -p $proc_id | acIncRefresh" | grep -v grep |awk '{print $2}'`
	 if [ "x${pids}" != "x" ]
	 then 
	    for pid in $pids
		do
			kill -9 $pid
			echo "killed pid[$pid]"
		done
	 fi
	 sleep 1
    cd $M2DB_HOME/bin&& ./drop_inst.exp $proc_id $time_out
    cd $M2DB_HOME/bin&& ./check_inst.exp $proc_id 5
    result=$?
  fi
#刷内存数据
  elif [ $Action == "refresh_mem" ] 
then
  cd $M2DB_HOME/bin&& ./check_inst.exp $proc_id 5
  if [ $? -ne 0 ]
  then
    echo "Instance $proc_id does not exist!"
    result=0
  else
    cd $M2DB_HOME/bin
	pids=`ps -fu $user | grep "cstorm $param" | grep -v grep | awk '{print $2}'`
	if [ "x${pids}" = "x" ]
	then 
		echo "not find $param,Not doing anything"
	else
		for pid in $pids
		do
			echo "refresh pid[$pid]"
			./cmd_trigger -p $pid -m 2
		done
	fi
	result=0
  fi
#检查
 elif [ $Action == "check" ] 
then
  cd $M2DB_HOME/bin&& ./check_inst.exp $proc_id $time_out
  result=$?
  
 #建表删表
elif [ $Action == "cmd_m2db" ]
then
  cd $M2DB_HOME/bin&& ./check_inst.exp $proc_id 5
  if [ $? -ne 0 ]
  then
    echo "Error! Connect to instance $proc_id failed!"
    result=1
  else
    cd $M2DB_HOME/bin&& ./cmd_inst.exp $proc_id $time_out "$param"
    result=$?
  fi
 #导出
elif [ $Action == "export_file" ]
then
  cd $M2DB_HOME/bin&& ./check_inst.exp $proc_id 5
  if [ $? -ne 0 ]
  then
    echo "Error! Connect to instance $proc_id failed!"
    result=1
  else
    #cd $M2DB_HOME/bin&& ./cmd_inst.exp $proc_id $time_out "hbexport -m $proc_id -s all -f $param"
	cd $M2DB_HOME/bin&& ./cmd_inst.exp $proc_id 8 "$param" | grep -E "^_DCACLOG_.*LM_ERROR.*|^Execute failed.*"
    result=$?
  fi
   #导入
elif [ $Action == "input_file" ]
then
  cd $M2DB_HOME/bin&& ./check_inst.exp $proc_id 5
  if [ $? -ne 0 ]
  then
    echo "Error! Connect to instance $proc_id failed!"
    result=1
  else
     #cd $M2DB_HOME/bin&& ./cmd_inst.exp $proc_id $time_out "$param" | grep -E "^_DCACLOG_.*LM_ERROR.*|^Execute failed.*"
      cd $M2DB_HOME/bin&& ./cmd_inst.exp $proc_id 10 "$param" | grep -E "^_DCACLOG_.*LM_ERROR.*|^Execute failed.*|No such file or directory.*" | sed -n '1,10p' | grep -E "^_DCACLOG_.*LM_ERROR.*|^Execute failed.*|No such file or directory.*" 
     if [ $? -eq 0 ]
     then
        result=1
     else
        result=0
     fi

  fi
 #刷新表
elif [ $Action == "refresh" ]
then
  cd $M2DB_HOME/bin&& ./check_inst.exp $proc_id 5
  if [ $? -ne 0 ]
  then
    echo "Error! Connect to instance $proc_id failed!"
    result=1
  else
  
    check_refresh_process

    if [ $? -ne 0 ]
    then 
      echo "acWholeRefresh process does not exist! Start it now!"
	  
	  start_refresh_process
	  
      if [ $? -ne 0 ]
      then
        echo "Start acWholeRefresh process failed!"
        result=1
      else
        cd $M2DB_HOME/bin&& ./refresh_inst.exp $proc_id $time_out "$param"
        result=$?
      fi
    else
	  if [ $restart == '1' ]
	  then
	    echo "Begin restart acWholeRefresh process ..."
	    stop_refresh_process
		
        start_refresh_process
		
        if [ $? -ne 0 ]
        then
          echo "Restart acWholeRefresh process failed!"
          result=1
        else
          cd $M2DB_HOME/bin&& ./refresh_inst.exp $proc_id $time_out "$param"
          result=$?
        fi
	  else
	    cd $M2DB_HOME/bin&& ./refresh_inst.exp $proc_id $time_out "$param"
        result=$?
	  fi
    fi
  fi

else
  echo "Error! Unsurport action!"
  exit 1
fi

if [ $result = "0" ]
then 
  echo "======Deal success!"
  exit 0
else
  echo "======Deal error!"
  exit 1
fi

