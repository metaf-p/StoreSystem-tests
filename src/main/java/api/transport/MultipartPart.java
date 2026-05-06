package api.transport;

import org.apache.http.entity.mime.MIME;

import java.io.File;

public record MultipartPart(
        String controlName,
        String value,
        File file,
        String mimeType
) {
    public static MultipartPart field(String controlName, String value) {
        return new MultipartPart(controlName, value, null, null);
    }

    public static MultipartPart file(String controlName, File file, String mimeType) {
        return new MultipartPart(controlName, null, file, mimeType);
    }

    public boolean isFile() {
        return file != null;
    }
}
