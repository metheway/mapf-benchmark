# mapf-benchmark
A benchmarking tool for researchers working on multi-agent path finding.

### Currently implemented algorithms:
  * [Conflict-based search](https://www.aaai.org/ocs/index.php/AAAI/AAAI12/paper/view/5062/) (Sharon, et al.)
  * [Independence Detection](http://www.aaai.org/ocs/index.php/AAAI/AAAI10/paper/view/1926) (Standley)
  * Cooperative A*
  * Multi-agent A*
  
### Dependencies
#### Java
Requires Java 8.

#### Sat4
Sat4j is used to solve SAT problems for the SAT approach to mapf. 
Versioning: Sat4j Core Version 2.3.5 and Sat4j Sat Version 2.3.4
Three JARs are included in the `external_libraries` directory. 
Two are compiled sources of Core and Sat and one is the source of Core.
These JARs should be added as dependencies when compiling.

#### Internal Infrastructure
All of the lower-level infrastructure can be found in the `utilities` package.

### Disclaimer
This project is in progress. Visualization of paths and maps will be provided soon.
