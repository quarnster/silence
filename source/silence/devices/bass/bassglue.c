#include <windows.h>
#include <mmsystem.h>

#include "bassglue.h"
#include "bass.h" // bass include file

HMUSIC mod = NULL;
int synced = 0;
int effect;
int paused = 0;

void CALLBACK syncCallback(HSYNC, DWORD, DWORD data, DWORD) {
	synced = 1;
	effect = (int) data;
}

JNIEXPORT jint JNICALL Java_silence_devices_bass_BassDevice_synced(JNIEnv* jne, jobject obj) {
	if (synced) {
		int tmp = (int) effect;
		effect = 0;
		synced = 0;
		return tmp;
	} else {
		return -1;
	}
}

JNIEXPORT void JNICALL Java_silence_devices_bass_BassDevice_init(JNIEnv* jne, jobject obj, jboolean sound) {
	jclass BassException = jne->FindClass("silence/devices/bass/BassException");
	int soundmode = -1; // default sound device

	if ( sound == JNI_FALSE ) soundmode = -2; // nosound mode

	if ( !BASS_Init(soundmode, 44100, 0, GetForegroundWindow()) ) {
		jne->ThrowNew(BassException, "Init failed...");
		return;
	}
}
JNIEXPORT void JNICALL Java_silence_devices_bass_BassDevice_setVolume(JNIEnv* jne, jobject obj, jint vol) {
	if (mod != NULL && vol <= 100 && vol >= 0) {
		BASS_SetVolume(vol);
	}
}

JNIEXPORT void JNICALL Java_silence_devices_bass_BassDevice_Nplay(JNIEnv* jne, jobject obj, jstring module, jboolean loop) {
	jclass BassException = jne->FindClass("silence/devices/bass/BassException");
	char *str = (char *)jne->GetStringUTFChars(module, 0);

	if ( mod != NULL ) return;

	if ( !(mod = BASS_MusicLoad((loop == JNI_TRUE), str,  0, 0, 0)) ) {
		jne->ReleaseStringUTFChars(module, str);
		jne->ThrowNew(BassException, "Could not load module...");
		return;
	}

	jne->ReleaseStringUTFChars(module, str);

	BASS_ChannelSetSync(mod, BASS_SYNC_MUSICFX, 1, &syncCallback, 0);
	BASS_Start();
	BASS_MusicPlay(mod);
}

JNIEXPORT void JNICALL Java_silence_devices_bass_BassDevice_Nstop(JNIEnv*, jobject) {
	if (mod != NULL) {
		BASS_Stop();
		mod = NULL;
	}
}
JNIEXPORT void JNICALL Java_silence_devices_bass_BassDevice_pause(JNIEnv *, jobject) {
        if (paused) {
                BASS_Start();
        } else {
                BASS_Pause();
        }
        paused = !paused;
}

JNIEXPORT void JNICALL Java_silence_devices_bass_BassDevice_close(JNIEnv *, jobject) {
	BASS_Free();
}
