#
# $Id: Makefile,v 1.8 2003/08/21 09:19:59 fredde Exp $
#

CLASSPATH = $CLASSPATH
JAVAC = javac
JAVAFLAGS = -O -deprecation

MuhmuAudioDir = ~/java/muhmuaudio/src
JOrbisDir = ~/java/jorbis-0.0.4
silenceDir = $(PWD)

.SUFFIXES: .class .java

SILENCE = org/gjt/fredde/silence/AudioException.class \
	org/gjt/fredde/silence/Silence.class \
	\
	org/gjt/fredde/silence/format/AudioFormat.class \
	\
	org/gjt/fredde/silence/format/xm/Pattern.class \
	org/gjt/fredde/silence/format/xm/Sample.class \
	org/gjt/fredde/silence/format/xm/Instrument.class \
	org/gjt/fredde/silence/format/xm/InstrumentManager.class \
	org/gjt/fredde/silence/format/xm/Channel.class \
	org/gjt/fredde/silence/format/xm/Xm.class \
	\
	org/gjt/fredde/silence/format/au/Au.class \
	org/gjt/fredde/silence/format/au/Decoder.class \
	org/gjt/fredde/silence/format/au/MulawDecoder.class

all: $(SILENCE)

tests: tests/SimplePlayTest.class test/ExampleApplet.class

javadoc:
	mkdir javadoc
	javadoc -version -author -d javadoc \
	`find org/gjt/fredde/silence/ -regex .*.java -print` \
	`find $(MuhmuAudioDir) -regex .*.java -print` \
	`find $(JOrbisDir) -regex .*.java -print`

dist: all tests javadoc

jar: all
	jar cvf silence.jar \
	`find org/gjt/fredde/silence/ -regex .*.class -print`
	\
	cd $(MuhmuAudioDir) && \
	jar uvf $(silenceDir)/silence.jar `find -regex .*.class -print`
	\
	cd $(JOrbisDir) && \
	jar uvf $(silenceDir)/silence.jar `find -regex .*.class -print`


.java.class:
	$(JAVAC) $(JAVAFLAGS) $<

clean:
	rm -f `find org/gjt/fredde/silence/ -regex .*.class -print`
	rm -f `find org/gjt/fredde/silence/ -regex .*~ -print`
	rm -f `find $(JOrbisDir) -regex .*.class -print`
	rm -f `find $(MuhmuAudioDir) -regex .*.class -print`
	rm -f silence.jar
	rm -f -r javadoc
