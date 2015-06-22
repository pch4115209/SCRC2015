package scr.ea.crossover;

import java.util.Random;

import scr.data.Individual;
import scr.data.Population;

public class GlobalDiscreteCrossover implements ICrossover {

    @Override
    public Population crossover(Population population, int rate) {
        Random generator = new Random();
        Population offspring = new Population(); // contains the new offspring generation
        Individual parent1 = null, parent2 = null, offspringIndividual = new Individual();
        float newMaxSpeedDist, newMaxSpeed, newAbsSlip, newAbsRange, newAbsWindSpeed, newApproachPosition, newApproachSensitivity, newApproachCorrection;
        int sample;

        /** loop the # times of population size **/
        for (int i = 0; i < population.size(); i++) {

            if (generator.nextInt(100) < rate) {

                /*** regenerate new gearUp array ***/
                parent1 = population.get(generator.nextInt(population.size()));// randomly select the individual
                parent2 = population.get(generator.nextInt(population.size()));// from the population
                int[] tempGear1 = parent1.getGearUp();
                int[] tempGear2 = parent2.getGearUp();
                sample = generator.nextInt(2);// generate 0 or 1
                if (sample == 0)
                    offspringIndividual.setGearUp(tempGear1);
                else
                    offspringIndividual.setGearUp(tempGear2);

                /*** regenerate new gearDown array ***/
                parent1 = population.get(generator.nextInt(population.size()));// randomly select the individual
                parent2 = population.get(generator.nextInt(population.size()));// from the population
                tempGear1 = parent1.getGearDown();
                tempGear2 = parent2.getGearDown();
                sample = generator.nextInt(2);// generate 0 or 1
                if (sample == 0)
                    offspringIndividual.setGearDown(tempGear1);
                else
                    offspringIndividual.setGearDown(tempGear2);

                /*** regenerate other paramenters ***/
                parent1 = population.get(generator.nextInt(population.size()));// randomly select the individual
                parent2 = population.get(generator.nextInt(population.size()));// from the population
                sample = generator.nextInt(2);
                newMaxSpeedDist = (sample == 0) ? parent1.getMaxSpeedDist() : parent2.getMaxSpeedDist();

                parent1 = population.get(generator.nextInt(population.size()));// randomly select the individual
                parent2 = population.get(generator.nextInt(population.size()));// from the population
                sample = generator.nextInt(2);
                newMaxSpeed = (sample == 0) ? parent1.getMaxSpeed() : parent2.getMaxSpeed();

                // parent1 = population.get( generaotor.nextInt(population.size()) );//randomly select the individual
                // parent2 = population.get( generaotor.nextInt(population.size()) );//from the population
                // sample = generaotor.nextInt(2);
                // newAbsSlip = ( sample == 0 ) ? parent1.getAbsSlip() : parent2.getAbsSlip();
                //
                // parent1 = population.get( generaotor.nextInt(population.size()) );//randomly select the individual
                // parent2 = population.get( generaotor.nextInt(population.size()) );//from the population
                // sample = generaotor.nextInt(2);
                // newAbsRange = (sample == 0 )? parent1.getAbsRange() : parent2.getAbsRange();
                //
                // parent1 = population.get( generaotor.nextInt(population.size()) );//randomly select the individual
                // parent2 = population.get( generaotor.nextInt(population.size()) );//from the population
                // sample = generaotor.nextInt(2);
                // newAbsWindSpeed = ( sample == 0 ) ? parent1.getAbsMinSpeed() : parent2.getAbsMinSpeed();

                parent1 = population.get(generator.nextInt(population.size()));// randomly select the individual
                parent2 = population.get(generator.nextInt(population.size()));// from the population
                sample = generator.nextInt(2);
                newApproachPosition = (sample == 0) ? parent1.getApproachPosition() : parent2.getApproachPosition();

                parent1 = population.get(generator.nextInt(population.size()));// randomly select the individual
                parent2 = population.get(generator.nextInt(population.size()));// from the population
                sample = generator.nextInt(2);
                newApproachSensitivity = (sample == 0) ? parent1.getApproachSensitivity() : parent2.getApproachSensitivity();

                parent1 = population.get(generator.nextInt(population.size()));// randomly select the individual
                parent2 = population.get(generator.nextInt(population.size()));// from the population
                sample = generator.nextInt(2);
                newApproachCorrection = (sample == 0) ? parent1.getApproachCorrection() : parent2.getApproachCorrection();

                /*** Resetting all the selected parameters ***/
                offspringIndividual.setMaxSpeedDist(newMaxSpeedDist);
                offspringIndividual.setMaxSpeed(newMaxSpeed);
                // offspringIndividual.setAbsSlip(newAbsSlip);
                // offspringIndividual.setAbsRange(newAbsRange);
                // offspringIndividual.setAbsMinSpeed(newAbsWindSpeed);
                offspringIndividual.setApproachPosition(newApproachPosition);
                offspringIndividual.setApproachSensitivity(newApproachSensitivity);
                offspringIndividual.setApproachCorrection(newApproachCorrection);

                /*** add the new individual to new population ***/
                offspring.add(offspringIndividual);
            }
            else {
                offspring.add(population.get(i));
            }
        }

        return offspring;
    }
}
