#!/bin/sh

#任何不正确的命令都直接退出
set -o errexit

#检查脚本参数是否正确
pre(){
	echo "sync.sh params error..."
}

if test $# -lt 5 ;then
	pre
	exit -1
fi
#bussiness目录
BUSSINESS_DIV=$1
#配置文件的目录名
CONF_DIV=$2

CPATH=`pwd`
#包类型，决定上传到哪个目录里面
PACKAGE_TYPE=$3

#包名称 OCS_V1.0.0.tar.gz
FILE_NAME=$4

#上一个版本的名称 OCS_V0.1.0.tar.gz
LAST_VERSION_FILE_NAME=$5

TAR_UNZIP_COMMOND="unzip -qo"
TAR_ZIP_COMMOND="zip -qr"
TAR_UNZIP_D_C="-d"
#文件后缀.tar.gz or .zip
FILE_EXTENSION=".${FILE_NAME##*.}"

if [ "$FILE_EXTENSION" == ".gz" ]; then
    FILE_EXTENSION=".tar.gz"
	TAR_UNZIP_COMMOND="tar -xf"
	TAR_UNZIP_D_C="-C"
	TAR_ZIP_COMMOND="tar -zcf"
fi

#当前版本
#不带后缀名的包名称 OCS_V1.0.0
NO_SUFF_FILE_NAME=${FILE_NAME%$FILE_EXTENSION}

#有可能当前版本和上一个版本后缀名不一样， 所以上一个版本也要判断
FILE_LAST_EXTENSION=".${LAST_VERSION_FILE_NAME##*.}"
if [ "$FILE_LAST_EXTENSION" == ".gz" ]; then
    FILE_LAST_EXTENSION=".tar.gz" 
fi

#不带后缀名的上一个版本包名称 OCS_V0.1.1
NO_SUFF_LAST_FILE_NAME=${LAST_VERSION_FILE_NAME%$FILE_LAST_EXTENSION}

#版本上传后的tmp目录
TMP_PATH=$CPATH/$BUSSINESS_DIV/$PACKAGE_TYPE

#创建cfg_tmp解压目录
FILE_DEC_TMP_PATH="cfg_tmp"
if test ! -d $TMP_PATH/$FILE_DEC_TMP_PATH ;then
	mkdir -p $TMP_PATH/$FILE_DEC_TMP_PATH
fi
#创建log目录
if test ! -d $TMP_PATH/log ;then
	mkdir -p $TMP_PATH/log
fi

#创建release的目录，用于合并版本包文件
RELEASE_TMP_PATH="release"
if test ! -d $TMP_PATH/$RELEASE_TMP_PATH ;then
	mkdir -p $TMP_PATH/$RELEASE_TMP_PATH
fi

#把OCS_tmp目录下的所有文件删掉，不进行合并，否则配置文件合并后会把release目录里面的配置文件覆盖
rm -rf $TMP_PATH/$FILE_DEC_TMP_PATH/*
 
#在release目录里按版本名建目录 release/ocs_v0.0.1/
mkdir -p $TMP_PATH/$RELEASE_TMP_PATH/$NO_SUFF_FILE_NAME
cd $TMP_PATH/$NO_SUFF_FILE_NAME
files=`ls *`
if [ "x" == "x$files" ]; then
   echo "${NO_SUFF_FILE_NAME} this file format Failed"
   exit -2
fi
#把文件解压到OCS_tmp目录
for TMP_TAR_NAME2 in $files
do
	#合并配置文件
	TAR_NAME=${TMP_TAR_NAME2%$FILE_EXTENSION}
	if [ $TAR_NAME = "commons" ];
	then
	    echo "commons.tar.gz not doing"
	else
	    mkdir -p $TMP_PATH/$FILE_DEC_TMP_PATH/$TAR_NAME
		#把版本包中的内容拷贝到cfg_tmp目录，并按照tar的目录进行创建
		$TAR_UNZIP_COMMOND $TMP_PATH/$NO_SUFF_FILE_NAME/$TMP_TAR_NAME2 $TAR_UNZIP_D_C $TMP_PATH/$FILE_DEC_TMP_PATH/$TAR_NAME
		#将文件合并后放入到相应的目录，再压缩，用于后续的部署
		mkdir -p $TMP_PATH/$RELEASE_TMP_PATH/$TAR_NAME
		#应业务要求，在合并包中创建一个log目录
		mkdir -p $TMP_PATH/$RELEASE_TMP_PATH/$TAR_NAME/log
		$TAR_UNZIP_COMMOND $TMP_PATH/$NO_SUFF_FILE_NAME/$TMP_TAR_NAME2 $TAR_UNZIP_D_C $TMP_PATH/$RELEASE_TMP_PATH/$TAR_NAME
		if [ -f "$TMP_PATH/$NO_SUFF_FILE_NAME/commons$FILE_EXTENSION" ]; then
			 $TAR_UNZIP_COMMOND $TMP_PATH/$NO_SUFF_FILE_NAME/commons$FILE_EXTENSION $TAR_UNZIP_D_C $TMP_PATH/$RELEASE_TMP_PATH/$TAR_NAME
		fi
		cd $TMP_PATH/$RELEASE_TMP_PATH/$TAR_NAME
		$TAR_ZIP_COMMOND ../$TAR_NAME$FILE_EXTENSION * 
		mv ../$TAR_NAME$FILE_EXTENSION $TMP_PATH/$RELEASE_TMP_PATH/$NO_SUFF_FILE_NAME
	fi
done

 

#进入OCS_V1.0.0_tmp目录下
cd $TMP_PATH/$FILE_DEC_TMP_PATH
#ftp版本服务器 配置文件的目录名
FTP_CONF_DIV=$CPATH/conf/business_config/release/$PACKAGE_TYPE
#上一个版本为空或者找不到上一个版本的配置文件根目录
#拷贝OCS_tmp中的配置文件内容，按照目录进行拷贝
if  [ ! -d $FTP_CONF_DIV/$NO_SUFF_LAST_FILE_NAME -o ! -n "$NO_SUFF_LAST_FILE_NAME" -o  "$NO_SUFF_LAST_FILE_NAME" = "$NO_SUFF_FILE_NAME" ];then
	for TAR_FILE_NAME in `ls`
	do
		#把版本包中的配置文件拷贝到配置文件目录下
		TAR_CFG_PATH=$FTP_CONF_DIV/$NO_SUFF_FILE_NAME/$TAR_FILE_NAME
		mkdir -p $TAR_CFG_PATH
		if [ ! -d "$TMP_PATH/$FILE_DEC_TMP_PATH/$TAR_FILE_NAME/$CONF_DIV" ]; then  
                     mkdir -p $TMP_PATH/$FILE_DEC_TMP_PATH/$TAR_FILE_NAME/$CONF_DIV  
		fi 
		#cp -R $TMP_PATH/$FILE_DEC_TMP_PATH/$TAR_FILE_NAME/$CONF_DIV/. $TAR_CFG_PATH
		find $FTP_CONF_DIV/$NO_SUFF_FILE_NAME -name $TAR_FILE_NAME -type d | xargs -i cp -R $TMP_PATH/$FILE_DEC_TMP_PATH/$TAR_FILE_NAME/$CONF_DIV/. {}
	done
else
	#上一个版本不为空
	LAST_CFG_VERSION_PATH=$FTP_CONF_DIV/$NO_SUFF_LAST_FILE_NAME
	CURR_CFG_VERSION_PATH=$FTP_CONF_DIV/$NO_SUFF_FILE_NAME
	#把上个版本的配置文件拷贝一份
	cp -R $LAST_CFG_VERSION_PATH $CURR_CFG_VERSION_PATH
	#把OCS_tmp中的配置文件拷贝至已经复制出来的文件夹中
	for TAR_FILE_NAME2 in `ls`
	do
		#需要拷贝配置文件的包名称，在已经复制好的上一个版本的配置文件中进行查找
		CFG_DIR_EXIST_FLAG=`find $CURR_CFG_VERSION_PATH -name $TAR_FILE_NAME2 -type d`
		
		#找不到配置文件目录，则为此次新增的配置文件目录，直接拷贝至当前配置文件目录下
		if [[ ! -n $CFG_DIR_EXIST_FLAG ]]; then
			TAR_CFG_PATH2=$FTP_CONF_DIV/$NO_SUFF_FILE_NAME/$TAR_FILE_NAME2
			mkdir -p $TAR_CFG_PATH2
			if [ ! -d "$TMP_PATH/$FILE_DEC_TMP_PATH/$TAR_FILE_NAME2/$CONF_DIV" ]; then
                             mkdir -p $TMP_PATH/$FILE_DEC_TMP_PATH/$TAR_FILE_NAME2/$CONF_DIV  
		        fi
			cp -R $TMP_PATH/$FILE_DEC_TMP_PATH/$TAR_FILE_NAME2/$CONF_DIV/. $TAR_CFG_PATH2
		else
			find $CURR_CFG_VERSION_PATH -name $TAR_FILE_NAME2 -type d | xargs -i cp -R $TMP_PATH/$FILE_DEC_TMP_PATH/$TAR_FILE_NAME2/$CONF_DIV/. {}
		fi
	done
fi

#稽核校验
#切换到cfg_tmp目录，将cfg_tmp目录下的所有文件对应生成MD5值,并且将生成的MD5文件保存到release目录下
##echo -e "start files check ..."
##cd $TMP_PATH/$FILE_DEC_TMP_PATH
#生成cfg_tmp目录下所有文件的MD5值
##find ./ -type f -print0 | xargs -0 md5sum >> ../release/tmp.md5
#切换到release目录下，校验当前目录下文件MD5和tmp.md5值对比
##cd $TMP_PATH/$RELEASE_TMP_PATH
##md5sum -c tmp.md5 | grep "FAILED" >> result.log
#将对比异常结果输出
##cat result.log
#将文件删除
##rm -rf result.log
##rm -rf tmp.md5
##echo -e "end files check ..."

echo "Sync Config Files And Merge Pakage Success..."
echo "Success"