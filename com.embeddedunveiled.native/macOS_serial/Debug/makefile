################################################################################
# Automatically-generated file. Do not edit!
################################################################################

-include ../makefile.init

RM := rm -rf

# All of the sources participating in the build are defined here
-include sources.mk
-include src/subdir.mk
-include subdir.mk
-include objects.mk

ifneq ($(MAKECMDGOALS),clean)
ifneq ($(strip $(C_DEPS)),)
-include $(C_DEPS)
endif
endif

-include ../makefile.defs

# Add inputs and outputs from these tool invocations to the build variables 

# All Target
all: mac_1.0.0_x86_64.dylib

# Tool invocations
mac_1.0.0_x86_64.dylib: $(OBJS) $(USER_OBJS)
	@echo 'Building target: $@'
	@echo 'Invoking: MacOS X C Linker'
	gcc -nostartfiles -nodefaultlibs -m64 -framework IOKit -framework CoreFoundation -framework System -dynamiclib -o "mac_1.0.0_x86_64.dylib" $(OBJS) $(USER_OBJS) $(LIBS)
	@echo 'Finished building target: $@'
	@echo ' '

# Other Targets
clean:
	-$(RM) $(OBJS)$(C_DEPS)$(LIBRARIES) mac_1.0.0_x86_64.dylib
	-@echo ' '

.PHONY: all clean dependents
.SECONDARY:

-include ../makefile.targets
