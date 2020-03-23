cmake_minimum_required(VERSION 3.10)

# set the project name
project(virtuoso_api_source)


ADD_DEFINITIONS(-DUNICODE)
ADD_DEFINITIONS(-D_UNICODE)
SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} /UMBCS /D_UNICODE /DUNICODE")

# add the executable
add_executable(virtuoso_api_source
	virtuoso_api.cpp virtuoso_api.hpp
	Virtuoso.cpp Virtuoso.hpp
	VirtuosoConfig.cpp VirtuosoConfig.hpp
	VirtuosoHelper.cpp VirtuosoHelper.hpp
	pch.cpp pch.h
	rapidxml.hpp
	rapidxml_iterators.hpp
	rapidxml_iterators_unicode.hpp
	rapidxml_print.hpp
	rapidxml_print_unicode.hpp
	rapidxml_unicode.hpp
	rapidxml_utils.hpp
	rapidxml_utils_unicode.hpp
	utils.cpp utils.hpp
	virt.hpp
	xml_writer.hpp
	sparqlib_light.hpp)

#include_directories(PROJECT_SOURCE_DIR/3p/docopt)
#include_directories(PROJECT_SOURCE_DIR/3p/fmt)
target_link_libraries(virtuoso_api_source docopt)
target_link_libraries(virtuoso_api_source fmt)
#add_library(virtuoso_odbc PROJECT_SOURCE_DIR/3p/virtodbc.dll)