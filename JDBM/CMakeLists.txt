CMAKE_MINIMUM_REQUIRED(VERSION 3.16)

set(JDBM_NAME "jdbm")
set(JDBM_VERSION 0.0.0)
project(${JDBM_NAME} VERSION ${JDBM_VERSION} LANGUAGES CXX C)

find_package(Java 11 REQUIRED)
find_package(Java COMPONENTS Development)

set(JAVA_JVM_LIBRARY NotNeeded)
find_package(JNI REQUIRED)

include(UseJava)
include(ExternalProject)

set(JDBM_TARGETDIR "${CMAKE_BINARY_DIR}/${JDBM_NAME}" CACHE PATH "Traget directory for build files")

set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${JDBM_TARGETDIR}/lib)
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${JDBM_TARGETDIR}/lib)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${JDBM_TARGETDIR}/lib) # Place dll files in lib

set(CMAKE_POSITION_INDEPENDENT_CODE ON)
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -static-libgcc -static-libstdc++")

if(WIN32)
    set(CMAKE_SHARED_LIBRARY_PREFIX "") # dont add lib prefix to dll files
endif()

set(EXTERNAL_INSTALL_LOCATION ${CMAKE_BINARY_DIR}/external CACHE PATH "Install location for external dependencies")
ExternalProject_Add(udbm-ext
    GIT_REPOSITORY https://github.com/UPPAALModelChecker/UDBM
    GIT_TAG v2.0.10
    CMAKE_ARGS -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE} -DCMAKE_C_COMPILER=${CMAKE_C_COMPILER} -DCMAKE_CXX_COMPILER=${CMAKE_CXX_COMPILER} -DCMAKE_INSTALL_PREFIX=${EXTERNAL_INSTALL_LOCATION} -DCMAKE_BUILD_TYPE=Release
    INSTALL_COMMAND ${CMAKE_COMMAND} -E copy_directory udbm/lib ${EXTERNAL_INSTALL_LOCATION}/lib && 
                    ${CMAKE_COMMAND} -E copy_directory ../udbm-ext/include/ ${EXTERNAL_INSTALL_LOCATION}/include
)
link_directories(${EXTERNAL_INSTALL_LOCATION}/lib)
include_directories(${EXTERNAL_INSTALL_LOCATION}/include)

add_jar(udbmjava
    src/DBMLib.java
    GENERATE_NATIVE_HEADERS udbm-native
    OUTPUT_NAME jdbm
    OUTPUT_DIR ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}
)

add_library(JDBM SHARED
    src/lib_DBMLib.cpp
)
add_dependencies(JDBM udbm-ext udbmjava)
target_link_libraries(JDBM PRIVATE udbm-native libdbm.a libbase.a libhash.a libudebug.a )