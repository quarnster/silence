#include "midas.h"
#include <midasdll.h>

MIDASmodule module = NULL;
MIDASmodulePlayHandle handle;

int synced = FALSE;
unsigned effect;

void syncCallback(unsigned sync, unsigned pos, unsigned row ) {
	synced = TRUE;
	effect = sync;
}

char* Error() {
	return MIDASgetErrorMessage(MIDASgetLastError());
}

JNIEXPORT jint JNICALL Java_silence_devices_midas_MidasDevice_synced(JNIEnv* jne, jobject obj) {
	if (synced) {
		int tmp = (int) effect;
		effect = 0;
		synced = FALSE;
		return tmp;
	} else {
		return -1;
	}
}

JNIEXPORT void JNICALL Java_silence_devices_midas_MidasDevice_init(JNIEnv* jne, jobject obj, jboolean sound) {
	jclass MidasException = jne->FindClass("silence/devices/midas/MidasException");

	if ( !MIDASstartup() ) {
		jne->ThrowNew(MidasException, Error());
		return;
	}

	if (sound == JNI_FALSE)
		MIDASsetOption(MIDAS_OPTION_FORCE_NO_SOUND, 1);

	if ( !MIDASinit() ) {
		jne->ThrowNew(MidasException, Error());
		return;
	}
}
JNIEXPORT void JNICALL Java_silence_devices_midas_MidasDevice_setVolume(JNIEnv* jne, jobject obj, jint vol) {
	if (module != NULL && vol <= 64 && vol >= 0) {
		MIDASsetMusicVolume(handle, vol);
	}
}

JNIEXPORT void JNICALL Java_silence_devices_midas_MidasDevice_Nplay(JNIEnv* jne, jobject obj, jstring mod, jboolean loop) {
	jclass MidasException = jne->FindClass("silence/devices/midas/MidasException");
	char *str = (char *)jne->GetStringUTFChars(mod, 0);

	if (module != NULL) return;

	if ( !(module = MIDASloadModule(str)) ) {
		jne->ReleaseStringUTFChars(mod, str);
		jne->ThrowNew(MidasException, Error());
		return;
	}

	jne->ReleaseStringUTFChars(mod, str);

	if ( !(handle = MIDASplayModule(module, (loop == JNI_TRUE))) ) {
		jne->ThrowNew(MidasException, Error());
		return;
	}

	MIDASsetMusicSyncCallback (handle, syncCallback);
	if ( !MIDASstartBackgroundPlay(0) )
		jne->ThrowNew(MidasException, Error());
}

JNIEXPORT void JNICALL Java_silence_devices_midas_MidasDevice_Nstop(JNIEnv*, jobject) {
	if (module != NULL) {
		MIDASstopBackgroundPlay();
		MIDASstopModule(handle);
		MIDASfreeModule(module);
		module = NULL;
	}
}

JNIEXPORT void JNICALL Java_silence_devices_midas_MidasDevice_close(JNIEnv *, jobject) {
	MIDASclose();
}
