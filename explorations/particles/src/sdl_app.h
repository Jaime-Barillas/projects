#pragma once

#include "SDL3/SDL_events.h"
#include "SDL3/SDL_gpu.h"
#include "SDL3/SDL_init.h"
#include "SDL3/SDL_video.h"

/*********/
/* Types */
/*********/
class SDLApp {
  bool window_claimed = false;

  void init_sdl();
  void quit_sdl();
  void init_imgui();
  void quit_imgui();

protected:
  struct RenderContext {
    SDL_GPUCommandBuffer *command_buffer;
    SDL_GPUTexture *swapchain_texture;
  };

  SDL_Window *window = nullptr;
  SDL_GPUDevice *device = nullptr;
  float dpi = 1.0f;

  virtual void init() {};
  virtual void update_ui() {};
  virtual void update() {};
  virtual void draw(RenderContext ctx) {};
  /** Default ImGui drawing logic.
   * Renders the ImGui drawing data, then draws the UI within its own
   * render pass.
   */
  virtual void draw_ui(RenderContext ctx);
  virtual void quit() {};

public:
  SDLApp() = default;
  virtual ~SDLApp() = default;

  void _app_init();
  SDL_AppResult _app_event(SDL_Event *event);
  void _app_iterate();
  void _app_quit();
};
