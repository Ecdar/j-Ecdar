#include "lib_CDDLib.h"

#include <cdd/cdd.h>
#include <cdd/kernel.h>
#include <iostream>
#include <helper_functions.h>

/*
 * Class:     lib_CDDLib
 * Method:    cddInit
 * Signature: (III)I
 */
jint JNICALL Java_lib_CDDLib_cddInit
  (JNIEnv *env, jclass thisObject, jint maxsize, jint cs, jint stacksize){
    return cdd_init(maxsize,cs,stacksize);
}

/*
 * Class:     lib_CDDLib
 * Method:    cddDone
 * Signature: ()V
 */
void JNICALL Java_lib_CDDLib_cddDone
  (JNIEnv *env, jclass thisObject){
   cdd_done();
}

/*
 * Class:     lib_CDDLib
 * Method:    allocateCdd
 * Signature: ()J
 */
jlong JNICALL Java_lib_CDDLib_allocateCdd
  (JNIEnv *env, jclass cdd_class){
    cdd* cdd_object = new cdd();
    return (jlong)cdd_object;
  }

/*
 * Class:     lib_CDDLib
 * Method:    freeCdd
 * Signature: (J)V
 */
void JNICALL Java_lib_CDDLib_freeCdd
  (JNIEnv *env, jclass cdd_class, jlong pointer){
    cdd* cdd_object = (cdd*) pointer;
    delete cdd_object;
  }

/*
 * Class:     lib_CDDLib
 * Method:    conjunction
 * Signature: (JJ)V
 */
jlong JNICALL Java_lib_CDDLib_conjunction
  (JNIEnv *env, jclass cdd_class, jlong pointer_l, jlong pointer_r){
    cdd* cdd_object_l = (cdd*) pointer_l;
    cdd* cdd_object_r = (cdd*) pointer_r;
    cdd* cdd_result = new cdd((*cdd_object_l & *cdd_object_r));
    return (jlong)cdd_result;
}

/*
 * Class:     lib_CDDLib
 * Method:    disjunction
 * Signature: (JJ)J
 */
jlong JNICALL Java_lib_CDDLib_disjunction
  (JNIEnv *, jclass, jlong pointer_l, jlong pointer_r){
    cdd* cdd_object_l = (cdd*) pointer_l;
    cdd* cdd_object_r = (cdd*) pointer_r;
    cdd* cdd_result = new cdd((*cdd_object_l | *cdd_object_r));
    return (jlong)cdd_result;
}

/*
 * Class:     lib_CDDLib
 * Method:    negation
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_lib_CDDLib_negation
  (JNIEnv *, jclass, jlong cdd_pointer){
    cdd* cdd_object = (cdd*) cdd_pointer;
    cdd* cdd_result = new cdd(!(*cdd_object));
    return (jlong)cdd_result;
}

/*
 * Class:     lib_CDDLib
 * Method:    reduce
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_lib_CDDLib_reduce
  (JNIEnv *, jclass, jlong cdd_pointer){
      cdd* cdd_object = (cdd*) cdd_pointer;
      cdd* cdd_result = new cdd(cdd_reduce(*cdd_object));
      return (jlong)cdd_result;
}

/*
 * Class:     lib_CDDLib
 * Method:    interval
 * Signature: (IIII)J
 */
jlong JNICALL Java_lib_CDDLib_interval
  (JNIEnv *env, jclass cdd_class, jint i, jint j, jint lower, jint upper){
    cdd* cdd_object = new cdd(cdd_interval(i,j,lower,upper));
    return (jlong)cdd_object;
}

/*
 * Class:     lib_CDDLib
 * Method:    lower
 * Signature: (III)J
 */
jlong JNICALL Java_lib_CDDLib_lower
  (JNIEnv *, jclass, jint i, jint j, jint lower){
    cdd* cdd_object = new cdd(cdd_lower(i,j,lower));
    return (jlong)cdd_object;
}

/*
 * Class:     lib_CDDLib
 * Method:    upper
 * Signature: (III)J
 */
jlong JNICALL Java_lib_CDDLib_upper
  (JNIEnv *, jclass, jint i, jint j, jint upper){
    cdd* cdd_object = new cdd(cdd_upper(i,j,upper));
    return (jlong)cdd_object;
}

/*
 * Class:     lib_CDDLib
 * Method:    cddNodeCount
 * Signature: (J)I
 */
jint JNICALL Java_lib_CDDLib_cddNodeCount
  (JNIEnv *env, jclass cdd_class, jlong pointer){
    cdd* cdd_object = (cdd*) pointer;
    return cdd_nodecount(*cdd_object);
}

/*
 * Class:     lib_CDDLib
 * Method:    cddAddClocks
 * Signature: (I)V
 */
void JNICALL Java_lib_CDDLib_cddAddClocks
  (JNIEnv *env, jclass cdd_class, jint n){
    cdd_add_clocks(n);
}

/*
 * Class:     lib_CDDLib
 * Method:    getRootNode
 * Signature: (J)J
 */
jlong JNICALL Java_lib_CDDLib_getRootNode
  (JNIEnv *env, jclass java_class, jlong pointer){
    cdd* cdd_object = (cdd*) pointer;
    cddNode* node = (cddNode*)cdd_object->handle();
    return (jlong)node;
}

/*
 * Class:     lib_CDDLib
 * Method:    getNodeLevel
 * Signature: (J)I
 */
jint JNICALL Java_lib_CDDLib_getNodeLevel
  (JNIEnv *env, jclass java_class, jlong pointer){
    cddNode* node = (cddNode*) pointer;
    return node->level;
}

/*
 * Class:     lib_CDDLib
 * Method:    isElemArrayNullTerminator
 * Signature: (JI)Z
 */
jboolean JNICALL Java_lib_CDDLib_isElemArrayNullTerminator
  (JNIEnv *env, jclass java_class, jlong cddNode_pointer, jint index){
    //char* first_byte_of_elem = (char*)cddNode_pointer + index * sizeof(Elem);
    //return *first_byte_of_elem == 0;
    cddNode* node_ptr = (cddNode*)cddNode_pointer;
    return node_ptr->elem[index].child == nullptr;
}

/*
 * Class:     lib_CDDLib
 * Method:    getChildFromElemArray
 * Signature: (JI)J
 */
jlong JNICALL Java_lib_CDDLib_getChildFromElemArray
  (JNIEnv *env, jclass java_class, jlong cddNode_pointer, jint index){
    cddNode* node = (cddNode*) cddNode_pointer;
    return (jlong)node->elem[index].child;
}

/*
 * Class:     lib_CDDLib
 * Method:    getBoundFromElemArray
 * Signature: (JI)I
 */
jint JNICALL Java_lib_CDDLib_getBoundFromElemArray
  (JNIEnv *env, jclass java_class, jlong cddNode_pointer, jint index){
    cddNode* node = (cddNode*) cddNode_pointer;
    return (jint)node->elem[index].bnd;
}

/*
 * Class:     lib_CDDLib
 * Method:    cddFromDbm
 * Signature: ([II)J
 */
JNIEXPORT jlong JNICALL Java_lib_CDDLib_cddFromDbm
  (JNIEnv *env, jclass, jintArray dbm, jint dim){
    // get size of dbm
    jsize len = env->GetArrayLength(dbm);

    // call library with built array
    auto converted = helper_functions::jintToC(env, dbm, len);

    cdd* cdd_object = new cdd(converted, dim);

    return (jlong) cdd_object;
}

/*
 * Class:     lib_CDDLib
 * Method:    cddPrintDot
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_lib_CDDLib_cddPrintDot__J
  (JNIEnv *, jclass, jlong cdd_pointer){
    cdd* cdd_object = (cdd*)cdd_pointer;
    cdd_printdot(cdd_object->handle(), true);
}

/*
 * Class:     lib_CDDLib
 * Method:    cddPrintDot
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_lib_CDDLib_cddPrintDot__JLjava_lang_String_2
  (JNIEnv *env, jclass, jlong cdd_pointer, jstring file_path_jstring){
    cdd* cdd_object = (cdd*)cdd_pointer;
    const char* path_name = env->GetStringUTFChars(file_path_jstring, 0);

    FILE* file = fopen(path_name, "w");
    cdd_fprintdot(file, cdd_object->handle(), true);

    fclose(file);
    env->ReleaseStringUTFChars(file_path_jstring, path_name);
}

/*
 * Class:     lib_CDDLib
 * Method:    addBddvar
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_lib_CDDLib_addBddvar
  (JNIEnv *, jclass, jint amount){
    return cdd_add_bddvar(amount);
}

/*
 * Class:     lib_CDDLib
 * Method:    cddBddvar
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_lib_CDDLib_cddBddvar
  (JNIEnv *, jclass, jint level){
      cdd* cdd_object = new cdd(cdd_bddvar(level));
      return (jlong)cdd_object;
}

/*
 * Class:     lib_CDDLib
 * Method:    cddNBddvar
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_lib_CDDLib_cddNBddvar
  (JNIEnv *, jclass, jint level){
    cdd* cdd_object = new cdd(cdd_bddnvar(level));
    return (jlong)cdd_object;
}

/*
 * Class:     lib_CDDLib
 * Method:    isTrue
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_lib_CDDLib_isTrue
  (JNIEnv *, jclass, jlong cdd_node_pointer){
    ddNode* node = (ddNode*)cdd_node_pointer;
    return node == cddtrue;
}

/*
 * Class:     lib_CDDLib
 * Method:    isFalse
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_lib_CDDLib_isFalse
  (JNIEnv *, jclass, jlong cdd_node_pointer){
    ddNode* node = (ddNode*)cdd_node_pointer;
    return node == cddfalse;
}

/*
 * Class:     lib_CDDLib
 * Method:    cddTrue
 */
JNIEXPORT jlong JNICALL Java_lib_CDDLib_cddTrue
  (JNIEnv *, jclass){
    cdd* cdd_true_node = new cdd(cdd_true());
    return (jlong)cdd_true_node;
}

/*
 * Class:     lib_CDDLib
 * Method:    cddFalse
 */
JNIEXPORT jlong JNICALL Java_lib_CDDLib_cddFalse
  (JNIEnv *, jclass){
    cdd* cdd_false_node = new cdd(cdd_false());
    return (jlong)cdd_false_node;
}

/*
 * Class:     lib_CDDLib
 * Method:    isTerminal
 */
JNIEXPORT jboolean JNICALL Java_lib_CDDLib_isTerminal
  (JNIEnv *, jclass, jlong cdd_pointer){
    cdd* cdd_object = (cdd*)cdd_pointer;
    return cdd_isterminal(cdd_object->handle());
}

/*
 * Class:     lib_CDDLib
 * Method:    delay
 */
JNIEXPORT jlong JNICALL Java_lib_CDDLib_delay
  (JNIEnv *, jclass, jlong cdd_pointer){
    cdd* cdd_object = (cdd*)cdd_pointer;
    cdd* cdd_result = new cdd(cdd_delay(*cdd_object));
    return (jlong)cdd_result;
}

/*
 * Class:     lib_CDDLib
 * Method:    delayInvar
 */
JNIEXPORT jlong JNICALL Java_lib_CDDLib_delayInvar
  (JNIEnv *, jclass, jlong cdd_pointer, jlong cdd_invar_pointer){
    cdd* cdd_object = (cdd*)cdd_pointer;
    cdd* cdd_invar_object = (cdd*)cdd_invar_pointer;
    cdd* cdd_result = new cdd(cdd_delay_invariant(*cdd_object, *cdd_invar_object));
    return (jlong)cdd_result;
}

/*
 * Class:     lib_CDDLib
 * Method:    exist
 */
JNIEXPORT jlong JNICALL Java_lib_CDDLib_exist
  (JNIEnv *env, jclass, jlong cdd_pointer, jintArray levels, jintArray clocks){
    cdd* cdd_object = (cdd*)cdd_pointer;

    jsize levels_len = env->GetArrayLength(levels);
    auto converted_levels = helper_functions::jintToC(env, levels, levels_len);

    jsize clocks_len = env->GetArrayLength(clocks);
    auto converted_clocks = helper_functions::jintToC(env, clocks, clocks_len);

    cdd* cdd_result = new cdd(cdd_exist(*cdd_object, converted_levels, converted_clocks));
    return (jlong)cdd_result;
}