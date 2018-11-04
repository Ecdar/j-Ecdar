# Ecdar-Engine

You can read about calling C++ code from Java here: https://www.ibm.com/developerworks/java/tutorials/j-jni/j-jni.html

How to run:
1. Build the library. You have to be in the src folder. Depends on OS, for Mac it's something like "g++ -I"/Library/Developer/CommandLineTools/SDKs/MacOSX.sdk/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers" lib_DBMLib.cpp ../dbm/libs/*.a -o libDBM.dylib" (the .a files are static libraries generated from dbm)

2. Run from IntelliJ.


If you want to support more methods from the DBM library you have to:
1. Add them as native methods in src/lib/DBMLib.java
2. From src/lib, run "javac DBMLib.java"
3. From the src folder, regenerate the C++ header file "javah lib.DBMLib"
4. Add the corresponding methods to lib_DBMLib.cpp
5. Rebuild the library
