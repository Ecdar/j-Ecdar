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
            t[i] = (raw_t) arr[i];
        return t;
    }

    jintArray& cToJint(JNIEnv *env, raw_t *t, jsize len) {
        // convert updated array to jintArray
        jintArray newT = env->NewIntArray(len);
        env->SetIntArrayRegion(newT, 0, len, t);
        return newT;
    }
}

JNIEXPORT jint JNICALL Java_lib_DBMLib_boundbool2raw(JNIEnv *env, jclass cls, jint bound, jboolean strict) {
    return dbm_boundbool2raw(bound, strict);
}

JNIEXPORT jint JNICALL Java_lib_DBMLib_raw2bound(JNIEnv *env, jclass cls, jint raw) {
   return dbm_raw2bound(raw);
}

JNIEXPORT jobject JNICALL Java_lib_DBMLib_constraint(JNIEnv *env, jclass cls, jint i, jint j, jint bound, jboolean isStrict) {

    auto constraint = dbm_constraint2(i, j, bound, isStrict);
    jclass clss = env->FindClass("lib/Constraint");
    jmethodID constructor = env->GetMethodID(clss, "<init>", "(III)V");
    jobject object = env->NewObject(clss, constructor, constraint.i, constraint.j, constraint.value);
    return object;
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

int main() { return 0; }