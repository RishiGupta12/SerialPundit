################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../src/unix_like_serial.c \
../src/unix_like_serial_lib.c \
../src/windows_serial.c \
../src/windows_serial_lib.c 

OBJS += \
./src/unix_like_serial.o \
./src/unix_like_serial_lib.o \
./src/windows_serial.o \
./src/windows_serial_lib.o 

C_DEPS += \
./src/unix_like_serial.d \
./src/unix_like_serial_lib.d \
./src/windows_serial.d \
./src/windows_serial_lib.d 


# Each subdirectory must supply rules for building sources it contributes
src/%.o: ../src/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: Cross GCC Compiler'
	gcc -I/home/r/packages/jdk/jdk1.6.0_45/include -include/home/r/packages/jdk/jdk1.6.0_45/include/jni.h -O3 -Wall -c -fmessage-length=0 -fPIC -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


