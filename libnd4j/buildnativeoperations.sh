#!/usr/bin/env bash
set -eu

# cd to the directory containing this script
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $DIR

export CMAKE_COMMAND="cmake"
if which cmake3 &> /dev/null; then
    export CMAKE_COMMAND="cmake3"
fi
export MAKE_COMMAND="make"
echo eval $CMAKE_COMMAND

[[ -z ${MAKEJ:-} ]] && MAKEJ=4

# Use > 1 to consume two arguments per pass in the loop (e.g. each
# argument has a corresponding value to go with it).
# Use > 0 to consume one or more arguments per pass in the loop (e.g.
# some arguments don't have a corresponding value to go with it such
# as in the --default example).
# note: if this is set to > 0 the /etc/hosts part is not recognized ( may be a bug )
PARALLEL="true"
OS=
CHIP=
BUILD=
COMPUTE=
ARCH=
LIBTYPE=
PACKAGING=
CHIP_EXTENSION=
CHIP_VERSION=
EXPERIMENTAL=
OPERATIONS=
CLEAN="false"
MINIFIER="false"
NAME=
while [[ $# > 0 ]]
do
key="$1"
value="${2:-}"
#Build type (release/debug), packaging type, chip: cpu,cuda, lib type (static/dynamic)
case $key in
    -o|-platform|--platform)
    OS="$value"
    shift # past argument
    ;;
    -b|--build-type)
    BUILD="$value"
    shift # past argument
    ;;
    -p|--packaging)
    PACKAGING="$value"
    shift # past argument
    ;;
    -c|--chip)
    CHIP="$value"
    shift # past argument
    ;;
    -cc|--compute)
    COMPUTE="$value"
    shift # past argument
    ;;
    -a|--arch)
    ARCH="$value"
    shift # past argument
    ;;
    -l|--libtype)
    LIBTYPE="$value"
    shift # past argument
    ;;
    -e|--chip-extension)
    CHIP_EXTENSION="$value"
    shift # past argument
    ;;
    -v|--chip-version)
    CHIP_VERSION="$value"
    shift # past argument
    ;;
    -x|--experimental)
    EXPERIMENTAL="$value"
    shift # past argument
    ;;
    -g|--generator)
    OPERATIONS="$value"
    shift # past argument
    ;;
    --name)
    NAME="$value"
    shift # past argument
    ;;
    -j)
    MAKEJ="$value"
    shift # past argument
    ;;
    clean)
    CLEAN="true"
    ;;
    -m|--minifier)
    MINIFIER="true"
    ;;
    --default)
    DEFAULT=YES
    ;;
    *)
            # unknown option
    ;;
esac
if [[ $# > 0 ]]; then
    shift # past argument or value
fi
done
HOST=$(uname -s | tr [A-Z] [a-z])
KERNEL=$HOST-$(uname -m | tr [A-Z] [a-z])
if [ "$(uname)" == "Darwin" ]; then
    HOST="macosx"
    KERNEL="darwin-x86_64"
    echo "RUNNING OSX CLANG"
elif [ "$(expr substr $(uname -s) 1 5)" == "MINGW" ] || [ "$(expr substr $(uname -s) 1 4)" == "MSYS" ]; then
    HOST="windows"
    KERNEL="windows-x86_64"
    echo "Running windows"
elif [ "$(uname -m)" == "ppc64le" ]; then
    if [ -z "$ARCH" ]; then
        ARCH="power8"
    fi
    KERNEL="linux-ppc64le"
fi

if [ -z "$OS" ]; then
    OS="$HOST"
fi

if [[ -z ${ANDROID_NDK:-} ]]; then
    export ANDROID_NDK=$HOME/Android/android-ndk/
fi

case "$OS" in
    linux-armhf)
    export CMAKE_COMMAND="$CMAKE_COMMAND -D CMAKE_TOOLCHAIN_FILE=$HOME/raspberrypi/pi.cmake"
    if [ -z "$ARCH" ]; then
        ARCH="armv7-r"
    fi
    ;;

    android-arm)
    if [ -z "$ARCH" ]; then
        ARCH="armv7-a"
    fi
    export ANDROID_BIN="$ANDROID_NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/$KERNEL/bin/arm-linux-androideabi"
    export ANDROID_CPP="$ANDROID_NDK/sources/cxx-stl/gnu-libstdc++/4.9/"
    export ANDROID_ROOT="$ANDROID_NDK/platforms/android-14/arch-arm/"
    export CMAKE_COMMAND="$CMAKE_COMMAND -DCMAKE_TOOLCHAIN_FILE=cmake/android-arm.cmake"
    ;;

    android-arm64)
    if [ -z "$ARCH" ]; then
        ARCH="armv8-a"
    fi
    export ANDROID_BIN="$ANDROID_NDK/toolchains/aarch64-linux-android-4.9/prebuilt/$KERNEL/bin/aarch64-linux-android"
    export ANDROID_CPP="$ANDROID_NDK/sources/cxx-stl/gnu-libstdc++/4.9/"
    export ANDROID_ROOT="$ANDROID_NDK/platforms/android-21/arch-arm64/"
    export CMAKE_COMMAND="$CMAKE_COMMAND -DCMAKE_TOOLCHAIN_FILE=cmake/android-arm64.cmake"
    ;;

    android-x86)
    if [ -z "$ARCH" ]; then
        ARCH="i686"
    fi
    export ANDROID_BIN="$ANDROID_NDK/toolchains/x86-4.9/prebuilt/$KERNEL/bin/i686-linux-android"
    export ANDROID_CPP="$ANDROID_NDK/sources/cxx-stl/gnu-libstdc++/4.9/"
    export ANDROID_ROOT="$ANDROID_NDK/platforms/android-14/arch-x86/"
    export CMAKE_COMMAND="$CMAKE_COMMAND -DCMAKE_TOOLCHAIN_FILE=cmake/android-x86.cmake"
    ;;

    android-x86_64)
    if [ -z "$ARCH" ]; then
        ARCH="x86-64"
    fi
    export ANDROID_BIN="$ANDROID_NDK/toolchains/x86_64-4.9/prebuilt/$KERNEL/bin/x86_64-linux-android"
    export ANDROID_CPP="$ANDROID_NDK/sources/cxx-stl/gnu-libstdc++/4.9/"
    export ANDROID_ROOT="$ANDROID_NDK/platforms/android-21/arch-x86_64/"
    export CMAKE_COMMAND="$CMAKE_COMMAND -DCMAKE_TOOLCHAIN_FILE=cmake/android-x86_64.cmake"
    ;;

    ios-x86_64)
    LIBTYPE="static"
    ARCH="x86-64"
    if xcrun --sdk iphoneos --show-sdk-version &> /dev/null; then
    export IOS_VERSION="$(xcrun --sdk iphoneos --show-sdk-version)"
    else
        export IOS_VERSION="10.3"
    fi
    XCODE_PATH="$(xcode-select --print-path)"
    export IOS_SDK="$XCODE_PATH/Platforms/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator$IOS_VERSION.sdk"
    export CMAKE_COMMAND="$CMAKE_COMMAND -DCMAKE_TOOLCHAIN_FILE=cmake/ios-x86_64.cmake --debug-trycompile"
    ;;

    ios-x86)
    LIBTYPE="static"
    ARCH="i386"
    if xcrun --sdk iphoneos --show-sdk-version &> /dev/null; then
    export IOS_VERSION="$(xcrun --sdk iphoneos --show-sdk-version)"
    else
        export IOS_VERSION="10.3"
    fi
    XCODE_PATH="$(xcode-select --print-path)"
    export IOS_SDK="$XCODE_PATH/Platforms/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator$IOS_VERSION.sdk"
    export CMAKE_COMMAND="$CMAKE_COMMAND -DCMAKE_TOOLCHAIN_FILE=cmake/ios-x86.cmake --debug-trycompile"
    ;;

    ios-arm64)
    LIBTYPE="static"
    ARCH="arm64"
    if xcrun --sdk iphoneos --show-sdk-version &> /dev/null; then
    export IOS_VERSION="$(xcrun --sdk iphoneos --show-sdk-version)"
    else
        export IOS_VERSION="10.3"
    fi
    XCODE_PATH="$(xcode-select --print-path)"
    export IOS_SDK="$XCODE_PATH/Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS$IOS_VERSION.sdk"
    export CMAKE_COMMAND="$CMAKE_COMMAND -DCMAKE_TOOLCHAIN_FILE=cmake/ios-arm64.cmake --debug-trycompile"
    ;;

    ios-arm)
    LIBTYPE="static"
    ARCH="armv7"
    if xcrun --sdk iphoneos --show-sdk-version &> /dev/null; then
    export IOS_VERSION="$(xcrun --sdk iphoneos --show-sdk-version)"
    else
        export IOS_VERSION="10.3"
    fi
    XCODE_PATH="$(xcode-select --print-path)"
    export IOS_SDK="$XCODE_PATH/Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS$IOS_VERSION.sdk"
    export CMAKE_COMMAND="$CMAKE_COMMAND -DCMAKE_TOOLCHAIN_FILE=cmake/ios-arm.cmake --debug-trycompile"
    ;;

    ios-armv7)
    # change those 2 parameters and make sure the IOS_SDK exists
    export iPhoneOS="iPhoneOS"
    export IOS_VERSION="10.3"
    LIBTYPE="static"
    ARCH="armv7"
    export IOS_SDK="/Applications/Xcode.app/Contents/Developer/Platforms/${iPhoneOS}.platform/Developer/SDKs/${iPhoneOS}${IOS_VERSION}.sdk"
    export CMAKE_COMMAND="$CMAKE_COMMAND -DCMAKE_TOOLCHAIN_FILE=cmake/ios-armv7.cmake --debug-trycompile"
    ;;

    linux*)
    ;;

    macosx*)
    # Do something under Mac OS X platform
    if [ "$CHIP" == "cuda" ]; then
        export CC=clang
        export CXX=clang++
        PARALLEL="false"
    else
        export CC="$(ls -1 /usr/local/bin/gcc-? | head -n 1)"
        export CXX="$(ls -1 /usr/local/bin/g++-? | head -n 1)"
        PARALLEL="true"
    fi
    export CMAKE_COMMAND="$CMAKE_COMMAND -DCMAKE_MACOSX_RPATH=ON"
    ;;

    windows*)
    # Do something under Windows NT platform
    if [ "$CHIP" == "cuda" ]; then
        export CMAKE_COMMAND="cmake -G \"Ninja\""
        export MAKE_COMMAND="ninja"
        export CC="cl.exe"
        export CXX="cl.exe"
        PARALLEL="true"
    else
        export CMAKE_COMMAND="cmake -G \"MSYS Makefiles\""
        export MAKE_COMMAND="make"

        # Sam, do we really need this?
        export CC=/mingw64/bin/gcc
        export CXX=/mingw64/bin/g++
        PARALLEL="true"

    fi
    # Try some defaults for Visual Studio 2013 if user has not run vcvarsall.bat or something
    if [ -z "${VCINSTALLDIR:-}" ]; then
        export VisualStudioVersion=12.0
        export VSINSTALLDIR="C:\\Program Files (x86)\\Microsoft Visual Studio $VisualStudioVersion"
        export VCINSTALLDIR="$VSINSTALLDIR\\VC"
        export WindowsSdkDir="C:\\Program Files (x86)\\Windows Kits\\8.1"
        export Platform=X64
        export INCLUDE="$VCINSTALLDIR\\INCLUDE;$WindowsSdkDir\\include\\shared;$WindowsSdkDir\\include\\um"
        export LIB="$VCINSTALLDIR\\LIB\\amd64;$WindowsSdkDir\\lib\\winv6.3\\um\\x64"
        export LIBPATH="$VCINSTALLDIR\\LIB\\amd64;$WindowsSdkDir\\References\\CommonConfiguration\\Neutral"
        export PATH="$PATH:$VCINSTALLDIR\\BIN\\amd64:$WindowsSdkDir\\bin\\x64:$WindowsSdkDir\\bin\\x86"
    fi
    # Make sure we are using 64-bit MinGW-w64
    export PATH=/mingw64/bin/:$PATH
    # export GENERATOR="MSYS Makefiles"
    ;;
esac

if [ -z "$BUILD" ]; then
 BUILD="release"

fi

if [ -z "$CHIP" ]; then
 CHIP="cpu"
fi

if [ -z "$LIBTYPE" ]; then
 LIBTYPE="dynamic"
fi

if [ -z "$PACKAGING" ]; then
 PACKAGING="none"
fi

if [ -z "$COMPUTE" ]; then
 COMPUTE="all"
fi

if [ "$CHIP_EXTENSION" == "avx512" ] || [ "$ARCH" == "avx512" ]; then
    CHIP_EXTENSION="avx512"
    ARCH="skylake-avx512"
elif [ "$CHIP_EXTENSION" == "avx2" ] || [ "$ARCH" == "avx2" ]; then
    CHIP_EXTENSION="avx2"
    ARCH="x86-64"
elif [ "$CHIP_EXTENSION" == "x86_64" ] || [ "$ARCH" == "x86_64" ]; then
    CHIP_EXTENSION="x86_64"
    ARCH="x86-64"
fi

if [ -z "$ARCH" ]; then
 ARCH="x86-64"
fi

OPERATIONS_ARG=

if [ -z "$OPERATIONS" ]; then
 OPERATIONS_ARG="-DLIBND4J_ALL_OPS=true"
else
 OPERATIONS_ARG=$OPERATIONS
fi

if [ -z "$EXPERIMENTAL" ]; then
 EXPERIMENTAL="no"
fi

if [ "$CHIP" == "cpu" ]; then
    BLAS_ARG="-DCPU_BLAS=true -DBLAS=TRUE"
else
    BLAS_ARG="-DCUDA_BLAS=true -DBLAS=TRUE"
fi

if [ -z "$NAME" ]; then
    if [ "$CHIP" == "cpu" ]; then
        NAME="nd4jcpu"
    else
        NAME="nd4jcuda"
    fi
fi

if [ "$LIBTYPE" == "dynamic" ]; then
     SHARED_LIBS_ARG="-DBUILD_SHARED_LIBS=OFF"
     else
         SHARED_LIBS_ARG="-DBUILD_SHARED_LIBS=ON"
fi

if [ "$BUILD" == "release" ]; then
        BUILD_TYPE="-DCMAKE_BUILD_TYPE=Release"
    else
        BUILD_TYPE="-DCMAKE_BUILD_TYPE=Debug"

fi

if [ "$PACKAGING" == "none" ]; then
    PACKAGING_ARG="-DPACKAGING=none"
fi

if [ "$PACKAGING" == "rpm" ]; then
    PACKAGING_ARG="-DPACKAGING=rpm"
fi

if [ "$PACKAGING" == "deb" ]; then
    PACKAGING_ARG="-DPACKAGING=deb"
fi

if [ "$PACKAGING" == "msi" ]; then
    PACKAGING_ARG="-DPACKAGING=msi"
fi

EXPERIMENTAL_ARG="no";
MINIFIER_ARG=
NAME_ARG="-DLIBND4J_NAME=$NAME"

if [ "$EXPERIMENTAL" == "yes" ]; then
    EXPERIMENTAL_ARG="-DEXPERIMENTAL=yes"
fi

if [ "$MINIFIER" == "true" ]; then
    MINIFIER_ARG="-DLIBND4J_BUILD_MINIFIER=true"
fi

ARCH_ARG="-DARCH=$ARCH -DEXTENSION=$CHIP_EXTENSION"

CUDA_COMPUTE="-DCOMPUTE=$COMPUTE"

if [ "$CHIP" == "cuda" ] && [ -n "$CHIP_VERSION" ]; then
    case $OS in
        linux*)
        export CUDA_PATH="/usr/local/cuda-$CHIP_VERSION/"
        ;;
        macosx*)
        export CUDA_PATH="/Developer/NVIDIA/CUDA-$CHIP_VERSION/"
        ;;
        windows*)
        export CUDA_PATH="C:/Program Files/NVIDIA GPU Computing Toolkit/CUDA/v$CHIP_VERSION/"
        ;;
    esac
fi

mkbuilddir() {
    if [ "$CLEAN" == "true" ]; then
        echo "Removing blasbuild"
        rm -Rf blasbuild
    fi
    mkdir -p blasbuild
    cd blasbuild
    CHIP_DIR="$CHIP"
    if [ -n "$CHIP_EXTENSION" ]; then
        CHIP_DIR="$CHIP_DIR-$CHIP_EXTENSION"
    fi
    if [ "$CHIP" == "cuda" ] && [ -n "$CHIP_VERSION" ]; then
        CHIP_DIR="$CHIP_DIR-$CHIP_VERSION"
    fi

    # create appropriate directories and links here for ND4J
    if [ "$CHIP" != "$CHIP_DIR" ]; then
        mkdir -p "$CHIP_DIR"
        rm -f "$CHIP"
        ln -s "$CHIP_DIR" "$CHIP"
        mkdir -p "$CHIP/blas"
        cd "$CHIP_DIR"
    else
        mkdir -p "$CHIP"
        cd "$CHIP"
    fi
}


echo PACKAGING  = "${PACKAGING}"
echo BUILD  = "${BUILD}"
echo CHIP     = "${CHIP}"
echo ARCH    = "${ARCH}"
echo CHIP_EXTENSION  = "${CHIP_EXTENSION}"
echo CHIP_VERSION    = "${CHIP_VERSION}"
echo GPU_COMPUTE_CAPABILITY    = "${COMPUTE}"
echo EXPERIMENTAL = ${EXPERIMENTAL}
echo LIBRARY TYPE    = "${LIBTYPE}"
echo OPERATIONS = "${OPERATIONS_ARG}"
echo MINIFIER = "${MINIFIER}"
echo NAME = "${NAME_ARG}"
mkbuilddir
pwd
eval $CMAKE_COMMAND  "$BLAS_ARG" "$ARCH_ARG" "$NAME_ARG" "$SHARED_LIBS_ARG" "$MINIFIER_ARG" "$OPERATIONS_ARG" "$BUILD_TYPE" "$PACKAGING_ARG" "$EXPERIMENTAL_ARG" "$CUDA_COMPUTE" -DDEV=FALSE -DMKL_MULTI_THREADED=TRUE ../..
if [ "$PARALLEL" == "true" ]; then
        eval $MAKE_COMMAND -j $MAKEJ && cd ../../..
    else
        eval $MAKE_COMMAND && cd ../../..
fi
