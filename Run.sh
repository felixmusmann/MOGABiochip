#!/usr/bin/env bash

# Java compiler settings
classpath="src:lib/gson-2.6.2.jar:lib/jmetal-algorithm-5.0.jar:lib/jmetal-core-5.0.jar:lib/jmetal-exec-5.0.jar:lib/jmetal-problem-5.0.jar"

# Directories and files
inputPath="data/input"
graphsPath="$inputPath/graphs"
libFile="$inputPath/TS_lib.txt"
devicesFile="$inputPath/devices.json"

# Parameters for NSGA-II
iterations=10
population=10
mutationRate=0.7
minWidth=5
minHeight=5

echo "Compiling source files."
if javac -encoding utf8 -classpath ${classpath} src/compilation/*.java src/synthesis/*.java src/synthesis/model/*.java
then
  echo "Compilation successful."
else
  echo "Compilation failed."
  exit
fi

echo "Start synthesis."
for graphFile in ${graphsPath}/*; do
  # Ignore files in ignore folder
  if ${graphFile} != "*graphs/ignore*"
  then
    echo "Processing $graphFile."
    java -classpath ${classpath} synthesis.NSGARunner ${iterations} ${population} ${mutationRate} ${minWidth} ${minHeight} ${graphFile} ${libFile} ${devicesFile}
  else
    echo "Ignoring $graphFile."
  fi
  echo "\n"
done

echo "Ending script."
