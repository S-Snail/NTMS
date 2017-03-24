LOCAL_PATH := $(call my-dir)
#############################################################################
include $(CLEAR_VARS)

LOCAL_LDLIBS   += -fuse-ld=bfd
LOCAL_CPPFLAGS += -fexceptions  
LOCAL_STATIC_LIBRARIES := libc
LOCAL_LDLIBS   += -ldl -llog -lz

LOCAL_MODULE    := ntjni 
LOCAL_SRC_FILES := opfun.c stajni.c

include $(BUILD_SHARED_LIBRARY) 

######################################################################


include $(CLEAR_VARS)

LOCAL_LDLIBS   += -fuse-ld=bfd
LOCAL_CPPFLAGS += -fexceptions
LOCAL_CFLAGS  += -pie -fPIE
LOCAL_LDFLAGS += -pie -fPIE
LOCAL_STATIC_LIBRARIES := libc
LOCAL_LDLIBS   += -ldl -llog -lz

LOCAL_MODULE    := daemon
LOCAL_SRC_FILES := daemon.c


include $(BUILD_EXECUTABLE)

