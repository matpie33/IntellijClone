package core.context.actionlisteners;

import javax.swing.*;
import java.awt.event.ActionListener;

public abstract class ContextAction<T> extends AbstractAction {

     public abstract void setContext(T context);


}
