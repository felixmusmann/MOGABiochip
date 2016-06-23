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

echo "######################"
echo "Configuration"
echo "######################"
echo "graphsPath: $graphsPath"
echo "libFile: $libFile"
echo "devicesFile: $devicesFile"
echo "iterations: $iterations"
echo "population: $population"
echo "mutationRate: $mutationRate"
echo "minWidth: $minWidth"
echo "minHeight: $minHeight"

echo ""
echo "######################"
echo "Compiling source files."
echo "######################"
if javac -encoding utf8 -classpath ${classpath} src/compilation/*.java src/synthesis/*.java src/synthesis/model/*.java
then
  echo "Compilation successful."
else
  echo "Compilation failed."
  exit
fi

echo ""
echo "######################"
echo "Start synthesis."
echo "######################"
for graphFile in ${graphsPath}/*.txt; do
  echo "Processing $graphFile."
  java -classpath ${classpath} synthesis.NSGARunner ${iterations} ${population} ${mutationRate} ${minWidth} ${minHeight} ${graphFile} ${libFile} ${devicesFile}
done

echo "Ending script."
