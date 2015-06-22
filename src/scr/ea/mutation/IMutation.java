package scr.ea.mutation;

import scr.data.Individual;
import scr.data.Population;

public interface IMutation {

    /**
     * Applies a mutation operation on the given population to generate a new population.
     * 
     * @param population
     *            existing population
     * @return new population
     */
    public Population mutate(Population population);

    /**
     * Applies a mutation operation on the given individual to generate a new individual.
     * 
     * @param individual
     *            existing individual
     * @return new individual
     */
    public Individual mutate(Individual individual);
}
