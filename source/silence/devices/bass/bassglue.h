/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class silence_devices_bass_BassDevice */

#ifndef _Included_silence_devices_bass_BassDevice
#define _Included_silence_devices_bass_BassDevice
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     silence_devices_bass_BassDevice
 * Method:    synced
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_silence_devices_bass_BassDevice_synced
  (JNIEnv *, jobject);

/*
 * Class:     silence_devices_bass_BassDevice
 * Method:    init
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_silence_devices_bass_BassDevice_init
  (JNIEnv *, jobject, jboolean);

/*
 * Class:     silence_devices_bass_BassDevice
 * Method:    Nplay
 * Signature: (Ljava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_silence_devices_bass_BassDevice_Nplay
  (JNIEnv *, jobject, jstring, jboolean);

/*
 * Class:     silence_devices_bass_BassDevice
 * Method:    setVolume
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_silence_devices_bass_BassDevice_setVolume
  (JNIEnv *, jobject, jint);

/*
 * Class:     silence_devices_bass_BassDevice
 * Method:    Nstop
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_silence_devices_bass_BassDevice_Nstop
  (JNIEnv *, jobject);

/*
 * Class:     silence_devices_bass_BassDevice
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_silence_devices_bass_BassDevice_close
  (JNIEnv *, jobject);

/*
 * Class:     silence_devices_bass_BassDevice
 * Method:    pause
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_silence_devices_bass_BassDevice_pause
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif