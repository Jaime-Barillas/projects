#include "SDL3/SDL_stdinc.h"
#define SDL_MAIN_USE_CALLBACKS 1 /* use the callbacks instead of main() */
#include <SDL3/SDL.h>
#include <SDL3/SDL_error.h>
#include <SDL3/SDL_gpu.h>
#include <SDL3/SDL_init.h>
#include <SDL3/SDL_log.h>
#include <SDL3/SDL_main.h>
#include <SDL3/SDL_video.h>
#include <stdint.h>

#include "imgui.h"
#include "imgui_impl_sdl3.h"
#include "imgui_impl_sdlgpu3.h"

typedef struct {
  SDL_Window *window;
  SDL_GPUDevice *device;
} AppContext;

SDL_AppResult SDL_AppInit(void **appstate, int argc, char *argv[]) {
  SDL_SetAppMetadata("Particles", "0.0.1", "xyz.lost-in-space.particles");

  if (!SDL_Init(SDL_INIT_VIDEO)) {
    SDL_Log("Couldn't initialize SDL: %s", SDL_GetError());
    return SDL_APP_FAILURE;
  }

  float dpi = SDL_GetDisplayContentScale(SDL_GetPrimaryDisplay());
  int32_t width = (int32_t)(1152 * dpi);
  int32_t height = (int32_t)(720 * dpi);
  SDL_WindowFlags window_flags =
      SDL_WINDOW_RESIZABLE | SDL_WINDOW_HIGH_PIXEL_DENSITY;
  SDL_Window *window =
      SDL_CreateWindow("Particles", width, height, window_flags);
  if (!window) {
    SDL_Log("Couldn't create window: %s", SDL_GetError());
    return SDL_APP_FAILURE;
  }

  SDL_GPUDevice *device =
      SDL_CreateGPUDevice(SDL_GPU_SHADERFORMAT_SPIRV, true, nullptr);
  if (device == nullptr) {
    SDL_Log("Couldn't create gpu device: %s", SDL_GetError());
    return SDL_APP_FAILURE;
  }
  if (!SDL_ClaimWindowForGPUDevice(device, window)) {
    SDL_Log("Couldn't claim window for gpu: %s", SDL_GetError());
  }

  // NOTE: Consider presentmode mailbox?
  SDL_SetGPUSwapchainParameters(device, window,
                                SDL_GPU_SWAPCHAINCOMPOSITION_SDR,
                                SDL_GPU_PRESENTMODE_VSYNC);

  // Imgui Initialization
  IMGUI_CHECKVERSION();
  ImGui::CreateContext();
  ImGuiIO &io = ImGui::GetIO(); // (void)io;
  io.ConfigFlags |= ImGuiConfigFlags_NavEnableKeyboard;

  ImGui::StyleColorsDark();
  ImGuiStyle &style = ImGui::GetStyle();
  style.FontScaleDpi = dpi;
  style.ScaleAllSizes(dpi);
  style.FontSizeBase = (int32_t)(16 * dpi); // Round by truncation

  ImGui_ImplSDL3_InitForSDLGPU(window);
  ImGui_ImplSDLGPU3_InitInfo init_info = {};
  init_info.Device = device;
  init_info.ColorTargetFormat =
      SDL_GetGPUSwapchainTextureFormat(device, window);
  init_info.MSAASamples = SDL_GPU_SAMPLECOUNT_1;
  init_info.SwapchainComposition = SDL_GPU_SWAPCHAINCOMPOSITION_SDR;
  init_info.PresentMode = SDL_GPU_PRESENTMODE_VSYNC;
  ImGui_ImplSDLGPU3_Init(&init_info);
  // Our state
  bool show_demo_window = true;
  bool show_another_window = false;
  ImVec4 clear_color = ImVec4(0.45f, 0.55f, 0.60f, 1.00f);

  AppContext *context = (AppContext *)SDL_malloc(sizeof(AppContext));
  context->window = window;
  context->device = device;
  *appstate = context;

  return SDL_APP_CONTINUE; /* carry on with the program! */
}

SDL_AppResult SDL_AppEvent(void *appstate, SDL_Event *event) {
  AppContext *context = (AppContext *)appstate;
  ImGui_ImplSDL3_ProcessEvent(event);

  if (event->type == SDL_EVENT_QUIT) {
    return SDL_APP_SUCCESS; /* end the program, reporting success to the OS. */
  }
  return SDL_APP_CONTINUE; /* carry on with the program! */
}

/* This function runs once per frame, and is the heart of the program. */
SDL_AppResult SDL_AppIterate(void *appstate) {
  AppContext *context = (AppContext *)appstate;

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

    static int32_t radio_group = 0;
    ImGui::SeparatorText("Implementations");
    ImGui::RadioButton("Singlethreaded", &radio_group, 0);
    ImGui::RadioButton("Multithreaded (Slow)", &radio_group, 1);
    ImGui::RadioButton("Multithreaded (Fast)", &radio_group, 2);
    ImGui::RadioButton("Singlethreaded SIMD", &radio_group, 3);
    ImGui::RadioButton("Multithreaded SIMD", &radio_group, 4);
    ImGui::RadioButton("GPU", &radio_group, 5);
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
  SDL_WaitAndAcquireGPUSwapchainTexture(command_buffer, context->window,
                                        &swapchain_texture, nullptr, nullptr);

  if (swapchain_texture != nullptr && !is_minimized) {
    // This is mandatory: call ImGui_ImplSDLGPU3_PrepareDrawData() to upload the
    // vertex/index buffer!
    ImGui_ImplSDLGPU3_PrepareDrawData(draw_data, command_buffer);

    // Setup and start a render pass
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

  // Submit the command buffer
  SDL_SubmitGPUCommandBuffer(command_buffer);

  return SDL_APP_CONTINUE; /* carry on with the program! */
}

/* This function runs once at shutdown. */
void SDL_AppQuit(void *appstate, SDL_AppResult result) {
  AppContext *context = (AppContext *)appstate;

  SDL_WaitForGPUIdle(context->device);
  ImGui_ImplSDL3_Shutdown();
  ImGui_ImplSDLGPU3_Shutdown();
  ImGui::DestroyContext();

  if (context == nullptr)
    return;
  if (context->window == nullptr)
    return;
  SDL_ReleaseWindowFromGPUDevice(context->device, context->window);
  SDL_DestroyWindow(context->window);

  if (context->device == nullptr)
    return;
  SDL_DestroyGPUDevice(context->device);

  SDL_Quit();
}
