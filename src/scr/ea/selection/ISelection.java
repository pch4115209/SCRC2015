package scr.ea.selection;

import scr.data.Population;

public interface ISelection {

    /**
     * Selects a population of individuals given a population of parents and a population of offspring.
     * 
     * @param parents
     *            parent population
     * @param offspring
     *            offspring population
     * @return selected population
     */
    public Population select(Population parents, Population offspring);
}
