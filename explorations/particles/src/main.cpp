#define SDL_MAIN_USE_CALLBACKS 1
#include <SDL3/SDL.h>
#include <SDL3/SDL_error.h>
#include <SDL3/SDL_gpu.h>
#include <SDL3/SDL_init.h>
#include <SDL3/SDL_log.h>
#include <SDL3/SDL_main.h>
#include <SDL3/SDL_stdinc.h>
#include <SDL3/SDL_video.h>

#include "imgui.h"
#include "imgui_impl_sdl3.h"
#include "imgui_impl_sdlgpu3.h"

#include <exception>
#include <format>
#include <stdexcept>
#include <stdint.h>

/*********************/
/* Type Declarations */
/*********************/
typedef struct {
  SDL_Window *window;
  SDL_GPUDevice *device;
  float dpi;
} AppContext;

/*************/
/* Functions */
/*************/
void init_sdl(AppContext *context) {
  SDL_SetAppMetadata("Particles", "0.0.1", "xyz.lost-in-space.particles");

  if (!SDL_Init(SDL_INIT_VIDEO)) {
    throw std::runtime_error(
        std::format("Couldn't initialize SDL: %s", SDL_GetError()));
  }

  float dpi = SDL_GetDisplayContentScale(SDL_GetPrimaryDisplay());
  int32_t width = static_cast<int32_t>(1152 * dpi);
  int32_t height = static_cast<int32_t>(720 * dpi);
  SDL_WindowFlags window_flags =
      SDL_WINDOW_RESIZABLE | SDL_WINDOW_HIGH_PIXEL_DENSITY;

  SDL_Window *window =
      SDL_CreateWindow("Particles", width, height, window_flags);
  if (!window) {
    throw std::runtime_error(
        std::format("Couldn't create window: %s", SDL_GetError()));
  }

  SDL_GPUDevice *device =
      SDL_CreateGPUDevice(SDL_GPU_SHADERFORMAT_SPIRV, true, nullptr);
  if (device == nullptr) {
    throw std::runtime_error(
        std::format("Couldn't create gpu device: %s", SDL_GetError()));
  }

  if (!SDL_ClaimWindowForGPUDevice(device, window)) {
    throw std::runtime_error(
        std::format("Couldn't claim window for gpu: %s", SDL_GetError()));
  }

  // NOTE: Consider presentmode mailbox?
  SDL_SetGPUSwapchainParameters(device,
                                window,
                                SDL_GPU_SWAPCHAINCOMPOSITION_SDR,
                                SDL_GPU_PRESENTMODE_VSYNC);

  context->window = window;
  context->device = device;
  context->dpi = dpi;
}

void init_imgui(AppContext *context) {
  IMGUI_CHECKVERSION();
  ImGui::CreateContext();
  ImGuiIO &io = ImGui::GetIO();
  io.ConfigFlags |= ImGuiConfigFlags_NavEnableKeyboard;

  ImGui::StyleColorsDark();
  ImGuiStyle &style = ImGui::GetStyle();
  style.FontScaleDpi = context->dpi;
  style.ScaleAllSizes(context->dpi);
  style.FontSizeBase = SDL_floorf(16 * context->dpi);

  ImGui_ImplSDL3_InitForSDLGPU(context->window);
  ImGui_ImplSDLGPU3_InitInfo init_info = {};
  init_info.Device = context->device;
  init_info.ColorTargetFormat =
      SDL_GetGPUSwapchainTextureFormat(context->device, context->window);
  init_info.MSAASamples = SDL_GPU_SAMPLECOUNT_1;
  init_info.SwapchainComposition = SDL_GPU_SWAPCHAINCOMPOSITION_SDR;
  init_info.PresentMode = SDL_GPU_PRESENTMODE_VSYNC;
  ImGui_ImplSDLGPU3_Init(&init_info);
}

SDL_AppResult SDL_AppInit(void **appstate, int argc, char *argv[]) {
  AppContext *context =
      static_cast<AppContext *>(SDL_malloc(sizeof(AppContext)));

  try {
    init_sdl(context);
    init_imgui(context);
    *appstate = context;
  } catch (std::exception ex) {
    SDL_Log("Failed to initialize SDL: %s", ex.what());
    return SDL_APP_FAILURE;
  }

  return SDL_APP_CONTINUE; /* carry on with the program! */
}

SDL_AppResult SDL_AppEvent(void *appstate, SDL_Event *event) {
  AppContext *context = static_cast<AppContext *>(appstate);
  ImGui_ImplSDL3_ProcessEvent(event);

  if (event->type == SDL_EVENT_QUIT) {
    return SDL_APP_SUCCESS; /* end the program, reporting success to the OS. */
  }
  return SDL_APP_CONTINUE; /* carry on with the program! */
}

/* This function runs once per frame, and is the heart of the program. */
SDL_AppResult SDL_AppIterate(void *appstate) {
  AppContext *context = static_cast<AppContext *>(appstate);

  // Start the Dear ImGui frame
  ImGui_ImplSDLGPU3_NewFrame();
  ImGui_ImplSDL3_NewFrame();
  ImGui::NewFrame();

  ImGui::SetNextWindowSize(ImVec2(0.0f, 0.0f));
  ImGui::Begin("Particles");
  {
    ImGui::Text("Update: %.2f", 9999.0f);
    ImGui::Text("Frametime: %.2f", 9999.0f);
    ImGui::Text("Particle Count: %d", 9999);

    static int32_t max_particles = 10'000;
    ImGui::Text("Max Particles:");
    ImGui::SameLine();
    ImGui::SetNextItemWidth(200.0f);
    ImGui::InputInt("Max Particles", &max_particles, 100, 10'000);

    static int32_t mode = 0;
    ImGui::SeparatorText("Implementations");
    ImGui::RadioButton("Singlethreaded", &mode, 0);
    ImGui::RadioButton("Multithreaded (Slow)", &mode, 1);
    ImGui::RadioButton("Multithreaded (Fast)", &mode, 2);
    ImGui::RadioButton("Singlethreaded SIMD", &mode, 3);
    ImGui::RadioButton("Multithreaded SIMD", &mode, 4);
    ImGui::RadioButton("GPU", &mode, 5);
  }
  ImGui::End();

  // Rendering
  ImGui::Render();
  ImDrawData *draw_data = ImGui::GetDrawData();
  const bool is_minimized =
      (draw_data->DisplaySize.x <= 0.0f || draw_data->DisplaySize.y <= 0.0f);

  SDL_GPUCommandBuffer *command_buffer =
      SDL_AcquireGPUCommandBuffer(context->device);

  SDL_GPUTexture *swapchain_texture;
  SDL_WaitAndAcquireGPUSwapchainTexture(
      command_buffer, context->window, &swapchain_texture, nullptr, nullptr);

  if (swapchain_texture != nullptr && !is_minimized) {
    // This is mandatory: call ImGui_ImplSDLGPU3_PrepareDrawData() to upload the
    // vertex/index buffer!
    ImGui_ImplSDLGPU3_PrepareDrawData(draw_data, command_buffer);

    SDL_GPUColorTargetInfo target_info = {};
    target_info.texture = swapchain_texture;
    target_info.clear_color = SDL_FColor{0.2f, 0.2f, 0.2, 1.0f};
    target_info.load_op = SDL_GPU_LOADOP_CLEAR;
    target_info.store_op = SDL_GPU_STOREOP_STORE;
    target_info.mip_level = 0;
    target_info.layer_or_depth_plane = 0;
    target_info.cycle = false;
    SDL_GPURenderPass *render_pass =
        SDL_BeginGPURenderPass(command_buffer, &target_info, 1, nullptr);

    // Render ImGui
    ImGui_ImplSDLGPU3_RenderDrawData(draw_data, command_buffer, render_pass);

    SDL_EndGPURenderPass(render_pass);
  }

  SDL_SubmitGPUCommandBuffer(command_buffer);

  return SDL_APP_CONTINUE;
}

void quit_imgui(const AppContext &context) {
  SDL_WaitForGPUIdle(context.device);
  ImGui_ImplSDL3_Shutdown();
  ImGui_ImplSDLGPU3_Shutdown();
  ImGui::DestroyContext();
}

void quit_sdl(AppContext &context) {
  if (context.window == nullptr)
    return;
  SDL_ReleaseWindowFromGPUDevice(context.device, context.window);
  SDL_DestroyWindow(context.window);

  if (context.device == nullptr)
    return;
  SDL_DestroyGPUDevice(context.device);

  SDL_Quit();
}

void SDL_AppQuit(void *appstate, SDL_AppResult result) {
  AppContext *context = static_cast<AppContext *>(appstate);

  quit_imgui(*context);
  quit_sdl(*context);
  SDL_free(context);
}
