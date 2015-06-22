______________________________________________________

  INSTRUCTIONS
______________________________________________________

Our car racing software can be run using the "Main" class in the "scr" package. Prior to submitting our driver for the competition, the driver is trained using offline learning. Then during the warm-up stage, the driver is trained further using online learning, based on the specific track that is encountered. The steps for running both types of learning are shown below.

__________________

 Offline Learning
__________________

1. Build the JAR file.

javac -d bin src/scr/*.java src/scr/data/*.java src/scr/ea/*.java src/scr/ea/crossover/*.java src/scr/ea/mutation/*.java src/scr/ea/selection/*.java src/scr/net/*.java
jar cfe group_3_driver.jar scr.Main -C bin scr drivers


2. Modify "torcs_directory.txt" to point to your TORCS installation folder.


3. Copy all XML configuration files from "/config/tracks/" in the assignment folder, to "/config/raceman/" in the TORCS intallation folder.


4. Run offline learning.

java -jar group_3_driver.jar port:3001 stage:-1 track:forza > results_forza.txt

__________________

 Online Learning
__________________

1. Build the JAR file.

javac -d bin src/scr/*.java src/scr/data/*.java src/scr/ea/*.java src/scr/ea/crossover/*.java src/scr/ea/mutation/*.java src/scr/ea/selection/*.java src/scr/net/*.java
jar cfe group_3_driver.jar scr.Main -C bin scr drivers


2. Run the driver in stage 0 of the competition (make sure that damage is disabled for this stage).

java -jar group_3_driver.jar port:3001 track:forza stage:0 maxSteps:100000


3. Run the driver in stage 1 of the competition.

java -jar group_3_driver.jar port:3001 track:forza stage:1 maxSteps:10000


4. Run the driver in stage 2 of the competition.

java -jar group_3_driver.jar port:3001 track:forza stage:2

______________________________________________________

  GROUP MEMBERS
______________________________________________________

Slava Shekh
a1649566
slava.shekh@student.adelaide.edu.au

Henry Stevens
a1194229
a1194229@student.adelaide.edu.au

Viet Tuong Khanh Nguyen
a1208362
v.nguyen@student.adelaide.edu.au

Chenghao Pan
a1215247
a1215247@student.adelaide.edu.au