ifndef SYSTEM
SYSTEM = linux
endif

CLASSPATH = ./
JAVAC = jikes
JAVAFLAGS = -g -deprecation
JAVADIR = /usr/local/jdk1.2.2
MIDASDIR = /usr/local/src/midas-1.1.2

ifeq ($(SYSTEM), linux)
	CLIB = libmidasglue.so
	CC = g++
	CFLAGS = --shared -ggdb -Wall -I$(JAVADIR)/include -I$(JAVADIR)/include/linux -I$(MIDASDIR)/include -L$(MIDASDIR)/lib/linux/gcdebug
else
	CLIB = midasglue.dll
	CC = bcc32
	CFLAGS = -P -w- 
endif

.SUFFIXES: .class .java .c .o

CLASSES = silence/AudioException.class \
	silence/devices/AudioDevice.class \
	silence/devices/midas/MidasDevice.class \
	silence/devices/midas/MidasException.class


all: $(CLASSES)

dist: all nativelibs jar

nativelibs: midaslib
	
.java.class:
	$(JAVAC) $(JAVAFLAGS) $<

clean:
	rm -f silence/*.class
	rm -f silence/devices/*.class
	rm -f silence/devices/midas/*.class
	rm -f *.so 
	rm -f *.jar
jar:
	jar cvf silence.jar silence/*.class silence/devices/*.class silence/devices/midas/*.class

midaslib:
ifeq ($(SYSTEM), linux)
	$(CC) $(CFLAGS) silence/devices/midas/midas.c -lmidas -o $(CLIB)
else
	$(CC) $(CFLAGS) sound\Midas.c -osound\Midas.obj
	ilink32 sound\midas.obj,$(CLIB),,sound\midaslib.lib
endif
