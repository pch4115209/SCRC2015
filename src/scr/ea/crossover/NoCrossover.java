package scr.ea.crossover;

import scr.data.Population;

public class NoCrossover implements ICrossover {

    /**
     * Returns the input population without applying any crossover operations.
     * 
     * @param population
     *            existing population
     * @param rate
     *            crossover rate
     * @return the same population
     */
    @Override
    public Population crossover(Population population, int rate) {
        return population;
    }
}
