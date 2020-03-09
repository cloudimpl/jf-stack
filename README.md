# jf-stack
dpdk asynchronous tcp engine based on f-stack

change the config ini with correct network configuration <br>
[port0] <br>
addr= ena card ip <br>
netmask= subnet  <br>
broadcast= broad cast ip <br>
gateway= gateway ip<br>

example : <br>
client :<br>
      java -Djava.library.path=lib/. -cp target/f-stack-1.0.jar com.cloudimpl.net.example.PingPong -- c <remoteip> 12345 <br>

server: <br>
      java -Djava.library.path=lib/. -cp target/f-stack-1.0.jar com.cloudimpl.net.example.PingPong -- s 12345
