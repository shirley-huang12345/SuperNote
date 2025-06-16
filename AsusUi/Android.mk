LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

use_amax_prebuilt := true

ifeq ($(use_amax_prebuilt),true)
    LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
        asus-common-ui:libs/asus-common-ui_v0.8.22.jar
    include $(BUILD_MULTI_PREBUILT)
endif
