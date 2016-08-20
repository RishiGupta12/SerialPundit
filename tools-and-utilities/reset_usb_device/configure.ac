# -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.

# This file is part of SerialPundit.
#
# Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
#
# The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero 
# General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
# license for commercial use of this software. 
#
# The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
# without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

##### Common #####

# Minimum autoconf program version to process the configure.ac.
AC_PREREQ(2.63)

# Name, version of the software package for which to generate a configure script, email address of the developer.
AC_INIT([spusbrst], [1.0], [SerialPundit.com])

# Safety check that the required source file exist.
AC_CONFIG_SRCDIR([src/reset_usb_device.c])

# Specify dir as the location of additional local Autoconf macros.
AC_CONFIG_MACRO_DIR([m4])

# Names the header file which will hold the preprocessor macro definitions at run time. When there are many if 
# defined tests things will become messy. So take all the options from user input and system specific and create 
# a consolidated header file that each source file (abc.c) will include.
AC_CONFIG_HEADERS([config.h])

# Directory where install.sh scripts etc will be found.
AC_CONFIG_AUX_DIR([build-aux])

# List of flags/options for automake. It does several things that are required for proper Automake operation.
AM_INIT_AUTOMAKE([foreign 1.11 -Wall -Wno-portability subdir-objects silent-rules color-tests dist-xz tar-ustar])

# Determine host OS. It will set host variables (host_cpu, host_vendor, host_os) which can be used than for OS 
# specific configuration or hacks.
AC_CANONICAL_HOST
AC_DEFINE_UNQUOTED([CANONICAL_HOST], "$host", [Canonical host string.])

# Set CC variable to compiler found on system.
AC_PROG_CC

# Set output variable INSTALL to the name of a BSD-compatible install program, if one is found in the current PATH. 
# Otherwise, set INSTALL to ‘dir/install-sh -c’, checking the directories specified to AC_CONFIG_AUX_DIR (or its default 
# directories). Also set the variables INSTALL_PROGRAM and INSTALL_SCRIPT to ‘${INSTALL}’ and INSTALL_DATA to ‘${INSTALL} 
# -m 644’.
AC_PROG_INSTALL

# Check if M4 is present or not.
AC_CHECK_PROGS([M4], gm4 m4, no)
AS_IF([test "x$M4" = "xno"], AC_MSG_ERROR([m4 missing]))

#### Udev support ####

AC_ARG_ENABLE([udev],
    AS_HELP_STRING([--disable-udev],[Disable optional UDEV support]))

AS_IF([test "x$enable_udev" != "xno"],
    [PKG_CHECK_MODULES(UDEV, [ libudev >= 143 ], HAVE_UDEV=1, HAVE_UDEV=0)],
    HAVE_UDEV=0)

AS_IF([test "x$enable_udev" = "xyes" && test "x$HAVE_UDEV" = "x0"],
    [AC_MSG_ERROR([*** UDEV support not found])])

AC_SUBST(HAVE_UDEV)
AM_CONDITIONAL([HAVE_UDEV], [test "x$HAVE_UDEV" = x1])
AS_IF([test "x$HAVE_UDEV" = "x1"], AC_DEFINE([HAVE_UDEV], 1, [Have UDEV.]))

# Give option to user if he wants to use udev or not, set install location of udev, group and mode for device nodes. 
UDEV=/lib/udev
AC_ARG_WITH(udev, 
    AC_HELP_STRING([--with-udev=DIR],[directory where udev is installed [default=/lib/udev]]),[UDEV="${withval}"], [])
AC_SUBST(UDEV)

UDEV_RULES=99-spusbrst.rules
AC_ARG_WITH(udev-rules,
    AC_HELP_STRING([--with-udev-rules=NAME],[file name for udev rules [default=99-spusbrst.rules]]),[UDEV_RULES="${withval}"], [])
AC_SUBST(UDEV_RULES)

UDEV_GROUP=
AC_ARG_WITH(udev-group,
    AC_HELP_STRING([--with-udev-group=GROUP],[file group for device nodes [default: none specified]]),[UDEV_GROUP="-g\"${withval}\""], [])
AC_SUBST(UDEV_GROUP)

# Optionally set mode for device nodes
UDEV_MODE=
AC_ARG_WITH(udev-mode,
    AC_HELP_STRING([--with-udev-mode=GROUP],[file mode for device nodes [default: none specified]]),[UDEV_MODE="-m\"${withval}\""], [])
AC_SUBST(UDEV_MODE)

#### Determine host OS ####

os_is_linux=0

AC_MSG_CHECKING([host operating system])
case "$host_os" in
    linux*)
        AC_MSG_RESULT([linux])
        os_is_linux=1
    ;;
    *)
        AC_MSG_ERROR([This utility is for Linux OS only !] )
    ;;
esac

#### Compiler flags ####

AX_APPEND_COMPILE_FLAGS([-Wall -W -Wextra -pipe -Wno-long-long -Wno-overlength-strings -Wunsafe-loop-optimizations -Wundef -Wformat=2 -Wlogical-op -Wsign-compare -Wformat-security -Wmissing-include-dirs -Wformat-nonliteral -Wold-style-definition -Wpointer-arith -Winit-self -Wdeclaration-after-statement -Wfloat-equal -Wmissing-prototypes -Wstrict-prototypes -Wredundant-decls -Wmissing-declarations -Wmissing-noreturn -Wshadow -Wendif-labels -Wcast-align -Wstrict-aliasing -Wwrite-strings -Wno-unused-parameter -ffast-math -fno-common -fdiagnostics-show-option -fdiagnostics-color=auto], , [-pedantic -Werror])

AS_CASE([" $CFLAGS "], [*" -O0 "*], [], [
    # Don't append the flag if it already exists. Only enable fastpath asserts when doing a debug build, 
    # e.g. from bootstrap.sh.
    AX_APPEND_FLAG([-DFASTPATH], [CPPFLAGS])

    # Cannot use AX_APPEND_FLAG here, as it assumes no space inside the added flags.
    # Cannot append flags with AX_APPEND_FLAG one by one, as this would destroy all fortifications
    # if CPPFLAGS already contain -D_FORTIFY_SOURCE=2.

    # Warnings to be aware of that appear with -D_FORTIFY_SOURCE=2 but without -U_FORTIFY_SOURCE:
    # On Fedora 20 with -O0: #warning _FORTIFY_SOURCE requires compiling with optimization (-O) [-Wcpp]
    # On Gentoo with -O2:    "_FORTIFY_SOURCE" redefined [enabled by default]
    AS_VAR_APPEND([CPPFLAGS],[" -U_FORTIFY_SOURCE -D_FORTIFY_SOURCE=2"])
])

#### Linker flags ####

# Check whether the linker supports the -version-script option. The Make variable $(srcdir) needs to be in the 
# LDFLAGS in that form, so that it is expanded the right way in every subdir.
AX_CHECK_LINK_FLAG(["-Wl,-version-script=${srcdir}/src/map-file"],
    [VERSIONING_LDFLAGS='-Wl,-version-script=$(abs_top_srcdir)/src/map-file'])
AC_SUBST([VERSIONING_LDFLAGS])

#### ARM NEON optimisations ####
AC_ARG_ENABLE([neon-opt],
    AS_HELP_STRING([--enable-neon-opt], [Enable NEON optimisations on ARM CPUs that support it]))

AS_IF([test "x$enable_neon_opt" != "xno"],
    [save_CFLAGS="$CFLAGS"; CFLAGS="-mfpu=neon $CFLAGS"
     AC_COMPILE_IFELSE(
        [AC_LANG_PROGRAM([[#include <arm_neon.h>]], [])],
        [
         HAVE_NEON=1
         NEON_CFLAGS="-mfpu=neon"
        ],
        [
         HAVE_NEON=0
         NEON_CFLAGS=
        ])
     CFLAGS="$save_CFLAGS"
    ],
    [HAVE_NEON=0])

AS_IF([test "x$enable_neon_opt" = "xyes" && test "x$HAVE_NEON" = "x0"],
      [AC_MSG_ERROR([*** Compiler does not support -mfpu=neon or CFLAGS override -mfpu])])

AC_SUBST(HAVE_NEON)
AC_SUBST(NEON_CFLAGS)
AM_CONDITIONAL([HAVE_NEON], [test "x$HAVE_NEON" = x1])
AS_IF([test "x$HAVE_NEON" = "x1"], AC_DEFINE([HAVE_NEON], 1, [Have NEON support?]))

#### Basic environment checks ####

# Define STDC_HEADERS if the system has ANSI C header files.
AC_HEADER_STDC

# Required headers.
AC_CHECK_HEADER(errno.h, , [AC_MSG_ERROR([Couldn't find errno.h] )])
AC_CHECK_HEADER(sys/ioctl.h, , [AC_MSG_ERROR([Couldn't find sys/ioctl.h] )])
AC_CHECK_HEADER(linux/usbdevice_fs.h, , [AC_MSG_ERROR([Couldn't find linux/usbdevice_fs.h] )])

# Required libs
AC_SEARCH_LIBS([dlopen], [dl])

#### Build and Install man pages ####

AC_ARG_ENABLE([manpages],
    AS_HELP_STRING([--disable-manpages],[Disable building and installation of man pages]))

AM_CONDITIONAL([BUILD_MANPAGES], [test "x$enable_manpages" != "xno"])

# Automake uses these to determine which files to create.
AC_CONFIG_FILES([
Makefile
src/Makefile
])
AC_OUTPUT

# Print configure summary and list make options
AC_DEFUN([AX_SUMMARIZE_ENV],
[
BUILD_USER=${USER}
BUILD_ARCH=${host}
BUILD_HOST=${ac_hostname}
BUILD_DATE=$(date +'%F %H:%M')

AC_DEFINE_UNQUOTED([BUILD_USER],     "${BUILD_USER}",     [The fine user who built the package])
AC_DEFINE_UNQUOTED([BUILD_ARCH],     "${BUILD_ARCH}",     [Architecture of the build host])
AC_DEFINE_UNQUOTED([BUILD_HOST],     "${BUILD_HOST}",     [Build host name])
AC_DEFINE_UNQUOTED([BUILD_DATE],     "${BUILD_DATE}",     [Build date])

AC_SUBST(BUILD_USER)
AC_SUBST(BUILD_ARCH)
AC_SUBST(BUILD_HOST)
AC_SUBST(BUILD_DATE)
AC_SUBST(BUILD_VERSION)
])

AC_DEFUN([AX_SUMMARIZE_CONFIG],
[
echo
echo '##########################################################################'
echo '#                                SUMMARY                                 #'
echo '##########################################################################'
echo
echo Package version............... : $PACKAGE-$VERSION
echo C compiler.................... : $CC
echo C compiler flags.............. : $CFLAGS
echo Configure date................ : $BUILD_DATE
echo Build architecture............ : $BUILD_ARCH
echo Build host.................... : $BUILD_HOST
echo Build user.................... : $USER
echo Install dir................... : $prefix

echo Configure complete! Now procced with:
echo "    - 'make'               compile the project"
echo "    - 'sudo make install'  install the utility"
])

AX_SUMMARIZE_ENV
AX_SUMMARIZE_CONFIG

