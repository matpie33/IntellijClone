package core.uievents;

import javax.swing.*;

public interface UIViewUpdater {

    void updateNeeded (Object data);

    JDialog getDialog ();

}
