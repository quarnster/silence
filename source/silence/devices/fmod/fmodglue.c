#include <fmod.h>
#include <fmod_errors.h>
#include "fmodglue.h"

FMUSIC_MODULE *mod;

int synced = 0;
unsigned char effect;
signed char pause;

void callback(FMUSIC_MODULE *mod, unsigned char sync) {
	synced = 1;
	effect = sync;
}

JNIEXPORT void JNICALL Java_silence_devices_fmod_FmodDevice_init(JNIEnv *jne, jobject obj, jboolean sound) {
	// FmodException
	jclass FmodException = jne->FindClass("silence/devices/fmod/FmodException");

	if (FSOUND_GetVersion() != FMOD_VERSION) {
		jne->ThrowNew(FmodException, ("Error : You are using the wrong DLL version!  You should be using FMOD ???\n"));
		return;
	}

	// Set nosound option
	if (sound != JNI_TRUE) {
		FSOUND_SetOutput(FSOUND_OUTPUT_NOSOUND);
	}

	// init
	if (! FSOUND_Init(44100, 32, 0)) {
		jne->ThrowNew(FmodException, FMOD_ErrorString(FSOUND_GetError()));
	}

}

JNIEXPORT void JNICALL Java_silence_devices_fmod_FmodDevice_Nplay(JNIEnv *jne, jobject obj, jstring file, jboolean loop) {
	// FmodException
	jclass FmodException = jne->FindClass("silence/devices/fmod/FmodException");

        // the module name
        char *str = (char *)jne->GetStringUTFChars(file, 0);

        if (mod != NULL) return;

	// load module
        mod = FMUSIC_LoadSong(str);
	if (!mod) {
                jne->ReleaseStringUTFChars(file, str);
		jne->ThrowNew(FmodException, FMOD_ErrorString(FSOUND_GetError()));
                return;
	}

	// play
	FMUSIC_PlaySong(mod);

	// set callback
        FMUSIC_SetZxxCallback(mod, callback);
}

JNIEXPORT void JNICALL Java_silence_devices_fmod_FmodDevice_pause(JNIEnv *jne, jobject obj) {
        if (mod != NULL) {
                pause = !pause;
                FMUSIC_SetPaused(mod, pause);
        }
}

JNIEXPORT void JNICALL Java_silence_devices_fmod_FmodDevice_Nstop(JNIEnv *jne, jobject obj) {
        if (mod != NULL) {
                FMUSIC_FreeSong(mod);
                mod = NULL;
        }
}

JNIEXPORT void JNICALL Java_silence_devices_fmod_FmodDevice_close(JNIEnv *jne, jobject obj) {
	FSOUND_Close();
}

JNIEXPORT void JNICALL Java_silence_devices_fmod_FmodDevice_setVolume(JNIEnv *jne, jobject obj, jint vol) {
        if (mod != NULL) {
                FMUSIC_SetMasterVolume(mod, vol);
        }
}

JNIEXPORT jint JNICALL Java_silence_devices_fmod_FmodDevice_synced(JNIEnv *jne, jobject obj) {
	if (synced) {
		synced = 0;
		return effect;
	} else {
		return -1;
	}
}
