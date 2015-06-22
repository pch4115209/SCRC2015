package scr.ea.mutation;

import java.util.Random;

import scr.data.Individual;
import scr.data.Population;

public class OneSigmaMutation implements IMutation {

    public Individual mutate(Individual individual) {
        Individual inClone = new Individual(individual);
        double[] chro = inClone.getOneSigmaArray();
        // sigma value is the last element of the array
        double sigma = (float) chro[chro.length - 1];

        Random generator = new Random();
        // threshold
        double threshold = inClone.getOneSigmaRateThreshold();
        // new sigma = old sigma * exp(T * Norm(0,1))
        // T (learning Rate) = 1/(n^0.5)
        sigma = sigma * Math.exp((1.0 / Math.sqrt(2 * (chro.length - 1))) * generator.nextGaussian());
        // boundary rules:
        // if the value gets below the threshold, it is = threshold
        if (sigma < threshold) {
            sigma = threshold;
        }
        // rpm tuning for gearup and geardown
        for (int i = 0; i < 12; i++) {
            double derivation = sigma * chro[i];
            // new value = old val + new sigma * Norm(0,1)
            int newValue = (int) (generator.nextGaussian() * derivation + chro[i]);
            if (newValue > 0) {
                chro[i] = newValue;
            }
        }
        // abs, approach
        for (int i = 12; i < chro.length - 1; i++) {
            double derivation = sigma * chro[i];
            double newValue = (generator.nextGaussian() * derivation + chro[i]);
            if (newValue > 0) {
                chro[i] = newValue;
            }
        }
        chro[chro.length - 1] = sigma;
        inClone.setOneSigmaArray(chro);

        return inClone;
    }

    @Override
    public Population mutate(Population population) {
        Population offspring = new Population();
        for (int i = 0; i < population.size(); i++) {
            offspring.add(mutate(population.get(i)));
        }
        return offspring;
    }
}
