### Intro
Hello My name is Manny, I am a software engineer. I am very interested in distributed computing     

### Motivation
This project was done for Towson University Cosc 880 - Graduate Project     

Quote from Towson University Computer Science Website "A graduate project (COSC880) serves the purpose of providing applied skills to the student. That means, graduate project should be focused on implementation and learning skills for the student. The graduate project can also be based on survey of current topics in a given field of study and write a paper for publication. Graduate faculty plays a role of training the student to write a proposal, analyze a problem, collect requirements for a problem, design, implement, test, and demonstrate a chosen problem."

### Software Parts
The naming of the sub-projects was inspired by spanish cities names
#### Lugo
Groovy Distributed Task Management    

![alt tag](https://raw.githubusercontent.com/mcomp2010/barium/master/concept1.png)

##### Task Framework Overview
 * Ability to use the processing power of many CPU cores over many machines
 * The framework is generic so it can be used for other processes that require the use of many machine for Tasks. 
 * Tasks are based on using Interface which allows you to put your own implementation code
 * Minimum Configuration (currently one config file for connections info)
 * Uses message passing between processes
 * You extend TaskOwnerBase Abstract class to make your own TaskOwner to handle the Tasks 

#### Terminonlogy
 * TaskOwner- Task generator and responsible for task results
 * Tasks- executable units that are executed concurrently on a single or more workers using multiprocessing
     * Tasks are execute asynchronously in the background and notify TaskOwner when completed
 * Worker- responsible for executing tasks from queue and sending results to TaskOwner
 * Messaging System- the way multiple framework processes communicate each other 
     * Uses Hazelcast for communication 


#### Aragon
Front-end for **barium** created in nodeJs and AngularJs    
This is 

### Development Setup
This is a guide how to setup nodeJs,git with Yeoman    
http://yeoman.io/codelab/setup.html    

for Node Dependency     
https://nodejs.org/en/    

for Git Dependency    
https://git-scm.com/    

