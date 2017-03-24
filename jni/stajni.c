#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <string.h>
#include <strings.h>
#include <unistd.h>
#include <setjmp.h>
#include <math.h>
#include <sys/ipc.h>
#include <sys/mman.h>
#include <ctype.h>
#include <errno.h>
#include <time.h>
#include <fcntl.h>
#include <netdb.h>
#include <dirent.h>
#include <net/if.h>
#include <termios.h>
#include <sys/ioctl.h>
#include <sys/time.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <sys/vfs.h>
#include <sys/resource.h>
#include <sys/utsname.h>
#include <utime.h>
#include <pthread.h>
#include <jni.h>
#include <dirent.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <android/log.h>

#include "opfun.h"

#ifdef __cplusplus
    extern "C" {
#endif

void __exidx_start() {}
void __exidx_end()   {}

jstring stringToJString(JNIEnv* env, const char* pat)
{
	jclass  strClass = (*env)->FindClass(env,"java/lang/String");
	jmethodID ctorID = (*env)->GetMethodID(env,strClass,"<init>","([BLjava/lang/String;)V");
	jbyteArray bytes = (*env)->NewByteArray(env,strlen(pat));
	(*env)->SetByteArrayRegion(env,bytes,0,strlen(pat),(jbyte*)pat);

	jstring encoding = (*env)->NewStringUTF(env,"utf-8");

	return (jstring)(*env)->NewObject(env,strClass,ctorID,bytes,encoding);
}

void jstringToChar(JNIEnv *env,jstring jstr,char *buf,int len)
{
   jclass     clsstring=(*env)->FindClass(env,"java/lang/String");
   jstring    strencode=(*env)->NewStringUTF(env,"utf-8");
   jmethodID  mid      = (*env)->GetMethodID(env,clsstring,"getBytes","(Ljava/lang/String;)[B");
   jbyteArray barr     =(jbyteArray)(*env)->CallObjectMethod(env,jstr,mid,strencode);
   jsize      alen     =(*env)->GetArrayLength(env,barr);
   jbyte*     ba       =(*env)->GetByteArrayElements(env,barr,JNI_FALSE);
   if(alen>0 && alen<len-1){
		memcpy(buf,ba,alen); buf[alen]=0;
   }else{
		buf[0]=0;
   }
   (*env)->ReleaseByteArrayElements(env,barr,ba,0);
}

//==================================================================================================================

JNIEXPORT jstring JNICALL Java_com_ntms_baseFun_getMac(JNIEnv*  env, jobject obj)
{
	char  addr[64];

	memset(addr,0x00,sizeof(addr));

	if(get_mac_info(addr)>1){
		return stringToJString(env,addr);
	}
	return NULL;
}

JNIEXPORT jstring JNICALL Java_com_ntms_baseFun_getNetmask( JNIEnv* env,jobject thiz,jstring dev)
{
	char buf[128]="\0";
	char rtbuf[128]="\0";
	memset(buf,0x00,sizeof(buf));
	memset(rtbuf,0x00,sizeof(rtbuf));
	jstringToChar(env,dev,buf,128);
	get_netmask(buf,rtbuf);

	if(rtbuf[0]!=0){
		return stringToJString(env,rtbuf);
	}else{
		return NULL;
	}
}


JNIEXPORT jstring JNICALL Java_com_ntms_baseFun_getGateway( JNIEnv* env,jobject thiz,jstring dev)
{
	char buf[128]="\0";
	char rtbuf[128]="\0";

	memset(buf,0x00,sizeof(buf));
	memset(rtbuf,0x00,sizeof(rtbuf));
	jstringToChar(env,dev,buf,128);
	get_gateway(buf,rtbuf);
	if(rtbuf[0]!=0){
		return stringToJString(env,rtbuf);
	}else{
		return NULL;
	}
}

JNIEXPORT jint JNICALL Java_com_ntms_baseFun_writeCom( JNIEnv* env,jobject thiz,jstring dev,int baund,jstring cmdStr){

	char buf[64]="\0";
	char cmd[256]="\0";

	memset(buf,0x00,sizeof(buf));
	memset(cmd,0x00,sizeof(cmd));

	jstringToChar(env,dev,buf,64);
	jstringToChar(env,cmdStr,cmd,256);

	return com_write(buf,baund,cmd);
}

JNIEXPORT jint JNICALL Java_com_ntms_baseFun_sync( JNIEnv* env,jobject thiz){

	sync();
	return 0;
}

#ifdef __cplusplus
    }
#endif


