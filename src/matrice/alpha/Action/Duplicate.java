package matrice.alpha.Action;

import matrice.alpha.Cellular.CelluGroup;
import matrice.alpha.Cellular.Node;
import matrice.alpha.Complexity.Environment;

/**
 * Created by Frapo on 2017/1/22.
 */
public class Duplicate implements IAction {

    @Override
    public Object apply(Object param, Environment env) {
        // Object param
        Node[] x = ((CelluGroup)param).getArea();
        // CelluGroup
        return null;
    }
}
