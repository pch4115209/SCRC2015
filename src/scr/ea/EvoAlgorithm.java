package scr.ea;

import scr.data.Config;
import scr.data.Individual;
import scr.data.Population;
import scr.ea.crossover.ICrossover;
import scr.ea.crossover.NoCrossover;
import scr.ea.mutation.IMutation;
import scr.ea.mutation.NSigmaMutation;
import scr.ea.selection.ElitistSelection;
import scr.ea.selection.ISelection;
import scr.net.Client;

public class EvoAlgorithm {

    private final static int CROSSOVER_RATE = 10; // rate at which crossover occurs (1-100%)
    private final static int MAX_GENERATIONS = 50;

    private int popSize = 10; // population size
    private int offSize = 10; // offspring size
    private ISelection selection = new ElitistSelection(); // selection operator to use
    private IMutation mutation = new NSigmaMutation(); // mutation operator to use
    private ICrossover crossover = new NoCrossover(); // crossover operator to use

    private Client client; // the client used to evaluate the fitness of an individual

    public EvoAlgorithm(Config config) {
        client = new Client(config);
        System.out.println("Track\t"+config.getTrackName());
    }

    /**
     * Runs this evolutionary algorithm using the given client.
     * 
     * @param client
     *            client
     */
    public void run() {
        System.out.println("Generation" + "\t" + "Average Lap Time");

        // Generate the initial population
        Population parents = new Population();
        Individual initialIndividual = new Individual();
        double initialFitness = client.evaluateFitness(initialIndividual);
        for (int i = 0; i < popSize; i++) {
            Individual individual = new Individual();
            individual.setFitness(initialFitness);
            parents.add(individual);
        }
        System.out.println("0" + "\t" + parents.getBestIndividual().getFitness());

        // Run the EA for the required number of generations
        for (int i = 1; i <= MAX_GENERATIONS; i++) {
            parents = newGeneration(parents, client);
            System.out.println(i + "\t" + parents.getBestIndividual().getFitness());

            if ((i % 5) == 0) {
                System.out.println();
                System.out.println(parents.getBestIndividual().toString());
                System.out.println();
            }
        }
    }

    /**
     * Run a single generation of the EA on the given parent population.
     * 
     * @param parents
     *            parent population
     * @param client
     *            client
     * @return new population
     */
    private Population newGeneration(Population parents, Client client) {
        Population offspring = crossover.crossover(parents, CROSSOVER_RATE);
        offspring = mutation.mutate(offspring);
        for (Individual individual : offspring) {
            individual.setFitness(client.evaluateFitness(individual));
        }
        return selection.select(parents, offspring);
    }

    public int getPopSize() {
        return popSize;
    }

    public void setPopSize(int popSize) {
        this.popSize = popSize;
    }

    public int getOffSize() {
        return offSize;
    }

    public void setOffSize(int offSize) {
        this.offSize = offSize;
    }

    public IMutation getMutation() {
        return mutation;
    }

    public void setMutation(IMutation mutation) {
        this.mutation = mutation;
    }

    public ICrossover getCrossover() {
        return crossover;
    }

    public void setCrossover(ICrossover crossover) {
        this.crossover = crossover;
    }

    public ISelection getSelection() {
        return selection;
    }

    public void setSelection(ISelection selection) {
        this.selection = selection;
    }
}
