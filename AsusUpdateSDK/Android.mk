LOCAL_PATH := $(call my-dir)

#######################################################################
include $(CLEAR_VARS)
LOCAL_MODULE := AsusUpdateSdk
LOCAL_STATIC_JAVA_LIBRARIES := support-v4
LOCAL_STATIC_JAVA_LIBRARIES += google-play-service
LOCAL_STATIC_JAVA_LIBRARIES += glide-3.6.0
LOCAL_STATIC_JAVA_LIBRARIES += volley-1.0.19
LOCAL_STATIC_JAVA_LIBRARIES += glide-volley-integration-1.3.1

#LOCAL_SDK_VERSION := current

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

include $(BUILD_STATIC_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := google-play-service:libs/google-play-services-v7.5.71.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += support-v4:libs/android-support-v4.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += glide-3.6.0:libs/glide-3.6.0.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += volley-1.0.19:libs/volley-1.0.19.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += glide-volley-integration-1.3.1:libs/glide-volley-integration-1.3.1.jar

include $(BUILD_MULTI_PREBUILT)
#######################################################################
