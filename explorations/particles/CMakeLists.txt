cmake_minimum_required(VERSION 3.31)

# Generate compile_commands.json for lsp.
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

project(
  particles
  VERSION 0.0.1
  LANGUAGES CXX
)

include(FetchContent)

find_program(
  GLSLANG
  name glslang
)

FetchContent_Declare(
  sdl3
  GIT_REPOSITORY https://github.com/libsdl-org/SDL.git
  GIT_TAG release-3.4.0
  GIT_SHALLOW TRUE
  OVERRIDE_FIND_PACKAGE # Ensure find_package() uses fetched SDL3
)
find_package(SDL3)

FetchContent_Declare(
  imgui
  GIT_REPOSITORY https://github.com/ocornut/imgui.git
  GIT_TAG v1.92.5
  GIT_SHALLOW TRUE
)
FetchContent_MakeAvailable(imgui)
# We have to manually fetch the file and add them to the project.
# NOTE: Will need to re-run CMake to refresh source files.
file(
  GLOB
  imgui_SRCS
  "${imgui_SOURCE_DIR}/*.cpp"
  "${imgui_SOURCE_DIR}/backends/imgui_impl_sdl3.cpp"
  "${imgui_SOURCE_DIR}/backends/imgui_impl_sdlgpu3.cpp"
)

add_executable(${PROJECT_NAME})
target_compile_features(
  ${PROJECT_NAME}
  PUBLIC
    cxx_std_23
)
target_sources(
  ${PROJECT_NAME}
  PRIVATE
    src/main.cpp
    ${imgui_SRCS}
)
target_include_directories(
  ${PROJECT_NAME}
  PRIVATE
    "${imgui_SOURCE_DIR}"
    "${imgui_SOURCE_DIR}/backends"
)
target_link_libraries(
  ${PROJECT_NAME}
  PRIVATE
    SDL3-shared
)
target_compile_options(
  ${PROJECT_NAME}
  PRIVATE
    "$<$<CONFIG:Debug>:-g>"
    "$<$<CONFIG:Debug>:-O0>"
)
set_target_properties(
  ${PROJECT_NAME}
  PROPERTIES # APPEND PROPERTY
# Only the installed (and thus, packaged) version should have a different
# rpath set.
#    BUILD_RPATH "$<$<PLATFORM_ID:Linux>:$ORIGIN>"
    INSTALL_RPATH "$<$<PLATFORM_ID:Linux>:$ORIGIN>"
)
install(
  TARGETS
    ${PROJECT_NAME}
  RUNTIME DESTINATION bin
)
#set_target_properties(SDL3 PROPERTIES VERSION "" SOVERSION "") # Remove versioned symlinks in packaging
install(
  TARGETS SDL3-shared
  RUNTIME DESTINATION bin # Windows .dll
  LIBRARY DESTINATION bin # .so, .dylib
  ARCHIVE DESTINATION bin # .a, Windows .lib
)

###########
# Packaging
###########
set(CPACK_PACKAGE_NAME "${PROJECT_NAME}")
set(CPACK_PACKAGE_VERSION "${PROJECT_VERSION}")
set(CPACK_PACKAGE_DIRECTORY "${CMAKE_BINARY_DIR}/package")
if (WIN32)
  set(CPACK_GENERATOR "ZIP")
else()
  set(CPACK_GENERATOR "TGZ")
endif()

include(CPack)
