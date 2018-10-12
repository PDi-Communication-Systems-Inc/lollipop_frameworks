#include "utils/Log.h"

#include "jni.h"
#include "JNIHelp.h"
#include "android_runtime/AndroidRuntime.h"
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include<stdio.h>
#include<stdlib.h>
#include<fcntl.h>


#include "com_android_server_WiredAccessoryManager.h"

#include "utils/Log.h"

/*
 * Make sure to add package name prefixed after java. i.e com.android.setting
   java_<package name with _ instead of . >_<JavaFileName>_<functionname>

 */

namespace android
{

 jchar  com_android_server_WiredAccessoryManager_returnSpkDet (JNIEnv * env, jobject cls)
{
    char AIOPin = '1';		
    FILE *fp;
	char gpio_path[31];
	strcpy(gpio_path, "/sys/class/gpio/gpio200/value");    //SD3_RST gpio200
	if ((fp = fopen(gpio_path, "rb")) != NULL) /*file in binary for reading only*/
	{
		rewind(fp);	/*Set pointer to begining of the file*/
		fread(&AIOPin, sizeof(char), 1, fp); /* read value to the file*/
		ALOGE("Doge return SD3_RST %x\n", AIOPin);
	}    
	else
        ALOGE("Doge open gpio200 failed!");
   	fclose(fp);
    return AIOPin;	
}

  jchar  com_android_server_WiredAccessoryManager_returnAIOSetting (JNIEnv * env, jobject cls)
{
    char AIOPin = '1';		
    FILE *fp;
	char gpio_path[30];
	strcpy(gpio_path, "/sys/class/gpio/gpio37/value");    //DI5
	if ((fp = fopen(gpio_path, "rb")) != NULL) /*file in binary for reading only*/
	{
		rewind(fp);	/*Set pointer to begining of the file*/
		fread(&AIOPin, sizeof(char), 1, fp); /* read value to the file*/
		ALOGE("Doge return AIOSetting %x\n", AIOPin);
	}    
	else
        ALOGE("Doge open gpio37 failed!");
   	fclose(fp);
    return AIOPin;	
}

 jchar  com_android_server_WiredAccessoryManager_returnIntSpkSetting (JNIEnv * env, jobject cls)
{
    char AIOPin = '1';		
    FILE *fp;
	char gpio_path[30];
	strcpy(gpio_path, "/sys/class/gpio/gpio39/value");    //DO2
//endless loop， remove it
//strcpy(gpio_path, "/cache/backup/internalSpeakers");    //SAGAR reading value from Cache instead of the gpio

	if ((fp = fopen(gpio_path, "rb")) != NULL) /*file in binary for reading only*/
	{
		rewind(fp);	/*Set pointer to begining of the file*/
		fread(&AIOPin, sizeof(char), 1, fp); /* read value to the file*/
		ALOGE("Doge return IntSpkSetting %x\n", AIOPin);
	}    
	else
        ALOGE("Doge open gpio39 failed!");
   	fclose(fp);
    return AIOPin;	
}



static JNINativeMethod method_table[] = {
 { "nativeReturnSpkDet", "()C", (void*) com_android_server_WiredAccessoryManager_returnSpkDet },
 { "nativeReturnAIOSetting", "()C", (void*) com_android_server_WiredAccessoryManager_returnAIOSetting },
 { "nativeReturnIntSpkSetting", "()C", (void*) com_android_server_WiredAccessoryManager_returnIntSpkSetting }
};


int register_android_server_WiredAccessoryManager(JNIEnv *env)
{
    return jniRegisterNativeMethods(env, "com/android/server/WiredAccessoryManager",
            method_table, NELEM(method_table));
}

}



