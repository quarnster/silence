#include <fmod.h>
#include <fmod_errors.h>
#include "fmodglue.h"

FMUSIC_MODULE *fmodule;

int fsynced = 0;
unsigned char feffect;
signed char pause;

void callback(FMUSIC_MODULE *mod, unsigned char sync) {
	fsynced = 1;
	feffect = sync;
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

        if (fmodule != NULL) return;

	// load module
        fmodule = FMUSIC_LoadSong(str);
	if (!fmodule) {
                jne->ReleaseStringUTFChars(file, str);
		jne->ThrowNew(FmodException, FMOD_ErrorString(FSOUND_GetError()));
                return;
	}

	// play
	FMUSIC_PlaySong(fmodule);

	// set callback
        FMUSIC_SetZxxCallback(fmodule, callback);
}

JNIEXPORT void JNICALL Java_silence_devices_fmod_FmodDevice_pause(JNIEnv *jne, jobject obj) {
        if (fmodule != NULL) {
                pause = !pause;
                FMUSIC_SetPaused(fmodule, pause);
        }
}

JNIEXPORT void JNICALL Java_silence_devices_fmod_FmodDevice_Nstop(JNIEnv *jne, jobject obj) {
        if (fmodule != NULL) {
                FMUSIC_FreeSong(fmodule);
                fmodule = NULL;
        }
}

JNIEXPORT void JNICALL Java_silence_devices_fmod_FmodDevice_close(JNIEnv *jne, jobject obj) {
	FSOUND_Close();
}

JNIEXPORT void JNICALL Java_silence_devices_fmod_FmodDevice_setVolume(JNIEnv *jne, jobject obj, jint vol) {
        if (fmodule != NULL) {
                FMUSIC_SetMasterVolume(fmodule, vol);
        }
}

JNIEXPORT jint JNICALL Java_silence_devices_fmod_FmodDevice_synced(JNIEnv *jne, jobject obj) {
	if (fsynced) {
		fsynced = 0;
		return feffect;
	} else {
		return -1;
	}
}
