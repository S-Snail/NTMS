
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <termio.h>
#include <fcntl.h>
#include <signal.h>
#include <errno.h>
#include <net/if.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <sys/vfs.h>



#define ANJNI


#ifdef ANJNI
#include <jni.h>
#include <android/log.h>
#ifdef __cplusplus
    extern "C" {
#endif
void __exidx_start() {}
void __exidx_end() {}
#ifdef __cplusplus
    }
#endif

#ifdef LOG_TAG
#undef LOG_TAG
#endif
#define LOG_TAG  "daemon"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define printf LOGI
#endif

#define MAXCONN 5


unsigned long getTickCount(struct timeval start_tm){

	struct timeval cur;
	gettimeofday(&cur,NULL);

	return  (cur.tv_sec-start_tm.tv_sec)*1000+ (cur.tv_usec-start_tm.tv_usec)/1000;
}

int chk_run(char *argv){
	int  cnt=0;
	FILE *fp=NULL;
	char buf[256],*s=NULL;

	memset(buf,0x00,sizeof(buf));
	if((fp=popen("ps","r"))!=NULL){
		if((s=strrchr(argv,'/'))!=NULL) {

			while((fgets(buf,sizeof(buf)-4,fp))!=NULL) {
				if(strstr(buf,s+1)!=NULL){
					cnt++;
				}
				memset(buf,0x00,sizeof(buf));
			}
		}
		pclose(fp);
	}
	return cnt;
}

int start_net(int port){

	char   buf[512]="\0",*s=NULL,*t=NULL;
	int    ret=-1,on=1,listenfd=-1,conn=-1,conn_new=-1,i=0,res=0,k=0,svrlen=0;
	int    rcvtimout=0,playSta=1;
	int    errCnt=0;

	unsigned long monitime=60*1000;//180*1000;

	struct sockaddr_in servaddr,peeraddr;
	struct timeval tv;
	struct timeval start_tm;
	fd_set rset,wset,listenset;

begin:
	if(conn!=-1){
		close(conn); conn=-1;
	}
	if(conn_new!=-1){
		close(conn_new); conn_new=-1;
	}
	if(listenfd!=-1){
		close(listenfd); listenfd=-1;
	}
    if ((listenfd = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP)) < 0){
        return -1;
	}
    memset(&servaddr,0,sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_port   = htons(port);
    servaddr.sin_addr.s_addr = htonl(INADDR_ANY);

    if (setsockopt(listenfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0){
        return -1;
	}
    if (bind(listenfd, (struct sockaddr*)&servaddr,sizeof(servaddr)) < 0){
        return -1;
	}
    if (listen(listenfd,MAXCONN) < 0) {
        return -1;
    }
	socklen_t peerlen = sizeof(peeraddr); memset(buf,0x00,sizeof(buf));

	printf("\nnow listen port %d...\n",port);

	gettimeofday(&start_tm,NULL);

    for(;;) {
    	if(playSta==1){
			if(getTickCount(start_tm)>monitime){
				if((errCnt++)>=4){
					printf("will reboot by status err[%d]\n",errCnt);
					system("date >> /mnt/sdcard/.rbt");
					system("reboot &");
				}
				printf("=========unrespond[%d]============",errCnt);
				gettimeofday(&start_tm,NULL);
			}
    	}
        tv.tv_sec=0; tv.tv_usec=80000;  FD_ZERO(&listenset); FD_SET(listenfd,&listenset);

        ret=select(listenfd+1,&listenset,NULL,NULL,&tv);
        if(ret==-1) {
            if(errno==EINTR) {
				FD_CLR(listenfd,&rset);  continue;
			}
			goto end;
        }else if(ret==0){
            FD_CLR(listenfd,&rset);

		}else if(FD_ISSET(listenfd,&listenset)) {
			FD_CLR(listenfd,&rset);

            conn_new = accept(listenfd, (struct sockaddr*)&peeraddr, &peerlen);
            if (conn_new == -1){
                goto end;
            }else{
				if(conn!=-1) close(conn); conn=conn_new; memset(buf,0x00,sizeof(buf));
			}
            printf("\nrecv connect ip=%s port=%d\n", inet_ntoa(peeraddr.sin_addr),ntohs(peeraddr.sin_port));
        }
		if(conn_new>=0){
			rcvtimout=0;
			for(;;){

				if(playSta==1){
					if(getTickCount(start_tm)>monitime){
						if((errCnt++)>=4){
							printf("will reboot by status err[%d]\n",errCnt);
							system("date >> /mnt/sdcard/.rbt");
							system("reboot &");
						}
						printf("=========unrespond[%d]============",errCnt);
						gettimeofday(&start_tm,NULL);
					}
				}
				tv.tv_sec=0; tv.tv_usec=100000;  FD_ZERO(&rset); FD_SET(conn_new,&rset);

				ret=select(conn_new+1,&rset,NULL,NULL,&tv);
				if(ret==-1) {
					if(errno==EINTR){
						FD_CLR(conn_new,&rset); continue;
					}
					printf("select error rcv"); goto end;

				}else if(ret==0){
					if((rcvtimout++)>128){
						rcvtimout=0; break;
					}
					FD_CLR(conn_new,&rset); continue;
				}
				if(FD_ISSET(conn_new, &rset)) {
					FD_CLR(conn_new,&rset);

					svrlen=strlen(buf); if(svrlen>sizeof(buf)-8) svrlen=0; rcvtimout=0;

					if((ret=read(conn_new,buf+svrlen,sizeof(buf)-svrlen-8))>0){
						char *t=NULL,*s=NULL;
						buf[ret+svrlen]=0;
						printf("rcv Str: %s\n",buf);
						if((t=strstr(buf,"<Key>"))!=NULL && (s=strstr(buf,"</Key>"))!=NULL){
							if(t!=s && s>t+6){
								*s=0;
								system(t+5);
							}
							gettimeofday(&start_tm,NULL);
						}else if(strstr(buf,"<Sta>0</Sta>")!=NULL)	{
							memset(buf,0x00,sizeof(buf));
							playSta=0;
							errCnt=0;
							printf("\nstatus report end\n");
						}else if(strstr(buf,"<Sta>1</Sta>")!=NULL){
							memset(buf,0x00,sizeof(buf));
							playSta=1;
							errCnt=0;
							gettimeofday(&start_tm,NULL);
						}
					}else{
						close(conn_new); conn_new=-1; conn=-1; printf("net rcv break\n"); break;
					}
				}
			}
		}
    }
end:
	if(conn!=-1){
		close(conn); conn=-1;
	}
	if(conn_new!=-1){
		close(conn_new); conn_new=-1;
	}
	if(listenfd!=-1){
		close(listenfd); listenfd=-1;
	}
    return 0;
}

int main(int argc,char *argv[])
{
	if(chk_run(argv[0])>1){
		 return 0;
	}
	signal(SIGPIPE, SIG_IGN);

	for(;;){

		start_net(argc>1?atoi(argv[1]):3333);

		sleep(10);
	}

	return 0;
}


