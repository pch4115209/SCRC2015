package scr.ea.mutation;

import java.util.Random;

import scr.data.Individual;
import scr.data.Population;

public class NSigmaMutation implements IMutation {

    public Individual mutate(Individual individual) {
        Individual inClone = new Individual(individual);
        double[] chro = inClone.getNSigmaArray();
        Random generator = new Random();
        double threshold = inClone.getNSigmaRateThreshold();

        // calculate new sigma for each value
        // new sig = old sig * exp(t*Norm(0,1) + t2*Norm(0,1)
        for (int i = chro.length / 2; i < chro.length; i++) {
            double t = 1.0 / Math.sqrt(chro.length);
            double t2 = 1.0 / Math.sqrt(Math.sqrt(chro.length));
            chro[i] = chro[i] * Math.exp(t * generator.nextGaussian() + t2 * generator.nextGaussian());
            if (chro[i] < threshold) {
                chro[i] = threshold;
            }
        }

        for (int i = 0; i < 12; i++) {
            double derivation = chro[i + 17] * chro[i];
            // x' = x + sig'i * N (0,1)
            int newValue = (int) (generator.nextGaussian() * derivation + chro[i]);
            if (newValue > 0) {
                chro[i] = newValue;
            }
        }
        for (int i = 12; i < chro.length / 2; i++) {
            double derivation = chro[i + 17] * chro[i];
            double newValue = (generator.nextGaussian() * derivation + chro[i]);
            if (newValue > 0) {
                chro[i] = newValue;
            }
        }
        inClone.setNSigmaArray(chro);

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
