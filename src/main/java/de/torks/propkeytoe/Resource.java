package de.torks.propkeytoe;

public class Resource {

    private static final EnumValueContent DEFAULT_ENUM_VALUE_CONTENT = EnumValueContent.KEY;

    String file;
    EnumValueContent enumValueContent;

    public String getFilename() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public EnumValueContent getEnumValueContent() {
        if (enumValueContent == null)
            return DEFAULT_ENUM_VALUE_CONTENT;

        return enumValueContent;
    }

    public void setEnumValueContent(EnumValueContent enumValueContent) {
        this.enumValueContent = enumValueContent;
    }

    @Override
    public String toString() {
        return "ResourceFile{" +
                "filename='" + file + '\'' +
                ", enumValueContent=" + enumValueContent +
                '}';
    }

    public enum EnumValueContent {
        KEY, VALUE
    }
}
