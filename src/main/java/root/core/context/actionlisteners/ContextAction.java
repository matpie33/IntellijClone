package root.core.context.actionlisteners;

import javax.swing.*;

public abstract class ContextAction<T> extends AbstractAction {

     public abstract void setContext(T context);


}
