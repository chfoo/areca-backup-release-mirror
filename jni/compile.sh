#!/usr/bin/env bash
########################################################
# JNI Compilation script
# If you want to generate the .h file, use : "javah -jni -classpath "lib/areca.jar" com.myJava.file.metadata.posix.jni.wrapper.FileAccessWrapper"
########################################################
gcc -c -fPIC -lacl com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper.c -o com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper.o
gcc -shared -lacl -Wl,-soname,libarecafs.so -o libarecafs.so  com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper.o