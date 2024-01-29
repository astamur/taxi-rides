# A taxi rides analyzer
## Implementation and trade-offs
The application calculates average distances fot taxi trips based on a given time interval. 
This project is based on the interval tree implementation. 
I use an augmented AVL tree (`AVLIntervalTree`) which also tracks ends of intervals.
The main alternative was the Red-Black tree, but it is a bit slower in search queries (but faster during insertion).
Because, based on requirements, the application should be read-intensive I chose AVL. 
This implementation gives us `O(log(N) + K)` search complexity (where `N` is the total number of dataset and `K` is the number of trips in a search interval). 

### Main features:
- We can use different **granularity** for trips by truncating theirs start and end to seconds/minutes/hours/days. 
A search interval is also truncated. This trade-off shouldn't affect results because we are interested in average distances.
But we can save a lot of memory (see `Performance statistics` section). 
- Nodes of the tree store **average statistics** but not the trips. It also helps to same memory.


## Run

To launch the application you need to do following steps staying in the project's root directory (`${PROJECT_ROOT}`):

- Download test data: `wget -i files.txt -P ${PROJECT_ROOT}/data`.
- Build the project: `./gradlew clean build`.
- Run the application's jar: `java -jar build/libs/taxi-rides-0.0.1.jar`.
- Load data: `load ${PROJECT_ROOT}/data` (`-l` flag can be used to limit records per file).
- Query data: `query 2020-01-01T00:00:00 2021-01-01T00:00:00`.
- Repeat queries...
- Exit application: `exit`

## Logs and Metrics
Log files are located in `logs` directory. When the application is stopped (by entering `exit` command), 
some key metrics are published to logs.  

## Other thoughts 
- The application doesn't support updates now. But versioning can be implemented on the subtrees level by copying them (not the full tree).
- Parallel range computations can be implemented by using partitions, for example. 

## Performance statistics
