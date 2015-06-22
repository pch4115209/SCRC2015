package scr.ea.crossover;

import java.util.Random;

import scr.data.Individual;
import scr.data.Population;

public class LocalDiscreteCrossover implements ICrossover {

    @Override
    public Population crossover(Population population, int rate) {
        Random generator = new Random();
        Population offsprings = new Population(); // contains the new offspring generation
        Individual parent1 = null, parent2 = null, offspringIndividual = new Individual();
        float newMaxSpeedDist, newMaxSpeed, newAbsSlip, newAbsRange, newAbsWindSpeed, newApproachPosition, newApproachSensitivity, newApproachCorrection;
        int sample;

        /** loop the # times of population size **/
        for (int i = 0; i < population.size(); i++) {
            parent1 = population.get(generator.nextInt(population.size()));// randomly select the individual
            parent2 = population.get(generator.nextInt(population.size()));// from the population

            if (generator.nextInt(100) < rate) {

                /*** regenerate new gearUp array ***/
                int[] tempGear1 = parent1.getGearUp();
                int[] tempGear2 = parent2.getGearUp();
                sample = generator.nextInt(2);// generate 0 or 1
                if (sample == 0)
                    offspringIndividual.setGearUp(tempGear1);
                else
                    offspringIndividual.setGearUp(tempGear2);

                /*** regenerate new gearDown array ***/
                tempGear1 = parent1.getGearDown();
                tempGear2 = parent2.getGearDown();
                sample = generator.nextInt(2);// generate 0 or 1
                if (sample == 0)
                    offspringIndividual.setGearDown(tempGear1);
                else
                    offspringIndividual.setGearDown(tempGear2);

                /*** regenerate other paramenters ***/
                sample = generator.nextInt(2);
                newMaxSpeedDist = (sample == 0) ? parent1.getMaxSpeedDist() : parent2.getMaxSpeedDist();

                sample = generator.nextInt(2);
                newMaxSpeed = (sample == 0) ? parent1.getMaxSpeed() : parent2.getMaxSpeed();

                // sample = generaotor.nextInt(2);
                // newAbsSlip = ( sample == 0 ) ? parent1.getAbsSlip() : parent2.getAbsSlip();

                // sample = generaotor.nextInt(2);
                // newAbsRange = (sample == 0 )? parent1.getAbsRange() : parent2.getAbsRange();

                // sample = generaotor.nextInt(2);
                // newAbsWindSpeed = ( sample == 0 ) ? parent1.getAbsMinSpeed() : parent2.getAbsMinSpeed();

                sample = generator.nextInt(2);
                newApproachCorrection = (sample == 0) ? parent1.getApproachCorrection() : parent2.getApproachCorrection();

                sample = generator.nextInt(2);
                newApproachPosition = (sample == 0) ? parent1.getApproachPosition() : parent2.getApproachPosition();

                sample = generator.nextInt(2);
                newApproachSensitivity = (sample == 0) ? parent1.getApproachSensitivity() : parent2.getApproachSensitivity();

                /*** Resetting all the selected parameters ***/
                offspringIndividual.setMaxSpeedDist(newMaxSpeedDist);
                offspringIndividual.setMaxSpeed(newMaxSpeed);
                // offspringIndividual.setAbsSlip(newAbsSlip);
                // offspringIndividual.setAbsRange(newAbsRange);
                // offspringIndividual.setAbsMinSpeed(newAbsWindSpeed);
                offspringIndividual.setApproachCorrection(newApproachCorrection);
                offspringIndividual.setApproachPosition(newApproachPosition);
                offspringIndividual.setApproachSensitivity(newApproachSensitivity);

                /*** add the new individual to new population ***/
                offsprings.add(offspringIndividual);
            }
            else {

                offsprings.add(population.get(i));
            }
        }

        return offsprings;

    }
}
