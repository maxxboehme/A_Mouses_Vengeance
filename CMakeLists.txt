cmake_minimum_required(VERSION 2.8)
project(AMousesVengeance Java)

find_package(Java REQUIRED)
include(UseJava)

file(GLOB SOURCES "src/*.java")
file(GLOB_RECURSE IMAGES RELATIVE "${CMAKE_CURRENT_SOURCE_DIR}" *.png)
add_jar(AMousesVengeance ${SOURCES} ${IMAGES} ENTRY_POINT RodentsRevengeJFrame)
