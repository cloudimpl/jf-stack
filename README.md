# jf-stack
dpdk asynchronous tcp engine based on f-stack

change the config ini with correct network configuration
[port0]
addr=<ena card ip>
netmask=<subnet>
broadcast=<broad cast ip>
gateway=< gateway ip>

example : 
client : 
      java -Djava.library.path=lib/. -cp target/f-stack-1.0.jar com.cloudimpl.net.example.PingPong -- c <remoteip> 12345

server:
      java -Djava.library.path=lib/. -cp target/f-stack-1.0.jar com.cloudimpl.net.example.PingPong -- s 12345
