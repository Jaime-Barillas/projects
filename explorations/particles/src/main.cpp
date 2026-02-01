#define SDL_MAIN_USE_CALLBACKS 1
#include "SDL3/SDL_init.h"
#include "SDL3/SDL_log.h"
#include "SDL3/SDL_main.h"

#include <exception>

#include "app.h"

SDL_AppResult SDL_AppInit(void **appstate, int argc, char *argv[]) {
  try {
    App *app = new App();
    app->_app_init();
    *appstate = app;
  } catch (std::exception ex) {
    SDL_Log("Failed to initialize SDL: %s", ex.what());
    return SDL_APP_FAILURE;
  }

  return SDL_APP_CONTINUE;
}

SDL_AppResult SDL_AppEvent(void *appstate, SDL_Event *event) {
  SDLApp *app = static_cast<SDLApp *>(appstate);

  return app->_app_event(event);
}

SDL_AppResult SDL_AppIterate(void *appstate) {
  SDLApp *app = static_cast<SDLApp *>(appstate);
  app->_app_iterate();

  return SDL_APP_CONTINUE;
}

void SDL_AppQuit(void *appstate, SDL_AppResult result) {
  SDLApp *app = static_cast<SDLApp *>(appstate);
  app->_app_quit();
  delete app;
}
