package scr.ea.selection;

import java.util.Collections;

import scr.data.Population;

public class ElitistSelection implements ISelection {

    /**
     * Selects a population of individuals based on the most fit members of the parents and offspring.
     *
     * For population size of 1, implements 1+1 selection by picking either the parent or offspring.
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
        selected.addAll(parents);
        selected.addAll(offspring);
        Collections.sort(selected);
        while (selected.size() > parents.size()) {
            selected.remove(selected.size() - 1);
        }
        return selected;
    }
}
