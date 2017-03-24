#ifndef NTFUN
#define NTFUN

#ifdef LOG_TAG
#undef LOG_TAG
#endif
#define LOG_TAG  "jni"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define printf LOGI

int com_write(char *devName,int baund,char *cmd);

int get_netmask(char *eth,char *maskaddr);
int get_gateway(char *eth,char *gateaddr);
int get_mac_info(char *addr);


#endif










