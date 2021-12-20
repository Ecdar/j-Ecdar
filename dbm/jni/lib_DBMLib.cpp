#include "lib_DBMLib.h"
#include "../include/dbm/constraints.h"
#include "../include/dbm/dbm.h"
#include "../include/dbm/fed.h"
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

    jintArray cToJint(JNIEnv *env, const raw_t *t, jsize len) {
        // convert updated array to jintArray
        jintArray newT = env->NewIntArray(len);
        int *arr = new int[len];
        for (int i = 0; i < len; i++) {
            arr[i] = t[i];
        }
        env->SetIntArrayRegion(newT, 0, len, (const jint*)arr);
        return newT;
    }

    jobjectArray cFedtoJavaFed(JNIEnv *env, dbm::fed_t fed, jsize len) {
        jint fedSize = fed.size();

        jclass intArray1DClass = env->FindClass("[I");
        jobjectArray zoneArray = env->NewObjectArray(fedSize, intArray1DClass, NULL);

        jint y = 0;
        for (auto i = fed.begin(); i != fed.end(); ++i) {
            auto x = i->const_dbm();
            env->SetObjectArrayElement(zoneArray, y, helper_functions::cToJint(env, x, len));
            y++;
        }

        return zoneArray;
    }

    dbm::fed_t javaFedtoCFed(JNIEnv *env, jobjectArray fed, jsize size, jint dim) {
        jsize length = env->GetArrayLength(fed);

        dbm::fed_t cFed = (*new dbm::fed_t(dim));

        for (int i = 0; i < length; i++) {
            jintArray obj = (jintArray) env->GetObjectArrayElement(fed, i);
            cFed = cFed.add(helper_functions::jintToC(env, obj, size), dim);
        }

        return cFed;
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

JNIEXPORT jintArray JNICALL Java_lib_DBMLib_dbm_1constrainBound(JNIEnv *env, jclass cls, jintArray dbm, jint dim, jint i,
    jint j, jint bound, jboolean strict) {
    jsize len = env->GetArrayLength(dbm);
    auto converted = helper_functions::jintToC(env, dbm, len);

    raw_t constraint = dbm_boundbool2raw(bound, strict);
    dbm_constrain1(converted, dim, i, j, constraint);

    return helper_functions::cToJint(env, converted, len);
}

JNIEXPORT jintArray JNICALL Java_lib_DBMLib_dbm_1constrainRaw(JNIEnv *env, jclass cls, jintArray dbm, jint dim, jint i,
 jint j, jint raw) {
    jsize len = env->GetArrayLength(dbm);
    auto converted = helper_functions::jintToC(env, dbm, len);

    dbm_constrain1(converted, dim, i, j, raw);

    return helper_functions::cToJint(env, converted, len);
}

JNIEXPORT jintArray JNICALL Java_lib_DBMLib_dbm_1up(JNIEnv *env, jclass cls, jintArray dbm, jint dim) {
    jsize len = env->GetArrayLength(dbm);

    auto converted = helper_functions::jintToC(env, dbm, len);
    dbm_up(converted, dim);

    return helper_functions::cToJint(env, converted, len);
}

JNIEXPORT jintArray JNICALL Java_lib_DBMLib_dbm_1close(JNIEnv *env, jclass cls, jintArray dbm, jint dim) {
    jsize len = env->GetArrayLength(dbm);

    auto converted = helper_functions::jintToC(env, dbm, len);
    dbm_close(converted, dim);

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

JNIEXPORT jboolean JNICALL Java_lib_DBMLib_dbm_1isEmpty(JNIEnv *env, jclass cls, jintArray dbm, jint dim) {
    jsize len = env->GetArrayLength(dbm);

    auto converted = helper_functions::jintToC(env, dbm, len);
    return dbm_isEmpty(converted, dim);
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


JNIEXPORT jintArray JNICALL Java_lib_DBMLib_dbm_1freeClock(JNIEnv *env, jclass cls, jintArray dbm, jint dim, jint clockIndex) {
    jsize len = env->GetArrayLength(dbm);

    auto converted = helper_functions::jintToC(env, dbm, len);
    dbm_freeClock(converted, dim, clockIndex);

    return helper_functions::cToJint(env, converted, len);
}



JNIEXPORT jboolean JNICALL Java_lib_DBMLib_dbm_1rawIsStrict(JNIEnv *env, jclass cls, jint raw) {
   return dbm_rawIsStrict(raw);
}

JNIEXPORT jint JNICALL Java_lib_DBMLib_dbm_1addRawRaw(JNIEnv *env, jclass cls, jint raw1, jint raw2) {
   return dbm_addRawRaw(raw1, raw2);
}

JNIEXPORT jobjectArray JNICALL Java_lib_DBMLib_dbm_1minus_1dbm(JNIEnv *env, jclass cls, jintArray dbm1, jintArray dbm2, jint dim) {
    jsize len = env->GetArrayLength(dbm1);

    auto converted1 = helper_functions::jintToC(env, dbm1, len);
    auto converted2 = helper_functions::jintToC(env, dbm2, len);

    auto fed = dbm::fed_t::subtract(converted1, converted2, dim);

    return helper_functions::cFedtoJavaFed(env, fed, len);
}

JNIEXPORT jobjectArray JNICALL Java_lib_DBMLib_fed_1minus_1dbm(JNIEnv *env, jclass cls, jobjectArray fed, jintArray dbm, jint dim) {
    jsize len = env->GetArrayLength(dbm);

    auto convertedFed = helper_functions::javaFedtoCFed(env, fed, len, dim);
    auto convertedDbm = helper_functions::jintToC(env, dbm, len);

    convertedFed -= convertedDbm;

    return helper_functions::cFedtoJavaFed(env, convertedFed, len);
}



JNIEXPORT jobjectArray JNICALL Java_lib_DBMLib_fed_1freeClock(JNIEnv *env, jclass cls, jobjectArray fed, jint dim, jint clockIndex) {
    jint len = dim * dim;
    auto convertedFed = helper_functions::javaFedtoCFed(env, fed, len, dim);
    return helper_functions::cFedtoJavaFed(env, convertedFed.freeClock(clockIndex), len);
}



JNIEXPORT jobjectArray JNICALL Java_lib_DBMLib_fed_1down(JNIEnv *env, jclass cls, jobjectArray fed, jint dim) {
    jint len = dim * dim;
    auto convertedFed = helper_functions::javaFedtoCFed(env, fed, len, dim);
    return helper_functions::cFedtoJavaFed(env, convertedFed.down(), len);
}







JNIEXPORT jobjectArray JNICALL Java_lib_DBMLib_fed_1const_1predt(JNIEnv *env, jclass cls, jobjectArray fed1, jobjectArray fed2, jint dim) {
    jint len = dim * dim;

    auto convertedFed1 = helper_functions::javaFedtoCFed(env, fed1, len, dim);
    auto convertedFed2 = helper_functions::javaFedtoCFed(env, fed2, len, dim);

    convertedFed1.predt(convertedFed2);

    return helper_functions::cFedtoJavaFed(env, convertedFed1, len);
}

JNIEXPORT jboolean JNICALL Java_lib_DBMLib_fed_1intersects_1dbm  (JNIEnv *env, jclass cls, jobjectArray fed1, jobjectArray fed2, jint dim)
{
    jint len = dim * dim;

    auto convertedFed1 = helper_functions::javaFedtoCFed(env, fed1, len, dim);
    auto convertedFed2 = helper_functions::javaFedtoCFed(env, fed2, len, dim);

	return convertedFed1.intersects(convertedFed2);
}


JNIEXPORT jobjectArray JNICALL Java_lib_DBMLib_fed_1minus_1fed(JNIEnv *env, jclass cls, jobjectArray fed1, jobjectArray fed2, jint dim) {
    jint len = dim * dim;

    auto convertedFed1 = helper_functions::javaFedtoCFed(env, fed1, len, dim);
    auto convertedFed2 = helper_functions::javaFedtoCFed(env, fed2, len, dim);

    convertedFed1 -= convertedFed2;

    return helper_functions::cFedtoJavaFed(env, convertedFed1, len);
}




  JNIEXPORT jboolean JNICALL Java_lib_DBMLib_fed_1isSubsetEq (JNIEnv *env, jclass cls, jobjectArray fed1, jobjectArray fed2, jint dim) {
  jint len = dim * dim;
        auto convertedFed1 = helper_functions::javaFedtoCFed(env, fed1, len, dim);
        auto convertedFed2 = helper_functions::javaFedtoCFed(env, fed2, len, dim);

        return convertedFed2 >= convertedFed1;


  }




    JNIEXPORT jobjectArray JNICALL Java_lib_DBMLib_fed_1up (JNIEnv *env, jclass cls, jobjectArray fed, jint dim) {
        jint len = dim * dim;
        auto convertedFed = helper_functions::javaFedtoCFed(env, fed, len, dim);
        return helper_functions::cFedtoJavaFed(env, convertedFed.up(), len);
        }










JNIEXPORT jobjectArray JNICALL Java_lib_DBMLib_fed_1plus_1fed(JNIEnv *env, jclass cls, jobjectArray fed1, jobjectArray fed2, jint dim) {
    jint len = dim * dim;

    auto convertedFed1 = helper_functions::javaFedtoCFed(env, fed1, len, dim);
    auto convertedFed2 = helper_functions::javaFedtoCFed(env, fed2, len, dim);

    convertedFed1 += convertedFed2;

    return helper_functions::cFedtoJavaFed(env, convertedFed1, len);
}

JNIEXPORT jobjectArray JNICALL Java_lib_DBMLib_fed_1intersect_1fed(JNIEnv *env, jclass cls, jobjectArray fed1, jobjectArray fed2, jint dim) {
    jint len = dim * dim;

    auto convertedFed1 = helper_functions::javaFedtoCFed(env, fed1, len, dim);
    auto convertedFed2 = helper_functions::javaFedtoCFed(env, fed2, len, dim);

    convertedFed1 &= convertedFed2;

    return helper_functions::cFedtoJavaFed(env, convertedFed1, len);
}

JNIEXPORT jboolean JNICALL Java_lib_DBMLib_fed_1eq_1fed(JNIEnv *env, jclass cls, jobjectArray fed1, jobjectArray fed2, jint dim) {
    jint len = dim * dim;

    auto convertedFed1 = helper_functions::javaFedtoCFed(env, fed1, len, dim);
    auto convertedFed2 = helper_functions::javaFedtoCFed(env, fed2, len, dim);

    return convertedFed1.eq(convertedFed2);
}

JNIEXPORT jintArray JNICALL Java_lib_DBMLib_dbm_1extrapolateMaxBounds(JNIEnv *env, jclass cls, jintArray dbm, jint dim, jintArray max) {
    jsize len = env->GetArrayLength(dbm);

    auto converted = helper_functions::jintToC(env, dbm, len);
    auto convertedMax = helper_functions::jintToC(env, max, len);
    dbm_extrapolateMaxBounds(converted, dim, convertedMax);

    return helper_functions::cToJint(env, converted, len);
}

int main() { return 0; }