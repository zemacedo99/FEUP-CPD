
#!/bin/bash

data="
3
4096
32
3
6144
32
3
8192
32
3
10240
32
3
4096
64
3
6144
64
3
8192
64
3
10240
64
0
"

cd ..
cd src
 ./fileout < <(echo ${data})
