#include "midasglue.h"
#include <midasdll.h>

MIDASmodule module = NULL;
MIDASmodulePlayHandle handle;

int msynced = FALSE;
unsigned meffect;

void syncCallback(unsigned sync, unsigned pos, unsigned row ) {
	msynced = TRUE;
	meffect = sync;
}

char* Error() {
	return MIDASgetErrorMessage(MIDASgetLastError());
}

JNIEXPORT jint JNICALL Java_silence_devices_midas_MidasDevice_synced(JNIEnv* jne, jobject obj) {
	if (msynced) {
		int tmp = (int) meffect;
		meffect = 0;
		msynced = FALSE;
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
	vol = (vol > 100) ? 100 : (vol < 0) ? 0 : vol;
	if (module != NULL) {
		vol = (int) (((float) vol / 100) * 64);
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
