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
	gcc -I/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers -O0 -g3 -Wall -c -fmessage-length=0 -m64 -fPIC -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


