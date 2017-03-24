#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <string.h>
#include <strings.h>
#include <unistd.h>
#include <errno.h>
#include <time.h>
#include <fcntl.h>
#include <netdb.h>
#include <ctype.h>
#include <errno.h>
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
#include <android/log.h>
#include <limits.h>
#include <sys/reboot.h>
#include <linux/ioctl.h>
#include <linux/types.h>



#include "opfun.h"

int open_com(char *dev_com,int baund,struct termios *dev_tio)
{
	int    fd=0;
	struct termios newtio;

 	if((fd=open(dev_com,O_RDWR|O_NOCTTY|O_NONBLOCK))<0){
		printf("\nopen com fail[%s]\r\n",dev_com);  return -1;
	}else{
 		tcgetattr(fd,dev_tio);  bzero(&newtio,sizeof(newtio));
 	}
 	if(baund==4800)	        newtio.c_cflag=B4800|CS8|CLOCAL|CREAD;
 	else if(baund==9600)	newtio.c_cflag=B9600|CS8|CLOCAL|CREAD;
 	else if(baund==38400)	newtio.c_cflag=B38400|CS8|CLOCAL|CREAD;
 	else                    newtio.c_cflag=B115200|CS8|CLOCAL|CREAD;

 	newtio.c_iflag=IGNPAR;
 	newtio.c_oflag=0;
 	newtio.c_lflag=0;
 	newtio.c_cc[VTIME]=2;
 	newtio.c_cc[VMIN] =1;

 	tcflush(fd,TCIFLUSH);  tcsetattr(fd,TCSANOW,&newtio);

 	printf("\nopen com %s ok\r\n",dev_com);

 	return fd;
}

int close_com(int *fd,struct termios *dev_tio)
{
	if(*fd>=0){
		tcsetattr(*fd,TCSANOW,dev_tio);
		*fd=-1;
		printf("\ncom closed\r\n");
		return 1;
	}
	return 0;
}

int write_data(int fd,char *str){

	fd_set     set;
	struct     timeval tv;
	int        i=0,k=0,res=0,buflen=strlen(str);

	if(fd<0){
		return 0;
	}
	for(i=0;i<16;i++){
		tv.tv_sec=0; tv.tv_usec=50000;
		FD_ZERO(&set); FD_SET(fd,&set);
		if(select(fd+1,NULL,&set,NULL,&tv)<=0){
			FD_CLR(fd,&set); continue;
		}
		if(FD_ISSET(fd,&set)){
			FD_CLR(fd,&set);
			k=write(fd,str+res,buflen-res);
			if(k>0){
				res+=k;
				if(res>=buflen){
					printf("\n------------------------------\nsend com cmd: %s",str);
					return res;
				}
			}
		}
	}
	return 0;
}


int com_write(char *devName,int baund,char *cmd){

	struct termios dev_tio;
	int    fd=-1;

	if(fd<0){
		if(devName==NULL || strlen(devName)<1){
			printf("com dev name err");
			return 0;
		}else{
			fd=open_com((char *)devName,baund,&dev_tio);
		}
		if(fd<0){
			return 0;
		}
	}
	write_data(fd,cmd);

	close_com(&fd,&dev_tio);

	return 1;
}

//----------------------------------------------------------------------------------------------

int get_mac_info(char *addr)
{
	int  i,sk;  struct ifreq ifreq;

	if((sk=socket(AF_INET,SOCK_STREAM,0))<0) return -1;
	strcpy(ifreq.ifr_name,"eth0");

	if(ioctl(sk,SIOCGIFHWADDR,&ifreq)<0)   {
		close(sk);  return -1;
	}

	for(i=0;i<6;i++) sprintf(addr+2*i,"%02X",(unsigned char)ifreq.ifr_hwaddr.sa_data[i]);

	close(sk);

	return (int)strlen(addr);
}


int get_gateway(char *eth,char *gateaddr)
{
	int sockfd; struct ifreq req;
	if((sockfd=socket(PF_INET,SOCK_DGRAM,0))<0) return -1;  memset(&req,0,sizeof(struct ifreq));  sprintf(req.ifr_name,"%s",eth);
	if(ioctl(sockfd,SIOCGIFBRDADDR,(char*)&req)){
		close(sockfd); return -1;
	}else{
		struct in_addr ip_addr; ip_addr.s_addr=*((int*) &req.ifr_addr.sa_data[2]);  strcpy(gateaddr,inet_ntoa(ip_addr));
	}
	close(sockfd);

	return (int)strlen(gateaddr);
}


int get_netmask(char *eth,char *maskaddr)
{
	int sockfd; struct ifreq req;
	if((sockfd=socket(PF_INET,SOCK_DGRAM,0))<0) return -1;  memset(&req,0,sizeof(struct ifreq));  sprintf(req.ifr_name,"%s",eth);
	if(ioctl(sockfd,SIOCGIFNETMASK,(char*)&req)){
		close(sockfd); return -1;
	}else{
		struct sockaddr_in *addr; addr=(struct sockaddr_in *) &(req.ifr_addr); strcpy(maskaddr,inet_ntoa( addr->sin_addr));
	}
	close(sockfd);
	return (int)strlen(maskaddr);
}












