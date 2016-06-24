# Biochip Synthesis with NSGA-II

## Folder structure and imporant files

```
MOGABiochip
+- data
|  +- input          # all necessary input files (module and device libraries)
|  |  +- graphs      # graphs that will be used for nsga
|  |     +- ignore   # graphs that will be ignored
|  +- logs           # log files for each graph and run with execution time and cpu time
|  +- results        # json files with results of all runs for each graph
+- lib               # necessary libs for compiling source code (GSON, JMetal...)
+- src
|  +- compilation
|  +- synthesis
+- Run.sh
```


## How-to Run.sh

The Run.sh script will compile the source code and run the algorithm for every graph in `data/input/graphs`. Graphs in subfolders will be ignored. 
For running the script simply use `nohup sh Run.sh > Run.log 2>&1 &`. This will execute the script in the background and write the output to Run.log.
If you don't want the log file just use `nohup sh Run.sh &`. Without the ampersand the script will be run in the foreground. nohup prevents it from being closed when exiting the shell.

## Class overview

| class | description |
|---|---|
| BiochipCrossover | The crossover operation takes two solutions, splits them randomly and merges them to generate two new biochips. |
| BiochipMutation | The mutation operation alters a given solution with a predefined probability. |
| BiochipSolution | This class extends the Biochip model for the JMetal framework. |
| JSONParser | This class handles all the serialization and deserialization for biochips, devices and application graphs. |
| LogTool | The LogTool is used to save the results and initialize the Logger for information during execution. |
| NSGARunner | This class contains the main method and runs the algorithm. It defines the parameters and puts everything together. |
| Pair | Generic class to create pairs of values. It is used for coordinates. |
| Synthesis | This is a playground to test methods. |
| SynthesisProblem | This class defines the problem the MOGA has to solve. Mainly it is responsible for evaluating the objectives and constraints for each solution as well as creating initial solutions. |
| Biochip | |
| Cell | |
| CellStructure | |
| Device | |
| DeviceCell | |
| DeviceLibrary | |
| Electrode | |
| Shape | |

## File Formats

**The specifications still need some work, cause there are inconsistencies owed to the mix of old and new formats and the current implementation!** 

### Biochip

The biochip is defined by an coordinate system, that starts in the top left corner.

**General**

| member | type | description |
|---|---|---|
| width | int | width of biochip including devices |
| height | int | height of biochip including devices |
| inactiveElectrodes | array | contains arrays with two integers of which each represents the coordinates [x, y] |
| devices | array | contains all device objects |

**Device object**

| member | type | description |
|---|---|---|
| type | string | specifies the type (fluid dependent) |
| id | int | unique identification |
| x | int | x coordinate of upper left corner on biochip |
| y | int | y coordinate of upper left corner on biochip |
| executionTime | int | time in seconds |
| cost | int | cost of the device |
| shape | object | see shape object spec below |

**Shape object**

| member | type | description |
|---|---|---|
| width | int | width of device |
| height | int | height of device |
| startX | int | inner x coordinate where device gets connected to biochip |
| startY | int |inner y coordinate where device gets connected to biochip |

#### Example
```javascript
{
  "width": 10,
  "height": 10,
  "inactiveElectrodes": [[0, 0], [1, 5]],
  "devices": [
    {
      "type": "disS",
      "id": 0,
      "x": 4,
      "y": 4,
      "executionTime": 7,
      "cost": 4,
      "shape": {
        "width": 2,
        "height": 2,
        "startX": 1,
        "startY": 1
      }
    }
  ]
}
```

### Application Graph

The application graph is based on the [BEL JSON Graph Format](http://jsongraphformat.info/).

#### Example

Old format:
```
node 1 O1 MIX mix
node 2 O2 MIX mix
node 3 O3 MIX mix
node 4 O4 MIX mix
node 5 O5 MIX mix
node 6 O6 MIX mix
node 7 O7 IN disB
node 8 O8 DILUTION dilution
node 9 O9 IN disB
node 10 O10 DILUTION dilution
    edge M1 O1 O5
    edge M2 O2 O5
    edge M3 O3 O6
    edge M4 O4 O6
    edge M5 O5 O8
    edge M6 O7 O8
    edge M7 O6 O10
    edge M8 O9 O10
```

Directly converted to new JSON format:
**This still contains a few inconsistencies owed to the current implementation!**
```javascript
{
  "graph": {
    "type": "bioprotocol",
    "directed": true,
    "nodes": [
      {
        "id": "1",
        "label": "O1",
        "type": "mix"
      },
      {
        "id": "2",
        "label": "O2",
        "type": "mix"
      },
      {
        "id": "3",
        "label": "O3",
        "type": "mix"
      },
      {
        "id": "4",
        "label": "O4",
        "type": "mix"
      },
      {
        "id": "5",
        "label": "O5",
        "type": "mix"
      },
      {
        "id": "6",
        "label": "O6",
        "type": "mix"
      },
      {
        "id": "7",
        "label": "O7",
        "type": "in",
        "metadata": {
          "fluid": "disB"
        }
      },
      {
        "id": "8",
        "label": "O8",
        "type": "dilution"
      },
      {
        "id": "9",
        "label": "O9",
        "type": "in",
        "metadata": {
          "fluid": "disB"
        }
      },
      {
        "id": "10",
        "label": "O10",
        "type": "dilution"
      }
    ],
    "edges": [
      {
        "source": "O1",
        "target": "O5",
        "metadata": {
          "label": "M1"
        }
      },
      {
        "source": "O2",
        "target": "O5",
        "metadata": {
          "label": "M2"
        }
      },
      {
        "source": "O3",
        "target": "O6",
        "metadata": {
          "label": "M3"
        }
      },
      {
        "source": "O4",
        "target": "O6",
        "metadata": {
          "label": "M4"
        }
      },
      {
        "source": "O5",
        "target": "O8",
        "metadata": {
          "label": "M5"
        }
      },
      {
        "source": "O7",
        "target": "O8",
        "metadata": {
          "label": "M6"
        }
      },
      {
        "source": "O6",
        "target": "O10",
        "metadata": {
          "label": "M7"
        }
      },
      {
        "source": "O9",
        "target": "O10",
        "metadata": {
          "label": "M8"
        }
      }
    ]
  }
}
```

### Device Library

The device library is used to populate a biochip with required non-reconfigurable devices during initialization (in future for mutation/crossover/repair too).

```javascript
[
  {
    "type": "disB",
    "id": 0,
    "x": 0,
    "y": 0,
    "executionTime": 7,
    "cost": 4,
    "shape": {
      "width": 5,
      "height": 3,
      "startX": 0,
      "startY": 0
    }
  },
  {
    "type": "disS",
    "id": 1,
    "x": 0,
    "y": 0,
    "executionTime": 7,
    "cost": 4,
    "shape": {
      "width": 5,
      "height": 2,
      "startX": 3,
      "startY": 0
    }
  }
]
```

### Module Library

This is a library of predefined reconfigurable modules used in the compilation process.

```javascript
mix 4 7 2
mix 4 6 3
mix 5 5 7
mix 3 5 5
mix 4 4 10
dilution 4 7 4
dilution 4 6 5
dilution 5 5 10
dilution 3 5 7
dilution 4 4 12
store 3 3 -1
opt 1 1 30
```