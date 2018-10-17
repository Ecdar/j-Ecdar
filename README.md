# Ecdar-Engine

You can read about calling C++ code from Java here: https://www.ibm.com/developerworks/java/tutorials/j-jni/j-jni.html

How to run:
1. Build the library. Depends on OS, I use something like "gcc -I"/Library/Developer/CommandLineTools/SDKs/MacOSX.sdk/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers" DBMLib.cpp -o libDBM.dylib"
2. Run "javac DBMLib.java"
3. Run "java DBMLib"


If you want to support more methods from the DBM library you have to:
1. Add them as native methods in DBMLib.java
2. Regenerate the C++ header file "javah DBMLib"
3. Add the corresponding methods to DBMLib.cpp
4. Rebuild the library
5. Recompile DBMLib.java
