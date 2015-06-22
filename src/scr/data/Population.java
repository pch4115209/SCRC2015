package scr.data;

import java.util.ArrayList;

public class Population extends ArrayList<Individual> {

    private static final long serialVersionUID = 1L;

    public Population() {
        super();
    }

    /**
     * Returns the best individual in the population (the one with the lowest fitness value).
     * 
     * @return best individual
     */
    public Individual getBestIndividual() {
        Individual bestIndividual = null;
        Double bestFitness = null;
        for (int i = 0; i < size(); i++) {
            if (bestFitness == null || get(i).getFitness() < bestFitness) {
                bestIndividual = get(i);
                bestFitness = get(i).getFitness();
            }
        }
        return bestIndividual;
    }
}
