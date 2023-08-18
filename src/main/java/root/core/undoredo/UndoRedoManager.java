package root.core.undoredo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import root.core.dto.*;

import java.util.LinkedList;
import java.util.Optional;

@Component
@Scope("prototype")
public class UndoRedoManager {

    private LinkedList<TextChangeDTO> actionsDone = new LinkedList<>();
    private LinkedList<TextChangeDTO> actionsUndone = new LinkedList<>();

    private InsertChangeDTO insertChangeDTO;

    private RemoveChangeDTO removeChangeDTO;

    public void clearChanges (){
        insertChangeDTO = null;
        removeChangeDTO = null;
    }

    public void addCurrentChangesToList (){
        addCurrentInsertionsToList();
        addCurrentRemovalsToList();
    }

    private void addCurrentInsertionsToList(){
        if (insertChangeDTO != null){
            addNewChange(insertChangeDTO);
            System.out.println(insertChangeDTO);
            insertChangeDTO = null;
        }

    }

    private void addCurrentRemovalsToList (){
        if (removeChangeDTO != null){
            addNewChange(removeChangeDTO);
            System.out.println(removeChangeDTO);
            removeChangeDTO = null;
        }
    }

    public void createImportInsertChange (int offsetForImport, String fullyQualifiedClassName){
        insertChangeDTO = new InsertImportChangeDTO(offsetForImport, fullyQualifiedClassName);
    }

    public void handleInsertChange(String textAdded, int offset){
        addCurrentRemovalsToList();
        if (insertChangeDTO == null){
            insertChangeDTO = new InsertChangeDTO(offset);
        }
        insertChangeDTO.appendText(textAdded);
    }

    public void handleRemoveChange (String removedText, int offset){
        addCurrentInsertionsToList();
        if (removeChangeDTO == null){
            removeChangeDTO = new RemoveChangeDTO(offset);
        }
        removeChangeDTO.appendText(removedText);
    }


    public void addNewChange(TextChangeDTO fileEditDTO) {
        if (!actionsUndone.isEmpty()){
            actionsUndone.clear();
            actionsDone.clear();
        }
        actionsDone.addFirst(fileEditDTO);
    }

    public TextChangeDTO getNextUndoAction(){
            return Optional.ofNullable(actionsDone.pollFirst()).map(action->{actionsUndone.addFirst(action); return action;}).orElse(new NoChangeDTO());
    }

    public TextChangeDTO getNextRedoAction(){
        return Optional.ofNullable(actionsUndone.pollFirst()).map(action->{actionsDone.addFirst(action); return action;}).orElse(new NoChangeDTO());


    }

}
