project('slog', 'c',
  version: '1.8.37',
  license: 'MIT'
)

slog_inc = include_directories('src')

threads = dependency('threads')

slog = library('slog',
  'src/slog.c',
  include_directories: slog_inc,
  dependencies: threads
)

slog_dep = declare_dependency(
  link_with: slog,
  include_directories: slog_inc
)

meson.override_dependency('slog', slog_dep)
