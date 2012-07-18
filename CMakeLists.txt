
cmake_minimum_required(VERSION 2.8)

find_package(Java COMPONENTS Development)
include(UseJava)

if(NOT Java_Development_FOUND)
    message(FATAL ERROR "Java development tools not found")
endif()

set(MUHMUAUDIO ${PROJECT_BINARY_DIR}/muhmuaudio-0.1.zip)
set(JORBIS ${PROJECT_BINARY_DIR}/jorbis-0.0.17.zip)
set(JLAYER ${PROJECT_BINARY_DIR}/jlayer1.0.1.tar.gz)

if(NOT EXISTS ${MUHMUAUDIO})
    message("Downloading required component Muhmuaudio...")
    file(DOWNLOAD http://sourceforge.net/projects/muhmuaudio/files/muhmuaudio/0.1/muhmuaudio-0.1.zip/download ${MUHMUAUDIO})

    execute_process(COMMAND ${CMAKE_COMMAND} -E make_directory muhmuaudio-0.1 WORKING_DIRECTORY ${PROJECT_SOURCE_DIR})
    execute_process(COMMAND ${Java_JAR_EXECUTABLE} xf ${MUHMUAUDIO} WORKING_DIRECTORY ${PROJECT_SOURCE_DIR}/muhmuaudio-0.1 OUTPUT_QUIET)
    execute_process(COMMAND find muhmuaudio-0.1 -name "*.java" -not -name "DirectSound*" WORKING_DIRECTORY ${PROJECT_SOURCE_DIR} OUTPUT_VARIABLE MUHMUAUDIO_JAVA)
    string(REGEX REPLACE "([^\n]+)\n" "\\1;" MUHMUAUDIO_JAVA ${MUHMUAUDIO_JAVA})
endif()

if(NOT EXISTS ${JORBIS})
    message("Downloading required component jorbis...")
    file(DOWNLOAD http://www.jcraft.com/jorbis/jorbis-0.0.17.zip ${JORBIS})
    execute_process(COMMAND ${Java_JAR_EXECUTABLE} xf ${JORBIS} WORKING_DIRECTORY ${PROJECT_SOURCE_DIR} OUTPUT_QUIET)
    execute_process(COMMAND find jorbis-0.0.17 -name "*.java" WORKING_DIRECTORY ${PROJECT_SOURCE_DIR} OUTPUT_VARIABLE JORBIS_JAVA)
    string(REGEX REPLACE "([^\n]+)\n" "\\1;" JORBIS_JAVA ${JORBIS_JAVA})
endif()


if(NOT EXISTS ${JLAYER})
    message("Downloading required component jlayer...")
    file(DOWNLOAD http://www.javazoom.net/javalayer/sources/jlayer1.0.1.tar.gz ${JLAYER})
    execute_process(COMMAND ${CMAKE_COMMAND} -E tar xf ${JLAYER} WORKING_DIRECTORY ${PROJECT_SOURCE_DIR} OUTPUT_QUIET)
    execute_process(COMMAND find JLayer1.0.1/src/  -name "*.java" WORKING_DIRECTORY ${PROJECT_SOURCE_DIR} OUTPUT_VARIABLE JLAYER_JAVA)
    string(REGEX REPLACE "([^\n]+)\n" "\\1;" JLAYER_JAVA ${JLAYER_JAVA})
endif()

set(CMAKE_JAVA_COMPILE_FLAGS -source 1.4)

set(JLAYER_JAVA ${JLAYER_JAVA} CACHE STRING "")
set(JORBIS_JAVA ${JORBIS_JAVA} CACHE STRING "")
set(MUHMUAUDIO_JAVA ${MUHMUAUDIO_JAVA} CACHE STRING "")

add_jar(silence
     ${JORBIS_JAVA}
     ${JLAYER_JAVA}
     ${MUHMUAUDIO_JAVA}
     ExampleApplet.java
     silence/AudioException.java
     silence/format/au/Au.java
     silence/format/au/Decoder.java
     silence/format/au/MulawDecoder.java
     silence/format/AudioFormat.java
     silence/format/xm/Channel.java
     silence/format/xm/ChannelUpdateData.java
     silence/format/xm/data/Envelope.java
     silence/format/xm/data/Instrument.java
     silence/format/xm/data/Module.java
     silence/format/xm/data/Pattern.java
     silence/format/xm/data/Sample.java
     silence/format/xm/data/Vibrato.java
     silence/format/xm/EffectManager.java
     silence/format/xm/EnvelopeHandler.java
     silence/format/xm/InstrumentManager.java
     silence/format/xm/ModulePlayer.java
     silence/format/xm/SamplePlayer.java
     silence/format/xm/Xm.java
     silence/format/xm/XmLoader.java
     silence/Silence.java
     SimplePlayTest.java
     silence/format/mp3/Mp3.java
     silence/format/ogg/Ogg.java
 )
