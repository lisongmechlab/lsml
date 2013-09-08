package lisong_mechlab.model.loadout.metrics;

import lisong_mechlab.model.loadout.Loadout;

/**
 * A base class for all metrics. A metric is a derived quantity that is calculated from a {@link Loadout}.
 * 
 * @author Emily Björk
 */
public interface Metric{
   /**
    * Calculates the value of the metric. May employ caching but the caching must be transparent.
    * 
    * @return The value of the metric.
    */
   public double calculate();
}
