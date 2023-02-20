#bash

echo -e '#bash
cd ..
java main/TestClient 127.0.0.1 8000 put file.txt
java main/TestClient 127.0.0.1 8000 put test.txt
java main/TestClient 127.0.0.1 8000 put test1.txt
java main/TestClient 127.0.0.1 8000 put another.txt' > put.sh


echo -e '#bash
cd ..
java main/TestClient 127.0.0.1 8000 get 9bba5c53a0545e0c80184b946153c9f58387e3bd1d4ee35740f29ac2e718b019
java main/TestClient 127.0.0.1 8000 get 590c9f8430c7435807df8ba9a476e3f1295d46ef210f6efae2043a4c085a569e
java main/TestClient 127.0.0.1 8000 get 9bba5c53a0545e0c80184b946153c9f58387e3bd1d4ee35740f29ac2e718b019
java main/TestClient 127.0.0.1 8000 get 187897ce0afcf20b50ba2b37dca84a951b7046f29ed5ab94f010619f69d6e189 ' > get.sh

echo -e '#bash
cd ..
java main/TestClient 127.0.0.1 8000 delete 9bba5c53a0545e0c80184b946153c9f58387e3bd1d4ee35740f29ac2e718b019
java main/TestClient 127.0.0.1 8000 delete 590c9f8430c7435807df8ba9a476e3f1295d46ef210f6efae2043a4c085a569e
java main/TestClient 127.0.0.1 8000 delete 9bba5c53a0545e0c80184b946153c9f58387e3bd1d4ee35740f29ac2e718b019
java main/TestClient 127.0.0.1 8000 delete ae448ac86c4e8e4dec645729708ef41873ae79c6dff84eff73360989487f08e5 ' > delete.sh

echo -e '#bash
cd ..
java main/TestClient 127.0.0.1 8000 join
java main/TestClient 127.0.0.2 8000 join
java main/TestClient 127.0.0.3 8000 join
java main/TestClient 127.0.0.4 8000 join ' > join.sh

echo -e '#bash
cd ..
java main/TestClient 127.0.0.1 8000 leave
java main/TestClient 127.0.0.2 8000 leave
java main/TestClient 127.0.0.3 8000 leave
java main/TestClient 127.0.0.4 8000 leave ' > leave.sh

chmod +x put.sh
chmod +x get.sh
chmod +x delete.sh
chmod +x join.sh
chmod +x leave.sh