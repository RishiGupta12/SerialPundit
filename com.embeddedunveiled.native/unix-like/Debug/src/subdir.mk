################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../src/unix_like_serial.c \
../src/unix_like_serial_lib.c 

OBJS += \
./src/unix_like_serial.o \
./src/unix_like_serial_lib.o 

C_DEPS += \
./src/unix_like_serial.d \
./src/unix_like_serial_lib.d 


# Each subdirectory must supply rules for building sources it contributes
src/%.o: ../src/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -I/home/r/packages/jdk/jdk1.6.0_45/include -include/home/r/packages/jdk/jdk1.6.0_45/include/jni.h -O0 -g3 -Wall -c -fmessage-length=0 -v -fPIC -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


