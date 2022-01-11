# Ecdar-Engine

You can read about calling C++ code from Java here: https://www.ibm.com/developerworks/java/tutorials/j-jni/j-jni.html

How to run:
1. Build the library and jar file (see instructions below)
2. The DBM native library (so or dll file) should be placed in the `src` folder
3. DBMLib.jar file should be placed in the `lib` folder
4. Run from IntelliJ.

## Building DBMLib
The DBM static library files should be placed in the `DBMLib/libs` folder.

Compile from the root folder.

### Windows
`g++ -fPIC -shared -I"JAVA_HOME\include" -I"JAVA_HOME\include\win32" -I"DBMLib\include" DBMLib\lib\lib_DBMLib.cpp DBMLib\libs\libdbm.a DBMLib\libs\libbase.a DBMLib\libs\libhash.a -o src/DBM.dll`


### Linux
`g++ -fPIC -shared -I"JAVA_HOME/include" -I"JAVA_HOME/include/linux" -I"DBMLib/include" DBMLib/lib/lib_DBMLib.cpp DBMLib/libs/*.a -o src/libDBM.so`


## Creating the DBMLib.jar
From the DBMLib folder run:
1. `javac DBMLib/lib/DBMLib.java`
2. `jar cfe ../lib/DBMLib.jar lib.DBMLib /lib/DBMLib.class`

## Adding more methods to the DBM library
If you want to support more methods from the DBM library you have to:
1. Add them as native methods in `DBMLib/lib/DBMLib.java`
2. From the `DBMLib/lib` folder, regenerate the C++ header file `javac -h . DBMLib.java`
3. Implement the corresponding methods in `lib_DBMLib.cpp`
4. Rebuild the library and jar file using the instructions in the sections above