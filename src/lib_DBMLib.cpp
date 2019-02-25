#include "lib_DBMLib.h"
#include "../dbm/constraints.h"
#include "../dbm/dbm.h"
#include <string.h>

namespace helper_functions
{
    raw_t* jintToC(JNIEnv *env, jintArray dbm, jsize len) {
        // build array to pass to library
        raw_t *t = new raw_t[len];
        jint *arr = env->GetIntArrayElements(dbm, 0);
        for (int i = 0; i < len; i++)
            t[i] = arr[i];
        return t;
    }

    jintArray cToJint(JNIEnv *env, raw_t *t, jsize len) {
        // convert updated array to jintArray
        jintArray newT = env->NewIntArray(len);
        int *arr = new int[len];
        for (int i = 0; i < len; i++) {
            arr[i] = t[i];
        }
        env->SetIntArrayRegion(newT, 0, len, arr);
        return newT;
    }
}

JNIEXPORT jint JNICALL Java_lib_DBMLib_boundbool2raw(JNIEnv *env, jclass cls, jint bound, jboolean strict) {
    return dbm_boundbool2raw(bound, strict);
}

JNIEXPORT jint JNICALL Java_lib_DBMLib_raw2bound(JNIEnv *env, jclass cls, jint raw) {
   return dbm_raw2bound(raw);
}

JNIEXPORT jintArray JNICALL Java_lib_DBMLib_dbm_1init(JNIEnv *env, jclass cls, jintArray dbm, jint dim) {
   // get size of dbm
    jsize len = env->GetArrayLength(dbm);
    // call library with built array
    auto converted = helper_functions::jintToC(env, dbm, len);

    dbm_init(converted, dim);

    return helper_functions::cToJint(env, converted, len);
}

JNIEXPORT jintArray JNICALL Java_lib_DBMLib_dbm_1zero(JNIEnv *env, jclass cls, jintArray dbm, jint dim) {
    jsize len = env->GetArrayLength(dbm);
    auto converted = helper_functions::jintToC(env, dbm, len);
     dbm_zero(converted, dim);
     return helper_functions::cToJint(env, converted, len);
}

JNIEXPORT jintArray JNICALL Java_lib_DBMLib_dbm_1constrain1(JNIEnv *env, jclass cls, jintArray dbm, jint dim, jint i,
    jint j, jint bound, jboolean strict) {
    jsize len = env->GetArrayLength(dbm);
    auto converted = helper_functions::jintToC(env, dbm, len);

    raw_t constraint = dbm_boundbool2raw(bound, strict);
    dbm_constrain1(converted, dim, i, j, constraint);

    return helper_functions::cToJint(env, converted, len);
}

JNIEXPORT jintArray JNICALL Java_lib_DBMLib_dbm_1up(JNIEnv *env, jclass cls, jintArray dbm, jint dim) {
    jsize len = env->GetArrayLength(dbm);

    auto converted = helper_functions::jintToC(env, dbm, len);
    dbm_up(converted, dim);

    return helper_functions::cToJint(env, converted, len);
}

JNIEXPORT jboolean JNICALL Java_lib_DBMLib_dbm_1isSubsetEq(JNIEnv *env, jclass cls, jintArray dbm1, jintArray dbm2, jint dim) {
    jsize len = env->GetArrayLength(dbm2);

    auto converted1 = helper_functions::jintToC(env, dbm1, len);
    auto converted2 = helper_functions::jintToC(env, dbm2, len);

    return dbm_isSubsetEq(converted1, converted2, dim);
}

JNIEXPORT jintArray JNICALL Java_lib_DBMLib_dbm_1updateValue(JNIEnv *env, jclass cls, jintArray dbm, jint dim, jint clockIndex, jint value) {
    jsize len = env->GetArrayLength(dbm);

    auto converted = helper_functions::jintToC(env, dbm, len);
    dbm_updateValue(converted, dim, clockIndex, value);

    return helper_functions::cToJint(env, converted, len);
}

JNIEXPORT jboolean JNICALL Java_lib_DBMLib_dbm_1isValid(JNIEnv *env, jclass cls, jintArray dbm, jint dim) {
    jsize len = env->GetArrayLength(dbm);

    auto converted = helper_functions::jintToC(env, dbm, len);
    return dbm_isValid(converted, dim);
}

JNIEXPORT jboolean JNICALL Java_lib_DBMLib_dbm_1intersection(JNIEnv *env, jclass cls, jintArray dbm1, jintArray dbm2, jint dim) {
    jsize len = env->GetArrayLength(dbm2);

    auto converted1 = helper_functions::jintToC(env, dbm1, len);
    auto converted2 = helper_functions::jintToC(env, dbm2, len);

    return dbm_intersection(converted1, converted2, dim);
}

JNIEXPORT jintArray JNICALL Java_lib_DBMLib_dbm_1freeAllDown(JNIEnv *env, jclass cls, jintArray dbm, jint dim) {
    jsize len = env->GetArrayLength(dbm);

    auto converted = helper_functions::jintToC(env, dbm, len);
    dbm_freeAllDown(converted, dim);

    return helper_functions::cToJint(env, converted, len);
}

JNIEXPORT jintArray JNICALL Java_lib_DBMLib_dbm_1freeDown(JNIEnv *env, jclass cls, jintArray dbm, jint dim, jint clockIndex) {
    jsize len = env->GetArrayLength(dbm);

    auto converted = helper_functions::jintToC(env, dbm, len);
    dbm_freeDown(converted, dim, clockIndex);

    return helper_functions::cToJint(env, converted, len);
}

JNIEXPORT jboolean JNICALL Java_lib_DBMLib_dbm_1rawIsStrict(JNIEnv *env, jclass cls, jint raw) {
   return dbm_rawIsStrict(raw);
}

int main() { return 0; }