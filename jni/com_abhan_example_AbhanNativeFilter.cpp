#include "com_abhan_example_AbhanNativeFilter.h"
#include "SketchFilter.h"
#include "Util.h"

jintArray Java_com_abhan_example_AbhanNativeFilter_sketchFilter(JNIEnv* env, jclass object,
		jintArray pixels, jint width, jint height) {
	jintArray result = procImage(env, pixels, width, height, sketchFilter);
	return result;
}
