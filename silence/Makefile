#
# $Id: Makefile,v 1.3 2000/10/12 15:53:47 fredde Exp $
#

CLASSPATH = $CLASSPATH
JAVAC = jikes
JAVAFLAGS = -g -deprecation
MuhmuAudioDir = ~/java/muhmuaudio/src
silenceDir = $(PWD)

.SUFFIXES: .class .java

CLASSES = org/gjt/fredde/silence/AudioException.class \
	org/gjt/fredde/silence/Silence.class

all: $(CLASSES) formats

formats: $(CLASSES) \
	org/gjt/fredde/silence/format/AudioFormat.class \
	org/gjt/fredde/silence/format/xm/Pattern.class \
	org/gjt/fredde/silence/format/xm/Sample.class \
	org/gjt/fredde/silence/format/xm/Instrument.class \
	org/gjt/fredde/silence/format/xm/Channel.class \
	org/gjt/fredde/silence/format/xm/Xm.class \
	org/gjt/fredde/silence/format/au/Au.class

tests: formats \
	tests/SimplePlayTest.class

javadoc:
	mkdir javadoc
	javadoc -version -author -d javadoc \
	org.gjt.fredde.silence \
	org.gjt.fredde.silence.format \
	org.gjt.fredde.silence.format.xm \
	org.gjt.fredde.silence.format.au

dist: all tests javadoc

jar: all
	jar cvf silence.jar \
	`find org/gjt/fredde/silence/ -regex .*.class -print`
	cd $(MuhmuAudioDir) && \
	jar uvf $(silenceDir)/silence.jar \
	`find -regex .*.class -print`

.java.class:
	$(JAVAC) $(JAVAFLAGS) $<

clean:
	rm -f `find org/gjt/fredde/silence/ -regex .*.class -print`
	rm -f silence.jar
	rm -f -r javadoc
