#
# $Id: Makefile,v 1.1 2005/12/25 21:56:08 quarn Exp $
#

MuhmuAudioDir = ../muhmuaudio
JOrbisDir = ../jorbis-0.0.14/
JLayerDir = ../JLayer1.0/src/
silenceDir = $(PWD)

CLASSPATH =  /usr/lib/j2se/1.4/jre/lib/rt.jar:$(JLayerDir):$(JOrbisDir):$(silenceDir):$(MuhmuAudioDir)

JAVAC = jikes
JAVAFLAGS = -g -deprecation -classpath $(CLASSPATH)

.SUFFIXES: .class .java

SILENCE = silence/AudioException.class \
	silence/Silence.class \
	\
	silence/format/AudioFormat.class \
	\
	silence/format/xm/data/Module.class \
	silence/format/xm/data/Pattern.class \
	silence/format/xm/data/Sample.class \
	silence/format/xm/data/Instrument.class \
	silence/format/xm/data/Envelope.class \
	\
	silence/format/xm/Channel.class \
	silence/format/xm/ChannelUpdateData.class \
	silence/format/xm/EffectManager.class \
	silence/format/xm/EnvelopeHandler.class \
	silence/format/xm/InstrumentManager.class \
	silence/format/xm/ModulePlayer.class \
	silence/format/xm/SamplePlayer.class \
	\
	silence/format/xm/Xm.class \
	silence/format/xm/XmLoader.class \
	\
	silence/format/ogg/Ogg.class \
	\
	silence/format/mp3/Mp3.class \
	\
	silence/format/au/Au.class \
	silence/format/au/Decoder.class \
	silence/format/au/MulawDecoder.class \
	SimplePlayTest.class \
	ExampleApplet.class

all: $(SILENCE)

tests: tests/SimplePlayTest.class test/ExampleApplet.class

javadoc:
	mkdir javadoc
	javadoc -version -author -d javadoc \
	`find silence/ -regex .*.java -print` \
	`find $(MuhmuAudioDir) -regex .*.java -print` \
	`find $(JOrbisDir) -regex .*.java -print`

dist: all tests javadoc

jar: all
	jar cvf silence.jar \
	`find silence/ -regex .*.class -print`
	cd $(MuhmuAudioDir) && \
	jar uvf $(silenceDir)/silence.jar `find -name "*.class" -print`
	cd $(JLayerDir) && \
	jar uvf $(silenceDir)/silence.jar `find -name "*.class" -print -or  -name "*.ser" -print`
	cd $(JOrbisDir) && \
	jar uvf $(silenceDir)/silence.jar `find -name "*.class" -print`


.java.class:
	$(JAVAC) $(JAVAFLAGS) $<

clean:
	rm -f `find silence/ -regex .*.class -print`
	rm -f `find silence/ -regex .*~ -print`
	rm -f silence.jar
	rm -f -r javadoc
