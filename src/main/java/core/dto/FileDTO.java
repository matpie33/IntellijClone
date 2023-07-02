package core.dto;

public class FileDTO {

    private String name;
    private String absolutePath;

    public FileDTO(String name, String absolutePath) {
        this.name = name;
        this.absolutePath = absolutePath;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }


    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "File: " +
                "name='" + name + '\'' +
                ", absolutePath='" + absolutePath+ ".";
    }
}
