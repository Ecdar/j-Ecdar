#ifndef HELPER_FUNCTIONS_H
#define HELPER_FUNCTIONS_H

#include <jni.h>
#include <dbm/dbm.h>
#include <dbm/fed.h>

namespace helper_functions
{
    raw_t* jintToC(JNIEnv *env, jintArray dbm, jsize len);

    jintArray cToJint(JNIEnv *env, const raw_t *t, jsize len);

    jobjectArray cFedtoJavaFed(JNIEnv *env, dbm::fed_t fed, jsize len);

    dbm::fed_t javaFedtoCFed(JNIEnv *env, jobjectArray fed, jsize size, jint dim);

    jintArray cPointerToJavaArray(JNIEnv *env,  const int32_t *arrayPointer, jsize len);

    int32_t* jintToCIntArray(JNIEnv *env, jintArray dbm, jsize len);
}

#endif