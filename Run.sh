#!/usr/bin/env bash

inputPath="data/input"
graphsPath="$inputPath/graphs"
libFile="$inputPath/TS_lib.txt"
devicesFile="$inputPath/devices.json"

iterations=10
population=10
mutationRate=0.7
minWidth=5
minHeight=5

echo "Compiling source files."
if [ javac -encoding utf8 -classpath lib/gson-2.6.2.jar:lib/jmetal-algorithm-5.0.jar:lib/jmetal-core-5.0.jar:lib/jmetal-exec-5.0.jar:lib/jmetal-problem-5.0.jar src/compilation/*.java src/synthesis/*.java src/synthesis/model/*.java ]
then
  echo "Compilation successful."
else
  echo "Compilation failed."
  exit
fi

echo "Start synthesis."
for graph in ${graphsPath}/*; do
  echo ${graph}
  java -classpath src:lib/gson-2.6.2.jar:lib/jmetal-algorithm-5.0.jar:lib/jmetal-core-5.0.jar:lib/jmetal-exec-5.0.jar:lib/jmetal-problem-5.0.jar synthesis.NSGARunner ${iterations} ${population} ${mutationRate} ${minWidth} ${minHeight} ${graph} ${libFile} ${devicesFile}
done

echo "Ending script."
