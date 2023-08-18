package root.core.configuration;

public interface ConfigurationExtractor<T> {

    String getConfigKey();

    boolean isNewValue();

    ConfigurationHolderType getType();

    void extractConfigValue(String configurationValue);

    void saveValue(String configurationValue);

    String createConfigValue();

    T getValue();

    void handleConfigValueNotPresent();
}
