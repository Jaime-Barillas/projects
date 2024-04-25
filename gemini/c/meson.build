project('gemini', 'c',
  version: '0.1',
  license: 'MIT',
  default_options: [
    'c_std=c11',
    'warning_level=3',
    # Bcrypt is required by LibreSSL's libcrypto on Windows.
    'c_winlibs=-lWs2_32,-lBcrypt'
  ]
)

libressl = subproject('libressl',
  default_options: [
    'c_std=gnu99', # TODO: only if meson.get_compiler('c').get_id() == 'gcc'
    'warning_level=0'
  ]
)

crypto = dependency('crypto')
ssl = dependency('ssl')
tls = dependency('tls')
threads = dependency('threads')
deps = [threads, tls, ssl, crypto]

executable('server',
  'server/main.c',
  dependencies: deps
)
