package org.esa.beam.operator;

import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;

/**
 * Operator for preparation/modification of Diversity Actual Evapotranspiration auxdata
 *
 * @author olafd
 */
@OperatorMetadata(alias = "Diversity.Auxdata.Evapo", version = "1.0",
                  authors = "Olaf Danne",
                  copyright = "(c) 2013 Brockmann Consult",
                  internal = true,
                  description = "Operator for preparation/modification of Diversity Actual Evapotranspiration auxdata.")
public class ActualEvapoOp extends Operator
{
    @Override
    public void initialize() throws OperatorException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(ActualEvapoOp.class);
        }
    }
}
