package classifiers.distance_based.elastic_ensemble.iteration;

import evaluation.tuning.ParameterSet;
import evaluation.tuning.ParameterSpace;

public class ParameterSetIterator extends AbstractIterator<ParameterSet> {
    private final ParameterSpace parameterSpace;
    private final AbstractIterator<Integer> iterator;

    public ParameterSetIterator(final ParameterSpace parameterSpace,
                                final AbstractIterator<Integer> iterator) {
        this.parameterSpace = parameterSpace;
        this.iterator = iterator;
    }

    public ParameterSetIterator(ParameterSetIterator other) {
        this(other.parameterSpace, other.iterator.iterator()); // todo need to copy param space
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    @Override
    public void add(final ParameterSet parameterSet) {
        parameterSpace.addParameter(parameterSet);
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public ParameterSet next() {
        return parameterSpace.get(iterator.next());
    }

    @Override
    public ParameterSetIterator iterator() {
        return new ParameterSetIterator(parameterSpace, iterator.iterator());
    }
}
