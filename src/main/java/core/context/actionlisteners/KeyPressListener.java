package core.context.actionlisteners;

import core.context.providers.ContextProvider;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class KeyPressListener<T> extends AbstractAction {

    private ContextAction<T> contextAction;

    private ContextProvider<T> contextProvider;


    public KeyPressListener(ContextAction<T> contextAction, ContextProvider<T> contextProvider) {
        this.contextAction = contextAction;
        this.contextProvider = contextProvider;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        T context = contextProvider.getContext(e);
        contextAction.setContext(context);
        contextAction.actionPerformed(e);
    }
}
