################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../src/unix_like_find_usb.c \
../src/unix_like_serial.c \
../src/unix_like_serial_lib.c \
../src/unix_like_usb_connected.c \
../src/unix_like_util.c \
../src/unix_like_vcp_devnode.c 

OBJS += \
./src/unix_like_find_usb.o \
./src/unix_like_serial.o \
./src/unix_like_serial_lib.o \
./src/unix_like_usb_connected.o \
./src/unix_like_util.o \
./src/unix_like_vcp_devnode.o 

C_DEPS += \
./src/unix_like_find_usb.d \
./src/unix_like_serial.d \
./src/unix_like_serial_lib.d \
./src/unix_like_usb_connected.d \
./src/unix_like_util.d \
./src/unix_like_vcp_devnode.d 


# Each subdirectory must supply rules for building sources it contributes
src/%.o: ../src/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -I/usr/lib/jvm/java-1.7.0-openjdk-amd64/include/linux -I/usr/lib/jvm/java-7-openjdk-amd64/include -O0 -g3 -Wall -c -fmessage-length=0 -m64 -fPIC -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


