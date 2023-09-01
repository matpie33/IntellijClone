package root.core.utility;

import org.springframework.stereotype.Component;
import root.core.dto.CommentsPositionsDTO;
import root.core.dto.TokenPositionDTO;

@Component
public class CommentsHandler {

    private boolean isInMultilineCommentSection;

    public static final String SINGLE_COMMENT_START = "//";
    public static final String MULTILINE_COMMENT_START = "/*";
    public static final String MULTILINE_COMMENT_END_SIGN = "*/";

    public CommentsPositionsDTO findCommentSections(int offsetToAddText, String textToAdd) {
        CommentsPositionsDTO commentsPositionsDTO = new CommentsPositionsDTO();
        handleSingleLineComment(offsetToAddText, textToAdd, commentsPositionsDTO);
        handleMultilineComments(offsetToAddText, textToAdd, commentsPositionsDTO);
        return commentsPositionsDTO;
    }

    private TokenPositionDTO getTokenPosition(int offset, String textToAdd, int indexOfComment) {
        int startOfComment = offset + indexOfComment;
        int length = textToAdd.length() - indexOfComment;
        return new TokenPositionDTO(startOfComment, length);
    }

    public boolean isInMultilineCommentSection() {
        return isInMultilineCommentSection;
    }

    private void handleMultilineComments(int offsetToAddText, String textToAdd, CommentsPositionsDTO commentsPositionsDTO) {

        int indexOfCommentStart = textToAdd.indexOf(MULTILINE_COMMENT_START);
        int textLength = textToAdd.length();
        handleLineWithOnlyMultilineCommentEndSign(offsetToAddText, textToAdd, commentsPositionsDTO, indexOfCommentStart);
        handleMultipleCommentSectionsInSingleLine(offsetToAddText, textToAdd, commentsPositionsDTO, indexOfCommentStart, textLength);
    }

    private void handleMultipleCommentSectionsInSingleLine(int offsetToAddText, String textToAdd, CommentsPositionsDTO commentsPositionsDTO, int indexOfCommentStart, int textLength) {
        while (indexOfCommentStart != -1){
            int indexOfCommentEnd = textToAdd.indexOf(MULTILINE_COMMENT_END_SIGN, indexOfCommentStart);
            TokenPositionDTO tokenPositionDTO;

            if (indexOfCommentEnd !=-1){
                tokenPositionDTO = new TokenPositionDTO(offsetToAddText + indexOfCommentStart, indexOfCommentEnd + MULTILINE_COMMENT_END_SIGN.length() - indexOfCommentStart);
                commentsPositionsDTO.addCommentSection(tokenPositionDTO);
            }
            else{
                tokenPositionDTO = new TokenPositionDTO(offsetToAddText + indexOfCommentStart, textLength - indexOfCommentStart);
                commentsPositionsDTO.addCommentSection(tokenPositionDTO);
                isInMultilineCommentSection = true;
                break;
            }
            indexOfCommentStart = textToAdd.indexOf(MULTILINE_COMMENT_START, indexOfCommentEnd);

        }
    }

    private void handleLineWithOnlyMultilineCommentEndSign(int offsetToAddText, String textToAdd, CommentsPositionsDTO commentsPositionsDTO, int indexOfCommentStart) {
        if (indexOfCommentStart == -1){
            int indexOfCommentEnd = textToAdd.indexOf(MULTILINE_COMMENT_END_SIGN);
            if (indexOfCommentEnd !=-1){
                TokenPositionDTO commentPosition = new TokenPositionDTO(offsetToAddText, indexOfCommentEnd + MULTILINE_COMMENT_END_SIGN.length());
                commentsPositionsDTO.addCommentSection(commentPosition);
                isInMultilineCommentSection = false;
            }
        }
    }

    private void handleSingleLineComment(int offsetToAddText, String textToAdd, CommentsPositionsDTO commentsPositionsDTO) {
        int indexOfCommentStart = textToAdd.indexOf(SINGLE_COMMENT_START);
        if (indexOfCommentStart != -1){
            TokenPositionDTO commentPosition = getTokenPosition(offsetToAddText, textToAdd, indexOfCommentStart);
            commentsPositionsDTO.addCommentSection(commentPosition);

        }

    }

}
