# A taxi rides analyzer

## Implementation and trade-offs

The application calculates average distances fot taxi trips based on a given time interval.
This project is based on the interval tree implementation.
I use an augmented AVL tree (`AVLIntervalTree`) which also tracks ends of intervals.
The main alternative was the Red-Black tree, but it is a bit slower in search queries (but faster during insertion).
Because, based on requirements, the application should be read-intensive I chose AVL.
This implementation gives us `O(log(N) + K)` search complexity (where `N` is the total number of dataset and `K` is the
number of trips in a search interval).

### Main features:

- We can use different **granularity** for trips by truncating theirs start and end to seconds/minutes/hours/days.
  A search interval is also truncated. This trade-off shouldn't affect results because we are interested in average
  distances. This option can be set by using `-g` flag for `load` command (default value `HOURS`).
  But we can save a lot of memory (see `Performance statistics` section).
- Nodes of the tree store **average statistics** but not the trips. It also helps to same memory.
- Parallel files loading is possible (configured by `--fileThreadsCount` flag for `load` command, default value is 10).

## Run

To launch the application you need to do following steps staying in the project's root directory (`${PROJECT_ROOT}`):

- Download test data: `wget -i files.txt -P ${PROJECT_ROOT}/data`.
- Build the project: `./gradlew clean build`.
- Run the application's jar: `java -jar build/libs/taxi-rides-0.0.1.jar` (Use `java -Xms12g -Xmx12g ...` for small
  granularity).
- Load data: `load ${PROJECT_ROOT}/data` (`-l` flag can be used to limit records per file).
- Query data: `query 2020-01-01T00:00:00 2021-01-01T00:00:00`.
- Execute more queries...
- Exit application: `exit`

## Logs and Metrics

Log files are located in `logs` directory. When the application is stopped (by entering `exit` command),
some key metrics are published to logs.

## Other thoughts

- The application doesn't support updates now. But versioning can be implemented on the subtrees level by copying them (
  not the full tree).
- Parallel range computations can be implemented by using partitions, for example.

## Performance statistics

### Test dataset

- Total trips:  `23'839'125`.
- Files readers pool: `10 threads`.

### Results

| Granularity | Trips      | Heap Used(MB) | Tree Size | Tree Nodes | Queue Size <br/>(mean) | Queue Size<br/>(median) | Query Time<br/>(99 %ile, millis) |
|-------------|------------|---------------|-----------|------------|------------------------|-------------------------|----------------------------------|
| SECONDS     | 1'200'000  | ~455 MB       | ~422 MB   | 1'193'606  | 829.71                 | 974.00                  | 612.52                           |
| MINUTES     | 23'839'125 | ~3,5 GB       | ~3 GB     | 7'929'183  | 417.80                 | 176.00                  | 8691.20                          |
| MINUTES_5   | 23'839'125 | ~659 MB       | ~613 MB   | 959'467    | 373.07                 | 20.00                   | 1343.12                          |
| MINUTES _15 | 23'839'125 | ~190 MB       | ~158 MB   | 200'941    | 360.79                 | 19.00                   | 382.12                           |
| HOURS       | 23'839'125 | ~66 MB        | ~35 MB    | 36'083     | 384.57                 | 19.00                   | 84.24                            |
| DAYS        | 23'839'125 | ~28 MB        | ~7 MB     | 794        | 388.19                 | 21.00                   | 6.09                             |

### Used requests

```
java -Xms16g -Xmx16g -jar build/libs/taxi-rides-0.0.1.jar
```

```
load /Users/astamur.kirillin/git-personal/taxi-rides/data -g SECONDS -l 100000 -m
load /Users/astamur.kirillin/git-personal/taxi-rides/data -g MINUTES -m
load /Users/astamur.kirillin/git-personal/taxi-rides/data -g MINUTES_5 -m
load /Users/astamur.kirillin/git-personal/taxi-rides/data -g MINUTES_15 -m
load /Users/astamur.kirillin/git-personal/taxi-rides/data -g HOURS -m
load /Users/astamur.kirillin/git-personal/taxi-rides/data -g DAYS -m
```

```
query 2020-01-01T00:00:00 2021-01-01T00:00:00 -r 100
```
