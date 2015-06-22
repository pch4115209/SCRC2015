package scr.ea.crossover;

import scr.data.Population;

public interface ICrossover {

    /**
     * Applies a crossover operation to the given population to generate a new population.
     * 
     * @param population
     *            existing population
     * @param rate
     *            crossover rate
     * @return new population
     */
    public Population crossover(Population population, int rate);
}
