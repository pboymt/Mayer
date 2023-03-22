
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.22.1)

# Declares and names the project.

project("mayer")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             mayer

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             mayer.cpp )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       mayer

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )


# OpenCV
# join path with cmake_path
cmake_path(SET OPENCV_DIR "${CMAKE_SOURCE_DIR}/../../../../opencv" NORMALIZE)
MESSAGE("OPENCV_DIR: ${OPENCV_DIR}")
add_library(libopencv_java4 SHARED IMPORTED)
set_target_properties(libopencv_java4 PROPERTIES IMPORTED_LOCATION
        ${OPENCV_DIR}/native/libs/${ANDROID_ABI}/libopencv_java4.so)
include_directories(${OPENCV_DIR}/native/jni/include)

