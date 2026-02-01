#include "app.h"

#include "imgui_impl_sdl3.h"
#include "imgui_impl_sdlgpu3.h"

void App::update_ui() {
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
    ImGui::SeparatorText("Options");
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
}
