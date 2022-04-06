# J-Ecdar

This is a model checking engine for ECDAR (Environment for Compositional Design and Analysis of Real Time Systems) 
written in Java, using JNI for linking with the UCDD library.

In order to run you need to clone the protobuf submodule and compile the native module. See section Building JCDD. The resulting (jar and so/dll/dylib) 
files should be places in the "lib/" folder. 

To clone the protobuf submodule, run the following command or run the subModulesUpdate gradle task:

``git submodule update --init --recursive``

## Building JCDD

### Linux
```
apt-get update && apt-get install cmake openjdk-11-jdk g++
cd JCDD && mkdir build
./getlibs.sh
cmake -B build/
cmake --build build/ 
```

### Windows
```
apt-get update && apt-get install cmake openjdk-11-jdk mingw-w64-x86-64-dev mingw-w64-tools g++-mingw-w64-x86-64
cd JCDD && mkdir build-win
./getlibs.sh
cmake -B build-win/ -DCMAKE_TOOLCHAIN_FILE=./toolchain-x86_64-w64-mingw32.cmake
cmake --build build-win/ 
```

Target files are place in "<build>/jcdd/lib/". For development move the files to the "lib/" folder.