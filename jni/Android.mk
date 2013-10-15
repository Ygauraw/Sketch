LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := AbhanFilter
LOCAL_SRC_FILES := com_abhan_example_AbhanNativeFilter.cpp

LOCAL_LDLIBS := -lm -llog

include $(BUILD_SHARED_LIBRARY)
