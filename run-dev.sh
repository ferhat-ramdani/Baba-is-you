#!/bin/bash
echo "Compiling source code..."
ant compile
if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi
echo "Launching game..."
java -cp "classes:lib/*" baba.engine.Main "$@"
