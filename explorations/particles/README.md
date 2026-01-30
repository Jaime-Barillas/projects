# Particles - CPU, Multithreaded, SIMD, and GPU

Explore multithreading/parallel computing via a simple particle system.

## Scope

+ C++
+ CMake
+ Interactively swap between:
  - [ ] A singlethreaded CPU implementation.
  - [ ] A multithreaded implementation.
  - [ ] A singlethreaded SIMD implementation.
  - [ ] A Multithreaded + SIMD implementation.
  - [ ] A GPU implementation.
+ Compare performance of each implementation.
  - Target 300,000 particles
  - [600,000 - 665,000 particles @60fps on an Apple M1 (base model?)](https://forum.odin-lang.org/t/simd-is-slower-compared-to-soa-or-even-aos-what-am-i-doing-wrong/1364)
    - 4 performance cores + 4 efficiency cores
    - [About 2x as fast as Core i7 7700HQ](https://www.cpu-monkey.com/en/compare_cpu-apple_m1-vs-intel_core_i7_7700hq)
    - [About 2x on cpu-benchmark too](https://cpu-benchmark.org/compare/apple-m1/intel-core-i7-7700hq/)
