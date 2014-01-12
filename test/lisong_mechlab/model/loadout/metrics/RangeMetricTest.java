package lisong_mechlab.model.loadout.metrics;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.util.WeaponRanges;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RangeMetricTest{
   class ConcreteAbstractCut extends RangeMetric{
      public ConcreteAbstractCut(Loadout aLoadout){
         super(aLoadout);
      }

      @Override
      public double calculate(double aRange){
         return 0;
      }
   }

   @Mock
   private Loadout             loadout;
   private ConcreteAbstractCut cut;
   private List<Item>          items = new ArrayList<>();

   @Before
   public void startup(){
      Mockito.when(loadout.getAllItems()).thenReturn(items);
      cut = Mockito.spy(new ConcreteAbstractCut(loadout));
   }

   /**
    * After a call to {@link RangeMetric#calculate()}, {@link RangeMetric#getRange()} should return the range for which
    * {@link RangeMetric#calculate(double)} returned the highest value of the ranges determined by the weapons on the
    * loadout.
    */
   @Test
   public final void testGetRange(){
      // Should give ranges: 0, 270, 450, 540, 900
      items.add(ItemDB.lookup("MEDIUM LASER"));
      items.add(ItemDB.lookup("LARGE LASER"));

      Mockito.when(cut.calculate(Matchers.anyDouble())).thenReturn(0.0);
      Mockito.when(cut.calculate(270.0)).thenReturn(1.0);
      Mockito.when(cut.calculate(450.0)).thenReturn(3.0);
      Mockito.when(cut.calculate(540.0)).thenReturn(2.0);
      Mockito.when(cut.calculate(900.0)).thenReturn(1.0);

      cut.calculate();

      assertEquals(450.0, cut.getRange(), 0.0);
   }

   /**
    * If {@link RangeMetric#changeRange(double)} has not been called; a call to {@link RangeMetric#calculate()} should
    * return the maximum value of {@link RangeMetric#calculate(double)} for all the ranges returned by
    * {@link WeaponRanges#getRanges(Loadout)}.
    */
   @Test
   public final void testCalculate_noChangeRange() throws Exception{
      // Should give ranges: 0, 270, 450, 540, 900
      items.add(ItemDB.lookup("MEDIUM LASER"));
      items.add(ItemDB.lookup("LARGE LASER"));

      Mockito.when(cut.calculate(Matchers.anyDouble())).thenReturn(0.0);
      Mockito.when(cut.calculate(270.0)).thenReturn(1.0);
      Mockito.when(cut.calculate(450.0)).thenReturn(3.0);
      Mockito.when(cut.calculate(540.0)).thenReturn(2.0);
      Mockito.when(cut.calculate(900.0)).thenReturn(1.0);

      assertEquals(3.0, cut.calculate(), 0.0);
   }

   /**
    * If {@link RangeMetric#changeRange(double)} was last called with a negative or zero argument; a call to
    * {@link RangeMetric#calculate()} should return the maximum value of {@link RangeMetric#calculate(double)} for all
    * the ranges returned by {@link WeaponRanges#getRanges(Loadout)}.
    */
   @Test
   public final void testCalculate_negativeChangeRange() throws Exception{
      cut.changeRange(10.0);
      cut.changeRange(-1.0);

      testCalculate_noChangeRange();
   }

   /**
    * A call to {@link RangeMetric#calculate()} after {@link RangeMetric#changeRange(double)} has been called with a
    * positive, non-negative argument should return the value of {@link RangeMetric#calculate(double)} called with the
    * same argument as {@link RangeMetric#changeRange(double)} was called.
    */
   @Test
   public final void testCalculate_changeRange() throws Exception{
      double range = 20.0;

      Mockito.when(cut.calculate(Matchers.anyDouble())).thenReturn(0.0);
      Mockito.when(cut.calculate(range)).thenReturn(1.0);

      cut.changeRange(range);
      assertEquals(1.0, cut.calculate(), 0.0);
   }

}
