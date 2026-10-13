cmake_minimum_required(VERSION 3.22)

project(voidnode_core LANGUAGES CXX)

add_library(voidnode_core STATIC
    src/voidnode_core.cpp
)

target_include_directories(voidnode_core PUBLIC
    ${CMAKE_CURRENT_SOURCE_DIR}/include
)

target_compile_features(voidnode_core PUBLIC cxx_std_17)
