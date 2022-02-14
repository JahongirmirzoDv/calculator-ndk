#include <jni.h>
#include <string>

extern "C" JNIEXPORT jfloat JNICALL
Java_com_example_a2task1_MainActivity_addJNI(
        JNIEnv *env,
        jobject, /* this */
        jfloat a, jfloat b) {
    return a + b;
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_example_a2task1_MainActivity_subJNI(
        JNIEnv *env,
        jobject, /* this */
        jfloat a, jfloat b) {
    return a - b;
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_example_a2task1_MainActivity_multiJNI(
        JNIEnv *env,
        jobject, /* this */
        jfloat a, jfloat b) {
    return a * b;
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_example_a2task1_MainActivity_divJNI(
        JNIEnv *env,
        jobject, /* this */
        jfloat a, jfloat b) {
    return a / b;
}