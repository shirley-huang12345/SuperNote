LOCAL_PATH:= $(call my-dir)

ifneq ($(TARGET_PROJECT),AMAX_NEXUS7)
    include $(call my-dir)/Android_source.mk
else
    ifneq ($(PLATFORM_VERSION),4.3)
        include $(call my-dir)/Android_source.mk
    endif
endif
