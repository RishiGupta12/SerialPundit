ProgSTM32 : Flash firmware in stm32 microcontrollers
-----------------------------------

In STM32 (ARM Cortex-M based) microcontrollers, a default bootloader is programmed into system memory by ST Microelectronics. This bootloader can communicate to host computer through USART port using a well defined protocol.

**progstm32 sdk**: Java applications can implement functionality to upgrade firmware in their stm32 based product by using the APIs provided by this sdk. It implements complete protocol to communicate with factory bootloader in stm32 microcontroller. GUI based application can use this in their `'Help->Upgrade'` menu option, where if use selects this option new firmware gets flashed in the end user product. It saves time to market, application development cost and helps engineers to focus more on business use case.

**progstm32 app**: It is a commandline application based on progstm32 sdk. It can be used as an independent flashing utility for stm32 microcontrollers. More information about options and usage can be obtained from manpage.

## Features
- Erase, read and write firmware in memory
- No restriction on addresses and address length
- Read bootloader ID, ROM-programmed data, product ID and bootloader version
- Handle device specific quirks internally
- Robust error handling
- Enable/disable read/write protection of memory
- Resume communication with stm32 if required
- Convert intel hex firmware to plain binary
- Fully documented and tested

This is hosted as a [separate project here](https://github.com/RishiGupta12/ProgSTM32)
