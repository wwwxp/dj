#!/bin/sh

PROGNAME=`basename $0`
#auto.sh脚本副本位置
AUTO_SH_EXECPATH=`pwd`
#获取auto.sh脚本真实位置
cd ../../
PROGPATH=`pwd`
who=`whoami`

if [ -e  /etc/profile ]
then
    source /etc/profile
fi

if [ -e  ~/.bashrc ]
then
    source ~/.bashrc
fi

if [ -e  ~/.bash_profile ]
then
    source ~/.bash_profile
fi

cd $PROGPATH

################### JAVA  ##################
##export JAVA_HOME=$PROGPATH/env/jdk
export JAVA_HOME=$JAVA_HOME
export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar:$CLASSPATH

################### rocketmq  ###################
export ROCKETMQ_HOME=$PROGPATH/env/$8/rocketmq

################### FastDFS  ###################
export FASTDFS_HOME=$PROGPATH/env/$8/fastdfs

################### NodeJS###################
export NODEJS_HOME=$PROGPATH/env/$8/dca/node

################### DCAS  ##################
export DCAS_ROOT_HOME=$PROGPATH/env/$8/dca
export REDIS_HOME=$PROGPATH/env/$8/dca/redis
export DCAS_HOME=$PROGPATH/env/$8/dca/DCAS
export DCAM_HOME=$PROGPATH/env/$8/dca/DCAM
export SSHPASS_HOME=$PROGPATH/env/$8/dca/sshpass
export DAEMON_DCA_HOME=$PROGPATH/env/$8/dca/daemon

################### MONITOR  ##################
export MONITOR_HOME=$PROGPATH/env/$8/monitor/monitor

################### MONITOR_COMPONENT  ##################
export MONITOR_COMPONENT_HOME=$PROGPATH/env/$8/monitor/component

################### DMDB  ##################
export DMDB_HOME=$PROGPATH/env/$8/dmdb

################### CLOUDB  ###################
export CLOUDB_HOME=$PROGPATH/env/$8/cloudb

################### ZOOKEEPER  ###################
export ZOOKEEPER_HOME=$PROGPATH/env/$8/zookeeper

################### JSTORM  ##################
export JSTORM_HOME=$PROGPATH/env/$8/jstorm

################### M2DB  ##################
export M2DB_HOME=$PROGPATH/env/$8/m2db
export AC_CONF_ROOT=$M2DB_HOME/config

################### DcLog  ##################
export DCLOG_HOME=$PROGPATH/env/$8/dclog

################### PATH  ##################
export PATH=$JAVA_HOME/bin:$JSTORM_HOME/bin:$FASTDFS_HOME/bin:$NODEJS_HOME/bin:$SSHPASS_HOME/bin:$M2DB_HOME/bin:$REDIS_HOME:$PATH

################### LD_LIBRARY_PATH  ##################
export LD_LIBRARY_PATH=$FASTDFS_HOME/lib:$DCAS_ROOT_HOME/lib:$M2DB_HOME/lib:$NODEJS_HOME/lib:$MONITOR_COMPONENT_HOME/lib:$LD_LIBRARY_PATH

print_usage()
{
  echo -e "Usage: "
  echo -e "  ./$PROGNAME -s <rocketmq|fastdfs|dca|dmdb|monitor|dclog|zookeeper|m2db|jstorm> Failed."
  echo -e "  ./$PROGNAME -e <rocketmq|fastdfs|dca|dmdb|monitor|dclog|zookeeper|m2db|jstorm> Failed."
  echo -e "  ./$PROGNAME -d <rocketmq|fastdfs|dca|dmdb|monitor|dclog|zookeeper|m2db|jstorm> Failed."
}

if [ $# -lt 2 ];
then
	print_usage
	exit 1
else
	while getopts ':s:e:d:c:i:r:t:2:3:4:5:' OPT; do
    case $OPT in
        s)
            CMD="start"
            PARAM="$OPTARG";;
        e)
            CMD="end"
            PARAM="$OPTARG";;
		d)
            CMD="delete"
            PARAM="$OPTARG";;
		c)
			CMD="check"
			PARAM="$OPTARG";;
		i)
			CMD="input"
			PARAM="$OPTARG";;
		r)
			CMD="refresh"
			PARAM="$OPTARG";;
		t)
			CMD="table"
			PARAM="$OPTARG";;
		2)
			ARG="$OPTARG";;
		3)
			ARG2="$OPTARG";;
		4)
			ARG3="$OPTARG";;
		5)
			ARG4="$OPTARG";;
        *)
            echo "please check the command's correctness,Failed."
            print_usage
            exit 1;;
    esac
	done
fi

#================ RocketMq Start And Stop ======================================#
StartNameServ()
{
	cd $PROGPATH
	echo -e "Start Rocketmq NameServ ......"
	cfg=$PROGPATH/conf/rocketmq/$ARG2
	instpath=$ROCKETMQ_HOME/conf/2m-2s-async/$ARG2
	if [ -e $ROCKETMQ_HOME/conf/2m-2s-async/$ARG2 ];
	then
		rm -rf $ROCKETMQ_HOME/conf/2m-2s-async/$ARG2
	fi
	if [ ! -d ${instpath%/*} ];then
		mkdir -p ${instpath%/*}
	fi
	cd $ROCKETMQ_HOME/conf/2m-2s-async
	ln -s $cfg $ARG2

	cd $ROCKETMQ_HOME/bin;
	namesrvpid1=`ps ux |grep NamesrvStartup|grep $instpath |grep -v grep|awk '{print $2}'`
	if [ "-$namesrvpid1" == "-" ];
	then
		nohup sh mqnamesrv -c $instpath > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1 &
		sleep 4
		cat $AUTO_SH_EXECPATH/nohup_sh.out
    else
		echo -e "namesrv is run. "
   	fi
	namesrvpid=`ps ux |grep NamesrvStartup|grep $instpath |grep -v grep|awk '{print $2}'`
	echo -e "Namesrv PID -----> $namesrvpid"
	if [ "-$namesrvpid" != "-" ];
	then
		echo " Success."
	else
		echo " Failed."
	fi
}

StopNameServ()
{
	cd $PROGPATH
	echo -e "Stop Rocketmq NameServ ......"
	instpath=$ROCKETMQ_HOME/conf/2m-2s-async/$ARG2
	cd $ROCKETMQ_HOME/bin;
	namesrvpid=`ps ux | grep -i 'com.apache.rocketmq.namesrv.NamesrvStartup' |grep java |grep $instpath | grep -v grep | awk '{print $2}'`
	echo -e "Namesrv PID -----> $namesrvpid"
	if [ "-$namesrvpid" != "-" ];
	then
		kill -9 $namesrvpid
		sleep 4
	else
		echo -e "NameServ is not running... "
	fi
	namesrvpid=`ps ux |grep NamesrvStartup|grep $instpath |grep -v grep|awk '{print $2}'`
	if [ "-$namesrvpid" != "-" ];
	then
		echo " Failed."
	else
		echo " Success."
	fi
}

StartBroker()
{
	if [ "-$ARG2" != "-" ];
    then
		echo -e "Start Rocketmq Broker ......"
    	cfg=$PROGPATH/conf/rocketmq/$ARG2
		instpath=$ROCKETMQ_HOME/conf/2m-2s-async/$ARG2
		if [ -e $ROCKETMQ_HOME/conf/2m-2s-async/$ARG2 ];
		then
			rm -rf $ROCKETMQ_HOME/conf/2m-2s-async/$ARG2
		fi
		if [ ! -d ${instpath%/*} ];then
		 mkdir -p ${instpath%/*}
		fi
		cd $ROCKETMQ_HOME/conf/2m-2s-async
		ln -s $cfg $ARG2

		cd $ROCKETMQ_HOME/bin;
		brokerpid1=`ps ux|grep BrokerStartup |grep $instpath |grep -v grep|awk '{print $2}'`
		if [ "-$brokerpid1" != "-" ];
		then
			echo " broker is run. "
		else
			nohup sh mqbroker -c $instpath > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1 &
			sleep 10
			cat $AUTO_SH_EXECPATH/nohup_sh.out
		fi
		brokerpid=`ps ux|grep BrokerStartup |grep $instpath |grep -v grep|awk '{print $2}'`
		echo -e "Broker PID -----> $brokerpid"
		if [ "-$brokerpid" != "-" ];
		then
			echo " Success."
		else
			echo " Failed."
		fi
	else
		echo "PARAM is null, Failed."
	fi
}

StopBroker()
{
	if [ "-$ARG2" != "-" ];
    then
		echo -e "Stop Rocketmq Broker ......"
		instpath=$ROCKETMQ_HOME/conf/2m-2s-async/$ARG2
		cd $ROCKETMQ_HOME/bin;
		brokerpid=`ps ux|grep BrokerStartup |grep $instpath |grep -v grep|awk '{print $2}'`
		if [ "-$brokerpid" != "-" ];
		then
			kill -9 ${brokerpid}
			sleep 4
		else
			echo "Broker is not running... "
		fi
		brokerpid=`ps ux|grep BrokerStartup |grep $instpath |grep -v grep|awk '{print $2}'`
		if [ "-$brokerpid" != "-" ];
		then
			echo " Failed."
		else
			echo " Success."
		fi
	else
		echo "PARAM is null, Failed."
	fi
}

StartRocketmq()
{
   if [ "-$ARG" == "-namesrv" ];
     then
          StartNameServ
 	 elif [ "-$ARG" == "-broker" ];
     then
          StartBroker
	 elif [ "-$ARG" == "-all" ];
     then
	      StartNameServ
          StartBroker
	 else
     echo "$ARG is not a supported param Failed.";
   fi
}

StopRocketmq()
{
	if [ "-$ARG" == "-namesrv" ];
     then
          StopNameServ
 	 elif [ "-$ARG" == "-broker" ];
     then
          StopBroker
	 elif [ "-$ARG" == "-all" ];
     then
	       StopNameServ
          StopBroker
	 else
     echo "$ARG is not a supported param Failed.";
   fi
}

#================ RocketMq Start And Stop ======================================#



#================ FastDFS Start And Stop ======================================#
StartTracker()
{
	if [ "-$ARG2" != "-" ];
	then
		cd $PROGPATH
		echo -e "Start FastDFS Tracker ......"
		cfg=$FASTDFS_HOME/etc/fdfs/$ARG2
		if [ -e $FASTDFS_HOME/etc/fdfs/$ARG2 ];
		then
			rm -rf $FASTDFS_HOME/etc/fdfs/$ARG2
		fi
		if [ ! -d ${cfg%/*} ];then
		 mkdir -p ${cfg%/*}
		fi
		cd $FASTDFS_HOME/etc/fdfs/
		ln -s $PROGPATH/conf/fastdfs/$ARG2 $ARG2

		cd $FASTDFS_HOME/bin;
		trackerpid1=`ps ux|grep fdfs_trackerd |grep $FASTDFS_HOME/etc/fdfs/$ARG2 |grep -v grep|awk '{print $2}'`
		if [ "-$trackerpid1" == "-" ];
		then
			$FASTDFS_HOME/bin/fdfs_trackerd $FASTDFS_HOME/etc/fdfs/$ARG2 start > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1
			sleep 4
			cat $AUTO_SH_EXECPATH/nohup_sh.out
		else
			echo "tracker is run. "
		fi

		trackerpid=`ps ux|grep fdfs_trackerd |grep $FASTDFS_HOME/etc/fdfs/$ARG2 |grep -v grep|awk '{print $2}'`
		echo -e "Tracker PID -----> $trackerpid"
		if [ "-$trackerpid" != "-" ];
		then
			echo " Success."
		else
			echo " Failed."
		fi
	else
		echo "PARAM is null, Failed."
	fi

}

StopTracker()
{
	if [ "-$ARG2" != "-" ];
	then
		cd $PROGPATH
		echo -e "Stop FastDFS Tracker ......"

		cd $FASTDFS_HOME/bin;
		trackerpid=`ps ux|grep fdfs_trackerd |grep $FASTDFS_HOME/etc/fdfs/$ARG2 |grep -v grep|awk '{print $2}'`
		if [ "-$trackerpid" != "-" ];
		then
			$FASTDFS_HOME/bin/fdfs_trackerd $FASTDFS_HOME/etc/fdfs/$ARG2 stop > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1
			sleep 4
			cat $AUTO_SH_EXECPATH/nohup_sh.out
		else
			echo "Tracker is not running ..."
		fi

		trackerpid=`ps ux|grep fdfs_trackerd |grep $FASTDFS_HOME/etc/fdfs/$ARG2 |grep -v grep|awk '{print $2}'`
		if [ "-$trackerpid" != "-" ];
		then
			echo " Failed."
		else
			echo " Success."
		fi
	else
		echo "PARAM is null, Failed."
	fi
}

StartStorage()
{
	if [ "-$ARG2" != "-" ];
	then
		cd $PROGPATH
		echo -e "Start FastDFS Storage ......"
		cfg=$FASTDFS_HOME/etc/fdfs/$ARG2
		if [ -e $FASTDFS_HOME/etc/fdfs/$ARG2 ];
		then
			rm -rf $FASTDFS_HOME/etc/fdfs/$ARG2
		fi
		if [ ! -d ${cfg%/*} ];then
		 mkdir -p ${cfg%/*}
		fi

		cd $FASTDFS_HOME/etc/fdfs/
		ln -s $PROGPATH/conf/fastdfs/$ARG2 $ARG2

		cd $FASTDFS_HOME/bin;
		storagepid=`ps ux|grep fdfs_storaged|grep $FASTDFS_HOME/etc/fdfs/$ARG2 |grep -v grep|awk '{print $2}'`
		if [ "-$storagepid" == "-" ];
		then
			$FASTDFS_HOME/bin/fdfs_storaged $FASTDFS_HOME/etc/fdfs/$ARG2 start >$AUTO_SH_EXECPATH/nohup_sh.out 2>&1
			sleep 4
			cat $AUTO_SH_EXECPATH/nohup_sh.out
		else
			echo "storage is run ..."
		fi

		storagepid=`ps ux|grep fdfs_storaged|grep $FASTDFS_HOME/etc/fdfs/$ARG2 |grep -v grep|awk '{print $2}'`
		echo -e "Storage PID -----> $storagepid"
		if [ "-$storagepid" != "-" ];
		then
			echo " Success."
		else
			echo " Failed."
		fi
	else
		echo "PARAM is null, Failed."
	fi
}

StopStorage()
{
	if [ "-$ARG2" != "-" ];
	then
		echo -e "Stop FastDFS Storage ......"
		cd $FASTDFS_HOME/bin;

		storagepid=`ps ux|grep fdfs_storaged |grep $FASTDFS_HOME/etc/fdfs/$ARG2 |grep -v grep|awk '{print $2}'`
		if [ "-$storagepid" != "-" ];
		then
			$FASTDFS_HOME/bin/fdfs_storaged $FASTDFS_HOME/etc/fdfs/$ARG2 stop >$AUTO_SH_EXECPATH/nohup_sh.out 2>&1
			sleep 4
			cat $AUTO_SH_EXECPATH/nohup_sh.out
		else
			echo "Storage is not running ..."
		fi

		storagepid=`ps ux|grep fdfs_storaged |grep $FASTDFS_HOME/etc/fdfs/$ARG2 |grep -v grep|awk '{print $2}'`
		if [ "-$storagepid" != "-" ];
		then
			echo " Failed."
		else
			echo " Success."
		fi
	else
		echo "PARAM is null, Failed."
	fi
}

startNginx()
{
	if [ "-$ARG2" != "-" ];
	then
		echo -e "Stop FastDFS Nginx ......"

		cfg=$FASTDFS_HOME/nginx/conf/$ARG2
		if [ -e $FASTDFS_HOME/nginx/conf/$ARG2 ];
		then
			rm -rf $FASTDFS_HOME/nginx/conf/$ARG2
		fi
		if [ ! -d ${cfg%/*} ];then
		 mkdir -p ${cfg%/*}
		fi
		if [ ! -e ${cfg%/*}/mini.types ];
		then
			cp -r $FASTDFS_HOME/nginx/conf/mime.types ${cfg%/*}
		fi

		cd $FASTDFS_HOME/nginx/conf/
		ln -s $PROGPATH/conf/fastdfs/$ARG2 $ARG2

		cd $FASTDFS_HOME/nginx;
		./monitor.sh $FASTDFS_HOME/nginx $cfg > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1
		if [ $? = 0 ];
		then
			echo " Nginx is running ..."
		else
			cat $AUTO_SH_EXECPATH/nohup_sh.out
			./start.sh $FASTDFS_HOME/nginx $cfg > $AUTO_SH_EXECPATH/nohup_start_sh.out 2>&1
			sleep 2
			cat $AUTO_SH_EXECPATH/nohup_start_sh.out
		fi
		./monitor.sh $FASTDFS_HOME/nginx $cfg
		if [ $? = 0 ];
		then
			echo -e "Success."
		else
			echo -e "Failed."
		fi
	else
		echo "PARAM is null, Failed."
	fi
}

stopNginx()
{
	if [ "-$ARG2" != "-" ];
	then
		echo -e "Stop FastDFS Nginx ......"
		cfg=$FASTDFS_HOME/nginx/conf/$ARG2

		cd $FASTDFS_HOME/nginx;
		./monitor.sh $FASTDFS_HOME/nginx $cfg > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1
		if [ $? = 1 ];
		then
			echo " Nginx is not running ..."
		else
			cat $AUTO_SH_EXECPATH/nohup_sh.out
			./stop.sh $FASTDFS_HOME/nginx $cfg > $AUTO_SH_EXECPATH/nohup_stop_sh.out 2>&1
			sleep 2
			cat $AUTO_SH_EXECPATH/nohup_stop_sh.out
		fi
		./monitor.sh $FASTDFS_HOME/nginx $cfg
		if [ $? = 1 ];
		then
			echo -e "Success."
		else
			echo -e "Failed."
		fi
	else
		echo "PARAM is null, Failed."
	fi
}

StartFastdfs()
{
   if [ "-$ARG" == "-tracker" ];
     then
          StartTracker
   elif [ "-$ARG" == "-storage" ];
     then
          StartStorage
   elif [ "-$ARG" == "-nginx" ];
	 then
		  startNginx
   else
     echo "$ARG is not a supported param Failed.";
   fi
}

StopFastdfs()
{
	if [ "-$ARG" == "-tracker" ];
     then
          StopTracker
   elif [ "-$ARG" == "-storage" ];
     then
          StopStorage
   elif [ "-$ARG" == "-nginx" ];
     then
          stopNginx
   else
     echo "$ARG is not a supported param Failed.";
   fi
}

#================ FastDFS Start And Stop ======================================#




#================ DCAS Start And Stop ======================================#

#================ DCAS Start And Stop ======================================#
StartDCAM()
{
	if [ "-$ARG2" != "-" ] && [ "-$ARG4" != "-" ];
	then
		echo -e "start dcamServer ......";
		cfg=$DCAM_HOME/config
		if [ -e $cfg/$ARG2 ];
		then
			rm -rf $cfg/$ARG2
		fi
		if [ ! -d $cfg/$ARG2 ];then
			mkdir -p $cfg/$ARG2
		fi	
		cd $cfg
		ln -s $PROGPATH/conf/dca/dcam/$ARG2/* $ARG2
		
		echo -e "PM2 Name -----> $ARG4"
		echo -e "PATH -----> $PATH"
		echo -e "LD_LIBRARY_PATH -----> $LD_LIBRARY_PATH"
		
		cd $DCAM_HOME;
		dcampid=`../pm2/bin/pm2 list | grep $ARG4| awk '{print $8}'`
		if [ "-$dcampid" != "-" ];
		then
			../pm2/bin/pm2 delete $ARG4 2>&1
			sleep 1
		fi
		../pm2/bin/pm2 -f -n $ARG4 start app.js -- $cfg/$ARG2/app.js $cfg/$ARG2/log.js > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1
		sleep 5
		cat $AUTO_SH_EXECPATH/nohup_sh.out
		
		dcamstatus=`../pm2/bin/pm2 show $ARG4|grep status |awk '{print $4}'`
		echo "DCAM Status -----> $dcamstatus"
		if [ "-$dcamstatus" == "-online" ];
		then
			echo " Success."
		else
			echo " Failed."
		fi
	else
		echo "PARAM is null, Failed."
	fi
}

StopDCAM()
{
	if [ "-$ARG2" != "-" ] && [ "-$ARG4" != "-" ];
	then
		echo -e "Stop dcamServer ......"		
		echo -e "PM2 Name -----> $ARG4"
		cd $DCAM_HOME;
		../pm2/bin/pm2 delete $ARG4 > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1
		sleep 4
		cat $AUTO_SH_EXECPATH/nohup_sh.out
		
		dcamstatus=`../pm2/bin/pm2 show $ARG4|grep status |awk '{print $4}'`
		if [ "-$dcamstatus" == "-" ];
		then
			echo " Success."
		else
			echo " Failed."
		fi
	else
		echo "PARAM is null, Failed."
	fi
}

StartDCAS()
{
	if [ "-$ARG2" != "-" ] && [ "-$ARG4" != "-" ];
	then
		echo -e "start dcasServer ......";
		cfg=$DCAS_HOME/config
		if [ -e $cfg/$ARG2 ];
		then
			rm -rf $cfg/$ARG2
		fi
		if [ ! -d $cfg/$ARG2 ];then
			mkdir -p $cfg/$ARG2
		fi	
		cd $cfg
		ln -s $PROGPATH/conf/dca/dcas/$ARG2/* $ARG2
		
		echo "PM2 Name -----> $ARG4"
		cd $DCAS_HOME; 
		
		dcaspid=`../pm2/bin/pm2 list | grep $ARG4| awk '{print $8}'`
		if [ "-$dcaspid" != "-" ];
		then
			../pm2/bin/pm2 delete $ARG4 2>&1
			sleep 1
		fi
		
		../pm2/bin/pm2 -f -n $ARG4 start index.js -- $cfg/$ARG2/app.js $cfg/$ARG2/log.js > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1
		sleep 4
		cat $AUTO_SH_EXECPATH/nohup_sh.out
		
		dcasstatus=`../pm2/bin/pm2 show $ARG4|grep status |awk '{print $4}'`
		echo "DCAS Status -----> $dcasstatus"
		if [ "-$dcasstatus" == "-online" ];
		then
			echo " Success."
		else
			echo " Failed."
		fi
	else
		echo "PARAM is null, Failed."
	fi
}

StopDCAS()
{
	if [ "-$ARG2" != "-" ] && [ "-$ARG4" != "-" ];
	then
		echo -e "Stop dcasServer ......"
		cfg=$DCAS_HOME/config
		echo -e "PM2 Name -----> $ARG4"
		
		cd $DCAS_HOME; 
		../pm2/bin/pm2 delete $ARG4 > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1
		sleep 4
		cat $AUTO_SH_EXECPATH/nohup_sh.out
		
		dcasstatus=`../pm2/bin/pm2 show $ARG4|grep status |awk '{print $4}'`
		if [ "-$dcasstatus" == "-" ];
		then
			echo " Success."
		else
			echo " Failed."
		fi
	else
		echo "PARAM is null, Failed."
	fi
}

StartRedis()
{
	if [ "-$ARG2" != "-" ];
	then
		echo -e "Start redisServer ......"
		cfg=$REDIS_HOME/$ARG2
		if [ -e $cfg ];
		then
			rm -rf $cfg
		fi
		if [ ! -d ${cfg%/*} ];
		then
			mkdir -p ${cfg%/*}
		fi	
		cd $REDIS_HOME
		ln -s $PROGPATH/conf/dca/redis/$ARG2 $ARG2
		echo " Success."
	else
		echo "PARAM is null, Failed."
	fi
}




StopRedis()
{
	if [ "-$ARG2" != "-" ];
	then
		echo -e "Stop redisServer ......"
		echo -e "Redis PORT -----> $ARG2"
		redispid=$ARG2
		pid=`ps ux |grep redis-server|grep -v grep|grep $redispid|awk '{print $2}'`
		if [ "-$pid" == "-" ];
		then
			echo " Redis is not running ..."
		else
			kill -9 $pid
			
			newpid=`ps ux |grep redis-server|grep -v grep|grep $redispid|awk '{print $2}'`
			if [ "-$newpid" == "-" ];
			then
				echo " Success."
			else 
				echo " Failed."
			fi
		fi
	else
		echo "PARAM is null, Failed."
	fi
}

StartDaemon()
{
	echo -e "start dcaDaemon ......";
	if [ -e $DAEMON_DCA_HOME/config.js ];
	then
		rm -rf $DAEMON_DCA_HOME/config.js
	fi
	if [ -L $DAEMON_DCA_HOME/config.js ];
	then
		rm -rf $DAEMON_DCA_HOME/config.js
	fi
	cd $DAEMON_DCA_HOME
	ln -s $PROGPATH/conf/dca/daemon/config.js config.js
	
	##create daemon log path
	logpath=`cat $PROGPATH/conf/dca/daemon/config.js | grep customBaseDir | cut -d "'" -f 2 | cut -d "'" -f -1`
	echo -e "daemon logpath ---> $logpath"
	if [ "-$logpath" != "-" ];
	then
		mkdir -p $logpath
	fi
	
	daemonName=`cat $PROGPATH/conf/dca/daemon/config.js | grep appName | cut -d "'" -f 2 | cut -d "'" -f -1`
	echo -e "daemon daemonName ---> $daemonName"
	
	#daemon程序实例已经存在，先删掉
	daemonpid=`../pm2/bin/pm2 list | grep $daemonName | awk '{print $8}'`
	if [ "-$daemonpid" != "-" ];
	then
		../pm2/bin/pm2 delete $daemonName 2>&1
		sleep 1
	fi
	
	../pm2/bin/pm2 -f -n $daemonName start daemonDCA.js > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1
	sleep 4
	cat $AUTO_SH_EXECPATH/nohup_sh.out
	#dcadaemonid=`ps ux | grep node | grep $DAEMON_DCA_HOME/daemonDCA.js | grep -v debug-port |awk '{print $2}'`
	dcadaemonid=`ps ux | grep node | grep $DAEMON_DCA_HOME/daemon | grep -v debug-port |awk '{print $2}'`
	echo -e "DCA DAEMON PID -----> $dcadaemonid"
	if [ "-$dcadaemonid" != "-" ];
	then
		echo " Success."
	else
		echo " Failed."
	fi
}

StopDaemon()
{
	echo -e "Stop dcasServer ......"
	cd $DAEMON_DCA_HOME; 
	daemonName=`cat $PROGPATH/conf/dca/daemon/config.js | grep appName | cut -d "'" -f 2 | cut -d "'" -f -1`
	echo -e "DCA daemon AppName ---> $daemonName"
	../pm2/bin/pm2 delete $daemonName > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1
	sleep 4
	cat $AUTO_SH_EXECPATH/nohup_sh.out
	dcadaemonid=`ps ux | grep node | grep $DAEMON_DCA_HOME/daemonDCA.js |grep -v grep|awk '{print $2}'`
	if [ "-$dcadaemonid" == "-" ];
	then
		echo " Success."
	else
		echo " Failed."
	fi
}

##启动增量刷新
StartRedisIncRefresh()
{
	echo -e "start redisIncRefresh ......"	
	if [ -e $DCAS_ROOT_HOME/etc/$ARG2 ];
	then
		rm -rf $DCAS_ROOT_HOME/etc/$ARG2
	fi
	
	finalPath=$DCAS_ROOT_HOME/etc/$ARG2
	if [ ! -d ${finalPath%/*} ];
	then
		mkdir -p ${finalPath%/*}
	fi
	ln -s $PROGPATH/conf/dca/$ARG2 $DCAS_ROOT_HOME/etc/$ARG2
	
	#Create log path
	logPath=`cat $PROGPATH/conf/dca/$ARG2 | grep "<log" | grep "path" | awk -F 'path="' '{print $2}' | awk -F '"' '{print $1}'`
	if [ ! -e $logPath ];
	then
		echo -e "create log path ---> $logPath"
		mkdir -p $logPath
	fi
	
	#Create inerface path
	interfacepath=`cat $PROGPATH/conf/dca/$ARG2 | grep "<interface" | grep "path" | awk -F 'path="' '{print $2}' | awk -F '"' '{print $1}'`
	if [[ "-$interfacepath" != "-" ]] && [[ ! -e $interfacepath ]];
	then
		echo -e "create interface path ---> $interfacepath"
		mkdir -p $interfacepath
	fi
	
	cd $DCAS_ROOT_HOME/bin
	nohup ./redisIncRefresh $DCAS_ROOT_HOME/etc/$ARG2 > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1 &
	
	sleep 4
	cat $AUTO_SH_EXECPATH/nohup_sh.out
	redisIncid=`ps ux | grep redisIncRefresh | grep $DCAS_ROOT_HOME/etc/$ARG2 | grep -v debug-port |awk '{print $2}'`
	echo -e "DCA RedisIncRefresh PID -----> $redisIncid"
	if [ "-$redisIncid" != "-" ];
	then
		echo " Success."
	else
		echo " Failed."
	fi
}

##启动全量刷新
StartRedisWholeRefresh()
{
	echo -e "start redisWholeRefresh ......"
	if [ -e $DCAS_ROOT_HOME/etc/$ARG2 ];
	then
		rm -rf $DCAS_ROOT_HOME/etc/$ARG2
	fi
	finalPath=$DCAS_ROOT_HOME/etc/$ARG2
	if [ ! -d ${finalPath%/*} ];then
		mkdir -p ${finalPath%/*}
	fi	
	ln -s $PROGPATH/conf/dca/$ARG2 $DCAS_ROOT_HOME/etc/$ARG2
	
	#创建目录
	logPath=`cat $PROGPATH/conf/dca/$ARG2 | grep "path" | awk -F 'path="' '{print $2}' | awk -F '"' '{print $1}'`
	if [ ! -e $logPath ];
	then
		echo -e "create log path ---> $logPath"
		mkdir -p $logPath
	fi

	cd $DCAS_ROOT_HOME/bin
	nohup ./redisWholeRefresh $DCAS_ROOT_HOME/etc/$ARG2 > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1 &
	sleep 4
	cat $AUTO_SH_EXECPATH/nohup_sh.out
	redisWholeid=`ps ux | grep redisWholeRefresh | grep $DCAS_ROOT_HOME/etc/$ARG2 | grep -v debug-port |awk '{print $2}'`
	echo -e "DCA RedisWholeRefresh PID -----> $redisWholeid"
	if [ "-$redisWholeid" != "-" ];
	then
		echo " Success."
	else
		echo " Failed."
	fi
}

##启动全量校验
StartRedisWholeCheck()
{
	echo -e "start redisWholeCheck ......"
	if [ -e $DCAS_ROOT_HOME/etc/$ARG2 ];
	then
		rm -rf $DCAS_ROOT_HOME/etc/$ARG2
	fi
	finalPath=$DCAS_ROOT_HOME/etc/$ARG2
	if [ ! -d ${finalPath%/*} ];then
		mkdir -p ${finalPath%/*}
	fi	
	ln -s $PROGPATH/conf/dca/$ARG2 $DCAS_ROOT_HOME/etc/$ARG2
	
	#Create log path
	logPath=`cat $PROGPATH/conf/dca/$ARG2 | grep "<log" | grep "path" | awk -F 'path="' '{print $2}' | awk -F '"' '{print $1}'`
	if [ ! -e $logPath ];
	then
		echo -e "create log path ---> $logPath"
		mkdir -p $logPath
	fi

	cd $DCAS_ROOT_HOME/bin
	nohup ./redisWholeCheck $DCAS_ROOT_HOME/etc/$ARG2 > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1 &
	
	sleep 4
	cat $AUTO_SH_EXECPATH/nohup_sh.out
	redisWholeid=`ps ux | grep redisWholeCheck | grep $DCAS_ROOT_HOME/etc/$ARG2 | grep -v debug-port |awk '{print $2}'`
	echo -e "DCA RedisWholeCheck PID -----> $redisWholeid"
	if [ "-$redisWholeid" != "-" ];
	then
		echo " Success."
	else
		echo " Failed."
	fi
}

##启动增量稽核
StartRedisIncCheck()
{
	echo -e "start redisIncCheck ......"
	if [ -e $DCAS_ROOT_HOME/etc/$ARG2 ];
	then
		rm -rf $DCAS_ROOT_HOME/etc/$ARG2
	fi
	finalPath=$DCAS_ROOT_HOME/etc/$ARG2
	if [ ! -d ${finalPath%/*} ];
	then
		mkdir -p ${finalPath%/*}
	fi	
	ln -s $PROGPATH/conf/dca/$ARG2 $DCAS_ROOT_HOME/etc/$ARG2
	
	#Create log path
	logPath=`cat $PROGPATH/conf/dca/$ARG2 | grep "<log" | grep "path" | awk -F 'path="' '{print $2}' | awk -F '"' '{print $1}'`
	if [ ! -e $logPath ];
	then
		echo -e "create log path ---> $logPath"
		mkdir -p $logPath
	fi
	
	#Create inerface path
	interfacepath=`cat $PROGPATH/conf/dca/$ARG2 | grep "<interface" | grep "path" | awk -F 'path="' '{print $2}' | awk -F '"' '{print $1}'`
	if [[ "-$interfacepath" != "-" ]] && [[ ! -e $interfacepath ]];
	then
		echo -e "create interface path ---> $interfacepath"
		mkdir -p $interfacepath
	fi
	
	cd $DCAS_ROOT_HOME/bin
	nohup ./redisIncCheck $DCAS_ROOT_HOME/etc/$ARG2 > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1 &
	
	sleep 4
	cat $AUTO_SH_EXECPATH/nohup_sh.out
	redisIncid=`ps ux | grep redisIncCheck | grep $DCAS_ROOT_HOME/etc/$ARG2 | grep -v debug-port |awk '{print $2}'`
	echo -e "DCA RedisIncCheck PID -----> $redisIncid"
	if [ "-$redisIncid" != "-" ];
	then
		echo " Success."
	else
		echo " Failed."
	fi
}

##启动修复程序
StartRedisRevise()
{
	echo -e "start redisRevise ......"
	if [ -e $DCAS_ROOT_HOME/etc/$ARG2 ];
	then
		rm -rf $DCAS_ROOT_HOME/etc/$ARG2
	fi
	finalPath=$DCAS_ROOT_HOME/etc/$ARG2
	if [ ! -d ${finalPath%/*} ];then
		mkdir -p ${finalPath%/*}
	fi	
	ln -s $PROGPATH/conf/dca/$ARG2 $DCAS_ROOT_HOME/etc/$ARG2
	
	#Create log path
	logPath=`cat $PROGPATH/conf/dca/$ARG2 | grep "path" | awk -F 'path="' '{print $2}' | awk -F '"' '{print $1}'`
	if [ ! -e $logPath ];
	then
		echo -e "create log path ---> $logPath"
		mkdir -p $logPath
	fi
	
	cd $DCAS_ROOT_HOME/bin
	nohup ./redisRevise $DCAS_ROOT_HOME/etc/$ARG2 > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1 &
	
	sleep 4
	cat $AUTO_SH_EXECPATH/nohup_sh.out
	redisReviseid=`ps ux | grep redisRevise | grep $DCAS_ROOT_HOME/etc/$ARG2 | grep -v debug-port |awk '{print $2}'`
	echo -e "DCA RedisRevise PID -----> $redisReviseid"
	if [ "-$redisReviseid" != "-" ];
	then
		echo " Success."
	else
		echo " Failed."
	fi
}

#停止增量刷新
StopRedisIncRefresh()
{
	if [ "-$ARG2" != "-" ];
    then
		echo -e "Stop redisIncRefresh ......"
		instpath=$DCAS_ROOT_HOME/etc/$ARG2
		redisIncid=`ps ux | grep redisIncRefresh | grep $instpath |grep -v grep|awk '{print $2}'`
		echo -e "redisIncRefresh PID ---> $redisIncid"
		if [ "-$redisIncid" != "-" ];
		then
			kill -9 ${redisIncid}
			sleep 4
		else
			echo "RedisIncRefresh is not running... "
		fi
		redisIncid=`ps ux | grep redisIncRefresh | grep $instpath |grep -v grep|awk '{print $2}'`
		if [ "-$redisIncid" != "-" ];
		then
			echo " Failed."
		else
			echo " Success."
		fi
	else
		echo "PARAM is null, Failed."
	fi
}

#停止全量刷新
StopRedisWholeRefresh()
{
	if [ "-$ARG2" != "-" ];
    then
		echo -e "Stop redisWholeRefresh ......"
		instpath=$DCAS_ROOT_HOME/etc/$ARG2
		redisIncid=`ps ux | grep redisWholeRefresh | grep $instpath |grep -v grep|awk '{print $2}'`
		echo -e "RedisWholeRefresh PID ---> $redisIncid"
		if [ "-$redisIncid" != "-" ];
		then
			kill -9 ${redisIncid}
			sleep 4
		else
			echo "RedisWholeRefresh is not running... "
		fi
		redisIncid=`ps ux | grep redisWholeRefresh | grep $instpath |grep -v grep|awk '{print $2}'`
		if [ "-$redisIncid" != "-" ];
		then
			echo " Failed."
		else
			echo " Success."
		fi
	else
		echo "PARAM is null, Failed."
	fi
}

#停止全量校验
StopRedisWholeCheck()
{
	if [ "-$ARG2" != "-" ];
    then
		echo -e "Stop redisWholeCheck ......"
		instpath=$DCAS_ROOT_HOME/etc/$ARG2
		redisIncid=`ps ux | grep redisWholeCheck | grep $instpath |grep -v grep|awk '{print $2}'`
		echo -e "RedisWholeCheck PID ---> $redisIncid"
		if [ "-$redisIncid" != "-" ];
		then
			kill -9 ${redisIncid}
			sleep 4
		else
			echo "RedisWholeCheck is not running... "
		fi
		redisIncid=`ps ux | grep redisWholeCheck | grep $instpath |grep -v grep|awk '{print $2}'`
		if [ "-$redisIncid" != "-" ];
		then
			echo " Failed."
		else
			echo " Success."
		fi
	else
		echo "PARAM is null, Failed."
	fi
}

#停止修改程序
StopRedisRevise()
{
	if [ "-$ARG2" != "-" ];
    then
		echo -e "Stop redisRevise ......"
		instpath=$DCAS_ROOT_HOME/etc/$ARG2
		redisIncid=`ps ux | grep redisRevise | grep $instpath |grep -v grep|awk '{print $2}'`
		echo -e "RedisRevise PID ---> $redisIncid"
		if [ "-$redisIncid" != "-" ];
		then
			kill -9 ${redisIncid}
			sleep 4
		else
			echo "RedisRevise is not running... "
		fi
		redisIncid=`ps ux | grep redisRevise | grep $instpath |grep -v grep|awk '{print $2}'`
		if [ "-$redisIncid" != "-" ];
		then
			echo " Failed."
		else
			echo " Success."
		fi
	else
		echo "PARAM is null, Failed."
	fi
}

#停止增量校验
StopRedisIncCheck()
{
	if [ "-$ARG2" != "-" ];
    then
		echo -e "Stop redisIncCheck ......"
		instpath=$DCAS_ROOT_HOME/etc/$ARG2
		redisIncid=`ps ux | grep redisIncCheck | grep $instpath |grep -v grep|awk '{print $2}'`
		echo -e "redisIncCheck PID ---> $redisIncid"
		if [ "-$redisIncid" != "-" ];
		then
			kill -9 ${redisIncid}
			sleep 4
		else
			echo "redisIncCheck is not running... "
		fi
		redisIncid=`ps ux | grep redisIncCheck | grep $instpath |grep -v grep|awk '{print $2}'`
		if [ "-$redisIncid" != "-" ];
		then
			echo " Failed."
		else
			echo " Success."
		fi
	else
		echo "PARAM is null, Failed."
	fi
}

CheckDCAM()
{
	if [ "-$ARG2" != "-" ] && [ "-$ARG4" != "-" ];
	then
		cd $DCAM_HOME; 		
		dcampid=`../pm2/bin/pm2 list | grep $ARG4| awk '{print $8}'`
		echo "$dcampid"
	else
		echo "PARAM is null, Failed."
	fi
}

CheckDCAS()
{
	if [ "-$ARG2" != "-" ] && [ "-$ARG4" != "-" ];
	then
		cd $DCAS_HOME; 		
		dcaspid=`../pm2/bin/pm2 list | grep $ARG4| awk '{print $8}'`
		echo "$dcaspid"
	else
		echo "PARAM is null, Failed."
	fi
}

StartDCA()
{
	if [ "-$ARG" == "-dcas" ];
    then
         StartDCAS
	elif [ "-$ARG" == "-dcam" ];
    then 
        StartDCAM
	elif [ "-$ARG" == "-redis" ];
    then 
        StartRedis
	elif [ "-$ARG" == "-daemon" ];
    then 
        StartDaemon
	elif [ "-$ARG" == "-redisIncRefresh" ];
	then
		StartRedisIncRefresh
	elif [ "-$ARG" == "-redisWholeRefresh" ];
	then
		StartRedisWholeRefresh
	elif [ "-$ARG" == "-redisWholeCheck" ];
	then
		StartRedisWholeCheck
	elif [ "-$ARG" == "-redisIncCheck" ];
	then
		StartRedisIncCheck
	elif [ "-$ARG" == "-redisRevise" ];
	then
		StartRedisRevise
	else
     echo "$ARG is not a supported param Failed.";
	fi
}

StopDCA()
{
	if [ "-$ARG" == "-dcas" ];
    then
        StopDCAS
	elif [ "-$ARG" == "-dcam" ];
    then 
        StopDCAM
	elif [ "-$ARG" == "-redis" ];
    then 
        StopRedis
	elif [ "-$ARG" == "-daemon" ];
    then 
        StopDaemon
	elif [ "-$ARG" == "-redisIncRefresh" ];
	then
		StopRedisIncRefresh
	elif [ "-$ARG" == "-redisWholeRefresh" ];
	then
		StopRedisWholeRefresh
	elif [ "-$ARG" == "-redisWholeCheck" ];
	then
		StopRedisWholeCheck
	elif [ "-$ARG" == "-redisIncCheck" ];
	then
		StopRedisIncCheck
	elif [ "-$ARG" == "-redisRevise" ];
	then
		StopRedisRevise
	else
		echo "$ARG is not a supported param Failed.";
	fi
}

CheckDCA()
{
	if [ "-$ARG" == "-dcam" ];
    then
        CheckDCAM
	elif [ "-$ARG" == "-dcas" ];
	then
		CheckDCAS
	else
		echo "$ARG is not a supported param Failed.";
	fi
}
#================ DCAS Start And Stop ======================================#


#================ Monitor Start And Stop======================================#
StartES()
{
	echo "Start elasticsearch ...... "
	if [ -e $MONITOR_HOME/elasticsearch/config/elasticsearch.yml ];
	then
		rm -rf $MONITOR_HOME/elasticsearch/config/elasticsearch.yml
	fi
	cd $MONITOR_HOME/elasticsearch/config
	cp -af $PROGPATH/conf/monitor/elasticsearch/elasticsearch.yml elasticsearch.yml
	echo "elasticsearch.yml init ---【success】"

	cd $MONITOR_HOME/elasticsearch/template
	cp -af -af $PROGPATH/conf/monitor/elasticsearch/es_init.sh es_init.sh
	chmod a+x es_init.sh
	./es_init.sh > /dev/null 2>&1
	echo "es_init.sh init ---【success】 "

	cd $MONITOR_HOME/elasticsearch/bin
	chmod a+x elasticsearch	
	./elasticsearch -d
	sleep 4
	espid=`ps ux |grep elasticsearch|grep -v grep|awk '{print $2}'`
	echo -e "Es PID -----> $espid"
	if [ "-$espid" != "-" ];
	then
		echo " Success."
	else
		echo " Failed."
	fi
}

StopES()
{
	echo -e "Stop elasticsearch ......"
	espid=`ps ux |grep elasticsearch|grep -v grep|awk '{print $2}'`
	if [ "-$espid" == "-" ];
	then
		echo " Success."
	else
		kill -9 $espid
	fi
	espid2=`ps ux |grep elasticsearch|grep -v grep|awk '{print $2}'`
	if [ "-$espid2" == "-" ];
	then
		echo " Success."
	else 
		echo "Failed."
	fi
}

StartProgrammerMonitor()
{
	if [ "-$ARG2" != "-" ];
	then
		echo -e "Start monitor ......"
		if [ -e $MONITOR_HOME/conf/$ARG2 ];
		then
			rm -rf $MONITOR_HOME/conf/$ARG2
		fi
		
		## delete exists programmer first
		StopProgrammerMonitor
		
		if [ ! -L $MONITOR_COMPONENT_HOME/dcfile/node/bin/pm2 ];
		then
			rm -rf $MONITOR_COMPONENT_HOME/dcfile/node/bin/pm2
			ln -s $MONITOR_COMPONENT_HOME/dcfile/node/lib/node_modules/pm2/bin/pm2 $MONITOR_COMPONENT_HOME/dcfile/node/bin/pm2
			echo -e "Create symbolic link pm2 ...... "
		fi
		if [ ! -L $MONITOR_COMPONENT_HOME/dcfile/node/bin/pm2-dev ];
		then
			rm -rf $MONITOR_COMPONENT_HOME/dcfile/node/bin/pm2-dev
			ln -s $MONITOR_COMPONENT_HOME/dcfile/node/lib/node_modules/pm2/bin/pm2-dev $MONITOR_COMPONENT_HOME/dcfile/node/bin/pm2-dev
			echo -e "Create symbolic link pm2-dev ...... "
		fi
		
		cd $MONITOR_HOME/conf
		ln -s $PROGPATH/conf/monitor/$ARG2 $ARG2
		cd $MONITOR_HOME/bin
		chmod a+x *
		./start.sh > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1
		sleep 4
		if [ "-$?" == "-0" ];
		then
			cat $AUTO_SH_EXECPATH/nohup_sh.out
			echo -e "Start monitor component ......"
			if [ -e $MONITOR_COMPONENT_HOME/collector/collector.cfg ];
			then
				rm -rf $MONITOR_COMPONENT_HOME/collector/collector.cfg
			fi
			if [ -e $MONITOR_COMPONENT_HOME/dca/config/app.js ];
			then
				rm -rf $MONITOR_COMPONENT_HOME/dca/config/app.js
			fi
			if [ -e $MONITOR_COMPONENT_HOME/dcfile/etc/config.js ];
			then
				rm -rf $MONITOR_COMPONENT_HOME/dcfile/etc/config.js
			fi
			if [ -e $MONITOR_COMPONENT_HOME/dcq/socket.properties ];
			then
				rm -rf $MONITOR_COMPONENT_HOME/dcq/socket.properties
			fi
			cd $MONITOR_COMPONENT_HOME
			ln -s $PROGPATH/conf/monitor/collector.cfg collector/collector.cfg
			ln -s $PROGPATH/conf/monitor/app.js dca/config/app.js
			ln -s $PROGPATH/conf/monitor/config.js dcfile/etc/config.js
			ln -s $PROGPATH/conf/monitor/socket.properties dcq/socket.properties

			url=`cat $PROGPATH/conf/monitor/dmdb.properties | grep "url" | cut -d "=" -f 2-`
			port=`cat $PROGPATH/conf/monitor/dmdb.properties | grep "port" | cut -d "=" -f 2-`
			#echo -e "DMDB URL -----> $url"
			#echo -e "DMDB PORT -----> $port"
			
			cd $MONITOR_COMPONENT_HOME
			chmod a+x *
			./start.sh $MONITOR_COMPONENT_HOME $url $port
			sleep 4
			if [ "-$?" != "-0" ];
			then 
				cd $MONITOR_HOME/bin;
				./stop.sh
				sleep 4
			else
				echo "Success."
			fi
		else
			echo "Failed."
		fi
	else
		echo "PARAM is null, Failed."
	fi
}

StopProgrammerMonitor()
{
	echo -e "Stop monitor ......"
	cd $MONITOR_HOME/bin;
	./stop.sh > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1
	sleep 4
	cat $AUTO_SH_EXECPATH/nohup_sh.out
	cd $MONITOR_COMPONENT_HOME;
	./stop.sh > $AUTO_SH_EXECPATH/nohup_component_sh.out 2>&1
	sleep 4
	cat $AUTO_SH_EXECPATH/nohup_component_sh.out
	echo "Success."
}

StartMonitor()
{
	if [ "-$ARG" == "-monitor" ];
    then
		StartProgrammerMonitor
	elif [ "-$ARG" == "-elasticsearch" ];
    then 
        StartES
	else
		echo "$ARG is not a supported param Failed.";
	fi
}

StopMonitor()
{
	if [ "-$ARG" == "-monitor" ];
    then
        StopProgrammerMonitor
	elif [ "-$ARG" == "-elasticsearch" ];
    then 
        StopES
	else
		echo "$ARG is not a supported param Failed.";
	fi
}
#================ Monitor Start And Stop======================================#


#========================= Dmdb Start And Stop ===============================#
StartDMDB()
{
	 
	echo -e "Start dmdb......." 
	if [ -e $DMDB_HOME/conf/mdb.conf ];
	then
		rm -rf $DMDB_HOME/conf/mdb.conf
	fi
	cd $DMDB_HOME/conf
	ln -s $PROGPATH/conf/dmdb/mdb.conf mdb.conf
	 
	cd $DMDB_HOME
	chmod +x *.sh
	./boot.sh >$AUTO_SH_EXECPATH/nohup_sh.out 2>&1
	sleep 3
	cat $AUTO_SH_EXECPATH/nohup_sh.out
	
	#检查是否已经正常启动#
	./check.sh >$AUTO_SH_EXECPATH/nohup_check_sh.out 2>&1
	if [ $? = 0 ];
	then
		echo " Success."
	else
		cat $AUTO_SH_EXECPATH/nohup_check_sh.out
		./stop.sh
		echo " Failed."
	fi
	 
}

StopDMDB()
{ 
	echo -e "Stop dmdb ......"
	
	cd $DMDB_HOME
	
	chmod +x *.sh
	./stop.sh > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1
	sleep 3
	cat $AUTO_SH_EXECPATH/nohup_sh.out
	 
	echo " Success."
	 
}

DeleteDmdb()
{
	if [ "-$ARG" != "-instance_pattern" -a "-$ARG" != "-route_pattern" -a "-$ARG" != "-main_pattern" ]
	then
		echo "$ARG is not a supported param Failed.";
		return 1
	fi
	
	if [ "-$ARG2" != "-" ]
	then
		echo -e "Delete dmdb $ARG data ......"
		
		cd $DMDB_HOME
		
		instpath=$DMDB_HOME/cfg/$ARG2
		chmod +x *.sh
		
		./delete_inst_data.sh $ARG $instpath > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1
		if [ $? = 0 ];
		then
			echo " Success."
		else
			cat $AUTO_SH_EXECPATH/nohup_sh.out
			echo " Failed."
		fi
	else
		echo "PARAM is null, Failed."
	fi
}
#========================= Dmdb Start And Stop ===============================#


#========================zookeeper Start And Stop ============================#
#$ARG4 ---> ClusterCode
StartZookeeper()
{
	echo -e "Start zookeeper......"
	if [ -e $ZOOKEEPER_HOME/conf/zoo.cfg ];
	then
		rm -rf $ZOOKEEPER_HOME/conf/zoo.cfg
	fi
	cd $ZOOKEEPER_HOME/conf
	ln -s $PROGPATH/conf/zookeeper/zoo.cfg zoo.cfg
	
	cd $ZOOKEEPER_HOME/bin;
	nohup ./zkServer.sh start > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1 &
	sleep 4
	cat $AUTO_SH_EXECPATH/nohup_sh.out
	
	zkpid=`ps ux|grep QuorumPeerMain|grep $ZOOKEEPER_HOME|grep -v grep|awk '{print $2}'`
	echo -e "Zookeeper PID -----> $zkpid"
	if [ "-$zkpid" != "-" ];
	then
		echo "Success."
	else 
		echo "Failed."
	fi
}

StopZookeeper()
{
	echo -e "Stop Zookeeper ......"
	cd $ZOOKEEPER_HOME/bin;
	zookeeperpid=`ps ux|grep QuorumPeerMain|grep $ZOOKEEPER_HOME|grep -v grep|awk '{print $2}'`
	echo -e "zookeeperpid ----> $zookeeperpid "
	if [ "-$zookeeperpid" == "-" ];
	then
		echo "Zookeeper not running ..."
	else 
		nohup ./zkServer.sh stop > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1 &
		sleep 4
		cat $AUTO_SH_EXECPATH/nohup_sh.out
		zookeeperpid=`ps ux|grep QuorumPeerMain|grep $ZOOKEEPER_HOME|grep -v grep|awk '{print $2}'`
		if [ "-$zookeeperpid" != "-" ];
		then
			echo " Failed."
		else
			echo " Success."
		fi
	fi
}
#========================zookeeper Start And Stop ============================#


#================Jstorm Start And Stop =========================#

StartJstorm()
{
   if [ "-$ARG" == "-nimbus" ];
     then
          StartNimbus
 	 elif [ "-$ARG" == "-supervisor" ];
     then 
          StartSupervisor
	 else
     echo "$ARG is not a supported param Failed.";
   fi
}

StopJstorm()
{
	if [ "-$ARG" == "-nimbus" ];
     then
          StopNimbus
 	 elif [ "-$ARG" == "-supervisor" ];
     then 
          StopSupervisor
	 else
     echo "$ARG is not a supported param Failed.";
   fi
}

StartNimbus()
{
	echo -e "Start NimbusServer ......"
	if [ -e $JSTORM_HOME/conf/storm.yaml ];
	then
		rm -rf $JSTORM_HOME/conf/storm.yaml
	fi
	if [ -e $JSTORM_HOME/conf/jstorm.logback.xml ];
	then
		rm -rf $JSTORM_HOME/conf/jstorm.logback.xml
	fi
	
	cd $JSTORM_HOME/conf
	ln -s $PROGPATH/conf/jstorm/storm.yaml storm.yaml
	ln -s $PROGPATH/conf/jstorm/jstorm.logback.xml jstorm.logback.xml
	
	cd $PROGPATH
	nimbuspid=`ps ux |grep NimbusServer|grep $PROGPATH|grep -v grep|awk '{print $2}'`
	if [ "-$nimbuspid" != "-" ];
	then
		echo " Success, process already running . can't run a new process."
	else
		##判断DATA目录是否需要删除，如果需要删除给将DATA目录数据清空
		if [ "-$ARG4" == "-1" ];
		then
			datapath=`cat $PROGPATH/conf/jstorm/storm.yaml | grep storm.local.dir | awk -F '%' '{print $3}' | awk -F '"' '{print $1}'`
			echo -e "jstorm DATA PATH ---> $JSTORM_HOME/$datapath"
			if [ -e $JSTORM_HOME/$datapath ];
			then
				rm -rf $JSTORM_HOME/$datapath
			fi
		fi
	
		cd $JSTORM_HOME/bin; 
		nohup ./jstorm nimbus > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1 &
		sleep 5
		cat $AUTO_SH_EXECPATH/nohup_sh.out
		
		nimbuspid=`ps ux |grep NimbusServer|grep $PROGPATH|grep -v grep|awk '{print $2}'`
		echo -e "Nimbus PID -----> $nimbuspid"
		if [ "-$nimbuspid" != "-" ];
		then
			echo " Success."
		else
			echo " Failed."
		fi
	fi
}

StopNimbus()
{
	echo -e "Stop NimbusServer ......"
    echo -e "Stop NumbusServer Version ---> $ARG3"
	#nimbuspid=`jps |grep NimbusServer|grep -v grep|awk '{print $1}'`
    nimbuspid=`ps ux |grep NimbusServer | grep $ARG3/jstorm |grep -v grep|awk '{print $2}'`
	echo -e "Nimbus PID -----> $nimbuspid"
	if [ "-$nimbuspid" != "-" ];
	then
		kill -9 $nimbuspid
		echo " Success."
	else
		echo " Success."
		echo "pleace check if the process exists."
	fi
}

StartSupervisor()
{
	echo -e "Start Supervisor ......"
	if [ -e $JSTORM_HOME/conf/storm.yaml ];
	then
		rm -rf $JSTORM_HOME/conf/storm.yaml
	fi
	if [ -e $JSTORM_HOME/conf/jstorm.logback.xml ];
	then
		rm -rf $JSTORM_HOME/conf/jstorm.logback.xml
	fi
	
	cd $JSTORM_HOME/conf
	ln -s $PROGPATH/conf/jstorm/storm.yaml storm.yaml
	ln -s $PROGPATH/conf/jstorm/jstorm.logback.xml jstorm.logback.xml
	
	cd $PROGPATH
	supervisorpid=`ps ux |grep Supervisor|grep $PROGPATH|grep -v grep|awk '{print $2}'`
	if [ "-$supervisorpid" != "-" ];
	then
		echo " Success, process already running . can't run a new process."
	else
		##判断DATA目录是否需要删除，如果需要删除给将DATA目录数据清空
		if [ "-$ARG4" == "-1" ];
		then
			datapath=`cat $PROGPATH/conf/jstorm/storm.yaml | grep storm.local.dir | awk -F '%' '{print $3}' | awk -F '"' '{print $1}'`
			echo -e "jstorm DATA PATH ---> $JSTORM_HOME/$datapath"
			if [ -e $JSTORM_HOME/$datapath ];
			then
				rm -rf $JSTORM_HOME/$datapath
			fi
		fi
	
		cd $JSTORM_HOME/bin; 
		nohup ./jstorm supervisor > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1 &
		sleep 10
		cat $AUTO_SH_EXECPATH/nohup_sh.out
		supervisorpid=`ps ux |grep Supervisor|grep $PROGPATH|grep -v grep|awk '{print $2}'`
		echo -e "Supervisor PID -----> $supervisorpid"
		if [ "-$supervisorpid" != "-" ];
		then
			echo " Success."
		else
			echo " Failed."
		fi
	fi
}

StopSupervisor()
{
	echo -e "Stop Supervisor ......"
    echo -e "Stop Supservisor Version ---> $ARG3"

	workerarr=(`ps ux |grep Worker | grep $ARG3/jstorm |grep -v grep|awk '{print $2}'`)
    echo -e "Stop Worker PID ---> $workerarr"

	supervisorparr=(`ps ux |grep Supervisor | grep $ARG3/jstorm |grep -v grep|awk '{print $2}'`)
    echo -e "Stop Supervisor PID ---> $supervisorparr"

	pidarr=(`ps ux | grep cstorm | grep $ARG3/jstorm |grep -v grep|awk '{print $2}'`)
    echo -e "Stop Cstorm PID ---> $pidarr"
	for i in ${supervisorparr[@]}
	  do
	  kill -9 $i
	  done
	  for i in ${workerarr[@]}
	  do
	  kill -9 $i
	  done
	  for i in ${pidarr[@]}
	  do
	  kill -9 $i
	  done
	  echo " Success."
}

#================Jstorm Start And Stop =========================#


#================ Cloudb Start And Stop ======================================#
StartCloudb() {
		echo -e " Cloudb Start ..."
		cfg=$CLOUDB_HOME/conf       
		#DMDB_HOME=$PROGPATH/env/$8/cloudb/conf
		#instpath=$cfg/$ARG2
		
		if [ -e $cfg/server.xml ];
		then
			rm -f $cfg/server.xml
		fi
		
		if [ -e $cfg/server.dtd ];
		then
			rm -f $cfg/server.dtd
		fi
		
		if [ -e $cfg/rule.dtd ];
		then
			rm -f $cfg/rule.dtd
		fi
		
		if [ -e $cfg/rule.xml ];
		then
			rm -f $cfg/rule.xml
		fi
		
		if [ -e $cfg/schema.dtd ];
		then
			rm -f $cfg/schema.dtd
		fi
		
		if [ -e $cfg/schema.xml ];
		then
			rm -f $cfg/schema.xml
		fi
		
		cd $cfg
		ln -s $PROGPATH/conf/cloudb/$ARG2/server.xml $cfg/server.xml     #PROGPATH=`pwd`
		ln -s $PROGPATH/conf/cloudb/$ARG2/server.dtd $cfg/server.dtd
		# cd $CLOUDB_HOME/bin ./cloudb start
		#.$CLOUDB_HOME/bin/cloudb start
		cd $CLOUDB_HOME/bin/;./cloudb start
}

StopCloudb() {
		echo -e "Cloudb Stop ..."
		cfg=$CLOUDB_HOME/conf       
		#DMDB_HOME=$PROGPATH/env/$8/cloudb/conf
		#instpath=$cfg/$ARG2
		if [ -e $cfg/server.xml ];
		then
			rm -f $cfg/server.xml
		fi
		
		if [ -e $cfg/server.dtd ];
		then
			rm -f $cfg/server.dtd
		fi
		
		if [ -e $cfg/rule.dtd ];
		then
			rm -f $cfg/rule.dtd
		fi
		
		if [ -e $cfg/rule.xml ];
		then
			rm -f $cfg/rule.xml
		fi
		
		if [ -e $cfg/schema.dtd ];
		then
			rm -f $cfg/schema.dtd
		fi
		
		if [ -e $cfg/schema.xml ];
		then
			rm -f $cfg/schema.xml
		fi
		
		cd $cfg
		ln -s $PROGPATH/conf/cloudb/$ARG2/server.xml $cfg/server.xml     #PROGPATH=`pwd`
		ln -s $PROGPATH/conf/cloudb/$ARG2/server.dtd $cfg/server.dtd
		# cd $CLOUDB_HOME/bin ./cloudb start
		#.$CLOUDB_HOME/bin/cloudb start
		cd $CLOUDB_HOME/bin/;./cloudb stop
}
#================Cloudb Start And Stop =========================#

#================DCLog Start And Stop =========================#
StartDclog()
{
	echo -e "Start dclog ......"
	#if [ -e $DCLOG_HOME/conf/agent.cfg ];
	#then
	#	rm -rf $DCLOG_HOME/conf/agent.cfg
	#fi
	#if [ -e $DCLOG_HOME/conf/analysis.cfg ];
	#then
	#	rm -rf $DCLOG_HOME/conf/analysis.cfg
	#fi
	#if [ -e $DCLOG_HOME/conf/flume.cfg ];
	#then
	#	rm -rf $DCLOG_HOME/conf/flume.cfg
	#fi
	if [ -e $DCLOG_HOME/conf/dclog.cfg ];
	then
		rm -rf $DCLOG_HOME/conf/dclog.cfg
	fi
	 
	cd $DCLOG_HOME/conf;
  #  ln -s $PROGPATH/conf/dclog/agent.cfg agent.cfg
  #	ln -s $PROGPATH/conf/dclog/analysis.cfg analysis.cfg
  #	ln -s $PROGPATH/conf/dclog/flume.cfg flume.cfg
	ln -s $PROGPATH/conf/dclog/dclog.cfg dclog.cfg
	 
	cd $DCLOG_HOME/bin;
	./start.sh $ARG > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1
	echo "Start dclog result ---> $?"
	if [ $? -eq 0 ]
	then
	    echo "Success."
	else
		echo "Failed."
	fi
	sleep 2
	cat $AUTO_SH_EXECPATH/nohup_sh.out
}

StopDclog()
{
	echo -e "Stop dclog ......"
	cd $DCLOG_HOME/bin;
	./stop.sh > $AUTO_SH_EXECPATH/nohup_sh.out 2>&1
	sleep 4
	cat $AUTO_SH_EXECPATH/nohup_sh.out
	echo " Success. command finished."
}
#================DCLog Start And Stop =========================#


#================M2DB Start And Stop =========================#
StartM2db()
{
	echo -e "M2DB Start ......"
	if [ -e $M2DB_HOME/config/CACHE_CONFIG.cfg ];
	then
		rm -rf $M2DB_HOME/config/CACHE_CONFIG.cfg
	fi
	if [ -e $M2DB_HOME/config/FREQUENCY_CONFIG.cfg ];
	then
		rm -rf $M2DB_HOME/config/FREQUENCY_CONFIG.cfg
	fi
	if [ -e $M2DB_HOME/config/TNS_NAME.cfg ];
	then
		rm -rf $M2DB_HOME/config/TNS_NAME.cfg
	fi
	
	##创建数据保存目录
	ipcKey=`cat $PROGPATH/conf/m2db/CACHE_CONFIG.cfg | grep "<memory.all.ipc_key_name>" | cut -d ">" -f 2 | cut -d "<" -f -1`
	for key in $ipcKey
	do
		echo -e "create ---> ${key%/*}"
		mkdir -p ${key%/*}
	done
	dataStore=`cat $PROGPATH/conf/m2db/CACHE_CONFIG.cfg | grep "<datastore_dir>" | cut -d ">" -f 2 | cut -d "<" -f -1`
	for dataPath in $dataStore
	do
		echo -e "create ---> $dataPath"
		mkdir -p $dataPath
	done
	flashBack=`cat $PROGPATH/conf/m2db/CACHE_CONFIG.cfg | grep "<flashback_path>" | cut -d ">" -f 2 | cut -d "<" -f -1`
	for flashPath in $flashBack
	do
		echo -e "create ---> $flashPath"
		mkdir -p $flashPath
	done

	
	cd $M2DB_HOME/config
    ln -s $PROGPATH/conf/m2db/CACHE_CONFIG.cfg CACHE_CONFIG.cfg
	ln -s $PROGPATH/conf/m2db/FREQUENCY_CONFIG.cfg FREQUENCY_CONFIG.cfg
	ln -s $PROGPATH/conf/m2db/TNS_NAME.cfg TNS_NAME.cfg
	cd $M2DB_HOME/bin;
	./m2db_inst.sh create $ARG2 35
	sleep 1 
}

StopM2db()
{
	echo -e "M2DB Stop ......"
	cd $M2DB_HOME/bin;
	./m2db_inst.sh drop $ARG2 6
}

CheckM2db()
{
	echo -e "M2DB Status Check ......"
	cd $M2DB_HOME/bin;
	./m2db_inst.sh check $ARG2 5
}

M2dbInput()
{
   echo -e "M2DB input table ......"
   cd $M2DB_HOME/bin;
   #paramVar=`echo $ARG3 | sed 's/|/ /g'`
   #./hbimport -m ${paramVar[1]} -s all -f data/${paramVar[0]} 
   ./m2db_inst.sh input_file $ARG 8 "execfile -f data/$ARG2"
   #./hbimport -m $ARG -s all -f data/$ARG2
}

M2dbRefreshMem()
{
   echo -e "M2DB refresh Mem ......"
   cd $M2DB_HOME/bin;
   ./m2db_inst.sh refresh_mem $ARG 5 "$ARG2"
}
 
M2dbRefreshTable()
{ 
   echo -e "M2DB refresh table ......" 
   cd $M2DB_HOME/bin;
   echo -e "command:./m2db_inst.sh ${ARG%|*} ${ARG#*|} 5 $ARG2 $ARG4"
   ./m2db_inst.sh ${ARG%|*} ${ARG#*|} 5 "$ARG2" $ARG4
}
#================M2DB Start And Stop =========================#



#================ process method =========================#

StartProcess()
{
	if [ "-$PARAM" == "-rocketmq" ];
	then
	    StartRocketmq
	elif [ "-$PARAM" == "-fastdfs" ];
	then 
	    StartFastdfs
	elif [ "-$PARAM" == "-cloudb" ];
	then
		StartCloudb
	elif [ "-$PARAM" == "-dca" ];
	then 
	    StartDCA
	elif [ "-$PARAM" == "-monitor" ];
	then 
	    StartMonitor
	elif [ "-$PARAM" == "-dmdb" ];
	then 
		StartDMDB
	elif [ "-$PARAM" == "-zookeeper" ];
	then 
		StartZookeeper
	elif [ "-$PARAM" == "-jstorm" ];
	then 
		StartJstorm
	elif [ "-$PARAM" == "-m2db" ];
	then 
		StartM2db
	elif [ "-$PARAM" == "-dclog" ];
	then 	
		StartDclog
	else
		echo "$PARAM is not a supported param";
	fi
}

StopProcess()
{
	if [ "-$PARAM" == "-rocketmq" ];
	then
	  StopRocketmq
	elif [ "-$PARAM" == "-fastdfs" ];
	then 
	  StopFastdfs
	elif [ "-$PARAM" == "-cloudb" ]
	then
	  StopCloudb
	elif [ "-$PARAM" == "-dca" ];
	then 
	  StopDCA
	elif [ "-$PARAM" == "-monitor" ];
	then 
	  StopMonitor
	elif [ "-$PARAM" == "-dmdb" ];
	then 
		StopDMDB
	elif [ "-$PARAM" == "-zookeeper" ];
	then 
		StopZookeeper
	elif [ "-$PARAM" == "-jstorm" ];
	then 
		StopJstorm
	elif [ "-$PARAM" == "-m2db" ];
	then 
		StopM2db
	elif [ "-$PARAM" == "-dclog" ];
	then 	
		StopDclog
	else
		echo "$PARAM is not a supported param";
	fi
}

checkProcess()
{
	if [ "-$PARAM" == "-m2db" ];
	then
		CheckM2db
	elif [ "-$PARAM" == "-dca" ];
	then
		CheckDCA
	else
		echo "$PARAM is not a supported param";
	fi
}

delProcess()
{
	if [ "-$PARAM" == "-dmdb" ];
	then
	  DeleteDmdb
	else
		echo "$PARAM is not a supported param";
	fi
}

if [ "-${CMD}" == "-start" ];
then
    StartProcess
elif [ "-${CMD}" == "-end" ];
  then
    StopProcess
elif [ "-${CMD}" == "-check" ];
  then
    checkProcess
elif [ "-${CMD}" == "-delete" ];
  then
    delProcess
elif [ "-${CMD}" == "-input" ];
  then 
	M2dbInput
elif [ "-${CMD}" == "-refresh" ];
  then 
	M2dbRefreshMem
elif [ "-${CMD}" == "-table" ];
  then 
    M2dbRefreshTable
else
	print_usage
fi