/*
 * This file is part of the UEA Time Series Machine Learning (TSML) toolbox.
 *
 * The UEA TSML toolbox is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 *
 * The UEA TSML toolbox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with the UEA TSML toolbox. If not, see <https://www.gnu.org/licenses/>.
 */
 
package tsml.classifiers.distance_based.utils.collections.params.dimensions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import tsml.classifiers.distance_based.utils.collections.params.ParamSet;
import tsml.classifiers.distance_based.utils.collections.params.ParamSpace;
import tsml.classifiers.distance_based.utils.collections.params.ParamSpaceTest;

/**
 * Purpose: // todo - docs - type the purpose of the code here
 * <p>
 * Contributors: goastler
 */
public class IndexedParameterSpaceTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNonDiscreteParameterDimensionException() {
        IndexedParameterSpace space = new IndexedParameterSpace(new ParamSpaceTest().buildLParams());
        for(int i = 0; i < space.size(); i++) {
            space.get(i);
        }
    }

    @Test
    public void testUniquePermutations() {
        IndexedParameterSpace space = new IndexedParameterSpace(new ParamSpaceTest().buildWParams());
        int size = space.size();
        Set<ParamSet> paramSets = new HashSet<>();
        for(int i = 0; i < size; i++) {
            ParamSet paramSet = space.get(i);
            boolean added = paramSets.add(paramSet);
//            System.out.println(paramSet);
            if(!added) {
                Assert.fail("duplicate parameter set: " + paramSet);
            }
        }
    }

    @Test
    public void testEquals() {
        ParamSpace wParams = new ParamSpaceTest().buildWParams();
        IndexedParameterSpace a = new IndexedParameterSpace(wParams);
        IndexedParameterSpace b = new IndexedParameterSpace(wParams);
        ParamSpace alt = new ParamSpace();
        alt.add("letters", new DiscreteParameterDimension<>(Arrays.asList("a", "b", "c")));
        IndexedParameterSpace c = new IndexedParameterSpace(alt);
        IndexedParameterSpace d = new IndexedParameterSpace(alt);
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
        Assert.assertEquals(c, d);
        Assert.assertEquals(c.hashCode(), c.hashCode());
        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(a.hashCode(), c.hashCode());
        Assert.assertNotEquals(b, c);
        Assert.assertNotEquals(b.hashCode(), c.hashCode());
        Assert.assertNotEquals(a, d);
        Assert.assertNotEquals(a.hashCode(), d.hashCode());
        Assert.assertNotEquals(b, d);
        Assert.assertNotEquals(b.hashCode(), d.hashCode());
    }
}
