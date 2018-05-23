# CMake toolchain to build libnd4j for Android 4.0 or newer. Sample usage:
#
# ANDROID_BIN=/path/to/android-ndk/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/bin/arm-linux-androideabi \
# ANDROID_CPP=/path/to/android-ndk/sources/cxx-stl/gnu-libstdc++/4.9/ \
# ANDROID_ROOT=/path/to/android-ndk/platforms/android-14/arch-arm/ \
# cmake -DCMAKE_TOOLCHAIN_FILE=android-arm.cmake -DCMAKE_INSTALL_PREFIX=..
#
# If you really need to use libnd4j on a CPU with no FPU, replace "libs/armeabi-v7a" by "libs/armeabi" and
# "-march=armv7-a -mfloat-abi=softfp -mfpu=vfpv3-d16" with "-march=armv5te -mtune=xscale -msoft-float"

set(CMAKE_SYSTEM_NAME UnixPaths)
set(CMAKE_SYSTEM_PROCESSOR arm)
set(ANDROID TRUE)

set(CMAKE_C_COMPILER   "$ENV{ANDROID_BIN}-gcc")
set(CMAKE_CXX_COMPILER "$ENV{ANDROID_BIN}-g++")

set(CMAKE_C_LINK_EXECUTABLE    "<CMAKE_C_COMPILER>   <FLAGS> <CMAKE_C_LINK_FLAGS>   <LINK_FLAGS> <OBJECTS> -o <TARGET> <LINK_LIBRARIES> -L$ENV{ANDROID_ROOT}/usr/lib/ --sysroot=$ENV{ANDROID_ROOT}")
set(CMAKE_CXX_LINK_EXECUTABLE  "<CMAKE_CXX_COMPILER> <FLAGS> <CMAKE_CXX_LINK_FLAGS> <LINK_FLAGS> <OBJECTS> -o <TARGET> <LINK_LIBRARIES> -L$ENV{ANDROID_CPP}/libs/armeabi-v7a/ -L$ENV{ANDROID_ROOT}/usr/lib/ -lgnustl_static --sysroot=$ENV{ANDROID_ROOT}")

set(CMAKE_C_CREATE_SHARED_LIBRARY    "<CMAKE_C_COMPILER> <CMAKE_SHARED_LIBRARY_C_FLAGS> <LANGUAGE_COMPILE_FLAGS> <LINK_FLAGS> <CMAKE_SHARED_LIBRARY_CREATE_C_FLAGS> <CMAKE_SHARED_LIBRARY_SONAME_C_FLAG><TARGET_SONAME> -Wl,--no-undefined -z text -o <TARGET> <OBJECTS> <LINK_LIBRARIES> -L$ENV{ANDROID_ROOT}/usr/lib/ --sysroot=$ENV{ANDROID_ROOT}")
set(CMAKE_CXX_CREATE_SHARED_LIBRARY  "<CMAKE_CXX_COMPILER> <CMAKE_SHARED_LIBRARY_CXX_FLAGS> <LANGUAGE_COMPILE_FLAGS> <LINK_FLAGS> <CMAKE_SHARED_LIBRARY_CREATE_CXX_FLAGS> <CMAKE_SHARED_LIBRARY_SONAME_CXX_FLAG><TARGET_SONAME> -Wl,--no-undefined -z text -o <TARGET> <OBJECTS> <LINK_LIBRARIES> -L$ENV{ANDROID_CPP}/libs/armeabi-v7a/ -L$ENV{ANDROID_ROOT}/usr/lib/ -lgnustl_static --sysroot=$ENV{ANDROID_ROOT}")

add_definitions(-D__ANDROID_API__=14 -DANDROID -fPIC -ffunction-sections -funwind-tables -fstack-protector -march=armv7-a -mfloat-abi=softfp -mfpu=vfpv3-d16 -fomit-frame-pointer -fstrict-aliasing -funswitch-loops -finline-limit=300 -Wno-attributes)

include_directories("$ENV{ANDROID_CPP}/include/" "$ENV{ANDROID_CPP}/libs/armeabi-v7a/include/" "$ENV{ANDROID_ROOT}/usr/include/" "$ENV{ANDROID_NDK}/sysroot/usr/include/" "$ENV{ANDROID_NDK}/sysroot/usr/include/arm-linux-androideabi/")
