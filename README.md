# A taxi rides analyzer

-Djol.magicFieldOffset=true

### Tasks:
- Implement a parquet files reader;
- Implement an interval tree;
- Cover files parsing with tests;
- Cover tree operations with tests:
  - population;
  - range queries;
- Add comparisons statistics (memory consumption, processing time) for:
  - trees with different granularity ();
  - linear approach comparison.

### Questions
- Parallel population?
- Online algorithm?
- Locking and transactions?
- Parallel range computations?

# Tree's memory consumption. 1'000'000 rides
### Granularity: 1 second. Node's value: a list of rides.  
```
   COUNT       AVG       SUM   DESCRIPTION
  995801        32  31865632   dev.astamur.taxirides.model.tree.BaseInterval
 1000000        24  24000000   dev.astamur.taxirides.model.Ride
       1        16        16   dev.astamur.taxirides.tree.AVLIntervalTree
  995801        40  39832040   dev.astamur.taxirides.tree.AVLIntervalTree$Node
  995801        32  31865632   java.util.LinkedList
 1000000        24  24000000   java.util.LinkedList$Node
 4987404           151563320   (total)
```

### Granularity: 1 minute. Node's value: a list of rides.
```
   COUNT       AVG       SUM   DESCRIPTION
      39        32      1248   dev.astamur.taxirides.model.tree.BaseInterval
 1000000        24  24000000   dev.astamur.taxirides.model.Ride
       1        16        16   dev.astamur.taxirides.tree.AVLIntervalTree
      39        40      1560   dev.astamur.taxirides.tree.AVLIntervalTree$Node
      39        32      1248   java.util.LinkedList
 1000000        24  24000000   java.util.LinkedList$Node
 2000118            48004072   (total)
 
    COUNT       AVG       SUM   DESCRIPTION
 1000000        24  24000000   dev.astamur.taxirides.model.Ride
       1        16        16   dev.astamur.taxirides.tree.AVLIntervalTree
      39        56      2184   dev.astamur.taxirides.tree.AVLIntervalTree$Node
      39        32      1248   java.util.LinkedList
 1000000        24  24000000   java.util.LinkedList$Node
 2000079            48003448   (total)
```

### Granularity: 1 hour. Node's value: a list of rides.
```
   COUNT       AVG       SUM   DESCRIPTION
       5        32       160   dev.astamur.taxirides.model.tree.BaseInterval
 1000000        24  24000000   dev.astamur.taxirides.model.Ride
       1        16        16   dev.astamur.taxirides.tree.AVLIntervalTree
       5        40       200   dev.astamur.taxirides.tree.AVLIntervalTree$Node
       5        32       160   java.util.LinkedList
 1000000        24  24000000   java.util.LinkedList$Node
 2000016            48000536   (total)
```