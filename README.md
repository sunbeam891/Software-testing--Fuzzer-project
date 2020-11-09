Copyright 2020 Shubham Parth, University of Melbourne

This Repository is not to be copied or used anywhere without permission from the owner. The code should not be submitted as a project anywhere.

Structure of this repository:

* src/original/: -  where the code for the original application lives
* src/vuln-1 -- src/vuln-5 - where your vulnerable versions will live
* poc/:        -  where your PoCs will live
* fuzzer/:     -  where your fuzzer will live
* bin/:        -  where your compiled programs will live
* tests/:      -  where your generated tests will live

Pre-Included Scripts:

* Makefile         - makefile for building the C implementation etc.
* get_coverage.sh  - script to generate coverage reports
* run_fuzzer.sh    - script for running your fuzzer to generate inputs 
* run_tests.sh     - script for running your generated tests against compiled programs 

Vulnerable Versions (you should put your security vulnerabilities in here):

* src/vuln-1/dc.c -- src/vuln-5/dc.c

Proofs of Concept (PoCs that you should provide for each vulnerability):

* poc/vuln-1.poc -- poc/vuln-5.poc

