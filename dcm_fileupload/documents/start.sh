#!/bin/sh
usage()
{
	echo "you can run like this:"
	echo "start.sh start -- start DCM_SERVER"
	echo "start.sh stop --stop DCM_SERVER"
	echo "start.sh check --chech DCM_SERVER state"
}

if [ $# -lt 1 ];then
 usage
 exit -2
fi

#DCM_SERVER程序
DCM_SERVER=fileupload-0.0.1-SNAPSHOT.jar
#DCM_SERVER后台进程
DCM_SERVER_PROCESS=FileUpload

currPath=`pwd`
if [ $2 = "start" ];then
	for file in `ls -a ../lib/* | sort -r`
	do
		jars=$jars:$file
	done	
	echo -e "depend jars ---> $jars"
#nohup ${JAVA_HOME}/bin/java -Xms1024M -Xmx1024M -cp $1:../plugin/*:../lib/* com.tydic.dcm.DcmSystem $DCM_SERVER > ../log/dcm_start.log 2>&1 &
nohup ${JAVA_HOME}/bin/java -Xms1024M -Xmx1024M -jar $1:../plugin/*$jars -Dspring.config.location=$1:../config/application.properties com.tydic.bp.FileuploadApplication $DCM_SERVER > ../log/fileupload_start.log 2>&1 &
	sleep 4
#	cat ../log/dcm_start.log
#	rm -rf ../log/dcm_start.log
	pid=`ps ux|grep $DCM_SERVER_PROCESS | grep $1 | grep -v grep|awk '{print $2}'`
	if [ "-$pid" != "-" ];
	then
		echo "DCM PID ---> $pid"
		echo "Success"
	else 
		echo "Failed."
	fi
elif [ $2 = "stop" ];then
	dcmPid=`ps ux|grep $DCM_SERVER_PROCESS | grep $1 | grep -v grep|awk '{print $2}'`
	echo -e "DCM PID -----> $dcmPid"
	if [ "-$dcmPid" != "-" ];
	then
		kill -9 $dcmPid
		lastPid=`ps ux|grep $DCM_SERVER_PROCESS | grep $1 | grep -v grep|awk '{print $2}'`
		if [ "-$lastPid" != "-" ];
			then
				echo "Failed"
			else
				echo "Success"
		fi
	else 
		echo "Success"
	fi
elif [ $2 = "check" ];then
	pid=`ps ux|grep $DCM_SERVER_PROCESS | grep $1 | grep -v grep|awk '{print $2}'`
	if [ "-$pid" != "-" ];
	then
		echo "Exists"
		echo "run $pid"
	else 
		echo "Not Exists"
	fi
fi
