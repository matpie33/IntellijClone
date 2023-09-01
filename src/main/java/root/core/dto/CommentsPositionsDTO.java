package root.core.dto;

import java.util.ArrayList;
import java.util.List;

public class CommentsPositionsDTO {

    private List<TokenPositionDTO> commentedCodeSections = new ArrayList<>();


    public void addCommentSection(TokenPositionDTO tokenPositionDTO){
        commentedCodeSections.add(tokenPositionDTO);
    }


    public List<TokenPositionDTO> getCommentedCodeSections() {
        return commentedCodeSections;
    }

}
