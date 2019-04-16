# BPFilter
##### 16/4/19

aResourceName = "driving_car_single_bt.js";<br>
evolutionResolution = 3;<br>
fitnessNumOfIterations = 10;<br>
numOfLanes = 4;<br>
laneLength = 9;<br>
malfunctionProbability = 0.2;<br>
malfunctionWindow=4;<br>
<br>

**Reality Based Transition**

accuracy to mutationProbability\populationSize<br>

|   | 0.1 | 0.2 | 0.3 |
|:---:|:---:|:---:|:---:|
|  5 |   0.2  |  1   |   0  |
| 10 |   0.82  |   1  |  0.3   |
| 15 |   0.1  |   0.09  |   0.9  |

bt accuracy to mutationProbability\populationSize<br>

|   | 0.1 | 0.2 | 0.3 |
|:---:|:---:|:---:|:---:|
|  5 |   0.74  |   1  |   0.67  |
| 10 |   0.95  |   1  |   0.57  |
| 15 |   0.74  |  0.7   |   0.97  |

**Simulation Based Transition**

accuracy to mutationProbability\populationSize<br>

|   | 0.1 | 0.2 | 0.3 |
|:---:|:---:|:---:|:---:|
|  5 |  0  |  0  | 0 |
| 10 |  0   | 0.09 |  0  |
| 15 |  0.45  |  0  |  0.1  |

bt accuracy to mutationProbability\populationSize<br>

|   | 0.1 | 0.2 | 0.3 |
|:---:|:---:|:---:|:---:|
|  5 |   0.07  | 0.16 |  0.24 |
| 10 |   0.15  | 0.25 |  0.14 |
| 15 |  0.64  |  0.125  |  0.47 |

**RealityFirst Based Transition**

accuracy to mutationProbability\populationSize<br>

|   | 0.1 | 0.2 | 0.3 |
|:---:|:---:|:---:|:---:|
|  5 |  0.81  | 0.3 | 0.91 |
| 10 |   0.73  | 0.9 |  0.2  |
| 15 |  0.3  | 0.1 |  0.6  |

bt accuracy to mutationProbability\populationSize<br>

|   | 0.1 | 0.2 | 0.3 |
|:---:|:---:|:---:|:---:|
|  5 |   0.95  | 0.59  |  0.97 |
| 10 |   0.92  |  0.97  |   0.4  |
| 15 |  0.67  | 0.58   |   0.9  |

