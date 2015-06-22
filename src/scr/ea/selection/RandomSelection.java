package scr.ea.selection;

import java.util.Random;

import scr.data.Individual;
import scr.data.Population;

public class RandomSelection implements ISelection {

    /**
     * Randomly selects a population of individuals from the parents and offspring.
     * 
     * @param parents
     *            parent population
     * @param offspring
     *            offspring population
     * @return selected population
     */
    @Override
    public Population select(Population parents, Population offspring) {
        Population selected = new Population();
        Random random = new Random();
        for (int i = 0; i < parents.size(); i++) {
            boolean isParents = random.nextBoolean();
            if (isParents) {
                int randomIndex = random.nextInt(parents.size());
                Individual individual = new Individual(parents.get(randomIndex));
                selected.add(individual);
            }
            else {
                int randomIndex = random.nextInt(offspring.size());
                Individual individual = new Individual(offspring.get(randomIndex));
                selected.add(individual);
            }
        }
        return selected;
    }
}
