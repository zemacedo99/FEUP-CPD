#bash

echo -e '#bash\ncd ..\njava main/Store 224.0.0.1 6789 127.0.0.1 8000 ' > store1.sh
echo -e '#bash\ncd ..\njava main/Store 224.0.0.1 6789 127.0.0.2 8000 ' > store2.sh
echo -e '#bash\ncd ..\njava main/Store 224.0.0.1 6789 127.0.0.3 8000 ' > store3.sh
echo -e '#bash\ncd ..\njava main/Store 224.0.0.1 6789 127.0.0.4 8000 ' > store4.sh
chmod +x store1.sh
chmod +x store2.sh
chmod +x store3.sh
chmod +x store4.sh



#gnome-terminal -x ./store1.sh
#xterm -e ./store1.sh
