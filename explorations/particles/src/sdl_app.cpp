#include "sdl_app.h"

#include "SDL3/SDL_error.h"
#include "SDL3/SDL_events.h"
#include "SDL3/SDL_gpu.h"
#include "SDL3/SDL_init.h"
#include "SDL3/SDL_video.h"
#include "imgui.h"
#include "imgui_impl_sdl3.h"
#include "imgui_impl_sdlgpu3.h"

#include <format>
#include <stdexcept>
#include <stdint.h>

/**********/
/* SDLApp */
/**********/

void SDLApp::init_sdl() {
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

  this->window = window;
  this->device = device;
  this->dpi = dpi;
  this->window_claimed = true;
}

void SDLApp::init_imgui() {
  IMGUI_CHECKVERSION();
  ImGui::CreateContext();
  ImGuiIO &io = ImGui::GetIO();
  io.ConfigFlags |= ImGuiConfigFlags_NavEnableKeyboard;

  ImGui::StyleColorsDark();
  ImGuiStyle &style = ImGui::GetStyle();
  style.FontScaleDpi = dpi;
  style.ScaleAllSizes(dpi);
  style.FontSizeBase = SDL_floorf(16 * dpi);

  ImGui_ImplSDL3_InitForSDLGPU(window);
  ImGui_ImplSDLGPU3_InitInfo init_info = {};
  init_info.Device = device;
  init_info.ColorTargetFormat =
      SDL_GetGPUSwapchainTextureFormat(device, window);
  init_info.MSAASamples = SDL_GPU_SAMPLECOUNT_1;
  init_info.SwapchainComposition = SDL_GPU_SWAPCHAINCOMPOSITION_SDR;
  init_info.PresentMode = SDL_GPU_PRESENTMODE_VSYNC;
  ImGui_ImplSDLGPU3_Init(&init_info);
}

void SDLApp::_app_init() {
  init_sdl();
  init_imgui();
  init(); // User hook
}

void SDLApp::quit_imgui() {
  SDL_WaitForGPUIdle(device);
  ImGui_ImplSDL3_Shutdown();
  ImGui_ImplSDLGPU3_Shutdown();
  ImGui::DestroyContext();
}

void SDLApp::quit_sdl() {
  if (window_claimed) {
    SDL_ReleaseWindowFromGPUDevice(device, window);
  }

  if (device) {
    SDL_DestroyGPUDevice(device);
  }

  if (window) {
    SDL_DestroyWindow(window);
  }

  SDL_Quit();
}

void SDLApp::_app_quit() {
  quit(); // User hook
  quit_imgui();
  quit_sdl();
}

SDL_AppResult SDLApp::_app_event(SDL_Event *event) {
  ImGui_ImplSDL3_ProcessEvent(event);

  if (event->type == SDL_EVENT_QUIT) {
    return SDL_APP_SUCCESS;
  }

  return SDL_APP_CONTINUE;
}

void SDLApp::draw_ui(SDLApp::RenderContext ctx) {
  ImGui::Render();
  ImDrawData *draw_data = ImGui::GetDrawData();
  const bool is_minimized =
      (draw_data->DisplaySize.x <= 0.0f || draw_data->DisplaySize.y <= 0.0f);

  if (ctx.swapchain_texture != nullptr && !is_minimized) {
    // This is mandatory: call ImGui_ImplSDLGPU3_PrepareDrawData() to upload
    // the vertex/index buffer!
    ImGui_ImplSDLGPU3_PrepareDrawData(draw_data, ctx.command_buffer);

    SDL_GPUColorTargetInfo target_info = {};
    target_info.texture = ctx.swapchain_texture;
    target_info.clear_color = SDL_FColor{0.2f, 0.2f, 0.2, 1.0f};
    target_info.load_op = SDL_GPU_LOADOP_CLEAR;
    target_info.store_op = SDL_GPU_STOREOP_STORE;
    target_info.mip_level = 0;
    target_info.layer_or_depth_plane = 0;
    target_info.cycle = false;
    SDL_GPURenderPass *render_pass =
        SDL_BeginGPURenderPass(ctx.command_buffer, &target_info, 1, nullptr);

    ImGui_ImplSDLGPU3_RenderDrawData(
        draw_data, ctx.command_buffer, render_pass);

    SDL_EndGPURenderPass(render_pass);
  }
}

void SDLApp::_app_iterate() {
  RenderContext ctx;
  ctx.command_buffer = SDL_AcquireGPUCommandBuffer(device);
  SDL_WaitAndAcquireGPUSwapchainTexture(
      ctx.command_buffer, window, &ctx.swapchain_texture, nullptr, nullptr);

  // User hooks.
  update_ui();
  update();
  draw(ctx);
  draw_ui(ctx);

  SDL_SubmitGPUCommandBuffer(ctx.command_buffer);
}
