#
# $Id: Makefile,v 1.1 2000/09/25 16:34:35 fredde Exp $
#

CLASSPATH = $CLASSPATH
JAVAC = jikes
JAVAFLAGS = -g -deprecation

.SUFFIXES: .class .java

CLASSES = org/gjt/fredde/silence/AudioException.class \
	org/gjt/fredde/silence/Silence.class

all: $(CLASSES) formats

formats: org/gjt/fredde/silence/format/AudioFormat.class \
	org/gjt/fredde/silence/format/xm/Pattern.class \
	org/gjt/fredde/silence/format/xm/Sample.class \
	org/gjt/fredde/silence/format/xm/Instrument.class \
	org/gjt/fredde/silence/format/xm/Channel.class \
	org/gjt/fredde/silence/format/xm/Xm.class \
	org/gjt/fredde/silence/format/au/Au.class

javadoc:
	mkdir javadoc
	javadoc -version -author -d javadoc \
	org.gjt.fredde.silence \
	org.gjt.fredde.silence.format \
	org.gjt.fredde.silence.format.xm \
	org.gjt.fredde.silence.format.au

dist: all javadoc

.java.class:
	$(JAVAC) $(JAVAFLAGS) $<

clean:
	rm -f org/gjt/fredde/silence/*.class
	rm -f org/gjt/fredde/silence/format/*.class
	rm -f org/gjt/fredde/silence/format/xm/*.class
	rm -f org/gjt/fredde/silence/format/au/*.class
	rm -f -r javadoc