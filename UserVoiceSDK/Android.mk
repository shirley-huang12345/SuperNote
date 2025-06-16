LOCAL_PATH := $(call my-dir)

#######################################################################
include $(CLEAR_VARS)
LOCAL_MODULE := UserVoiceSDK
LOCAL_STATIC_JAVA_LIBRARIES := support_v4
LOCAL_STATIC_JAVA_LIBRARIES += signpost_commonshttp4-1.2.1.2
LOCAL_STATIC_JAVA_LIBRARIES += signpost_core-1.2.1.2

#LOCAL_SDK_VERSION := current

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

include $(BUILD_STATIC_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := signpost_commonshttp4-1.2.1.2:libs/signpost-commonshttp4-1.2.1.2.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += signpost_core-1.2.1.2:libs/signpost-core-1.2.1.2.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += support_v4:libs/android-support-v4.jar
include $(BUILD_MULTI_PREBUILT)
#######################################################################
