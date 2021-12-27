# Ecdar-Engine

You can read about calling C++ code from Java here: https://www.ibm.com/developerworks/java/tutorials/j-jni/j-jni.html

How to run:
1. Build the library (see instructions below)

2. Run from IntelliJ.

## Building DBMLib
The DBM static library files should be placed in the DBMLib/lib folder.

Compile from the root folder.

### Windows
`g++ -fPIC -shared -I"JAVA_HOME\include" -I"JAVA_HOME\include\win32" -I"DBMLib\include" DBMLib\src\main\java\lib\lib_DBMLib.cpp DBMLib\libs\libdbm.a DBMLib\libs\libbase.a DBMLib\libs\libhash.a -o src/DBM.dll`


### Linux
`g++ -fPIC -shared -I"JAVA_HOME/include" -I"JAVA_HOME/include/linux" -I"DBMLib/include" DBMLib/src/main/java/lib/lib_DBMLib.cpp DBMLib/libs/*.a -o src/libDBM.so`


## Adding more methods to the DBM library
If you want to support more methods from the DBM library you have to:
1. Add them as native methods in DBMLib/src/main/java/lib/DBMLib.java
2. From the root directory, run "javac DBMLib/src/main/java/lib/DBMLib.java"
3. To create the jar file run "jar cf lib/DBMLib.jar DBMLib/src/main/java/lib/DBMLib.class"
4. From the DBMLib/src/main/java/lib folder, regenerate the C++ header file "javac -h . DBMLib.java"
5. Add the corresponding methods to lib_DBMLib.cpp
6. Rebuild the library using the instructions in the section above