package lisong_mechlab.model.tables;

import java.util.ArrayList;

import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Weapon;

public class MissileEntry{
   
    private ArrayList<Double> damageList;
    private ArrayList<Double> coolDownList;
    private int volleyAmount;
    private Ammunition ammoType;

   public MissileEntry(Ammunition theType){
      damageList = new ArrayList<>();
      coolDownList = new ArrayList<>();
      volleyAmount = 0;
      ammoType = theType;
   }
   
   public void addAnotherWeapon(Weapon weapon){
      volleyAmount = weapon.getAmmoPerPerShot();
      damageList.add(weapon.getDamagePerShot());
      coolDownList.add(weapon.getSecondsPerShot());      
   }

   public int getVolleyTotal(){
      // TODO Auto-generated method stub
      return volleyAmount;
   }

   public Ammunition getAmmoType(){
      // TODO Auto-generated method stub
      return ammoType;
   }
   
   public double getDamageAverage(){
      Double total = (double)0;
      if(damageList.isEmpty()) return (double)0;
      for(Double inter : damageList){
         total += inter;
      }
      return total / volleyAmount;
   }
   
   public double getCooldownAverage(){
      Double total = (double)0;
      if(coolDownList.isEmpty()) return 0;
      for(Double inter : coolDownList){
         total = inter + total;
      }
      return total / coolDownList.size();
   }

}
