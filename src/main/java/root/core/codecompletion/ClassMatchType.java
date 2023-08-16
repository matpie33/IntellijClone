package root.core.codecompletion;

public enum ClassMatchType {

    FULL_MATCH, PARTIAL_MATCH, NO_MATCH;

    public boolean isMatch(){
        return !equals(NO_MATCH);
    }

}
