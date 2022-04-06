CMAKE_MINIMUM_REQUIRED(VERSION 3.16)

set(JCDD_NAME "jcdd")
set(JCDD_VERSION 0.0.0)
project(${JCDD_NAME} VERSION ${JCDD_VERSION} LANGUAGES CXX C)

set(CMAKE_CXX_STANDARD 17)

find_package(Java 11 REQUIRED)
find_package(Java COMPONENTS Development)

set(JAVA_AWT_LIBRARY NotNeeded)
set(JAVA_JVM_LIBRARY NotNeeded)
find_package(JNI REQUIRED)

include(UseJava)

set(JCDD_TARGETDIR "${CMAKE_BINARY_DIR}/${JCDD_NAME}" CACHE PATH "Target directory for build files")

set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${JCDD_TARGETDIR}/lib)
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${JCDD_TARGETDIR}/lib)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${JCDD_TARGETDIR}/lib) # Place dll files in lib

set(CMAKE_POSITION_INDEPENDENT_CODE ON)
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -static-libgcc -static-libstdc++")

if(WIN32)
    set(CMAKE_SHARED_LIBRARY_PREFIX "") # dont add lib prefix to dll files
endif()

set(CMAKE_PREFIX_PATH "${CMAKE_PREFIX_PATH};${CMAKE_BINARY_DIR}/ext-libs")

find_package(xxHash 0.8.0 REQUIRED COMPONENTS xxhash)
find_package(UUtils 1.1.1 REQUIRED PATHS ${CMAKE_BINARY_DIR}/ext-libs/UUtils)
find_package(UDBM 2.0.11 REQUIRED PATHS ${CMAKE_BINARY_DIR}/ext-libs/UDBM)
find_package(UCDD 0.2.1 REQUIRED PATHS ${CMAKE_BINARY_DIR}/ext-libs/UCDD)

include_directories(src)

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
add_dependencies(JCDD UCDD ucddjava)
target_link_libraries(JCDD PRIVATE ucdd-native UCDD)
