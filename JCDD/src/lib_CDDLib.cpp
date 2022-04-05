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

    jsize num_levels = env->GetArrayLength(levels);
    auto converted_levels = helper_functions::jintToC(env, levels, num_levels);
    jsize num_clocks = env->GetArrayLength(clocks);
    auto converted_clocks = helper_functions::jintToC(env, clocks, num_clocks);

    cdd* cdd_result = new cdd(cdd_exist(*cdd_object, converted_levels, converted_clocks, num_levels, num_clocks));
    return (jlong)cdd_result;
}

/*
 * Class:     lib_CDDLib
 * Method:    past
 */
JNIEXPORT jlong JNICALL Java_lib_CDDLib_past
  (JNIEnv *, jclass, jlong cdd_pointer){
    cdd* cdd_object = (cdd*)cdd_pointer;
    cdd* cdd_result = new cdd(cdd_past(*cdd_object));
    return (jlong)cdd_result;
}

/*
 * Class:     lib_CDDLib
 * Method:    removeNegative
 */
JNIEXPORT jlong JNICALL Java_lib_CDDLib_removeNegative
  (JNIEnv *, jclass, jlong cdd_pointer){
    cdd* cdd_object = (cdd*)cdd_pointer;
    cdd* cdd_result = new cdd(cdd_remove_negative(*cdd_object));
    return (jlong)cdd_result;
}

/*
 * Class:     lib_CDDLib
 * Method:    applyReset
 */
JNIEXPORT jlong JNICALL Java_lib_CDDLib_applyReset
  (JNIEnv *env, jclass, jlong cdd_pointer, jintArray clock_resets, jintArray clock_values, jintArray bool_resets, jintArray bool_values){
    cdd* cdd_object = (cdd*)cdd_pointer;

    jsize num_clock_resets = env->GetArrayLength(clock_resets);
    auto converted_clock_resets = helper_functions::jintToC(env, clock_resets, num_clock_resets);
    auto converted_clock_values = helper_functions::jintToC(env, clock_values, num_clock_resets);

    jsize num_bool_resets = env->GetArrayLength(clock_resets);
    auto converted_bool_resets = helper_functions::jintToC(env, bool_resets, num_bool_resets);
    auto converted_bool_values = helper_functions::jintToC(env, bool_values, num_bool_resets);

    cdd* cdd_result = new cdd(cdd_apply_reset(*cdd_object,
            converted_clock_resets, converted_clock_values, num_bool_resets,
            converted_bool_resets, converted_bool_values, num_bool_resets));
    return (jlong)cdd_result;
}

/*
 * Class:     lib_CDDLib
 * Method:    minus
 */
JNIEXPORT jlong JNICALL Java_lib_CDDLib_minus
  (JNIEnv *, jclass, jlong cdd_l, jlong cdd_r){
    cdd* cdd_l_object = (cdd*)cdd_l;
    cdd* cdd_r_object = (cdd*)cdd_r;
    cdd* cdd_result = new cdd((*cdd_l_object - *cdd_r_object));
    return (jlong)cdd_result;
}

/*
 * Class:     lib_CDDLib
 * Method:    copy
 */
JNIEXPORT jlong JNICALL Java_lib_CDDLib_copy
  (JNIEnv *, jclass, jlong cdd_pointer){
    cdd* cdd_object = (cdd*)cdd_pointer;
    cdd* cdd_copy = new cdd(*cdd_object);
    return (jlong)cdd_copy;
}

/*
 * Class:     lib_CDDLib
 * Method:    transition
 */
JNIEXPORT jlong JNICALL Java_lib_CDDLib_transition
  (JNIEnv *env, jclass, jlong cdd_pointer, jlong cdd_guard_pointer, jintArray clock_resets, jintArray clock_values, jintArray bool_resets, jintArray bool_values){
    cdd* cdd_object = (cdd*)cdd_pointer;
    cdd* cdd_guard_object = (cdd*)cdd_guard_pointer;

    jsize num_clock_resets = env->GetArrayLength(clock_resets);
    auto converted_clock_resets = helper_functions::jintToC(env, clock_resets, num_clock_resets);
    auto converted_clock_values = helper_functions::jintToC(env, clock_values, num_clock_resets);

    jsize num_bool_resets = env->GetArrayLength(clock_resets);
    auto converted_bool_resets = helper_functions::jintToC(env, bool_resets, num_bool_resets);
    auto converted_bool_values = helper_functions::jintToC(env, bool_values, num_bool_resets);

    cdd* cdd_result = new cdd(cdd_transition(*cdd_object, *cdd_guard_object,
            converted_clock_resets, converted_clock_values, num_clock_resets,
            converted_bool_resets, converted_bool_values, num_bool_resets));
    return (jlong)cdd_result;
}

/*
 * Class:     lib_CDDLib
 * Method:    transitionBack
 */
JNIEXPORT jlong JNICALL Java_lib_CDDLib_transitionBack
  (JNIEnv *env, jclass, jlong cdd_pointer, jlong cdd_guard_pointer, jlong cdd_update_pointer, jintArray clock_resets, jintArray bool_resets){
    cdd* cdd_object = (cdd*)cdd_pointer;
    cdd* cdd_guard_object = (cdd*)cdd_guard_pointer;
    cdd* cdd_update_object = (cdd*)cdd_update_pointer;

    jsize num_clock_resets = env->GetArrayLength(clock_resets);
    auto converted_clock_resets = helper_functions::jintToC(env, clock_resets, num_clock_resets);

    jsize num_bool_resets = env->GetArrayLength(clock_resets);
    auto converted_bool_resets = helper_functions::jintToC(env, bool_resets, num_bool_resets);

    cdd* cdd_result = new cdd(cdd_transition_back(*cdd_object, *cdd_guard_object, *cdd_update_object,
            converted_clock_resets, num_clock_resets,
            converted_bool_resets, num_bool_resets));
    return (jlong)cdd_result;
}

/*
 * Class:     lib_CDDLib
 * Method:    transitionBackPast
 */
JNIEXPORT jlong JNICALL Java_lib_CDDLib_transitionBackPast
  (JNIEnv *env, jclass, jlong cdd_pointer, jlong cdd_guard_pointer, jlong cdd_update_pointer, jintArray clock_resets, jintArray bool_resets){
    cdd* cdd_object = (cdd*)cdd_pointer;
    cdd* cdd_guard_object = (cdd*)cdd_guard_pointer;
    cdd* cdd_update_object = (cdd*)cdd_update_pointer;

    jsize num_clock_resets = env->GetArrayLength(clock_resets);
    auto converted_clock_resets = helper_functions::jintToC(env, clock_resets, num_clock_resets);

    jsize num_bool_resets = env->GetArrayLength(clock_resets);
    auto converted_bool_resets = helper_functions::jintToC(env, bool_resets, num_bool_resets);

    cdd* cdd_result = new cdd(cdd_transition_back_past(*cdd_object, *cdd_guard_object, *cdd_update_object,
            converted_clock_resets, num_clock_resets,
            converted_bool_resets, num_bool_resets));
    return (jlong)cdd_result;
}

/*
 * Class:     lib_CDDLib
 * Method:    predt
 */
JNIEXPORT jlong JNICALL Java_lib_CDDLib_predt
  (JNIEnv *env, jclass, jlong cdd_target_pointer, jlong cdd_safe_pointer){
    cdd* cdd_target_object = (cdd*)cdd_target_pointer;
    cdd* cdd_safe_object = (cdd*)cdd_safe_pointer;

    cdd* cdd_result = new cdd(cdd_predt(*cdd_target_object, *cdd_safe_object));
    return (jlong)cdd_result;
}

/*
 * Class:     lib_CDDLib
 * Method:    extractBddAndDbm
 */
JNIEXPORT jlong JNICALL Java_lib_CDDLib_extractBddAndDbm
  (JNIEnv *env, jclass, jlong cdd_pointer){
    cdd* cdd_object = (cdd*)cdd_pointer;

    extraction_result* result = new extraction_result(cdd_extract_bdd_and_dbm(*cdd_object));
    return (jlong)result;
}

/*
 * Class:     lib_CDDLib
 * Method:    getCddPartFromExtractionResult
 */
JNIEXPORT jlong JNICALL Java_lib_CDDLib_getCddPartFromExtractionResult
  (JNIEnv *env, jclass, jlong extraction_result_pointer){
    extraction_result* extraction_result_object = (extraction_result*)extraction_result_pointer;

    cdd* result = &(extraction_result_object->CDD_part);
    return (jlong)result;
}

/*
 * Class:     lib_CDDLib
 * Method:    getBddPartFromExtractionResult
 */
JNIEXPORT jlong JNICALL Java_lib_CDDLib_getBddPartFromExtractionResult
  (JNIEnv *env, jclass, jlong extraction_result_pointer){
    extraction_result* extraction_result_object = (extraction_result*)extraction_result_pointer;

    cdd* result = &(extraction_result_object->BDD_part);
    return (jlong)result;
}

/*
 * Class:     lib_CDDLib
 * Method:    getDbmFromExtractionResult
 */
JNIEXPORT jintArray JNICALL Java_lib_CDDLib_getDbmFromExtractionResult
  (JNIEnv *env, jclass, jlong extraction_result_pointer){
    extraction_result* result_object = (extraction_result*)extraction_result_pointer;

    return helper_functions::cToJint(env, result_object->dbm, result_object->CDD_part.numClocks());
}


