cmake_minimum_required(VERSION 3.10)

# set the project name
project(virtuoso_api)

# specify the C++ standard
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED True)

#Add Subdirectories to Incluce
add_subdirectory(source)
add_subdirectory(3p/docopt)
add_subdirectory(3p/fmt)


message(STATUS  "Subdirectories included")
message(STATUS "************************************************")

ADD_DEFINITIONS(-DUNICODE)
ADD_DEFINITIONS(-D_UNICODE)
SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} /UMBCS /D_UNICODE /DUNICODE")

message(STATUS  "Flags Set")
message(STATUS "************************************************")

# Set Warninglevel to 4
if(CMAKE_BUILD_TOOL MATCHES "(msdev|devenv|nmake)")
    add_definitions(/W4)
endif()


message(STATUS "WarningLevel set")
message(STATUS "************************************************")

if(WIN32)
  # List names of ODBC libraries on Windows
  set(ODBC_LIBRARY odbc32.lib)
  set(_odbc_lib_names odbc32;)

  # List additional libraries required to use ODBC library
  if(MSVC OR CMAKE_CXX_COMPILER_ID MATCHES "Intel")
    set(_odbc_required_libs_names odbccp32;ws2_32)
    message(STATUS "odbcpp32, w2_32")
  elseif(MINGW)
    set(_odbc_required_libs_names odbccp32)
    message(STATUS "only odbcpp32")
  endif()
  message(STATUS "ODBC-library included")
endif()


message(STATUS "************************************************")


add_definitions(-D_AFXDLL)
Set(CMAKE_MFC_FLAGS 2)

#message(STATUS "fmt-Format")
#message(STATUS "************************************************")
#add_library(fmt INTERFACE)
#target_sources(fmt INTERFACE format.h)

#target_include_directories(fmt INTERFACE
#    "${PROJECT_SOURCE_DIR}/3p/fmt/include"
#)


message(STATUS "CMake Done")
message(STATUS "************************************************")