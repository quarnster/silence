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

JNIEXPORT void JNICALL Java_silence_devices_midas_MidasDevice_init(JNIEnv* jne, jobject obj, jstring mod, jboolean sound) {
	char *str = (char *)jne->GetStringUTFChars(mod, 0);
	jclass MidasException = jne->FindClass("silence/devices/midas/MidasException");

	if ( !MIDASstartup() ) {
		jne->ReleaseStringUTFChars(mod, str);
		jne->ThrowNew(MidasException, Error());
		return;
	}

	if (sound == JNI_FALSE)
		MIDASsetOption(MIDAS_OPTION_FORCE_NO_SOUND, 1);

	if ( !MIDASinit() ) {
		jne->ReleaseStringUTFChars(mod, str);
		jne->ThrowNew(MidasException, Error());
		return;
	}

	if ( !(module = MIDASloadModule(str)) ) {
		jne->ReleaseStringUTFChars(mod, str);
		jne->ThrowNew(MidasException, Error());
		return;
	}

	jne->ReleaseStringUTFChars(mod, str);

	if ( !(handle = MIDASplayModule(module, FALSE)) ) {
		jne->ThrowNew(MidasException, Error());
		return;
	}
}

JNIEXPORT void JNICALL Java_silence_devices_midas_MidasDevice_Nplay(JNIEnv* env, jobject obj) {
	jclass SoundException = env->FindClass("silence/devices/midas/MidasException");

	MIDASsetMusicSyncCallback (handle, syncCallback);
	if ( !MIDASstartBackgroundPlay(0) )
		env->ThrowNew(SoundException, Error());
}

JNIEXPORT void JNICALL Java_silence_devices_midas_MidasDevice_Nstop(JNIEnv*, jobject) {
	MIDASstopBackgroundPlay();
	MIDASstopModule(handle);
}

JNIEXPORT void JNICALL Java_silence_devices_midas_MidasDevice_close(JNIEnv *, jobject) {
	MIDASfreeModule(module);
	MIDASclose();
}
