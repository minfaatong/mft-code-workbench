# MFT Code Workbench project

[![Java CI with Maven](https://github.com/minfaatong/mft-code-workbench/actions/workflows/maven.yml/badge.svg)](https://github.com/minfaatong/mft-code-workbench/actions/workflows/maven.yml)

MFT Code Workbench project, a minimal helper tool I made for myself to clone git projects and properly manage them in git and working folders (as my liking).

My initiative is to automate most of how I work; hence, the tool might not be as helpful to you.

I'll update "quality-of-life" improvement from time to time, feel free to use it if interested, and do not hesitate to submit your changes if you have some great ideas.

## To build

``mvn clean install``

## To run with maven

``mvn clean JavaFX:run``

## Todo 

- make it runnable as a standalone jar
- it's still a bit hard to use, and some manual config is required, settings are place-holder, yet to be implemented.
- there's a housekeeping script which I use to archive older projects from time to time (e.g. more than 6 months), to save some disk space, I plan to add that in too.
- the GUI is quite basic and still not very pleasant to look at, I'll polish that when I have the time.
