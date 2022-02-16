CMAKE_MINIMUM_REQUIRED(VERSION 3.16)

set(JCDD_NAME "jcdd")
set(JCDD_VERSION 0.0.0)
project(${JCDD_NAME} VERSION ${JCDD_VERSION} LANGUAGES CXX C)

find_package(Java 11 REQUIRED)
find_package(Java COMPONENTS Development)

set(JAVA_AWT_LIBRARY NotNeeded)
set(JAVA_JVM_LIBRARY NotNeeded)
find_package(JNI REQUIRED)

include(UseJava)
include(ExternalProject)

set(JCDD_TARGETDIR "${CMAKE_BINARY_DIR}/${JCDD_NAME}" CACHE PATH "Traget directory for build files")

set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${JCDD_TARGETDIR}/lib)
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${JCDD_TARGETDIR}/lib)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${JCDD_TARGETDIR}/lib) # Place dll files in lib

set(CMAKE_POSITION_INDEPENDENT_CODE ON)
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -static-libgcc -static-libstdc++")

if(WIN32)
    set(CMAKE_SHARED_LIBRARY_PREFIX "") # dont add lib prefix to dll files
endif()

set(EXTERNAL_INSTALL_LOCATION ${CMAKE_BINARY_DIR}/external CACHE PATH "Install location for external dependencies")
ExternalProject_Add(ucdd-ext
    GIT_REPOSITORY https://github.com/UPPAALModelChecker/UCDD
    GIT_TAG origin/main
    CMAKE_ARGS -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE} -DCMAKE_C_COMPILER=${CMAKE_C_COMPILER} -DCMAKE_CXX_COMPILER=${CMAKE_CXX_COMPILER} -DCMAKE_INSTALL_PREFIX=${EXTERNAL_INSTALL_LOCATION} -DCMAKE_BUILD_TYPE=Release
    INSTALL_COMMAND ${CMAKE_COMMAND} -E copy_directory ocdd/lib ${EXTERNAL_INSTALL_LOCATION}/lib && 
                    ${CMAKE_COMMAND} -E copy_directory ../ucdd-ext/include/ ${EXTERNAL_INSTALL_LOCATION}/include
)
link_directories(${EXTERNAL_INSTALL_LOCATION}/lib)
include_directories(${EXTERNAL_INSTALL_LOCATION}/include src)

add_jar(ucddjava
    src/CDDLib.java src/DBMLib.java
    GENERATE_NATIVE_HEADERS ucdd-native
    OUTPUT_NAME jcdd
    OUTPUT_DIR ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}
)

add_library(JCDD SHARED
    src/lib_CDDLib.cpp
    src/lib_DBMLib.cpp
    src/helper_functions.cpp
)
add_dependencies(JCDD ucdd-ext ucddjava)
target_link_libraries(JCDD PRIVATE ucdd-native libcdd.a libdbm.a libbase.a libhash.a libudebug.a )