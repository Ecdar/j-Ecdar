#include "lib_DBMLib.h"
#include "../dbm/modules/include/dbm/constraints.h"
#include <string.h>

JNIEXPORT jint JNICALL Java_lib_DBMLib_boundbool2raw(JNIEnv *env, jclass cls, jint bound, jboolean strict) {
    return dbm_boundbool2raw(bound, strict);
}

JNIEXPORT jint JNICALL Java_lib_DBMLib_raw2bound(JNIEnv *env, jclass cls, jint raw) {
   return dbm_raw2bound(raw);
}

int main(){}