#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <jni.h>
#include <grp.h>
#include <pwd.h>
#include <attr/xattr.h>
#include <sys/acl.h>
#include <acl/libacl.h>
#include "com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper.h"

/**
 * C code for the JNI file metadata handler
 * <BR>.h generation      : javah -jni -classpath "lib/areca.jar" com.myJava.file.metadata.posix.jni.wrapper.FileAccessWrapper
 * <BR>Compilation        : gcc -c -fPIC -lacl com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper.c -o com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper.o
 * <BR>Library generation : gcc -shared -lacl -Wl,-soname,libarecafs.so -o libarecafs.so  com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper.o
 *
 * 
 *
 */

 /*
 Copyright 2005-2015, Olivier PETRUCCI.

This file is part of Areca.

    Areca is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Areca is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Areca; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 */

JNIEXPORT jint JNICALL Java_com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_getGroupId (JNIEnv * env, jclass clazz, jstring name) {
   jboolean iscopy;
   const char* groupname = (*env)->GetStringUTFChars(env, name, &iscopy);
   struct group* grp = getgrnam(groupname);

   // Free memory
   (*env)->ReleaseStringUTFChars(env, name, groupname);

   if (grp == NULL) {
      return (jint)-1;
   } else {
      return (jint)grp->gr_gid;
   }
}

JNIEXPORT jstring JNICALL Java_com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_getGroupName(JNIEnv * env, jclass clazz, jint id) {
   struct group* grp = getgrgid((int)id);

   if (grp == NULL) {
      return (jstring)NULL;
   } else {
      return (*env)->NewStringUTF(env, grp->gr_name);
   }
}

JNIEXPORT jint JNICALL Java_com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_getUserId(JNIEnv * env, jclass clazz, jstring name) {
   jboolean iscopy;
   const char* username = (*env)->GetStringUTFChars(env, name, &iscopy);
   struct passwd* usr = getpwnam(username);

   // Free memory
   (*env)->ReleaseStringUTFChars(env, name, username);

   if (usr == NULL) {
      return (jint)-1;
   } else {
      return (jint)usr->pw_uid;
   }
}

JNIEXPORT jstring JNICALL Java_com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_getUserName(JNIEnv * env, jclass clazz, jint id) {
   struct passwd* usr = getpwuid((int)id);

   if (usr == NULL) {
      return (jstring)NULL;
   } else {
      return (*env)->NewStringUTF(env, usr->pw_name);
   }
}


JNIEXPORT jobject JNICALL Java_com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_setFileOwner (JNIEnv * env, jclass clazz, jstring name, jint owner, jint group, jboolean followSymLinks) {
   jboolean iscopy;
   const char* filename = (*env)->GetStringUTFChars(env, name, &iscopy);

   jint ret;
   if (followSymLinks) {
   	ret = (jint)chown (filename, owner, group);
   } else {
   	ret = (jint)lchown (filename, owner, group);
   }

   // Attempt to find and instanciate the SetFileOwnerResult class.
   clazz = (*env)->FindClass (env, "com/myJava/file/metadata/posix/jni/wrapper/SetFileOwnerResult");
   jmethodID construct = (*env)->GetMethodID(env, clazz, "<init>", "()V");
   jmethodID midE = (*env)->GetMethodID(env, clazz, "setError", "(III)V");
   jobject obj = (*env)->NewObject(env, clazz, construct);

   // handle errors
   if (ret != 0) {
      (*env)->CallVoidMethod(env, obj, midE, ret, errno, errTranscode(errno));
   }

   // Free memory
   (*env)->ReleaseStringUTFChars(env, name, filename);

   return obj;
}

JNIEXPORT jobject JNICALL Java_com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_setFileModeImpl (JNIEnv * env, jclass clazz, jstring name, jint bitfield) {
   jboolean iscopy;
   const char* filename = (*env)->GetStringUTFChars(env, name, &iscopy);

   jint ret;
   //if (followSymLinks) {
   ret = (jint)chmod (filename, bitfield);
   //} else {
   //   ret = (jint)lchmod (filename, bitfiel);
   //}

   // Attempt to find and instanciate the SetFileModeResult class.
   clazz = (*env)->FindClass (env, "com/myJava/file/metadata/posix/jni/wrapper/SetFileModeResult");
   jmethodID construct = (*env)->GetMethodID(env, clazz, "<init>", "()V");
   jmethodID midE = (*env)->GetMethodID(env, clazz, "setError", "(III)V");
   jobject obj = (*env)->NewObject(env, clazz, construct);

   // handle errors
   if (ret != 0) {
      (*env)->CallVoidMethod(env, obj, midE, ret, errno, errTranscode(errno));
   }

   // Free memory
   (*env)->ReleaseStringUTFChars(env, name, filename);

   return obj;
}

JNIEXPORT jobject JNICALL Java_com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_getData (JNIEnv * env, jclass clazz, jstring name, jboolean followSymLinks) {
   // Get data
   struct stat myfstat;

   int ret;
   jboolean iscopy;
   const char* filename = (*env)->GetStringUTFChars(env, name, &iscopy);

   if (followSymLinks) {
      ret = stat64 (filename, &myfstat);
   } else {
      ret = lstat64 (filename, &myfstat);
   }

   // Free memory
   (*env)->ReleaseStringUTFChars(env, name, filename);

   // Attempt to find and instanciate the GetDataResult class.
   clazz = (*env)->FindClass (env, "com/myJava/file/metadata/posix/jni/wrapper/GetDataResult");
   jmethodID construct = (*env)->GetMethodID(env, clazz, "<init>", "()V");
   jmethodID midE = (*env)->GetMethodID(env, clazz, "setError", "(III)V");
   jobject obj = (*env)->NewObject(env, clazz, construct);

   // handle errors
   if (ret != 0) {
      (*env)->CallVoidMethod(env, obj, midE, ret, errno, errTranscode(errno));
   } else {
      // Attempt to find the fields.
      jfieldID fst_ctime = (*env)->GetFieldID (env, clazz, "st_ctime", "J");
      jfieldID fst_mtime = (*env)->GetFieldID (env, clazz, "st_mtime", "J");
      jfieldID fst_atime = (*env)->GetFieldID (env, clazz, "st_atime", "J");
      jfieldID fst_mode = (*env)->GetFieldID (env, clazz, "st_mode", "J");
      jfieldID fst_size = (*env)->GetFieldID (env, clazz, "st_size", "J");
      jfieldID fst_dev = (*env)->GetFieldID (env, clazz, "st_dev", "J");
      jfieldID fst_ino = (*env)->GetFieldID (env, clazz, "st_ino", "J");
      jfieldID fst_nlink = (*env)->GetFieldID (env, clazz, "st_nlink", "J");
      jfieldID fst_uid = (*env)->GetFieldID (env, clazz, "st_uid", "J");
      jfieldID fst_gid = (*env)->GetFieldID (env, clazz, "st_gid", "J");
      jfieldID fst_rdev = (*env)->GetFieldID (env, clazz, "st_rdev", "J");
      jfieldID fst_blksize = (*env)->GetFieldID (env, clazz, "st_blksize", "J");
      jfieldID fst_blocks = (*env)->GetFieldID (env, clazz, "st_blocks", "J");

      // Init return object
      (*env)->SetLongField (env, obj, fst_ctime, myfstat.st_ctime);
      (*env)->SetLongField (env, obj, fst_mtime, myfstat.st_mtime);
      (*env)->SetLongField (env, obj, fst_atime, myfstat.st_atime);
      (*env)->SetLongField (env, obj, fst_mode, myfstat.st_mode);
      (*env)->SetLongField (env, obj, fst_size, myfstat.st_size);
      (*env)->SetLongField (env, obj, fst_dev, myfstat.st_dev);
      (*env)->SetLongField (env, obj, fst_ino, myfstat.st_ino);
      (*env)->SetLongField (env, obj, fst_nlink, myfstat.st_nlink);
      (*env)->SetLongField (env, obj, fst_uid, myfstat.st_uid);
      (*env)->SetLongField (env, obj, fst_gid, myfstat.st_gid);
      (*env)->SetLongField (env, obj, fst_rdev, myfstat.st_rdev);
      (*env)->SetLongField (env, obj, fst_blksize, myfstat.st_blksize);
      (*env)->SetLongField (env, obj, fst_blocks, myfstat.st_blocks);
   }

   // Return object
   return obj;
}

JNIEXPORT jobject JNICALL Java_com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_getAttributeNames(JNIEnv *env, jclass clazz, jstring name, jint buffer, jboolean followSymLinks) {
   ssize_t length;
   jboolean iscopy;
   const char* filename = (*env)->GetStringUTFChars(env, name, &iscopy);
   char* list = (char*)malloc(buffer*sizeof(char));
   char* ptr;
   char* d;
   char* pl;
   jstring nm;

   if (followSymLinks) {
      length = listxattr(filename, list, buffer);
   } else {
      length = llistxattr(filename, list, buffer);
   }

   // Attempt to find and instanciate the GetAttributeNamesResult class.
   clazz = (*env)->FindClass (env, "com/myJava/file/metadata/posix/jni/wrapper/GetAttributeNamesResult");
   jmethodID construct = (*env)->GetMethodID(env, clazz, "<init>", "()V");
   jobject obj = (*env)->NewObject(env, clazz, construct);
   jmethodID mid = (*env)->GetMethodID(env, clazz, "addName", "(Ljava/lang/String;)V");

   // Check allocated buffer
   if (buffer < length || errno == ERANGE || errno == ENOTSUP) {
      jmethodID midE = (*env)->GetMethodID(env, clazz, "setError", "(III)V");
      (*env)->CallVoidMethod(env, obj, midE, -1, errno, errTranscode(errno));
   } else if (length > 0) {
      pl = list;
      ptr = strchr(list, '\0')+1;
      do {
         d = (char*)malloc((ptr-pl)*sizeof(char));
         strcpy(d, pl);
         nm = (*env)->NewStringUTF(env, d);
         (*env)->CallVoidMethod(env, obj, mid, nm);

         free(d);
         pl = ptr;
         ptr = strchr(ptr, '\0')+1;
      } while (ptr <= list + length);
   }

   // Free memory
   (*env)->ReleaseStringUTFChars(env, name, filename);
   free(list);

   // Return object
   return obj;
}


JNIEXPORT jobject JNICALL Java_com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_getAttributeValue (JNIEnv * env, jclass clazz, jstring fname, jstring aname, jlong buffer, jboolean followSymLinks) {
   jboolean iscopy;
   const char* filename = (*env)->GetStringUTFChars(env, fname, &iscopy);
   const char* attrname = (*env)->GetStringUTFChars(env, aname, &iscopy);
   char* value = malloc(buffer*sizeof(char));
   ssize_t length;

   if (followSymLinks) {
      length = getxattr(filename, attrname, value, buffer);
   } else {
      length = lgetxattr(filename, attrname, value, buffer);
   }

   // Attempt to find and instanciate the GetAttributeValueResult class.
   clazz = (*env)->FindClass (env, "com/myJava/file/metadata/posix/jni/wrapper/GetAttributeValueResult");
   jmethodID construct = (*env)->GetMethodID(env, clazz, "<init>", "()V");
   jobject obj = (*env)->NewObject(env, clazz, construct);
   jmethodID mid = (*env)->GetMethodID(env, clazz, "setValue", "([B)V");

   // Check allocated buffer
   if (length == -1 && (errno == ENOATTR || errno == ERANGE || errno == ENOTSUP)) {
      jmethodID midE = (*env)->GetMethodID(env, clazz, "setError", "(III)V");
      (*env)->CallVoidMethod(env, obj, midE, -1, errno, errTranscode(errno));
   } else if (length > 0) {
      jbyteArray jb;
      jb=(*env)->NewByteArray(env, length);
      (*env)->SetByteArrayRegion(env, jb, 0, length, (jbyte *)value);
      (*env)->CallVoidMethod(env, obj, mid, jb);
   }

   // Free memory
   (*env)->ReleaseStringUTFChars(env, fname, filename);
   (*env)->ReleaseStringUTFChars(env, aname, attrname);
   free(value);

   // Return object
   return obj;
}

JNIEXPORT jobject JNICALL Java_com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_setAttributeValue(JNIEnv * env, jclass clazz, jstring fname, jstring aname, jbyteArray adata, jboolean followSymLinks) {
   jboolean iscopy;
   const char* filename = (*env)->GetStringUTFChars(env, fname, &iscopy);
   const char* attrname = (*env)->GetStringUTFChars(env, aname, &iscopy);
   int ret;
   jint asize = (*env)->GetArrayLength(env, adata);

   // get primitive data type
   char* data = (*env)->GetByteArrayElements(env, adata, &iscopy);

   // set the attribute value
   if (followSymLinks) {
      ret = setxattr(filename, attrname, data, (int)asize, 0);
   } else {
      ret = lsetxattr(filename, attrname, data, (int)asize, 0);
   }

   // Attempt to find and instanciate the SetAttributeValueResult class.
   clazz = (*env)->FindClass (env, "com/myJava/file/metadata/posix/jni/wrapper/SetAttributeValueResult");
   jmethodID construct = (*env)->GetMethodID(env, clazz, "<init>", "()V");
   jobject obj = (*env)->NewObject(env, clazz, construct);

   // Check result
   if (ret != 0 && (errno == ENOSPC || errno == EDQUOT || errno == ENOTSUP)) {
      jmethodID midE = (*env)->GetMethodID(env, clazz, "setError", "(III)V");
      (*env)->CallVoidMethod(env, obj, midE, ret, errno, errTranscode(errno));
   }

   // Free memory
   (*env)->ReleaseStringUTFChars(env, fname, filename);
   (*env)->ReleaseStringUTFChars(env, aname, attrname);
   (*env)->ReleaseByteArrayElements(env, adata, data, 0);

   // Return object
   return obj;
}

JNIEXPORT jobject JNICALL Java_com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_getACL (JNIEnv * env, jclass clazz, jstring name, jboolean defaultACL) {
   jboolean iscopy;
   const char* filename = (*env)->GetStringUTFChars(env, name, &iscopy);
   acl_t aclPointer;
   acl_type_t type = defaultACL ? ACL_TYPE_DEFAULT : ACL_TYPE_ACCESS;

   // Get ACL
   aclPointer = acl_get_file(filename, type);

   // Attempt to find the GetACLResult class, get the class' constructor method and Create object
   clazz = (*env)->FindClass (env, "com/myJava/file/metadata/posix/jni/wrapper/GetACLResult");
   jmethodID construct = (*env)->GetMethodID(env, clazz, "<init>", "()V");
   jobject obj = (*env)->NewObject(env, clazz, construct);
   jmethodID midE = (*env)->GetMethodID(env, clazz, "setError", "(III)V");

   // Check allocated buffer
   if (aclPointer == NULL) {
      (*env)->CallVoidMethod(env, obj, midE, -1, errno, errTranscode(errno));
   } else {
      jmethodID mid = (*env)->GetMethodID(env, clazz, "addEntry", "(IIZZZ)V");

      // Iterate on the ACL
      int result;
      int tResult;
      int pResult;
      void* aclQualifier = NULL;
      acl_tag_t tag;
      acl_permset_t permSet;
      acl_entry_t entryPointer;
      int qualifierOK;

      result = acl_get_entry(aclPointer, ACL_FIRST_ENTRY, &entryPointer);

      while(result == 1) {
         // Process entry

         // Get tag
 	 tResult = acl_get_tag_type(entryPointer, &tag);

         if (tResult != 0) {
            (*env)->CallVoidMethod(env, obj, midE, tResult, errno, errTranscode(errno));
         } else {
            qualifierOK = 1;
            if (tag == ACL_USER || tag == ACL_GROUP) {
               // Get qualifier
               aclQualifier = acl_get_qualifier(entryPointer);
               if (aclQualifier == NULL) {
                  qualifierOK = 0;
               }
            }

            if (qualifierOK == 0) {
               (*env)->CallVoidMethod(env, obj, midE, -1, errno, errTranscode(errno));
            } else {
               // Get permissions set
 	       pResult = acl_get_permset(entryPointer, &permSet);
               if (pResult != 0) {
                  (*env)->CallVoidMethod(env, obj, midE, pResult, errno, errTranscode(errno));
               } else {
                  // Call Java method
                  jint qual = -1;
                  if (tag == ACL_USER) {
                     qual = (jint)*((uid_t*)aclQualifier);
                  } else if (tag == ACL_GROUP) {
                     qual = (jint)*((gid_t*)aclQualifier);
                  }
                  (*env)->CallVoidMethod(env, obj, mid, (jint)tag, qual, acl_get_perm(permSet, ACL_READ), acl_get_perm(permSet, ACL_WRITE), acl_get_perm(permSet, ACL_EXECUTE));
               }
            }
         }

         // Free memory
         if (aclQualifier != NULL) {
            acl_free(aclQualifier);
         }

         // Fetch next entry
         result = acl_get_entry(aclPointer, ACL_NEXT_ENTRY, &entryPointer);
      }

      if (result < 0) {
         (*env)->CallVoidMethod(env, obj, midE, result, errno, errTranscode(errno));
      }
   }
   // Free memory
   (*env)->ReleaseStringUTFChars(env, name, filename);
   acl_free(aclPointer);

   // Return object
   return obj;
}

JNIEXPORT jboolean JNICALL Java_com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_test (JNIEnv * env, jclass clazz) {
   return (jboolean)1;
}

int errTranscode(int errorNumber) {
   switch(errorNumber) {
      case ENOMEM :
         return com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_ERR_NOT_ENOUGH_MEMORY;
      case ENOTSUP :
         return com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_ERR_UNSUPPORTED;
      case ENAMETOOLONG :
         return com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_ERR_NAME_TOOLONG;
      case ENOTDIR :
         return com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_ERR_NOT_A_DIRECTORY;
      case ENOENT :
         return com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_ERR_FILE_NOT_FOUND;
      case ERANGE :
         return com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_ERR_BUFFER_TOO_SMALL;
      case ENOATTR :
         return com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_ERR_ATTRIBUTE_NOT_FOUND;
      case EINVAL :
         return com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_ERR_INVALID_DATA;
      case ENOSPC :
         return com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_ERR_NOT_ENOUGH_DISK_SPACE;
      case EPERM :
         return com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_ERR_PERMISSION_DENIED;
      case EROFS :
         return com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_ERR_RO_FILESYSTEM;
      case EDQUOT :
         return com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_ERR_DISK_QUOTA;
      case EFAULT :
         return com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_ERR_INTERNAL;
      case EIO :
         return com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_ERR_IO;
      case ENOSYS :
         return com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_ERR_NOT_IMPLEMENTED;
      case ELOOP :
         return com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_ERR_LOOP;
      case EACCES :
         return com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_ERR_ACCESS_DENIED;
      default :
         return com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_ERR_UNEXPECTED;
   }
}

JNIEXPORT jobject JNICALL Java_com_myJava_file_metadata_posix_jni_wrapper_FileAccessWrapper_setACL(JNIEnv * env, jclass clazz, jstring name, jobject sourceacl, jint size, jboolean defaultACL) {
   jboolean iscopy;
   const char* filename = (*env)->GetStringUTFChars(env, name, &iscopy);
   int result;
   acl_type_t type = defaultACL ? ACL_TYPE_DEFAULT : ACL_TYPE_ACCESS;
   acl_t targetacl;

   // Init Java classes and methods variables
   jclass resultClazz = (*env)->FindClass (env, "com/myJava/file/metadata/posix/jni/wrapper/SetACLResult");
   jmethodID construct = (*env)->GetMethodID(env, resultClazz, "<init>", "()V");
   jmethodID midE = (*env)->GetMethodID(env, resultClazz, "setError", "(III)V");

   // Create object
   jobject retObject = (*env)->NewObject(env, resultClazz, construct);

   // Init the ACL
   targetacl = acl_init((int)size);

   if (targetacl == NULL) {
      (*env)->CallVoidMethod(env, retObject, midE, -1, errno, errTranscode(errno));
   } else {
      // Load ACL & ACLEntry class and attributes
      jclass aclClazz = (*env)->FindClass (env, "com/myJava/file/metadata/posix/ACL");
      jmethodID midG = (*env)->GetMethodID(env, aclClazz, "getEntryAt", "(I)Lcom/myJava/file/metadata/posix/ACLEntry;");

      jclass aclEntryClazz = (*env)->FindClass (env, "com/myJava/file/metadata/posix/ACLEntry");
      jfieldID fr = (*env)->GetFieldID (env, aclEntryClazz, "r", "Z");
      jfieldID fw = (*env)->GetFieldID (env, aclEntryClazz, "w", "Z");
      jfieldID fx = (*env)->GetFieldID (env, aclEntryClazz, "x", "Z");
      jfieldID ftag = (*env)->GetFieldID (env, aclEntryClazz, "tag", "I");
      jfieldID fidentifier = (*env)->GetFieldID (env, aclEntryClazz, "identifier", "I");

      // Populate the acl
      result = 0;
      jobject jentry;
      jint tag;
      jint identifier;
      jboolean r;
      jboolean w;
      jboolean x;
      acl_entry_t targetEntry;
      id_t qual;
      acl_permset_t permSet;
      int i;

      for (i=0; (i<size) && (result == 0); i++) {
         // Retrieve entry data
         jentry = (*env)->CallObjectMethod(env, sourceacl, midG, (jint)i);
         r = (*env)->GetIntField(env, jentry, fr);
         w = (*env)->GetIntField(env, jentry, fw);
         x = (*env)->GetIntField(env, jentry, fx);
         tag = (*env)->GetIntField(env, jentry, ftag);
         identifier = (*env)->GetIntField(env, jentry, fidentifier);

         // Add new entry in target acl
         result = acl_create_entry(&targetacl, &targetEntry);
         if (result == 0) {

            // Initialize the new entry
            result = acl_set_tag_type(targetEntry, (acl_tag_t)tag);
            if (result == 0) {
               if ((acl_tag_t)tag == ACL_USER) {
                  qual = (uid_t)identifier;
                  result = acl_set_qualifier(targetEntry, &qual);
               } else if ((acl_tag_t)tag == ACL_GROUP) {
                  qual = (gid_t)identifier;
                  result = acl_set_qualifier(targetEntry, &qual);
               }
            }

            if (result == 0) {
 	       result = acl_get_permset(targetEntry, &permSet);

               if (r && result == 0) {
                  result = acl_add_perm(permSet, ACL_READ);
               }
               if (w && result == 0) {
                  result = acl_add_perm(permSet, ACL_WRITE);
               }
               if (x && result == 0) {
                  result = acl_add_perm(permSet, ACL_EXECUTE);
               }

               if (result == 0) {
                  result = acl_set_permset(targetEntry, permSet);
               }
            }
         }
      }

      if (result == 0) {
         // set the acl
         result = acl_set_file(filename, type, targetacl);
      }

      // handle errors
      if (result != 0) {
         (*env)->CallVoidMethod(env, retObject, midE, result, errno, errTranscode(errno));
      }

      // Free memory
      acl_free(targetacl);
   }

   // Free memory
   (*env)->ReleaseStringUTFChars(env, name, filename);

   // Return object
   return retObject;
}
