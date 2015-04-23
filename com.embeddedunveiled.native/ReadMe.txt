
- windows_serial folder : contains native shared library developed in visual studio.

- linux_serial folder : contains eclipse CDT project using gcc toolchain for Linux platform.

- macOS_serial : contains eclipse CDT project using /macosx gcc toolchain for Apple Mac book.

Note:

1. The linux and mac share common header file interface to java layer.

2. The C source files for linux and mac is exactly same. In order to maintain consistency
   and integrity of eclipse projects due to different settings and toolchain, two different
   eclipse projects are maintained.
