package matrice.alpha.Action;

import matrice.alpha.Cellular.CelluGroup;
import matrice.alpha.Cellular.Node;
import matrice.alpha.Complexity.Environment;

/**
 *
 * Created by Frapo on 2017/1/20.
 *
 */
public class Respire implements IAction{
    public Object apply(Object param,Environment e){
        // Changes A into trash, energy , synergy
        Node[] x = new Node[1];
        if(param instanceof CelluGroup){
            x = ((CelluGroup)param).getArea();
        }
        // Increase Energy(Electricity(Electro-Energy) And Temperature / Entropy)
        // Speed ---<--- Collision Rate ----<---- Pressure / Electro Activism
        return x;
    }

    public static Object Collide(Object param){
        // Collide - > -
        if(param instanceof CelluGroup){
            Node[] x = ((CelluGroup)param).getArea();
        }
        return null;
    }
}
