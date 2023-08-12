package root.core.undoredo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import root.core.dto.EmptyChangeDTO;
import root.core.dto.TextChangeDTO;

import java.util.LinkedList;
import java.util.Optional;

@Component
@Scope("prototype")
public class UndoRedoManager {

    private LinkedList<TextChangeDTO> actionsDone = new LinkedList<>();
    private LinkedList<TextChangeDTO> actionsUndone = new LinkedList<>();

    public void addNewChange(TextChangeDTO fileEditDTO) {
        if (!actionsUndone.isEmpty()){
            actionsUndone.clear();
            actionsDone.clear();
        }
        actionsDone.addFirst(fileEditDTO);
    }

    public TextChangeDTO getNextUndoAction(){
            return Optional.ofNullable(actionsDone.pollFirst()).map(action->{actionsUndone.addFirst(action); return action;}).orElse(new EmptyChangeDTO());
    }

    public TextChangeDTO getNextRedoAction(){
        return Optional.ofNullable(actionsUndone.pollFirst()).map(action->{actionsDone.addFirst(action); return action;}).orElse(new EmptyChangeDTO());


    }

}
