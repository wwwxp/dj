#!/bin/sh

#任何不正确的命令都直接退出
set -o errexit

#检查脚本参数是否正确
pre(){
	echo "params Failed"
}

if [ $# -lt 2 ];then
 pre
 exit -2
fi

#业务类型(zk,nimbus,supervisor,rocketmq,np,rb 等)
TYPE=$1

#组件完整目录
DEST_FILE_PATH=$2

#组件是否需要安装JDK
NEED_JDK=$3

#本机BASE_PATH基本目录[基本环境使用]
DEST_BASE_PATH=`pwd`

#env本地目录[基本环境使用]
DEST_ENV_PATH=$DEST_BASE_PATH/env

#本地环境目录是否存在
if [ ! -d $DEST_BASE_PATH ];then
 mkdir -p $DEST_BASE_PATH
fi

#本地jdk目录不存在，
if [ ! -d $DEST_ENV_PATH ];then
 mkdir -p $DEST_ENV_PATH
fi
source ~/.bash_profile 

##JAVA_VERSION=`java -version 2>&1 |awk 'NR==1{ gsub(/"/,""); print $3 }'`
##检查基础环境，目前就只需要检查jdk环境
##-o $TYPE = "dca" -o $TYPE = "fastdfs" 
##echo -e "TYPE ---> $TYPE"
##echo -e "PATH ---> $DEST_FILE_PATH"
##echo -e "NEED_JDK ---> $NEED_JDK"
if [ $NEED_JDK = '1' ];then	
	#判断jdk是否已经解压，如果没有解压，则解压
	if [ ! -d $DEST_ENV_PATH/jdk ];then
		echo -e "unzip [JDK] -------------------- Start"
		unzip -o -q $DEST_ENV_PATH/jdk.zip -d $DEST_ENV_PATH
		chmod -R a+x $DEST_ENV_PATH/jdk
		echo -e "unzip [JDK] -------------------- Success"
		 
	    echo "export JAVA_HOME=$DEST_ENV_PATH/jdk" >> ~/.bash_profile
	    echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> ~/.bash_profile
	    echo "export CLASSPATH=.:\$JAVA_HOME/lib/dt.jar:\$JAVA_HOME/lib/tools.jar" >> ~/.bash_profile
	    echo "source jdk_home env success."
		 
	fi
fi

#echo "get ["${TYPE}"] -------------------- Start"
##判断目标版本路径是否存在
##通过截取并获得路径，去掉右斜杠的字符串：/public/aa.txt 得到 /public
DEST_PATH=${DEST_FILE_PATH%/*}
if [ ! -d $DEST_PATH ];then
 mkdir -p $DEST_PATH
fi

#解压版本包并赋权限
echo "unzip ["${TYPE}"] -------------------- Start"
unzip -o -q $DEST_FILE_PATH -d $DEST_PATH
rm -rf $DEST_FILE_PATH
chmod -R a+x  $DEST_PATH/$TYPE
echo "unzip ["${TYPE}"] -------------------- Success"

#fastDFS部署需要Nginx环境变量
#if [ $TYPE = "fastdfs" ];then
#	#echo -e "DEST_ENV_PATH -----> $DEST_ENV_PATH"
#	if [ ! -d $DEST_ENV_PATH/fsCommon ];then
#		mkdir -p $DEST_ENV_PATH/fsCommon
#	fi
#	if [ ! -d $DEST_ENV_PATH/fsCommon/lib ];then
#		#echo -e "copy fastdfs nginx lib directories -------------------Start"
#		cp -r $DEST_ENV_PATH/$DEPLOY_VERSION/$TYPE/lib $DEST_ENV_PATH/fsCommon/lib
#		rst=`echo $LD_LIBRARY_PATH | grep "$DEST_ENV_PATH/fsCommon/lib" | wc -l`
#		#echo -e "Lib Rst -----> $rst"
#		if [ $rst = 0 ];then
#			echo "export LD_LIBRARY_PATH=$DEST_ENV_PATH/fsCommon/lib:\$LD_LIBRARY_PATH" >> ~/.bash_profile
#		fi
#		#echo -e "copy fastdfs nginx lib directories -------------------Success"
#	fi
#	if [ ! -d $DEST_ENV_PATH/fsCommon/include ];then
#		#echo -e "copy fastdfs nginx include directories-------------------Start"
#		cp -r $DEST_ENV_PATH/$DEPLOY_VERSION/$TYPE/include $DEST_ENV_PATH/fsCommon
#		#echo -e "copy fastdfs nginx include directories-------------------Success"
#	fi
#fi
#source ~/.bash_profile

#dcas部署需要设置PM环境变量
#if [ $TYPE = "dcas" ];then	
#	echo -e "DEST_ENV_PATH ----> $DEST_ENV_PATH"
#	echo -e "DEST_ENV_PATH ----> $DEST_ENV_PATH"
#	if [ ! -d $DEST_ENV_PATH/node ];then
#		echo -e "install [NODE] -----------------------------Start"
#		cp -r $DEST_ENV_PATH/$DEPLOY_VERSION/dcas/node $DEST_ENV_PATH
#		chmod a+x $DEST_ENV_PATH/node/bin
#		echo "NODE Start ............."
#		rst=`echo $PATH | grep "$DEST_ENV_PATH/node/bin" | wc -l`
#		echo "NODE  rst ---> $rst"
#		echo "NODE end ............."
#		if [ $rst = 0 ];then
#			   ##echo "export NODE_HOME=$DEST_ENV_PATH/node" >> ~/.bash_profile
#			   echo "export PATH=$DEST_ENV_PATH/node/bin:\$PATH" >> ~/.bash_profile
#			   echo "export LD_LIBRARY_PATH=$DEST_ENV_PATH/node/lib:\$LD_LIBRARY_PATH" >> ~/.bash_profile
#			   echo "source node env success."
#		fi
#	fi
#	if [ ! -d $DEST_ENV_PATH/pm2 ];then
#		echo -e "install [PM2] -----------------------------Start"
#		cp -r $DEST_ENV_PATH/$DEPLOY_VERSION/dcas/pm2 $DEST_ENV_PATH
#		chmod a+x $DEST_ENV_PATH/pm2/bin
#		echo "deploy PATH ---> $PATH"
#		rst=`echo $PATH | grep "$DEST_ENV_PATH/pm2/bin" | wc -l`
#		echo "PM2 rst ------> $rst"
#		if [ $rst = 0 ];then
#			   ##echo "export PM2_HOME=$DEST_ENV_PATH/pm2" >> ~/.bash_profile
#			   echo "export PATH=$DEST_ENV_PATH/pm2/bin:\$PATH" >> ~/.bash_profile
#			   echo "source pm2 env success."
#		fi
#	fi
#fi
#source ~/.bash_profile 
exit 0
