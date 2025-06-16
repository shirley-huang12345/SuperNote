LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

include $(BUILD_MULTI_PREBUILT)
###############################################################################
include $(CLEAR_VARS)
LOCAL_MODULE := libPdfConverter
LOCAL_MODULE_PATH := $(TARGET_OUT_SHARED_LIBRARIES)
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE_TAGS := optional
ifeq ($(TARGET_ARCH),x86)
LOCAL_SRC_FILES := libs/x86/libPdfConverter.so
else ifeq ($(TARGET_ARCH),arm64)
LOCAL_SRC_FILES := libs/arm64/libPdfConverter.so
else
LOCAL_SRC_FILES := libs/armeabi/libPdfConverter.so
endif
include $(BUILD_PREBUILT)
#################################################################
include $(CLEAR_VARS)

LOCAL_MODULE := SuperNote
prebuilt_sample_PRODUCT_AAPT_CONFIG := $(subst $(comma), ,$(PRODUCT_AAPT_CONFIG))
ifneq ($(filter $(TARGET_PROJECT), PF400CG),)
    LOCAL_SRC_FILES := hdpi_tvdpi/$(LOCAL_MODULE).apk
else ifneq ($(filter $(TARGET_PROJECT), PF450CL),)
    LOCAL_SRC_FILES := hdpi_tvdpi/$(LOCAL_MODULE).apk
else ifneq ($(filter $(TARGET_PROJECT), A68),)
    LOCAL_SRC_FILES := hdpi_xhdpi/$(LOCAL_MODULE).apk
else ifneq ($(filter $(TARGET_PROJECT), PF500KL),)
    LOCAL_SRC_FILES := hdpi_xxhdpi/$(LOCAL_MODULE).apk
else ifneq (,$(filter xxhdpi,$(prebuilt_sample_PRODUCT_AAPT_CONFIG)))
    LOCAL_SRC_FILES := xxhdpi/$(LOCAL_MODULE).apk
else ifneq (,$(filter xhdpi,$(prebuilt_sample_PRODUCT_AAPT_CONFIG)))
    LOCAL_SRC_FILES := xhdpi/$(LOCAL_MODULE).apk
else ifneq (,$(filter tvdpi,$(prebuilt_sample_PRODUCT_AAPT_CONFIG)))
    LOCAL_SRC_FILES := tvdpi/$(LOCAL_MODULE).apk
else ifneq (,$(filter hdpi,$(prebuilt_sample_PRODUCT_AAPT_CONFIG)))
    LOCAL_SRC_FILES := hdpi/$(LOCAL_MODULE).apk
else ifneq (,$(filter mdpi,$(prebuilt_sample_PRODUCT_AAPT_CONFIG)))
    LOCAL_SRC_FILES := mdpi/$(LOCAL_MODULE).apk
else
    LOCAL_SRC_FILES := $(LOCAL_MODULE).apk
endif

LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)
LOCAL_CERTIFICATE := PRESIGNED
LOCAL_REQUIRED_MODULES := libPdfConverter 
LOCAL_PACKAGE_NAME := SuperNote
LOCAL_PRIVILEGED_MODULE := true
include $(BUILD_PREBUILT)

###############################################################################
# Build other projects
#include $(call all-makefiles-under, $(LOCAL_PATH))
