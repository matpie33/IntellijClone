package root.core.context.conditionalmenu;


public interface ConditionChecker<T> {

    boolean isConditionFulfilled (T context);

}

