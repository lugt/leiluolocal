package matrice.alpha.Action;
import matrice.alpha.Complexity.Environment;

/**
 * Created by Frapo on 2017/1/20.
 */
public interface IAction {
    Object apply(Object param, Environment env);
}
