package machine_learning.classifiers.tuned.progressive;

import tsml.classifiers.EnhancedAbstractClassifier;
import tsml.classifiers.ProgressiveBuildClassifier;
import utilities.ArrayUtilities;
import weka.core.Instance;
import weka.core.Instances;

import java.util.List;
import java.util.Set;

public class IncrementalTunedClassifier extends EnhancedAbstractClassifier implements ProgressiveBuildClassifier {

    private BenchmarkIterator benchmarkIterator = new BenchmarkIterator() {
        @Override
        public boolean hasNext() {
            return false;
        }
    };
    private List<Benchmark> collectedBenchmarks;
    private BenchmarkCollector benchmarkCollector = new BestBenchmarkCollector(benchmark -> benchmark.getResults().getAcc());
    private BenchmarkEnsembler benchmarkEnsembler = BenchmarkEnsembler.byScore(benchmark -> benchmark.getResults().getAcc());
    private List<Double> ensembleWeights;

    @Override
    public boolean hasNextBuildTick() throws Exception {
        return benchmarkIterator.hasNext();
    }

    @Override
    public void nextBuildTick() throws Exception {
        Set<Benchmark> nextBenchmarks = benchmarkIterator.next();
        collectedBenchmarks.removeAll(nextBenchmarks);
        collectedBenchmarks.addAll(nextBenchmarks);
    }

    @Override
    public void finishBuild() throws Exception {
        collectedBenchmarks = benchmarkCollector.getCollectedBenchmarks();
        if(collectedBenchmarks.isEmpty()) {
            throw new IllegalStateException("no benchmarks");
        }
        ensembleWeights = benchmarkEnsembler.weightVotes(collectedBenchmarks);
    }

    @Override
    public void startBuild(Instances data) throws Exception {

    }

    public BenchmarkIterator getBenchmarkIterator() {
        return benchmarkIterator;
    }

    public void setBenchmarkIterator(BenchmarkIterator benchmarkIterator) {
        this.benchmarkIterator = benchmarkIterator;
    }

    @Override
    public double[] distributionForInstance(Instance testCase) throws Exception {
        double[] distribution = new double[numClasses];
        for(int i = 0; i < collectedBenchmarks.size(); i++) {
            Benchmark benchmark = collectedBenchmarks.get(i);
            double[] constituentDistribution = benchmark.getClassifier().distributionForInstance(testCase);
            ArrayUtilities.normaliseInPlace(constituentDistribution);
            ArrayUtilities.multiplyInPlace(constituentDistribution, ensembleWeights.get(i));
            ArrayUtilities.addInPlace(distribution, constituentDistribution);
        }
        ArrayUtilities.normaliseInPlace(distribution);
        return distribution;
    }

    @Override
    public double classifyInstance(Instance testCase) throws Exception {
        return ArrayUtilities.bestIndex(Doubles.asList(distributionForInstance(testCase)), rand);
    }
}
