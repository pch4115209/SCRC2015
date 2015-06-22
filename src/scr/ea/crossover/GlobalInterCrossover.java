package scr.ea.crossover;

import java.util.Random;

import scr.data.Individual;
import scr.data.Population;

public class GlobalInterCrossover implements ICrossover {

    public Population crossover(Population population, int rate) {
        Random generator = new Random(); // use to select the
        Population offsprings = new Population(); // contains the new offspring generation
        Individual parent1 = null, parent2 = null, offspringIndividual = null;
        int[] newGearUp = new int[new Individual().getGearUp().length], newGearDown = new int[new Individual().getGearDown().length];
        float newMaxSpeedDist, newMaxSpeed, newAbsSlip, newAbsRange, newAbsWindSpeed, newApproachPosition, newApproachSensitivity, newApproachCorrection;

        /** loop the # times of population size **/
        for (int i = 0; i < population.size(); i++) {
            if (generator.nextInt(100) < rate) {

                /*** regenerate new gearUp array ***/
                parent1 = population.get(generator.nextInt(population.size()));// randomly select the individual
                parent2 = population.get(generator.nextInt(population.size()));// from the population
                int[] tempGear1 = parent1.getGearUp();
                int[] tempGear2 = parent2.getGearUp();
                for (int j = 0; j < tempGear1.length; j++)
                    newGearUp[j] = (tempGear1[j] + tempGear2[j]) / 2;

                /*** regenerate new gearDown array ***/
                parent1 = population.get(generator.nextInt(population.size()));// randomly select the individual
                parent2 = population.get(generator.nextInt(population.size()));// from the population
                tempGear1 = parent1.getGearDown();
                tempGear2 = parent2.getGearDown();
                for (int j = 0; j < tempGear2.length; j++)
                    newGearDown[j] = (tempGear1[j] + tempGear2[j]) / 2;

                /*** regenerate other paramenters ***/
                parent1 = population.get(generator.nextInt(population.size()));// randomly select the individual
                parent2 = population.get(generator.nextInt(population.size()));// from the population
                newMaxSpeedDist = (parent1.getMaxSpeedDist() + parent2.getMaxSpeedDist()) / 2;

                parent1 = population.get(generator.nextInt(population.size()));// randomly select the individual
                parent2 = population.get(generator.nextInt(population.size()));// from the population
                newMaxSpeed = (parent1.getMaxSpeed() + parent2.getMaxSpeed()) / 2;

                parent1 = population.get(generator.nextInt(population.size()));// randomly select the individual
                parent2 = population.get(generator.nextInt(population.size()));// from the population
                newApproachCorrection = (parent1.getApproachCorrection() + parent2.getApproachCorrection()) / 2;

                parent1 = population.get(generator.nextInt(population.size()));// randomly select the individual
                parent2 = population.get(generator.nextInt(population.size()));// from the population
                newApproachPosition = (parent1.getApproachPosition() + parent2.getApproachPosition()) / 2;

                parent1 = population.get(generator.nextInt(population.size()));// randomly select the individual
                parent2 = population.get(generator.nextInt(population.size()));// from the population
                newApproachSensitivity = (parent1.getApproachSensitivity() + parent2.getApproachSensitivity()) / 2;

                /*
                 * parent1 = population.get( generaotor.nextInt(population.size()) );//randomly select the individual
                 * parent2 = population.get( generaotor.nextInt(population.size()) );//from the population newAbsSlip =
                 * (parent1.getAbsSlip() + parent2.getAbsSlip()) / 2;
                 * 
                 * parent1 = population.get( generaotor.nextInt(population.size()) );//randomly select the individual
                 * parent2 = population.get( generaotor.nextInt(population.size()) );//from the population newAbsRange =
                 * (parent1.getAbsRange() + parent2.getAbsRange()) / 2;
                 * 
                 * parent1 = population.get( generaotor.nextInt(population.size()) );//randomly select the individual
                 * parent2 = population.get( generaotor.nextInt(population.size()) );//from the population
                 * newAbsWindSpeed = (parent1.getAbsMinSpeed() + parent2.getAbsMinSpeed()) / 2;
                 */

                /*** Resetting all the selected parameters ***/
                offspringIndividual = new Individual();
                offspringIndividual.setGearUp(newGearUp);
                offspringIndividual.setGearDown(newGearDown);
                offspringIndividual.setMaxSpeedDist(newMaxSpeedDist);
                offspringIndividual.setMaxSpeed(newMaxSpeed);
                offspringIndividual.setApproachCorrection(newApproachCorrection);
                offspringIndividual.setApproachPosition(newApproachPosition);
                offspringIndividual.setApproachSensitivity(newApproachSensitivity);
                // offspringIndividual.setAbsSlip(newAbsSlip);
                // offspringIndividual.setAbsRange(newAbsRange);
                // offspringIndividual.setAbsMinSpeed(newAbsWindSpeed);

                /*** add the new individual to new population ***/
                offsprings.add(offspringIndividual);
            }
            else {

                offsprings.add(population.get(i));
            }
        }// end of for loop

        return offsprings;
    }
}
