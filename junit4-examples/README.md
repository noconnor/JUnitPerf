### Running instructions:

To run examples using maven:

* From project root directory run: `mvn clean install -Dgpg.skip`
* From `junit4-examples` directory run: `mvn clean test -DskipTests=false`

**NOTE:** The example tests contain some example failure scenarios, so you should expect `mvn test` command to fail when running these examples. 
