# J-Ecdar

This is a model checking engine for ECDAR (Environment for Compositional Design and Analysis of Real Time Systems) 
written in Java, using JNI for linking with the UDBM library.

In order to run you need to compile the native module. See section Building JDBM. The resulting (jar and so/dll/dylib) 
files should be places in the "lib/" folder. 

## Building JDBM

### Linux
```
apt-get update && apt-get install cmake openjdk-11-jdk g++
cd JDBM && mkdir build
cmake -B build/
cmake --build build/ 
```

### Windows
```
apt-get update && apt-get install cmake openjdk-11-jdk mingw-w64-x86-64-dev mingw-w64-tools g++-mingw-w64-x86-64
cd JDBM && mkdir build-win
cmake -B build-win/ --DCMAKE_TOOLCHAIN_FILE=./toolchain-mingw.cmake
cmake --build build-win/ 
```

Target files are place in "<build>/jdbm/lib/". For development move the files to the "lib/" folder.