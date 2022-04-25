#include <helper_functions.h>

raw_t* helper_functions::jintToC(JNIEnv *env, jintArray dbm, jsize len) {
    // build array to pass to library
    raw_t *t = new raw_t[len];
    jint *arr = env->GetIntArrayElements(dbm, 0);
    for (int i = 0; i < len; i++)
        t[i] = arr[i];
    env->ReleaseIntArrayElements(dbm, arr, JNI_ABORT);
    return t;
}

jintArray helper_functions::cToJint(JNIEnv *env, const raw_t *t, jsize len) {
    // convert updated array to jintArray
    jintArray newT = env->NewIntArray(len);
    if(newT == NULL){
        return NULL; // out of memory error
    }
    jint arr[len];
    for (int i = 0; i < len; i++) {
        arr[i] = t[i];
    }
    env->SetIntArrayRegion(newT, 0, len, arr);
    return newT;
}

jobjectArray helper_functions::cFedtoJavaFed(JNIEnv *env, dbm::fed_t fed, jsize len) {
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

dbm::fed_t helper_functions::javaFedtoCFed(JNIEnv *env, jobjectArray fed, jsize size, jint dim) {
    jsize length = env->GetArrayLength(fed);

    dbm::fed_t cFed = (*new dbm::fed_t(dim));

    for (int i = 0; i < length; i++) {
        jintArray obj = (jintArray) env->GetObjectArrayElement(fed, i);
        cFed = cFed.add(helper_functions::jintToC(env, obj, size), dim);
    }

    return cFed;
}

jobjectArray helper_functions::cPointerToJavaArray(
        JNIEnv *env,
         int** arrayPointer, int numArrays, int lengthArrays) {

    // Get the int array class
    jclass cls = env->FindClass("[I");

    jintArray iniVal = env->NewIntArray(lengthArrays);
    // Create the returnable jobjectArray with an initial value
    jobjectArray outer = env->NewObjectArray(numArrays,cls, iniVal);

    for (int i = 0; i < numArrays; i++)
    {
        jintArray inner = env->NewIntArray(lengthArrays);
        env->SetIntArrayRegion(inner, 0, lengthArrays, arrayPointer[i]);
        // set inner's values
        env->SetObjectArrayElement(outer, i, inner);
        env->DeleteLocalRef(inner);
    }
    return outer;
}